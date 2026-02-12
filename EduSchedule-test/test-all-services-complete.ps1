# Test complet de tous les services EduSchedule
# Vérifie que les 17 microservices + frontend sont opérationnels

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TEST COMPLET - TOUS LES SERVICES" -ForegroundColor Cyan
Write-Host "   EduSchedule - 17 Microservices" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration des services à tester
$services = @(
    @{Name="Eureka Server"; Url="http://localhost:8761"; Port=8761},
    @{Name="Config Server"; Url="http://localhost:8888/actuator/health"; Port=8888},
    @{Name="API Gateway"; Url="http://localhost:8080/actuator/health"; Port=8080},
    @{Name="User Service"; Url="http://localhost:8081/actuator/health"; Port=8081},
    @{Name="Resource Service"; Url="http://localhost:8082/actuator/health"; Port=8082},
    @{Name="School Service"; Url="http://localhost:8083/actuator/health"; Port=8083},
    @{Name="Course Service"; Url="http://localhost:8084/actuator/health"; Port=8084},
    @{Name="Scheduling Service"; Url="http://localhost:8085/actuator/health"; Port=8085},
    @{Name="Reservation Service"; Url="http://localhost:8086/actuator/health"; Port=8086},
    @{Name="Notification Service"; Url="http://localhost:8087/actuator/health"; Port=8087},
    @{Name="Reporting Service"; Url="http://localhost:8088/actuator/health"; Port=8088},
    @{Name="Calendar Service"; Url="http://localhost:8089/actuator/health"; Port=8089},
    @{Name="Room Service"; Url="http://localhost:8090/actuator/health"; Port=8090},
    @{Name="Event Service"; Url="http://localhost:8091/actuator/health"; Port=8091},
    @{Name="Teacher Availability Service"; Url="http://localhost:8092/actuator/health"; Port=8092},
    @{Name="AI Service"; Url="http://localhost:8093/actuator/health"; Port=8093},
    @{Name="Maintenance Service"; Url="http://localhost:8094/actuator/health"; Port=8094},
    @{Name="ENT Integration Service"; Url="http://localhost:8095/actuator/health"; Port=8095},
    @{Name="Frontend"; Url="http://localhost:3000"; Port=3000}
)

$infrastructure = @(
    @{Name="MySQL"; Port=3306},
    @{Name="Redis"; Port=6379},
    @{Name="RabbitMQ"; Port=5672},
    @{Name="RabbitMQ Management"; Url="http://localhost:15672"; Port=15672},
    @{Name="Zipkin"; Url="http://localhost:9411"; Port=9411}
)

# Fonction pour tester la connectivité d'un port
function Test-Port {
    param($hostname, $port)
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.ConnectAsync($hostname, $port).Wait(3000)
        $result = $tcpClient.Connected
        $tcpClient.Close()
        return $result
    }
    catch {
        return $false
    }
}

# Fonction pour tester une URL HTTP
function Test-HttpEndpoint {
    param($url)
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 10 -UseBasicParsing
        return $response.StatusCode -eq 200
    }
    catch {
        return $false
    }
}

Write-Host "🔍 VERIFICATION DE L'INFRASTRUCTURE..." -ForegroundColor Yellow
Write-Host ""

$infraOk = 0
$infraTotal = $infrastructure.Count

foreach ($infra in $infrastructure) {
    Write-Host "Testing $($infra.Name) (Port $($infra.Port))..." -NoNewline
    
    if ($infra.Url) {
        $isOk = Test-HttpEndpoint $infra.Url
    } else {
        $isOk = Test-Port "localhost" $infra.Port
    }
    
    if ($isOk) {
        Write-Host " ✅ OK" -ForegroundColor Green
        $infraOk++
    } else {
        Write-Host " ❌ FAILED" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "📊 VERIFICATION DES MICROSERVICES..." -ForegroundColor Yellow
Write-Host ""

$servicesOk = 0
$servicesTotal = $services.Count

foreach ($service in $services) {
    Write-Host "Testing $($service.Name) (Port $($service.Port))..." -NoNewline
    
    # Test du port d'abord
    $portOk = Test-Port "localhost" $service.Port
    
    if (-not $portOk) {
        Write-Host " ❌ PORT CLOSED" -ForegroundColor Red
        continue
    }
    
    # Test de l'endpoint HTTP si disponible
    if ($service.Url) {
        $httpOk = Test-HttpEndpoint $service.Url
        if ($httpOk) {
            Write-Host " ✅ OK" -ForegroundColor Green
            $servicesOk++
        } else {
            Write-Host " ⚠️  PORT OK, HTTP FAILED" -ForegroundColor Yellow
        }
    } else {
        Write-Host " ✅ PORT OK" -ForegroundColor Green
        $servicesOk++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           RESULTATS DU TEST" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "🏗️  INFRASTRUCTURE: $infraOk/$infraTotal services OK" -ForegroundColor $(if($infraOk -eq $infraTotal){"Green"}else{"Red"})
Write-Host "🔧 MICROSERVICES: $servicesOk/$servicesTotal services OK" -ForegroundColor $(if($servicesOk -eq $servicesTotal){"Green"}else{"Red"})

$totalOk = $infraOk + $servicesOk
$totalServices = $infraTotal + $servicesTotal

Write-Host ""
Write-Host "📈 TOTAL: $totalOk/$totalServices services opérationnels" -ForegroundColor $(if($totalOk -eq $totalServices){"Green"}else{"Red"})

if ($totalOk -eq $totalServices) {
    Write-Host ""
    Write-Host "🎉 TOUS LES SERVICES SONT OPERATIONNELS !" -ForegroundColor Green
    Write-Host ""
    Write-Host "Accès aux interfaces:" -ForegroundColor Cyan
    Write-Host "🌐 Frontend: http://localhost:3000" -ForegroundColor White
    Write-Host "🔗 API Gateway: http://localhost:8080" -ForegroundColor White
    Write-Host "📊 Eureka: http://localhost:8761" -ForegroundColor White
    Write-Host "🐰 RabbitMQ: http://localhost:15672" -ForegroundColor White
    Write-Host "🔍 Zipkin: http://localhost:9411" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "⚠️  CERTAINS SERVICES NE SONT PAS OPERATIONNELS" -ForegroundColor Red
    Write-Host ""
    Write-Host "Actions recommandées:" -ForegroundColor Yellow
    Write-Host "1. Vérifiez les logs: docker-compose logs -f" -ForegroundColor White
    Write-Host "2. Redémarrez les services: docker-compose restart" -ForegroundColor White
    Write-Host "3. Vérifiez l'état: docker-compose ps" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

# Test spécifique de l'API Gateway
Write-Host ""
Write-Host "🔍 TEST SPECIFIQUE API GATEWAY..." -ForegroundColor Yellow

try {
    $gatewayResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 10
    if ($gatewayResponse.StatusCode -eq 200) {
        Write-Host "✅ API Gateway répond correctement" -ForegroundColor Green
        
        # Test des routes enregistrées
        try {
            $routesResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/gateway/routes" -UseBasicParsing -TimeoutSec 10
            Write-Host "✅ Routes Gateway accessibles" -ForegroundColor Green
        }
        catch {
            Write-Host "⚠️  Routes Gateway non accessibles" -ForegroundColor Yellow
        }
    }
}
catch {
    Write-Host "❌ API Gateway non accessible" -ForegroundColor Red
}

Write-Host ""
Write-Host "Test terminé. Appuyez sur une touche pour continuer..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")