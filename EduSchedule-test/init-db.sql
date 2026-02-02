-- Base de données unique pour tous les services
-- Créée automatiquement par MYSQL_DATABASE dans docker-compose.yml

-- Tables pour user-service (authentification)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Tables pour scheduling-service (emplois du temps)
CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    room VARCHAR(100),
    teacher VARCHAR(100),
    course VARCHAR(100),
    group_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    schedule_id BIGINT,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
);

-- Tables pour notification-service
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    sent_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scheduled_for TIMESTAMP NULL,
    event_type VARCHAR(50) NULL,
    event_id BIGINT NULL,
    priority VARCHAR(10) DEFAULT 'NORMAL',
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    template_name VARCHAR(100) NULL,
    metadata TEXT NULL
);

-- Table pour refresh tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index pour optimisation
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_enabled ON users(enabled);
CREATE INDEX IF NOT EXISTS idx_schedules_dates ON schedules(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient);
CREATE INDEX IF NOT EXISTS idx_notifications_event_id ON notifications(event_id);
CREATE INDEX IF NOT EXISTS idx_notifications_event_type ON notifications(event_type);
CREATE INDEX IF NOT EXISTS idx_notifications_scheduled_for ON notifications(scheduled_for);
CREATE INDEX IF NOT EXISTS idx_notifications_priority ON notifications(priority);

-- Tables pour school-service (écoles et filières)
CREATE TABLE IF NOT EXISTS schools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS filieres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL,
    school_id BIGINT NOT NULL,
    level VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS groupes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    filiere_id BIGINT NOT NULL,
    capacity INT DEFAULT 30,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (filiere_id) REFERENCES filieres(id) ON DELETE CASCADE
);

-- Tables pour room-service (salles et équipements)
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    building VARCHAR(100),
    floor INT,
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    quantity INT DEFAULT 1,
    room_id BIGINT,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- Tables pour course-service (cours)
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL,
    hours_per_week INT DEFAULT 2,
    filiere_id BIGINT NOT NULL,
    teacher_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Amélioration de la table schedules
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS course_id BIGINT;
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS room_id BIGINT;
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS group_id BIGINT;
ALTER TABLE schedules ADD COLUMN IF NOT EXISTS school_id BIGINT;

-- Index pour optimisation
CREATE INDEX idx_filieres_school ON filieres(school_id);
CREATE INDEX idx_groupes_filiere ON groupes(filiere_id);
CREATE INDEX idx_rooms_type ON rooms(type);
CREATE INDEX idx_equipments_room ON equipments(room_id);
CREATE INDEX idx_courses_filiere ON courses(filiere_id);
CREATE INDEX idx_schedules_course ON schedules(course_id);
CREATE INDEX idx_schedules_room ON schedules(room_id);

-- Table pour refresh tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table pour reset password tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table pour email verification tokens
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Ajouter colonnes pour les nouvelles fonctionnalités
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS remember_me_token VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP NULL;

-- Index pour les tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_token ON email_verification_tokens(token);

-- Données de test
INSERT INTO roles (name) VALUES ('ADMIN'), ('TEACHER') ON DUPLICATE KEY UPDATE name=name;

-- Utilisateurs de test (password = "admin123" hashé en BCrypt)
INSERT INTO users (username, email, password, role, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES 
('admin', 'admin@iusjc.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true, true, true, true),
('prof.dupont', 'dupont@iusjc.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', true, true, true, true),
('prof.martin', 'martin@iusjc.cm', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', true, true, true, true)
ON DUPLICATE KEY UPDATE username=username;

-- Écoles de l'IUSJC
INSERT INTO schools (name, code, description) VALUES 
('Saint Jean Institute', 'SJI', 'École d\'ingénierie'),
('Saint Jean Medical', 'SJM', 'École de médecine'),
('PrepaVogt', 'PREPAV', 'Classes préparatoires'),
('CPGE', 'CPGE', 'Classes préparatoires aux grandes écoles')
ON DUPLICATE KEY UPDATE name=name;

-- Filières exemple pour SJI
INSERT INTO filieres (name, code, school_id, level) VALUES 
('Informatique', 'INFO', 1, 'Licence'),
('Génie Civil', 'GC', 1, 'Licence'),
('Électronique', 'ELEC', 1, 'Master')
ON DUPLICATE KEY UPDATE name=name;

-- Groupes exemple
INSERT INTO groupes (name, filiere_id, capacity) VALUES 
('ISI 4A', 1, 35),
('ISI 4B', 1, 30),
('GC 3A', 2, 40)
ON DUPLICATE KEY UPDATE name=name;

-- Salles exemple
INSERT INTO rooms (name, code, type, capacity, building, floor) VALUES 
('Amphi A', 'AMPH-A', 'AMPHITHEATRE', 200, 'Bâtiment Principal', 1),
('Salle A101', 'A101', 'CLASSROOM', 40, 'Bâtiment A', 1),
('Labo Info 1', 'LAB-I1', 'LABORATORY', 30, 'Bâtiment B', 2),
('Salle TD 201', 'TD201', 'CLASSROOM', 35, 'Bâtiment C', 2)
ON DUPLICATE KEY UPDATE name=name;

-- Équipements exemple
INSERT INTO equipments (name, type, quantity, room_id, status) VALUES 
('Projecteur', 'PROJECTOR', 1, 1, 'AVAILABLE'),
('Ordinateurs', 'COMPUTER', 30, 3, 'AVAILABLE'),
('Tableau Blanc', 'WHITEBOARD', 1, 2, 'AVAILABLE'),
('Baffles', 'SPEAKER', 2, 1, 'AVAILABLE')
ON DUPLICATE KEY UPDATE name=name;
