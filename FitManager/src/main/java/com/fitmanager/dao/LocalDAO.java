package com.fitmanager.dao;

import com.fitmanager.model.Local;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour la table `local`.
 * NOTE : `local` est un mot rÃ©servÃ© MySQL â€” toutes les requÃªtes utilisent des backticks.
 */
public class LocalDAO {

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    public static Local mapRow(ResultSet rs) throws SQLException {
        return new Local(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("adresse"),
                rs.getInt("capacite_max")
        );
    }

    // -------------------------------------------------------------------------
    // Lectures
    // -------------------------------------------------------------------------

    public List<Local> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM `local` ORDER BY nom";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Local> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    public Optional<Local> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM `local` WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        }
    }

    public Optional<Local> findByNom(Connection conn, String nom) throws SQLException {
        String sql = "SELECT * FROM `local` WHERE nom = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Ã‰critures
    // -------------------------------------------------------------------------

    public int insert(Connection conn, Local local) throws SQLException {
        String sql = "INSERT INTO `local` (nom, adresse, capacite_max) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, local.getNom().trim());
            ps.setString(2, local.getAdresse().trim());
            ps.setInt   (3, local.getCapaciteMax());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Insertion local Ã©chouÃ©e, aucune ligne affectÃ©e.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Insertion local Ã©chouÃ©e, aucun ID gÃ©nÃ©rÃ©.");
            }
        }
    }

    public boolean update(Connection conn, Local local) throws SQLException {
        String sql = "UPDATE `local` SET nom = ?, adresse = ?, capacite_max = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, local.getNom().trim());
            ps.setString(2, local.getAdresse().trim());
            ps.setInt   (3, local.getCapaciteMax());
            ps.setInt   (4, local.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM `local` WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
