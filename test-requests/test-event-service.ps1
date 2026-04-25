#!/usr/bin/env pwsh
# ============================================================
# Script de test - Event Service (Creation d'evenements)
# URL directe : http://localhost:8089
# URL via gateway : http://localhost:8080
# ============================================================

$BASE_URL = "http://localhost:8089/api/v1/events"
$GATEWAY_URL = "http://localhost:8080/api/v1/events"

$GREEN  = "`e[32m"
$RED    = "`e[31m"
$YELLOW = "`e[33m"
$CYAN   = "`e[36m"
$RESET  = "`e[0m"

$passed = 0
$failed = 0

function Write-Result($label, $success, $detail = "") {
    if ($success) {
        Write-Host "${GREEN}[PASS]${RESET} $label" 
        $script:passed++
    } else {
        Write-Host "${RED}[FAIL]${RESET} $label"
        if ($detail) { Write-Host "       $detail" }
        $script:failed++
    }
}

function Invoke-Api($method, $url, $body = $null) {
    try {
        $params = @{ Method = $method; Uri = $url; ContentType = "application/json"; ErrorAction = "Stop" }
        if ($body) { $params.Body = ($body | ConvertTo-Json -Depth 5) }
        $response = Invoke-WebRequest @params
        return @{ Success = $true; Status = $response.StatusCode; Body = ($response.Content | ConvertFrom-Json) }
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        return @{ Success = $false; Status = $status; Error = $_.Exception.Message }
    }
}

# ============================================================
Write-Host ""
Write-Host "${CYAN}============================================================${RESET}"
Write-Host "${CYAN}  TEST EVENT SERVICE - Creation et gestion d'evenements${RESET}"
Write-Host "${CYAN}============================================================${RESET}"
Write-Host ""

# ------------------------------------------------------------
# TEST 1 : Health check
# ------------------------------------------------------------
Write-Host "${YELLOW}--- 1. Health Check ---${RESET}"
$r = Invoke-Api "GET" "$BASE_URL/test"
Write-Result "GET /test - service disponible" ($r.Success -and $r.Status -eq 200) $r.Error

# ------------------------------------------------------------
# TEST 2 : Creation d'un evenement valide
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 2. Creation d'un evenement valide ---${RESET}"

$newEvent = @{
    title               = "Seminaire Intelligence Artificielle"
    description         = "Seminaire sur les avancees de l'IA en education"
    type                = "SEMINAR"
    startDateTime       = "2026-05-10T09:00:00"
    endDateTime         = "2026-05-10T12:00:00"
    resourceId          = 1
    organizerId         = 1
    maxParticipants     = 100
    registrationRequired = $true
    registrationDeadline = "2026-05-08T23:59:00"
    equipmentNeeded     = "Projecteur, Microphone"
    specialRequirements = "Salle climatisee"
}

$r = Invoke-Api "POST" $BASE_URL $newEvent
Write-Result "POST / - creation evenement SEMINAR" ($r.Success -and $r.Status -eq 201) $r.Error

$createdId = $null
if ($r.Success) {
    $createdId = $r.Body.id
    Write-Host "       ID cree : $createdId"
    Write-Result "  -> id non null"          ($null -ne $createdId)
    Write-Result "  -> titre correct"        ($r.Body.title -eq $newEvent.title)
    Write-Result "  -> type = SEMINAR"       ($r.Body.type -eq "SEMINAR")
    Write-Result "  -> statut = PLANNED"     ($r.Body.status -eq "PLANNED")
    Write-Result "  -> resourceId = 1"       ($r.Body.resourceId -eq 1)
}

# ------------------------------------------------------------
# TEST 3 : Creation d'un deuxieme evenement (type different)
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 3. Creation d'une conference ---${RESET}"

