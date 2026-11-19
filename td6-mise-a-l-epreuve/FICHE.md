# FICHE d'identité — assistant de la clinique (version corrigée, solution)

## Outils et capacités

| Outil | Nature | Qui peut l'appeler |
|---|---|---|
| `rechercherProprietaires`, `trouverProprietaire`, `creaturesDuProprietaire`, `consultationsDeLaCreature`, `soigneursPourEspece` | lecture (le téléphone n'est jamais exposé) | tout utilisateur authentifié |
| `consulterTarif`, `stockPharmacie`, `chercherMedicament`, `creneauxLibres` | lecture (Excel, base catalogue, agenda) | tout utilisateur authentifié |
| `chercherDansWikipedia` | lecture, texte externe non fiable | gardien du bestiaire (et chat) |
| `proposerConsultation`, `proposerEvenementAgenda` | **proposition** (n'écrit rien) | PERSONNEL, vérifié dans l'outil |
| confirmation d'une proposition, validation d'un dossier | **écriture**, hors modèle : endpoints REST | PERSONNEL |

Aucun outil ne confirme, ne crée ni ne valide. Chaque agent n'a que les outils de son rôle (test `GardeFousTest`).

## Protections en place

1. Droits dans les outils (`DroitsOutils`) : rôles pris dans le `ToolContext` rempli par le contrôleur ; absence de rôle = refus.
2. Contenu externe encadré, tronqué et expurgé des consignes grossières (`ContenuExterne`) : Wikipédia, colonne Unité d'Excel, titres d'événements d'agenda.
3. Bornes sur les dates proposées (`BornesPlanification`) : ni passé, ni au-delà d'un an, 9h-18h, pas de 30 minutes.
4. Plafonds : étapes (≤ 20), tokens, appels par outil (3) et au total (12), même appel répété deux fois de suite.
5. Règles dures (`ReglesDures`) derrière le gardien LLM : argent/loup-garou, ail ou soleil/vampire ne peuvent pas être APPROUVE.
6. Sorties validées : bornes sur le diagnostic, verdict absent = DOUTE, sortie illisible = renvoi à un humain.
7. Audit sans donnée inutile (`Masquage`) : téléphones et courriels masqués dans les traces et le PDF, propriétaire réduit à son initiale.
8. Filtre d'entrée grossier contre l'injection directe (`FiltreEntree`).

## Limites connues

- Filtre d'entrée et expurgation du contenu externe sont des listes de motifs : une injection reformulée passe (test `troncatureEtReformulationNonCouverte`).
- Les règles dures ne couvrent que deux contre-indications.
- Les modèles ne sont testés ici que par un simulateur : la résistance d'un vrai LLM aux attaques A1, A2, A7 n'est pas mesurée.
- `GoogleAgendaAdapter` ne remonte pas les titres d'événements (seul l'agenda simulé porte l'événement piégé) ; l'adaptateur n'a pas été exécuté contre Google.
- Les jetons sont estimés quand le serveur n'en annonce pas.

## Les trois attaques les plus craintes

1. A2 indirecte : un texte externe qui contient une consigne reformulée.
2. A7 : un gardien LLM persuadé par la justification de l'acteur.
3. A3 par un chemin non prévu : tout nouvel outil d'écriture ajouté sans contrôle de droits.
