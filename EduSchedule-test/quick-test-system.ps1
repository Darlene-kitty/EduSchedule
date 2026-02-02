# Test rapide du système EduSchedule
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Rapide du Système EduSchedule" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$baseUrl = "http://localhost:8080"
$frontendUrl = "http://localhost:3000"

# Fonction pour tester un endpoint
function Test-Endpoint {
    param($url, $name)
    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 5
        Write-Host "✅ $name : OK" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "❌ $name : ERREUR" -ForegroundColor Red
        return $false
    }
}

# Test des services
Write-Host "Test des services backend:" -ForegroundColor Yellow
Write-Host ""

$services = @(
    @{url="http://localhost:8761"; name="Eureka Server"},
    @{url="$baseUrl/actuator/health"; name="API Gateway"},
    @{url="$baseUrl/api/users/health"; name="User Service"},
    @{url="$baseUrl/api/notifications/health"; name="Notification Service"},
    @{url="$baseUrl/api/resources/health"; name="Resource Service"},
    @{url="$baseUrl/api/courses/health"; name="Course Service"},
    @{url="$baseUrl/api/reservations/health"; name="Reservation Service"},
    @{url="$baseUrl/api/schedules/health"; name="Scheduling Service"},
    @{url="$baseUrl/api/schools/health"; name="School Service"}
)

$successCount = 0
foreach ($service in $services) {
    if (Test-Endpoint -url $service.url -name $service.name) {
        $successCount++
    }
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "Test du frontend:" -ForegroundColor Yellow
if (Test-Endpoint -url $frontendUrl -name "Frontend React") {
    $successCount++
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Résultats du test:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$totalServices = $services.Count + 1
$percentage = [math]::Round(($successCount / $totalServices) * 100, 1)

if ($percentage -eq 100) {
    Write-Host "🎉 Système entièrement fonctionnel ! ($successCount/$totalServices services)" -ForegroundColor Green
} elseif ($percentage -ge 80) {
    Write-Host "⚠️  Système majoritairement fonctionnel ($successCount/$totalServices services - $percentage%)" -ForegroundColor Yellow
} else {
    Write-Host "❌ Problèmes détectés ($successCount/$totalServices services - $percentage%)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Liens utiles:" -ForegroundColor Cyan
Write-Host "- Application: $frontendUrl" -ForegroundColor White
Write-Host "- Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "- API Gateway Health: $baseUrl/actuator/health" -ForegroundColor White
Write-Host ""

if ($percentage -lt 100) {
    Write-Host "Pour diagnostiquer les problèmes:" -ForegroundColor Yellow
    Write-Host "1. Vérifiez que tous les services sont démarrés" -ForegroundColor White
    Write-Host "2. Attendez 2-3 minutes pour l'enregistrement Eureka" -ForegroundColor White
    Write-Host "3. Consultez les logs des services en erreur" -ForegroundColor White
    Write-Host ""
}

Write-Host "Test terminé." -ForegroundColor Cyan