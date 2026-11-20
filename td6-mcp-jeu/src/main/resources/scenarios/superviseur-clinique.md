# Superviseur et travailleurs

Un planificateur découpe la demande en tâches, chaque tâche est confiée à un travailleur spécialisé, puis une synthèse assemble
les réponses. Les trois travailleurs sont le même agent Java (« travailleur ») : ce qui les distingue, ce sont les outils que
le scénario leur autorise. Le planificateur connaît leurs noms : dossier, pharmacie, agenda.

## Entrées
- demande : "Quel est le dossier de Pyrrha, et combien coûte un traitement pour un dragon ?"

## Agents
- planificateur
- dossier = travailleur : outils dossier
- pharmacie = travailleur : outils pharmacie
- agenda = travailleur : outils agenda
- synthese

## Graphe
entrée -> planificateur
planificateur => dossier, pharmacie, agenda
dossier, pharmacie, agenda -> synthese -> fin
