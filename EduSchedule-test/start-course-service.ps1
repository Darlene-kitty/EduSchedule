# Démarrage simple du Course Service
# Utilise ce script si les tables sont déjà créées

Write-Host "=== Démarrage du Course Service ===" -ForegroundColor Cyan

# Vérifier que nous sommes dans le bon répertoire
if (-not (Test-Path "course-service")) {
    Write-Host "❌ Répertoire course-service non trouvé" -ForegroundColor Red
    Write-Host "Assurez-vous d'être dans le répertoire racine du projet" -ForegroundColor Yellow
    exit 1
}

# Aller dans le répertoire du service
Set-Location course-service

Write-Host "`nDémarrage du Course Service sur le port 8084..." -ForegroundColor Yellow
Write-Host "Appuyez sur Ctrl+C pour arrêter le service" -ForegroundColor Cyan

try {
    # Démarrer le service Spring Boot
    mvn spring-boot:run
}
catch {
    Write-Host "❌ Erreur lors du démarrage: $($_.Exception.Message)" -ForegroundColor Red
}
finally {
    # Retourner au répertoire parent
    Set-Location ..
    Write-Host "`nService arrêté" -ForegroundColor Yellow
}