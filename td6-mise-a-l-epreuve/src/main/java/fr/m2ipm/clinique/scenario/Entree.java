package fr.m2ipm.clinique.scenario;

import java.util.List;
import java.util.Map;

/**
 * Ce que l'orchestrateur donne à un agent.
 *
 * @param runId       identifiant de l'exécution (utile pour le contexte des outils)
 * @param texte       l'entrée principale : la demande, la consigne d'une tâche, la proposition à examiner…
 * @param donnees     les champs saisis dans la page (les « entree » du .md), tous sous forme de texte
 * @param retours     remarques du critique au tour précédent (boucle-critique), vide au premier tour
 * @param tour        numéro du tour (à partir de 1)
 * @param proposition résultat de l'agent précédent (le producteur, pour un critique ; les réponses des travailleurs, pour la synthèse)
 * @param trace       pour ajouter des nœuds au graphe
 */
public record Entree(String runId, String texte, Map<String, String> donnees, List<String> retours, int tour,
                     Resultat proposition, Trace trace) {

    public Entree {
        donnees = donnees == null ? Map.of() : Map.copyOf(donnees);
        retours = retours == null ? List.of() : List.copyOf(retours);
    }

    public String donnee(String cle, String defaut) {
        String v = donnees.get(cle);
        return v == null || v.isBlank() ? defaut : v;
    }

    Entree avecTrace(Trace t) {
        return new Entree(runId, texte, donnees, retours, tour, proposition, t);
    }
}
