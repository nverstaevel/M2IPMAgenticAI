---
id: superviseur-clinique
titre: Superviseur et travailleurs
architecture: superviseur
entree:
  - id: demande
    libelle: Demande
    type: texte
    defaut: "Quel est le dossier de Pyrrha, et combien coûte un traitement pour un dragon ?"
agents:
  planificateur: planificateur
  travailleurs:
    DOSSIER: travailleur-dossier
    PHARMACIE: travailleur-pharmacie
    AGENDA: travailleur-agenda
  synthese: synthese
---
Un planificateur découpe la demande en tâches, chaque tâche est confiée à un travailleur spécialisé, puis une synthèse assemble les réponses.
