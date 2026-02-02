# Script de test pour les services du tableau de bord
Write-Host "=== Test des Services pour le Tableau de Bord ===" -ForegroundColor Green

# Services à tester
$services = @(
    @{Name="User Service"; Port=8081; HealthPath="/actuator/health"},
    @{Name="Reservation Service"; Port=8082; HealthPath="/actuator/health"},
    @{Name="Resource Service"; Port=8083; HealthPath="/actuator/health"}
)

Write-Host "`n1. Vérification de l'état des services..." -ForegroundColor Yellow

$allServicesUp = $true

foreach ($service in $services) {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:$($service.Port)$($service.HealthPath)" -Method GET -TimeoutSec 5
        if ($response.status -eq "UP") {
            Write-Host "✓ $($service.Name) (port $($service.Port)) - ACTIF" -ForegroundColor Green
        } else {
            Write-Host "✗ $($service.Name) (port $($service.Port)) - INACTIF" -ForegroundColor Red
            $allServicesUp = $false
        }
    } catch {
        Write-Host "✗ $($service.Name) (port $($service.Port)) - NON ACCESSIBLE" -ForegroundColor Red
        $allServicesUp = $false
    }
}

if (-not $allServicesUp) {
    Write-Host "`nCertains services ne sont pas actifs. Utilisez 'start-dashboard-services.bat' pour les démarrer." -ForegroundColor Yellow
    exit 1
}

Write-Host "`n2. Test des endpoints analytics..." -ForegroundColor Yellow

# Test des endpoints analytics
$analyticsEndpoints = @(
    @{Name="Dashboard Stats"; Url="http://localhost:8082/api/analytics/dashboard-stats?period=week"},
    @{Name="Room Occupancy"; Url="http://localhost:8082/api/analytics/room-occupancy?period=week"},
    @{Name="Analytics Health"; Url="http://localhost:8082/api/analytics/health"}
)

foreach ($endpoint in $analyticsEndpoints) {
    try {
        $response = Invoke-RestMethod -Uri $endpoint.Url -Method GET -TimeoutSec 10
        Write-Host "✓ $($endpoint.Name) - OK" -ForegroundColor Green
        
        # Afficher un échantillon des données pour dashboard stats
        if ($endpoint.Name -eq "Dashboard Stats") {
            Write-Host "  - Total salles: $($response.totalRooms)" -ForegroundColor Cyan
            Write-Host "  - Réservations actives: $($response.activeReservations)" -ForegroundColor Cyan
            Write-Host "  - Taux d'occupation: $($response.occupancyRate)%" -ForegroundColor Cyan
            Write-Host "  - Score d'efficacité: $($response.efficiencyScore)%" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "✗ $($endpoint.Name) - ERREUR: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n3. Test du frontend..." -ForegroundColor Yellow

try {
    $frontendResponse = Invoke-WebRequest -Uri "http://localhost:3000" -Method GET -TimeoutSec 5
    if ($frontendResponse.StatusCode -eq 200) {
        Write-Host "✓ Frontend accessible sur http://localhost:3000" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ Frontend non accessible - Démarrez-le avec: cd frontend && npm run dev" -ForegroundColor Yellow
}

Write-Host "`n=== Instructions d'accès ===" -ForegroundColor Green
Write-Host "1. Tableau de bord principal: http://localhost:3000" -ForegroundColor White
Write-Host "2. Analytics avancées: http://localhost:3000/analytics" -ForegroundColor White
Write-Host "3. API Analytics directe: http://localhost:8082/api/analytics/dashboard-stats" -ForegroundColor White

Write-Host "`n=== Résumé ===" -ForegroundColor Green
if ($allServicesUp) {
    Write-Host "✓ Tous les services sont actifs" -ForegroundColor Green
    Write-Host "✓ Le tableau de bord dynamique devrait fonctionner" -ForegroundColor Green
} else {
    Write-Host "✗ Certains services ne sont pas actifs" -ForegroundColor Red
    Write-Host "Utilisez 'start-dashboard-services.bat' pour les démarrer" -ForegroundColor Yellow
}