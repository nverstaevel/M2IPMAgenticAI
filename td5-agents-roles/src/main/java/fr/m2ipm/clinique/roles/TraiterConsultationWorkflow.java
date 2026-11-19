package fr.m2ipm.clinique.roles;

import fr.m2ipm.clinique.dto.CreatureDto;
import fr.m2ipm.clinique.dto.SoigneurDto;
import fr.m2ipm.clinique.externe.AgendaPort;
import fr.m2ipm.clinique.externe.TarifsExcelReader;
import fr.m2ipm.clinique.service.CliniqueService;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Le même travail que le superviseur, en Java ordinaire et sans modèle : chemin d'exécution écrit à l'avance.
 * La table espèce + mot-clé du symptôme → médicament remplace le raisonnement du modèle.
 */
@Service
public class TraiterConsultationWorkflow {

    /** (espèce|mot-clé) → médicament. Ajouter une espèce ou un symptôme = ajouter une ligne ici. */
    static final Map<String, String> MEDICAMENTS = Map.of(
            "licorne|sabot", "Onguent de givre",
            "dragon|fievre", "Elixir de valeriane",
            "dragon|toux", "Baume ignifuge",
            "loup-garou|insomnie", "Elixir de valeriane",
            "yeti|allergie", "Onguent de givre",
            "vampire|fatigue", "Serum de sang-frais",
            "phenix|rhume", "Cendre de renaissance");

    public record Resultat(CreatureDto creature, SoigneurDto soigneur, String medicament, int stock,
                           double tarifConsultation, LocalDateTime creneau) {}

    public static class EchecWorkflow extends RuntimeException {
        public EchecWorkflow(String message) { super(message); }
    }

    private final CliniqueService clinique;
    private final TarifsExcelReader excel;
    private final AgendaPort agenda;

    public TraiterConsultationWorkflow(CliniqueService clinique, TarifsExcelReader excel, AgendaPort agenda) {
        this.clinique = clinique;
        this.excel = excel;
        this.agenda = agenda;
    }

    public Resultat traiter(String nomCreature, String symptome, LocalDate jour) {
        CreatureDto creature = clinique.rechercherProprietaires("").stream()
                .flatMap(p -> clinique.creaturesDuProprietaire(p.id()).stream())
                .filter(c -> c.nom().equalsIgnoreCase(nomCreature.trim())).findFirst()
                .orElseThrow(() -> new EchecWorkflow("Créature inconnue : " + nomCreature));
        List<SoigneurDto> soigneurs = clinique.soigneursPourEspece(creature.espece());
        if (soigneurs.isEmpty()) throw new EchecWorkflow("Aucun soigneur compétent pour " + creature.espece());
        String medicament = MEDICAMENTS.entrySet().stream()
                .filter(e -> e.getKey().startsWith(plat(creature.espece()) + "|") && plat(symptome).contains(e.getKey().split("\\|")[1]))
                .map(Map.Entry::getValue).findFirst()
                .orElseThrow(() -> new EchecWorkflow("Pas de médicament connu pour " + creature.espece() + " / " + symptome));
        int stock = excel.stock(medicament).orElse(0);
        if (stock <= 0) throw new EchecWorkflow("Médicament en rupture : " + medicament);
        double tarif = excel.tarif("Consultation", creature.espece())
                .orElseThrow(() -> new EchecWorkflow("Pas de tarif pour " + creature.espece()));
        LocalTime premier = agenda.creneauxLibres(jour).stream().findFirst()
                .orElseThrow(() -> new EchecWorkflow("Aucun créneau libre le " + jour));
        return new Resultat(creature, soigneurs.get(0), medicament, stock, tarif, jour.atTime(premier));
    }

    private static String plat(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
