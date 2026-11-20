package fr.m2ipm.clinique;

import static org.assertj.core.api.Assertions.assertThat;

import fr.m2ipm.clinique.agent.Evenement;
import fr.m2ipm.clinique.agent.SuiviExecution;
import fr.m2ipm.clinique.roles.*;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

/** Le graphe du duo : tours du vétérinaire et du gardien, puis validation humaine en attente. */
class SuiviRolesTest {

    static final CasBestiaire CAS = new CasBestiaire(1, "Lunatique", "Loup-garou", "Clairdelune", "insomnie",
            "compléments à base d'argent", Verdict.REJETE);

    @Test
    void leDuoProduitUnGrapheAvecValidationHumaine() {
        var suivi = new SuiviExecution();
        var duo = new DuoService(new VeterinaireAgent(ChatClient.create(new ModeleTexte(DuoServiceTest.DIAG.formatted("argent")))),
                new GardienDuBestiaire(ChatClient.create(new ModeleTexte(DuoServiceTest.REJETE, DuoServiceTest.APPROUVE))), Clock.systemUTC(), "test", suivi);
        var run = duo.traiter("run-duo-1", CAS, ProfilActeur.PRESSE);
        var evts = suivi.evenements("run-duo-1").orElseThrow();
        assertThat(evts).extracting(Evenement::id).contains("cas", "v1", "g1", "humain", "fin");
        assertThat(evts.stream().filter(e -> "g1".equals(e.id())).reduce((a, b) -> b).orElseThrow().statut()).isEqualTo(Evenement.ERREUR);
        assertThat(evts.stream().filter(e -> "humain".equals(e.id())).findFirst().orElseThrow().statut()).isEqualTo(Evenement.ATTENTE);
        assertThat(run.tours()).isNotEmpty();
    }

    /** Un travailleur dont l'appel échoue deux fois (appel d'outil rejeté par le fournisseur) ne doit pas faire échouer tout le plan. */
    @Test
    void leSuperviseurPoursuitAvecUnResultatPartielSiUnTravailleurEchoue() {
        var suivi = new SuiviExecution();
        var plan = "{\"sousTaches\":[{\"travailleur\":\"DOSSIER\",\"consigne\":\"dossier\"},{\"travailleur\":\"PHARMACIE\",\"consigne\":\"stock\"}]}";
        var superviseur = ChatClient.create(new ModeleTexte(plan, "Synthèse partielle."));
        var casse = new org.springframework.ai.chat.model.ChatModel() {
            @Override public org.springframework.ai.chat.model.ChatResponse call(org.springframework.ai.chat.prompt.Prompt p) {
                throw new IllegalStateException("400: tool call validation failed");
            }
        };
        var travailleurs = new java.util.EnumMap<Plan.Travailleur, ChatClient>(Plan.Travailleur.class);
        travailleurs.put(Plan.Travailleur.DOSSIER, ChatClient.create(casse));
        travailleurs.put(Plan.Travailleur.PHARMACIE, ChatClient.create(new ModeleTexte("Stock : 12.")));
        var agent = new SuperviseurAgent(superviseur, travailleurs, new fr.m2ipm.clinique.agent.TraceStore(
                java.nio.file.Path.of(System.getProperty("java.io.tmpdir"), "traces-test-sup")), suivi);
        var r = agent.executer("run-sup-1", "demande");
        assertThat(r.synthese()).isEqualTo("Synthèse partielle.");
        var evts = suivi.evenements("run-sup-1").orElseThrow();
        assertThat(evts.stream().filter(e -> "t1".equals(e.id())).reduce((a, b) -> b).orElseThrow().statut()).isEqualTo(Evenement.ERREUR);
        assertThat(evts.stream().filter(e -> "t2".equals(e.id())).reduce((a, b) -> b).orElseThrow().statut()).isEqualTo(Evenement.OK);
        assertThat(evts.stream().filter(e -> "fin".equals(e.id())).findFirst().orElseThrow().statut()).isEqualTo(Evenement.OK);
    }
}
