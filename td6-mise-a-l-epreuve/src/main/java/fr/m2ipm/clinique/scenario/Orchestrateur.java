package fr.m2ipm.clinique.scenario;

import fr.m2ipm.clinique.agent.Evenement;
import fr.m2ipm.clinique.agent.SuiviExecution;
import fr.m2ipm.clinique.agent.SuiviOutils;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Exécute un scénario : choisit la topologie d'après « architecture », appelle les agents par leur nom, et alimente le suivi
 * (graphe en direct) : un nœud par appel d'agent, avec ses outils, ses tokens et sa durée. Les agents ne s'occupent de rien de cela.
 *
 * <ul>
 *   <li>react : un agent possède tout le graphe (il émet ses propres nœuds) ; l'orchestrateur lui donne le runId.</li>
 *   <li>sequence : étapes appelées dans l'ordre, la sortie de l'une devient l'entrée de la suivante.</li>
 *   <li>boucle-critique : un producteur et un critique, jusqu'à approbation, doute ou {@code maxTours}.</li>
 *   <li>superviseur : un planificateur produit des tâches, des travailleurs les exécutent, une synthèse conclut.</li>
 * </ul>
 */
@Service
public class Orchestrateur {

    private static final Logger LOG = LoggerFactory.getLogger(Orchestrateur.class);

    public static final List<String> ARCHITECTURES = List.of("react", "sequence", "boucle-critique", "superviseur");

    private final Map<String, Agent> agents;
    private final SuiviExecution suivi;
    private final ExecutorService executeur = Executors.newVirtualThreadPerTaskExecutor();

    public Orchestrateur(Map<String, Agent> agents, SuiviExecution suivi) {
        this.agents = agents;
        this.suivi = suivi;
    }

    @PreDestroy
    void arreter() { executeur.shutdownNow(); }

    public List<String> agentsDisponibles() { return agents.keySet().stream().sorted().toList(); }

    // ------------------------------------------------------------------ validation

    /** Problèmes de définition, en plus de ceux de la lecture du fichier : architecture inconnue, rôle manquant, agent absent. */
    public List<String> valider(ScenarioDef d) {
        List<String> e = new ArrayList<>(d.erreurs());
        if (!ARCHITECTURES.contains(d.architecture())) {
            e.add("architecture « " + d.architecture() + " » inconnue (attendu : " + String.join(", ", ARCHITECTURES) + ").");
            return e;
        }
        switch (d.architecture()) {
            case "react" -> role(d, "agent", e);
            case "sequence" -> {
                List<String> etapes = liste(d.agents().get("etapes"));
                if (etapes.isEmpty()) { e.add("agents.etapes : liste d'au moins un agent attendue."); }
                etapes.forEach(n -> existe(n, "agents.etapes", e));
            }
            case "boucle-critique" -> {
                role(d, "producteur", e);
                role(d, "critique", e);
                int max = entier(d.options().get("maxTours"), 3);
                if (max < 1 || max > 10) { e.add("options.maxTours doit être compris entre 1 et 10."); }
            }
            case "superviseur" -> {
                role(d, "planificateur", e);
                if (d.agents().get("synthese") != null) { role(d, "synthese", e); }
                if (travailleurs(d).isEmpty()) { e.add("agents.travailleurs : au moins un travailleur (NOM: agent) attendu."); }
                travailleurs(d).forEach((nom, bean) -> existe(bean, "agents.travailleurs." + nom, e));
            }
            default -> { }
        }
        return e;
    }

    private void role(ScenarioDef d, String role, List<String> e) {
        Object v = d.agents().get(role);
        if (v == null || String.valueOf(v).isBlank()) {
            e.add("agents." + role + " : nom d'agent attendu.");
        } else {
            existe(String.valueOf(v).trim(), "agents." + role, e);
        }
    }

    private void existe(String bean, String champ, List<String> e) {
        if (!agents.containsKey(bean)) {
            e.add(champ + " : aucun agent nommé « " + bean + " » (agents disponibles : " + String.join(", ", agentsDisponibles()) + ").");
        }
    }

