package com.fitmanager.model;

import java.util.Objects;

/**
 * ReprÃ©sente une salle de sport (branche) dans FitManager.
 * Correspond Ã  la table `local` en base (mot rÃ©servÃ© MySQL â†’ backticks dans les requÃªtes).
 */
public class Local {

    private int    id;
    private String nom;
    private String adresse;
    private int    capaciteMax;

    // -------------------------------------------------------------------------
    // Constructeurs
    // -------------------------------------------------------------------------

    public Local() {}

    public Local(String nom, String adresse, int capaciteMax) {
        this.nom         = nom;
        this.adresse     = adresse;
        this.capaciteMax = capaciteMax;
    }

    public Local(int id, String nom, String adresse, int capaciteMax) {
        this(nom, adresse, capaciteMax);
        this.id = id;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public int    getId()                   { return id; }
    public void   setId(int id)             { this.id = id; }

    public String getNom()                  { return nom; }
    public void   setNom(String nom)        { this.nom = nom; }

    public String getAdresse()              { return adresse; }
    public void   setAdresse(String v)      { this.adresse = v; }

    public int    getCapaciteMax()          { return capaciteMax; }
    public void   setCapaciteMax(int v)     { this.capaciteMax = v; }

    // -------------------------------------------------------------------------
    // equals / hashCode / toString
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Local that = (Local) o;
        return id == that.id && Objects.equals(nom, that.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom);
    }

    @Override
    public String toString() {
        return "Local{"
                + "id=" + id
                + ", nom='" + nom + '\''
                + ", adresse='" + adresse + '\''
                + ", capaciteMax=" + capaciteMax
                + '}';
    }
}
