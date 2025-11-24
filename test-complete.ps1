# Script de Test Complet - EduSchedule
# Teste tous les aspects de la connexion backend/frontend

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Complet EduSchedule" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$apiUrl = "http://localhost:8080"
$frontendUrl = "http://localhost:3000"
$frontendUrl2 = "http://localhost:3001"

$allTestsPassed = $true

# Test 1: API Gateway Health
Write-Host "[Test 1/8] API Gateway Health Check..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$apiUrl/actuator/health" -Method GET -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ API Gateway accessible" -ForegroundColor Green
    } else {
        Write-Host "❌ API Gateway non accessible (Status: $($response.StatusCode))" -ForegroundColor Red
        $allTestsPassed = $false
    }
} catch {
    Write-Host "❌ API Gateway non accessible: $($_.Exception.Message)" -ForegroundColor Red
    $allTestsPassed = $false
}
Write-Host ""

# Test 2: CORS Port 3000
Write-Host "[Test 2/8] CORS pour port 3000..." -ForegroundColor Yellow
try {
    $headers = @{
        "Origin" = "http://localhost:3000"
        "Access-Control-Request-Method" = "POST"
        "Access-Control-Request-Headers" = "Content-Type"
    }
    $response = Invoke-WebRequest -Uri "$apiUrl/api/auth/login" -Method OPTIONS -Headers $headers -UseBasicParsing
    $allowOrigin = $response.Headers["Access-Control-Allow-Origin"]
    if ($allowOrigin) {
        Write-Host "✅ CORS configuré pour port 3000" -ForegroundColor Green
        Write-Host "   Allow-Origin: $allowOrigin" -ForegroundColor Gray
    } else {
        Write-Host "❌ CORS non configuré pour port 3000" -ForegroundColor Red
        $allTestsPassed = $false
    }
} catch {
    Write-Host "❌ Erreur CORS port 3000: $($_.Exception.Message)" -ForegroundColor Red
    $allTestsPassed = $false
}
Write-Host ""

# Test 3: CORS Port 3001
Write-Host "[Test 3/8] CORS pour port 3001..." -ForegroundColor Yellow
try {
    $headers = @{
        "Origin" = "http://localhost:3001"
        "Access-Control-Request-Method" = "POST"
        "Access-Control-Request-Headers" = "Content-Type"
    }
    $response = Invoke-WebRequest -Uri "$apiUrl/api/auth/login" -Method OPTIONS -Headers $headers -UseBasicParsing
    $allowOrigin = $response.Headers["Access-Control-Allow-Origin"]
    if ($allowOrigin) {
        Write-Host "✅ CORS configuré pour port 3001" -ForegroundColor Green
        Write-Host "   Allow-Origin: $allowOrigin" -ForegroundColor Gray
    } else {
        Write-Host "❌ CORS non configuré pour port 3001" -ForegroundColor Red
        $allTestsPassed = $false
    }
} catch {
    Write-Host "❌ Erreur CORS port 3001: $($_.Exception.Message)" -ForegroundColor Red
    $allTestsPassed = $false
}
Write-Host ""

# Test 4: Login Endpoint Structure
Write-Host "[Test 4/8] Structure Login Endpoint..." -ForegroundColor Yellow
try {
    $body = @{
        username = "test"
        password = "test"
    } | ConvertTo-Json
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    $response = Invoke-WebRequest -Uri "$apiUrl/api/auth/login" -Method POST -Body $body -Headers $headers -UseBasicParsing -ErrorAction SilentlyContinue
    
    # On s'attend à une erreur 401 ou 403, pas 404
    if ($response.StatusCode -eq 401 -or $response.StatusCode -eq 403) {
        Write-Host "✅ Endpoint login disponible (401/403 attendu)" -ForegroundColor Green
    } elseif ($response.StatusCode -eq 404) {
        Write-Host "❌ Endpoint login non trouvé (404)" -ForegroundColor Red
        $allTestsPassed = $false
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403 -or $statusCode -eq 400) {
        Write-Host "✅ Endpoint login disponible (erreur $statusCode attendue)" -ForegroundColor Green
    } elseif ($statusCode -eq 404) {
        Write-Host "❌ Endpoint login non trouvé (404)" -ForegroundColor Red
        $allTestsPassed = $false
    } else {
        Write-Host "⚠️  Réponse inattendue: $statusCode" -ForegroundColor Yellow
    }
}
Write-Host ""

