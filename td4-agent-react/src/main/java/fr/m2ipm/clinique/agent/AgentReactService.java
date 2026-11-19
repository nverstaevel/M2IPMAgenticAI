package fr.m2ipm.clinique.agent;

import fr.m2ipm.clinique.ai.OutilsClinique;
import fr.m2ipm.clinique.externe.OutilsExternes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Boucle ReAct écrite à la main : le ChatModel est appelé directement et ne lance jamais les outils
 * (internalToolExecutionEnabled=false) ; c'est cette classe qui exécute les outils, compte, trace et décide de s'arrêter.
 */
@Service
public class AgentReactService {

    public static final String PROMPT_SYSTEME = """
            Tu es l'agent de la clinique des créatures fantastiques. Tu accomplis la demande en enchaînant des outils.
            Avant CHAQUE appel d'outil, écris une phrase qui dit ce que tu cherches et pourquoi.
            N'invente jamais un identifiant, un tarif, un stock ou un créneau : obtiens-les avec un outil.
            Si un outil ne trouve rien, ou si la demande est impossible ou absurde, dis-le et demande une précision.
            Tu ne peux que PROPOSER des consultations ou des événements : un membre du personnel confirme.
            Quand tu as tout ce qu'il faut, réponds à l'utilisateur sans appeler d'outil.
            """;

    static final int LONGUEUR_RESULTAT = 500;

    private final ChatModel modele;
    private final ToolCallback[] outils;
    private final TraceStore traces;
    private final ToolCallingManager gestionnaire = ToolCallingManager.builder().build();
    private final int budgetTokens;

    @Autowired
    public AgentReactService(ChatModel modele, OutilsClinique clinique, OutilsExternes externes, TraceStore traces,
                             @Value("${agent.budget-tokens:20000}") int budgetTokens) {
        this(modele, ToolCallbacks.from(clinique, externes), traces, budgetTokens);
    }

    /** Constructeur de test : outils quelconques, modèle scripté. */
    public AgentReactService(ChatModel modele, ToolCallback[] outils, TraceStore traces, int budgetTokens) {
        this.modele = modele;
        this.outils = outils;
        this.traces = traces;
        this.budgetTokens = budgetTokens;
    }

    public record Execution(String runId, ResultatAgent resultat) {}

    public Execution executer(String demande, int maxEtapes) {
        String runId = UUID.randomUUID().toString();
        return new Execution(runId, executer(runId, demande, maxEtapes));
    }

    public ResultatAgent executer(String runId, String demande, int maxEtapes) {
        ToolCallingChatOptions options = ToolCallingChatOptions.builder()
                .toolCallbacks(outils)
                .internalToolExecutionEnabled(false)
                .toolContext(Map.of("conversationId", runId))
                .build();
        Prompt prompt = new Prompt(List.<Message>of(new SystemMessage(PROMPT_SYSTEME), new UserMessage(demande)), options);
        List<Etape> etapes = new ArrayList<>();
        int tokens = 0;
        String appelPrecedent = null;
        try {
            for (int numero = 1; ; numero++) {
                long debut = System.nanoTime();
                ChatResponse reponse = modele.call(prompt);
                AssistantMessage sortie = reponse.getResult().getOutput();
                String texte = sortie.getText() == null ? "" : sortie.getText();
                int tokensEtape = tokens(reponse, prompt, texte);
                tokens += tokensEtape;

                if (!reponse.hasToolCalls()) {
                    enregistrer(runId, etapes, new Etape(numero, texte, "", "", "", ms(debut), tokensEtape));
                    return new ResultatAgent(texte, MotifArret.REPONSE_FINALE, etapes, tokens);
                }

                var appels = sortie.getToolCalls();
                String noms = appels.stream().map(AssistantMessage.ToolCall::name).collect(Collectors.joining("+"));
                String arguments = appels.size() == 1 ? appels.get(0).arguments()
                        : appels.stream().map(AssistantMessage.ToolCall::arguments).collect(Collectors.joining(",", "[", "]"));
                String signature = noms + arguments;

                if (Objects.equals(signature, appelPrecedent)) {
                    enregistrer(runId, etapes, new Etape(numero, texte, noms, arguments,
                            "(non exécuté : même appel que l'étape précédente)", ms(debut), tokensEtape));
                    return new ResultatAgent("Arrêt : l'agent répète le même appel (" + noms + ").",
                            MotifArret.BOUCLE_DETECTEE, etapes, tokens);
                }
                appelPrecedent = signature;

                ToolExecutionResult execution = gestionnaire.executeToolCalls(prompt, reponse);
                String resultat = resultatsOutils(execution.conversationHistory());
                enregistrer(runId, etapes, new Etape(numero, texte, noms, arguments, tronquer(resultat), ms(debut), tokensEtape));

                if (tokens >= budgetTokens) {
                    return new ResultatAgent("Arrêt : budget de " + budgetTokens + " tokens atteint.",
                            MotifArret.BUDGET_TOKENS, etapes, tokens);
                }
                if (numero >= maxEtapes) {
                    return new ResultatAgent("Arrêt : plafond de " + maxEtapes + " étapes atteint.",
                            MotifArret.PLAFOND_ETAPES, etapes, tokens);
                }
                prompt = new Prompt(execution.conversationHistory(), options);
            }
        } catch (RuntimeException e) {
            return new ResultatAgent("Erreur : " + e.getMessage(), MotifArret.ERREUR, etapes, tokens);
        }
    }

    private void enregistrer(String runId, List<Etape> etapes, Etape etape) {
        etapes.add(etape);
        traces.ajouter(runId, etape);
    }

    private static String resultatsOutils(List<Message> historique) {
        Message dernier = historique.get(historique.size() - 1);
        if (dernier instanceof ToolResponseMessage retour) {
            return retour.getResponses().stream().map(ToolResponseMessage.ToolResponse::responseData)
                    .collect(Collectors.joining(" | "));
        }
        return "";
    }

    private static String tronquer(String s) {
        return s.length() > LONGUEUR_RESULTAT ? s.substring(0, LONGUEUR_RESULTAT) + "…" : s;
    }

    private static long ms(long debutNanos) {
        return (System.nanoTime() - debutNanos) / 1_000_000;
    }

    /** Tokens annoncés par le serveur ; à défaut, estimation à 1 pour 4 caractères (prompt + réponse). */
    private static int tokens(ChatResponse reponse, Prompt prompt, String texte) {
        var usage = reponse.getMetadata() == null ? null : reponse.getMetadata().getUsage();
        Integer total = usage == null ? null : usage.getTotalTokens();
        if (total != null && total > 0) {
            return total;
        }
        int caracteres = prompt.getInstructions().stream().mapToInt(m -> m.getText() == null ? 0 : m.getText().length()).sum();
        return (caracteres + texte.length() + 3) / 4;
    }
}
