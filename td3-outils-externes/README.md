# TD3 — Outils reliés au monde extérieur (solution)

Projet cumulatif : tout le TD2 (chat, mémoire, propositions) + quatre sources externes et une expérience de contexte.
**Ne pas distribuer avant la fin du TD3.**

## Outils ajoutés (`externe/OutilsExternes`)

| Outil | Source | Remarques |
|---|---|---|
| `chercherDansWikipedia(titre)` | API REST de Wikipédia FR | User-Agent explicite, résumé tronqué à 1500 caractères, page absente ou d'homonymie expliquée au modèle, texte étiqueté « source externe non fiable » |
| `consulterTarif(acte, espece)` | classeur Excel (feuille Tarifs) | lu **une seule fois** au démarrage (Apache POI), recherche insensible à la casse et aux accents |
| `stockPharmacie(medicament)` | classeur Excel (feuille Pharmacie) | idem ; signale les ruptures |
| `chercherMedicament(nom, espece)` | seconde base H2 (catalogue) | second `DataSource` + `@Qualifier`, requêtes paramétrées uniquement (jamais de SQL écrit par le modèle) |
| `creneauxLibres(jour)` | agenda (`AgendaPort`) | créneaux de 30 min entre 9 h et 18 h |
| `proposerEvenementAgenda(creatureId, dateHeure, motif)` | agenda | **n'écrit rien** ; la confirmation passe par `POST /api/propositions-agenda/{id}/confirmer` (rôle PERSONNEL), même principe qu'au TD2 |

Chaque outil renvoie du texte lisible, y compris en cas d'échec : le modèle peut dire « je n'ai pas trouvé » au lieu de recevoir une exception.

## Agenda : port et adaptateurs

`AgendaPort` (interface) → `AgendaSimule` (défaut, en mémoire, 3 déc. 2026 à 10 h et 14 h 30 occupés) ou `GoogleAgendaAdapter`
(compte de service, API REST v3, agenda **de test**, aucun invité). Choix par `agenda.mode=simule|google`.
Pour Google : `agenda.mode=google`, `agenda.google.calendar-id=…`, `agenda.google.credentials-file=/chemin/service-account.json`
(fichier hors Git, agenda partagé avec l'adresse du compte de service).

## Deux bases de données

Dès qu'on déclare un `DataSource`, Spring Boot ne configure plus celui de la clinique : `CatalogueConfig` déclare donc les deux
(la clinique en `@Primary`). Le catalogue n'expose pas de `JdbcTemplate` (il remplacerait celui de la mémoire de conversation) mais un
`JdbcClient` qualifié. Piège rencontré en CI : sans `generateUniqueName(true)`, deux contextes de test partageaient la même base mémoire.

## Expérience de contexte (`experience/`)

`ExperienceContexte` compare deux stratégies sur `questions-reference.csv` (10 questions, faits cachés dans `grand-bestiaire.txt`, ≈180 000 caractères) :
A = tout le texte dans le prompt (tronqué à `tailleMax`), B = outil de recherche (≤ 3 passages de 800 caractères).
Variations : position du passage (début/milieu/fin) × bruit (aucun/moitié/double). Mesures écrites en CSV
(`question;strategie;position;bruit;jetons_entree;jetons_sortie;latence_ms;cout_estime;exactitude`), jetons estimés à 1 pour 4 caractères, coût fictif.
Le modèle est un `Function<String,String>` : brancher `triageClient` (sans mémoire ni outils) pour une vraie mesure.
**Les tests utilisent un faux modèle qui recopie son prompt : ils valident la mécanique, pas la qualité d'un vrai LLM. Un `mesures.csv` réel est à produire avec Ollama/LM Studio.**
La stratégie C (similarité vectorielle en mémoire) est laissée en option aux étudiants.

## Données et scripts

`scripts/generer-xlsx.py` (classeur), `scripts/generer-bestiaire.py` (corpus + questions, graine fixe), `src/main/resources/donnees/catalogue-medicaments.sql`.
Tout est synthétique (créatures et tarifs fictifs).

## Vérification

`mvn verify` en CI GitHub : 43 tests verts (modèle simulé, Wikipédia simulé par `MockRestServiceServer`, agenda simulé).
**Non vérifié** : appel réel à Wikipédia, à Google Agenda, comportement d'un vrai LLM face aux six outils.

## Tester avec Claude (API Anthropic)

1. Créez une clé sur la Claude Console (console.anthropic.com) ; l'API est facturée à l'usage, indépendamment de l'abonnement Claude.ai.
2. Définissez `ANTHROPIC_API_KEY` dans l'environnement de lancement (Spring Tools : `Run Configurations… > Environment`), jamais dans Git.
3. Lancez avec le profil `claude` (`mvn spring-boot:run -Dspring-boot.run.profiles=claude`). Starter `spring-ai-starter-model-anthropic`, modèle par défaut `claude-haiku-5-5` (rapide, économique) ; `-Dspring.ai.anthropic.chat.model=claude-sonnet-5-5` pour plus de qualité.

`temperature`, `top-p` et `top-k` sont ignorés par ce module : inutile de les régler (le paramètre `temperature` de `/api/mesures` n'aura pas d'effet avec Claude).
**Non vérifié** : aucun appel réel à l'API depuis l'environnement de rédaction.

## Tester avec Gemini (API Google)

1. Créez une clé sur Google AI Studio (aistudio.google.com) ; vérifiez sur place les quotas et la tarification applicables à votre compte.
2. Définissez `GEMINI_API_KEY` dans l'environnement de lancement (Spring Tools : `Run Configurations… > Environment`), jamais dans Git.
3. Lancez avec le profil `gemini` (`mvn spring-boot:run -Dspring-boot.run.profiles=gemini`). Starter `spring-ai-starter-model-google-genai`, modèle par défaut `gemini-3.8-flash` ; `-Dspring.ai.google.genai.chat.model=<nom>` pour un autre modèle (liste des modèles disponibles sur aistudio.google.com).

**Non vérifié** : aucun appel réel à l'API depuis l'environnement de rédaction.

## Diagnostic du fournisseur de modèle

`GET /api/diagnostic/modele` (connecté) appelle directement le modèle actif avec un message minimal et un délai de 30 s, puis renvoie
le statut, la durée et l'exception éventuelle. À essayer en premier quand le chat ne répond pas :
`curl -u gribouille:gribouille localhost:8080/api/diagnostic/modele`.

## Tester avec Groq (API compatible OpenAI)

1. Créez une clé sur console.groq.com ; vérifiez sur place les quotas et la tarification de votre compte.
2. Définissez `GROQ_API_KEY` dans l'environnement de lancement (Spring Tools : `Run Configurations… > Environment`), jamais dans Git.
3. Lancez avec le profil `groq` (`mvn spring-boot:run -Dspring-boot.run.profiles=groq`). Modèle par défaut `openai/gpt-oss-120b` ;
   `-Dspring.ai.openai.chat.model=<nom>` pour un autre (liste : `GET https://api.groq.com/openai/v1/models` avec la clé). Il doit gérer l'appel d'outils.

**Non vérifié** : aucun appel réel à l'API depuis l'environnement de rédaction.
