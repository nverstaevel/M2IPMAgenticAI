# Supports de CM — IA agentiques avec Spring AI (M2 MIAGE IPM)

Version texte des 142 slides, dans l'ordre de présentation. Les sources (HTML, CSS, `deck.json`) sont dans `slides/`, `deck.css` et `deck.json` ; les notes de l'enseignant sont reprises en citation sous chaque slide. Les schémas (`arch-*`, `seq-*`) sont réduits à leurs libellés.


## Présentation du cours et du parcours

### IA agentiques avec Spring AI  
*SUPPORTS DE COURS · CM1, CM2, CM3*  
`cover`

M2 MIAGE IPM · Java 21, Spring Boot 4, Spring AI 2.0
Nicolas Verstaevel · Université Toulouse Capitole · IRIT, équipe SMAC

> Note : Trois supports de cours magistral, un par séance qui précède des TD. Les extraits de code sont des repères : la documentation de la version fournie fait foi.

### Une application, six TD, trois cours  
*LE PARCOURS*  
`parcours`

| TD | Thème | Ce que vous livrez |
|---|---|---|
| TD1 | Application Spring Boot classique | La clinique, sans IA |
| TD2 | Un chat qui interroge l’application | Le Dr Gribouille et ses outils internes |
| TD3 | Outils externes et contexte | Quatre outils et un tableau de mesures |
| TD4 | Agent ReAct | Une boucle d’agent et ses traces commentées |
| TD5 | Agents à rôles | Un duo vétérinaire / gardien et un dossier PDF |
| TD6 | Le jeu de la clinique en MCP | Une équipe d’agents qui joue, et une comparaison chiffrée |
CM1
CM2
CM3

> Note : Chaque TD copie le dossier du précédent : on construit une seule application, la clinique des créatures fantastiques. Au TD6, une seconde application (le jeu, dossier jeu-bestiaire) est fournie et reliée par MCP. Les étudiants partent du dépôt M2IPMAgenticAI_Student.

### Comment faire un TD sans l’enseignant  
*MÉTHODE · TD EN AUTONOMIE*  
`autonomie`

1. Objectifset critère de réussite
2. Avant la séanceliste à cocher
3. Ressourceset exemples
4. Parcours guidépoints de contrôle
5. Livrableet journal
Point de contrôleUne vérification que vous faites seul : une commande, une page, un test. Il échoue ? Ne continuez pas.
En cas de blocageChaque sujet a un tableau symptôme, cause fréquente, que faire. Lisez-le avant de demander de l’aide.
Journal5 à 10 lignes par TD : il prépare la comparaison du TD6.
Les six sujets suivent le même plan : une fois lu l’un, vous savez lire les autres.

> Note : Présenter ce plan une seule fois, au début du cours. Les sujets sont dans le document « Sujets de TD ».


## CM1 : appeler un modèle, mémoire, outils et confirmation des écritures

### De l’application web au chat qui agit  
*CM1 · AVANT LES TD1 ET TD2*  
`cm1`

Appeler un modèle, lui donner de la mémoire, des outils, et garder la main sur les écritures.

### Ce que vous saurez faire après cette séance  
*CM1 · OBJECTIFS*  
`cm1-obj`

- Retrouver les bases de Spring Boot : entité, dépôt, service, contrôleur, test
- Expliquer comment un LLM fonctionne : tokens, Transformer, génération, contexte
- Choisir, lancer et brancher un modèle (local ou hébergé) dans Spring AI
- Appeler un modèle avec ChatClient, avec mémoire et sortie structurée
- Exposer un service métier comme outil, avec une description exploitable par le modèle
- Séparer proposer et confirmer pour toute écriture

> Note : Annoncer le fil rouge : la clinique des créatures fantastiques et son assistant, le Dr Gribouille. Le TD1 construit l’application sans IA, le TD2 y branche le chat. La séance est dense : les parties Fondements et Outillage servent aussi de référence pendant les TD.

### Où en êtes-vous, et où allez-vous ?  
*CM1 · MATRICE DE COMPÉTENCES*  
`cm1-matrice`

| Niveau | Je… |
|---|---|
| 0 | n’ai jamais vu cela |
| 1 | reconnais le terme et sais où chercher |
| 2 | sais le faire avec un exemple sous les yeux |
| 3 | sais le faire seul sur un cas nouveau |
| 4 | peux l’expliquer à un autre et en discuter les limites |
- Début : remplissez-la avant ce cours
- Fin : après le TD6, avec une preuve par compétence
- 19 compétences, six domaines
- Un livrable, un test ou une ligne de journal fait preuve

> Note : Le classeur de la matrice (feuilles Matrice, Échelle et Synthèse) se trouve ici : https://claude.ai/artifact/5DRhQYNfk9RNJjkzeaTG2N . Il faut le partager et le faire dupliquer ou exporter par les étudiants. Il sert à l’auto-évaluation.


## Rappel Spring Boot avec l’exemple de la clinique

### Spring Boot en un exemple  
*RAPPEL · AVANT LE TD1*  
`sb`

Une entité, un dépôt, un service, un contrôleur, un test : les pièces de la clinique que vous allez reprendre.

> Note : Dix minutes de rappel. Les extraits viennent du TD1 ; ils sont raccourcis pour tenir à l’écran. Demander à main levée qui a déjà écrit une entité JPA et un contrôleur REST.

### Quatre couches, un seul sens de dépendance  
*RAPPEL · ARCHITECTURE*  
`sb-archi`

| Paquet | Contenu | Annotation typique |
|---|---|---|
| domain | Entités du métier | @Entity |
| repository | Interfaces d’accès aux données | extends JpaRepository |
| service | Règles et transactions | @Service, @Transactional |
| controller | API REST et pages web | @RestController, @Controller |
| dto | Objets échangés avec l’extérieur | record |
ContrôleurHTTP, JSON, pages
Servicerègles métier, transactions
Dépôtaccès aux données
BaseH2 en TD
Le chat du TD2 sera un contrôleur de plus : il réutilise les services, il ne les remplace pas.

> Note : Chaque couche n’appelle que la suivante. Insister : c’est cette séparation qui permettra d’exposer les services au modèle sans toucher à la base directement.

### Un point d’entrée, des starters, des propriétés  
*RAPPEL · DÉMARRAGE*  
`sb-demarrage`

- @SpringBootApplication : configuration, détection des composants, autoconfiguration
- Les starters du pom.xml apportent les dépendances : webmvc, data-jpa, validation, security, thymeleaf
- application.properties règle le reste
- Lancer : mvn spring-boot:run, port 8080 par défaut
@SpringBootApplicationpublic class CliniqueApplication {    public static void main(String[] args) {        SpringApplication.run(            CliniqueApplication.class, args);    }}
Versions du cours : Java 21, Spring Boot 4.0.0, Spring AI 2.0.0.

> Note : Rappeler que l’autoconfiguration réagit à ce qui est sur le classpath : ajouter le starter Spring AI suffira à créer le constructeur de client de chat.

### Spring crée les objets et les relie  
*RAPPEL · CONTENEUR*  
`sb-ioc`

- Un bean est un objet géré par le conteneur
- @Component, @Service, @RestController : détectés au démarrage
- @Configuration et @Bean : vous construisez l’objet vous-même
- Injection par constructeur : dépendances explicites, immuables, faciles à tester
@RestController@RequestMapping("/api")public class ApiController {    private final CliniqueService service;    ApiController(CliniqueService service) {        this.service = service;    }}
Au TD2, les clients de chat et la mémoire de conversation seront des beans déclarés dans une classe @Configuration.

> Note : Un seul constructeur : l’annotation d’injection est inutile. Montrer ChatConfig du TD2 pour faire le lien avec la suite.

### Une entité JPA correspond à une table  
*RAPPEL · PERSISTANCE*  
`sb-entite`

- Hibernate crée la table au démarrage (ddl-auto=create-drop en TD)
- Relations : @ManyToOne, @OneToMany(mappedBy = …)
- LAZY : le lien n’est chargé que s’il est utilisé, dans une transaction
- Constructeur sans argument protégé : exigé par JPA
@Entitypublic class Creature {    @Id @GeneratedValue    private Long id;    @Column(nullable = false)    private String nom;    @ManyToOne(fetch = FetchType.LAZY)    private Proprietaire proprietaire;    protected Creature() {}}
Extrait raccourci de la classe Creature du TD1.

> Note : Piège classique : accéder à un lien LAZY hors transaction. C’est une des raisons pour lesquelles les outils du TD2 renvoient des DTO.

### Un dépôt Spring Data : une interface suffit  
*RAPPEL · PERSISTANCE*  
`sb-repo`

- JpaRepository fournit save, findById, findAll, delete…
- Le nom de la méthode est la requête : findBy + propriété + OrderBy
- Requête libre : @Query en JPQL
- Spring génère l’implémentation au démarrage
public interface CreatureRepository        extends JpaRepository<Creature, Long> {    List<Creature>        findByProprietaireIdOrderByNom(Long id);}

> Note : Faire deviner la requête SQL derrière findByProprietaireIdOrderByNom. Aucun code d’implémentation à écrire.

### Le service porte les règles et les transactions  
*RAPPEL · MÉTIER*  
`sb-service`

