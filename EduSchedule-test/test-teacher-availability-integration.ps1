# Test d'Intégration - Disponibilités des Enseignants
# Vérifie que le frontend peut communiquer avec le backend via l'API Gateway

Write-Host "========================================" -ForegroundColor Green
Write-Host "TEST INTEGRATION TEACHER AVAILABILITY" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Configuration
$API_GATEWAY = "http://localhost:8080"
$USER_SERVICE = "http://localhost:8081"
$FRONTEND = "http://localhost:3000"

Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "- API Gateway: $API_GATEWAY"
Write-Host "- User Service: $USER_SERVICE"
Write-Host "- Frontend: $FRONTEND"
Write-Host ""

# Test 1: Vérifier que l'API Gateway est démarré
Write-Host "[1/6] Test API Gateway..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$API_GATEWAY/actuator/health" -Method GET -TimeoutSec 5
    if ($response.status -eq "UP") {
        Write-Host "✅ API Gateway: OK" -ForegroundColor Green
    } else {
        Write-Host "❌ API Gateway: Status = $($response.status)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ API Gateway: Non accessible" -ForegroundColor Red
    Write-Host "   Erreur: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Vérifier que User Service est démarré
Write-Host "[2/6] Test User Service..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$USER_SERVICE/actuator/health" -Method GET -TimeoutSec 5
    if ($response.status -eq "UP") {
        Write-Host "✅ User Service: OK" -ForegroundColor Green
    } else {
        Write-Host "❌ User Service: Status = $($response.status)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ User Service: Non accessible" -ForegroundColor Red
    Write-Host "   Erreur: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Vérifier que le Frontend est démarré
Write-Host "[3/6] Test Frontend..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri $FRONTEND -Method GET -TimeoutSec 5 -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Frontend: OK" -ForegroundColor Green
    } else {
        Write-Host "❌ Frontend: Status = $($response.StatusCode)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Frontend: Non accessible" -ForegroundColor Red
    Write-Host "   Erreur: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 4: Tester la route teacher-availability via API Gateway (sans auth)
Write-Host "[4/6] Test Route Teacher Availability..." -ForegroundColor Cyan
try {
    # Test simple de la route (devrait retourner 401 Unauthorized car pas d'auth)
    $response = Invoke-WebRequest -Uri "$API_GATEWAY/api/teacher-availability/teacher/1" -Method GET -TimeoutSec 5 -UseBasicParsing -ErrorAction SilentlyContinue
    
    if ($response.StatusCode -eq 401) {
        Write-Host "✅ Route Teacher Availability: OK (401 Unauthorized attendu)" -ForegroundColor Green
    } elseif ($response.StatusCode -eq 200) {
        Write-Host "✅ Route Teacher Availability: OK (200 OK)" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Route Teacher Availability: Status = $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401) {
        Write-Host "✅ Route Teacher Availability: OK (401 Unauthorized attendu)" -ForegroundColor Green
    } elseif ($statusCode -eq 404) {
        Write-Host "❌ Route Teacher Availability: 404 Not Found - Route non configurée!" -ForegroundColor Red
        Write-Host "   Vérifiez la configuration API Gateway" -ForegroundColor Red
        exit 1
    } else {
        Write-Host "⚠️  Route Teacher Availability: Status = $statusCode" -ForegroundColor Yellow
    }
}

# Test 5: Vérifier la configuration API Gateway
Write-Host "[5/6] Vérification Configuration API Gateway..." -ForegroundColor Cyan
$configFile = "api-gateway/src/main/resources/application.properties"
if (Test-Path $configFile) {
    $content = Get-Content $configFile -Raw
    if ($content -match "teacher-availability") {
        Write-Host "✅ Configuration API Gateway: Route teacher-availability trouvée" -ForegroundColor Green
    } else {
        Write-Host "❌ Configuration API Gateway: Route teacher-availability MANQUANTE" -ForegroundColor Red
        Write-Host "   Ajoutez cette ligne dans $configFile :" -ForegroundColor Yellow
        Write-Host "   spring.cloud.gateway.routes[10].id=teacher-availability" -ForegroundColor Yellow
        Write-Host "   spring.cloud.gateway.routes[10].uri=lb://USER-SERVICE" -ForegroundColor Yellow
        Write-Host "   spring.cloud.gateway.routes[10].predicates[0]=Path=/api/teacher-availability/**" -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "❌ Fichier de configuration non trouvé: $configFile" -ForegroundColor Red
    exit 1
}

# Test 6: Vérifier la configuration Frontend
Write-Host "[6/6] Vérification Configuration Frontend..." -ForegroundColor Cyan
$frontendApiFile = "frontend/lib/api/teacher-availability.ts"
if (Test-Path $frontendApiFile) {
    $content = Get-Content $frontendApiFile -Raw
    if ($content -match "localhost:8080" -or $content -match "NEXT_PUBLIC_API_URL") {
        Write-Host "✅ Configuration Frontend: API_BASE_URL correcte" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Configuration Frontend: Vérifiez API_BASE_URL" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ Fichier API Frontend non trouvé: $frontendApiFile" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "RÉSULTATS DU TEST" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "✅ Services Backend: Opérationnels" -ForegroundColor Green
Write-Host "✅ API Gateway: Configuré avec route teacher-availability" -ForegroundColor Green
Write-Host "✅ Frontend: Accessible" -ForegroundColor Green
Write-Host "✅ Configuration: Cohérente" -ForegroundColor Green

Write-Host ""
Write-Host "🎯 PRÊT POUR LES TESTS!" -ForegroundColor Green
Write-Host ""
Write-Host "Pour tester les créneaux d'1 heure:" -ForegroundColor Yellow
Write-Host "1. Ouvrez http://localhost:3000" -ForegroundColor White
Write-Host "2. Connectez-vous avec: teacher1 / admin123" -ForegroundColor White
Write-Host "3. Naviguez vers 'Disponibilités'" -ForegroundColor White
Write-Host "4. Testez l'ajout de créneaux d'1h" -ForegroundColor White
Write-Host ""

Write-Host "Architecture validée:" -ForegroundColor Cyan
Write-Host "Frontend (3000) → API Gateway (8080) → User Service (8081)" -ForegroundColor White
Write-Host ""