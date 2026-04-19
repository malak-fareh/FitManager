package com.fitmanager.servlet;

import com.fitmanager.dao.UtilisateurDAO;
import com.fitmanager.model.Utilisateur;
import com.fitmanager.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/register.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String email      = req.getParameter("email");
        String pseudo     = firstNonBlank(
                req.getParameter("pseudo"),
                req.getParameter("email"),
                req.getParameter("nom")
        );
        String motDePasse = firstNonBlank(
                req.getParameter("motDePasse"),
                req.getParameter("password")
        );
        String nom        = req.getParameter("nom");
        String prenom     = req.getParameter("prenom");
        String ageParam   = req.getParameter("age");

        if (isBlank(email) || isBlank(pseudo) || isBlank(motDePasse) || isBlank(nom) || isBlank(prenom)) {
            if (!isBlank(nom) && isBlank(prenom)) {
                String[] parts = nom.trim().split("\\s+");
                if (parts.length >= 2) {
                    prenom = parts[0];
                    nom = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                } else {
                    prenom = nom.trim();
                }
            }
            if (isBlank(email) || isBlank(pseudo) || isBlank(motDePasse) || isBlank(nom) || isBlank(prenom)) {
                resp.sendRedirect(req.getContextPath() + "/register.html?error=missing");
                return;
            }
        }

        try {
            UtilisateurDAO dao = new UtilisateurDAO(req.getServletContext());
            if (dao.existsByEmail(email.trim())) {
                resp.sendRedirect(req.getContextPath() + "/register.html?error=email");
                return;
            }
            if (dao.existsByPseudo(pseudo.trim())) {
                resp.sendRedirect(req.getContextPath() + "/register.html?error=pseudo");
                return;
            }
            Utilisateur u = new Utilisateur();
            u.setEmail(email.trim());
            u.setPseudo(pseudo.trim());
            u.setMotDePasse(PasswordUtil.hash(motDePasse));
            u.setNom(nom.trim());
            u.setPrenom(prenom.trim());
            u.setRole(Utilisateur.Role.ADHERENT);
            if (ageParam != null && !ageParam.trim().isEmpty()) {
                try { u.setAge(Integer.parseInt(ageParam.trim())); }
                catch (NumberFormatException ignored) {}
            }
            dao.insert(u);
            resp.sendRedirect(req.getContextPath() + "/login.html?registered=1");
        } catch (SQLException e) {
            getServletContext().log("Inscription : erreur SQL", e);
            resp.sendRedirect(req.getContextPath() + "/register.html?error=server");
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
