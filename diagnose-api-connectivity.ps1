#!/usr/bin/env pwsh
# Script de diagnostic pour les problèmes de connectivité API

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "DIAGNOSTIC CONNECTIVITE API" -ForegroundColor Cyan
Write-Host "Vérification des services et endpoints" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Fonction pour tester un endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [int]$TimeoutSec = 5
    )
    
    Write-Host "Test de $Name..." -ForegroundColor Yellow
    Write-Host "  URL: $Url" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $Url -Method GET -TimeoutSec $TimeoutSec -ErrorAction Stop
        Write-Host "  ✅ OK - Status: 200" -ForegroundColor Green
        return $true
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode) {
            Write-Host "  ❌ Erreur HTTP $statusCode" -ForegroundColor Red
        } else {
            Write-Host "  ❌ Connexion impossible - $($_.Exception.Message)" -ForegroundColor Red
        }
        return $false
    }
}

# Fonction pour tester un service complet
function Test-Service {
    param(
        [string]$ServiceName,
        [string]$BaseUrl,
        [string[]]$Endpoints
    )
    
    Write-Host ""
    Write-Host "=== $ServiceName ===" -ForegroundColor Cyan
    
    $results = @()
    foreach ($endpoint in $Endpoints) {
        $fullUrl = "$BaseUrl$endpoint"
        $result = Test-Endpoint -Name $endpoint -Url $fullUrl
        $results += $result
    }
    
    $successCount = ($results | Where-Object { $_ -eq $true }).Count
    $totalCount = $results.Count
    
    Write-Host "  Résultat: $successCount/$totalCount endpoints accessibles" -ForegroundColor $(if($successCount -eq $totalCount){'Green'}else{'Yellow'})
    
    return $successCount -eq $totalCount
}

Write-Host "1. Test des services d'infrastructure..." -ForegroundColor Cyan

# Test Eureka
$eurekaOK = Test-Endpoint -Name "Eureka Server" -Url "http://localhost:8761/actuator/health"

# Test API Gateway
$gatewayOK = Test-Endpoint -Name "API Gateway" -Url "http://localhost:8080/actuator/health"

Write-Host ""
Write-Host "2. Test des services métier..." -ForegroundColor Cyan

# Test des services individuels
$userServiceOK = Test-Service -ServiceName "User Service" -BaseUrl "http://localhost:8081" -Endpoints @(
    "/actuator/health",
    "/api/auth/health"
)

$reservationServiceOK = Test-Service -ServiceName "Reservation Service" -BaseUrl "http://localhost:8085" -Endpoints @(
    "/actuator/health"
)

$teacherAvailabilityOK = Test-Service -ServiceName "Teacher Availability Service" -BaseUrl "http://localhost:8089" -Endpoints @(
    "/actuator/health",
    "/api/teacher-availability/health"
)

Write-Host ""
Write-Host "3. Test du routage API Gateway..." -ForegroundColor Cyan

if ($gatewayOK) {
    $gatewayRoutes = Test-Service -ServiceName "API Gateway Routes" -BaseUrl "http://localhost:8080" -Endpoints @(
        "/api/users/health",
        "/api/teacher-availability/health",
        "/api/v1/reservations/health"
    )
} else {
    Write-Host "  ⚠️  API Gateway non accessible - impossible de tester les routes" -ForegroundColor Yellow
    $gatewayRoutes = $false
}

Write-Host ""
Write-Host "4. Test des endpoints Analytics..." -ForegroundColor Cyan

# Test des endpoints analytics qui causent l'erreur
$analyticsEndpoints = @(
    "http://localhost:8085/api/analytics/dashboard-stats",
    "http://localhost:8085/api/analytics/room-occupancy",
    "http://localhost:8085/api/analytics/hourly-occupancy",
    "http://localhost:8080/api/v1/reservations/analytics/dashboard-stats"
)

$analyticsOK = $true
foreach ($endpoint in $analyticsEndpoints) {
    $result = Test-Endpoint -Name "Analytics Endpoint" -Url $endpoint
    if (-not $result) { $analyticsOK = $false }
}

