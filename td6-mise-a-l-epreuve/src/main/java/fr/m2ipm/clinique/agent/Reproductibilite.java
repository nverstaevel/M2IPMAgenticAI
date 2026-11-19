package fr.m2ipm.clinique.agent;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Synthèse de N exécutions du même scénario (tableau « Reproductibilité » du TD6). */
public final class Reproductibilite {

    public record Synthese(int executions, int reussies, int etapesMin, double etapesMediane, int etapesMax,
                           double tokensMoyens, Map<MotifArret, Long> motifs) {}

    private Reproductibilite() {}

    /** Réussie = arrêt sur REPONSE_FINALE ; la qualité de la réponse reste à juger sur la trace. */
    public static Synthese synthese(List<ResultatAgent> resultats) {
        if (resultats.isEmpty()) throw new IllegalArgumentException("aucune exécution");
        List<Integer> etapes = resultats.stream().map(r -> r.etapes().size()).sorted().toList();
        int n = etapes.size();
        double mediane = n % 2 == 1 ? etapes.get(n / 2) : (etapes.get(n / 2 - 1) + etapes.get(n / 2)) / 2.0;
        Map<MotifArret, Long> motifs = new EnumMap<>(MotifArret.class);
        resultats.forEach(r -> motifs.merge(r.motif(), 1L, Long::sum));
        return new Synthese(n, (int) resultats.stream().filter(r -> r.motif() == MotifArret.REPONSE_FINALE).count(),
                etapes.get(0), mediane, etapes.stream().max(Comparator.naturalOrder()).orElse(0),
                resultats.stream().mapToInt(ResultatAgent::tokens).average().orElse(0), motifs);
    }
}
