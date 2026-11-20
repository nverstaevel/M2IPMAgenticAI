package fr.m2ipm.clinique.roles;

import fr.m2ipm.clinique.agent.Evenement;
import fr.m2ipm.clinique.agent.SuiviExecution;
import fr.m2ipm.clinique.agent.SuiviOutils;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Boucle actor-critic : le vétérinaire propose, le gardien critique, trois tours au plus. */
@Service
public class DuoService {

    public static final int MAX_TOURS = 3;

    private final VeterinaireAgent veterinaire;
    private final GardienDuBestiaire gardien;
    private final Clock horloge;
    private final String modele;
    private final SuiviExecution suivi;
    private final Map<String, DossierRun> runs = new ConcurrentHashMap<>();

    @Autowired
    public DuoService(VeterinaireAgent veterinaire, GardienDuBestiaire gardien, SuiviExecution suivi,
                      @Value("${spring.ai.model.chat:inconnu}") String modele) {
        this(veterinaire, gardien, Clock.systemDefaultZone(), modele, suivi);
    }

    public DuoService(VeterinaireAgent veterinaire, GardienDuBestiaire gardien, Clock horloge, String modele) {
        this(veterinaire, gardien, horloge, modele, new SuiviExecution());
    }

    public DuoService(VeterinaireAgent veterinaire, GardienDuBestiaire gardien, Clock horloge, String modele, SuiviExecution suivi) {
        this.veterinaire = veterinaire;
        this.gardien = gardien;
        this.horloge = horloge;
        this.modele = modele;
        this.suivi = suivi;
    }

    public DossierRun traiter(CasBestiaire cas, ProfilActeur profil) {
        return traiter(UUID.randomUUID().toString(), cas, profil);
    }

    /** Le runId est choisi par l'appelant pour que la page /agents suive l'exécution avant sa fin. */
    public DossierRun traiter(String runId, CasBestiaire cas, ProfilActeur profil) {
        suivi.demarrer(runId, "Duo : cas " + cas.id() + " · " + cas.creature() + " (" + profil + ")");
        suivi.noeud(runId, "cas", List.of(), Evenement.UTILISATEUR, "Cas " + cas.id() + " · " + cas.creature(), Evenement.OK,
                cas.espece() + " — symptôme : " + cas.symptome() + "\nProfil du vétérinaire : " + profil, null, null, null);
        long debut = System.nanoTime();
        List<Tour> tours = new ArrayList<>();
        List<String> objections = List.of();
        int caracteres = 0;
        StatutDuo statut = StatutDuo.NON_CONVERGENTE;
        String precedent = "cas";
        for (int numero = 1; numero <= MAX_TOURS; numero++) {
            String idV = "v" + numero, idG = "g" + numero;
            Diagnostic d;
            Critique c;
            String enCours = idV;
            try {
                suivi.noeud(runId, idV, List.of(precedent), Evenement.AGENT, "Vétérinaire · tour " + numero, Evenement.EN_COURS,
                        objections.isEmpty() ? null : "Objections du gardien à prendre en compte :\n- " + String.join("\n- ", objections),
                        null, null, null);
                long t0 = System.nanoTime();
                try (var p = SuiviOutils.ouvrir(suivi, runId, idV)) {
                    d = veterinaire.proposer(cas, profil, objections);
                }
                var invalide = verifier(d);
                if (invalide != null) {
                    suivi.maj(runId, idV, Evenement.ERREUR, null, "Sortie refusée : " + invalide, ms(t0), null);
                    tours.add(tourIllisible(numero, cas, "Sortie du vétérinaire refusée : " + invalide));
                    precedent = idV;
                    statut = StatutDuo.VERS_HUMAIN;
                    break;
                }
                suivi.maj(runId, idV, Evenement.OK, resume(d), null, ms(t0), null);

                enCours = idG;
                suivi.noeud(runId, idG, List.of(idV), Evenement.CRITIQUE, "Gardien · tour " + numero, Evenement.EN_COURS,
                        "Examine : " + d.diagnostic() + " — " + String.join(" ; ", d.traitements()), null, null, null);
                long t1 = System.nanoTime();
                try (var p = SuiviOutils.ouvrir(suivi, runId, idG)) {
                    c = normaliser(gardien.examiner(cas.espece(), d));
                }
                suivi.maj(runId, idG, c.verdict() == Verdict.APPROUVE ? Evenement.OK : Evenement.ERREUR,
                        "Verdict : " + c.verdict(), resume(c), ms(t1), null);
                precedent = idG;
            } catch (RuntimeException e) {
                // Sortie illisible ou erreur du modèle : on ne devine pas, on renvoie à un humain.
                suivi.maj(runId, enCours, Evenement.ERREUR, null, "Sortie illisible ou erreur du modèle : " + e.getMessage(), null, null);
                tours.add(tourIllisible(numero, cas, "Sortie illisible ou erreur du modèle : " + e.getMessage()));
                precedent = enCours;
                statut = StatutDuo.VERS_HUMAIN;
                break;
            }
            // Filet déterministe : un traitement contre-indiqué par la clinique n'est jamais APPROUVE (A7).
            var regle = c.verdict() == Verdict.APPROUVE ? fr.m2ipm.clinique.garde.ReglesDures.contreIndication(cas.espece(), d.traitements()) : java.util.Optional.<String>empty();
            if (regle.isPresent()) {
                var objectionsRegle = new ArrayList<>(c.objections());
                objectionsRegle.add(regle.get());
                c = new Critique(Verdict.REJETE, objectionsRegle, c.sources());
                String idR = "r" + numero;
                suivi.noeud(runId, idR, List.of(idG), Evenement.CODE, "Règle dure : contre-indication", Evenement.ERREUR,
                        regle.get(), "L'approbation du gardien est annulée (REJETE).", null, null);
                precedent = idR;
            }
            tours.add(new Tour(numero, d, c));
            caracteres += d.toString().length() + c.toString().length();
            if (c.verdict() == Verdict.APPROUVE) { statut = StatutDuo.APPROUVEE; break; }
            if (c.verdict() == Verdict.DOUTE) { statut = StatutDuo.VERS_HUMAIN; break; }
            objections = c.objections() == null ? List.of() : c.objections();
        }
        long ms = (System.nanoTime() - debut) / 1_000_000;
        var run = new DossierRun(runId, cas, profil, tours, statut, ms, caracteres / 4,
                modele, Instant.now(horloge));
        runs.put(run.runId(), run);

        suivi.noeud(runId, "humain", List.of(precedent), Evenement.HUMAIN, "Validation humaine (PERSONNEL)", Evenement.ATTENTE,
                "Aucun traitement n'est appliqué sans la décision d'un membre du personnel : POST /api/dossiers/" + runId + "/valider",
                null, null, null);
        suivi.noeud(runId, "fin", List.of("humain"), Evenement.FIN, "Dossier : " + statut,
                statut == StatutDuo.APPROUVEE ? Evenement.OK : Evenement.ERREUR,
                tours.size() + " tour(s), dernier verdict : " + tours.get(tours.size() - 1).critique().verdict(),
                null, ms, run.tokensEstimes());
        suivi.terminer(runId, statut.name());
        return run;
    }

