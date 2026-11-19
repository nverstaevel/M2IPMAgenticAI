package fr.m2ipm.clinique.garde;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Horloge de l'application ; {@code clinique.horloge=2026-03-01T08:00:00Z} la fige (tests reproductibles). */
@Configuration
public class HorlogeConfig {

    @Bean
    Clock horloge(@Value("${clinique.horloge:}") String fixe) {
        return fixe.isBlank() ? Clock.systemDefaultZone() : Clock.fixed(Instant.parse(fixe), ZoneId.of("Europe/Paris"));
    }
}
