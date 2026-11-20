# Duo vétérinaire / gardien du bestiaire

Le vétérinaire propose un diagnostic, le gardien du bestiaire le critique. S'il rejette, le vétérinaire reprend avec les objections
(deux reprises au plus, donc trois tours) ; s'il doute, ou si le désaccord persiste, la décision part vers un humain.
Aucun traitement n'est appliqué sans validation du personnel.

## Entrées
- cas (Cas du bestiaire) : 1 = Cas 1 | 2 = Cas 2 | 3 = Cas 3
- profil (Profil du vétérinaire) : PRUDENT = Prudent | PRESSE = Pressé | OBSTINE = Obstiné

## Agents
- veterinaire : outils dossier, pharmacie
- gardien : outils bestiaire

## Graphe
entrée -> veterinaire -> gardien
gardien -> veterinaire : REJETE (max 2)
gardien -> humain : APPROUVE, DOUTE, REJETE
humain -> fin