    // ------------------------------------------------------------------ aperçu

    /** Graphe prévu, sans rien exécuter. Les identifiants sont ceux de l'exécution, quand ils sont connus à l'avance. */
    public List<Evenement> apercu(ScenarioDef d) {
        List<Evenement> ev = new ArrayList<>();
        BiConsumerNoeud n = (id, parents, genre, libelle, detail) -> ev.add(new Evenement(ev.size() + 1L, 0L, "apercu", "noeud", id,
                parents, genre, libelle, Evenement.PREVU, detail, null, null, null, null, null, null, null));
        n.ajouter("demande", List.of(), Evenement.UTILISATEUR, "Entrée", champsEnTexte(d));
        switch (d.architecture()) {
            case "react" -> {
                n.ajouter("e1", List.of("demande"), Evenement.MODELE, "Étape 1 · modèle", "L'agent « " + d.agents().get("agent") + " » possède ce graphe : un nœud par étape du modèle, un par outil, autant d'itérations que nécessaire.");
                n.ajouter("e1-outil", List.of("e1"), Evenement.OUTIL, "Outil appelé", null);
                n.ajouter("fin", List.of("e1-outil"), Evenement.FIN, "Réponse finale", null);
            }
            case "sequence" -> {
                String precedent = "demande";
                int i = 0;
                for (String nom : liste(d.agents().get("etapes"))) {
                    i++;
                    n.ajouter("s" + i, List.of(precedent), Evenement.AGENT, nom, "Étape " + i);
                    precedent = "s" + i;
                }
                n.ajouter("fin", List.of(precedent), Evenement.FIN, "Résultat", null);
            }
            case "boucle-critique" -> {
                int max = entier(d.options().get("maxTours"), 3);
                String precedent = "demande";
                for (int t = 1; t <= max; t++) {
                    n.ajouter("p" + t, List.of(precedent), Evenement.AGENT, d.agents().get("producteur") + " · tour " + t, t == 1 ? "Producteur" : "Reprend les remarques du critique (si rejet)");
                    n.ajouter("c" + t, List.of("p" + t), Evenement.CRITIQUE, d.agents().get("critique") + " · tour " + t, "Critique : approuve, rejette (nouveau tour) ou doute (renvoi à un humain)");
                    precedent = "c" + t;
                }
                if (booleen(d.options().get("validationHumaine"), true)) {
                    n.ajouter("humain", List.of(precedent), Evenement.HUMAIN, "Validation humaine", "Aucune décision n'est appliquée sans le personnel.");
                    precedent = "humain";
                }
                n.ajouter("fin", List.of(precedent), Evenement.FIN, "Résultat", "Le graphe réel s'arrête au premier tour approuvé ou douteux.");
            }
            case "superviseur" -> {
                n.ajouter("plan", List.of("demande"), Evenement.AGENT, d.agents().get("planificateur") + " · plan", "Produit la liste des tâches");
                List<String> ts = new ArrayList<>();
                travailleurs(d).forEach((nom, bean) -> {
                    n.ajouter("t-" + nom, List.of("plan"), Evenement.TRAVAILLEUR, nom + " (" + bean + ")", "Exécuté si le plan lui confie une tâche");
                    ts.add("t-" + nom);
                });
                String synthese = d.agents().get("synthese") == null ? null : String.valueOf(d.agents().get("synthese"));
                if (synthese != null) {
                    n.ajouter("synthese", ts, Evenement.AGENT, synthese + " · synthèse", "Assemble les réponses des travailleurs");
                    n.ajouter("fin", List.of("synthese"), Evenement.FIN, "Réponse finale", null);
                } else {
                    n.ajouter("fin", ts, Evenement.FIN, "Réponse finale", null);
                }
            }
            default -> { }
        }
        return ev;
    }

    @FunctionalInterface
    private interface BiConsumerNoeud {
        void ajouter(String id, List<String> parents, String genre, String libelle, String detail);
    }

