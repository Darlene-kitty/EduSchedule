# Demarrage du Scheduling Service pour les tests P1
# Port: 8084

Write-Host "Demarrage du Scheduling Service (Port 8084)" -ForegroundColor Cyan

# Verifier si le service est deja en cours d'execution
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8084/api/schedules" -Method GET -TimeoutSec 5
    Write-Host "Service deja en cours d'execution" -ForegroundColor Green
    exit 0
} catch {
    Write-Host "Service non demarre, lancement en cours..." -ForegroundColor Yellow
}

# Charger les variables d'environnement
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^([^#][^=]+)=(.*)$") {
            [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
        }
    }
    Write-Host "Variables d'environnement chargees depuis .env" -ForegroundColor Green
}

# Demarrer le service
Set-Location "scheduling-service"

Write-Host "Compilation du Scheduling Service..." -ForegroundColor Yellow
& ../mvnw clean compile -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation reussie" -ForegroundColor Green
    
    Write-Host "Demarrage du service sur le port 8084..." -ForegroundColor Yellow
    Write-Host "URL: http://localhost:8084" -ForegroundColor Gray
    Write-Host "API: http://localhost:8084/api/schedules" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Utilisez Ctrl+C pour arreter le service" -ForegroundColor Yellow
    Write-Host "Lancez 'test-integration-finale.ps1' dans un autre terminal" -ForegroundColor Yellow
    Write-Host ""
    
    # Demarrer avec profil dev
    & ../mvnw spring-boot:run -Dspring-boot.run.profiles=dev
} else {
    Write-Host "Erreur lors de la compilation" -ForegroundColor Red
    Set-Location ..
    exit 1
}

Set-Location ..