# Script de Test Complet - Tous les Services EduSchedule
# Teste: MySQL, Eureka, API Gateway, Services, SMTP, RabbitMQ, Frontend

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Complet de Tous les Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$results = @{
    MySQL = $false
    Eureka = $false
    APIGateway = $false
    UserService = $false
    NotificationService = $false
    RabbitMQ = $false
    Frontend = $false
    CORS = $false
    SMTP = $false
}

# Test 1: MySQL
Write-Host "[1/9] Test MySQL..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $tcpClient.Connect("localhost", 3306)
    $tcpClient.Close()
    Write-Host "✅ MySQL accessible sur port 3306" -ForegroundColor Green
    $results.MySQL = $true
} catch {
    Write-Host "❌ MySQL non accessible" -ForegroundColor Red
    Write-Host "   Démarrez MySQL ou vérifiez le port 3306" -ForegroundColor Gray
}
Write-Host ""

# Test 2: Eureka Server
Write-Host "[2/9] Test Eureka Server..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8761" -Method GET -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Eureka Server accessible" -ForegroundColor Green
        $results.Eureka = $true
        
        # Vérifier les services enregistrés
        try {
            $appsResponse = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps" -Method GET -UseBasicParsing -TimeoutSec 5
            $content = $appsResponse.Content
            
            if ($content -match "USER-SERVICE") {
                Write-Host "   ✅ User Service enregistré" -ForegroundColor Green
                $results.UserService = $true
            } else {
                Write-Host "   ⚠️  User Service non enregistré" -ForegroundColor Yellow
            }
            
            if ($content -match "NOTIFICATION-SERVICE") {
                Write-Host "   ✅ Notification Service enregistré" -ForegroundColor Green
                $results.NotificationService = $true
            } else {
                Write-Host "   ⚠️  Notification Service non enregistré" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "   ⚠️  Impossible de vérifier les services enregistrés" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "❌ Eureka Server non accessible" -ForegroundColor Red
    Write-Host "   Démarrez Eureka Server sur port 8761" -ForegroundColor Gray
}
Write-Host ""

# Test 3: API Gateway
Write-Host "[3/9] Test API Gateway..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ API Gateway accessible" -ForegroundColor Green
        $results.APIGateway = $true
        
        $health = $response.Content | ConvertFrom-Json
        Write-Host "   Status: $($health.status)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ API Gateway non accessible" -ForegroundColor Red
    Write-Host "   Démarrez API Gateway sur port 8080" -ForegroundColor Gray
}
Write-Host ""

# Test 4: CORS Configuration
Write-Host "[4/9] Test Configuration CORS..." -ForegroundColor Yellow
$corsTests = @(
    @{ Port = 3000; Name = "Port 3000" },
    @{ Port = 3001; Name = "Port 3001" },
    @{ Port = 3002; Name = "Port 3002" }
)

$corsSuccess = 0
foreach ($test in $corsTests) {
    try {
        $headers = @{
            "Origin" = "http://localhost:$($test.Port)"
            "Access-Control-Request-Method" = "POST"
        }
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method OPTIONS -Headers $headers -UseBasicParsing -TimeoutSec 5
        $allowOrigin = $response.Headers["Access-Control-Allow-Origin"]
        
        if ($allowOrigin) {
            Write-Host "   ✅ CORS OK pour $($test.Name)" -ForegroundColor Green
            $corsSuccess++
        }
    } catch {
        Write-Host "   ❌ CORS échoué pour $($test.Name)" -ForegroundColor Red
    }
}

if ($corsSuccess -eq $corsTests.Count) {
    Write-Host "✅ CORS configuré correctement pour tous les ports" -ForegroundColor Green
    $results.CORS = $true
} else {
    Write-Host "⚠️  CORS partiellement configuré ($corsSuccess/$($corsTests.Count))" -ForegroundColor Yellow
}
Write-Host ""

