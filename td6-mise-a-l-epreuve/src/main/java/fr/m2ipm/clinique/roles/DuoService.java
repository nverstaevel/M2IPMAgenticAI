package fr.m2ipm.clinique.roles;

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
    private final Map<String, DossierRun> runs = new ConcurrentHashMap<>();

    @Autowired
    public DuoService(VeterinaireAgent veterinaire, GardienDuBestiaire gardien,
                      @Value("${spring.ai.model.chat:inconnu}") String modele) {
        this(veterinaire, gardien, Clock.systemDefaultZone(), modele);
    }

    public DuoService(VeterinaireAgent veterinaire, GardienDuBestiaire gardien, Clock horloge, String modele) {
        this.veterinaire = veterinaire;
        this.gardien = gardien;
        this.horloge = horloge;
        this.modele = modele;
    }

    public DossierRun traiter(CasBestiaire cas, ProfilActeur profil) {
        long debut = System.nanoTime();
        List<Tour> tours = new ArrayList<>();
        List<String> objections = List.of();
        int caracteres = 0;
        StatutDuo statut = StatutDuo.NON_CONVERGENTE;
        for (int numero = 1; numero <= MAX_TOURS; numero++) {
            Diagnostic d;
            Critique c;
            try {
                d = veterinaire.proposer(cas, profil, objections);
                var invalide = verifier(d);
                if (invalide != null) {
                    tours.add(tourIllisible(numero, cas, "Sortie du vétérinaire refusée : " + invalide));
                    statut = StatutDuo.VERS_HUMAIN;
                    break;
                }
                c = normaliser(gardien.examiner(cas.espece(), d));
            } catch (RuntimeException e) {
                // Sortie illisible ou erreur du modèle : on ne devine pas, on renvoie à un humain.
                tours.add(tourIllisible(numero, cas, "Sortie illisible ou erreur du modèle : " + e.getMessage()));
                statut = StatutDuo.VERS_HUMAIN;
                break;
            }
            // Filet déterministe : un traitement contre-indiqué par la clinique n'est jamais APPROUVE (A7).
            var regle = c.verdict() == Verdict.APPROUVE ? fr.m2ipm.clinique.garde.ReglesDures.contreIndication(cas.espece(), d.traitements()) : java.util.Optional.<String>empty();
            if (regle.isPresent()) {
                var objectionsRegle = new ArrayList<>(c.objections());
                objectionsRegle.add(regle.get());
                c = new Critique(Verdict.REJETE, objectionsRegle, c.sources());
            }
            tours.add(new Tour(numero, d, c));
            caracteres += d.toString().length() + c.toString().length();
            if (c.verdict() == Verdict.APPROUVE) { statut = StatutDuo.APPROUVEE; break; }
            if (c.verdict() == Verdict.DOUTE) { statut = StatutDuo.VERS_HUMAIN; break; }
            objections = c.objections() == null ? List.of() : c.objections();
        }
        long ms = (System.nanoTime() - debut) / 1_000_000;
        var run = new DossierRun(UUID.randomUUID().toString(), cas, profil, tours, statut, ms, caracteres / 4,
                modele, Instant.now(horloge));
        runs.put(run.runId(), run);
        return run;
    }

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
