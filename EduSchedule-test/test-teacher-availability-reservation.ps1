#!/usr/bin/env pwsh
# Script de test pour Teacher Availability Service et Reservation Service sans Docker

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST TEACHER AVAILABILITY & RESERVATION SERVICES" -ForegroundColor Cyan
Write-Host "Test sans Docker - Configuration MySQL locale" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Fonction pour tester un service
function Test-Service {
    param(
        [string]$ServiceName,
        [string]$Port,
        [string]$HealthEndpoint
    )
    
    Write-Host "Test de $ServiceName sur le port $Port..." -ForegroundColor Yellow
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:$Port$HealthEndpoint" -Method GET -TimeoutSec 5
        Write-Host "✅ $ServiceName est accessible" -ForegroundColor Green
        Write-Host "   Status: $($response.status)" -ForegroundColor Gray
        return $true
    }
    catch {
        Write-Host "❌ $ServiceName n'est pas accessible" -ForegroundColor Red
        Write-Host "   Erreur: $($_.Exception.Message)" -ForegroundColor Gray
        return $false
    }
}

# Fonction pour tester l'API Teacher Availability
function Test-TeacherAvailabilityAPI {
    Write-Host "Test des endpoints Teacher Availability Service..." -ForegroundColor Yellow
    
    try {
        # Test endpoint de santé spécifique
        $healthResponse = Invoke-RestMethod -Uri "http://localhost:8089/api/teacher-availability/health" -Method GET -TimeoutSec 5
        Write-Host "✅ Health endpoint OK" -ForegroundColor Green
        
        # Test endpoint de statistiques
        $statsResponse = Invoke-RestMethod -Uri "http://localhost:8089/api/teacher-availability/stats" -Method GET -TimeoutSec 5
        Write-Host "✅ Stats endpoint OK" -ForegroundColor Green
        Write-Host "   Disponibilités actives: $($statsResponse.totalActiveAvailabilities)" -ForegroundColor Gray
        Write-Host "   Enseignants avec disponibilités: $($statsResponse.teachersWithAvailabilities)" -ForegroundColor Gray
        
        return $true
    }
    catch {
        Write-Host "❌ Erreur lors du test des endpoints" -ForegroundColor Red
        Write-Host "   Erreur: $($_.Exception.Message)" -ForegroundColor Gray
        return $false
    }
}

# Vérification des prérequis
Write-Host "1. Vérification des prérequis..." -ForegroundColor Cyan

# Vérifier si MySQL est accessible
try {
    # Test de connexion MySQL (nécessite mysql client)
    Write-Host "   Vérification de MySQL..." -ForegroundColor Gray
    # Note: Cette vérification nécessiterait le client MySQL
    Write-Host "   ⚠️  Vérifiez manuellement que MySQL est démarré sur localhost:3306" -ForegroundColor Yellow
}
catch {
    Write-Host "   ⚠️  Impossible de vérifier MySQL automatiquement" -ForegroundColor Yellow
}

# Vérifier si Eureka est accessible
$eurekaOK = Test-Service -ServiceName "Eureka Server" -Port "8761" -HealthEndpoint "/actuator/health"

Write-Host ""
Write-Host "2. Test des services..." -ForegroundColor Cyan

# Test Teacher Availability Service
$teacherAvailabilityOK = Test-Service -ServiceName "Teacher Availability Service" -Port "8089" -HealthEndpoint "/actuator/health"

if ($teacherAvailabilityOK) {
    $apiOK = Test-TeacherAvailabilityAPI
}

# Test Reservation Service
$reservationOK = Test-Service -ServiceName "Reservation Service" -Port "8085" -HealthEndpoint "/actuator/health"

Write-Host ""
Write-Host "3. Test de l'API Gateway..." -ForegroundColor Cyan

# Test API Gateway
$gatewayOK = Test-Service -ServiceName "API Gateway" -Port "8080" -HealthEndpoint "/actuator/health"

if ($gatewayOK) {
    Write-Host "Test du routage via API Gateway..." -ForegroundColor Yellow
    
    try {
        # Test du routage vers Teacher Availability Service
        $routeResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/teacher-availability/health" -Method GET -TimeoutSec 5
        Write-Host "✅ Routage API Gateway → Teacher Availability Service OK" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Erreur de routage API Gateway → Teacher Availability Service" -ForegroundColor Red
        Write-Host "   Erreur: $($_.Exception.Message)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RÉSUMÉ DES TESTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "Services Infrastructure:" -ForegroundColor White
Write-Host "  Eureka Server:           $(if($eurekaOK){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($eurekaOK){'Green'}else{'Red'})
Write-Host "  API Gateway:             $(if($gatewayOK){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($gatewayOK){'Green'}else{'Red'})

Write-Host ""
Write-Host "Services Métier:" -ForegroundColor White
Write-Host "  Teacher Availability:    $(if($teacherAvailabilityOK){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($teacherAvailabilityOK){'Green'}else{'Red'})
Write-Host "  Reservation Service:     $(if($reservationOK){'✅ OK'}else{'❌ KO'})" -ForegroundColor $(if($reservationOK){'Green'}else{'Red'})

Write-Host ""
if ($teacherAvailabilityOK -and $reservationOK -and $gatewayOK) {
    Write-Host "🎉 TOUS LES SERVICES SONT OPÉRATIONNELS !" -ForegroundColor Green
    Write-Host ""
    Write-Host "Vous pouvez maintenant:" -ForegroundColor Cyan
    Write-Host "  • Utiliser le frontend sur http://localhost:3000" -ForegroundColor Gray
    Write-Host "  • Accéder aux APIs via l'API Gateway sur http://localhost:8080" -ForegroundColor Gray
    Write-Host "  • Gérer les disponibilités enseignants avec Teacher Availability Service" -ForegroundColor Gray
    Write-Host "  • Créer des réservations avec Reservation Service" -ForegroundColor Gray
} else {
    Write-Host "⚠️  CERTAINS SERVICES NE SONT PAS DISPONIBLES" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Actions recommandées:" -ForegroundColor Cyan
    if (-not $eurekaOK) {
        Write-Host "  • Démarrer Eureka Server: cd eureka-server && mvn spring-boot:run" -ForegroundColor Gray
    }
    if (-not $teacherAvailabilityOK) {
        Write-Host "  • Démarrer Teacher Availability Service: cd teacher-availability-service && mvn spring-boot:run" -ForegroundColor Gray
    }
    if (-not $reservationOK) {
        Write-Host "  • Démarrer Reservation Service: cd reservation-service && mvn spring-boot:run" -ForegroundColor Gray
    }
    if (-not $gatewayOK) {
        Write-Host "  • Démarrer API Gateway: cd api-gateway && mvn spring-boot:run" -ForegroundColor Gray
    }
    Write-Host "  • Vérifier que MySQL est démarré et accessible" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Pour plus d'informations, consultez:" -ForegroundColor Cyan
Write-Host "  • INTEGRATION_FRONTEND_BACKEND.md" -ForegroundColor Gray
Write-Host "  • GUIDE_DISPONIBILITES_ENSEIGNANTS.md" -ForegroundColor Gray
Write-Host ""