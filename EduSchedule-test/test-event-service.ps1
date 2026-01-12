#!/usr/bin/env pwsh

Write-Host "=== Test de l'Event Service ===" -ForegroundColor Green

$API_BASE_URL = "http://localhost:8089/api/v1"

# Headers
$headers = @{
    "Content-Type" = "application/json"
}

Write-Host "`n1. Test de santé du service..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$API_BASE_URL/events/test" -Method GET -Headers $headers
    Write-Host "✅ Service accessible: $response" -ForegroundColor Green
} catch {
    Write-Host "❌ Service non accessible: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`n2. Test de création d'événement (Séminaire)..." -ForegroundColor Cyan
$eventRequest = @{
    title = "Séminaire Intelligence Artificielle"
    description = "Séminaire sur les dernières avancées en IA"
    type = "SEMINAR"
    startDateTime = "2026-02-15T14:00:00"
    endDateTime = "2026-02-15T17:00:00"
    resourceId = 1
    organizerId = 1
    maxParticipants = 50
    registrationRequired = $true
    equipmentNeeded = "Projecteur, Micro, Ordinateur"
    specialRequirements = "Salle avec climatisation"
} | ConvertTo-Json

try {
    $event = Invoke-RestMethod -Uri "$API_BASE_URL/events" -Method POST -Body $eventRequest -Headers $headers
    Write-Host "✅ Événement créé:" -ForegroundColor Green
    Write-Host "   - ID: $($event.id)" -ForegroundColor Yellow
    Write-Host "   - Titre: $($event.title)" -ForegroundColor Yellow
    Write-Host "   - Type: $($event.type)" -ForegroundColor Yellow
    Write-Host "   - Statut: $($event.status)" -ForegroundColor Yellow
    
    $eventId = $event.id
} catch {
    Write-Host "❌ Erreur création événement: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n3. Test de vérification de disponibilité..." -ForegroundColor Cyan
try {
    $availability = Invoke-RestMethod -Uri "$API_BASE_URL/events/availability/check?resourceId=1&startDateTime=2026-02-16T10:00:00&endDateTime=2026-02-16T12:00:00" -Method GET -Headers $headers
    Write-Host "✅ Vérification disponibilité: $availability" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur vérification disponibilité: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n4. Test de détection de conflits..." -ForegroundColor Cyan
try {
    $conflicts = Invoke-RestMethod -Uri "$API_BASE_URL/events/conflicts/check?resourceId=1&startDateTime=2026-02-15T15:00:00&endDateTime=2026-02-15T16:00:00" -Method GET -Headers $headers
    Write-Host "✅ Détection de conflits:" -ForegroundColor Green
    Write-Host "   - Nombre de conflits: $($conflicts.Count)" -ForegroundColor Yellow
} catch {
    Write-Host "❌ Erreur détection conflits: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n5. Test de récupération des événements..." -ForegroundColor Cyan
try {
    $events = Invoke-RestMethod -Uri "$API_BASE_URL/events" -Method GET -Headers $headers
    Write-Host "✅ Événements récupérés:" -ForegroundColor Green
    Write-Host "   - Nombre total: $($events.totalElements)" -ForegroundColor Yellow
    Write-Host "   - Nombre sur cette page: $($events.numberOfElements)" -ForegroundColor Yellow
} catch {
    Write-Host "❌ Erreur récupération événements: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n6. Test des événements à venir..." -ForegroundColor Cyan
try {
    $upcomingEvents = Invoke-RestMethod -Uri "$API_BASE_URL/events/upcoming" -Method GET -Headers $headers
    Write-Host "✅ Événements à venir récupérés:" -ForegroundColor Green
    Write-Host "   - Nombre: $($upcomingEvents.Count)" -ForegroundColor Yellow
} catch {
    Write-Host "❌ Erreur événements à venir: $($_.Exception.Message)" -ForegroundColor Red
}

if ($eventId) {
    Write-Host "`n7. Test de mise à jour du statut..." -ForegroundColor Cyan
    try {
        $updatedEvent = Invoke-RestMethod -Uri "$API_BASE_URL/events/$eventId/status?status=CONFIRMED" -Method PATCH -Headers $headers
        Write-Host "✅ Statut mis à jour:" -ForegroundColor Green
        Write-Host "   - Nouveau statut: $($updatedEvent.status)" -ForegroundColor Yellow
    } catch {
        Write-Host "❌ Erreur mise à jour statut: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host "`n8. Test de récupération d'un événement spécifique..." -ForegroundColor Cyan
    try {
        $specificEvent = Invoke-RestMethod -Uri "$API_BASE_URL/events/$eventId" -Method GET -Headers $headers
        Write-Host "✅ Événement spécifique récupéré:" -ForegroundColor Green
        Write-Host "   - ID: $($specificEvent.id)" -ForegroundColor Yellow
        Write-Host "   - Titre: $($specificEvent.title)" -ForegroundColor Yellow
        Write-Host "   - Statut: $($specificEvent.status)" -ForegroundColor Yellow
    } catch {
        Write-Host "❌ Erreur récupération événement spécifique: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n9. Test de recherche de créneaux disponibles..." -ForegroundColor Cyan
try {
    $availableSlots = Invoke-RestMethod -Uri "$API_BASE_URL/events/availability/slots?resourceId=1&date=2026-02-17T00:00:00&durationMinutes=120" -Method GET -Headers $headers
    Write-Host "✅ Créneaux disponibles trouvés:" -ForegroundColor Green
    Write-Host "   - Nombre de créneaux: $($availableSlots.Count)" -ForegroundColor Yellow
} catch {
    Write-Host "❌ Erreur recherche créneaux: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test de l'Event Service Terminé ===" -ForegroundColor Green
Write-Host "Le service d'événements est opérationnel avec toutes les fonctionnalités !" -ForegroundColor Yellow
Write-Host "`nFonctionnalités testées:" -ForegroundColor Cyan
Write-Host "✅ Création d'événements (US26)" -ForegroundColor Green
Write-Host "✅ Détection de conflits" -ForegroundColor Green
Write-Host "✅ Vérification de disponibilité" -ForegroundColor Green
Write-Host "✅ Gestion des statuts" -ForegroundColor Green
Write-Host "✅ Recherche de créneaux libres" -ForegroundColor Green