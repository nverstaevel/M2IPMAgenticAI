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
| `td6-mise-a-l-epreuve` | Garde-fous, attaques, reproductibilité | écrit, CI verte (90 tests, modèle simulé) |

Ne pas distribuer aux étudiants avant la fin du TD concerné.

## Ouvrir les projets dans Spring Tools (Eclipse) ou IntelliJ

Le dépôt n'est pas un projet unique : chaque dossier `tdN-…` est un projet Maven autonome, et le `pom.xml` à la racine n'est qu'un agrégateur.

- **Spring Tools / Eclipse** : `File > Import > Maven > Existing Maven Projects`, racine = le dossier cloné, cocher les projets voulus (ou un seul `tdN-…`). Chaque TD contient aussi un `.project` et un `.classpath` : `File > Import > General > Existing Projects into Workspace` fonctionne alors dans **le dossier d'un TD** (ou à la racine, en cochant les six), puis faire `Maven > Update Project…`.
- **Java** : JDK 21 requis (`Window > Preferences > Java > Installed JREs`). Spring Boot 4 demande une version récente de Spring Tools (Eclipse 2025 ou plus).
- **IntelliJ** : ouvrir le `pom.xml` racine (ou celui d'un TD) comme projet.
- Après l'import : `Maven > Update Project…` (Alt+F5), cocher « Force Update ».
- Lancer : classe `CliniqueApplication` (`Run As > Spring Boot App`), ou `mvn spring-boot:run` dans le dossier du TD.

Dans tous les cas, pas de `.project` sur le dossier racine : ouvrir le dossier d'un TD, pas la racine, avec l'import « General ».

Les six projets ont des noms distincts (`clinique-td1` à `clinique-td6`) : Eclipse refuse deux projets du même nom dans un espace de travail.

## Vérification

Le workflow GitHub Actions (`.github/workflows/ci.yml`) lance `mvn verify` sur chaque dossier.
Les tests utilisent un modèle de chat simulé et un agenda simulé : aucune clé d'API ni compte Google n'est nécessaire.
