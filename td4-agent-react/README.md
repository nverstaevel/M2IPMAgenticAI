# TD4 — Agent ReAct (solution)

Cumulatif sur le TD3. **Ne pas distribuer avant la fin du TD4.**

## Ce qui est ajouté (`agent/`)

- `AgentReactService.executer(demande, maxEtapes)` : boucle écrite à la main. Le `ChatModel` est appelé directement, les outils sont exécutés par `ToolCallingManager`.
  Arrêts : `REPONSE_FINALE`, `PLAFOND_ETAPES`, `BUDGET_TOKENS` (`agent.budget-tokens`), `BOUCLE_DETECTEE` (même outil + mêmes arguments deux fois de suite : le second appel n'est pas exécuté), `ERREUR`.
- `Etape` (numéro, raisonnement, outil, arguments JSON, résultat tronqué à 500 caractères, durée, tokens) et `ResultatAgent`.
- `TraceStore` : `traces/{runId}.jsonl`, une ligne par étape (`agent.traces.dir`). Le runId est validé (pas de traversée de répertoire).
- `POST /api/agent` (`{demande, maxEtapes}` → `{runId, resultat}`) et `GET /api/agent/{runId}/trace`, **réservés au rôle PERSONNEL** (coût, et traces contenant des données du dossier).
- `POST /api/agent/framework` : mêmes outils et même prompt via `ChatClient` (boucle du framework), pour la comparaison du §5.
- Les outils d'écriture restent des propositions (TD2/TD3) ; le `runId` sert d'identifiant de conversation dans le `ToolContext`.

## Notes Spring AI 2.0

- `ToolCallingChatOptions.Builder` n'a plus `internalToolExecutionEnabled` : le modèle n'exécute jamais les outils, la boucle vit dans le `ToolCallingAdvisor`. Le repère de code du sujet reste valable.
- Tokens : valeur annoncée par le serveur si présente, sinon estimation à 1 pour 4 caractères (le modèle simulé n'en annonce pas).
- `maxTotalToolCalls` / `maxCallsPerTool` du `ToolCallingManager` : non utilisés ici (API non vérifiée en 2.0) ; c'est l'exercice de comparaison.

## Tests (54 au total, CI verte)

`AgentReactServiceTest` (sans Spring : modèle scripté, outil factice) couvre réponse finale, plafond, boucle détectée, budget, erreur, trace JSONL et croissance de l'historique ; `AgentApiTest` : chaîne complète avec le modèle simulé, droits, framework.

## Non vérifié

Comportement d'un vrai LLM sur C1–C3, variance des traces, `traces/` des neuf exécutions et `JOURNAL.md` (catalogue des échecs) : à produire par les étudiants avec Ollama/LM Studio.

## Tester avec Claude (API Anthropic)

1. Créez une clé sur la Claude Console (console.anthropic.com) ; l'API est facturée à l'usage, indépendamment de l'abonnement Claude.ai.
2. Définissez `ANTHROPIC_API_KEY` dans l'environnement de lancement (Spring Tools : `Run Configurations… > Environment`), jamais dans Git.
3. Lancez avec le profil `claude` (`mvn spring-boot:run -Dspring-boot.run.profiles=claude`). Modèle par défaut : `claude-haiku-5-5` (rapide, économique) ; `-Dspring.ai.openai.chat.model=claude-sonnet-5-5` pour plus de qualité.

Le profil passe par la couche de compatibilité OpenAI d'Anthropic, prévue pour tester et comparer, pas pour la production. Ne pas fixer `temperature` (< 1 refusé par les modèles récents) et ne pas combiner avec le profil `local`.
**Non vérifié** : aucun appel réel à l'API depuis l'environnement de rédaction.
