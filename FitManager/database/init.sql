CREATE DATABASE fitmanager;
USE fitmanager;

CREATE TABLE local (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    adresse TEXT NOT NULL,
    capacite_max INT NOT NULL
);

CREATE TABLE IF NOT EXISTS utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    pseudo VARCHAR(50) UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    age INT,
    role ENUM('ADHERENT', 'ADMIN', 'GERANT') NOT NULL DEFAULT 'ADHERENT',
    local_principal_id INT,
    FOREIGN KEY (local_principal_id) REFERENCES local(id)
);

CREATE TABLE abonnement (
    id INT PRIMARY KEY AUTO_INCREMENT,
    adherent_id INT NOT NULL,
    local_id INT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    statut ENUM('actif', 'suspendu', 'expire') DEFAULT 'actif',
    FOREIGN KEY (adherent_id) REFERENCES utilisateur(id),
    FOREIGN KEY (local_id) REFERENCES local(id)
);

CREATE TABLE reservation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    adherent_id INT NOT NULL,
    local_id INT NOT NULL,
    date_reservation DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    statut ENUM('confirmee', 'annulee') DEFAULT 'confirmee',
    FOREIGN KEY (adherent_id) REFERENCES utilisateur(id),
    FOREIGN KEY (local_id) REFERENCES local(id)
);

CREATE TABLE demande_transfert (
    id INT PRIMARY KEY AUTO_INCREMENT,
    adherent_id INT NOT NULL,
    local_source_id INT NOT NULL,
    local_destination_id INT NOT NULL,
    date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('en_attente', 'validee', 'refusee') DEFAULT 'en_attente',
    FOREIGN KEY (adherent_id) REFERENCES utilisateur(id),
    FOREIGN KEY (local_source_id) REFERENCES local(id),
    FOREIGN KEY (local_destination_id) REFERENCES local(id)
);


INSERT INTO local (nom, adresse, capacite_max) VALUES
('Salle Centre-Ville', '15 rue Habib Bourguiba, Tunis', 100),
('Salle Lac', 'Immeuble Les Berges du Lac, Tunis', 150),
('Salle Marsa', '5 avenue Taieb Mhiri, La Marsa', 80);

INSERT INTO utilisateur (nom, email, mot_de_passe, role, local_gere_id) VALUES
('Super Admin', 'admin@fitmanager.com', 'admin123', 'admin', NULL),
('Gérant Centre-Ville', 'gerant.centre@fitmanager.com', '123', 'gerant', 1),
('Gérant Lac', 'gerant.lac@fitmanager.com', '123', 'gerant', 2),
('Gérant Marsa', 'gerant.marsa@fitmanager.com', '123', 'gerant', 3),
('Mariem', 'mariem@test.com', '123', 'adherent', NULL),
('Ahmed', 'ahmed@test.com', '123', 'adherent', NULL);

INSERT INTO abonnement (adherent_id, local_id, date_debut, date_fin, statut) VALUES
(4, 1, '2026-01-01', '2026-12-31', 'actif'),
(5, 2, '2026-01-15', '2026-12-31', 'actif');