# Test 5: Endpoints d'Authentification
Write-Host "[5/9] Test Endpoints d'Authentification..." -ForegroundColor Yellow
try {
    $body = @{
        username = "test"
        password = "test"
    } | ConvertTo-Json
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $body -Headers $headers -UseBasicParsing -ErrorAction SilentlyContinue
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 401 -or $statusCode -eq 403 -or $statusCode -eq 400) {
            Write-Host "✅ Endpoint /api/auth/login disponible" -ForegroundColor Green
        } elseif ($statusCode -eq 404) {
            Write-Host "❌ Endpoint /api/auth/login non trouvé (404)" -ForegroundColor Red
        }
    }
} catch {
    Write-Host "❌ Erreur lors du test d'authentification" -ForegroundColor Red
}
Write-Host ""

# Test 6: RabbitMQ
Write-Host "[6/9] Test RabbitMQ..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $tcpClient.Connect("localhost", 5672)
    $tcpClient.Close()
    Write-Host "✅ RabbitMQ accessible sur port 5672" -ForegroundColor Green
    $results.RabbitMQ = $true
    
    # Test Management UI
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:15672" -Method GET -UseBasicParsing -TimeoutSec 5
        Write-Host "   ✅ RabbitMQ Management UI accessible: http://localhost:15672" -ForegroundColor Green
        Write-Host "   Credentials: guest/guest" -ForegroundColor Gray
    } catch {
        Write-Host "   ⚠️  RabbitMQ Management UI non accessible" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ RabbitMQ non accessible" -ForegroundColor Red
    Write-Host "   Démarrez RabbitMQ ou vérifiez le port 5672" -ForegroundColor Gray
}
Write-Host ""

# Test 7: Configuration SMTP
Write-Host "[7/9] Test Configuration SMTP..." -ForegroundColor Yellow
$smtpConfigured = $false

# Vérifier les variables d'environnement
$envFile = Get-Content ".env" -ErrorAction SilentlyContinue
if ($envFile) {
    $mailHost = ($envFile | Select-String "MAIL_HOST=").ToString().Split("=")[1]
    $mailUsername = ($envFile | Select-String "MAIL_USERNAME=").ToString().Split("=")[1]
    $mailPassword = ($envFile | Select-String "MAIL_PASSWORD=").ToString().Split("=")[1]
    
    if ($mailHost -and $mailUsername -and $mailPassword) {
        Write-Host "✅ Configuration SMTP trouvée dans .env" -ForegroundColor Green
        Write-Host "   Host: $mailHost" -ForegroundColor Gray
        Write-Host "   Username: $mailUsername" -ForegroundColor Gray
        Write-Host "   Password: ****" -ForegroundColor Gray
        $smtpConfigured = $true
        $results.SMTP = $true
    } else {
        Write-Host "⚠️  Configuration SMTP incomplète dans .env" -ForegroundColor Yellow
        Write-Host "   Vérifiez MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD" -ForegroundColor Gray
    }
} else {
    Write-Host "⚠️  Fichier .env non trouvé" -ForegroundColor Yellow
}

# Test de connexion SMTP (optionnel)
if ($smtpConfigured) {
    Write-Host "   ℹ️  Pour tester l'envoi d'email, utilisez l'endpoint:" -ForegroundColor Cyan
    Write-Host "   POST http://localhost:8080/api/v1/notifications" -ForegroundColor Gray
}
Write-Host ""

# Test 8: Frontend
Write-Host "[8/9] Test Frontend..." -ForegroundColor Yellow
$frontendPorts = @(3000, 3001)
$frontendFound = $false

foreach ($port in $frontendPorts) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$port" -Method GET -UseBasicParsing -TimeoutSec 3
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ Frontend accessible sur port $port" -ForegroundColor Green
            Write-Host "   URL: http://localhost:$port" -ForegroundColor Gray
            Write-Host "   Test: http://localhost:$port/test-connection" -ForegroundColor Gray
            $frontendFound = $true
            $results.Frontend = $true
            break
        }
    } catch {
        # Continue to next port
    }
}

if (-not $frontendFound) {
    Write-Host "⚠️  Frontend non accessible" -ForegroundColor Yellow
    Write-Host "   Démarrez avec: cd frontend && npm run dev" -ForegroundColor Gray
}
Write-Host ""

