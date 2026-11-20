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
}
