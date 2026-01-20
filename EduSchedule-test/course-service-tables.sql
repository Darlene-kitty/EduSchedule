-- Script de création des tables pour Course Service
-- À exécuter dans la base de données iusjcdb

USE iusjcdb;

-- Table des cours
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    credits INT NOT NULL,
    duration INT NOT NULL COMMENT 'Durée en minutes',
    department VARCHAR(100) NOT NULL,
    level VARCHAR(50) NOT NULL COMMENT 'L1, L2, L3, M1, M2, DOCTORAT',
    semester VARCHAR(50) NOT NULL COMMENT 'S1, S2',
    teacher_id BIGINT,
    max_students INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_courses_code (code),
    INDEX idx_courses_department (department),
    INDEX idx_courses_level (level),
    INDEX idx_courses_semester (semester),
    INDEX idx_courses_teacher (teacher_id),
    INDEX idx_courses_active (active)
);

-- Table des groupes de cours
CREATE TABLE IF NOT EXISTS course_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    group_name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'COURS, TD, TP, EXAMEN',
    max_students INT,
    current_students INT NOT NULL DEFAULT 0,
    teacher_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_course_groups_course (course_id),
    INDEX idx_course_groups_teacher (teacher_id),
    INDEX idx_course_groups_type (type),
    INDEX idx_course_groups_active (active),
    UNIQUE KEY uk_course_group_name (course_id, group_name, active)
);

-- Données d'exemple
INSERT INTO courses (name, code, description, credits, duration, department, level, semester, teacher_id, max_students) VALUES
('Programmation Java', 'INF101', 'Introduction à la programmation orientée objet avec Java', 6, 120, 'Informatique', 'L1', 'S1', 1, 50),
('Base de Données', 'INF201', 'Conception et gestion de bases de données relationnelles', 6, 90, 'Informatique', 'L2', 'S1', 1, 40),
('Algorithmes Avancés', 'INF301', 'Structures de données et algorithmes complexes', 8, 120, 'Informatique', 'L3', 'S1', 1, 30),
('Mathématiques Discrètes', 'MAT101', 'Logique, ensembles, relations et fonctions', 6, 90, 'Mathématiques', 'L1', 'S1', 2, 60),
('Analyse Numérique', 'MAT201', 'Méthodes numériques et calcul scientifique', 6, 90, 'Mathématiques', 'L2', 'S2', 2, 35);

-- Groupes pour les cours
INSERT INTO course_groups (course_id, group_name, type, max_students, teacher_id) VALUES
-- Programmation Java (INF101)
(1, 'Groupe A', 'COURS', 25, 1),
(1, 'Groupe B', 'COURS', 25, 1),
(1, 'TD1', 'TD', 15, 1),
(1, 'TD2', 'TD', 15, 1),
(1, 'TP1', 'TP', 12, 1),
(1, 'TP2', 'TP', 12, 1),

-- Base de Données (INF201)
(2, 'Groupe Unique', 'COURS', 40, 1),
(2, 'TD1', 'TD', 20, 1),
(2, 'TD2', 'TD', 20, 1),
(2, 'TP1', 'TP', 15, 1),

-- Algorithmes Avancés (INF301)
(3, 'Groupe Master', 'COURS', 30, 1),
(3, 'TD Avancé', 'TD', 15, 1),
(3, 'TP Recherche', 'TP', 10, 1),

-- Mathématiques Discrètes (MAT101)
(4, 'Groupe A', 'COURS', 30, 2),
(4, 'Groupe B', 'COURS', 30, 2),
(4, 'TD1', 'TD', 20, 2),
(4, 'TD2', 'TD', 20, 2),

-- Analyse Numérique (MAT201)
(5, 'Groupe Unique', 'COURS', 35, 2),
(5, 'TD Numérique', 'TD', 18, 2),
(5, 'TP Calcul', 'TP', 12, 2);

-- Afficher les résultats
SELECT 'Courses créés:' as info;
SELECT id, name, code, department, level FROM courses;

SELECT 'Groupes créés:' as info;
SELECT cg.id, c.code, cg.group_name, cg.type, cg.max_students 
FROM course_groups cg 
JOIN courses c ON cg.course_id = c.id 
ORDER BY c.code, cg.type, cg.group_name;