package com.fitmanager.model;

import java.io.Serializable;
import java.util.Objects;

public class Utilisateur implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String email;
    /** Nom dâ€™utilisateur unique (connexion possible avec e-mail ou pseudo). */
    private String pseudo;
    private String motDePasse;
    private String nom;
    private String prenom;
    private Integer age;
    private Role role;

    public enum Role {
        ADHERENT, ADMIN, GERANT
    }

    public Utilisateur() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    /** Copie sans le hash du mot de passe (pour la session HTTP). */
    public Utilisateur sansMotDePasse() {
        Utilisateur o = new Utilisateur();
        o.setId(id);
        o.setEmail(email);
        o.setPseudo(pseudo);
        o.setNom(nom);
        o.setPrenom(prenom);
        o.setAge(age);
        o.setRole(role);
        return o;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utilisateur that = (Utilisateur) o;
        return Objects.equals(id, that.id) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
