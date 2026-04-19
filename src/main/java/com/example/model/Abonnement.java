package main.java.com.example.model;

import java.time.LocalDate;
import java.util.Objects;

public class Abonnement {
    private int id;
    private int adherentId;
    private int localId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Statut statut;

    public enum Statut {
        ACTIF("actif"), SUSPENDU("suspendu"), EXPIRE("expire");

        private final String value;

        Statut(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * Convertit une chaîne en Statut.
         * Lève une IllegalArgumentException si la valeur est inconnue
         * (évite les erreurs silencieuses).
         */
        public static Statut fromString(String s) {
            if (s == null || s.trim().isEmpty()) {
                return ACTIF;
            }
            switch (s.trim().toLowerCase()) {
                case "actif":    return ACTIF;
                case "suspendu": return SUSPENDU;
                case "expire":   return EXPIRE;
                default:
                    throw new IllegalArgumentException("Statut inconnu : '" + s + "'");
            }
        }
    }

    public Abonnement() {
        this.statut = Statut.ACTIF;
    }

    public Abonnement(int adherentId, int localId, LocalDate dateDebut, LocalDate dateFin, Statut statut) {
        this.adherentId = adherentId;
        this.localId    = localId;
        this.dateDebut  = dateDebut;
        this.dateFin    = dateFin;
        this.statut     = (statut == null) ? Statut.ACTIF : statut;
    }

    public Abonnement(int id, int adherentId, int localId, LocalDate dateDebut, LocalDate dateFin, Statut statut) {
        this(adherentId, localId, dateDebut, dateFin, statut);
        this.id = id;
    }

    // --- Getters / Setters ---

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public int getAdherentId()            { return adherentId; }
    public void setAdherentId(int v)      { this.adherentId = v; }

    public int getLocalId()               { return localId; }
    public void setLocalId(int v)         { this.localId = v; }

    public LocalDate getDateDebut()       { return dateDebut; }
    public void setDateDebut(LocalDate v) { this.dateDebut = v; }

    public LocalDate getDateFin()         { return dateFin; }
    public void setDateFin(LocalDate v)   { this.dateFin = v; }

    public Statut getStatut()             { return statut; }
    public void setStatut(Statut v)       { this.statut = (v == null) ? Statut.ACTIF : v; }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abonnement that = (Abonnement) o;
        return id == that.id
                && adherentId == that.adherentId
                && localId == that.localId
                && Objects.equals(dateDebut, that.dateDebut)
                && Objects.equals(dateFin, that.dateFin)
                && statut == that.statut;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, adherentId, localId, dateDebut, dateFin, statut);
    }

    @Override
    public String toString() {
        return "Abonnement{"
                + "id=" + id
                + ", adherentId=" + adherentId
                + ", localId=" + localId
                + ", dateDebut=" + dateDebut
                + ", dateFin=" + dateFin
                + ", statut=" + statut
                + '}';
    }
}