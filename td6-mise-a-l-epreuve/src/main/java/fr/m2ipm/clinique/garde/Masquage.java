package fr.m2ipm.clinique.garde;

import java.util.regex.Pattern;

/** Masque téléphones et courriels dans tout ce qui est tracé ou imprimé (audit sans donnée personnelle inutile). */
public final class Masquage {

    private static final Pattern TEL = Pattern.compile("(?<!\\d)(?:\\+33\\s?|0)[1-9](?:[\\s.-]?\\d{2}){4}(?!\\d)");
    private static final Pattern MAIL = Pattern.compile("[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+");

    private Masquage() {}

    public static String masquer(String texte) {
        if (texte == null) return null;
        return MAIL.matcher(TEL.matcher(texte).replaceAll("[tél. masqué]")).replaceAll("[courriel masqué]");
    }
}
