#!/usr/bin/env pwsh

# Script de test de connectivité API
# Teste la connectivité vers les différents services

Write-Host "=== Test de connectivité API ===" -ForegroundColor Green
Write-Host ""

# Configuration
$API_GATEWAY = "http://localhost:8080"
$USER_SERVICE = "http://localhost:8081"
$RESOURCE_SERVICE = "http://localhost:8082"
$COURSE_SERVICE = "http://localhost:8083"
$RESERVATION_SERVICE = "http://localhost:8085"

# Fonction pour tester un endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url
    )
    
    Write-Host "Testing $Name..." -NoNewline
    
    try {
        $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec 5 -ErrorAction Stop
        Write-Host " ✅ OK (Status: $($response.StatusCode))" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host " ❌ FAILED" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Yellow
        return $false
    }
}

# Test des services
Write-Host "1. Test des services individuels:" -ForegroundColor Cyan
$userServiceOk = Test-Endpoint "User Service" "$USER_SERVICE/actuator/health"
$resourceServiceOk = Test-Endpoint "Resource Service" "$RESOURCE_SERVICE/actuator/health"
$courseServiceOk = Test-Endpoint "Course Service" "$COURSE_SERVICE/actuator/health"
$reservationServiceOk = Test-Endpoint "Reservation Service" "$RESERVATION_SERVICE/actuator/health"

Write-Host ""
Write-Host "2. Test de l'API Gateway:" -ForegroundColor Cyan
$gatewayOk = Test-Endpoint "API Gateway" "$API_GATEWAY/actuator/health"

Write-Host ""
Write-Host "3. Test des endpoints via Gateway:" -ForegroundColor Cyan
Test-Endpoint "Auth endpoint" "$API_GATEWAY/api/auth/health"
Test-Endpoint "Resources endpoint" "$API_GATEWAY/api/v1/resources"
Test-Endpoint "Analytics endpoint" "$API_GATEWAY/api/v1/analytics/dashboard-stats"

Write-Host ""
Write-Host "=== Résumé ===" -ForegroundColor Green

if ($userServiceOk -and $resourceServiceOk -and $courseServiceOk -and $reservationServiceOk -and $gatewayOk) {
    Write-Host "✅ Tous les services sont opérationnels" -ForegroundColor Green
    Write-Host "Le problème 'Failed to fetch' peut venir de:" -ForegroundColor Yellow
    Write-Host "  - CORS non configuré correctement" -ForegroundColor Yellow
    Write-Host "  - Authentification requise mais token manquant" -ForegroundColor Yellow
    Write-Host "  - Endpoint analytics non implémenté côté backend" -ForegroundColor Yellow
} else {
    Write-Host "❌ Certains services ne répondent pas" -ForegroundColor Red
    Write-Host "Démarrez les services manquants avec:" -ForegroundColor Yellow
    Write-Host "  .\start-services-complets.bat" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Pour résoudre l'erreur 'Failed to fetch':" -ForegroundColor Cyan
Write-Host "1. Vérifiez que l'API Gateway est démarré (port 8080)" -ForegroundColor White
Write-Host "2. Vérifiez la configuration CORS dans l'API Gateway" -ForegroundColor White
Write-Host "3. Vérifiez que les endpoints analytics existent côté backend" -ForegroundColor White
Write-Host "4. Utilisez les données simulées en attendant" -ForegroundColor White