Write-Host ""
Write-Host "5. Vérification de la configuration Eureka..." -ForegroundColor Cyan

if ($eurekaOK) {
    try {
        $eurekaApps = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{"Accept"="application/json"} -TimeoutSec 5
        Write-Host "  Services enregistrés dans Eureka:" -ForegroundColor Gray
        
        if ($eurekaApps.applications.application) {
            foreach ($app in $eurekaApps.applications.application) {
                $appName = $app.name
                $instanceCount = if ($app.instance -is [array]) { $app.instance.Count } else { 1 }
                Write-Host "    - $appName ($instanceCount instance(s))" -ForegroundColor Gray
            }
        } else {
            Write-Host "    ⚠️  Aucun service enregistré" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "  ❌ Erreur lors de la récupération des services Eureka" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RÉSUMÉ DU DIAGNOSTIC" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "Infrastructure:" -ForegroundColor White
Write-Host "  Eureka Server:           $(if($eurekaOK){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($eurekaOK){'Green'}else{'Red'})
Write-Host "  API Gateway:             $(if($gatewayOK){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($gatewayOK){'Green'}else{'Red'})

Write-Host ""
Write-Host "Services Métier:" -ForegroundColor White
Write-Host "  User Service:            $(if($userServiceOK){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($userServiceOK){'Green'}else{'Red'})
Write-Host "  Reservation Service:     $(if($reservationServiceOK){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($reservationServiceOK){'Green'}else{'Red'})
Write-Host "  Teacher Availability:    $(if($teacherAvailabilityOK){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($teacherAvailabilityOK){'Green'}else{'Red'})

Write-Host ""
Write-Host "Routage:" -ForegroundColor White
Write-Host "  Gateway Routes:          $(if($gatewayRoutes){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($gatewayRoutes){'Green'}else{'Red'})

Write-Host ""
Write-Host "Analytics:" -ForegroundColor White
Write-Host "  Endpoints Analytics:     $(if($analyticsOK){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($analyticsOK){'Green'}else{'Red'})

Write-Host ""
if ($eurekaOK -and $gatewayOK -and $userServiceOK -and $reservationServiceOK) {
    Write-Host "🎉 DIAGNOSTIC POSITIF - Services principaux opérationnels" -ForegroundColor Green
    Write-Host ""
    Write-Host "Le problème 'Failed to fetch' peut être dû à:" -ForegroundColor Cyan
    Write-Host "  • Endpoints analytics manquants dans Reservation Service" -ForegroundColor Gray
    Write-Host "  • Configuration CORS" -ForegroundColor Gray
    Write-Host "  • Authentification requise" -ForegroundColor Gray
} else {
    Write-Host "⚠️  PROBLÈMES DÉTECTÉS" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Actions recommandées:" -ForegroundColor Cyan
    
    if (-not $eurekaOK) {
        Write-Host "  • Démarrer Eureka Server: cd eureka-server && mvn spring-boot:run" -ForegroundColor Gray
    }
    if (-not $gatewayOK) {
        Write-Host "  • Démarrer API Gateway: cd api-gateway && mvn spring-boot:run" -ForegroundColor Gray
    }
    if (-not $userServiceOK) {
        Write-Host "  • Démarrer User Service: cd user-service && mvn spring-boot:run" -ForegroundColor Gray
    }
    if (-not $reservationServiceOK) {
        Write-Host "  • Démarrer Reservation Service: cd reservation-service && mvn spring-boot:run" -ForegroundColor Gray
    }
    if (-not $teacherAvailabilityOK) {
        Write-Host "  • Démarrer Teacher Availability Service: cd teacher-availability-service && mvn spring-boot:run" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "Pour démarrer tous les services:" -ForegroundColor Cyan
Write-Host "  .\start-services-complets.bat" -ForegroundColor Gray
Write-Host ""
Write-Host "Pour tester spécifiquement Teacher Availability + Reservation:" -ForegroundColor Cyan
Write-Host "  .\test-teacher-availability-reservation.ps1" -ForegroundColor Gray
Write-Host ""