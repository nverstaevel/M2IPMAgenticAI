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
            Diagnostic d = veterinaire.proposer(cas, profil, objections);
            Critique c = gardien.examiner(cas.espece(), d);
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

    public Optional<DossierRun> run(String runId) { return Optional.ofNullable(runs.get(runId)); }

    public Collection<DossierRun> runs() { return runs.values(); }
}
