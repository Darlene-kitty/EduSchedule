# Script de test pour le tableau de bord dynamique
Write-Host "=== Test du Tableau de Bord Dynamique ===" -ForegroundColor Green

# Vérifier que les services backend sont démarrés
Write-Host "1. Vérification des services backend..." -ForegroundColor Yellow

$services = @(
    @{Name="User Service"; Port=8081; Path="/actuator/health"},
    @{Name="Reservation Service"; Port=8082; Path="/actuator/health"},
    @{Name="Resource Service"; Port=8083; Path="/actuator/health"}
)

$allServicesRunning = $true

foreach ($service in $services) {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:$($service.Port)$($service.Path)" -Method GET -TimeoutSec 5
        if ($response.status -eq "UP") {
            Write-Host "✓ $($service.Name) est actif" -ForegroundColor Green
        } else {
            Write-Host "✗ $($service.Name) n'est pas actif" -ForegroundColor Red
            $allServicesRunning = $false
        }
    } catch {
        Write-Host "✗ $($service.Name) n'est pas accessible" -ForegroundColor Red
        $allServicesRunning = $false
    }
}

if (-not $allServicesRunning) {
    Write-Host "Certains services ne sont pas actifs. Démarrage des services..." -ForegroundColor Yellow
    Start-Process -FilePath "start-backend-only.bat" -NoNewWindow
    Write-Host "Attente de 30 secondes pour le démarrage des services..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
}

# Test des endpoints analytics
Write-Host "`n2. Test des endpoints analytics..." -ForegroundColor Yellow

$analyticsEndpoints = @(
    @{Name="Dashboard Stats"; Url="http://localhost:8082/analytics/dashboard-stats?period=week"},
    @{Name="Room Occupancy"; Url="http://localhost:8082/analytics/room-occupancy?period=week"},
    @{Name="Hourly Occupancy"; Url="http://localhost:8082/analytics/hourly-occupancy?date=2024-01-20"},
    @{Name="Weekly Data"; Url="http://localhost:8082/analytics/weekly-data?startDate=2024-01-15"}
)

foreach ($endpoint in $analyticsEndpoints) {
    try {
        $response = Invoke-RestMethod -Uri $endpoint.Url -Method GET -TimeoutSec 10
        Write-Host "✓ $($endpoint.Name) - Données récupérées" -ForegroundColor Green
        
        # Afficher un échantillon des données
        if ($endpoint.Name -eq "Dashboard Stats") {
            Write-Host "  - Total salles: $($response.totalRooms)" -ForegroundColor Cyan
            Write-Host "  - Réservations actives: $($response.activeReservations)" -ForegroundColor Cyan
            Write-Host "  - Taux d'occupation: $($response.occupancyRate)%" -ForegroundColor Cyan
            Write-Host "  - Score d'efficacité: $($response.efficiencyScore)%" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "✗ $($endpoint.Name) - Erreur: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test du frontend
Write-Host "`n3. Test du frontend..." -ForegroundColor Yellow

try {
    $frontendResponse = Invoke-WebRequest -Uri "http://localhost:3000" -Method GET -TimeoutSec 10
    if ($frontendResponse.StatusCode -eq 200) {
        Write-Host "✓ Frontend accessible" -ForegroundColor Green
    } else {
        Write-Host "✗ Frontend non accessible" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Frontend non accessible - Erreur: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Assurez-vous que le frontend est démarré avec 'npm run dev'" -ForegroundColor Yellow
}

Write-Host "`n=== Résumé ===" -ForegroundColor Green
Write-Host "Le tableau de bord a été mis à jour pour être dynamique:" -ForegroundColor White
Write-Host "• Utilise l'API analytics pour récupérer les données en temps réel" -ForegroundColor White
Write-Host "• Sélecteur de période (jour/semaine/mois/trimestre)" -ForegroundColor White
Write-Host "• Bouton d'actualisation" -ForegroundColor White
Write-Host "• Indicateur de dernière mise à jour" -ForegroundColor White
Write-Host "• Score d'efficacité global avec barre de progression" -ForegroundColor White
Write-Host "• Gestion des erreurs et états de chargement" -ForegroundColor White

Write-Host "`nAccédez au tableau de bord sur: http://localhost:3000" -ForegroundColor Cyan
Write-Host "Accédez aux analytics avancées sur: http://localhost:3000/analytics" -ForegroundColor Cyan