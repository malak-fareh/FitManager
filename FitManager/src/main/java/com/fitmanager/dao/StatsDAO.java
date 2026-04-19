package com.fitmanager.dao;

import com.fitmanager.model.Abonnement;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatsDAO {

    // -------------------------------------------------------------------------
    // Classes internes de rÃ©sultat
    // -------------------------------------------------------------------------

    public static class LocalStat {
        private final int    localId;
        private final String nom;
        private final int    count;

        public LocalStat(int localId, String nom, int count) {
            this.localId = localId;
            this.nom     = nom;
            this.count   = count;
        }

        public int    getLocalId() { return localId; }
        public String getNom()     { return nom; }
        public int    getCount()   { return count; }
    }

    public static class MonthlyStat {
        private final int year;
        private final int month;
        private final int count;

        public MonthlyStat(int year, int month, int count) {
            this.year  = year;
            this.month = month;
            this.count = count;
        }

        public int getYear()  { return year; }
        public int getMonth() { return month; }
        public int getCount() { return count; }
    }

    // -------------------------------------------------------------------------
    // RequÃªtes
    // -------------------------------------------------------------------------

    public int countTotal(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM abonnement";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * CORRECTION : statut 'actif' passÃ© en paramÃ¨tre prÃ©parÃ©
     * au lieu d'Ãªtre hardcodÃ© dans la chaÃ®ne SQL.
     */
    public int countActive(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM abonnement WHERE statut = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Abonnement.Statut.ACTIF.toString());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countByStatus(Connection conn, Abonnement.Statut statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM abonnement WHERE statut = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, (statut == null) ? Abonnement.Statut.ACTIF.toString() : statut.toString());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * NOTE : le nom de table `local` est un mot rÃ©servÃ© MySQL.
     * Les backticks sont conservÃ©s. Si possible, renommez la table
     * en `salle` ou `branche` pour Ã©viter tout conflit futur.
     */
    public List<LocalStat> countByLocal(Connection conn) throws SQLException {
        String sql = "SELECT l.id, l.nom, COUNT(a.id) AS cnt "
                   + "FROM `local` l "
                   + "LEFT JOIN abonnement a ON l.id = a.local_id "
                   + "GROUP BY l.id, l.nom "
                   + "ORDER BY cnt DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<LocalStat> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new LocalStat(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getInt("cnt")));
            }
            return list;
        }
    }

    public List<MonthlyStat> subscriptionsPerMonth(Connection conn, int monthsBack) throws SQLException {
        // monthsBack doit Ãªtre >= 1
        LocalDate start = LocalDate.now()
                .minusMonths(Math.max(1, monthsBack) - 1)
                .withDayOfMonth(1);

        String sql = "SELECT YEAR(date_debut) AS y, MONTH(date_debut) AS m, COUNT(*) AS cnt "
                   + "FROM abonnement "
                   + "WHERE date_debut >= ? "
                   + "GROUP BY y, m "
                   + "ORDER BY y DESC, m DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                List<MonthlyStat> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new MonthlyStat(
                            rs.getInt("y"),
                            rs.getInt("m"),
                            rs.getInt("cnt")));
                }
                return list;
            }
        }
    }
}
