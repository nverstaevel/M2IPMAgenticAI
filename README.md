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

## Vérification

Le workflow GitHub Actions (`.github/workflows/ci.yml`) lance `mvn verify` sur chaque dossier.
Les tests utilisent un modèle de chat simulé et un agenda simulé : aucune clé d'API ni compte Google n'est nécessaire.
