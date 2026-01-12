-- Script pour ajouter des champs de profil à la table users
-- Exécuter ce script si vous voulez étendre les informations utilisateur

USE iusjcdb;

-- Ajouter les nouveaux champs à la table users
ALTER TABLE users 
ADD COLUMN first_name VARCHAR(100) NULL COMMENT 'Prénom de l\'utilisateur',
ADD COLUMN last_name VARCHAR(100) NULL COMMENT 'Nom de famille de l\'utilisateur',
ADD COLUMN phone VARCHAR(20) NULL COMMENT 'Numéro de téléphone',
ADD COLUMN address VARCHAR(200) NULL COMMENT 'Adresse complète',
ADD COLUMN department VARCHAR(100) NULL COMMENT 'Département/Faculté',
ADD COLUMN specialization VARCHAR(100) NULL COMMENT 'Spécialisation/Matière principale',
ADD COLUMN bio TEXT NULL COMMENT 'Biographie/Description',
ADD COLUMN student_id VARCHAR(50) NULL COMMENT 'Numéro étudiant (pour les étudiants)',
ADD COLUMN level ENUM('L1', 'L2', 'L3', 'M1', 'M2', 'DOCTORAT') NULL COMMENT 'Niveau d\'études (pour les étudiants)',
ADD COLUMN title VARCHAR(100) NULL COMMENT 'Titre académique (Dr, Prof, etc.)',
ADD COLUMN office VARCHAR(100) NULL COMMENT 'Bureau (pour les enseignants)',
ADD COLUMN avatar_url VARCHAR(255) NULL COMMENT 'URL de la photo de profil';

-- Ajouter des index pour améliorer les performances
CREATE INDEX idx_users_department ON users(department);
CREATE INDEX idx_users_level ON users(level);
CREATE INDEX idx_users_student_id ON users(student_id);

-- Mettre à jour quelques utilisateurs existants avec des données d'exemple
UPDATE users SET 
    first_name = 'Administrateur',
    last_name = 'Système',
    department = 'Administration',
    title = 'Administrateur'
WHERE username = 'admin';

-- Afficher la structure mise à jour
DESCRIBE users;