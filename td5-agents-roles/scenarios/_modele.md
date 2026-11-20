---
# Copiez ce fichier (sans le « _ » initial), renommez-le, adaptez-le : il apparaît dans /agents > « Scénario (fichier .md) ».
id: mon-scenario                 # minuscules, chiffres, tirets
titre: Mon scénario
architecture: boucle-critique    # react | sequence | boucle-critique | superviseur
entree:                          # champs saisis dans la page ; lus par vos agents avec entree.donnee("id", "défaut")
  - id: demande
    libelle: Demande
    type: texte                  # texte | texte-long | nombre | choix
    defaut: "Combien coûte une consultation pour un dragon ?"
agents:                          # noms des beans Agent (voir SCENARIOS.md)
  producteur: mon-producteur
  critique: mon-critique
options:
  maxTours: 3
  validationHumaine: true
---
Décrivez ici l'architecture et le scénario : qui fait quoi, dans quel ordre, ce que vous voulez observer et analyser.
Ce texte s'affiche dans la page quand le scénario est choisi.
