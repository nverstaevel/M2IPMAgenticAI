# TD2 — Un chat qui interroge l'application (corrigé)

Reprend le TD1 et y branche un modèle avec Spring AI 2.0 : `ChatClient` (Dr Gribouille), mémoire de conversation,
cinq outils de lecture, sortie structurée, et une écriture sous confirmation humaine.

## Lancer avec un vrai modèle

Deux starters Spring AI sont présents ; `spring.ai.model.chat` choisit celui qui est actif.

| Serveur | Commande | Notes |
|---|---|---|
| **Ollama** (défaut) | `ollama pull qwen2.5:7b` puis `mvn spring-boot:run` | propriétés `spring.ai.ollama.*` |
| **LM Studio** ou tout serveur compatible OpenAI | `mvn spring-boot:run -Dspring-boot.run.profiles=lmstudio` | propriétés `spring.ai.openai.*`, URL de base `http://localhost:1234/v1` |

Le modèle doit gérer l'appel d'outils (tool calling). Pour changer de modèle ou de port sans toucher au dépôt :
copier `application-local.yml.example` en `application-local.yml` (ignoré par Git) et ajouter le profil `local`
(`-Dspring-boot.run.profiles=lmstudio,local`). Une API hébergée s'utilise comme LM Studio, avec son URL et sa clé
dans une variable d'environnement (`OPENAI_API_KEY`), jamais dans Git ; surveillez le budget.
Application : http://localhost:8080/chat (gribouille/gribouille). Profil `persistant` : voir `application-persistant.properties`.

## Lancer sans modèle (profil `simule`)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=simule
```

`ModeleSimule` répond de façon déterministe : « Simulé : … », un triage fixe, et `outil:NOM:{json}` déclenche
l'appel de l'outil NOM (par exemple `outil:rechercherProprietaires:{"nom":"brume"}`). Les tests l'utilisent.

## Contenu

| Élément | Fichier |
|---|---|
| Prompt système (base et variante stricte) | `ai/PromptsGribouille` |
| `ChatClient` + mémoire (fenêtre de 10 messages, dépôt JDBC) + `SimpleLoggerAdvisor` | `ai/ChatConfig` |
| Cinq outils de lecture + `proposerConsultation` | `ai/OutilsClinique` |
| Proposition → confirmation (expire à 10 min) | `ai/PropositionService`, `ChatController#confirmer` |
| Chat, flux SSE, triage structuré | `controller/ChatController`, `templates/chat.html` |
| Bouton « Effacer le contexte » | `DELETE /api/chat/{conversationId}` : vide la mémoire du modèle et les propositions en attente |
| Faux modèle | `ai/ModeleSimule` |

## Points de correction

- **Aucun outil ne confirme.** `PropositionService.confirmer` n'est appelé que par `POST /api/propositions/{id}/confirmer`,
  réservé au rôle PERSONNEL.
- **Le modèle ne voit pas le téléphone** : les outils renvoient `ProprietaireVue` (réponse à la question 3 du sujet).
- **Conflit de créneau** : `proposerConsultation` vérifie la disponibilité sans écrire ; le message d'erreur du TD1
  (« Le soigneur 1 (…) est déjà pris le 12/03 à 14h10 … ») remonte au modèle comme résultat d'outil.
- **Triage** : client séparé, sans mémoire ni outils (la mémoire exige un identifiant de conversation).
- **`ToolContext`** : l'identifiant de conversation est transmis à l'outil hors du prompt.

## Spring AI 2.0 : deux pièges rencontrés

- La boucle d'outils est dans le `ToolCallingAdvisor` du `ChatClient`, plus dans le `ChatModel`. Elle ignore toute requête dont
  les options ne sont pas des `ToolCallingChatOptions`.
- Le `ChatClient` part de `chatModel.getOptions()` (et non `getDefaultOptions()`) : un `ChatModel` maison doit surcharger `getOptions()`.

## Statut de vérification

27 tests passent en intégration continue : modèle simulé pour la chaîne complète, et deux tests qui vérifient que Ollama
(défaut) et le profil `lmstudio` câblent bien le bon `ChatModel` au démarrage, sans serveur.
**Non vérifié** (aucun serveur de modèles dans l'environnement de rédaction) : un échange réel avec un modèle, c'est-à-dire
la qualité des réponses, l'appel d'outils par un vrai modèle, le flux SSE, et l'URL exacte pour LM Studio (`/v1`).