$conference = @{
    title           = "Conference Annuelle des Enseignants"
    description     = "Bilan pedagogique annuel"
    type            = "CONFERENCE"
    startDateTime   = "2026-05-15T14:00:00"
    endDateTime     = "2026-05-15T17:00:00"
    resourceId      = 2
    organizerId     = 1
    maxParticipants = 200
    registrationRequired = $false
}

$r2 = Invoke-Api "POST" $BASE_URL $conference
Write-Result "POST / - creation evenement CONFERENCE" ($r2.Success -and $r2.Status -eq 201) $r2.Error
$createdId2 = if ($r2.Success) { $r2.Body.id } else { $null }

# ------------------------------------------------------------
# TEST 4 : Validation - champs obligatoires manquants
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 4. Validation - champs obligatoires ---${RESET}"

$invalid = @{ description = "Pas de titre ni de type" }
$r = Invoke-Api "POST" $BASE_URL $invalid
Write-Result "POST / - rejet si titre manquant (400)" (!$r.Success -and $r.Status -eq 400) "Status recu: $($r.Status)"

# ------------------------------------------------------------
# TEST 5 : Recuperation par ID
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 5. Recuperation par ID ---${RESET}"

if ($createdId) {
    $r = Invoke-Api "GET" "$BASE_URL/$createdId"
    Write-Result "GET /{id} - recuperation evenement cree" ($r.Success -and $r.Status -eq 200) $r.Error
    Write-Result "  -> id correspond" ($r.Body.id -eq $createdId)
}

$r = Invoke-Api "GET" "$BASE_URL/999999"
Write-Result "GET /999999 - 404 si inexistant" (!$r.Success -and $r.Status -eq 404) "Status recu: $($r.Status)"

# ------------------------------------------------------------
# TEST 6 : Liste de tous les evenements
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 6. Liste de tous les evenements ---${RESET}"

$r = Invoke-Api "GET" $BASE_URL
Write-Result "GET / - liste des evenements (200)" ($r.Success -and $r.Status -eq 200) $r.Error
if ($r.Success) {
    $count = if ($r.Body -is [array]) { $r.Body.Count } else { 1 }
    Write-Host "       Nombre d'evenements : $count"
    Write-Result "  -> liste non vide" ($count -gt 0)
}

# ------------------------------------------------------------
# TEST 7 : Evenements par organisateur
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 7. Evenements par organisateur ---${RESET}"

$r = Invoke-Api "GET" "$BASE_URL/organizer/1?page=0&size=10"
Write-Result "GET /organizer/1 - evenements de l'organisateur 1" ($r.Success -and $r.Status -eq 200) $r.Error

# ------------------------------------------------------------
# TEST 8 : Evenements a venir
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 8. Evenements a venir ---${RESET}"

$r = Invoke-Api "GET" "$BASE_URL/upcoming?page=0&size=5"
Write-Result "GET /upcoming - evenements a venir" ($r.Success -and $r.Status -eq 200) $r.Error

# ------------------------------------------------------------
# TEST 9 : Evenements du jour
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 9. Evenements du jour ---${RESET}"

$r = Invoke-Api "GET" "$BASE_URL/today"
Write-Result "GET /today - evenements du jour" ($r.Success -and $r.Status -eq 200) $r.Error

# ------------------------------------------------------------
# TEST 10 : Mise a jour d'un evenement
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 10. Mise a jour d'un evenement ---${RESET}"

if ($createdId) {
    $update = @{
        title               = "Seminaire IA - MISE A JOUR"
        description         = "Description mise a jour"
        type                = "SEMINAR"
        startDateTime       = "2026-05-10T10:00:00"
        endDateTime         = "2026-05-10T13:00:00"
        resourceId          = 1
        organizerId         = 1
        maxParticipants     = 150
        registrationRequired = $true
    }
    $r = Invoke-Api "PUT" "$BASE_URL/$createdId" $update
    Write-Result "PUT /{id} - mise a jour evenement" ($r.Success -and $r.Status -eq 200) $r.Error
    if ($r.Success) {
        Write-Result "  -> titre mis a jour" ($r.Body.title -eq "Seminaire IA - MISE A JOUR")
        Write-Result "  -> maxParticipants = 150" ($r.Body.maxParticipants -eq 150)
    }
}

