# Démarrage du Course Service uniquement
# Suppose que MySQL et les tables sont déjà configurés

Write-Host "=== Démarrage du Course Service ===" -ForegroundColor Cyan

# Vérifier les prérequis
Write-Host "`nVérification des prérequis..." -ForegroundColor Yellow

# 1. Vérifier que le répertoire existe
if (-not (Test-Path "course-service")) {
    Write-Host "❌ Répertoire course-service non trouvé" -ForegroundColor Red
    Write-Host "Assurez-vous d'être dans le répertoire racine du projet EduSchedule" -ForegroundColor Yellow
    exit 1
}

# 2. Vérifier que Maven est installé
try {
    $mvnVersion = mvn -version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Maven détecté" -ForegroundColor Green
    } else {
        throw "Maven non trouvé"
    }
}
catch {
    Write-Host "❌ Maven non installé ou non dans le PATH" -ForegroundColor Red
    Write-Host "Installez Maven ou ajoutez-le au PATH système" -ForegroundColor Yellow
    exit 1
}

# 3. Vérifier Java
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Java détecté" -ForegroundColor Green
    } else {
        throw "Java non trouvé"
    }
}
catch {
    Write-Host "❌ Java non installé ou non dans le PATH" -ForegroundColor Red
    Write-Host "Installez Java 17+ ou ajoutez-le au PATH système" -ForegroundColor Yellow
    exit 1
}

# 4. Aller dans le répertoire du service
Write-Host "`nNavigation vers course-service..." -ForegroundColor Yellow
Set-Location course-service

# 5. Compilation rapide
Write-Host "`nCompilation du service..." -ForegroundColor Yellow
Write-Host "Cela peut prendre quelques minutes la première fois..." -ForegroundColor Cyan

try {
    mvn clean compile -q
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Compilation réussie" -ForegroundColor Green
    } else {
        throw "Erreur de compilation"
    }
}
catch {
    Write-Host "❌ Erreur lors de la compilation" -ForegroundColor Red
    Write-Host "Vérifiez les logs Maven ci-dessus pour plus de détails" -ForegroundColor Yellow
    Set-Location ..
    exit 1
}

# 6. Démarrage du service
Write-Host "`n🚀 Démarrage du Course Service..." -ForegroundColor Green
Write-Host "Port: 8084" -ForegroundColor Cyan
Write-Host "Santé: http://localhost:8084/actuator/health" -ForegroundColor Cyan
Write-Host "API: http://localhost:8084/api/v1/courses" -ForegroundColor Cyan
Write-Host "`nAppuyez sur Ctrl+C pour arrêter le service" -ForegroundColor Yellow
Write-Host "=" * 50 -ForegroundColor Gray

try {
    mvn spring-boot:run
}
catch {
    Write-Host "`n❌ Erreur lors du démarrage du service" -ForegroundColor Red
}
finally {
    Write-Host "`n🛑 Service arrêté" -ForegroundColor Yellow
    Set-Location ..
}

Write-Host "`nRetour au répertoire principal" -ForegroundColor Gray