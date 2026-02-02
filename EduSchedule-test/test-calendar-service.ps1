#!/usr/bin/env pwsh

Write-Host "=== Test du Calendar Service ===" -ForegroundColor Green

$baseUrl = "http://localhost:8092"
$userId = "test-user-123"

# Fonction pour faire des requêtes HTTP
function Invoke-ApiRequest {
    param(
        [string]$Method,
        [string]$Uri,
        [object]$Body = $null
    )
    
    try {
        $headers = @{
            "Content-Type" = "application/json"
        }
        
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers -Body $jsonBody
        } else {
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers
        }
        
        return @{
            Success = $true
            Data = $response
        }
    } catch {
        return @{
            Success = $false
            Error = $_.Exception.Message
            StatusCode = $_.Exception.Response.StatusCode
        }
    }
}

# Test 1: Vérifier que le service est accessible
Write-Host "1. Test de connectivité du service..." -ForegroundColor Yellow
try {
    $healthCheck = Invoke-WebRequest -Uri "$baseUrl/actuator/health" -TimeoutSec 10 -ErrorAction SilentlyContinue
    if ($healthCheck.StatusCode -eq 200) {
        Write-Host "✓ Service accessible" -ForegroundColor Green
    } else {
        Write-Host "✗ Service non accessible" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Service non accessible: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Assurez-vous que le Calendar Service est démarré sur le port 8092" -ForegroundColor Yellow
    exit 1
}

# Test 2: Récupérer les intégrations d'un utilisateur (devrait être vide)
Write-Host "2. Test de récupération des intégrations..." -ForegroundColor Yellow
$integrationsResult = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/api/calendar/integrations/$userId"

if ($integrationsResult.Success) {
    Write-Host "✓ Récupération des intégrations réussie" -ForegroundColor Green
    Write-Host "  Nombre d'intégrations: $($integrationsResult.Data.Count)" -ForegroundColor Gray
} else {
    Write-Host "✗ Échec de récupération des intégrations: $($integrationsResult.Error)" -ForegroundColor Red
}

# Test 3: Créer une intégration de test
Write-Host "3. Test de création d'intégration..." -ForegroundColor Yellow
$testIntegration = @{
    userId = $userId
    provider = "GOOGLE"
    calendarId = "test-calendar-id"
    accessToken = "test-access-token"
    refreshToken = "test-refresh-token"
    tokenExpiresAt = (Get-Date).AddHours(1).ToString("yyyy-MM-ddTHH:mm:ss")
    enabled = $true
    syncEnabled = $true
    syncDirection = "BIDIRECTIONAL"
}

$createResult = Invoke-ApiRequest -Method "POST" -Uri "$baseUrl/api/calendar/integrations" -Body $testIntegration

if ($createResult.Success) {
    Write-Host "✓ Création d'intégration réussie" -ForegroundColor Green
    Write-Host "  ID de l'intégration: $($createResult.Data.id)" -ForegroundColor Gray
    $integrationId = $createResult.Data.id
} else {
    Write-Host "✗ Échec de création d'intégration: $($createResult.Error)" -ForegroundColor Red
    $integrationId = $null
}

# Test 4: Récupérer l'emploi du temps hebdomadaire
Write-Host "4. Test de récupération de l'emploi du temps..." -ForegroundColor Yellow
$scheduleResult = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/api/calendar/schedule/weekly/$userId/current"

if ($scheduleResult.Success) {
    Write-Host "✓ Récupération de l'emploi du temps réussie" -ForegroundColor Green
    Write-Host "  Semaine du: $($scheduleResult.Data.weekStart)" -ForegroundColor Gray
    Write-Host "  Nombre d'événements: $($scheduleResult.Data.totalEvents)" -ForegroundColor Gray
} else {
    Write-Host "✗ Échec de récupération de l'emploi du temps: $($scheduleResult.Error)" -ForegroundColor Red
}

# Test 5: Test d'export d'événement
Write-Host "5. Test d'export d'événement..." -ForegroundColor Yellow
$testEvent = @{
    userId = $userId
    title = "Réunion de test"
    description = "Événement de test pour le calendar service"
    startTime = (Get-Date).AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss")
    endTime = (Get-Date).AddHours(3).ToString("yyyy-MM-ddTHH:mm:ss")
    location = "Salle de test"
    isAllDay = $false
}

$exportResult = Invoke-ApiRequest -Method "POST" -Uri "$baseUrl/api/calendar/events/export" -Body $testEvent

if ($exportResult.Success) {
    Write-Host "✓ Export d'événement réussi" -ForegroundColor Green
} else {
    Write-Host "✗ Échec d'export d'événement: $($exportResult.Error)" -ForegroundColor Red
}

# Test 6: Test de synchronisation
Write-Host "6. Test de synchronisation..." -ForegroundColor Yellow
$syncResult = Invoke-ApiRequest -Method "POST" -Uri "$baseUrl/api/calendar/sync/$userId"

if ($syncResult.Success) {
    Write-Host "✓ Synchronisation réussie" -ForegroundColor Green
} else {
    Write-Host "✗ Échec de synchronisation: $($syncResult.Error)" -ForegroundColor Red
}

# Test 7: Test des emplois du temps par entité
Write-Host "7. Test des emplois du temps par entité..." -ForegroundColor Yellow

# Test emploi du temps enseignant
$teacherResult = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/api/calendar/schedule/teacher/teacher123/weekly?weekStart=2024-01-15"
if ($teacherResult.Success) {
    Write-Host "✓ Emploi du temps enseignant récupéré" -ForegroundColor Green
} else {
    Write-Host "✗ Échec emploi du temps enseignant: $($teacherResult.Error)" -ForegroundColor Red
}

# Test emploi du temps école
$schoolResult = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/api/calendar/schedule/school/school123/weekly?weekStart=2024-01-15"
if ($schoolResult.Success) {
    Write-Host "✓ Emploi du temps école récupéré" -ForegroundColor Green
} else {
    Write-Host "✗ Échec emploi du temps école: $($schoolResult.Error)" -ForegroundColor Red
}

# Test emploi du temps salle
$roomResult = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/api/calendar/schedule/room/room123/weekly?weekStart=2024-01-15"
if ($roomResult.Success) {
    Write-Host "✓ Emploi du temps salle récupéré" -ForegroundColor Green
} else {
    Write-Host "✗ Échec emploi du temps salle: $($roomResult.Error)" -ForegroundColor Red
}

Write-Host "" -ForegroundColor White
Write-Host "=== Résumé des tests ===" -ForegroundColor Green
Write-Host "Tests de base du Calendar Service terminés" -ForegroundColor White
Write-Host "" -ForegroundColor White
Write-Host "Pour tester les intégrations externes:" -ForegroundColor Yellow
Write-Host "1. Configurez les variables d'environnement Google/Outlook" -ForegroundColor Gray
Write-Host "2. Utilisez l'interface web pour connecter vos calendriers" -ForegroundColor Gray
Write-Host "3. Testez la synchronisation bidirectionnelle" -ForegroundColor Gray
Write-Host "" -ForegroundColor White
Write-Host "Interface web: http://localhost:3000/calendar" -ForegroundColor Cyan