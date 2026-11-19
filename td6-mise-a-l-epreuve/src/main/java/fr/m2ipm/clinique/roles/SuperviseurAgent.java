package fr.m2ipm.clinique.roles;

import fr.m2ipm.clinique.agent.Etape;
import fr.m2ipm.clinique.agent.TraceStore;
import fr.m2ipm.clinique.ai.OutilsClinique;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Orchestrateur-workers minimal : le superviseur planifie (sortie structurée, aucun outil), confie chaque sous-tâche
 * à un travailleur qui n'a que ses outils, puis assemble. Chaque appel de travailleur est une étape de la trace.
 * Le superviseur n'a aucun outil : il ne peut pas écrire dans l'agenda.
 */
@Component
public class SuperviseurAgent {

    static final String SYSTEME_SUPERVISEUR = """
            [ROLE:SUPERVISEUR]
            Tu supervises trois travailleurs : DOSSIER (dossier des créatures et des propriétaires), PHARMACIE (médicaments, stocks, tarifs)
            et AGENDA (créneaux libres, proposition d'événement). Pour la demande reçue, produis un plan : une liste ordonnée de
            sous-tâches, chacune avec le travailleur et une consigne autonome. Tu ne fais rien toi-même.
            """;

    public record Resultat(String runId, Plan plan, List<Etape> etapes, String synthese) {}

    private final ChatClient superviseur;
    private final Map<Plan.Travailleur, ChatClient> travailleurs = new EnumMap<>(Plan.Travailleur.class);
    private final TraceStore traces;

    /** Chaque client part d'un clone du builder : sinon les prompts et outils des uns fuiteraient chez les autres. */
    @Autowired
    public SuperviseurAgent(ChatClient.Builder builder, OutilsClinique dossier, OutilsPharmacie pharmacie,
                            OutilsAgenda agenda, TraceStore traces) {
        this(builder.clone().defaultSystem(SYSTEME_SUPERVISEUR).build(),
                Map.of(Plan.Travailleur.DOSSIER, builder.clone().defaultSystem("Tu es DossierWorker : tu réponds avec tes outils, sans rien inventer.").defaultTools(dossier).build(),
                        Plan.Travailleur.PHARMACIE, builder.clone().defaultSystem("Tu es PharmacieWorker : tu réponds avec tes outils, sans rien inventer.").defaultTools(pharmacie).build(),
                        Plan.Travailleur.AGENDA, builder.clone().defaultSystem("Tu es AgendaWorker : tu réponds avec tes outils ; tu ne fais que PROPOSER un événement.").defaultTools(agenda).build()),
                traces);
    }

    public SuperviseurAgent(ChatClient superviseur, Map<Plan.Travailleur, ChatClient> travailleurs, TraceStore traces) {
        this.superviseur = superviseur;
        this.travailleurs.putAll(travailleurs);
        this.traces = traces;
    }

    public Resultat executer(String demande) {
        String runId = UUID.randomUUID().toString();
        Plan plan = superviseur.prompt().user(demande).call().entity(Plan.class);
        List<Etape> etapes = new ArrayList<>();
        StringBuilder reponses = new StringBuilder();
        int numero = 0;
        for (Plan.SousTache t : plan.sousTaches()) {
            numero++;
            long debut = System.nanoTime();
            String reponse = travailleurs.get(t.travailleur()).prompt().user(t.consigne())
                    .toolContext(Map.of("conversationId", runId, "roles", "ROLE_PERSONNEL")).call().content();
            String r = reponse == null ? "" : reponse;
            var etape = new Etape(numero, "Délégation à " + t.travailleur() + "Worker", t.travailleur() + "Worker",
                    t.consigne(), r.length() > 500 ? r.substring(0, 500) + "…" : r,
                    (System.nanoTime() - debut) / 1_000_000, (t.consigne().length() + r.length()) / 4);
            etapes.add(etape);
            traces.ajouter(runId, etape);
            reponses.append(t.travailleur()).append(" : ").append(r).append('\n');
        }
        String synthese = superviseur.prompt()
                .user("Demande : " + demande + "\nRéponses des travailleurs :\n" + reponses
                        + "Rédige la réponse finale pour l'utilisateur, sans inventer d'information.")
                .call().content();
        return new Resultat(runId, plan, etapes, synthese);
    }
}
