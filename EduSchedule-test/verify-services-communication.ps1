# Script de verification de la communication entre services
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verification de la communication inter-services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$API_BASE = "http://localhost:8080"

# Test 1: Verifier que l'API Gateway est accessible
Write-Host "[TEST 1] API Gateway..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/actuator/health" -Method Get
    if ($response.status -eq "UP") {
        Write-Host "[OK] API Gateway est UP" -ForegroundColor Green
    }
}
catch {
    Write-Host "[ERREUR] API Gateway inaccessible" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 2: Verifier l'enregistrement Eureka
Write-Host "[TEST 2] Enregistrement Eureka..." -ForegroundColor Yellow
try {
    $eurekaResponse = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Method Get
    $apps = $eurekaResponse.applications.application
    
    $expectedServices = @(
        "API-GATEWAY", "USER-SERVICE", "RESOURCE-SERVICE", "SCHOOL-SERVICE",
        "COURSE-SERVICE", "SCHEDULING-SERVICE", "RESERVATION-SERVICE",
        "NOTIFICATION-SERVICE", "REPORTING-SERVICE", "CALENDAR-SERVICE",
        "ROOM-SERVICE", "EVENT-SERVICE", "TEACHER-AVAILABILITY-SERVICE"
    )
    
    $registeredServices = @()
    if ($apps) {
        foreach ($app in $apps) {
            $registeredServices += $app.name
        }
    }
    
    Write-Host "Services enregistres: $($registeredServices.Count)" -ForegroundColor Cyan
    foreach ($service in $registeredServices) {
        Write-Host "  - $service" -ForegroundColor Green
    }
    
    $missingServices = $expectedServices | Where-Object { $registeredServices -notcontains $_ }
    if ($missingServices.Count -gt 0) {
        Write-Host "Services manquants:" -ForegroundColor Yellow
        foreach ($service in $missingServices) {
            Write-Host "  - $service" -ForegroundColor Yellow
        }
    }
}
catch {
    Write-Host "[ERREUR] Impossible de contacter Eureka" -ForegroundColor Red
}
Write-Host ""

# Test 3: Tester les endpoints via API Gateway
Write-Host "[TEST 3] Endpoints via API Gateway..." -ForegroundColor Yellow

$endpoints = @(
    @{Name="User Service"; Path="/api/users/health"},
    @{Name="School Service"; Path="/api/schools/health"},
    @{Name="Course Service"; Path="/api/courses/health"},
    @{Name="Resource Service"; Path="/api/resources/health"},
    @{Name="Room Service"; Path="/api/rooms/health"}
)

foreach ($endpoint in $endpoints) {
    try {
        $response = Invoke-WebRequest -Uri "$API_BASE$($endpoint.Path)" -Method Get -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "[OK] $($endpoint.Name) accessible via Gateway" -ForegroundColor Green
        }
    }
    catch {
        Write-Host "[WARN] $($endpoint.Name) non accessible: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}
Write-Host ""

# Test 4: Verifier les conteneurs Docker
Write-Host "[TEST 4] Etat des conteneurs Docker..." -ForegroundColor Yellow
$containers = docker ps --format "table {{.Names}}\t{{.Status}}" | Select-Object -Skip 1

Write-Host "Conteneurs en cours d'execution:" -ForegroundColor Cyan
$containers | ForEach-Object {
    if ($_ -match "Up") {
        Write-Host "  $_" -ForegroundColor Green
    }
    else {
        Write-Host "  $_" -ForegroundColor Red
    }
}
Write-Host ""

# Test 5: Verifier les logs pour les erreurs critiques
Write-Host "[TEST 5] Verification des erreurs critiques..." -ForegroundColor Yellow
$criticalErrors = docker-compose logs --tail=100 2>&1 | Select-String -Pattern "ERROR|FATAL|Exception" | Select-Object -First 10

if ($criticalErrors) {
    Write-Host "[WARN] Erreurs detectees dans les logs:" -ForegroundColor Yellow
    $criticalErrors | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
}
else {
    Write-Host "[OK] Aucune erreur critique detectee" -ForegroundColor Green
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VERIFICATION TERMINEE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
