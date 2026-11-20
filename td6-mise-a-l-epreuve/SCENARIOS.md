# Décrire une architecture dans un fichier .md

Un scénario = un fichier `.md` : un en-tête YAML (l'architecture), puis du texte libre (le scénario). Les fichiers du dossier
`scenarios/` (à côté du `pom.xml`) sont relus à chaque actualisation de la page `/agents` : pas de redémarrage pour modifier un fichier.
Les scénarios fournis (`src/main/resources/scenarios/`) servent d'exemples ; un fichier de `scenarios/` de même `id` les remplace.
Les fichiers dont le nom commence par `_` sont ignorés (`_modele.md` est un gabarit).

## Les quatre architectures

| `architecture` | `agents:` attendus | Graphe produit |
|---|---|---|
| `react` | `agent` | l'agent possède le graphe (étapes modèle, outils) |
| `sequence` | `etapes: [a, b, c]` | a → b → c, la sortie d'une étape est l'entrée de la suivante |
| `boucle-critique` | `producteur`, `critique` ; options `maxTours` (1-10), `validationHumaine` | producteur → critique, nouveau tour si rejet |
| `superviseur` | `planificateur`, `travailleurs: {NOM: agent}`, `synthese` (facultatif) | plan → travailleurs → synthèse |

## Écrire un agent

Un agent est un bean Spring qui implémente `Agent` (une seule méthode) et est référencé **par son nom de bean** dans le `.md` :

```java
@Bean("mon-producteur")
Agent monProducteur(ChatClient.Builder builder) {
    ChatClient client = builder.clone().defaultAdvisors(ComptageJetons.advisor()).build();
    return entree -> Resultat.texte(client.prompt().user(entree.texte()).call().content());
}
```

- `Entree` : `texte()` (consigne ou production à examiner), `donnee("id", "défaut")` (champs de la page), `retours()` (remarques du critique au tour précédent), `tour()`, `proposition()` (résultat précédent, pour un critique ou une synthèse).
- `Resultat` : `Resultat.texte(..)`, `Resultat.de(texte, objet)`, `Resultat.avis(Decision.APPROUVE | REJETE | DOUTE, remarques)` pour un critique, `Resultat.plan(taches)` pour un planificateur.
- Le moteur crée le nœud de chaque appel d'agent, mesure sa durée, retente une fois un appel refusé par le fournisseur. Pour voir les **tokens** de l'agent, construisez son client avec `ComptageJetons.advisor()` ; pour voir ses **outils** comme nœuds enfants, passez-les par `SuiviOutils.envelopper(ToolCallbacks.from(outils))`.
- Besoin d'un nœud à vous (une étape de code, un calcul) : `entree.trace().noeud(genre, libelle, detail)` puis `terminer(id, ok, résultat)`.

## Observer et analyser

1. `/agents` → type « Scénario (fichier .md) » → choisir le scénario : description, champs et éventuelles erreurs du fichier s'affichent.
2. **Aperçu du graphe** : le graphe prévu (nœuds en pointillés) sans rien exécuter.
3. **Lancer et suivre** : le graphe réel se dessine en direct ; la page indique les nœuds prévus non atteints (tour inutile, travailleur non appelé) et les nœuds non prévus ; comparez aussi retours, tokens et durées.

Un agent ne peut pas dépasser 15 appels d'outils par exécution (`SuiviOutils.MAX_APPELS_OUTILS`) : au-delà, l'outil refuse et le nœud passe en erreur.
