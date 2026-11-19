# TD1 — La clinique des créatures fantastiques (corrigé)

Application Spring Boot « classique » sans IA : JPA, couche service, API REST, pages Thymeleaf,
sécurité et tests. Les TD suivants branchent un modèle de langage sur ces services.

## Lancer

```bash
mvn spring-boot:run     # http://localhost:8080  (gribouille/gribouille ou visiteur/visiteur)
mvn verify              # tests
```

Java 21, Spring Boot 4.0, base H2 en mémoire rechargée par `data.sql` à chaque démarrage.

## Architecture

| Package | Rôle |
|---|---|
| `domain` | entités JPA (Proprietaire, Espece, Creature, Soigneur, Consultation) |
| `repository` | Spring Data ; verrou pessimiste sur le soigneur pour la planification |
| `service` | `CliniqueService` : signatures imposées par le sujet, exceptions métier |
| `controller` | `ApiController` (JSON, DTO), `ApiExceptionHandler` (404/400/409), `WebController` (Thymeleaf) |
| `dto` | records exposés ; les entités ne sortent jamais de la couche service |
| `config` | `SecurityConfig` : formulaire + basic, rôles PERSONNEL et LECTURE |

## Choix de correction

- Chevauchement : deux consultations d'un même soigneur dont les débuts sont distants de moins de 30 min.
  Les créneaux adjacents (exactement 30 min) sont acceptés.
- Concurrence : `SELECT … FOR UPDATE` sur le soigneur (`findByIdPourPlanification`) sérialise les planifications.
  Une contrainte en base ne suffit pas (pas de contrainte d'intervalle portable) ; c'est la réponse à la question 2 du sujet.
- Erreurs : 404 (ressource absente), 400 (validation, diagnostic déjà saisi), 409 (conflit de créneau).
  Le corps est un `ProblemDetail` dont le champ `detail` est une phrase française lisible.
- Les identifiants sont générés ; `data.sql` résout les clés étrangères par nom (pas d'ids en dur).
- Pas de console H2 (module séparé dans Spring Boot 4) : inspectez les données via l'API.
- Mots de passe en clair = identifiants : uniquement pour le TD.

## Statut de vérification

Ce code a été écrit sans pouvoir être compilé dans l'environnement de rédaction (Maven Central inaccessible).
La vérification est faite par l'intégration continue du dépôt (`mvn verify`).
