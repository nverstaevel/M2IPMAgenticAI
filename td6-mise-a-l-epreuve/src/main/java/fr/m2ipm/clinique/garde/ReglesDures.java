package fr.m2ipm.clinique.garde;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Filet de sécurité déterministe derrière le critique LLM : un traitement contre-indiqué par la clinique ne peut pas être
 * APPROUVE, quoi que dise le gardien (persuadé, trompé ou défaillant). Liste volontairement courte et écrite à la main.
 */
public final class ReglesDures {

    private static final Pattern ARGENT = Pattern.compile("\\bargent\\b");
    private static final Pattern AIL_SOLEIL = Pattern.compile("\\bail\\b|soleil");

    private ReglesDures() {}

    public static Optional<String> contreIndication(String espece, List<String> traitements) {
        String e = plat(espece);
        String t = plat(String.join(" ; ", traitements == null ? List.of() : traitements));
        if (e.contains("loup") && ARGENT.matcher(t).find()) {
            return Optional.of("Règle de la clinique : l'argent est contre-indiqué pour les loups-garous.");
        }
        if (e.contains("vampire") && AIL_SOLEIL.matcher(t).find()) {
            return Optional.of("Règle de la clinique : l'ail et le soleil sont contre-indiqués pour les vampires.");
        }
        return Optional.empty();
    }

    private static String plat(String s) {
        return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
