# Script de monitoring en temps reel des services Docker
param(
    [int]$RefreshInterval = 5
)

function Get-ServiceStatus {
    $services = @(
        @{Name="MySQL"; Port=3306},
        @{Name="Redis"; Port=6379},
        @{Name="RabbitMQ"; Port=5672},
        @{Name="Eureka"; Port=8761},
        @{Name="API Gateway"; Port=8080},
        @{Name="User Service"; Port=8081},
        @{Name="Resource Service"; Port=8082},
        @{Name="School Service"; Port=8083},
        @{Name="Course Service"; Port=8084},
        @{Name="Scheduling Service"; Port=8085},
        @{Name="Reservation Service"; Port=8086},
        @{Name="Notification Service"; Port=8087},
        @{Name="Reporting Service"; Port=8088},
        @{Name="Calendar Service"; Port=8089},
        @{Name="Room Service"; Port=8090},
        @{Name="Event Service"; Port=8091},
        @{Name="Teacher Availability"; Port=8092},
        @{Name="AI Service"; Port=8093},
        @{Name="Maintenance Service"; Port=8094},
        @{Name="ENT Integration"; Port=8095},
        @{Name="Frontend"; Port=3000}
    )
    
    $results = @()
    foreach ($service in $services) {
        $status = "DOWN"
        $color = "Red"
        
        try {
            $tcpClient = New-Object System.Net.Sockets.TcpClient
            $tcpClient.Connect("localhost", $service.Port)
            $tcpClient.Close()
            $status = "UP"
            $color = "Green"
        }
        catch {
            $status = "DOWN"
            $color = "Red"
        }
        
        $results += [PSCustomObject]@{
            Service = $service.Name
            Port = $service.Port
            Status = $status
            Color = $color
        }
    }
    
    return $results
}

function Show-Dashboard {
    Clear-Host
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "   EduSchedule - Monitoring Services   " -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Derniere mise a jour: $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor Gray
    Write-Host ""
    
    $statuses = Get-ServiceStatus
    
    # Compter les services UP et DOWN
    $upCount = ($statuses | Where-Object { $_.Status -eq "UP" }).Count
    $downCount = ($statuses | Where-Object { $_.Status -eq "DOWN" }).Count
    $totalCount = $statuses.Count
    
    Write-Host "Statut Global: $upCount/$totalCount services operationnels" -ForegroundColor $(if ($upCount -eq $totalCount) { "Green" } else { "Yellow" })
    Write-Host ""
    
    # Afficher les services par catégorie
    Write-Host "INFRASTRUCTURE:" -ForegroundColor Cyan
    $statuses | Where-Object { $_.Service -in @("MySQL", "Redis", "RabbitMQ", "Eureka") } | ForEach-Object {
        $statusText = "[$($_.Status)]".PadRight(8)
        Write-Host "  $statusText $($_.Service.PadRight(25)) Port: $($_.Port)" -ForegroundColor $_.Color
    }
    Write-Host ""
    
    Write-Host "CORE SERVICES:" -ForegroundColor Cyan
    $statuses | Where-Object { $_.Service -in @("API Gateway") } | ForEach-Object {
        $statusText = "[$($_.Status)]".PadRight(8)
        Write-Host "  $statusText $($_.Service.PadRight(25)) Port: $($_.Port)" -ForegroundColor $_.Color
    }
    Write-Host ""
    
    Write-Host "MICROSERVICES:" -ForegroundColor Cyan
    $statuses | Where-Object { $_.Service -notin @("MySQL", "Redis", "RabbitMQ", "Eureka", "API Gateway", "Frontend") } | ForEach-Object {
        $statusText = "[$($_.Status)]".PadRight(8)
        Write-Host "  $statusText $($_.Service.PadRight(25)) Port: $($_.Port)" -ForegroundColor $_.Color
    }
    Write-Host ""
    
    Write-Host "FRONTEND:" -ForegroundColor Cyan
    $statuses | Where-Object { $_.Service -eq "Frontend" } | ForEach-Object {
        $statusText = "[$($_.Status)]".PadRight(8)
        Write-Host "  $statusText $($_.Service.PadRight(25)) Port: $($_.Port)" -ForegroundColor $_.Color
    }
    Write-Host ""
    
    # Afficher les ressources Docker
    Write-Host "RESSOURCES DOCKER:" -ForegroundColor Cyan
    try {
        $containers = docker ps --format "{{.Names}}" | Measure-Object
        Write-Host "  Conteneurs actifs: $($containers.Count)" -ForegroundColor White
        
        $stats = docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" | Select-Object -Skip 1 -First 5
        if ($stats) {
            Write-Host "  Top 5 consommation:" -ForegroundColor Gray
            $stats | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
        }
    }
    catch {
        Write-Host "  Impossible de recuperer les stats Docker" -ForegroundColor Yellow
    }
    Write-Host ""
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Appuyez sur Ctrl+C pour quitter" -ForegroundColor Gray
    Write-Host "Rafraichissement dans $RefreshInterval secondes..." -ForegroundColor Gray
}

# Boucle principale
Write-Host "Demarrage du monitoring..." -ForegroundColor Green
Write-Host "Rafraichissement toutes les $RefreshInterval secondes" -ForegroundColor Gray
Write-Host ""

try {
    while ($true) {
        Show-Dashboard
        Start-Sleep -Seconds $RefreshInterval
    }
}
catch {
    Write-Host ""
    Write-Host "Monitoring arrete." -ForegroundColor Yellow
}
