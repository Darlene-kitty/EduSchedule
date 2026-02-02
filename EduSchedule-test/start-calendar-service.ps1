#!/usr/bin/env pwsh

Write-Host "=== Démarrage du Calendar Service ===" -ForegroundColor Green

# Vérifier si Java est installé
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "Java détecté: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "ERREUR: Java n'est pas installé ou n'est pas dans le PATH" -ForegroundColor Red
    exit 1
}

# Vérifier si Maven est installé
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "Maven détecté: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "ERREUR: Maven n'est pas installé ou n'est pas dans le PATH" -ForegroundColor Red
    exit 1
}

# Vérifier si MySQL est en cours d'exécution
Write-Host "Vérification de MySQL..." -ForegroundColor Yellow
try {
    $mysqlProcess = Get-Process mysqld -ErrorAction SilentlyContinue
    if ($mysqlProcess) {
        Write-Host "MySQL est en cours d'exécution" -ForegroundColor Green
    } else {
        Write-Host "ATTENTION: MySQL ne semble pas être en cours d'exécution" -ForegroundColor Yellow
        Write-Host "Assurez-vous que MySQL est démarré avant de continuer" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Impossible de vérifier le statut de MySQL" -ForegroundColor Yellow
}

# Vérifier si Eureka Server est accessible
Write-Host "Vérification d'Eureka Server..." -ForegroundColor Yellow
try {
    $eurekaResponse = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps" -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($eurekaResponse.StatusCode -eq 200) {
        Write-Host "Eureka Server est accessible" -ForegroundColor Green
    } else {
        Write-Host "ATTENTION: Eureka Server n'est pas accessible" -ForegroundColor Yellow
    }
} catch {
    Write-Host "ATTENTION: Eureka Server n'est pas accessible sur le port 8761" -ForegroundColor Yellow
    Write-Host "Le service démarrera mais ne pourra pas s'enregistrer" -ForegroundColor Yellow
}

# Créer la base de données si elle n'existe pas
Write-Host "Création de la base de données calendar..." -ForegroundColor Yellow
try {
    mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS eduschedule_calendar;" 2>$null
    Write-Host "Base de données calendar créée/vérifiée" -ForegroundColor Green
} catch {
    Write-Host "ATTENTION: Impossible de créer la base de données calendar" -ForegroundColor Yellow
    Write-Host "Assurez-vous que MySQL est accessible avec root/root" -ForegroundColor Yellow
}

# Aller dans le répertoire du service
Set-Location calendar-service

Write-Host "Compilation du Calendar Service..." -ForegroundColor Yellow
try {
    mvn clean compile -q
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Compilation réussie" -ForegroundColor Green
    } else {
        Write-Host "ERREUR: Échec de la compilation" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "ERREUR: Échec de la compilation" -ForegroundColor Red
    exit 1
}

Write-Host "Démarrage du Calendar Service sur le port 8092..." -ForegroundColor Green
Write-Host "URL du service: http://localhost:8092" -ForegroundColor Cyan
Write-Host "API Calendrier: http://localhost:8092/api/calendar" -ForegroundColor Cyan
Write-Host "" -ForegroundColor White
Write-Host "Variables d'environnement recommandées:" -ForegroundColor Yellow
Write-Host "- GOOGLE_CLIENT_ID: ID client Google Calendar" -ForegroundColor Gray
Write-Host "- GOOGLE_CLIENT_SECRET: Secret client Google Calendar" -ForegroundColor Gray
Write-Host "- OUTLOOK_CLIENT_ID: ID client Outlook Calendar" -ForegroundColor Gray
Write-Host "- OUTLOOK_CLIENT_SECRET: Secret client Outlook Calendar" -ForegroundColor Gray
Write-Host "" -ForegroundColor White
Write-Host "Appuyez sur Ctrl+C pour arrêter le service" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Green

# Démarrer le service
try {
    mvn spring-boot:run
} catch {
    Write-Host "ERREUR: Échec du démarrage du service" -ForegroundColor Red
    exit 1
}