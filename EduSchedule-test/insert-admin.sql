-- Insérer un utilisateur admin directement
-- Mot de passe: admin123 (hashé avec BCrypt)

INSERT INTO users (username, email, password, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) 
VALUES (
    'admin', 
    'admin@iusjc.cm', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
    'ADMIN', 
    true,
    true,
    true,
    true,
    NOW(), 
    NOW()
) ON DUPLICATE KEY UPDATE 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    role = 'ADMIN',
    enabled = true,
    account_non_expired = true,
    account_non_locked = true,
    credentials_non_expired = true;

-- Insérer quelques utilisateurs de test supplémentaires
INSERT INTO users (username, email, password, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) 
VALUES 
    ('teacher1', 'teacher1@iusjc.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', true, true, true, true, NOW(), NOW()),
    ('teacher2', 'teacher2@iusjc.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', true, true, true, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    enabled = true,
    account_non_expired = true,
    account_non_locked = true,
    credentials_non_expired = true;

-- Vérifier les utilisateurs créés
SELECT id, username, email, role, enabled, created_at FROM users;