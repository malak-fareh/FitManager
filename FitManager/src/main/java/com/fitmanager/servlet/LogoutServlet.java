package com.fitmanager.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutServlet extends HttpServlet {
  @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Ligne 12-18 (ancien) : le GET invalidait la session â€” REMPLACE PAR une simple redirection
        // Un GET peut etre declenche par un lien ou une image externe (CSRF)
        // => le GET ne doit JAMAIS deconnecter, seulement rediriger vers login
        resp.sendRedirect(req.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Ligne 22 (ancien) : doGet(req, resp) REMPLACE PAR la vraie logique de deconnexion
        // La deconnexion ne se fait que sur POST (formulaire avec token CSRF)
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/index.html?logout=1");
    }
}
