package fr.m2ipm.clinique.garde;

import org.springframework.ai.chat.model.ToolContext;

/**
 * Contrôle d'accès DANS l'outil. Le prompt n'est pas un contrôle d'accès : un modèle poli ou persuadé reste un modèle.
 * Les rôles viennent du ToolContext rempli par le contrôleur depuis l'authentification, jamais du texte du modèle.
 * Absence de rôles = refus (échec fermé).
 */
public final class DroitsOutils {

    public static final String CLE = "roles";
    public static final String REFUS = "Action refusée : cette opération est réservée au personnel de la clinique.";

    private DroitsOutils() {}

    public static boolean estPersonnel(ToolContext contexte) {
        if (contexte == null || contexte.getContext() == null) return false;
        Object roles = contexte.getContext().get(CLE);
        return roles != null && roles.toString().contains("ROLE_PERSONNEL");
    }
}
