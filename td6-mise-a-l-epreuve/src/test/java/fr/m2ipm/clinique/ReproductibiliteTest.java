package fr.m2ipm.clinique;

import static org.assertj.core.api.Assertions.assertThat;

import fr.m2ipm.clinique.agent.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReproductibiliteTest {

    private static ResultatAgent r(MotifArret m, int etapes, int tokens) {
        return new ResultatAgent("x", m, java.util.Collections.nCopies(etapes, new Etape(1, "", "", "", "", 0, 0)), tokens);
    }

    @Test
    void syntheseDeVingtExecutions() {
        var s = Reproductibilite.synthese(List.of(r(MotifArret.REPONSE_FINALE, 4, 100), r(MotifArret.REPONSE_FINALE, 6, 300),
                r(MotifArret.PLAFOND_ETAPES, 8, 500), r(MotifArret.BOUCLE_DETECTEE, 2, 100)));
        assertThat(s.executions()).isEqualTo(4);
        assertThat(s.reussies()).isEqualTo(2);
        assertThat(s.etapesMin()).isEqualTo(2);
        assertThat(s.etapesMax()).isEqualTo(8);
        assertThat(s.etapesMediane()).isEqualTo(5.0);
        assertThat(s.tokensMoyens()).isEqualTo(250.0);
        assertThat(s.motifs()).containsEntry(MotifArret.REPONSE_FINALE, 2L).containsEntry(MotifArret.PLAFOND_ETAPES, 1L);
    }
}
