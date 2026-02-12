-- Tables pour le scheduling-service
-- Base de données : iusjcdb

USE iusjcdb;

-- Table schedules
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
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_start_time (start_time),
    INDEX idx_teacher (teacher),
    INDEX idx_room (room),
    INDEX idx_status (status)
);

-- Table time_slots
CREATE TABLE IF NOT EXISTS time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    schedule_id BIGINT,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    INDEX idx_day_of_week (day_of_week),
    INDEX idx_schedule_id (schedule_id)
);

-- Vérification des tables créées
SHOW TABLES LIKE '%schedule%';
SHOW TABLES LIKE '%time_slot%';

-- Afficher la structure des tables
DESCRIBE schedules;
DESCRIBE time_slots;