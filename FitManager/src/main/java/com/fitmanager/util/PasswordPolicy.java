package com.fitmanager.util;

/**
 * RÃ¨gles : au moins 8 caractÃ¨res, majuscule, minuscule, chiffre, caractÃ¨re spÃ©cial.
 */
public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    /** @return message dâ€™erreur en franÃ§ais, ou {@code null} si le mot de passe est acceptable */
    public static String validate(String password) {
        if (password == null || password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractÃ¨res.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Le mot de passe doit contenir au moins une lettre minuscule.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Le mot de passe doit contenir au moins une lettre majuscule.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Le mot de passe doit contenir au moins un chiffre.";
        }
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            return "Le mot de passe doit contenir au moins un caractÃ¨re spÃ©cial (ex. ! @ # $ % & *).";
        }
        return null;
    }
}
