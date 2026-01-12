-- Script pour mettre à jour la table users existante
-- Ajouter les colonnes manquantes pour Spring Security

-- Ajouter les nouvelles colonnes si elles n'existent pas
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS account_non_expired BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS account_non_locked BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS credentials_non_expired BOOLEAN DEFAULT TRUE;

-- Mettre à jour les valeurs par défaut pour les utilisateurs existants
UPDATE users 
SET 
    account_non_expired = TRUE,
    account_non_locked = TRUE,
    credentials_non_expired = TRUE
WHERE 
    account_non_expired IS NULL 
    OR account_non_locked IS NULL 
    OR credentials_non_expired IS NULL;

-- Vérifier la structure de la table
DESCRIBE users;

-- Afficher les utilisateurs existants
SELECT id, username, email, role, enabled, account_non_expired, account_non_locked, credentials_non_expired 
FROM users;