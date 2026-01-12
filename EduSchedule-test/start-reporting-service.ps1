#!/usr/bin/env pwsh

Write-Host "=== Démarrage du Reporting Service ===" -ForegroundColor Green

# Vérifier que Java est installé
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java détecté: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java non trouvé. Veuillez installer Java 17 ou plus récent." -ForegroundColor Red
    exit 1
}

# Vérifier que Maven est installé
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "✅ Maven détecté: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven non trouvé. Veuillez installer Maven." -ForegroundColor Red
    exit 1
}

# Créer le répertoire de rapports
$reportsDir = "./reports"
if (!(Test-Path $reportsDir)) {
    New-Item -ItemType Directory -Path $reportsDir -Force
    Write-Host "✅ Répertoire de rapports créé: $reportsDir" -ForegroundColor Green
}

# Vérifier que MySQL est accessible
Write-Host "`nVérification de la base de données..." -ForegroundColor Cyan
try {
    $testConnection = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue
    if ($testConnection.TcpTestSucceeded) {
        Write-Host "✅ MySQL accessible sur le port 3306" -ForegroundColor Green
    } else {
        Write-Host "⚠️ MySQL non accessible. Le service démarrera mais pourrait avoir des erreurs." -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️ Impossible de vérifier MySQL. Le service démarrera quand même." -ForegroundColor Yellow
}

# Vérifier qu'Eureka est accessible
Write-Host "Vérification d'Eureka Server..." -ForegroundColor Cyan
try {
    $eurekaResponse = Invoke-WebRequest -Uri "http://localhost:8761" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✅ Eureka Server accessible" -ForegroundColor Green
} catch {
    Write-Host "⚠️ Eureka Server non accessible. Le service démarrera en mode standalone." -ForegroundColor Yellow
}

Write-Host "`n=== Compilation et démarrage ===" -ForegroundColor Cyan

# Aller dans le répertoire du service
Set-Location reporting-service

Write-Host "Compilation du projet..." -ForegroundColor Yellow
try {
    $compileResult = mvn clean compile -q
    Write-Host "✅ Compilation réussie" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur de compilation" -ForegroundColor Red
    Set-Location ..
    exit 1
}

Write-Host "`nDémarrage du Reporting Service..." -ForegroundColor Yellow
Write-Host "Port: 8088" -ForegroundColor Cyan
Write-Host "Endpoints disponibles:" -ForegroundColor Cyan
Write-Host "  - GET  /api/v1/reports/test" -ForegroundColor Gray
Write-Host "  - GET  /api/v1/reports/statistics" -ForegroundColor Gray
Write-Host "  - POST /api/v1/reports/generate" -ForegroundColor Gray
Write-Host "  - POST /api/v1/reports/generate-async" -ForegroundColor Gray
Write-Host "  - GET  /api/v1/reports/{id}" -ForegroundColor Gray
Write-Host "  - GET  /api/v1/reports/{id}/download" -ForegroundColor Gray
Write-Host "  - GET  /api/v1/reports/user/{userId}" -ForegroundColor Gray
Write-Host "`nPour tester le service, utilisez: .\test-reporting-service.ps1" -ForegroundColor Green
Write-Host "Pour arrêter le service, appuyez sur Ctrl+C" -ForegroundColor Yellow

# Démarrer le service
try {
    mvn spring-boot:run
} catch {
    Write-Host "❌ Erreur lors du démarrage du service" -ForegroundColor Red
} finally {
    Set-Location ..
}