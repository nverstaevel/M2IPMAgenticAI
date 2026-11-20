---
id: react-clinique
titre: Agent ReAct de la clinique
architecture: react
entree:
  - id: demande
    libelle: Demande
    type: texte
    defaut: "Combien coûte une consultation pour un dragon ?"
  - id: maxEtapes
    libelle: Étapes maximum
    type: texte
    defaut: "8"
agents:
  agent: react
---
Un seul agent raisonne et agit en boucle (ReAct). Il émet lui-même son graphe : une étape du modèle, puis les outils appelés.
