package fr.m2ipm.clinique.scenario;

import java.util.List;
import java.util.Map;

/** Un scénario lu dans un fichier .md : en-tête YAML (architecture, agents, options, entrées) + texte Markdown. */
public record ScenarioDef(String id, String titre, String architecture, List<ChampEntree> entree, Map<String, Object> agents,
                          Map<String, Object> options, String description, String fichier, List<String> erreurs) {

    public ScenarioDef avecErreurs(List<String> e) {
        return new ScenarioDef(id, titre, architecture, entree, agents, options, description, fichier, List.copyOf(e));
    }

    public boolean valide() { return erreurs.isEmpty(); }
}
