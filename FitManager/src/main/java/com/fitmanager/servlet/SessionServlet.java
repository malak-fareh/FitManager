package com.fitmanager.servlet;

import java.io.IOException;

import com.fitmanager.model.Utilisateur;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * JSON minimal pour lâ€™accueil (affichage connectÃ© / invitÃ©).
 */
public class SessionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.getWriter().write("{\"loggedIn\":false}");
            return;
        }
        Object o = session.getAttribute(LoginServlet.SESSION_USER);
        if (!(o instanceof Utilisateur)) {
            resp.getWriter().write("{\"loggedIn\":false}");
            return;
        }
        Utilisateur u = (Utilisateur) o;

        String agePart = u.getAge() != null ? String.valueOf(u.getAge()) : "null";
        String json = String.format(
                "{\"loggedIn\":true,\"prenom\":%s,\"nom\":%s,\"email\":%s,\"pseudo\":%s,\"age\":%s,\"role\":%s}",
                quote(u.getPrenom()),
                quote(u.getNom()),
                quote(u.getEmail()),
                quote(u.getPseudo()),
                agePart,
                quote(u.getRole() != null ? u.getRole().name() : ""));
        resp.getWriter().write(json);
    }

    private static String quote(String s) {
        if (s == null) {
            return "null";
        }
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}
