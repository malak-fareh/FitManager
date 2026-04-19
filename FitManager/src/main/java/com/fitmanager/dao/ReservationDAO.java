package com.fitmanager.dao;

import com.fitmanager.model.Reservation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour la table `reservation`.
 */
public class ReservationDAO {

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    public static Reservation mapRow(ResultSet rs) throws SQLException {
        int  id         = rs.getInt("id");
        int  adherentId = rs.getInt("adherent_id");
        int  localId    = rs.getInt("local_id");

        Date sqlDate    = rs.getDate("date_reservation");
        LocalDate date  = (sqlDate == null) ? null : sqlDate.toLocalDate();

        Time sqlDebut   = rs.getTime("heure_debut");
        Time sqlFin     = rs.getTime("heure_fin");
        LocalTime debut = (sqlDebut == null) ? null : sqlDebut.toLocalTime();
        LocalTime fin   = (sqlFin   == null) ? null : sqlFin.toLocalTime();

        Reservation.Statut statut;
        try {
            statut = Reservation.Statut.fromString(rs.getString("statut"));
        } catch (IllegalArgumentException e) {
            System.err.println("[ReservationDAO] Statut inconnu pour id=" + id + " : " + e.getMessage());
            statut = Reservation.Statut.CONFIRMEE;
        }

        return new Reservation(id, adherentId, localId, date, debut, fin, statut);
    }

    // -------------------------------------------------------------------------
    // Lectures
    // -------------------------------------------------------------------------

    public List<Reservation> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM reservation ORDER BY date_reservation DESC, heure_debut";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Reservation> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        }
    }

    public Optional<Reservation> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        }
    }

    public List<Reservation> findByAdherentId(Connection conn, int adherentId) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE adherent_id = ? ORDER BY date_reservation DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adherentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reservation> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    public List<Reservation> findByLocalId(Connection conn, int localId) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE local_id = ? ORDER BY date_reservation DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, localId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reservation> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    /**
     * Retourne les rÃ©servations d'une salle pour une date donnÃ©e,
     * utile pour vÃ©rifier la disponibilitÃ© d'un crÃ©neau.
     */
    public List<Reservation> findByLocalAndDate(Connection conn, int localId, LocalDate date)
            throws SQLException {
        String sql = "SELECT * FROM reservation WHERE local_id = ? AND date_reservation = ? "
                   + "AND statut = ? ORDER BY heure_debut";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, localId);
            ps.setDate  (2, Date.valueOf(date));
            ps.setString(3, Reservation.Statut.CONFIRMEE.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<Reservation> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Ã‰critures
    // -------------------------------------------------------------------------

    public int insert(Connection conn, Reservation r) throws SQLException {
        String sql = "INSERT INTO reservation "
                   + "(adherent_id, local_id, date_reservation, heure_debut, heure_fin, statut) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, r.getAdherentId());
            ps.setInt   (2, r.getLocalId());
            ps.setDate  (3, (r.getDateReservation() == null) ? null : Date.valueOf(r.getDateReservation()));
            ps.setTime  (4, (r.getHeureDebut()      == null) ? null : Time.valueOf(r.getHeureDebut()));
            ps.setTime  (5, (r.getHeureFin()        == null) ? null : Time.valueOf(r.getHeureFin()));
            ps.setString(6, (r.getStatut()          == null)
                    ? Reservation.Statut.CONFIRMEE.toString()
                    : r.getStatut().toString());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Insertion rÃ©servation Ã©chouÃ©e, aucune ligne affectÃ©e.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Insertion rÃ©servation Ã©chouÃ©e, aucun ID gÃ©nÃ©rÃ©.");
            }
        }
    }

    /**
     * Seul le statut est modifiable aprÃ¨s crÃ©ation
     * (date et crÃ©neau ne changent pas â€” on annule et recrÃ©e).
     */
    public boolean updateStatut(Connection conn, int id, Reservation.Statut statut)
            throws SQLException {
        String sql = "UPDATE reservation SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, (statut == null) ? Reservation.Statut.CONFIRMEE.toString() : statut.toString());
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