    private static String champsEnTexte(ScenarioDef d) {
        return d.entree().isEmpty() ? null : d.entree().stream().map(c -> c.libelle() + " : " + (c.defaut().isBlank() ? "(à saisir)" : c.defaut()))
                .reduce((a, b) -> a + "\n" + b).orElse(null);
    }

    // ------------------------------------------------------------------ lancement

    /** Lance l'exécution en arrière-plan et renvoie tout de suite le runId, pour que la page suive le graphe en direct. */
    public String lancer(ScenarioDef d, Map<String, String> saisie) {
        List<String> erreurs = valider(d);
        if (!erreurs.isEmpty()) {
            throw new ScenarioException("Scénario invalide : " + String.join(" ; ", erreurs));
        }
        String runId = UUID.randomUUID().toString();
        suivi.demarrer(runId, d.titre());
        executeur.submit(() -> {
            try {
                executer(runId, d, saisie);
            } catch (RuntimeException e) {
                LOG.info("Scénario {} terminé en erreur : {}", d.id(), e.getMessage());
            }
        });
        return runId;
    }

    /** Exécution synchrone (utilisée par {@link #lancer} et par les tests). Les erreurs sont visibles dans le graphe, puis relancées. */
    public void executer(String runId, ScenarioDef d, Map<String, String> saisie) {
        suivi.demarrer(runId, d.titre());
        Map<String, String> donnees = completer(d, saisie);
        String demande = donnees.getOrDefault("demande", "");
        String[] dernier = {"demande"};
        try {
            if (d.architecture().equals("react")) {
                suivi.noeud(runId, "demande", List.of(), Evenement.UTILISATEUR, "Demande", Evenement.OK, demande.isBlank() ? champsSaisis(donnees) : demande, null, null, null);
                agent(String.valueOf(d.agents().get("agent")).trim()).executer(
                        new Entree(runId, demande, donnees, List.of(), 1, null, new TraceNoeud(suivi, runId, "demande")));
                suivi.terminer(runId, "REPONSE_FINALE");
                return;
            }
            suivi.noeud(runId, "demande", List.of(), Evenement.UTILISATEUR, "Entrée", Evenement.OK, demande.isBlank() ? champsSaisis(donnees) : demande, null, null, null);
            switch (d.architecture()) {
                case "sequence" -> sequence(runId, d, donnees, demande, dernier);
                case "boucle-critique" -> boucle(runId, d, donnees, demande, dernier);
                case "superviseur" -> superviseur(runId, d, donnees, demande, dernier);
                default -> throw new ScenarioException("architecture inconnue : " + d.architecture());
            }
        } catch (RuntimeException e) {
            suivi.noeud(runId, "fin", List.of(dernier[0]), Evenement.FIN, "Erreur", Evenement.ERREUR, String.valueOf(e.getMessage()), null, null, null);
            suivi.terminer(runId, "ERREUR");
            throw e;
        }
    }

    private void sequence(String runId, ScenarioDef d, Map<String, String> donnees, String demande, String[] dernier) {
        Resultat precedent = null;
        String texte = demande;
        int i = 0;
        for (String nom : liste(d.agents().get("etapes"))) {
            i++;
            String id = "s" + i;
            precedent = appeler(runId, id, List.of(dernier[0]), Evenement.AGENT, nom, nom,
                    new Entree(runId, texte, donnees, List.of(), i, precedent, null));
            dernier[0] = id;
            texte = precedent.texte();
        }
        suivi.noeud(runId, "fin", List.of(dernier[0]), Evenement.FIN, "Résultat", Evenement.OK, null, precedent == null ? "" : precedent.texte(), null, null);
        suivi.terminer(runId, "REPONSE_FINALE");
    }

