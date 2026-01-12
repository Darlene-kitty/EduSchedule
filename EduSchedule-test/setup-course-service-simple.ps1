# Configuration simple du Course Service
# Utilise les scripts existants du projet

Write-Host "=== Configuration Simple du Course Service ===" -ForegroundColor Cyan

# 1. Démarrer MySQL si nécessaire
Write-Host "`n1. Vérification de MySQL..." -ForegroundColor Yellow

if (Test-Path "setup-existing-mysql.bat") {
    Write-Host "Démarrage de MySQL avec le script existant..." -ForegroundColor Cyan
    & .\setup-existing-mysql.bat
    Start-Sleep -Seconds 5
} else {
    Write-Host "Script MySQL non trouvé. Assurez-vous que MySQL est démarré." -ForegroundColor Yellow
}

# 2. Créer les tables avec un script SQL simple
Write-Host "`n2. Création des tables Course Service..." -ForegroundColor Yellow

$sqlScript = @"
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

-- Données d'exemple
INSERT IGNORE INTO courses (name, code, description, credits, duration, department, level, semester, teacher_id, max_students) VALUES
('Programmation Java', 'INF101', 'Introduction à la programmation orientée objet avec Java', 6, 120, 'Informatique', 'L1', 'S1', 1, 50),
('Base de Données', 'INF201', 'Conception et gestion de bases de données relationnelles', 6, 90, 'Informatique', 'L2', 'S1', 1, 40),
('Algorithmes Avancés', 'INF301', 'Structures de données et algorithmes complexes', 8, 120, 'Informatique', 'L3', 'S1', 1, 30),
('Mathématiques Discrètes', 'MAT101', 'Logique, ensembles, relations et fonctions', 6, 90, 'Mathématiques', 'L1', 'S1', 2, 60),
('Analyse Numérique', 'MAT201', 'Méthodes numériques et calcul scientifique', 6, 90, 'Mathématiques', 'L2', 'S2', 2, 35);

SELECT 'Tables créées et données insérées avec succès!' as status;
"@

# Écrire le script SQL
$sqlFile = "course-service-setup.sql"
$sqlScript | Out-File -FilePath $sqlFile -Encoding UTF8

Write-Host "Script SQL créé: $sqlFile" -ForegroundColor Green

# 3. Instructions pour l'utilisateur
Write-Host "`n3. Instructions de configuration manuelle:" -ForegroundColor Yellow
Write-Host "Exécutez les commandes suivantes dans MySQL Workbench ou en ligne de commande:" -ForegroundColor Cyan

Write-Host "`nOption A - MySQL Workbench:" -ForegroundColor Green
Write-Host "1. Ouvrez MySQL Workbench"
Write-Host "2. Connectez-vous à votre serveur MySQL"
Write-Host "3. Ouvrez le fichier: $sqlFile"
Write-Host "4. Exécutez le script (Ctrl+Shift+Enter)"

Write-Host "`nOption B - Ligne de commande (si mysql est dans le PATH):" -ForegroundColor Green
Write-Host "mysql -u iusjc -p iusjcdb -e `"source $sqlFile`""

Write-Host "`nOption C - Copier-coller:" -ForegroundColor Green
Write-Host "Copiez le contenu du fichier $sqlFile et collez-le dans votre client MySQL"

# 4. Attendre confirmation
Write-Host "`n4. Attente de confirmation..." -ForegroundColor Yellow
Write-Host "Appuyez sur Entrée après avoir exécuté le script SQL..." -ForegroundColor Cyan
Read-Host

# 5. Démarrer le service
Write-Host "`n5. Démarrage du Course Service..." -ForegroundColor Yellow

if (Test-Path "course-service") {
    Set-Location course-service
    
    Write-Host "Compilation du service..." -ForegroundColor Cyan
    mvn clean compile -q
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Compilation réussie" -ForegroundColor Green
        Write-Host "`nDémarrage du service sur le port 8084..." -ForegroundColor Cyan
        Write-Host "Appuyez sur Ctrl+C pour arrêter le service" -ForegroundColor Yellow
        
        mvn spring-boot:run
    } else {
        Write-Host "❌ Erreur de compilation" -ForegroundColor Red
    }
    
    Set-Location ..
} else {
    Write-Host "❌ Répertoire course-service non trouvé" -ForegroundColor Red
}

Write-Host "`n=== Configuration terminée ===" -ForegroundColor Cyan