# ------------------------------------------------------------
# TEST 11 : Changement de statut
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 11. Changement de statut ---${RESET}"

if ($createdId) {
    $r = Invoke-Api "PATCH" "$BASE_URL/$createdId/status?status=CONFIRMED"
    Write-Result "PATCH /{id}/status - passage a CONFIRMED" ($r.Success -and $r.Status -eq 200) $r.Error
    if ($r.Success) {
        Write-Result "  -> statut = CONFIRMED" ($r.Body.status -eq "CONFIRMED")
    }
}

# ------------------------------------------------------------
# TEST 12 : Verification de disponibilite
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 12. Verification de disponibilite ---${RESET}"

$availUrl = "$BASE_URL/availability/check?resourceId=1&startDateTime=2026-06-01T09:00:00&endDateTime=2026-06-01T11:00:00"
$r = Invoke-Api "GET" $availUrl
Write-Result "GET /availability/check - ressource disponible" ($r.Success -and $r.Status -eq 200) $r.Error

# ------------------------------------------------------------
# TEST 13 : Detection de conflit (meme ressource, meme creneau)
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 13. Detection de conflit ---${RESET}"

$conflictEvent = @{
    title           = "Evenement en conflit"
    description     = "Meme ressource, meme creneau"
    type            = "MEETING"
    startDateTime   = "2026-05-10T10:30:00"
    endDateTime     = "2026-05-10T12:30:00"
    resourceId      = 1
    organizerId     = 2
    registrationRequired = $false
}
$r = Invoke-Api "POST" $BASE_URL $conflictEvent
Write-Result "POST / - conflit detecte (400)" (!$r.Success -and $r.Status -eq 400) "Status recu: $($r.Status) (conflit attendu car ressource 1 occupee)"

# ------------------------------------------------------------
# TEST 14 : Suppression
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 14. Suppression ---${RESET}"

if ($createdId2) {
    # D'abord remettre en PLANNED pour pouvoir supprimer
    $r = Invoke-Api "DELETE" "$BASE_URL/$createdId2"
    Write-Result "DELETE /{id} - suppression evenement" ($r.Success -and $r.Status -eq 204) $r.Error
    
    # Verifier qu'il n'existe plus
    $r = Invoke-Api "GET" "$BASE_URL/$createdId2"
    Write-Result "  -> 404 apres suppression" (!$r.Success -and $r.Status -eq 404) "Status recu: $($r.Status)"
}

# ------------------------------------------------------------
# TEST 15 : Via API Gateway
# ------------------------------------------------------------
Write-Host ""
Write-Host "${YELLOW}--- 15. Test via API Gateway (port 8080) ---${RESET}"

$r = Invoke-Api "GET" $GATEWAY_URL
Write-Result "GET via gateway :8080/api/v1/events" ($r.Success -and $r.Status -eq 200) $r.Error

# ------------------------------------------------------------
# RESUME
# ------------------------------------------------------------
Write-Host ""
Write-Host "${CYAN}============================================================${RESET}"
Write-Host "${CYAN}  RESUME DES TESTS${RESET}"
Write-Host "${CYAN}============================================================${RESET}"
Write-Host "  ${GREEN}Passes  : $passed${RESET}"
Write-Host "  ${RED}Echoues : $failed${RESET}"
Write-Host "  Total   : $($passed + $failed)"
Write-Host ""

if ($failed -eq 0) {
    Write-Host "${GREEN}Tous les tests sont passes !${RESET}"
} else {
    Write-Host "${RED}$failed test(s) en echec. Verifiez que le event-service est demarre sur le port 8089.${RESET}"
}
Write-Host ""