    private void boucle(String runId, ScenarioDef d, Map<String, String> donnees, String demande, String[] dernier) {
        String producteur = String.valueOf(d.agents().get("producteur")).trim();
        String critique = String.valueOf(d.agents().get("critique")).trim();
        int max = entier(d.options().get("maxTours"), 3);
        List<String> retours = List.of();
        Decision finale = null;
        int tours = 0;
        for (int t = 1; t <= max; t++) {
            tours = t;
            Resultat p = appeler(runId, "p" + t, List.of(dernier[0]), Evenement.AGENT, producteur + " · tour " + t, producteur,
                    new Entree(runId, demande, donnees, retours, t, null, null));
            Resultat c = appeler(runId, "c" + t, List.of("p" + t), Evenement.CRITIQUE, critique + " · tour " + t, critique,
                    new Entree(runId, p.texte(), donnees, retours, t, p, null));
            dernier[0] = "c" + t;
            finale = c.decision() == null ? Decision.DOUTE : c.decision();
            if (finale != Decision.REJETE) { break; }
            retours = c.remarques();
        }
        String motif = switch (finale == null ? Decision.DOUTE : finale) {
            case APPROUVE -> "APPROUVEE";
            case DOUTE -> "VERS_HUMAIN";
            case REJETE -> "NON_CONVERGENTE";
        };
        if (booleen(d.options().get("validationHumaine"), true)) {
            suivi.noeud(runId, "humain", List.of(dernier[0]), Evenement.HUMAIN, "Validation humaine", Evenement.ATTENTE,
                    "Aucune décision n'est appliquée sans le personnel.", null, null, null);
            dernier[0] = "humain";
        }
        suivi.noeud(runId, "fin", List.of(dernier[0]), Evenement.FIN, "Résultat : " + motif,
                finale == Decision.APPROUVE ? Evenement.OK : Evenement.ERREUR, tours + " tour(s)", null, null, null);
        suivi.terminer(runId, motif);
    }

    private void superviseur(String runId, ScenarioDef d, Map<String, String> donnees, String demande, String[] dernier) {
        String planificateur = String.valueOf(d.agents().get("planificateur")).trim();
        Map<String, String> travailleurs = travailleurs(d);
        Resultat plan = appeler(runId, "plan", List.of("demande"), Evenement.AGENT, planificateur + " · plan", planificateur,
                new Entree(runId, demande, donnees, List.of(), 1, null, null));
        dernier[0] = "plan";
        List<String> termines = new ArrayList<>();
        StringBuilder reponses = new StringBuilder();
        int i = 0;
        for (Tache t : plan.taches()) {
            i++;
            String id = "t" + i;
            String bean = travailleurs.get(t.travailleur());
            if (bean == null) {
                suivi.noeud(runId, id, List.of("plan"), Evenement.TRAVAILLEUR, String.valueOf(t.travailleur()), Evenement.ERREUR,
                        "Le plan demande un travailleur inconnu : " + t.travailleur() + " (déclarés : " + String.join(", ", travailleurs.keySet()) + ")", null, null, null);
                reponses.append(t.travailleur()).append(" : (travailleur inconnu)\n");
                termines.add(id);
                continue;
            }
            try {
                Resultat r = appeler(runId, id, List.of("plan"), Evenement.TRAVAILLEUR, t.travailleur() + " (" + bean + ")", bean,
                        new Entree(runId, t.consigne(), donnees, List.of(), i, plan, null));
                reponses.append(t.travailleur()).append(" : ").append(r.texte()).append('\n');
            } catch (RuntimeException e) {
                // Un travailleur qui échoue ne fait pas échouer tout le plan : la synthèse sait ce qui manque.
                reponses.append(t.travailleur()).append(" : (échec : ").append(e.getMessage()).append(")\n");
            }
            termines.add(id);
        }
        String synthese = d.agents().get("synthese") == null ? null : String.valueOf(d.agents().get("synthese")).trim();
        String texteFinal;
        if (synthese != null) {
            Resultat s = appeler(runId, "synthese", termines.isEmpty() ? List.of("plan") : termines, Evenement.AGENT, synthese + " · synthèse", synthese,
                    new Entree(runId, demande, donnees, List.of(), 1, Resultat.texte(reponses.toString()), null));
            dernier[0] = "synthese";
            texteFinal = s.texte();
        } else {
            dernier[0] = termines.isEmpty() ? "plan" : termines.get(termines.size() - 1);
            texteFinal = reponses.toString();
        }
        suivi.noeud(runId, "fin", synthese != null ? List.of("synthese") : (termines.isEmpty() ? List.of("plan") : termines), Evenement.FIN,
                "Réponse finale", Evenement.OK, null, texteFinal, null, null);
        suivi.terminer(runId, "REPONSE_FINALE");
    }

