-- Données de développement pour H2
-- Mot de passe: admin123 (hashé avec BCrypt)

INSERT INTO users (username, email, password, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES 
('admin', 'admin@iusjc.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, email, password, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES 
('teacher1', 'teacher1@iusjc.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, email, password, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES 
('teacher2', 'teacher2@iusjc.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);