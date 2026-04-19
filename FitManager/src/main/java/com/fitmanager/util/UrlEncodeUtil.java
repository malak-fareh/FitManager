package com.fitmanager.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;

import com.fitmanager.model.Utilisateur;

public final class UrlEncodeUtil {

    private UrlEncodeUtil() {
    }

    public static String encodeUtf8(String s) {
        if (s == null) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /** ParamÃ¨tres GET pour la page Â« inscription rÃ©ussie Â». */
    public static String registrationSuccessQuery(int id, Utilisateur u) throws UnsupportedEncodingException {
        return "id=" + id
                + "&prenom=" + URLEncoder.encode(u.getPrenom(), "UTF-8")
                + "&nom=" + URLEncoder.encode(u.getNom(), "UTF-8")
                + "&email=" + URLEncoder.encode(u.getEmail(), "UTF-8")
                + "&pseudo=" + URLEncoder.encode(u.getPseudo(), "UTF-8")
                + "&age=" + u.getAge();
    }

    /** Message court pour lâ€™URL (dÃ©bogage local : cause MySQL visible). */
    public static String encodeSqlHint(SQLException e) {
        String detail = e.getMessage();
        if (detail == null) {
            detail = e.getClass().getName();
        }
        String text = "MySQL : " + detail;
        if (text.length() > 240) {
            text = text.substring(0, 237) + "...";
        }
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return "";
        }
    }
}
