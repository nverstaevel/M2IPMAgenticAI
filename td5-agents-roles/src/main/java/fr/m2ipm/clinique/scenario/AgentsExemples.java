package fr.m2ipm.clinique.scenario;

import fr.m2ipm.clinique.agent.AgentReactService;
import fr.m2ipm.clinique.agent.ComptageJetons;
import fr.m2ipm.clinique.agent.SuiviOutils;
import fr.m2ipm.clinique.ai.OutilsClinique;
import fr.m2ipm.clinique.roles.*;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agents d'exemple, référencés par les scénarios fournis. Chaque bean est un {@link Agent} : c'est le modèle à imiter pour écrire
 * les vôtres (voir SCENARIOS.md). Règles à retenir :
 * <ul>
 *   <li>un client ChatClient construit avec {@code ComptageJetons.advisor()} voit ses appels et ses tokens comptés dans le graphe ;</li>
 *   <li>des outils passés par {@code SuiviOutils.envelopper(ToolCallbacks.from(...))} apparaissent comme nœuds enfants de l'agent.</li>
 * </ul>
 */
@Configuration
public class AgentsExemples {

    // ---- boucle-critique : le duo vétérinaire / gardien du TD5 -------------------------------------------------------

    @Bean("veterinaire")
    Agent veterinaire(VeterinaireAgent veterinaire, CasBestiaireRepository cas) {
        return e -> {
            CasBestiaire c = cas(cas, e);
            ProfilActeur profil = ProfilActeur.valueOf(e.donnee("profil", "PRUDENT"));
            Diagnostic d = veterinaire.proposer(c, profil, e.retours());
            return Resultat.de("Diagnostic : " + d.diagnostic() + "\nTraitements : " + String.join(" ; ", d.traitements())
                    + "\nJustification : " + d.justification(), d);
        };
    }

    @Bean("gardien")
    Agent gardien(GardienDuBestiaire gardien, CasBestiaireRepository cas) {
        return e -> {
            CasBestiaire c = cas(cas, e);
            Diagnostic d = (Diagnostic) e.proposition().valeur();
            Critique critique = gardien.examiner(c.espece(), d);
            Decision decision = critique.verdict() == null ? Decision.DOUTE : Decision.valueOf(critique.verdict().name());
            return Resultat.avis(decision, critique.objections()).avecValeur(critique);
        };
    }

    private static CasBestiaire cas(CasBestiaireRepository cas, Entree e) {
        int id = Integer.parseInt(e.donnee("cas", "1"));
        return cas.parId(id).orElseThrow(() -> new IllegalArgumentException("Cas inconnu : " + id));
    }

    // ---- react : l'agent ReAct du TD4 possède son graphe ------------------------------------------------------------

    @Bean("react")
    Agent react(AgentReactService service) {
        return e -> {
            int max = Integer.parseInt(e.donnee("maxEtapes", "8"));
            return Resultat.texte(service.executer(e.runId(), e.texte(), max).reponse());
        };
    }

    // ---- superviseur : planificateur, travailleurs, synthèse ---------------------------------------------------------

    @Bean("planificateur")
    Agent planificateur(ChatClient.Builder builder) {
        ChatClient client = builder.clone().defaultAdvisors(ComptageJetons.advisor()).defaultSystem("""
                Tu supervises trois travailleurs : DOSSIER (dossier des créatures et des propriétaires), PHARMACIE (médicaments, stocks, tarifs)
                et AGENDA (créneaux libres, proposition d'événement). Pour la demande reçue, produis un plan : une liste ordonnée de
                sous-tâches, chacune avec le travailleur et une consigne autonome. Tu ne fais rien toi-même.
                """).build();
        return e -> {
            Plan plan = client.prompt().user(e.texte()).call().entity(Plan.class);
            return Resultat.plan(plan.sousTaches().stream().map(t -> new Tache(t.travailleur().name(), t.consigne())).toList());
        };
    }

    @Bean("travailleur-dossier")
    Agent travailleurDossier(ChatClient.Builder builder, OutilsClinique outils) {
        return travailleur(builder, "Tu es DossierWorker : tu réponds avec tes outils, sans rien inventer.", outils);
    }

    @Bean("travailleur-pharmacie")
    Agent travailleurPharmacie(ChatClient.Builder builder, OutilsPharmacie outils) {
        return travailleur(builder, "Tu es PharmacieWorker : tu réponds avec tes outils, sans rien inventer.", outils);
    }

    @Bean("travailleur-agenda")
    Agent travailleurAgenda(ChatClient.Builder builder, OutilsAgenda outils) {
        return travailleur(builder, "Tu es AgendaWorker : tu réponds avec tes outils ; tu ne fais que PROPOSER un événement.", outils);
    }

    private static Agent travailleur(ChatClient.Builder builder, String systeme, Object outils) {
        ChatClient client = builder.clone().defaultSystem(systeme)
                .defaultToolCallbacks(SuiviOutils.envelopper(ToolCallbacks.from(outils)))
                .defaultAdvisors(ComptageJetons.advisor()).build();
        return e -> Resultat.texte(client.prompt().user(e.texte()).toolContext(Map.of("conversationId", e.runId())).call().content());
    }

    @Bean("synthese")
    Agent synthese(ChatClient.Builder builder) {
        ChatClient client = builder.clone().defaultAdvisors(ComptageJetons.advisor()).build();
        return e -> Resultat.texte(client.prompt().user("Demande : " + e.texte() + "\nRéponses des travailleurs :\n"
                + e.proposition().texte() + "Rédige la réponse finale pour l'utilisateur, sans inventer d'information.").call().content());
    }
}
