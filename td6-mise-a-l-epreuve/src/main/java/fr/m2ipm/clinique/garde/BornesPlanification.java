package fr.m2ipm.clinique.garde;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Bornes sur les dates proposées par le modèle : ni passé, ni au-delà d'un an, ni hors horaires (9h-18h, pas de 30 min). */
@Component
public class BornesPlanification {

    private final Clock horloge;

    public BornesPlanification(Clock horloge) { this.horloge = horloge; }

    /** Message d'erreur à renvoyer au modèle, ou vide si la date est acceptable. */
    public Optional<String> refus(LocalDateTime dh) {
        LocalDateTime maintenant = LocalDateTime.now(horloge);
        if (dh.isBefore(maintenant)) return Optional.of("Date refusée : " + dh + " est dans le passé.");
        if (dh.isAfter(maintenant.plusYears(1))) return Optional.of("Date refusée : " + dh + " est au-delà d'un an.");
        boolean heureOk = dh.getHour() >= 9 && dh.getHour() < 18 && (dh.getMinute() == 0 || dh.getMinute() == 30);
        if (!heureOk) return Optional.of("Date refusée : " + dh + " est hors des horaires (9h à 18h, créneaux de 30 minutes).");
        return Optional.empty();
    }
}
