-- Mise à jour de la table users pour le système d'email de bienvenue
-- Ajout des colonnes firstName et lastName

USE iusjcdb;

-- Ajouter les colonnes firstName et lastName si elles n'existent pas
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS first_name VARCHAR(50) NULL AFTER username,
ADD COLUMN IF NOT EXISTS last_name VARCHAR(50) NULL AFTER first_name;

-- Mettre à jour les utilisateurs existants avec des valeurs par défaut
UPDATE users 
SET 
    first_name = CASE 
        WHEN first_name IS NULL OR first_name = '' THEN 
            CASE 
                WHEN role = 'ADMIN' THEN 'Administrateur'
                WHEN role = 'TEACHER' THEN 'Enseignant'
                ELSE 'Utilisateur'
            END
        ELSE first_name
    END,
    last_name = CASE 
        WHEN last_name IS NULL OR last_name = '' THEN username
        ELSE last_name
    END
WHERE first_name IS NULL OR last_name IS NULL;

-- Vérifier les modifications
SELECT 
    id,
    username,
    first_name,
    last_name,
    email,
    role,
    enabled,
    created_at
FROM users
ORDER BY created_at DESC;

-- Afficher un résumé
SELECT 
    role,
    COUNT(*) as count,
    COUNT(CASE WHEN first_name IS NOT NULL AND first_name != '' THEN 1 END) as with_first_name,
    COUNT(CASE WHEN last_name IS NOT NULL AND last_name != '' THEN 1 END) as with_last_name
FROM users
GROUP BY role;

COMMIT;