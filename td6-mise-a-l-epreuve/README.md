# TD6 — Mise à l'épreuve (solution)

Cumulatif sur le TD5. **Ne pas distribuer avant la fin du TD6.**

## Garde-fous ajoutés (`garde/`) et où ils agissent

| Garde-fou | Classe | Branché dans |
|---|---|---|
| Droits dans l'outil (échec fermé) | `DroitsOutils` | `proposerConsultation`, `proposerEvenementAgenda` ; rôles mis dans le `ToolContext` par `ChatController`, `AgentReactService`, `SuperviseurAgent` |
| Contenu externe non fiable | `ContenuExterne` | `chercherDansWikipedia`, `stockPharmacie`, `creneauxLibres` (titres d'événements) |
| Bornes de dates | `BornesPlanification` + `HorlogeConfig` (`clinique.horloge` fige l'heure en test) | les deux outils de proposition |
| Plafonds d'appels | `AgentReactService` (`MotifArret.PLAFOND_APPELS`, `agent.max-appels-par-outil`, `agent.max-appels-total`) | boucle ReAct |
| Règles dures + validation des sorties | `ReglesDures`, `DuoService` | verdict APPROUVE annulé si contre-indication ; sortie illisible → VERS_HUMAIN |
| Audit sans donnée inutile | `Masquage` | `TraceStore`, `DossierPdfService` (propriétaire réduit à l'initiale) |
| Filtre d'entrée | `FiltreEntree` | `POST /api/chat` et flux |

Mesure de reproductibilité : `Reproductibilite.synthese` et `POST /api/agent/mesures` (`demande`, `executions` ≤ 50, `temperature`) renvoient réussites, étapes min/médiane/max, tokens moyens et motifs d'arrêt.

## Catalogue d'attaques : ce qui est rejoué automatiquement

| ID | Points | Garde-fou(s) | Test | Ce que le test prouve |
|---|---|---|---|---|
| A1 injection directe | 1 | filtre d'entrée | `AttaquesTest.A1_injectionDirecte`, `GardeFousTest.filtreDEntree…` | refus de la formulation grossière ; une phrase ordinaire passe |
| A2 injection indirecte | 3 | contenu externe | `ContenuExterneTest` (Wikipédia, Excel), `AttaquesTest.A2_…Agenda` | consigne retirée, texte délimité ; **pas** de couverture des reformulations |
| A3 droits | 3 | droits dans l'outil, confirmation hors modèle | `AttaquesTest.A3_…`, `GardeFousTest.aucunOutil…` | le visiteur ne crée aucune proposition ; aucun outil n'écrit |
| A4 boucle sans fin | 1 | bornes de dates, plafonds | `AttaquesTest.A4_…` (2 tests), `AgentReactServiceTest` | date passée ou 3 h refusées ; arrêt sur plafond d'appels |
| A5 argument inventé | 2 | outils qui échouent proprement | `AttaquesTest.A5_…` | créature 9999 : erreur explicite, pas d'invention côté outil |
| A6 dérive du coût | 1 | plafonds, validation des bornes | `AttaquesTest.A6_…` | `maxEtapes` et `executions` bornés (400) |
| A7 persuader le critique | 2 | règles dures, validation des sorties | `AttaquesTest.A7_…` (2 tests) | un gardien « persuadé » ne fait pas approuver l'argent ; sortie illisible → humain |
| A8 fuite dans le dossier | 3 | masquage | `AttaquesTest.A8_…`, `GardeFousTest.masquage…` | ni téléphone, ni courriel, ni nom du propriétaire dans le PDF |

Fixtures : `src/test/resources/wikipedia/wikipedia-piege.json`, `src/test/resources/donnees/tarifs-piege.xlsx` (régénérable par `scripts/generer-fixtures-pieges.py`), événement piégé de `AgendaSimule` (3 décembre 2026).

## Vérification et limites

CI verte : 90 tests, tous avec le modèle simulé. Ils établissent que les garde-fous **déterministes** fonctionnent. Ils ne mesurent pas : la sensibilité d'un vrai LLM aux injections (A1, A2, A7), les faux refus en usage réel, les 20 + 20 exécutions du scénario C2, ni le coût réel. `FICHE.md` décrit l'application ; `ANALYSE.md` est un modèle à remplir avec de vraies mesures ; `JOURNAL.md` reste à produire par les étudiants.

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
