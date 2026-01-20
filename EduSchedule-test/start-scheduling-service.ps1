# Démarrage du Scheduling Service pour les tests P1
# Port: 8084

Write-Host "🚀 Démarrage du Scheduling Service (Port 8084)" -ForegroundColor Cyan

# Vérifier si le service est déjà en cours d'exécution
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8084/api/schedules" -Method GET -TimeoutSec 5
    Write-Host "✅ Scheduling Service déjà en cours d'exécution" -ForegroundColor Green
    exit 0
} catch {
    Write-Host "⏳ Scheduling Service non démarré, lancement en cours..." -ForegroundColor Yellow
}

# Charger les variables d'environnement
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^([^#][^=]+)=(.*)$") {
            [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
        }
    }
    Write-Host "✅ Variables d'environnement chargées depuis .env" -ForegroundColor Green
}

# Démarrer le service
Set-Location "scheduling-service"

Write-Host "📦 Compilation du Scheduling Service..." -ForegroundColor Yellow
& ../mvnw clean compile -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation réussie" -ForegroundColor Green
    
    Write-Host "🚀 Démarrage du service sur le port 8084..." -ForegroundColor Yellow
    Write-Host "   URL: http://localhost:8084" -ForegroundColor Gray
    Write-Host "   API: http://localhost:8084/api/schedules" -ForegroundColor Gray
    Write-Host "" -ForegroundColor Gray
    Write-Host "💡 Utilisez Ctrl+C pour arrêter le service" -ForegroundColor Yellow
    Write-Host "💡 Lancez 'test-integration-finale.ps1' dans un autre terminal" -ForegroundColor Yellow
    Write-Host ""
    
    # Démarrer avec profil dev
    & ../mvnw spring-boot:run -Dspring-boot.run.profiles=dev
} else {
    Write-Host "Erreur lors de la compilation" -ForegroundColor Red
    Set-Location ..
    exit 1
}

Set-Location ..