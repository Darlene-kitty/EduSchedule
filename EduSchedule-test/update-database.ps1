#!/usr/bin/env pwsh

Write-Host "=== Mise à jour de la base de données ===" -ForegroundColor Green

# Charger les variables d'environnement
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^([^#][^=]+)=(.*)$") {
            [Environment]::SetEnvironmentVariable($matches[1], $matches[2])
        }
    }
    Write-Host "Variables d'environnement chargées depuis .env" -ForegroundColor Yellow
}

# Configuration de la base de données
$MYSQL_HOST = if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { "localhost" }
$MYSQL_PORT = if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { "3306" }
$MYSQL_DATABASE = if ($env:MYSQL_DATABASE) { $env:MYSQL_DATABASE } else { "iusjcdb" }
$MYSQL_USER = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "iusjc" }
$MYSQL_PASSWORD = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { "iusjc2025" }

Write-Host "Connexion à MySQL: ${MYSQL_USER}@${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}" -ForegroundColor Cyan

# Exécuter le script de mise à jour
Write-Host "Exécution du script de mise à jour..." -ForegroundColor Yellow

try {
    # Utiliser mysql client pour exécuter le script
    $mysqlCommand = "mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE"
    
    if (Test-Path "update-user-table.sql") {
        Write-Host "Exécution de update-user-table.sql..." -ForegroundColor Cyan
        Get-Content "update-user-table.sql" | & mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE
        Write-Host "✅ Mise à jour terminée avec succès!" -ForegroundColor Green
    } else {
        Write-Host "❌ Fichier update-user-table.sql introuvable!" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur lors de la mise à jour: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Vérifiez que MySQL est démarré et accessible." -ForegroundColor Yellow
}

Write-Host "=== Fin de la mise à jour ===" -ForegroundColor Green