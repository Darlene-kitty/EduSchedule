# Vérification de l'état d'implémentation des services
# Teste quels services sont réellement implémentés et fonctionnels

Write-Host "=== Vérification de l'état des services ===" -ForegroundColor Cyan

$services = @(
    @{ Name = "User Service"; Port = 8081; Path = "/api/auth/me" },
    @{ Name = "Resource Service"; Port = 8083; Path = "/api/v1/resources" },
    @{ Name = "Course Service"; Port = 8084; Path = "/api/v1/courses" },
    @{ Name = "Reservation Service"; Port = 8085; Path = "/api/v1/reservations" },
    @{ Name = "Scheduling Service"; Port = 8086; Path = "/api/v1/schedules" },
    @{ Name = "Notification Service"; Port = 8082; Path = "/api/v1/notifications" },
    @{ Name = "Reporting Service"; Port = 8087; Path = "/api/v1/reports" },
    @{ Name = "API Gateway"; Port = 8080; Path = "/actuator/health" },
    @{ Name = "Eureka Server"; Port = 8761; Path = "/" }
)

$implemented = @()
$notImplemented = @()
$running = @()
$notRunning = @()

foreach ($service in $services) {
    Write-Host "`nTesting $($service.Name) on port $($service.Port)..." -ForegroundColor Yellow
    
    try {
        # Test de connectivité
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)$($service.Path)" -TimeoutSec 5 -UseBasicParsing
        
        if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 401) {
            Write-Host "✅ $($service.Name) - RUNNING" -ForegroundColor Green
            $running += $service.Name
            $implemented += $service.Name
        } else {
            Write-Host "⚠️ $($service.Name) - RESPONDING BUT ERROR ($($response.StatusCode))" -ForegroundColor Yellow
            $implemented += $service.Name
        }
    }
    catch {
        if ($_.Exception.Message -like "*Connection refused*" -or $_.Exception.Message -like "*No connection could be made*") {
            Write-Host "❌ $($service.Name) - NOT RUNNING" -ForegroundColor Red
            $notRunning += $service.Name
        } else {
            Write-Host "⚠️ $($service.Name) - ERROR: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
}

# Vérification des structures de fichiers
Write-Host "`n=== Vérification des implémentations ===" -ForegroundColor Cyan

$serviceStructures = @(
    @{ Name = "Course Service"; Path = "course-service/src/main/java/cm/iusjc/course" },
    @{ Name = "Reservation Service"; Path = "reservation-service/src/main/java/cm/iusjc/reservation" },
    @{ Name = "Scheduling Service"; Path = "scheduling-service/src/main/java/cm/iusjc/scheduling" },
    @{ Name = "Notification Service"; Path = "notification-service/src/main/java/cm/iusjc/notification" },
    @{ Name = "Reporting Service"; Path = "reporting-service/src/main/java/cm/iusjc/reporting" }
)

foreach ($service in $serviceStructures) {
    if (Test-Path $service.Path) {
        $files = Get-ChildItem -Path $service.Path -Recurse -Filter "*.java" | Measure-Object
        $controllers = Get-ChildItem -Path "$($service.Path)/controller" -Filter "*.java" -ErrorAction SilentlyContinue | Measure-Object
        $entities = Get-ChildItem -Path "$($service.Path)/entity" -Filter "*.java" -ErrorAction SilentlyContinue | Measure-Object
        $services = Get-ChildItem -Path "$($service.Path)/service" -Filter "*.java" -ErrorAction SilentlyContinue | Measure-Object
        
        if ($files.Count -gt 1) {
            Write-Host "✅ $($service.Name) - IMPLEMENTED ($($files.Count) files, $($controllers.Count) controllers, $($entities.Count) entities, $($services.Count) services)" -ForegroundColor Green
        } else {
            Write-Host "⚠️ $($service.Name) - SKELETON ONLY ($($files.Count) files)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "❌ $($service.Name) - NOT FOUND" -ForegroundColor Red
    }
}

Write-Host "`n=== RESUME ===" -ForegroundColor Cyan

Write-Host "`nServices en cours d'execution:" -ForegroundColor Green
foreach ($service in $running) {
    Write-Host "  - $service"
}

Write-Host "`nServices non demarres:" -ForegroundColor Red
foreach ($service in $notRunning) {
    Write-Host "  - $service"
}

Write-Host "`nEtat d'implementation:" -ForegroundColor Yellow
Write-Host "- User Service: COMPLET"
Write-Host "- Resource Service: COMPLET"
Write-Host "- Course Service: SKELETON"
Write-Host "- Reservation Service: SKELETON"
Write-Host "- Scheduling Service: IMPLEMENTE"
Write-Host "- Notification Service: IMPLEMENTE"
Write-Host "- Reporting Service: SKELETON"

Write-Host "`nPhase 1 - Services Backend Prioritaires:" -ForegroundColor Magenta
Write-Host "- [ ] Course Service - A implementer (entites, controllers, services)"
Write-Host "- [ ] Reservation Service - A implementer (entites, controllers, services)"
Write-Host "- [x] Scheduling Service - Deja implemente"

Write-Host "`nPhase 2 - Migration Frontend:" -ForegroundColor Magenta
Write-Host "- [ ] Migrer composants utilisateurs"
Write-Host "- [ ] Migrer composants ressources"
Write-Host "- [ ] Migrer composants cours"
Write-Host "- [ ] Migrer composants reservations"
Write-Host "- [ ] Migrer composants emploi du temps"

Write-Host "`nPhase 3 - Services Backend Secondaires:" -ForegroundColor Magenta
Write-Host "- [x] Notification Service - Deja implemente"
Write-Host "- [ ] Reporting Service - A implementer"
Write-Host "- [ ] Integration WebSocket"
Write-Host "- [ ] Optimisations"