# Test 5: Register Endpoint Structure
Write-Host "[Test 5/8] Structure Register Endpoint..." -ForegroundColor Yellow
try {
    $body = @{
        username = "test.user"
        email = "test@example.com"
        password = "password123"
        role = "TEACHER"
    } | ConvertTo-Json
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    $response = Invoke-WebRequest -Uri "$apiUrl/api/auth/register" -Method POST -Body $body -Headers $headers -UseBasicParsing -ErrorAction SilentlyContinue
    
    if ($response.StatusCode -eq 201 -or $response.StatusCode -eq 200) {
        Write-Host "✅ Endpoint register disponible" -ForegroundColor Green
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 400 -or $statusCode -eq 409) {
        Write-Host "✅ Endpoint register disponible (erreur $statusCode attendue)" -ForegroundColor Green
    } elseif ($statusCode -eq 404) {
        Write-Host "❌ Endpoint register non trouvé (404)" -ForegroundColor Red
        $allTestsPassed = $false
    } else {
        Write-Host "⚠️  Réponse inattendue: $statusCode" -ForegroundColor Yellow
    }
}
Write-Host ""

# Test 6: Eureka Server
Write-Host "[Test 6/8] Eureka Server..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8761" -Method GET -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Eureka Server accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  Eureka Server non accessible (optionnel)" -ForegroundColor Yellow
}
Write-Host ""

# Test 7: Frontend Port 3000
Write-Host "[Test 7/8] Frontend sur port 3000..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $frontendUrl -Method GET -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Frontend accessible sur port 3000" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  Frontend non accessible sur port 3000" -ForegroundColor Yellow
    Write-Host "   Démarrez avec: cd frontend && npm run dev" -ForegroundColor Gray
}
Write-Host ""

# Test 8: MySQL
Write-Host "[Test 8/8] MySQL..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $tcpClient.Connect("localhost", 3306)
    $tcpClient.Close()
    Write-Host "✅ MySQL accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ MySQL non accessible" -ForegroundColor Red
    $allTestsPassed = $false
}
Write-Host ""

# Résumé
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Résumé des Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($allTestsPassed) {
    Write-Host "✅ Tous les tests critiques sont passés!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Vous pouvez maintenant:" -ForegroundColor White
    Write-Host "1. Accéder à http://localhost:3000" -ForegroundColor Gray
    Write-Host "2. Tester la connexion: http://localhost:3000/test-connection" -ForegroundColor Gray
    Write-Host "3. Se connecter avec admin/admin123" -ForegroundColor Gray
} else {
    Write-Host "❌ Certains tests ont échoué" -ForegroundColor Red
    Write-Host ""
    Write-Host "Vérifiez:" -ForegroundColor White
    Write-Host "1. MySQL est démarré" -ForegroundColor Gray
    Write-Host "2. API Gateway est démarré (port 8080)" -ForegroundColor Gray
    Write-Host "3. User Service est démarré et enregistré dans Eureka" -ForegroundColor Gray
    Write-Host "4. Configuration CORS dans SecurityConfig.java" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Pour plus de détails, consultez:" -ForegroundColor White
Write-Host "- RESUME_CORRECTIONS.md" -ForegroundColor Gray
Write-Host "- INCOHERENCES_RESOLUES.md" -ForegroundColor Gray
Write-Host "- GUIDE_MIGRATION.md" -ForegroundColor Gray
Write-Host ""