    /** Un appel d'agent = un nœud : EN_COURS, puis OK/ERREUR avec durée et résultat. Tokens et outils sont ajoutés par le suivi. */
    private Resultat appeler(String runId, String id, List<String> parents, String genre, String libelle, String bean, Entree e) {
        String detail = "Agent : " + bean + (e.retours().isEmpty() ? "" : "\nRemarques du critique à prendre en compte :\n- " + String.join("\n- ", e.retours()));
        suivi.noeud(runId, id, parents, genre, libelle, Evenement.EN_COURS, detail, null, null, null);
        long debut = System.nanoTime();
        Entree avecTrace = e.avecTrace(new TraceNoeud(suivi, runId, id));
        Resultat r;
        try (var p = SuiviOutils.ouvrir(suivi, runId, id)) {
            r = SuiviOutils.reessayer(suivi, runId, id, () -> agent(bean).executer(avecTrace));
        } catch (RuntimeException ex) {
            suivi.maj(runId, id, Evenement.ERREUR, null, ex.getClass().getSimpleName() + " : " + ex.getMessage(), ms(debut), null);
            throw ex;
        }
        if (r == null) { r = Resultat.texte(""); }
        boolean refus = genre.equals(Evenement.CRITIQUE) && r.decision() != Decision.APPROUVE;
        suivi.maj(runId, id, refus ? Evenement.ERREUR : Evenement.OK, null, r.texte(), ms(debut), null);
        return r;
    }

    private Agent agent(String nom) {
        Agent a = agents.get(nom);
        if (a == null) { throw new ScenarioException("Aucun agent nommé « " + nom + " »."); }
        return a;
    }

    // ------------------------------------------------------------------ utilitaires

    private static Map<String, String> completer(ScenarioDef d, Map<String, String> saisie) {
        Map<String, String> m = new LinkedHashMap<>();
        for (ChampEntree c : d.entree()) {
            String v = saisie == null ? null : saisie.get(c.id());
            m.put(c.id(), v == null || v.isBlank() ? c.defaut() : v.trim());
        }
        if (saisie != null) { saisie.forEach((k, v) -> m.putIfAbsent(k, v)); }
        return m;
    }

    private static String champsSaisis(Map<String, String> donnees) {
        return donnees.entrySet().stream().map(en -> en.getKey() + " : " + en.getValue()).reduce((a, b) -> a + "\n" + b).orElse("(aucune saisie)");
    }

    static Map<String, String> travailleurs(ScenarioDef d) {
        Map<String, String> r = new LinkedHashMap<>();
        if (d.agents().get("travailleurs") instanceof Map<?, ?> m) {
            m.forEach((k, v) -> r.put(String.valueOf(k), String.valueOf(v).trim()));
        }
        return r;
    }

    static List<String> liste(Object o) {
        if (o instanceof List<?> l) { return l.stream().map(x -> String.valueOf(x).trim()).toList(); }
        return o == null ? List.of() : List.of(String.valueOf(o).trim());
    }

    static int entier(Object o, int defaut) {
        if (o instanceof Number n) { return n.intValue(); }
        try { return o == null ? defaut : Integer.parseInt(String.valueOf(o).trim()); } catch (NumberFormatException e) { return defaut; }
    }

    static boolean booleen(Object o, boolean defaut) {
        if (o instanceof Boolean b) { return b; }
        return o == null ? defaut : Boolean.parseBoolean(String.valueOf(o).trim());
    }

    private static long ms(long debutNanos) { return (System.nanoTime() - debutNanos) / 1_000_000; }
}
