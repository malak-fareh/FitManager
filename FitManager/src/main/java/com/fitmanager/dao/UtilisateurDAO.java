package com.fitmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import com.fitmanager.model.Utilisateur;
import com.fitmanager.model.Utilisateur.Role;
import com.fitmanager.util.DBConnection;

import javax.servlet.ServletContext;

public class UtilisateurDAO {

    private final ServletContext ctx;

    public UtilisateurDAO(ServletContext ctx) {
        this.ctx = ctx;
    }

    public Optional<Utilisateur> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, email, pseudo, mot_de_passe, nom, prenom, age, role FROM utilisateur WHERE email = ?";
        try (Connection c = DBConnection.getConnection(ctx);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }
    }

    public Optional<Utilisateur> findByPseudo(String pseudo) throws SQLException {
        String sql = "SELECT id, email, pseudo, mot_de_passe, nom, prenom, age, role FROM utilisateur WHERE pseudo = ?";
        try (Connection c = DBConnection.getConnection(ctx);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pseudo.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }
    }

    /** Connexion avec e-mail ou nom dâ€™utilisateur. */
    public Optional<Utilisateur> findByLogin(String identifiant) throws SQLException {
        String s = identifiant.trim();
        if (s.contains("@")) {
            return findByEmail(s);
        }
        return findByPseudo(s);
    }

    public boolean existsByEmail(String email) throws SQLException {
        return findByEmail(email).isPresent();
    }

    public boolean existsByPseudo(String pseudo) throws SQLException {
        return findByPseudo(pseudo).isPresent();
    }

    public int insert(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateur (email, pseudo, mot_de_passe, nom, prenom, age, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection(ctx);
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getEmail().trim().toLowerCase());
            ps.setString(2, u.getPseudo().trim().toLowerCase());
            ps.setString(3, u.getMotDePasse());
            ps.setString(4, u.getNom().trim());
            ps.setString(5, u.getPrenom().trim());
            if (u.getAge() != null) { ps.setInt(6, u.getAge()); }
else { ps.setNull(6, java.sql.Types.INTEGER); }
            ps.setString(7, u.getRole().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Insertion utilisateur sans clÃ© gÃ©nÃ©rÃ©e.");
    }

    private static Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setPseudo(rs.getString("pseudo"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setAge(rs.getInt("age"));
        if (rs.wasNull()) {
            u.setAge(null);
        }
        u.setRole(Role.valueOf(rs.getString("role")));
        return u;
    }
}