# Test 9: Test d'Intégration Complet
Write-Host "[9/9] Test d'Intégration..." -ForegroundColor Yellow
if ($results.MySQL -and $results.Eureka -and $results.APIGateway -and $results.CORS) {
    Write-Host "✅ Tous les services critiques sont opérationnels" -ForegroundColor Green
    
    # Test de création de notification (si le service est disponible)
    if ($results.NotificationService) {
        Write-Host "   ℹ️  Vous pouvez tester l'envoi de notifications" -ForegroundColor Cyan
    }
} else {
    Write-Host "⚠️  Certains services critiques ne sont pas disponibles" -ForegroundColor Yellow
}
Write-Host ""

# Résumé Final
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Résumé des Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$totalTests = $results.Count
$passedTests = ($results.Values | Where-Object { $_ -eq $true }).Count

Write-Host "Tests réussis: $passedTests/$totalTests" -ForegroundColor $(if ($passedTests -eq $totalTests) { "Green" } else { "Yellow" })
Write-Host ""

foreach ($key in $results.Keys | Sort-Object) {
    $status = if ($results[$key]) { "✅" } else { "❌" }
    $color = if ($results[$key]) { "Green" } else { "Red" }
    Write-Host "$status $key" -ForegroundColor $color
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Actions Recommandées" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if (-not $results.MySQL) {
    Write-Host "❌ MySQL:" -ForegroundColor Red
    Write-Host "   docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8.0" -ForegroundColor Gray
    Write-Host ""
}

if (-not $results.Eureka) {
    Write-Host "❌ Eureka Server:" -ForegroundColor Red
    Write-Host "   cd eureka-server && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host ""
}

if (-not $results.APIGateway) {
    Write-Host "❌ API Gateway:" -ForegroundColor Red
    Write-Host "   cd api-gateway && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host ""
}

if (-not $results.UserService) {
    Write-Host "❌ User Service:" -ForegroundColor Red
    Write-Host "   cd user-service && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host ""
}

if (-not $results.NotificationService) {
    Write-Host "⚠️  Notification Service:" -ForegroundColor Yellow
    Write-Host "   cd notification-service && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host ""
}

if (-not $results.RabbitMQ) {
    Write-Host "⚠️  RabbitMQ:" -ForegroundColor Yellow
    Write-Host "   docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management" -ForegroundColor Gray
    Write-Host ""
}

if (-not $results.SMTP) {
    Write-Host "⚠️  Configuration SMTP:" -ForegroundColor Yellow
    Write-Host "   Éditez .env et configurez:" -ForegroundColor Gray
    Write-Host "   MAIL_HOST=smtp.gmail.com" -ForegroundColor Gray
    Write-Host "   MAIL_USERNAME=votre-email@gmail.com" -ForegroundColor Gray
    Write-Host "   MAIL_PASSWORD=votre-mot-de-passe-app" -ForegroundColor Gray
    Write-Host ""
}

if (-not $results.Frontend) {
    Write-Host "⚠️  Frontend:" -ForegroundColor Yellow
    Write-Host "   cd frontend && npm install && npm run dev" -ForegroundColor Gray
    Write-Host ""
}

if ($passedTests -eq $totalTests) {
    Write-Host "🎉 Félicitations! Tous les services fonctionnent correctement!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Prochaines étapes:" -ForegroundColor White
    Write-Host "1. Accédez au frontend: http://localhost:3000" -ForegroundColor Gray
    Write-Host "2. Testez la connexion: http://localhost:3000/test-connection" -ForegroundColor Gray
    Write-Host "3. Connectez-vous avec: admin / admin123" -ForegroundColor Gray
    Write-Host "4. Testez l'envoi d'email via l'interface" -ForegroundColor Gray
} else {
    Write-Host "⚠️  Certains services nécessitent votre attention" -ForegroundColor Yellow
    Write-Host "   Suivez les actions recommandées ci-dessus" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Documentation:" -ForegroundColor White
Write-Host "- DOCUMENTATION_INDEX.md - Index complet" -ForegroundColor Gray
Write-Host "- RESUME_CORRECTIONS.md - Résumé des corrections" -ForegroundColor Gray
Write-Host "- DEMARRAGE_RAPIDE.md - Guide de démarrage" -ForegroundColor Gray
Write-Host ""
