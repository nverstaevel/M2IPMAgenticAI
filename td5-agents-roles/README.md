# TD5 — Agents à rôles et dossier PDF (solution)

Cumulatif sur le TD4. **Ne pas distribuer avant la fin du TD5.**

## Ajouts (`roles/`)

- Contrats : `Diagnostic`, `Critique`, `Verdict` (APPROUVE / REJETE / DOUTE), `CasBestiaire` (5 cas dans `donnees/cas-bestiaire.csv`), `ProfilActeur` (PRUDENT / PRESSE).
- `VeterinaireAgent` (acteur : dossier + pharmacie/tarifs, deux prompts) et `GardienDuBestiaire` (critique : un seul outil, `chercherDansWikipedia`, aucune écriture). Sorties structurées via `entity(...)`.
  Les outils sont des sous-ensembles (`OutilsPharmacie`, `OutilsAgenda`, `OutilsBestiaire`) qui délèguent à `OutilsExternes` : un agent n'a que les outils de son rôle.
- `DuoService.traiter(cas, profil)` : actor-critic, 3 tours au plus ; APPROUVE → `APPROUVEE`, DOUTE → `VERS_HUMAIN`, rejets répétés → `NON_CONVERGENTE`.
  **Un dossier n'est jamais validé automatiquement** (état `EN_ATTENTE_DE_VALIDATION`).
- `DossierPdfService.generer(runId)` (PDFBox) : en-tête, un bloc par tour (proposition, verdict, objections, sources), décision et validation (agent humain + horodatage), annexe technique. Écrit aussi `dossiers/{runId}.pdf`.
- Validation humaine : `POST /api/dossiers/{runId}/valider` (une seule fois, 409 ensuite) et page `/dossiers` avec boutons Valider / Refuser. Tout `/api/dossiers/**`, `/dossiers/**`, `/api/agent/**` est réservé à PERSONNEL.
- `SuperviseurAgent` : plan structuré (`Plan`), trois travailleurs (Dossier, Pharmacie, Agenda) ayant chacun leurs outils ; chaque délégation est une `Etape` de la trace JSONL. Le superviseur n'a aucun outil.
- `TraiterConsultationWorkflow` : le même traitement sans modèle (table espèce + symptôme → médicament, stock et tarif Excel, premier créneau libre).
- Endpoints : `GET /api/cas`, `POST /api/duo`, `GET /api/dossiers/{id}` et `.pdf`, `POST /api/superviseur`, `POST /api/workflow`.

## Le modèle simulé joue les deux rôles

En profil `simule`, `ModeleSimule` reconnaît `[ROLE:VETERINAIRE]` (pressé : renvoie la piste habituelle ; prudent ou après objections : tisane de valériane), `[ROLE:GARDIEN]` (règles : argent → REJETE, ail ou soleil → REJETE, sauna → DOUTE, sinon APPROUVE) et `[ROLE:SUPERVISEUR]` (plan fixe).
Les verdicts des cinq cas sont donc reproductibles en CI, **mais ce gardien est un jeu de règles, pas un LLM qui lit Wikipédia** : les mesures du TD (rejets corrects, faux rejets, faux accords sur 15 exécutions) restent à faire avec un vrai modèle.

## Piège Spring AI

`ChatClient.Builder` est modifiable : réutiliser le même builder pour plusieurs clients mélange prompts système et outils. Cloner (`builder.clone()`) pour chaque client.

## Tests (CI verte)

`DuoServiceTest` (sans Spring, modèles scriptés : arrêt sur APPROUVE, DOUTE, plafond de 3 tours, transmission des objections, profils, validation unique) et `RolesApiTest` (cinq cas, PDF relus avec PDFBox, validation, droits, superviseur, workflow).

## Non vérifié

Qualité d'un vrai gardien et d'un vrai vétérinaire, appels réels à Wikipédia depuis le duo, mesures sur 15 exécutions, comparaison chiffrée workflow / superviseur, `JOURNAL.md`. Les PDF sont produits dans `target/dossiers-test` pendant les tests (non versionnés).

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
