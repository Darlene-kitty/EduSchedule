-- Course Service Tables
USE iusjcdb;

-- Table des cours
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    credits INT NOT NULL,
    duration INT NOT NULL,
    department VARCHAR(100) NOT NULL,
    level VARCHAR(50) NOT NULL,
    semester VARCHAR(50) NOT NULL,
    teacher_id BIGINT,
    max_students INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des groupes de cours
CREATE TABLE IF NOT EXISTS course_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    group_name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    max_students INT,
    current_students INT NOT NULL DEFAULT 0,
    teacher_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- DonnÃ©es d'exemple
INSERT IGNORE INTO courses (name, code, description, credits, duration, department, level, semester, teacher_id, max_students) VALUES
('Programmation Java', 'INF101', 'Introduction Ã  la programmation orientÃ©e objet avec Java', 6, 120, 'Informatique', 'L1', 'S1', 1, 50),
('Base de DonnÃ©es', 'INF201', 'Conception et gestion de bases de donnÃ©es relationnelles', 6, 90, 'Informatique', 'L2', 'S1', 1, 40),
('Algorithmes AvancÃ©s', 'INF301', 'Structures de donnÃ©es et algorithmes complexes', 8, 120, 'Informatique', 'L3', 'S1', 1, 30),
('MathÃ©matiques DiscrÃ¨tes', 'MAT101', 'Logique, ensembles, relations et fonctions', 6, 90, 'MathÃ©matiques', 'L1', 'S1', 2, 60),
('Analyse NumÃ©rique', 'MAT201', 'MÃ©thodes numÃ©riques et calcul scientifique', 6, 90, 'MathÃ©matiques', 'L2', 'S2', 2, 35);

SELECT 'Tables crÃ©Ã©es et donnÃ©es insÃ©rÃ©es avec succÃ¨s!' as status;
