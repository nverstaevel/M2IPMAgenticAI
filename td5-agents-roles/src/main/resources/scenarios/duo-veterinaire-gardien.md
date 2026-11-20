---
id: duo-veterinaire-gardien
titre: Duo vétérinaire / gardien du bestiaire
architecture: boucle-critique
entree:
  - id: cas
    libelle: Cas du bestiaire
    type: choix
    defaut: "1"
    options: ["1|Cas 1", "2|Cas 2", "3|Cas 3"]
  - id: profil
    libelle: Profil du vétérinaire
    type: choix
    defaut: PRUDENT
    options: ["PRUDENT|Prudent", "PRESSE|Pressé", "OBSTINE|Obstiné"]
agents:
  producteur: veterinaire
  critique: gardien
options:
  maxTours: 3
  validationHumaine: true
---
Le vétérinaire propose un diagnostic, le gardien du bestiaire le critique. S'il rejette, le vétérinaire reprend avec les objections ;
s'il doute, la décision part vers un humain. Aucun traitement n'est appliqué sans validation du personnel.