    private static String resume(Diagnostic d) {
        return "Diagnostic : " + d.diagnostic() + "\nTraitements : " + String.join(" ; ", d.traitements()) + "\nJustification : " + d.justification();
    }

    private static String resume(Critique c) {
        return "Verdict : " + c.verdict()
                + (c.objections() == null || c.objections().isEmpty() ? "" : "\nObjections :\n- " + String.join("\n- ", c.objections()))
                + (c.sources() == null || c.sources().isEmpty() ? "" : "\nSources : " + String.join(" ; ", c.sources()));
    }

    private static long ms(long debutNanos) { return (System.nanoTime() - debutNanos) / 1_000_000; }

    /** Bornes sur la sortie du vétérinaire : au moins un et au plus 5 traitements, de 200 caractères au plus. */
    private static String verifier(Diagnostic d) {
        if (d == null || d.traitements() == null || d.traitements().isEmpty()) return "aucun traitement";
        if (d.traitements().size() > 5) return "plus de 5 traitements";
        if (d.traitements().stream().anyMatch(t -> t == null || t.isBlank() || t.length() > 200)) return "traitement vide ou trop long";
        return null;
    }

    /** Verdict absent = DOUTE (échec fermé) ; listes absentes = vides. */
    private static Critique normaliser(Critique c) {
        if (c == null) return new Critique(Verdict.DOUTE, List.of("Critique absente"), List.of());
        return new Critique(c.verdict() == null ? Verdict.DOUTE : c.verdict(),
                c.objections() == null ? List.of() : c.objections(), c.sources() == null ? List.of() : c.sources());
    }

    private static Tour tourIllisible(int numero, CasBestiaire cas, String raison) {
        return new Tour(numero, new Diagnostic(cas.creature(), "(aucun diagnostic exploitable)", List.of(), raison),
                new Critique(Verdict.DOUTE, List.of(raison), List.of()));
    }

    public Optional<DossierRun> run(String runId) { return Optional.ofNullable(runs.get(runId)); }

    public Collection<DossierRun> runs() { return runs.values(); }
}
