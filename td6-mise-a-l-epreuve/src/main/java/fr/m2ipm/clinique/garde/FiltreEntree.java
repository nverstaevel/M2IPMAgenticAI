package fr.m2ipm.clinique.garde;

import java.util.regex.Pattern;

/** Filtre grossier des injections directes (A1). Contournable par reformulation ; il ne remplace pas les droits dans les outils. */
public final class FiltreEntree {

    public static final String REFUS = "Je ne peux pas répondre à cette demande.";

    private static final Pattern INJECTION = Pattern.compile(
            "(?iu)(ignore[zr]?\\s+(tes|vos|toutes?\\s+(tes|vos|les)|all|your|previous)\\s+(instructions?|consignes?)"
                    + "|(recopie|affiche|révèle|reveal|show|print)\\s+(ton|votre|your)\\s+(prompt|instructions?)"
                    + "|(prompt|message)\\s+syst[eè]me|system\\s+prompt)");

    private FiltreEntree() {}

    public static boolean suspect(String message) { return message != null && INJECTION.matcher(message).find(); }
}
