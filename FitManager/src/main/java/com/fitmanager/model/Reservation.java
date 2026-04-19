package com.fitmanager.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * ReprÃ©sente une rÃ©servation de crÃ©neau par un adhÃ©rent dans une salle.
 * Correspond Ã  la table `reservation` en base.
 */
public class Reservation {

    private int       id;
    private int       adherentId;
    private int       localId;
    private LocalDate dateReservation;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Statut    statut;

    // -------------------------------------------------------------------------
    // Enum Statut
    // -------------------------------------------------------------------------

    public enum Statut {
        CONFIRMEE("confirmee"), ANNULEE("annulee");

        private final String value;

        Statut(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * Convertit une chaÃ®ne en Statut.
         * LÃ¨ve une IllegalArgumentException si la valeur est inconnue.
         */
        public static Statut fromString(String s) {
            if (s == null || s.trim().isEmpty()) {
                return CONFIRMEE;
            }
            switch (s.trim().toLowerCase()) {
                case "confirmee": return CONFIRMEE;
                case "annulee":   return ANNULEE;
                default:
                    throw new IllegalArgumentException("Statut rÃ©servation inconnu : '" + s + "'");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Constructeurs
    // -------------------------------------------------------------------------

    public Reservation() {
        this.statut = Statut.CONFIRMEE;
    }

    public Reservation(int adherentId, int localId,
                       LocalDate dateReservation,
                       LocalTime heureDebut, LocalTime heureFin,
                       Statut statut) {
        this.adherentId      = adherentId;
        this.localId         = localId;
        this.dateReservation = dateReservation;
        this.heureDebut      = heureDebut;
        this.heureFin        = heureFin;
        this.statut          = (statut == null) ? Statut.CONFIRMEE : statut;
    }

    public Reservation(int id, int adherentId, int localId,
                       LocalDate dateReservation,
                       LocalTime heureDebut, LocalTime heureFin,
                       Statut statut) {
        this(adherentId, localId, dateReservation, heureDebut, heureFin, statut);
        this.id = id;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public int       getId()                       { return id; }
    public void      setId(int id)                 { this.id = id; }

    public int       getAdherentId()               { return adherentId; }
    public void      setAdherentId(int v)          { this.adherentId = v; }

    public int       getLocalId()                  { return localId; }
    public void      setLocalId(int v)             { this.localId = v; }

    public LocalDate getDateReservation()          { return dateReservation; }
    public void      setDateReservation(LocalDate v){ this.dateReservation = v; }

    public LocalTime getHeureDebut()               { return heureDebut; }
    public void      setHeureDebut(LocalTime v)    { this.heureDebut = v; }

    public LocalTime getHeureFin()                 { return heureFin; }
    public void      setHeureFin(LocalTime v)      { this.heureFin = v; }

    public Statut    getStatut()                   { return statut; }
    public void      setStatut(Statut v)           { this.statut = (v == null) ? Statut.CONFIRMEE : v; }

    // -------------------------------------------------------------------------
    // equals / hashCode / toString
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id
                && adherentId == that.adherentId
                && localId    == that.localId
                && Objects.equals(dateReservation, that.dateReservation)
                && Objects.equals(heureDebut,      that.heureDebut)
                && Objects.equals(heureFin,        that.heureFin)
                && statut == that.statut;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, adherentId, localId, dateReservation, heureDebut, heureFin, statut);
    }

    @Override
    public String toString() {
        return "Reservation{"
                + "id=" + id
                + ", adherentId=" + adherentId
                + ", localId=" + localId
                + ", dateReservation=" + dateReservation
                + ", heureDebut=" + heureDebut
                + ", heureFin=" + heureFin
                + ", statut=" + statut
                + '}';
    }
}
