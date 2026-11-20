# TD6 — Le jeu de la clinique en MCP (solution)

Cumulatif sur le TD5. **Ne pas distribuer avant la fin du TD6.**

## Ajouts

- `spring-ai-starter-mcp-client` ; `application.properties` désactive le client MCP, le profil `jeu` (`application-jeu.properties`) l'active et déclare la connexion `jeu` (`${JEU_URL:http://localhost:8090}`).
- `jeu/JeuMcp` : accès aux outils MCP du jeu (`outils()`, `connecte()`, `appeler()`, `etat()`).
- `scenario/OutilsDisponibles` : nouveau groupe d'outils **`jeu`** (les 8 outils du serveur, résolus à l'usage) ; chaque outil est aussi utilisable par son nom.
- `jeu/AgentsJeu` : agents `diagnosticien`, `gardien-jeu` (verdict structuré APPROUVE / DOUTE / REJETE, recopie le plan) et `apothicaire`.
- `jeu/JoueurService` : une partie = une exécution de scénario par journée ; la fin de journée est forcée si l'agent ne la déclenche pas ; renvoie un bilan (score, guéris, morts, trésorerie, durée).
- `jeu/JoueurController` : `GET /api/agent/jeu/outils`, `POST /api/agent/jeu/partie?scenario=&graine=`.
- Scénarios : `jeu-gribouille-seul.md` (ReAct) et `jeu-equipe.md` (diagnosticien → gardien → apothicaire, boucle de rejet bornée).

## Lancer

1. `mvn -f ../jeu-bestiaire/pom.xml spring-boot:run` (port 8090).
2. `mvn spring-boot:run -Dspring-boot.run.profiles=<modèle>,jeu` (port 8080).
3. `GET /api/agent/jeu/outils` doit répondre `connecte: true`.
4. `POST /api/agent/jeu/partie?scenario=jeu-equipe&graine=42`, puis lire les journées dans `/agents`.

Pour suivre la partie en direct : http://localhost:8090 (créatures animées, jauges de jours restants).

## Tests

`JoueurTest` (outils MCP simulés) ; `LiaisonMcpTest` joue contre un vrai serveur et n'est activé que par `-Djeu.reel=true` (la CI démarre le jeu avant). Aucun test ne nécessite un vrai modèle.

## Limites connues

Le comportement avec un vrai modèle sur le jeu n'est pas mesuré ici : les scores de référence sont à établir en séance de préparation.
