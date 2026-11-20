# M2IPMAgenticAI — Corrigés des TD « IA agentiques avec Spring AI »

Solutions (enseignant) des six TD du cours d'introduction aux IA agentiques, M2 MIAGE IPM
(Université Toulouse Capitole). Fil rouge : la clinique des créatures fantastiques du Dr Gribouille.

Chaque dossier est un projet Maven autonome : il contient l'état complet de l'application
à la fin du TD correspondant, et le TD N reprend le code du TD N-1. Le dossier `tdN-…` est l'état de référence à la fin du TD N.

| Dossier | Contenu | Statut |
|---|---|---|
| `td1-clinique` | Application Spring Boot classique (JPA, services, API REST) | écrit, CI verte |
| `td2-chat` | Chat interrogeant l'application (ChatClient, mémoire, outils internes) | écrit, CI verte (modèle simulé) |
| `td3-outils-externes` | Wikipedia, Excel, base externe, Google Agenda, expérience de contexte | écrit, CI verte (43 tests, sources simulées) |
| `td4-agent-react` | Boucle ReAct écrite à la main, traces | écrit, CI verte (54 tests, modèle simulé) |
| `td5-agents-roles` | Actor-critic (gardien du bestiaire), dossier PDF, superviseur | écrit, CI verte (modèle simulé) |
| `jeu-bestiaire` | Le jeu « Clinique du bestiaire » (port 8090), serveur MCP fourni aux étudiants | écrit, CI verte |
| `td6-mcp-jeu` | Client MCP : Gribouille joue au jeu, architectures d'agents comparées par score | écrit, CI verte (liaison MCP réelle testée) |

Ne pas distribuer aux étudiants avant la fin du TD concerné.

## Ouvrir les projets dans Spring Tools (Eclipse) ou IntelliJ

Le dépôt n'est pas un projet unique : chaque dossier `tdN-…` est un projet Maven autonome, et le `pom.xml` à la racine n'est qu'un agrégateur.

- **Spring Tools / Eclipse** : `File > Import > Maven > Existing Maven Projects`, racine = le dossier cloné, cocher les projets voulus (ou un seul `tdN-…`). Chaque TD contient aussi un `.project` et un `.classpath` : `File > Import > General > Existing Projects into Workspace` fonctionne alors dans **le dossier d'un TD** (ou à la racine, en cochant les six), puis faire `Maven > Update Project…`.
- **Java** : JDK 21 requis (`Window > Preferences > Java > Installed JREs`). Spring Boot 4 demande une version récente de Spring Tools (Eclipse 2025 ou plus).
- **IntelliJ** : ouvrir le `pom.xml` racine (ou celui d'un TD) comme projet.
- Après l'import : `Maven > Update Project…` (Alt+F5), cocher « Force Update ».
- Lancer : classe `CliniqueApplication` (`Run As > Spring Boot App`), ou `mvn spring-boot:run` dans le dossier du TD.

Dans tous les cas, pas de `.project` sur le dossier racine : ouvrir le dossier d'un TD, pas la racine, avec l'import « General ».

Les projets de la clinique ont des noms distincts (`clinique-td1` à `clinique-td6`) : Eclipse refuse deux projets du même nom dans un espace de travail.

## Scénarios décrits en Markdown (TD5, TD6)

Chaque architecture se décrit dans un fichier `.md` (titre, agents, graphe en flèches `a -> b`, boucles, outils autorisés) lu et exécuté par la page `/agents` : voir `SCENARIOS.md` et `scenarios/_modele.md` dans `td5-agents-roles` et `td6-mcp-jeu`.

## Le TD6 : le jeu et MCP

`jeu-bestiaire` est une application Spring Boot indépendante (patients à diagnostiquer, pharmacie, trésorerie, 5 jours, score) qui publie huit outils par MCP (Streamable HTTP, `http://localhost:8090/mcp`). Les étudiants n'y touchent pas : ils branchent la clinique dessus (`td6-mcp-jeu`) et conçoivent l'architecture d'agents qui joue.

```bash
mvn -f jeu-bestiaire/pom.xml spring-boot:run                      # terminal 1 : le jeu (8090)
cd td6-mcp-jeu && mvn spring-boot:run -Dspring-boot.run.profiles=<modèle>,jeu   # terminal 2 : la clinique (8080)
curl -u gribouille:gribouille -X POST "localhost:8080/api/agent/jeu/partie?scenario=jeu-equipe&graine=42"
```

Un dépôt étudiants (code fourni à chaque TD) viendra séparément ; le jeu y sera livré sans ce corrigé.

## Vérification

Le workflow GitHub Actions (`.github/workflows/ci.yml`) lance `mvn verify` sur chaque dossier.
Les tests utilisent un modèle de chat simulé et un agenda simulé : aucune clé d'API ni compte Google n'est nécessaire.