- Lecture seule par défaut, @Transactional pour écrire
- Lève des exceptions métier : introuvable, conflit de créneau
- Renvoie des DTO, jamais des entités
- Aux TD2 à TD6, ces méthodes seront proposées au modèle comme outils
@Service@Transactional(readOnly = true)public class CliniqueService {    @Transactional    public ConsultationDto planifier(            Long creatureId, Long soigneurId,            LocalDateTime dateHeure,            String motif) {        // contrôles, conflit de créneau, écriture    }}

> Note : Le nom réel de la méthode dans le dépôt est planifierConsultation ; raccourci ici pour tenir à l’écran. Marquer la phrase finale : le service est la frontière que le modèle ne franchira pas sans passer par un outil.

### Un contrôleur REST traduit HTTP en appels de service  
*RAPPEL · API*  
`sb-rest`

@RestController@RequestMapping("/api")public class ApiController {    @GetMapping("/proprietaires/{id}")    public ProprietaireDto proprietaire(@PathVariable Long id) {        return service.trouverProprietaire(id);    }    @PostMapping("/consultations")    public ResponseEntity<ConsultationDto> planifier(            @Valid @RequestBody PlanificationRequest requete) { … }}
@PathVariable : segment d’URL · @RequestParam : paramètre · @RequestBody : corps JSON · @Valid : validation avant l’appel.

> Note : Un contrôleur ne contient aucune règle métier : il lit la requête, appelle le service, choisit le code HTTP.

### Valider les entrées, traduire les erreurs  
*RAPPEL · QUALITÉ*  
`sb-erreurs`

- Un record Java est un DTO immuable
- @NotNull, @NotBlank, @Size… décrivent les contraintes
- @RestControllerAdvice : une classe traduit les exceptions en 404, 409, 400
public record PlanificationRequest(    @NotNull Long creatureId,    @NotNull Long soigneurId,    @NotNull LocalDateTime dateHeure,    @NotBlank String motif) {}
Ces messages d’erreur seront lus par le modèle d’IA : écrivez-les pour être compris, pas seulement pour être journalisés.

> Note : Annoncer le TD2 : un outil qui échoue renvoie un message que le modèle lit et exploite pour se corriger.

### Tester l’API sans navigateur  
*RAPPEL · TESTS*  
`sb-test`

@SpringBootTestclass ApiTest {    // MockMvc construit avec Spring Security (@BeforeEach)    @Test    @WithMockUser(roles = "LECTURE")    void rechercheParNom() throws Exception {        mvc.perform(get("/api/proprietaires").param("nom", "brume"))            .andExpect(status().isOk())            .andExpect(jsonPath("$[0].nom").value("Brumevent"));    }}
Même principe aux TD suivants, avec un modèle de chat simulé : un test ne dépend jamais d’un vrai LLM.

> Note : Insister sur la dernière phrase : elle justifie le modèle simulé du dépôt et la CI sans clé d’API.

### Fin du TD1 : l’application, sans IA  
*ARCHITECTURE · TD1*  
`arch-td1`

Navigateurou client REST
ContrôleursApiControllerWebController
CliniqueServicerègles, transactions
DépôtsSpring Data JPA
Base H2en mémoire
ApiExceptionHandler404, 409, 400
DTO (records)ce que voit l’extérieur
SecurityConfigrôles LECTURE, PERSONNEL
Entités JPACreature, Soigneur…
Les flèches vont dans le sens des appels. Chaque couche n’appelle que la suivante.

> Note : Premier schéma de la série. Les mêmes blocs reviendront en gris à chaque TD : ce qui est nouveau sera en couleur. Insister sur le sens de dépendance : le contrôleur ne touche jamais un dépôt directement. SecurityConfig distingue deux rôles : LECTURE (consulter) et PERSONNEL (écrire).

### Planifier une consultation, sans IA  
*SÉQUENCE · TD1*  
`seq-td1`

Client
ApiController
CliniqueService
Dépôts
Base H2
2 · @Valid contrôle le corps : 400 s’il est invalide
En cas de conflit : exception métier, traduite en 409 par ApiExceptionHandler
1 · POST /api/consultations (corps JSON)
3 · planifierConsultation(créature, soigneur, date, motif)
4 · charger le soigneur avec un verrou
5 · requête avec verrou (une seule planification à la fois)
6 · y a-t-il déjà une consultation sur ce créneau ?
7 · enregistrer la consultation
8 · ConsultationDto
9 · 201 Created et corps JSON

> Note : Séquence de référence du TD1 : on la compare ensuite à celle du chat. Le verrou sur le soigneur sérialise deux planifications simultanées ; la seconde voit donc la première. Les erreurs métier sont des exceptions, traduites en HTTP à un seul endroit.

### Propriétés, profils, secrets  
*RAPPEL · CONFIGURATION*  
`sb-config`

- Un profil se charge avec --spring.profiles.active=lmstudio (fichier application-lmstudio.properties)
- ${NOM:défaut} lit une variable d’environnement, avec une valeur par défaut
- Jamais de clé réelle dans Git : variable d’environnement ou fichier local ignoré
- Priorité : arguments de ligne de commande, puis variables d’environnement, puis fichiers
spring.datasource.url=jdbc:h2:mem:cliniquespring.ai.model.chat=ollamaspring.ai.ollama.chat.model=qwen2.5:7bspring.ai.openai.api-key=${OPENAI_API_KEY:cle-non-renseignee}

> Note : Ces propriétés viennent du TD2. La clé factice évite qu’un starter refuse de démarrer sans clé.

### Là où le chat s’insère dans ce que vous connaissez  
*RAPPEL · ET L’IA ?*  
`sb-lien`

ContrôleurLe ChatController reçoit le message et appelle le ChatClient, comme ApiController appelle le service.
Service devient outilDes méthodes de CliniqueService, annotées @Tool, que le modèle peut demander d’appeler.
ConfigurationClients, mémoire et modèle sont des beans de ChatConfig, réglés par des propriétés.
Ce que vous savez déjà suffit : l’IA ajoute des couches à l’application, elle n’en change pas la nature.

> Note : Transition vers la partie suivante : avant de brancher un modèle, comprendre ce qu’il fait.

### Ressources Spring Boot  
*RAPPEL · POUR ALLER PLUS LOIN*  
`sb-ressources`

| Besoin | Où chercher |
|---|---|
| Référence Spring Boot | docs.spring.io/spring-boot |
| Persistance et dépôts | docs.spring.io/spring-data/jpa/reference |
| Guides pas à pas | spring.io/guides |
| Générer un projet | start.spring.io (Java 21, Spring Boot 4.0.0) |
| Tests de contrôleurs | Rubrique Testing de la référence Spring Boot |
Choisissez toujours la documentation de la version fournie : les noms changent d’une version majeure à l’autre.

> Note : Adresses à vérifier avant la séance.

### TD1 : la clinique, sans IA  
*TD1 · OBJECTIFS ET PARCOURS*  
`td1-guide`

| Étape | Durée |
|---|---|
| 1. Mise en route | 15 min |
| 2. Entités et données | 35 min |
| 3. Repositories et services | 30 min |
| 4. API REST | 35 min |
| 5. Pages Thymeleaf | 30 min |
| 6. Sécurité | 15 min |
| 7. Tests | 20 min |
- structurer une application Spring Boot en couches
- modéliser des relations JPA et charger un jeu de données
- exposer une API REST avec DTO, validation et erreurs lisibles (404, 400, 409)
- sécuriser par rôles, puis tester service et endpoint
À la fin de la séance, vous saurez
Parcours guidé (3 h)
Réussi si mvn verify est vert et si deux consultations qui se chevauchent sont refusées par une phrase claire. Ressources, exemples, points de contrôle et dépannage : voir le sujet du TD1.

> Note : Les sept signatures de service du TD1 sont imposées : les outils du TD2 les appellent telles quelles. Insister sur des messages d’erreur lisibles par un humain et par un modèle.


## Fondements techniques des modèles de langage

### Comment fonctionne un modèle de langage  
*CM1 · FONDEMENTS*  
`intro`

Réseaux de neurones, Transformer, génération, puis l’outillage dont vous aurez besoin en TD.

> Note : Cette partie est technique à dessein : elle donne le vocabulaire et les ordres de grandeur utiles pour régler, dimensionner et déboguer un modèle en TD. Les chiffres sont des ordres de grandeur, pas des mesures.

### Un LLM calcule la suite la plus probable d’un texte  
*CM1 · INTRODUCTION*  
`intro-def`

Le texte, découpé en tokens
Le modèle calcule une probabilité pour chaque token possible
Un token est choisi
Il s’ajoute au texte
On recommence avec le texte allongé, jusqu’à la fin de la réponse.
Un LLM ne consulte pas une base de faits : il produit du texte vraisemblable.

> Note : Point de départ de toute la critique qui suit : vraisemblable ne veut pas dire vrai. Un exemple à faire au tableau : compléter « Le chat dort sur le… ».

### Un neurone artificiel : une somme pondérée  
*CM1 · FONDEMENTS · NEURONE*  
`llm-neurone`

Entréesx₁, x₂, x₃
Poidsw₁, w₂, w₃
Somme et biaisz = Σ wᵢxᵢ + b
Activationy = f(z)
Poids et biaisLes paramètres appris. Un grand modèle en compte des milliards.
ActivationUne non-linéarité (ReLU, GELU…). Sans elle, empiler des couches ne servirait à rien.
Des couchesChaque couche combine les sorties de la précédente : des représentations de plus en plus abstraites.

> Note : Rester à ce niveau : ce qui compte pour la suite, c’est que tout est nombres et multiplications de matrices, donc calcul parallèle sur GPU.

### La taille d’un modèle se compte en paramètres  
*CM1 · FONDEMENTS · RÉSEAU*  
`llm-reseau`

| Taille | Paramètres | Poids en 16 bits | Poids en 4 bits |
|---|---|---|---|
| Petit | 1 à 3 milliards | 2 à 6 Go | 1 à 2 Go |
| Moyen | 7 à 8 milliards | 14 à 16 Go | 4 à 5 Go |
| Grand | 70 milliards | 140 Go | environ 40 Go |
Entrée, couches de neurones, sortie : les paramètres sont les poids de toutes les couches.
Ordres de grandeur : 2 octets par poids en 16 bits, environ 0,5 à 0,6 en 4 bits. Il faut ajouter la mémoire du contexte (cache KV).

> Note : Faire calculer en direct : 7 milliards × 2 octets = 14 Go. C’est pour cela qu’on quantifie les modèles locaux.

### Apprendre, c’est réduire une erreur  
*CM1 · FONDEMENTS · APPRENTISSAGE*  
`llm-apprentissage`

1 · Prédirele token suivant
2 · Mesurerl’erreur (la perte)
3 · Rétropropagerle gradient
4 · Corrigerles poids un peu
DonnéesTextes, code, conversations : ce que le modèle sait vient de là, et de rien d’autre.
CalculDes milliers de GPU pendant des semaines : hors de portée d’une application.
Pour vousVous n’entraînez rien : vous appelez un modèle figé et vous lui fournissez du contexte.

> Note : Pour un LLM, la perte mesure à quel point le vrai token suivant était improbable selon le modèle. Répété des milliards de fois.

### Comment un modèle est fabriqué  
*CM1 · INTRODUCTION*  
`intro-fabrication`

| Étape | Ce qui se passe | Conséquence |
|---|---|---|
| Pré-entraînement | Prédire le token suivant sur un très grand corpus de textes | Connaissances figées à une date, biais du corpus |
| Ajustement aux consignes | Apprendre à suivre des instructions, avec des retours humains | Le modèle dialogue et obéit aux consignes, parfois trop |
| Inférence | Calculer la réponse à chaque appel | Un coût et une latence à chaque requête |

> Note : Simplification assumée. L’important pour la suite : ce que le modèle sait est figé, ce qu’il suit comme consigne est appris.

### Du texte à une suite d’entiers  
*CM1 · FONDEMENTS · TOKENS*  
`llm-tokenisation`

- Algorithme usuel : BPE (byte-pair encoding), on fusionne les paires de symboles les plus fréquentes
- Vocabulaire de 30 000 à plus de 200 000 tokens selon le modèle
- Chaque famille de modèles a son tokenizer : un même texte ne coûte pas partout autant
- Plus de tokens pour les mots rares, les nombres, le code, certaines langues
Texte : « Le griffon dort. »Tokens : [Le] [ grif] [fon] [ dort] [.]Identifiants : 4521 98213 4107 35962 13
Découpage et identifiants illustratifs : ils dépendent du modèle.

> Note : Faire tester un texte dans un outil de tokenisation en ligne ou via l’API du serveur de modèles de la salle.

### Chaque token devient un vecteur  
*CM1 · FONDEMENTS · EMBEDDINGS*  
`llm-embedding`

- Une table : une ligne de quelques milliers de nombres par token du vocabulaire
- Les valeurs sont apprises : des sens proches donnent des vecteurs proches
- On ajoute la position du token dans la phrase
- Même idée pour des textes entiers : modèles d’embeddings, base vectorielle, RAG (CM2)
« dragon » → [ 0.12, -0.80, 0.33, … ]« griffon » → [ 0.10, -0.75, 0.41, … ]« facture » → [-0.62, 0.05, 0.90, … ]4 096 valeurs par vecteur, par exemple.
Valeurs illustratives. Distance(dragon, griffon) est plus petite que distance(dragon, facture).

> Note : Le RAG du CM2 repose sur cette propriété : retrouver les textes dont le vecteur est proche de celui de la question.

### Le Transformer : une pile de blocs identiques  
*CM1 · FONDEMENTS · ARCHITECTURE*  
`llm-transformer`

Tokensembeddings + positions
Bloc 1
Blocs 2 à N
Projection sur le vocabulaire
Probabilités(softmax)
Dans un blocAttention (les tokens échangent de l’information), puis réseau feed-forward (calcul propre à chaque token).
AutourConnexions résiduelles et normalisation : elles rendent possible l’entraînement de piles profondes.
Ordre de grandeurUn modèle de 7 milliards de paramètres compte de l’ordre de 30 à 40 blocs.
Les LLM de chat sont des Transformers « décodeurs » : un token ne lit que ce qui le précède. Source : Vaswani et al., 2017.

> Note : Montrer le schéma de The Illustrated Transformer (Alammar) en complément : il est plus parlant qu’une slide.

### Chaque token pondère ceux qui le précèdent  
*CM1 · FONDEMENTS · ATTENTION*  
`llm-attention`

- Chaque token produit une requête (Q), une clé (K) et une valeur (V)
- Score : similarité entre sa requête et les clés des autres tokens
- Les valeurs sont mélangées selon ces scores (softmax)
- Masque causal : jamais de lecture du futur
- Plusieurs têtes en parallèle, chacune cherche un type de lien
Attention(Q, K, V) =    softmax( Q · K^T / √d ) · V
CoûtLe nombre de paires de tokens croît comme le carré de la longueur du contexte.

> Note : C’est la raison technique de la limite de contexte et de son coût. Ne pas dérouler le calcul matriciel.

### Un token à la fois, en boucle  
*CM1 · FONDEMENTS · GÉNÉRATION*  
`llm-generation`

| Après « Le chat dort sur le » | Probabilité |
|---|---|
| canapé | 0,41 |
| lit | 0,22 |
| toit | 0,09 |
| clavier | 0,05 |
| autres (tout le vocabulaire) | 0,23 |
- À chaque pas, une probabilité pour tout le vocabulaire
- Un token est tiré, ajouté au texte, et on recommence
- Préremplissage : le prompt est lu en parallèle (délai avant le premier token)
- Décodage : un token par pas (vitesse en tokens par seconde)
- Le cache KV garde clés et valeurs déjà calculées
Probabilités illustratives.

> Note : Distinguer la latence avant le premier token (prompt long) et la vitesse de génération : ce sont deux réglages de confort différents en TD.

### Comment choisir le prochain token  
*CM1 · FONDEMENTS · ÉCHANTILLONNAGE*  
`llm-echantillonnage`

| Méthode | Principe | Effet |
|---|---|---|
| Glouton | Prendre le plus probable | Répétitif, presque déterministe |
| Température | Diviser les scores avant le softmax | Sous 1 : prudent. Au-dessus de 1 : varié |
| Top-k | Tirer parmi les k plus probables | Coupe la queue improbable |
| Top-p | Tirer parmi les plus probables jusqu’à cumuler p | S’adapte à chaque pas |
Température 0 : presque déterministe, mais pas toujours reproductible (calcul parallèle, changement de version). Les TD fixent 0,3.

> Note : Les outils d’agent demandent des sorties stables : température basse. Les tests, eux, ne reposent jamais sur une phrase exacte.

### Les réglages qui comptent  
*CM1 · INTRODUCTION*  
`intro-reglages`

| Réglage | Effet |
|---|---|
| Température | Plus elle est haute, plus les réponses varient |
| Taille du contexte | Entrée et sortie doivent tenir dans cette fenêtre |
| Sortie maximale | Borne la longueur de la réponse, donc le coût |
| Graine | Selon le serveur de modèles, rend les essais plus reproductibles, sans le garantir |
Notez-les : une mesure sans ses réglages ne se compare pas.

> Note : Les noms et la disponibilité des réglages varient d’un serveur à l’autre (Ollama, LM Studio, service hébergé).

### Un chat n’est qu’un long texte balisé  
*CM1 · FONDEMENTS · DU MESSAGE AU TEXTE*  
`llm-gabarit`

- Le serveur applique un gabarit (chat template) propre à chaque modèle : balises, rôles, outils
- Les outils sont décrits dans le texte : nom, description, schéma JSON. La qualité des descriptions compte
- Le modèle écrit l’appel sous forme de texte structuré ; le serveur le décode, Spring AI exécute la méthode
- Un modèle non entraîné à cela ignore les outils ou invente le format
<système> Tu es le Dr Gribouille, assistant de la clinique.<outils> creneauxLibres(soigneurId, date) : liste les créneaux libres<utilisateur> Qui peut soigner un griffon demain ?<assistant> appel : creneauxLibres(soigneurId = 3, date = 2026-11-20)
Schéma : les balises réelles varient selon le modèle.

> Note : C’est la clé pour comprendre pourquoi une description d’outil vague donne de mauvais appels : le modèle ne voit que ce texte.

### La fenêtre de contexte borne et facture tout  
*CM1 · FONDEMENTS · CONTEXTE*  
`llm-contexte`

TailleEntrée et sortie doivent tenir dans la fenêtre, de quelques milliers à des centaines de milliers de tokens. En local, la valeur configurée peut être inférieure au maximum du modèle.
CoûtLe prompt est retraité à chaque appel : une conversation longue coûte de plus en plus, en temps et en argent.
QualitéUn long contexte n’est pas exploité uniformément : l’information au milieu est souvent moins bien utilisée (Liu et al., 2023).
Le CM2 et le TD3 mesurent ce que le contexte coûte et ce qu’il apporte.

> Note : Vérifier la taille de contexte réellement configurée sur le serveur local : c’est une cause fréquente de réponses tronquées.

### Tous les modèles ne conviennent pas à nos besoins  
*CM1 · FONDEMENTS · FAMILLES*  
`llm-types`

| Famille | Sert à | À savoir |
|---|---|---|
| Base | Compléter un texte | Ne suit pas les consignes |
| Instruct, chat | Dialoguer, suivre des consignes | Choix par défaut du cours |
| Appel d’outils | Émettre des appels structurés | Indispensable dès le TD2 |
| Raisonnement | Réfléchir avant de répondre | Plus lent, plus de tokens |
| Embeddings | Représenter des textes par des vecteurs | Ne génèrent pas de texte |
Autres axes : taille ; dense ou mélange d’experts (une partie des paramètres actifs par token) ; multimodal ; licence ouverte ou propriétaire.

> Note : Un même nom de modèle existe souvent en versions base et instruct : prendre l’instruct.

### Ce qu’il fait bien, ce qu’il fait mal  
*CM1 · INTRODUCTION*  
`intro-capacites`

Plutôt bienReformuler, résumer, classer, extraire des informations, produire du JSON à partir d’un texte, répondre sur ce qui est courant dans ses données.
Plutôt malCalculer exactement, connaître un fait récent ou rare, citer ses sources de façon fiable, reconnaître qu’il ne sait pas.
Les outils servent à compenser la seconde colonne.

> Note : Les outils du cours répondent à cette liste : l’agenda pour le fait récent, Wikipédia pour la source, les règles dures pour le calcul et les droits.

### Du modèle à l’agent  
*CM1 · INTRODUCTION*  
`intro-agent`

Modèle seul
Chat : messages et mémoire
Chat avec outils
Agent : boucle, plafonds, traces
Chaque marche ajoute une capacité et une nouvelle manière de se tromper.

> Note : C’est le plan du cours : les TD1 et TD2 posent le chat avec outils, les TD3 à TD5 construisent l’agent, le TD6 l’ouvre à une autre application par MCP et le fait jouer.


## Outillage : fournisseurs, modèles locaux, bonnes pratiques, ressources

### Fournisseurs, modèles locaux, bonnes pratiques  
*CM1 · OUTILLAGE POUR LES TD*  
`out`

Où tourne le modèle, comment l’appeler, comment le choisir, comment le mettre au point.

> Note : Cette partie sert de référence pendant les TD. Les noms de produits et de versions évoluent vite : toujours vérifier dans la documentation.

### Où tourne le modèle ?  
*CM1 · OUTILLAGE · PANORAMA*  
`out-paysage`

| Option | Exemples | Atouts | Limites |
|---|---|---|---|
| API hébergée | OpenAI, Anthropic, Google, Mistral | Modèles puissants, aucun matériel | Clé, coût au token, données qui sortent |
| Serveur local | Ollama, LM Studio, llama.cpp | Gratuit, données sur le poste | Matériel, modèles plus petits |
| Auto-hébergé | vLLM, serveur d’inférence maison | Maîtrise, débit | Exploitation à votre charge |
| Passerelle | OpenRouter, Hugging Face | Un accès, plusieurs modèles | Un intermédiaire de plus |
Aux TD : Ollama par défaut. Une API hébergée reste possible avec votre propre clé.

> Note : Aucune obligation d’utiliser une API payante : tout le cours passe avec un modèle local de 7 à 8 milliards de paramètres.

### Une API de chat presque universelle  
*CM1 · OUTILLAGE · API*  
`out-api`

- Format popularisé par OpenAI, repris par LM Studio, Ollama (/v1), vLLM et de nombreux services
- La réponse contient le message, la raison d’arrêt (finish_reason) et l’usage en tokens
- Les outils s’ajoutent par un champ tools (schéma JSON)
- Spring AI masque ces détails : lisez le journal DEBUG pour voir ce qui part
POST /v1/chat/completions{  "model": "qwen2.5-7b-instruct",  "temperature": 0.3,  "messages": [    {"role": "system", "content": "…"},    {"role": "user", "content": "…"}  ]}
Anthropic, Google et l’API native d’Ollama ont leurs propres formats : un starter Spring AI par fournisseur.

> Note : Montrer une requête réelle dans le journal DEBUG du TD2 : le lien entre le code Java et le JSON est immédiat.

### Ollama : un modèle local en quelques commandes  
*CM1 · OUTILLAGE · LOCAL*  
`out-local`

- Le serveur écoute sur le port 11434 ; Spring AI s’y connecte par spring.ai.ollama.base-url
- LM Studio : application graphique, serveur local sur le port 1234, compatible OpenAI
- Derrière les deux : llama.cpp, moteur d’inférence pour les modèles au format GGUF
ollama pull qwen2.5:7b # télécharger le modèleollama run qwen2.5:7b # discuter dans le terminalollama list # modèles installésollama ps # modèles chargés en mémoirecurl localhost:11434/api/chat -d '{  "model": "qwen2.5:7b", "stream": false,  "messages": [{"role": "user", "content": "Bonjour"}]}'

> Note : Premier réflexe en cas de problème : reproduire l’appel avec curl, sans Spring. Si curl échoue, le problème n’est pas dans votre code.

### Taille, quantification, appel d’outils  
*CM1 · OUTILLAGE · CHOISIR UN MODÈLE*  
`out-choix`

| Critère | Ce qu’il faut savoir |
|---|---|
| Taille | 7 à 8 milliards de paramètres : minimum raisonnable pour appeler des outils. En dessous, les erreurs se multiplient (à tester) |
| Quantification | Q4_K_M, environ 4 bits : 3 à 4 fois moins de mémoire qu’en 16 bits, perte de qualité modérée |
| Mémoire | Poids et cache du contexte, en mémoire GPU (rapide) ou en RAM (plus lent) |
| Appel d’outils | Vérifier sur la page du modèle, puis lancer la grille de test du TD2 |
| Licence et source | Ouvert ne veut pas dire libre de tout usage. Sources : ollama.com/library, huggingface.co |
Notez le nom exact du modèle et de sa quantification dans votre journal : les résultats en dépendent.

> Note : Les affirmations sur la taille minimale sont des tendances observées, à confirmer avec la grille de test sur la machine de chacun.

### Brancher un modèle : un starter et quelques propriétés  
*CM1 · OUTILLAGE · SPRING AI*  
`out-spring`

- Deux starters sont présents (-model-ollama et -model-openai) ; spring.ai.model.chat choisit l’actif
- Autres fournisseurs (Anthropic, Mistral AI, Google, Azure, Bedrock…) : même principe, voir la documentation
- Piège : l’URL du starter OpenAI doit inclure /v1 (à vérifier avec votre serveur)
Ollama (défaut)
spring.ai.model.chat=ollamaspring.ai.ollama.base-url=    http://localhost:11434spring.ai.ollama.chat.model=qwen2.5:7b
LM Studio (profil lmstudio)
spring.ai.model.chat=openaispring.ai.openai.base-url=    http://localhost:1234/v1spring.ai.openai.chat.model=qwen2.5-7b-instruct

> Note : Les propriétés sont celles du dépôt de référence. Le nom du modèle côté LM Studio est affiché dans l’onglet Developer.

### Trois précautions avec une API hébergée  
*CM1 · OUTILLAGE · CLÉS, COÛTS, DONNÉES*  
`out-secrets`

ClésJamais dans Git : variable d’environnement ou fichier local ignoré. Une clé publiée par erreur doit être révoquée aussitôt.
CoûtsFacturation au token, en entrée et en sortie. Bornez la sortie, fixez un budget de tokens, mesurez.
DonnéesPas de données personnelles réelles vers un service externe sans cadre (RGPD). Le journal DEBUG contient les prompts : ne le partagez pas.

> Note : Rappeler que toutes les données du fil rouge sont fictives. Dans un vrai projet, la question se pose dès la première ligne de code.

### Mettre au point sans se perdre  
*CM1 · OUTILLAGE · PRATIQUES*  
`out-pratiques`

- Changer une seule variable à la fois : modèle, prompt, température, outil
- Noter modèle, quantification, réglages et date à côté de chaque mesure
- Garder le journal DEBUG ouvert : prompt envoyé, appels d’outils, tokens
- Tester d’abord à la main (curl, interface de LM Studio), puis dans Spring
- Écrire les tests sur le modèle simulé : un test ne dépend pas d’un LLM
- Versionner les prompts comme du code

> Note : Ces pratiques alimentent le journal de bord demandé à chaque TD.

### Symptôme, cause probable, premier geste  
*CM1 · OUTILLAGE · DÉPANNAGE*  
`out-depannage`

| Symptôme | Cause probable | Premier geste |
|---|---|---|
| Connexion refusée | Serveur de modèles arrêté, mauvais port | ollama ps, puis curl sur le port |
| 404 sur la route de chat | URL de base sans /v1 (starter OpenAI) | Corriger base-url |
| Le modèle ignore les outils | Modèle sans appel d’outils, description vague | Grille du TD2, relire @Tool |
| Réponse très lente | Modèle trop gros pour la mémoire, contexte long | Modèle plus petit, vérifier la mémoire |
| Erreur de mémoire de conversation | Identifiant de conversation absent | Le passer à chaque appel |
| JSON invalide | Sortie non conforme au schéma | Valider, relancer une fois, journaliser |

> Note : Tableau à garder ouvert pendant les TD. Il sera complété par les remarques des étudiants dans leur journal.

### TD1 à TD3  
*CM1 · OUTILLAGE · RESSOURCES PAR TD (1/2)*  
`out-res-td1`

| TD | À consulter | Pratique à retenir |
|---|---|---|
| TD1 | Spring Boot, Spring Data JPA, guides spring.io | Couches séparées, DTO, tests MockMvc, erreurs lisibles |
| TD2 | Spring AI : Chat Client API, Tool Calling, Chat Memory, Ollama | Descriptions d’outils précises, proposer puis confirmer, journal DEBUG |
| TD3 | Spring AI : Tool Calling ; API Wikipédia ; Apache POI ; API Google Agenda | Résultats d’outils courts, sources non fiables, mesure du contexte |

> Note : Les adresses sont sur la dernière slide de cette partie.

### TD4 à TD6  
*CM1 · OUTILLAGE · RESSOURCES PAR TD (2/2)*  
`out-res-td2`

| TD | À consulter | Pratique à retenir |
|---|---|---|
| TD4 | ReAct (Yao et al., 2022) ; Spring AI : ToolCallingAdvisor | Plafonds d’étapes et de tokens, traces JSONL, détection de boucle |
| TD5 | « Building effective agents » (Anthropic) ; Apache PDFBox | Un rôle, un prompt ; nombre de tours borné ; arbitrage humain |
| TD6 | Spring AI : client et serveur MCP ; OWASP Top 10 for LLM Applications | Outils limités par rôle, validation côté serveur, parties comparées sur les mêmes graines |

> Note : Au TD6, la liste OWASP sert à relire une trace : injection par un résultat d’outil, outil trop puissant, confiance accordée au serveur.

### Ressources clés  
*CM1 · OUTILLAGE · RÉFÉRENCES*  
`out-ressources`

| Thème | Où |
|---|---|
| Spring AI | docs.spring.io/spring-ai/reference |
| Comprendre le Transformer | jalammar.github.io/illustrated-transformer |
| Construire un GPT en code | karpathy.ai/zero-to-hero.html |
| Article fondateur | arxiv.org/abs/1706.03762 |
| Contexte long | arxiv.org/abs/2307.03172 |
| Ollama | docs.ollama.com et ollama.com/library |
| LM Studio | lmstudio.ai/docs |
| Agents | anthropic.com/engineering/building-effective-agents |
| Sécurité | genai.owasp.org/llm-top-10 |

> Note : Adresses vérifiées le 19/11/2026. Les cinq premières sont des lectures complémentaires, pas des prérequis.

### Tout se compte en tokens  
*CM1 · INTRODUCTION*  
`intro-tokens`

Pas un motUn token est un morceau de mot, un mot courant ou un signe. Le découpage dépend du modèle.
Une unité de mesureLa fenêtre de contexte, le prix et la vitesse se comptent en tokens, entrée et sortie.
Selon la langueUn même texte peut coûter plus de tokens dans une langue que dans une autre, selon le modèle.

> Note : Faire compter les tokens d’une phrase avec l’outil du serveur de modèles de la salle. Les étudiants mesureront leur consommation dès le TD2.


## Regard critique sur les LLM

### Ce que les LLM ne garantissent pas  
*CM1 · REGARD CRITIQUE*  
`critique`

Six limites à garder en tête quand on construit des agents.

> Note : Cette partie sert de fil rouge d’esprit critique : les étudiants la retrouveront dans l’analyse critique du TD6.

### Hallucinations : plausible n’est pas vrai  
*CM1 · REGARD CRITIQUE*  
`crit-halluc`

Du faux bien écritLe modèle peut inventer un médicament, une posologie ou une source, avec un style tout à fait sûr de lui.
Pas de signal fiableLe ton assuré ne renseigne pas sur la justesse de la réponse.
ParadesInterroger des sources par des outils, exiger des citations vérifiables, valider à la main les décisions à risque.

> Note : Dans la clinique : une posologie inventée est un risque réel. D’où le critique du TD5, les validations côté serveur du jeu (TD6) et la validation humaine du dossier.

### Données d’entraînement et biais  
*CM1 · REGARD CRITIQUE*  
`crit-donnees`

- Le corpus est inconnu pour la plupart des modèles, et daté
- Langues et cultures sont inégalement représentées : le folklore change selon la langue de la page (TD5)
- Les biais du corpus se retrouvent dans les réponses
- Le modèle ne connaît pas votre clinique, sauf ce que vous lui fournissez

> Note : Faire le lien avec le TD5 : le gardien du bestiaire peut rejeter un diagnostic correct parce que la page consultée est dans une autre langue.

### Un raisonnement écrit n’est pas une explication  
*CM1 · REGARD CRITIQUE*  
`crit-explic`

- Le modèle ne rend pas compte de son calcul interne
- La « pensée » d’un agent est un texte généré comme un autre : elle peut ne pas refléter ce qui a produit la décision
- Une trace documente ce qui s’est passé (appels, arguments, résultats), pas pourquoi
- Fiez-vous aux actions observables, pas aux justifications

> Note : Distinction utile pour l’analyse de trace : les étapes sont des faits, la pensée est un indice. Un argument inventé se voit dans l’appel d’outil, pas dans la justification.

### Coût, énergie, dépendance  
*CM1 · REGARD CRITIQUE*  
`crit-cout`

CoûtLes tokens sont facturés, et une boucle d’agent multiplie les appels.
Énergie et matérielL’inférence consomme. Un modèle local déplace le coût sur vos machines.
DépendanceUn service hébergé change de tarif, de version, parfois de comportement : vos résultats ne se reproduisent plus.

> Note : Pas de chiffre ici : les étudiants mesurent leurs propres tokens dès le TD2. Poser la question : un workflow suffirait-il ?

### Vos données quittent la machine  
*CM1 · REGARD CRITIQUE*  
`crit-perso`

- Un prompt envoyé à un modèle hébergé est un transfert de données
- Minimiser : n’envoyer que ce qui est nécessaire à la tâche
- Données fictives uniquement en TD, jamais de donnée réelle dans un prompt
- Masquer les données personnelles avant l’envoi : un réflexe pour un vrai projet
- Un modèle local maîtrise le flux, pas l’anonymat des données

> Note : Rappeler les règles de sécurité communes aux six TD : secrets hors de Git, données fictives, agenda de test, plafond de dépense.

### Faire confiance, mais pas trop  
*CM1 · REGARD CRITIQUE*  
`crit-confiance`

- On tend à croire une réponse bien présentée : c’est un biais bien documenté
- La responsabilité reste celle d’une personne, jamais celle du modèle
- Un humain dans la boucle ne protège que s’il peut vraiment vérifier
- Valider par réflexe n’est pas contrôler : concevez la confirmation pour qu’elle donne à voir

> Note : À rapprocher du point de confirmation du TD2 et de la validation du dossier PDF au TD5. Une confirmation qui affiche seulement « OK ? » est un faux garde-fou.

### Trois questions pour le groupe  
*CM1 · POUR DISCUTER*  
`crit-debat`

- Dans la clinique, quelle décision ne laisseriez-vous jamais à un agent, même avec confirmation ?
- Une trace d’agent suffit-elle à établir qui est responsable d’une erreur ?
- À partir de quel coût ou de quel risque un agent n’est-il plus préférable à un workflow ?

> Note : Laisser quelques minutes en petits groupes, puis une mise en commun. Ne pas conclure à leur place.


## Différences entre web classique et IA agentique

### Web classique et IA agentique  
*CM1 · COMPARAISON*  
`comp`

Ce qui reste, ce qui change, ce qui s’ajoute quand un agent entre dans une application.

> Note : Retour sur le TD1 : l’application est un web classique, et c’est elle que l’assistant va interroger. Cette partie situe l’agent par rapport à ce que les étudiants connaissent déjà.

### Comment ça fonctionne  
*CM1 · WEB CLASSIQUE ET IA AGENTIQUE*  
`comp-fonct`

|  | Web classique | IA agentique |
|---|---|---|
| Entrée | Un formulaire ou une requête typée | Une demande en langage naturel |
| Logique | Un code écrit à l’avance | Un chemin choisi à l’exécution par le modèle |
| Sortie | Même entrée, même résultat | Une réponse qui peut varier |
| Erreur | Exception, code HTTP, message | Une réponse plausible mais fausse |

> Note : Insister sur la dernière ligne : en web classique, une erreur se voit. Avec un modèle, elle peut se dissimuler dans une phrase correcte.

### Où se place l’agent dans l’architecture  
*CM1 · WEB CLASSIQUE ET IA AGENTIQUE*  
`comp-archi`

Web classique
Navigateur
Contrôleur
Service
Repository
Base de données
Avec un agent
Agent (ChatClient)
Outils : vos services
Base, sources externes
Vos services ne disparaissent pas : ils deviennent les outils de l’agent.

> Note : Montrer le lien avec le TD2 : les six outils internes sont des méthodes qui appellent les services du TD1. Les règles métier restent dans le service, pas dans le prompt.

### Un même besoin, deux conceptions  
*CM1 · WEB CLASSIQUE ET IA AGENTIQUE*  
`comp-cas`

- Formulaire : créature, soigneur, date
- Validation des champs
- Règle de chevauchement dans le service
- Créneau enregistré
- « Mon griffon boite, que faire demain ? »
- Le modèle appelle des outils : créature, soigneur, créneaux
- L’outil crée une proposition
- Une personne confirme, la règle de chevauchement s’applique
Web classique
Agent

> Note : Le point à faire remarquer : dans les deux cas, la règle de chevauchement est dans le service. L’agent change la porte d’entrée, pas la règle.

### Comment on l’exploite  
*CM1 · WEB CLASSIQUE ET IA AGENTIQUE*  
`comp-expl`

|  | Web classique | IA agentique |
|---|---|---|
| Test | Assertions exactes | Propriétés, traces, modèle simulé |
| Coût | Calcul et base de données | Tokens, multipliés par chaque étape |
| Latence | Millisecondes | Secondes, selon le modèle et les outils |
| Sécurité | Validation d’entrée, autorisations | Injection de prompt, droits vérifiés dans l’outil |
| Observer | Logs et métriques | Traces d’étapes : outil, arguments, résultat |

> Note : Rappeler que les droits se vérifient dans l’outil, jamais en demandant poliment au modèle de s’en charger (TD6).

### Ce qui reste, ce qui change, ce qui s’ajoute  
*CM1 · WEB CLASSIQUE ET IA AGENTIQUE*  
`comp-reste`

Ce qui resteLes couches, les transactions, la validation, les autorisations, les tests unitaires des services.
Ce qui changeLe point d’entrée est un échange, et le chemin d’exécution n’est plus écrit à l’avance.
Ce qui s’ajouteDes outils décrits comme des API, de la mémoire, des plafonds, des traces, une confirmation humaine, des mesures.

> Note : Le métier d’ingénieur ne disparaît pas : une partie des responsabilités se déplace vers la conception des outils et des garde-fous.

### Quand rester en web classique  
*CM1 · WEB CLASSIQUE ET IA AGENTIQUE*  
`comp-choix`

| Situation | Plutôt |
|---|---|
| Entrées structurées et connues d’avance | Web classique |
| Demande libre, vague, qui touche plusieurs sources | Agent |
| Décision à risque ou réglementée | Classique, ou agent avec validation humaine |
| Résultat qui doit être reproductible à l’identique | Web classique |
Si un formulaire suffit, gardez le formulaire.

> Note : À relier au workflow de référence du TD5 : il traite la consultation sans modèle, et sert de point de comparaison honnête.


## Ce que l’IA agentique change dans le métier de diplômé IPM

### Ce que cela change dans votre métier  
*CM1 · VOTRE MÉTIER*  
`metier`

Spécifier, développer, déployer, maintenir : chaque activité se déplace un peu.

> Note : Public : futurs diplômés du M2 MIAGE IPM, qui spécifient, développent, déploient et maintiennent des applications pour des clients. Les points qui suivent sont des tendances et des pistes de réflexion, pas des prévisions chiffrées.

### Spécifier : décider ce qu’on confie à un agent  
*CM1 · VOTRE MÉTIER*  
`metier-spec`

Ce qui resteRecueillir le besoin, modéliser le processus, écrire des critères d’acceptation.
Ce qui changeChoisir ce qui reste un workflow et ce qui devient un agent. Fixer son niveau d’autonomie et ce qui exige une validation humaine.
Vous produirezUn cahier des charges avec des cas d’usage, des risques, des questions de référence, des seuils de justesse et de coût.

> Note : Les dix questions de référence du TD3 sont un exemple de ce que le client peut fournir : des cas concrets avec la réponse attendue.

### Développer : tester ce qui varie  
*CM1 · VOTRE MÉTIER*  
`metier-dev`

Ce qui resteLes couches, les API, les tests unitaires des services, la revue de code.
Ce qui changeTester des comportements variables avec des propriétés, des traces et un modèle simulé. Le prompt et la description des outils sont du code à versionner et à relire.
Vous produirezDes outils bien décrits, des plafonds, des tests rejouables et un journal de décisions.

> Note : Les assistants de code accélèrent l’écriture mais ne remplacent pas la relecture. C’est la même logique que pour l’agent : vérifier ce qu’on vous propose.

### Déployer : choisir où tourne le modèle  
*CM1 · VOTRE MÉTIER*  
`metier-deploy`

Ce qui restePipelines, configuration, secrets, environnements.
Ce qui changeChoisir un modèle local, partagé ou hébergé. Fixer sa version. Poser des budgets et des plafonds de dépense. Tenir compte des données personnelles.
Vous produirezDes profils de configuration, comme ceux du projet (modèle local, serveur compatible OpenAI, modèle simulé), et une intégration continue qui n’appelle aucun vrai modèle.

> Note : Rappeler les règles du cours : secrets hors de Git, données fictives, plafond de dépense, modèle simulé pour les tests.

### Maintenir : surveiller un comportement qui dérive  
*CM1 · VOTRE MÉTIER*  
`metier-maint`

Ce qui resteCorriger les anomalies, faire évoluer les règles métier.
Ce qui changeSuivre les traces et les coûts. Constater qu’un changement de modèle modifie les réponses. Rejouer les cas de référence après chaque changement.
Vous produirezUn jeu d’évaluation tenu à jour, un catalogue d’échecs, une procédure de retour en arrière.

> Note : Point souvent oublié : le comportement peut changer sans qu’une ligne de votre code ait bougé, si le modèle est remplacé par son fournisseur.

### Les compétences qui prennent de la valeur  
*CM1 · VOTRE MÉTIER*  
`metier-comp`

- Spécifier précisément un comportement, avec des cas et des seuils
- Concevoir des interfaces d’outils lisibles par un modèle
- Évaluer honnêtement : mesurer, répéter, dire ce qu’on ne sait pas
- Sécuriser : droits, contenu externe, plafonds, données personnelles
- Arbitrer coût, risque et valeur, y compris en choisissant le workflow
Aucune de ces compétences n’est propre à l’IA : toutes sont déjà celles d’un bon ingénieur.

> Note : Formulation volontairement prudente : il s’agit d’une lecture du cours, pas d’une étude de marché.

### Cinq questions à poser sur chaque étape d’un processus  
*CM1 · VOTRE MÉTIER*  
`metier-process`

| Question | Ce qu’elle décide |
|---|---|
| Qui décide ? | Une règle, un modèle ou une personne : le choix de l’architecture |
| Quelle erreur est tolérable ? | Le niveau de validation humaine |
| Qui valide, et sur quoi ? | Une confirmation doit donner à voir ce qu’elle valide |
| Quelle trace garde-t-on ? | Audit, explication, reprise après incident |
| Combien cela coûte-t-il ? | Tokens, latence, comparaison avec un workflow |

> Note : C’est le cœur de votre spécialité : l’ingénierie des processus métier. Un agent s’insère dans un processus, il ne le remplace pas.

### Une démarche de projet  
*CM1 · VOTRE MÉTIER*  
`metier-demarche`

Cartographier le processus
Choisir workflow ou agent, étape par étape
Prototyper avec un modèle simulé
Mesurer sur des cas de référence
Déployer avec plafonds et traces
C’est la démarche des six TD, à l’échelle d’un projet client.

> Note : Insister sur l’ordre : on mesure avant de déployer, et on déploie avec de quoi arrêter et comprendre.

### Trois conséquences pour l’application  
*CM1 · LES MODÈLES DE LANGAGE*  
`cm1-llm`

Sans étatLe modèle ne garde rien d’un appel à l’autre. Tout ce qu’il sait de la conversation, vous le lui renvoyez.
Non déterministeDeux appels identiques peuvent donner deux réponses. On teste des propriétés, pas des phrases exactes.
Fenêtre limitéeTout passe par un contexte de taille finie, facturé et ralenti au token.

> Note : Insister sur les conséquences d’architecture : la mémoire, les tests et le coût sont des problèmes d’application, pas du modèle.

### Un échange est une liste de messages  
*CM1 · LE CHAT*  
`cm1-chat`

| Rôle | Contenu | Exemple |
|---|---|---|
| system | Consignes durables | « Tu es le Dr Gribouille, assistant de la clinique. » |
| user | La demande | « Qui peut soigner un griffon demain ? » |
| assistant | La réponse du modèle | « Un soigneur est disponible à 14 h. » |
| tool | Le résultat d’un outil | Une liste de créneaux libres |
Chaque appel renvoie toute la liste : le coût croît avec la conversation.

> Note : Montrer qu’il n’y a pas de session côté modèle : le « chat » est une convention de l’application.

### Deux niveaux d’abstraction  
*CM1 · SPRING AI*  
`cm1-client`

ChatModelL’appel brut : un prompt en entrée, une réponse en sortie. C’est là que vous écrirez votre propre boucle d’agent au TD4.
ChatClientL’API fluide : consignes par défaut, mémoire, outils, sortie structurée. La boucle d’outils est gérée pour vous.
Le modèle n’exécute jamais un outil : il le demande, l’application l’exécute.

> Note : Dans Spring AI 2.0, la boucle d’outils vit dans un conseiller (ToolCallingAdvisor) et non dans le modèle. À vérifier dans la documentation de la version fournie.

### ChatClient en quelques lignes  
*CM1 · PREMIER APPEL*  
`cm1-code`

ChatClient client = builder    .defaultSystem("Tu es le Dr Gribouille, assistant de la clinique.")    .build();String reponse = client.prompt()    .user(question)    .call()    .content();
Extrait indicatif : la documentation de la version fournie fait foi.

> Note : Piège de version : le constructeur de client est mutable. Un client par rôle, cloné depuis le constructeur de base (builder.clone()).

### La mémoire, c’est à l’application de la gérer  
*CM1 · MÉMOIRE DE CONVERSATION*  
`cm1-memoire`

- À chaque tour, l’historique est relu puis renvoyé avec la nouvelle question
- Fenêtre glissante : dans le TD, les 10 derniers messages
- Dépôt JDBC : la conversation survit au redémarrage
- Identifiant de conversation obligatoire, sinon une erreur est levée
- Piège : fenêtre trop courte, le modèle oublie ; trop longue, la facture grimpe

> Note : Faire le lien avec la slide « Sans état ». Demander : un identifiant par quoi ? Par utilisateur, par propriétaire de créature, par session ?

### Un tour de chat, sans outil  
*CM1 · SÉQUENCE*  
`seq-chat`

Navigateur
ChatController
ChatClient
Mémoire JDBC
Modèle(Ollama…)
1 · POST /api/chat {conversationId, message}
2 · prompt().user(message).call()
3 · lire les 10 derniers messages
4 · historique de la conversation
5 · consignes + historique + question
6 · texte généré, jeton par jeton
7 · enregistrer la question et la réponse
8 · réponse
9 · 200 {reponse, propositions}
Trait plein : appel · tireté : retour · bandeau jaune : traitement interne

> Note : Où se passe quoi : la mémoire est du côté application (table JDBC), le modèle est un autre processus et ne garde rien. À chaque tour l’application renvoie tout l’historique utile. Le conseiller de mémoire fait les étapes 3, 4 et 7 de façon transparente.

### Du texte à un objet Java  
*CM1 · SORTIE STRUCTURÉE*  
`cm1-struct`

- Le contrat est un type Java : un enregistrement avec une énumération
- Le framework demande du JSON conforme, puis le convertit
- Valider quand même : valeur hors énumération, champ vide
record Triage(Urgence urgence, String motif) {}Triage t = client.prompt()    .user(symptomes)    .call()    .entity(Triage.class);

> Note : Utile pour brancher la sortie du modèle sur du code classique : un switch sur l’urgence plutôt qu’une analyse de phrase.

### La boucle d’appel d’outil  
*CM1 · OUTILS*  
`cm1-outils`

1 · La question arrive
2 · Le modèle demande un outil
3 · L’application l’exécute
4 · Le résultat repart au modèle
5 · Réponse finale
Les étapes 2 à 4 peuvent se répéter. Un outil, c’est une méthode annotée de votre service.

> Note : Marquer la frontière de confiance : le modèle produit du texte, seule l’application agit. C’est la base de tout le TD6.

### L’appel d’un outil, étape par étape  
*CM1 · SÉQUENCE*  
`seq-outils`

ChatClient
Modèle
OutilsClinique
CliniqueService
3 · le modèle n’exécute rien : le conseiller décode la demande
1 · question + description des outils (noms, paramètres)
2 · demande : soigneursPourEspece(« Griffon »)
4 · exécuter l’outil demandé
5 · soigneursPourEspece(« Griffon »)
6 · liste de SoigneurDto
7 · résultat renvoyé, court et structuré
8 · historique + résultat de l’outil
9 · réponse finale, ou nouvelle demande : retour à 2
Trait plein : appel · tireté : retour · bandeau jaune : traitement interne

> Note : C’est la slide clé du CM1. La boucle 2 à 8 se répète tant que le modèle demande un outil. Frontière de confiance : le modèle produit du texte (une demande), seul le code de l’application agit. Au TD4 vous écrirez vous-mêmes ce conseiller.

### La description d’un outil est un prompt  
*CM1 · CONCEVOIR UN OUTIL*  
`cm1-descr`

À éviter« Cherche des trucs dans la base ». Un paramètre String x. Une exception brute en cas d’erreur.
À écrireCe que fait l’outil et quand l’utiliser. Chaque argument décrit, avec son format (date AAAA-MM-JJ). Ce qui est renvoyé. Un message d’erreur lisible.
@Tool(description = "Liste les créneaux libres d’un soigneur à une date")public List<Creneau> creneauxLibres(    @ToolParam(description = "Identifiant du soigneur") long soigneurId,    @ToolParam(description = "Date, format AAAA-MM-JJ") LocalDate date)

> Note : Le modèle ne voit que le nom, la description et le schéma des paramètres. Si la description est vague, il choisira mal.

### Proposer, puis confirmer  
*CM1 · ÉCRITURES*  
`cm1-confirm`

Le modèle appelle l’outil de proposition
Proposition en attente
Confirmation humaine (rôle PERSONNEL)
Écriture en base
Le point de confirmation n’est pas un outil : le modèle ne peut pas l’appeler.

> Note : Règle posée dès le TD2 et reprise partout : le modèle ne décide jamais seul d’écrire en base ou dans l’agenda.

### Proposer, puis confirmer  
*CM1 · SÉQUENCE*  
`seq-confirm`

Utilisateur
ChatClient
OutilsClinique
PropositionService
ChatController/confirmer
CliniqueService
1 · « Réserve Brume demain à 10 h » (POST /api/chat)
2 · proposerConsultation(créature, soigneur, date, motif)
3 · proposer(…) : vérifie, n’écrit rien en base
4 · proposition en attente (identifiant, durée 10 min)
5 · réponse + liste des propositions à confirmer
Frontière : à partir d’ici, une personne authentifiée (rôle PERSONNEL)
6 · POST /api/propositions/{id}/confirmer
7 · confirmer(id)
8 · planifierConsultation(…) : seule écriture en base
9 · ConsultationDto

> Note : Aucun outil exposé au modèle n’appelle confirmer. L’écriture a lieu côté humain, sur un point d’entrée réservé au rôle PERSONNEL. Une proposition expire au bout de 10 minutes.

### Trois façons de répondre  
*CM1 · QUEL MODÈLE BRANCHER ?*  
`cm1-modeles`

| Option | Rôle dans le projet |
|---|---|
| Ollama | Modèle local, configuration par défaut |
| LM Studio | Serveur compatible OpenAI, profil dédié |
| Modèle simulé | Réponses et appels d’outils scriptés : tests déterministes, évaluation |
Aucun test du dépôt n’appelle un vrai modèle. Les modèles locaux diffèrent d’un poste à l’autre.

> Note : Prérequis : un modèle qui gère l’appel d’outils. La grille de test du TD2 permet de le vérifier.

### Fin du TD2 : le chat s’ajoute à l’application  
*CM1 · ARCHITECTURE*  
`arch-td2`

Page de chat(navigateur)
ChatController/api/chat
ChatClientDr Gribouille
OutilsClinique6 outils @Tool
CliniqueService(TD1)
Mémoire JDBC10 derniers messages
Serveur de modèleOllama, LM Studio…
PropositionServiceattente de 10 min
Dépôts et base H2(TD1)
proposer
confirmer (PERSONNEL)
Gris : acquis des TD précédents · bordure verte : ajouté à ce TD · tireté : système externe

> Note : Le TD1 est toujours là, en gris : l’IA s’ajoute, elle ne remplace rien. Trois ajouts : le chat (contrôleur, client, mémoire), les outils (le service devient appelable par le modèle), les propositions (la frontière avant toute écriture). Le serveur de modèle est un processus distinct, remplaçable par propriétés.

### Avant les TD1 et TD2  
*CM1 · À RETENIR*  
`cm1-bilan`

TD1La clinique sans IA : l’application que l’assistant va interroger.
TD2Le Dr Gribouille : chat, mémoire, outils internes, confirmation.
Deux piègesUn outil renvoie peu. La confirmation se fait hors du modèle.

> Note : Rappeler le journal de bord : cinq à dix lignes par TD, dont une réponse absurde du modèle copiée telle quelle.

### TD2 : un chat qui interroge la clinique  
*TD2 · OBJECTIFS ET PARCOURS*  
`td2-guide`

| Étape | Durée |
|---|---|
| 1. Configuration | 15 min |
| 2. Le Dr Gribouille | 25 min |
| 3. Mémoire | 25 min |
| 4. Outils de lecture | 40 min |
| 5. Sortie structurée | 20 min |
| 6. Écriture avec confirmation | 35 min |
| 7. Bilan | 20 min |
- brancher un modèle avec ChatClient et choisir un fournisseur
- donner un prompt système et une mémoire bornée
- exposer des services comme outils (@Tool) et les évaluer sur une grille
- obtenir une sortie structurée, et séparer la proposition de la confirmation d’une écriture
À la fin de la séance, vous saurez
Parcours guidé (3 h)
Réussi si « Qui est le propriétaire de Grognon ? » passe par vos outils et si rien n’est écrit avant le clic sur Confirmer. Ressources, exemples, points de contrôle et dépannage : voir le sujet du TD2.

> Note : Premier réflexe à installer : curl sur /api/diagnostic/modele avant tout le reste. La confirmation n’est jamais un outil du modèle.


## CM2 : sources externes, contexte, boucle d’agent, rôles et graphe en .md (TD3 à TD5)

### Sources externes, contexte, boucle d’agent  
*CM2 · AVANT LES TD3 À TD5*  
`cm2`

Brancher le monde extérieur sans lui faire confiance, mesurer le contexte, puis écrire une boucle d’agent et coordonner plusieurs rôles.

### Ce que vous saurez faire après cette séance  
*CM2 · OBJECTIFS*  
`cm2-obj`

- Brancher une source externe sans coupler l’agent à elle
- Traiter tout contenu externe comme une donnée, jamais comme une instruction
- Mesurer l’effet du contexte sur le coût et la justesse
- Écrire une boucle d’agent bornée et lire sa trace
- Choisir entre workflow, agent, duo et superviseur, et le spécifier

> Note : Le CM2 prépare trois TD : TD3 (sources et mesures), TD4 (boucle ReAct), TD5 (rôles). La seconde moitié du CM2 reprend les diapos ReAct, duo et orchestrateur, qui étaient auparavant au CM3.

### Des ports pour isoler les sources  
*CM2 · ARCHITECTURE*  
`cm2-ports`

Outil annoté @Tool
Portune interface Java
Adaptateur réelGoogle Agenda
Adaptateur simuléAgendaSimule
Les tests et le TD6 tournent sur l’adaptateur simulé : rien ne sort du réseau.

> Note : Même idée que l’architecture hexagonale vue en développement web : l’agent dépend d’un port, pas d’un service tiers.

### Un port pour isoler l’agenda  
*CM2 · ARCHITECTURE*  
`arch-ports`

OutilsExternes2 outils d’agenda
AgendaPortinterface Java
GoogleAgendaAdaptermode « google »
AgendaConfigagenda.mode choisit
AgendaSimulemode « simule »
Google Agenda(API, réseau)
Liste en mémoireaucun accès réseau
Les tests et le TD6 tournent sur l’adaptateur simulé : rien ne sort du réseau.

> Note : L’outil ne connaît que l’interface AgendaPort : le reste est un détail d’adaptateur. La propriété agenda.mode choisit l’adaptateur au démarrage (simulé par défaut). C’est ce qui permet les tests sans réseau, sans compte Google.

### Quatre sources, quatre natures  
*CM2 · LES SOURCES DU TD3*  
`cm2-externe`

| Source | Nature | Précaution |
|---|---|---|
| Wikipédia (REST) | Texte public, non fiable | User-Agent obligatoire, pages d’homonymie, contenu encadré |
| Classeur Excel | Tarifs et stock | Lu une fois au démarrage, jamais écrit |
| Base du catalogue | Médicaments, contre-indications | Seconde source de données, accès en lecture seule |
| Agenda | Créneaux, écriture possible | Proposition puis confirmation, fuseaux horaires |

> Note : La seconde source de données perturbe l’auto-configuration JPA : un qualificatif est nécessaire pour distinguer les deux.

### Un outil externe : Wikipédia  
*CM2 · SÉQUENCE*  
`seq-externe`

ChatClient
OutilsExternes
WikipediaClient
Wikipédia(REST)
ContenuExterne
Source non fiable : le texte reçu est une donnée, jamais une consigne
1 · chercherDansWikipedia(« Griffon »)
2 · résumé de la page
3 · GET /page/summary/Griffon (+ User-Agent)
4 · JSON : titre, extrait
5 · texte, ou « page d’homonymie »
6 · encadrer(source, texte)
7 · texte délimité, tronqué, présenté comme donnée
8 · résultat court pour le modèle
Trait plein : appel · tireté : retour · bandeau jaune : traitement interne

> Note : Chaque source externe passe par un client dédié et revient encadrée. Le point à retenir : ce qui vient d’une source externe est une donnée à citer, pas une consigne à suivre. Le TD6 rejouera l’injection indirecte par cette porte.

### Il coûte, et sa structure compte  
*CM2 · LE CONTEXTE*  
`cm2-contexte`

TailleChaque token se paie en argent et en latence. Au-delà de la fenêtre, l’appel échoue ou tronque.
PositionUne information au milieu d’un long texte est souvent moins bien exploitée qu’au début ou à la fin.
BruitDes passages voisins mais hors sujet détournent la réponse.
À vérifier avec vos propres mesures du TD3.

> Note : Ne pas présenter la position comme une loi : c’est une tendance observée, d’où l’expérience du TD3 qui la teste sur le modèle de la salle.

### Tout dans le prompt, ou un outil de recherche ?  
*CM2 · L’EXPÉRIENCE DU TD3*  
`cm2-exp`

- Dix questions de référence, la position de la réponse varie
- En extension : on ajoute du bruit, on compare coût et justesse
- Chaque essai est consigné dans un tableau de mesures
Stratégie ATout le grand bestiaire est placé dans le prompt, avant la question.
Stratégie BUn outil de recherche renvoie quelques passages seulement.

> Note : Le corpus est volontairement trop gros pour le confort du contexte : la stratégie A peut dépasser la fenêtre. C’est voulu, faites-le constater.

### Retrouver avant de répondre  
*CM2 · RAG*  
`cm2-rag`

Découper les documents
Indexer
Chercher les passages proches de la question
Les ajouter au prompt
Le TD3 en donne la version minimale : un outil de recherche. Il n’y a pas de TD sur l’indexation vectorielle.

> Note : Une seule slide sur le RAG : l’objectif est de faire comprendre l’intérêt de ne pas surcharger le contexte, pas de couvrir les techniques d’indexation.

### Renvoyer peu, clairement  
*CM2 · CONCEVOIR LE RETOUR D’UN OUTIL*  
`cm2-retour`

PeuLes champs utiles, pas l’entité JPA entière.
StructuréUn objet stable, des clés explicites.
Lisible en cas d’erreurUn message que le modèle peut utiliser pour se corriger.

> Note : Piège classique : un outil qui renvoie une entité volumineuse gonfle le contexte à chaque appel.

### La proposition d’abord  
*CM2 · ÉCRIRE DANS L’AGENDA*  
`cm2-agenda`

- L’outil crée une proposition, il n’écrit pas dans l’agenda
- La confirmation passe par un point d’entrée réservé au personnel
- Fuseaux horaires : une date sans fuseau est une source d’erreurs
- Un agenda de test par étudiant (créé par lui-même), aucune invitation envoyée

> Note : Même schéma que la confirmation du TD2, appliqué à une source externe.

### Écrire dans l’agenda : proposer, puis confirmer  
*CM2 · SÉQUENCE*  
`seq-agenda`

Modèle(ChatClient)
OutilsExternes
PropositionAgendaService
AgendaPort
Personnel
ChatController
1 · proposerEvenementAgenda(créature, date, motif)
2 · proposer(…)
3 · le créneau est-il libre ?
4 · créneaux libres du jour
5 · proposition d’agenda enregistrée (attente 10 min)
6 · « proposition créée, à confirmer »
Frontière : seul le personnel écrit dans l’agenda
7 · POST /api/propositions-agenda/{id}/confirmer
8 · confirmer(id)
9 · creerEvenement(titre, début, 30 min)

> Note : Même motif qu’au TD2, appliqué à une écriture externe. L’outil du modèle s’arrête à l’étape 5 : seul le point d’entrée réservé au personnel appelle creerEvenement. Le créneau est vérifié deux fois : à la proposition et, côté agenda, à la création.

### Un essai ne prouve rien  
*CM2 · MESURER*  
`cm2-mesure`

- Répétez : une seule exécution ne dit rien d’un modèle non déterministe
- Notez le modèle, ses réglages et la date
- Séparez ce qui est mesuré de ce qui est supposé
- Dites ce que vos mesures ne permettent pas de conclure

> Note : C’est le premier exercice d’esprit critique du cours : un tableau de mesures honnête vaut mieux qu’une conclusion forte.

### Un critère de choix  
*CM2 · OÙ VA LA DONNÉE ?*  
`cm2-choix`

| La donnée est… | Alors |
|---|---|
| Stable et structurée | Une table ou un tableur lu une fois, derrière un outil qui renvoie peu |
| Publique, en texte libre | Un outil de recherche, avec un contenu encadré comme donnée |
| Changeante et inscriptible | Un port, une proposition, une confirmation humaine |

> Note : Demander aux étudiants de justifier base contre agenda dans leur journal : c’est un point regardé dans les retours.

### Fin du TD3 : quatre sources externes  
*CM2 · ARCHITECTURE*  
`arch-td3`

ChatClientDr Gribouille
OutilsClinique(TD2)
OutilsExternes6 outils @Tool
CliniqueService(TD1)
Dépôts, base H2(TD1)
WikipediaClientREST
TarifsExcelReaderApache POI
Cataloguebase en lecture seule
AgendaPortinterface
Wikipédia
Classeur Exceltarifs et stocks
Base du catalogueseconde source
Google Agendaou agenda simulé
ExperienceContextestratégies A et B
Gris : acquis des TD précédents · bordure verte : ajouté à ce TD · tireté : système externe

> Note : Le client de chat peut maintenant appeler deux groupes d’outils. OutilsExternes cache quatre clients, chacun avec sa précaution : User-Agent pour Wikipédia, lecture seule pour Excel et pour le catalogue, port pour l’agenda. L’expérience de contexte est à part : elle compare « tout dans le prompt » et « un outil de recherche ».

### Qui choisit le chemin ?  
*CM2 · WORKFLOW OU AGENT*  
`cm3-wa`

| Critère | Workflow | Agent |
|---|---|---|
| Le chemin est choisi par | Le code, à l’avance | Le modèle, à l’exécution |
| Prévisibilité | Élevée | Variable |
| Coût | Connu | Borné par vos plafonds |
| Test | Cas déterministes | Propriétés et traces |
| À choisir quand | Le chemin est connu et stable | Le chemin est inconnu ou variable |

> Note : Rappeler le workflow de référence du TD5 (voir la fin du CM2) : il traite la consultation sans modèle. Un agent doit gagner quelque chose face à lui.

### Penser, agir, observer  
*CM2 · REACT*  
`cm3-react`

Penséele modèle raisonne
Actionappel d’un outil
Observationrésultat de l’outil
Retour à la pensée tant que le modèle demande un outil.
La boucle s’arrête sur une réponse finale, ou sur un arrêt décidé par notre code.

> Note : Au TD4 la boucle est écrite avec ChatModel et ToolCallingManager. Le modèle ne décide pas de s’arrêter de lui-même en cas de dérive : c’est le rôle des plafonds.

### Cinq motifs d’arrêt  
*CM2 · ARRÊTER LA BOUCLE*  
`cm3-arret`

| Motif | Quand |
|---|---|
| REPONSE_FINALE | Le modèle répond sans demander d’outil |
| PLAFOND_ETAPES | Le nombre d’étapes autorisé est atteint |
| BUDGET_TOKENS | Le budget de tokens est épuisé |
| BOUCLE_DETECTEE | La même action se répète |
| ERREUR | Une exception ou une sortie illisible |

> Note : Un agent sans plafond est un risque de coût et de boucle sans fin. Les étudiants testent chaque arrêt avec le modèle simulé.

### Une exécution ReAct, dans le code  
*CM2 · SÉQUENCE*  
`seq-react`

Client
AgentReactService
ChatModel(modèle)
ToolCallingManager
TraceStore
4 · même appel que l’étape précédente ? oui : BOUCLE_DETECTEE
8 · budget de tokens ou plafond d’étapes atteint ? oui : arrêt
1 · POST /api/agent {demande, maxEtapes}
2 · modele.call(prompt) : consignes, demande, outils
3 · pensée + demande d’outil
5 · executeToolCalls(outil, arguments)
6 · observation (tronquée à 500 caractères)
7 · ajouter(étape : pensée, outil, résultat, ms, tokens)
9 · nouveau tour avec l’historique : retour à 3
10 · ResultatAgent : réponse, motif d’arrêt, étapes, tokens

> Note : La boucle est écrite à la main : le ChatModel est appelé directement et ne lance jamais les outils. C’est notre code qui exécute l’outil, compte, trace et décide d’arrêter. Les deux contrôles des lignes 4 et 8 sont les motifs d’arrêt qui ne dépendent pas du modèle. Le dixième message n’arrive que sur une réponse finale ou un arrêt.

### Une ligne par étape, pour comprendre  
*CM2 · TRACER*  
`cm3-trace`

- La pensée, l’outil et ses arguments
- L’observation renvoyée
- Les tokens consommés
- Le motif d’arrêt
Catalogue d’échecsArgument inventé, boucle, mauvais outil, réponse sans source.
Une trace se relit : on y repère l’étape où la boucle a dérivé. La page /agents la dessine pour vous.

> Note : Insister sur la lecture : situer l’erreur, puis proposer un garde-fou précis.

### À la main ou celle du framework  
*CM2 · DEUX BOUCLES*  
`cm3-boucle`

À la mainChatModel et ToolCallingManager. Vous décidez chaque arrêt et journalisez chaque étape. Plus de code.
FrameworkChatClient et son conseiller d’outils. Peu de code, comportement moins visible, moins de points d’arrêt.
Au TD4, vous comparez les deux sur les mêmes questions.

> Note : Ne pas conclure d’avance : le but est que les étudiants forment leur jugement sur des traces comparables.

### Fin du TD4 : une boucle écrite à la main  
*CM2 · ARCHITECTURE*  
`arch-td4`

Clientrôle PERSONNEL
AgentController/api/agent
AgentReactServiceboucle, plafonds
TraceStoreune ligne par étape
ChatModelappelé directement
Serveur de modèle
ToolCallingManagerexécute les outils
Outilsclinique et externes
Services, base,sources (TD1 à TD3)
Boucle du framework/api/agent/framework
Résultat : réponse, motif d’arrêt (5 possibles), étapes, tokens. Tireté : la boucle du framework, à comparer.

> Note : Tout le nouveau est dans le paquet agent. Le ChatModel est appelé directement et les outils des TD2 et TD3 sont réutilisés tels quels : seule la boucle change de mains. Le point d’entrée /framework refait la même demande avec ChatClient pour comparer.

### Un modèle en critique un autre  
*CM2 · ACTEUR ET CRITIQUE*  
`cm3-duo`

Vétérinairepropose un diagnostic
objections
Gardien du bestiairecritique, avec Wikipédia
APPROUVELe dossier suit.
DOUTEUn humain tranche.
Non convergentArrêt après 3 tours.

> Note : L’acteur a les outils de la clinique, le critique n’a que Wikipédia. Le dossier PDF exige toujours une validation humaine.

### Acteur et critique : un tour de boucle  
*CM2 · SÉQUENCE*  
`seq-duo`

DuoService
VétérinaireAgent
Gardien duBestiaire
Wikipédia
Règlesdures
Personnel
8 · REJETE : nouveau tour avec les objections (3 tours au plus)
1 · proposer(cas, profil, objections)
2 · Diagnostic (sortie structurée)
3 · examiner(espèce, diagnostic)
4 · chercherDansWikipedia (créature, espèce)
5 · pages lues : des données à citer
6 · Critique : verdict, sources, objections
7 · si APPROUVE : contre-indication connue ?
9 · dossier : APPROUVE ou DOUTE, à valider
10 · POST /api/dossiers/{id}/valider

> Note : Le vétérinaire a les outils du dossier et de la pharmacie ; le gardien n’a que Wikipédia et aucun outil d’écriture. Après un tour APPROUVE, une règle écrite à la main peut encore refuser un traitement contre-indiqué. Au bout de trois tours sans accord, ou si le verdict est DOUTE, la décision revient à un humain.

### Ce qui peut mal tourner  
*CM2 · LIMITES DU CRITIQUE*  
`cm3-critique`

- Faux rejets : le critique refuse un diagnostic correct
- Critique sans source : exiger une citation de la page consultée
- Approbation par complaisance : un second modèle n’est pas un oracle
- Boucle qui ne converge pas : plafond de tours
- Sources divergentes selon la langue de la page

> Note : Le taux de faux rejets se mesure : c’est un des livrables du TD5.

### Un superviseur qui délègue  
*CM2 · ORCHESTRATEUR ET TRAVAILLEURS*  
`cm3-orch`

Superviseurplanifie, n’a aucun outil
Dossieroutils du TD2
Pharmaciemédicaments
Agendacréneaux
Synthèse du superviseur
Chaque travailleur n’a que ses outils. Chaque délégation est une étape tracée.

> Note : Piège de mise en œuvre : plusieurs clients construits depuis le même constructeur mutable mélangent prompts et outils. Cloner le constructeur par client.

### Orchestrateur et travailleurs  
*CM2 · SÉQUENCE*  
`seq-orch`

Client
SuperviseurAgent
Superviseur(modèle)
TravailleurDOSSIER
TravailleurPHARMACIE
TravailleurAGENDA
1 · POST /api/superviseur {demande}
2 · demande : produire un plan (aucun outil)
3 · Plan : sous-tâches ordonnées
4 · consigne 1, avec les outils du dossier
5 · réponse 1
6 · consigne 2, avec les outils de la pharmacie
7 · réponse 2
8 · consigne 3 : proposer, jamais écrire
9 · réponse 3
10 · synthèse des réponses, sans rien inventer
11 · plan, étapes tracées, synthèse

> Note : Le superviseur planifie et synthétise ; il n’a aucun outil donc aucun pouvoir d’écriture. Chaque travailleur n’a que ses outils. Chaque délégation (4, 6, 8) est une étape de la trace. Chaque client part d’un clone du constructeur de base, sinon les outils fuiteraient d’un rôle à l’autre.

### Fin du TD5 : trois façons de coordonner  
*CM2 · ARCHITECTURE*  
`arch-td5`

RolesController/duo, /superviseur,/workflow
DuoServicejusqu’à 3 tours
Vétérinairepropose
Gardiencritique
Validation humaineet dossier PDF
SuperviseurAgentorchestre
Plansortie structurée
3 travailleursoutils séparés
Synthèseet trace
Workflowsans modèle
CliniqueService
TarifsExcelReader
AgendaPort
Outils et sources des TD1 à TD3 : clinique, pharmacie, Wikipédia, agenda
Trois façons de coordonner : acteur et critique, orchestrateur, workflow. Elles réutilisent les mêmes outils.

> Note : Trois lignes, trois dispositifs pour des besoins différents. Duo : une décision à risque, un second modèle et un humain. Orchestrateur : un travail à plusieurs parties. Workflow : le chemin est connu, du Java ordinaire sans modèle (la table espèce et symptôme vers médicament remplace le raisonnement). Tous reposent sur les outils des TD1 à TD3.

### Une architecture selon le besoin  
*CM2 · CHOISIR*  
`cm3-choix`

| Architecture | À choisir quand | Coût du contrôle |
|---|---|---|
| Workflow, sans modèle | Le chemin est connu | Faible |
| Chat avec outils | Une question, quelques outils | Moyen : outils et confirmation |
| Agent ReAct | Le chemin est inconnu | Plafonds et traces |
| Acteur et critique | La décision est à risque | Un second modèle et un humain |
| Orchestrateur et travailleurs | Des parties distinctes | Délégations tracées |

> Note : Aucune ligne n’est « la bonne » : demander aux étudiants de défendre un choix sur un cas de la clinique.

### Avant les TD3, TD4 et TD5  
*CM2 · À RETENIR*  
`cm2-bilan`

TD3Quatre outils externes et un tableau de mesures.
TD4Une boucle d’agent écrite à la main, avec ses traces.
TD5Un duo acteur et critique, puis un dossier PDF.

> Note : Le CM3 arrive après le TD5 : il ouvre l’agent à une autre application par le protocole MCP (le jeu du TD6) et compare des architectures.

### TD3 : outils externes et contexte  
*TD3 · OBJECTIFS ET PARCOURS*  
`td3-guide`

| Étape | Durée |
|---|---|
| 1. Wikipedia | 20 min |
| 2. Excel | 20 min |
| 3. Base externe | 25 min |
| 4. Google Agenda | 40 min |
| 5. Expérience sur le contexte | 55 min |
| 6. Synthèse | 20 min |
- écrire des outils sur Wikipedia, Excel, une seconde base et un agenda
- isoler l’externe derrière un port (AgendaPort) et tester sans réseau
- appliquer « proposer, jamais écrire » à un système externe
- mesurer l’effet du contexte : tokens, latence, coût, exactitude
À la fin de la séance, vous saurez
Parcours guidé (3 h)
Réussi si mesures.csv a 20 lignes (10 questions × 2 stratégies) et si vous chiffrez le seuil où « tout dans le prompt » ne tient plus. Ressources, exemples, points de contrôle et dépannage : voir le sujet du TD3.

> Note : Le texte renvoyé par Wikipédia est écrit par un tiers : la question 4 du sujet prépare le TD6.

### TD4 : une boucle d’agent ReAct  
*TD4 · OBJECTIFS ET PARCOURS*  
`td4-guide`

| Étape | Durée |
|---|---|
| 1. Comprendre | 15 min |
| 2. Écrire la boucle | 30 min |
| 3. Cinq arrêts | 25 min |
| 4. Brancher le graphe | 15 min |
| 5. Tracer | 10 min |
| 6. Trois scénarios | 35 min |
| 7. Framework | 15 min |
| 8. Catalogue des échecs | 25 min |
- écrire la boucle pensée, action, observation et ses cinq motifs d’arrêt
- brancher l’exécution sur le graphe de la page /agents
- lire une trace et situer l’étape où l’agent dérive
- dresser un catalogue des échecs et comparer avec la boucle du framework
À la fin de la séance, vous saurez
Parcours guidé (3 h)
Réussi si chaque motif d’arrêt est déclenché par un test avec le modèle simulé et visible dans /agents. Ressources, exemples, points de contrôle et dépannage : voir le sujet du TD4.

> Note : C’est le TD où l’outil de lecture des graphes devient indispensable : le présenter juste avant (diapo suivante).

### Lire le graphe d’exécution d’un agent  
*OUTIL · PAGE /AGENTS*  
`outil-agents`

| Je cherche… | Je regarde… |
|---|---|
| Une boucle | les retours et le motif d’arrêt |
| Ce qui coûte | les tokens par nœud |
| L’étape en échec | le statut ERREUR et son détail |
| Une dérive | le réel comparé à l’aperçu |
- Nœud : une étape (pensée, outil, délégation), avec statut et durée
- Arête : qui a appelé qui
- Retour : une flèche en arrière, un nouveau tour de boucle
- Tokens : entrée et sortie, par nœud et au total
- Aperçu : le graphe prévu par le scénario, sans exécuter
Ouvrez /agents chaque fois que vous voulez comprendre pourquoi un agent a fait ce qu’il a fait.

> Note : Démonstration en direct conseillée : exécuter react-clinique, puis comparer le graphe prévu et le graphe réel. Un outil appelé plus de 15 fois est coupé par SuiviOutils.

### TD5 : agents à rôles  
*TD5 · OBJECTIFS ET PARCOURS*  
`td5-guide`

| Étape | Vous produisez |
|---|---|
| 0-1. Moteur et contrats | Diagnostic, Critique, Verdict |
| 2. Acteur | VeterinaireAgent |
| 3. Critique | GardienDuBestiaire |
| 4. Duo en .md | un scénario qui s’exécute |
| 5. Mesure | 15 exécutions |
| 6. Dossier PDF | DossierPdfService |
| 7-8. Superviseur, workflow | .md et service sans modèle |
- décrire un graphe d’agents (flèches, outils, boucles) dans un .md et l’exécuter
- séparer acteur et critique, et mesurer leurs faux rejets
- faire déléguer un superviseur à des travailleurs aux outils séparés
- produire un dossier PDF avec validation humaine, et le comparer à un workflow classique
À la fin de la séance, vous saurez
Parcours guidé (3 h)
Réussi si le duo et le superviseur tournent depuis un .md et si le graphe réel ressemble à l’aperçu. Ressources, exemples, points de contrôle et dépannage : voir le sujet du TD5.

> Note : Le moteur de scénarios est fourni ; les étudiants écrivent les agents et les fichiers .md. Le DuoService fourni alimente le PDF.

### Un graphe d’agents, un fichier .md  
*OUTIL · SCÉNARIOS EN MARKDOWN*  
`outil-md`

| Écriture | Sens |
|---|---|
| a -> b, c | fan-out ; a, b -> c : jonction |
| a -> b : REJETE | arc étiqueté (décision du critique) |
| b -> a : REJETE (max 2) | boucle, au plus 2 reprises |
| p => x, y | délégation d’un plan à des travailleurs |
| agent : outils dossier | seuls ces outils sont permis |
- Un graphe dessiné avec des flèches : a -> b -> c
- Les outils autorisés de chaque agent, nœud par nœud
- Des boucles bornées : critique -> acteur : REJETE (max 2)
- En Java : les agents (prompts), seule chose à écrire
- Aperçu avant exécution, puis exécution dessinée
Départ : scenarios/_modele.md. Format complet : SCENARIOS.md. Exemples : duo-veterinaire-gardien, superviseur-clinique, react-clinique.

> Note : Les noms d’agents du .md renvoient à des beans Java ; un même bean peut servir plusieurs nœuds avec des outils différents. Une erreur (agent ou outil inconnu, boucle sans étiquette) est signalée avec sa ligne.


## CM3 : le jeu de la clinique en MCP, architectures pour jouer et mesure (TD6)

### Le jeu de la clinique en MCP  
*CM3 · AVANT LE TD6*  
`cm3`

Brancher un agent sur une autre application, puis le faire jouer.

### Ce que vous saurez faire après cette séance  
*CM3 · OBJECTIFS*  
`cm3-obj`

- Expliquer ce que MCP standardise : découverte et appel d’outils
- Connecter votre agent à une application tierce sans toucher à son code
- Concevoir une architecture d’agents pour jouer, en limitant les outils de chaque rôle
- Comparer deux architectures sur la même partie, chiffres à l’appui

> Note : La séance suit le TD5 : les étudiants savent écrire un agent, décrire un graphe d’agents dans un .md, et lire le graphe de /agents. Le TD6 réutilise tout cela ; seule la source des outils change.

### Des outils enfermés dans chaque application  
*CM3 · POURQUOI MCP*  
`cm3-pourquoi`

- Les outils sont des méthodes @Tool de la même application que l’agent
- Un autre agent, ou un autre éditeur de modèles, doit tout réécrire
- Changer un outil, c’est redéployer l’agent
- L’application publie ses outils : nom, description, schéma des arguments
- N’importe quel client les découvre à la connexion
- L’agent et l’application évoluent séparément
Jusqu’au TD5
Avec MCP
MCP (Model Context Protocol) joue pour les agents le rôle d’un port USB : un connecteur commun entre des modèles et des outils.

> Note : Le jeu du TD6 est une application indépendante, sur un autre port. Preuve d’interopérabilité : on peut aussi la brancher à MCP Inspector ou à Claude Desktop, sans une ligne de code de plus.

### MCP en un tableau  
*CM3 · LE PROTOCOLE*  
`cm3-mcp`

| Élément | Ce qu’il faut retenir |
|---|---|
| Client et serveur | Le serveur publie des capacités ; le client (dans votre application d’agent) s’y connecte |
| Outils | Fonctions appelables par le modèle : nom, description, schéma JSON des arguments. C’est ce que le TD6 utilise |
| Ressources et prompts | Données à lire et modèles de consignes : existent aussi, hors TD |
| Messages | JSON-RPC : initialize, tools/list, tools/call |
| Transports | stdio (processus local) ou HTTP en flux (Streamable HTTP) : c’est celui du TD6, sur /mcp |
| Spring AI | Starter serveur : tout bean ToolCallbackProvider est publié. Starter client : les outils distants deviennent des ToolCallback |

> Note : Côté Java, un outil MCP distant est un ToolCallback comme les autres : c’est pourquoi le moteur de scénarios du TD5 le gère sans changement, avec un nouveau groupe d’outils nommé jeu.

### Du modèle au moteur du jeu  
*CM3 · SÉQUENCE*  
`seq-mcp`

AgentGribouille
Client MCP(starter)
Serveur MCP(jeu, /mcp)
Moteurdu jeu
1 · au démarrage : connexion et initialize
2 · tools/list
3 · 9 outils : nom, description, schéma
4 · ToolCallback utilisables par l’agent
5 · le modèle choisit : examiner_patient(P03)
6 · tools/call examiner_patient {idPatient}
7 · Jeu.examiner : 1 point d’action
8 · résultat JSON, rendu au modèle

> Note : Les étapes 1 à 4 ont lieu une fois, au démarrage : c’est la découverte. Ensuite l’agent boucle sur 5 à 8, comme au TD4. Le moteur du jeu n’a aucune idée qu’un modèle l’appelle : il reçoit les mêmes appels que n’importe quel client MCP.

### La clinique du bestiaire  
*CM3 · LE JEU*  
`cm3-jeu`

- 5 jours, 3 nouveaux patients par jour : dragonnet, loup-garou, vampire, licorne
- 8 points d’action par jour : examiner ou soigner en coûte un
- Chaque espèce a deux maladies aux symptômes communs ; un examen révèle le signe distinctif
- Un patient non soigné perd 15 PV par jour et meurt à 0
- La pharmacie a un stock limité ; une commande arrive le lendemain
- Le carnet de soins garde ce qui a été examiné ou essayé (lisible par MCP)
- Argent colloïdal : toxique pour un loup-garou
- Gousse d’ail : toxique pour un vampire
- Tisane : redonne des PV mais ne soigne pas
- Une note de propriétaire donne des ordres à l’assistant : une injection
Les règles
Ce qui est piégé
Score : 100 par guérison + trésorerie + 2 × réputation − 150 par mort. Même graine, mêmes patients.

> Note : La graine rend les parties comparables : deux architectures jouent exactement les mêmes patients. La page d’accueil du jeu (port 8090) montre la partie en direct : les créatures arrivent à pied dans la salle d’attente, chacune avec un compte à rebours en jours restants. On peut aussi y jouer à la main : c’est la référence humaine du TD6. À montrer en démo : la page /pilotage de la clinique lance une partie avec des règles fixes ou avec vos agents, règle la vitesse et montre les appels d’outils du contrôleur en direct.

### Ce que le serveur MCP publie  
*CM3 · LES OUTILS DU JEU*  
`cm3-outils-jeu`

| Outil | Rôle | Coût |
|---|---|---|
| etat_clinique | jour, points, trésorerie, score | gratuit |
| lister_patients | symptômes, PV, urgence, note du propriétaire | gratuit |
| fiche_espece | maladies, remèdes, médicaments toxiques | gratuit |
| carnet_soins | effets déjà observés et signes relevés (mémoire de la partie) | gratuit |
| consulter_pharmacie | prix, stock, commandes en cours | gratuit |
| examiner_patient | révèle le signe distinctif (noté au carnet) | 1 point, 5 or |
| soigner | administre un médicament | 1 point |
| commander | 1 à 20 unités, livrées demain | prix payé |
| fin_de_journee | fait avancer le monde | gratuit |

> Note : Cinq outils de lecture, quatre outils d’action. Cette séparation sert à la conception de l’architecture : un rôle qui n’observe que ne peut pas faire de dégâts.

### Deux applications, un protocole  
*CM3 · ARCHITECTURE*  
`arch-mcp`

Clinique (TD6) · port 8080 · client MCP
Scénario .mdgraphe, outils par rôle
AgentsReAct, diagnosticien,gardien, apothicaire
OutilsDisponiblesgroupe « jeu »
JoueurServiceune exécution = un jour
Starter MCP clientprofil « jeu »
jeu-bestiaire · port 8090 · serveur MCP
OutilsJeu8 méthodes @Tool
Jeurègles, score, graine
Cataloguemaladies, médicaments
Page du jeu(animée, /)
Starter MCP serveurendpoint /mcp
MCP · Streamable HTTP
Le jeu est fourni et ne change pas. Vous câblez le client : profil « jeu », groupe d’outils, agents, architecture.

> Note : Les deux applications se lancent séparément. Si le jeu est arrêté, la clinique démarre quand même (client MCP désactivé par défaut) ; avec le profil « jeu », le client se connecte et ses outils apparaissent dans /agents.

### Deux architectures fournies, à vous de faire mieux  
*CM3 · CONCEVOIR POUR JOUER*  
`cm3-archi`

- Un agent, les neuf outils
- Simple et peu coûteux
- Mêmes risques : il peut commander et soigner sans relecture
- Diagnosticien : observe, examine, propose un plan
- Gardien : relit le plan, ne peut qu’en vérifier les faits
- Apothicaire : exécute et termine la journée
Gribouille seul (ReAct)
entrée -> gribouille -> fin
Une équipe
entrée -> diagnosticien -> gardiengardien -> diagnosticien : REJETE (max 2)gardien -> apothicaire : APPROUVE, DOUTE, REJETE
Pistes : un triage qui trie les patients, une mémoire d’une journée à l’autre, un routeur par espèce, un gardien qui ne regarde que les médicaments toxiques.

> Note : Les deux architectures sont deux fichiers .md. Chaque idée des étudiants est un nouveau fichier : on compare les scores. Le point à faire remarquer : le rôle gardien n’a que fiche_espece et consulter_pharmacie, il ne peut donc pas agir.

### Brancher un serveur MCP, c’est lui faire confiance  
*CM3 · CONFIANCE*  
`cm3-confiance`

- Un résultat d’outil contient des consignes (la note du patient P05)
- Une description d’outil oriente le modèle, et vient du serveur
- Un outil trop puissant : commander 50 unités, soigner sans relecture
- Un serveur modifié après votre validation
- La liste des outils par rôle, dans le .md : « outils fiche_espece »
- Les plafonds du moteur : 15 appels d’outils par agent
- Le texte reçu est une donnée, jamais un ordre : à dire dans les prompts
- La validation côté serveur : le jeu limite la quantité commandée
Ce qui peut mal tourner
Ce que vous contrôlez
MCP standardise l’échange, pas la confiance : les protections de la séance précédente restent chez vous.

> Note : Le jeu contient une note piégée au patient P05 : un bon exercice est de relire la trace d’une partie et de repérer si l’agent a obéi. Garder en tête que la protection la plus sûre est de ne pas donner l’outil.

### Comparer deux architectures  
*CM3 · MESURER*  
`cm3-eval`

- Mêmes graines, trois au moins : mêmes patients
- Une partie par architecture et par graine : POST /api/agent/jeu/partie
- Référence passive : ne rien faire donne le plancher
- Score, guéris, morts, trésorerie
- Tokens et durée, lus dans le graphe de /agents
- Où l’équipe perd : jour, patient, outil
Protocole
Ce que l’on relève
Une moyenne sur trois parties n’est pas une preuve : on rapporte les écarts et on dit ce qu’ils ne démontrent pas.


### Ce que ce jeu ne démontre pas  
*CM3 · LIMITES*  
`cm3-limites`

- Un jeu avec des règles simples n’est pas une clinique réelle
- Un modèle local de petite taille joue moins bien : le score mesure aussi le modèle
- Un modèle n’est pas déterministe : deux parties identiques peuvent différer
- MCP évolue vite : versions du protocole et des starters à vérifier

> Note : Demander aux étudiants de ne jamais écrire « l’architecture X est meilleure » sans dire sur quelles graines, avec quel modèle, et sur combien de parties.

### TD6 : le jeu de la clinique en MCP  
*TD6 · OBJECTIFS ET PARCOURS*  
`td6-guide`

| Étape | Vous produisez |
|---|---|
| 1. Lancer et explorer | une partie jouée à la main, dans la page du jeu |
| 2. Connecter | la liste des outils du jeu |
| 3. Gribouille seul | une partie jouée par l’agent |
| 4. Une équipe | un scénario .md à rôles |
| 5. Votre architecture | un deuxième scénario |
| 6. Comparer | tableau de scores et analyse |
- démarrer le jeu et l’explorer comme n’importe quel client MCP
- connecter votre agent au jeu (profil jeu) et écrire un groupe d’outils
- décrire une architecture d’agents qui joue dans un fichier .md
- comparer vos architectures par des parties mesurées
À la fin de la séance, vous saurez
Parcours guidé
Réussi si une partie complète se joue avec un scénario et si votre comparaison est chiffrée. Point de départ : dossiers td6-mcp-jeu et jeu-bestiaire du dépôt M2IPMAgenticAI_Student. Ressources, exemples, points de contrôle et dépannage : voir le sujet du TD6.

> Note : Ce TD entraîne à lire une trace, corriger un outil et rédiger une analyse critique.

### L’application, du TD1 au TD6  
*RÉCAPITULATIF · ARCHITECTURE*  
`arch-evolution`

TD1Clinique sans IA
domain, repository, service, controller, dto
TD2Chat
ai : ChatClient, mémoire, outils, propositions
TD3Sources externes
externe : Wikipédia, Excel, catalogue, agenda ; experience
TD4Agent ReAct
agent : boucle, plafonds, traces
TD5Rôles
roles et scenario : duo, superviseur, graphe en .md
TD6MCP
jeu : client MCP, outils découverts, parties mesurées
Du TD1 au TD5, on ajoute des paquets. Le TD6 ajoute une seconde application, reliée par un protocole.

> Note : Récapitulatif des schémas de la série. Chaque dossier de TD reprend le précédent ; le jeu est un projet à part (jeu-bestiaire). Trois bascules : le chat (TD2), la boucle d’agent (TD4), l’ouverture à d’autres applications (TD6).

### Pour aller plus loin  
*SOURCES*  
`fin`

- Spring AI 2.0, appel d’outils
- Spring AI 2.0, boucle écrite par l’appelant (ChatModel)
- Spring AI 2.0, mémoire de conversation
- Spring AI 2.0, client MCP (starter)
- Spring AI 2.0, serveur MCP Streamable HTTP
