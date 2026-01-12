# Script de configuration et démarrage du Course Service
# Crée les tables et démarre le service

Write-Host "=== Configuration du Course Service ===" -ForegroundColor Cyan

# Configuration de la base de données
$MYSQL_HOST = "localhost"
$MYSQL_PORT = "3306"
$MYSQL_DATABASE = "iusjcdb"
$MYSQL_USER = "iusjc"
$MYSQL_PASSWORD = "iusjc2025"

# Test de connexion MySQL
Write-Host "`n1. Test de connexion à MySQL..." -ForegroundColor Yellow

try {
    # Tester la connexion avec une requête simple
    $testQuery = "SELECT 1 as test"
    $result = mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD -D $MYSQL_DATABASE -e $testQuery 2>$null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Connexion MySQL réussie" -ForegroundColor Green
    } else {
        throw "Connexion échouée"
    }
}
catch {
    Write-Host "❌ Impossible de se connecter à MySQL" -ForegroundColor Red
    Write-Host "Vérifiez que MySQL est démarré et que les identifiants sont corrects" -ForegroundColor Yellow
    Write-Host "Host: $MYSQL_HOST, Port: $MYSQL_PORT, Database: $MYSQL_DATABASE, User: $MYSQL_USER" -ForegroundColor Yellow
    exit 1
}

# Création des tables
Write-Host "`n2. Création des tables Course Service..." -ForegroundColor Yellow

$sqlCommands = @"
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
"@

try {
    # Écrire les commandes SQL dans un fichier temporaire
    $tempSqlFile = "temp_course_tables.sql"
    $sqlCommands | Out-File -FilePath $tempSqlFile -Encoding UTF8
    
    # Exécuter le fichier SQL avec une méthode compatible PowerShell
    $mysqlCmd = "mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD"
    Get-Content $tempSqlFile | & cmd /c "$mysqlCmd"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Tables créées avec succès" -ForegroundColor Green
    } else {
        throw "Erreur lors de la création des tables"
    }
    
    # Nettoyer le fichier temporaire
    Remove-Item $tempSqlFile -ErrorAction SilentlyContinue
}
catch {
    Write-Host "❌ Erreur lors de la création des tables: $($_.Exception.Message)" -ForegroundColor Red
    Remove-Item $tempSqlFile -ErrorAction SilentlyContinue
    exit 1
}

# Insertion des données d'exemple
Write-Host "`n3. Insertion des données d'exemple..." -ForegroundColor Yellow

$sampleData = @"
USE iusjcdb;

-- Supprimer les données existantes pour éviter les doublons
DELETE FROM course_groups WHERE course_id IN (SELECT id FROM courses WHERE code IN ('INF101', 'INF201', 'INF301', 'MAT101', 'MAT201'));
DELETE FROM courses WHERE code IN ('INF101', 'INF201', 'INF301', 'MAT101', 'MAT201');

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
(2, 'TP1', 'TP', 15, 1);
"@

try {
    $tempDataFile = "temp_course_data.sql"
    $sampleData | Out-File -FilePath $tempDataFile -Encoding UTF8
    
    # Exécuter le fichier SQL avec une méthode compatible PowerShell
    $mysqlCmd = "mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD"
    Get-Content $tempDataFile | & cmd /c "$mysqlCmd"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Données d'exemple insérées" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Erreur lors de l'insertion des données (peut-être déjà existantes)" -ForegroundColor Yellow
    }
    
    Remove-Item $tempDataFile -ErrorAction SilentlyContinue
}
catch {
    Write-Host "⚠️ Erreur lors de l'insertion des données: $($_.Exception.Message)" -ForegroundColor Yellow
    Remove-Item $tempDataFile -ErrorAction SilentlyContinue
}

# Vérification des données
Write-Host "`n4. Vérification des données..." -ForegroundColor Yellow

try {
    $courseCount = mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD -D $MYSQL_DATABASE -e "SELECT COUNT(*) FROM courses;" -s -N
    $groupCount = mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD -D $MYSQL_DATABASE -e "SELECT COUNT(*) FROM course_groups;" -s -N
    
    Write-Host "✅ Cours dans la base: $courseCount" -ForegroundColor Green
    Write-Host "✅ Groupes dans la base: $groupCount" -ForegroundColor Green
}
catch {
    Write-Host "⚠️ Impossible de vérifier les données" -ForegroundColor Yellow
}

# Compilation du service
Write-Host "`n5. Compilation du Course Service..." -ForegroundColor Yellow

try {
    Set-Location course-service
    
    Write-Host "Compilation en cours..." -ForegroundColor Cyan
    mvn clean compile -q
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Compilation réussie" -ForegroundColor Green
    } else {
        throw "Erreur de compilation"
    }
}
catch {
    Write-Host "❌ Erreur lors de la compilation: $($_.Exception.Message)" -ForegroundColor Red
    Set-Location ..
    exit 1
}

# Démarrage du service
Write-Host "`n6. Démarrage du Course Service..." -ForegroundColor Yellow
Write-Host "Le service va démarrer sur le port 8084..." -ForegroundColor Cyan
Write-Host "Appuyez sur Ctrl+C pour arrêter le service" -ForegroundColor Yellow

try {
    mvn spring-boot:run
}
catch {
    Write-Host "❌ Erreur lors du démarrage: $($_.Exception.Message)" -ForegroundColor Red
}
finally {
    Set-Location ..
}

Write-Host "`n=== Course Service configuré ===" -ForegroundColor Cyan