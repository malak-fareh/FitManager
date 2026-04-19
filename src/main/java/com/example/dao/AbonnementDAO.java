package main.java.com.example.dao;

import main.java.com.example.model.Abonnement;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbonnementDAO {

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    /**
     * Construit un Abonnement à partir de la ligne courante du ResultSet.
     * Gère l'exception de fromString pour ne pas propager une valeur inconnue
     * en erreur fatale : on retombe sur ACTIF avec un avertissement log.
     */
    public static Abonnement mapRow(ResultSet rs) throws SQLException {
        int id          = rs.getInt("id");
        int adherentId  = rs.getInt("adherent_id");
        int localId     = rs.getInt("local_id");
        Date dDeb       = rs.getDate("date_debut");
        Date dFin       = rs.getDate("date_fin");
        LocalDate dateDebut = (dDeb == null) ? null : dDeb.toLocalDate();
        LocalDate dateFin   = (dFin == null) ? null : dFin.toLocalDate();

        Abonnement.Statut statut;
        try {
            statut = Abonnement.Statut.fromString(rs.getString("statut"));
        } catch (IllegalArgumentException e) {
            // Valeur inconnue en base : on log et on retombe sur ACTIF
            System.err.println("[AbonnementDAO] Statut inconnu pour id=" + id + " : " + e.getMessage());
            statut = Abonnement.Statut.ACTIF;
        }

        return new Abonnement(id, adherentId, localId, dateDebut, dateFin, statut);
    }

    // -------------------------------------------------------------------------
    // Lectures
    // -------------------------------------------------------------------------

    public List<Abonnement> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM abonnement";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Abonnement> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    public Optional<Abonnement> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM abonnement WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        }
    }

    public List<Abonnement> findByAdherentId(Connection conn, int adherentId) throws SQLException {
        String sql = "SELECT * FROM abonnement WHERE adherent_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adherentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Abonnement> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    /**
     * CORRECTION : le statut n'est plus hardcodé en String littéral ;
     * on utilise un paramètre préparé pour éviter les typos et rester
     * cohérent avec l'enum Statut.
     */
    public List<Abonnement> findActiveByLocal(Connection conn, int localId) throws SQLException {
        String sql = "SELECT * FROM abonnement WHERE local_id = ? AND statut = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, localId);
            ps.setString(2, Abonnement.Statut.ACTIF.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<Abonnement> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Écritures
    // -------------------------------------------------------------------------

    public int insert(Connection conn, Abonnement a) throws SQLException {
        String sql = "INSERT INTO abonnement (adherent_id, local_id, date_debut, date_fin, statut) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getAdherentId());
            ps.setInt(2, a.getLocalId());
            ps.setDate(3, (a.getDateDebut() == null) ? null : Date.valueOf(a.getDateDebut()));
            ps.setDate(4, (a.getDateFin()   == null) ? null : Date.valueOf(a.getDateFin()));
            ps.setString(5, (a.getStatut()  == null)
                    ? Abonnement.Statut.ACTIF.toString()
                    : a.getStatut().toString());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Insertion abonnement échouée, aucune ligne affectée.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Insertion abonnement échouée, aucun ID généré.");
            }
        }
    }

    public boolean update(Connection conn, Abonnement a) throws SQLException {
        String sql = "UPDATE abonnement "
                   + "SET adherent_id = ?, local_id = ?, date_debut = ?, date_fin = ?, statut = ? "
                   + "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getAdherentId());
            ps.setInt(2, a.getLocalId());
            ps.setDate(3, (a.getDateDebut() == null) ? null : Date.valueOf(a.getDateDebut()));
            ps.setDate(4, (a.getDateFin()   == null) ? null : Date.valueOf(a.getDateFin()));
            ps.setString(5, (a.getStatut()  == null)
                    ? Abonnement.Statut.ACTIF.toString()
                    : a.getStatut().toString());
            ps.setInt(6, a.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM abonnement WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}