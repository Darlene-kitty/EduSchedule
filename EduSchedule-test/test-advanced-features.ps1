# Test des fonctionnalités avancées EduSchedule
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test des Fonctionnalités Avancées" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$baseUrl = "http://localhost:8080"
$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

# Fonction pour tester un endpoint
function Test-Endpoint {
    param($url, $name, $method = "GET", $body = $null)
    try {
        Write-Host "🔍 Test: $name" -ForegroundColor Yellow
        
        $params = @{
            Uri = $url
            Method = $method
            Headers = $headers
            TimeoutSec = 10
        }
        
        if ($body) {
            $params.Body = $body | ConvertTo-Json -Depth 10
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "✅ $name : OK" -ForegroundColor Green
        
        # Afficher un aperçu de la réponse
        if ($response -is [PSCustomObject] -or $response -is [Array]) {
            $preview = ($response | ConvertTo-Json -Depth 2 -Compress).Substring(0, [Math]::Min(200, ($response | ConvertTo-Json -Depth 2 -Compress).Length))
            Write-Host "   Aperçu: $preview..." -ForegroundColor Gray
        }
        
        return $true
    }
    catch {
        Write-Host "❌ $name : ERREUR - $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

Write-Host "1. TEST SYNCHRONISATION SCHEDULE ↔ RESERVATION" -ForegroundColor Magenta
Write-Host "================================================" -ForegroundColor Magenta

# Test des endpoints de synchronisation
$syncTests = @(
    @{url="$baseUrl/api/reservations/sync-status/1"; name="Statut synchronisation"},
    @{url="$baseUrl/api/schedules/health"; name="Scheduling Service Health"}
)

$syncSuccess = 0
foreach ($test in $syncTests) {
    if (Test-Endpoint -url $test.url -name $test.name) {
        $syncSuccess++
    }
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "2. TEST ALGORITHMES D'OPTIMISATION" -ForegroundColor Magenta
Write-Host "===================================" -ForegroundColor Magenta

# Test de recherche de salle optimale
$optimizationRequest = @{
    startTime = "2024-01-15T09:00:00"
    endTime = "2024-01-15T11:00:00"
    expectedAttendees = 30
    type = "COURSE"
    requiredEquipments = @("PROJECTOR", "WHITEBOARD")
}

$optimizationTests = @(
    @{url="$baseUrl/api/reservations/optimization/find-optimal-room"; name="Recherche salle optimale"; method="POST"; body=$optimizationRequest},
    @{url="$baseUrl/api/reservations/optimization/efficiency-score/1?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59"; name="Score efficacité salle"},
    @{url="$baseUrl/api/reservations/optimization/recommendations?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59"; name="Recommandations optimisation"},
    @{url="$baseUrl/api/reservations/optimization/optimize-usage?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59"; name="Optimisation utilisation"}
)

$optimizationSuccess = 0
foreach ($test in $optimizationTests) {
    if (Test-Endpoint -url $test.url -name $test.name -method $test.method -body $test.body) {
        $optimizationSuccess++
    }
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "3. TEST TABLEAU DE BORD AVANCÉ" -ForegroundColor Magenta
Write-Host "===============================" -ForegroundColor Magenta

# Test des endpoints d'analytics
$analyticsTests = @(
    @{url="$baseUrl/api/analytics/dashboard-stats?period=week"; name="Statistiques dashboard"},
    @{url="$baseUrl/api/analytics/room-occupancy?period=week"; name="Occupation des salles"},
    @{url="$baseUrl/api/analytics/hourly-occupancy?date=2024-01-15T00:00:00"; name="Occupation horaire"},
    @{url="$baseUrl/api/analytics/weekly-data?startDate=2024-01-08T00:00:00"; name="Données hebdomadaires"},
    @{url="$baseUrl/api/analytics/room-type-distribution"; name="Répartition types salles"},
    @{url="$baseUrl/api/analytics/summary?period=week"; name="Résumé analytics"},
    @{url="$baseUrl/api/analytics/health"; name="Analytics Service Health"}
)

$analyticsSuccess = 0
foreach ($test in $analyticsTests) {
    if (Test-Endpoint -url $test.url -name $test.name) {
        $analyticsSuccess++
    }
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "4. TEST FRONTEND AVANCÉ" -ForegroundColor Magenta
Write-Host "=======================" -ForegroundColor Magenta

# Test de l'interface frontend
$frontendTests = @(
    @{url="http://localhost:3000"; name="Frontend principal"},
    @{url="http://localhost:3000/analytics"; name="Page Analytics"}
)

$frontendSuccess = 0
foreach ($test in $frontendTests) {
    if (Test-Endpoint -url $test.url -name $test.name) {
        $frontendSuccess++
    }
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RÉSULTATS DES TESTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$totalTests = $syncTests.Count + $optimizationTests.Count + $analyticsTests.Count + $frontendTests.Count
$totalSuccess = $syncSuccess + $optimizationSuccess + $analyticsSuccess + $frontendSuccess

Write-Host ""
Write-Host "📊 Synchronisation Schedule ↔ Reservation:" -ForegroundColor White
Write-Host "   $syncSuccess/$($syncTests.Count) tests réussis" -ForegroundColor $(if($syncSuccess -eq $syncTests.Count) {"Green"} else {"Yellow"})

Write-Host ""
Write-Host "🧠 Algorithmes d'optimisation:" -ForegroundColor White
Write-Host "   $optimizationSuccess/$($optimizationTests.Count) tests réussis" -ForegroundColor $(if($optimizationSuccess -eq $optimizationTests.Count) {"Green"} else {"Yellow"})

Write-Host ""
Write-Host "📈 Tableau de bord avancé:" -ForegroundColor White
Write-Host "   $analyticsSuccess/$($analyticsTests.Count) tests réussis" -ForegroundColor $(if($analyticsSuccess -eq $analyticsTests.Count) {"Green"} else {"Yellow"})

Write-Host ""
Write-Host "🖥️  Interface frontend:" -ForegroundColor White
Write-Host "   $frontendSuccess/$($frontendTests.Count) tests réussis" -ForegroundColor $(if($frontendSuccess -eq $frontendTests.Count) {"Green"} else {"Yellow"})

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
$percentage = [math]::Round(($totalSuccess / $totalTests) * 100, 1)

if ($percentage -eq 100) {
    Write-Host "🎉 TOUTES LES FONCTIONNALITÉS AVANCÉES SONT OPÉRATIONNELLES !" -ForegroundColor Green
    Write-Host "   $totalSuccess/$totalTests tests réussis ($percentage%)" -ForegroundColor Green
} elseif ($percentage -ge 80) {
    Write-Host "✅ Fonctionnalités avancées majoritairement opérationnelles" -ForegroundColor Yellow
    Write-Host "   $totalSuccess/$totalTests tests réussis ($percentage%)" -ForegroundColor Yellow
} else {
    Write-Host "⚠️  Certaines fonctionnalités avancées nécessitent des corrections" -ForegroundColor Red
    Write-Host "   $totalSuccess/$totalTests tests réussis ($percentage%)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Fonctionnalités implémentées:" -ForegroundColor Cyan
Write-Host "✅ Synchronisation automatique Schedule ↔ Reservation" -ForegroundColor Green
Write-Host "✅ Algorithmes d'optimisation pour l'assignation des salles" -ForegroundColor Green
Write-Host "✅ Tableau de bord avancé avec visualisations d'occupation" -ForegroundColor Green
Write-Host "✅ Service d'analytics complet" -ForegroundColor Green
Write-Host "✅ Interface utilisateur avancée avec graphiques" -ForegroundColor Green

Write-Host ""
Write-Host "Pour accéder aux nouvelles fonctionnalités:" -ForegroundColor Cyan
Write-Host "- Tableau de bord avancé: http://localhost:3000/analytics" -ForegroundColor White
Write-Host "- API Optimisation: $baseUrl/api/reservations/optimization/*" -ForegroundColor White
Write-Host "- API Analytics: $baseUrl/api/analytics/*" -ForegroundColor White
Write-Host "- API Synchronisation: $baseUrl/api/reservations/sync-*" -ForegroundColor White

Write-Host ""
Write-Host "Test terminé." -ForegroundColor Cyan