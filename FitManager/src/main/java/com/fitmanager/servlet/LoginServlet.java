package com.fitmanager.servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

import com.fitmanager.dao.UtilisateurDAO;
import com.fitmanager.model.Utilisateur;
import com.fitmanager.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

    public static final String SESSION_USER = "utilisateur";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String identifiant = firstNonBlank(
                req.getParameter("identifiant"),
                req.getParameter("email"),
                req.getParameter("pseudo")
        );
        String motDePasse  = firstNonBlank(
                req.getParameter("motDePasse"),
                req.getParameter("password")
        );

        if (isBlank(identifiant) || isBlank(motDePasse)) {
            resp.sendRedirect(req.getContextPath() + "/login.html?error=missing");
            return;
        }

        try {
            UtilisateurDAO dao = new UtilisateurDAO(req.getServletContext());
            Optional<Utilisateur> opt = dao.findByLogin(identifiant);
            System.out.println("IDENTIFIANT: " + identifiant);
            System.out.println("USER FOUND: " + opt.isPresent());
            if (!opt.isPresent() || !PasswordUtil.verify(motDePasse, opt.get().getMotDePasse())) {
                resp.sendRedirect(req.getContextPath() + "/login.html?error=cred");
                return;
            }
            System.out.println("LOGIN SUCCESS BLOCK REACHED");
            Utilisateur u = opt.get().sansMotDePasse();
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute(SESSION_USER, u);
            resp.sendRedirect(req.getContextPath() + "/index.html?connected=1");
        } catch (SQLException e) {
            getServletContext().log("Connexion : erreur SQL", e);
            resp.sendRedirect(req.getContextPath() + "/login.html?error=server");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
