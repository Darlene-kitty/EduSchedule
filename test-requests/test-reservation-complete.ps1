#!/usr/bin/env pwsh
# ============================================
# Script de Test - Reservation
# ============================================
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Continue"
$API_GATEWAY = "http://localhost:8080"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TEST COMPLET - SYSTEME DE RESERVATION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# ETAPE 1: Verifier les services
# ============================================
Write-Host "[1/6] Verification des services..." -ForegroundColor Yellow

$services = @(
    @{Name="API Gateway";        Url="$API_GATEWAY/actuator/health"},
    @{Name="User Service";       Url="http://localhost:8081/actuator/health"},
    @{Name="Resource Service";   Url="http://localhost:8086/actuator/health"},
    @{Name="Reservation Service";Url="http://localhost:8083/actuator/health"}
)

$allServicesUp = $true
foreach ($service in $services) {
    try {
        $response = Invoke-RestMethod -Uri $service.Url -Method Get -TimeoutSec 5
        if ($response.status -eq "UP") {
            Write-Host "  OK $($service.Name): UP" -ForegroundColor Green
        } else {
            Write-Host "  FAIL $($service.Name): DOWN" -ForegroundColor Red
            $allServicesUp = $false
        }
    } catch {
        Write-Host "  FAIL $($service.Name): INACCESSIBLE" -ForegroundColor Red
        $allServicesUp = $false
    }
}

if (-not $allServicesUp) {
    Write-Host ""
    Write-Host "ERREUR: Certains services ne sont pas disponibles!" -ForegroundColor Red
    Write-Host "Demarrez tous les services avant de continuer." -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# ============================================
# ETAPE 2: Authentification
# ============================================
Write-Host "[2/6] Authentification..." -ForegroundColor Yellow

$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$API_GATEWAY/api/auth/login" `
        -Method Post `
        -Body $loginBody `
        -ContentType "application/json"

    $token = $authResponse.token
    Write-Host "  OK Authentification reussie" -ForegroundColor Green
    Write-Host "  Token: $($token.Substring(0, [Math]::Min(20, $token.Length)))..." -ForegroundColor Gray
} catch {
    Write-Host "  FAIL Echec de l'authentification" -ForegroundColor Red
    Write-Host "  Erreur: $($_.Exception.Message)" -ForegroundColor Red

    # Afficher le corps de la reponse si disponible
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $body = $reader.ReadToEnd()
            $reader.Close()
            Write-Host "  Reponse: $body" -ForegroundColor DarkRed
        } catch {}
    }
    exit 1
}

Write-Host ""

# ============================================
# ETAPE 3: Recuperer les ressources disponibles
# ============================================
Write-Host "[3/6] Recuperation des ressources disponibles..." -ForegroundColor Yellow

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

$resourceId = 1  # valeur par defaut

try {
    $resourcesResponse = Invoke-RestMethod -Uri "$API_GATEWAY/api/v1/salles" `
        -Method Get `
        -Headers $headers

    if ($resourcesResponse.data -and $resourcesResponse.data.Count -gt 0) {
        Write-Host "  OK $($resourcesResponse.data.Count) ressource(s) trouvee(s)" -ForegroundColor Green

        $count = [Math]::Min(3, $resourcesResponse.data.Count)
        for ($i = 0; $i -lt $count; $i++) {
            $r = $resourcesResponse.data[$i]
            Write-Host "    - ID: $($r.id) | Nom: $($r.nom) | Capacite: $($r.capacite)" -ForegroundColor Gray
        }

        $resourceId = $resourcesResponse.data[0].id
        Write-Host "  -> Utilisation de la ressource ID: $resourceId" -ForegroundColor Cyan
    } else {
        Write-Host "  WARN Aucune ressource disponible, ID par defaut: 1" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  WARN Impossible de recuperer les ressources" -ForegroundColor Yellow
    Write-Host "  Erreur: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  -> ID par defaut: 1" -ForegroundColor Yellow
}

Write-Host ""

# ============================================
# ETAPE 4: Recuperer l'ID utilisateur
# ============================================
Write-Host "[4/6] Recuperation de l'utilisateur..." -ForegroundColor Yellow

$userId = 1  # valeur par defaut

try {
    $userResponse = Invoke-RestMethod -Uri "$API_GATEWAY/api/users/username/admin" `
        -Method Get `
        -Headers $headers

    $userId = $userResponse.id
    Write-Host "  OK Utilisateur: $($userResponse.username) (ID: $userId)" -ForegroundColor Green
} catch {
    Write-Host "  WARN Impossible de recuperer l'utilisateur, ID par defaut: 1" -ForegroundColor Yellow
    Write-Host "  Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# ============================================
# ETAPE 5: Verifier les conflits
# ============================================
Write-Host "[5/6] Verification des conflits..." -ForegroundColor Yellow

$startTime = (Get-Date).AddDays(1).Date.AddHours(10).ToString("yyyy-MM-ddTHH:mm:ss")
$endTime   = (Get-Date).AddDays(1).Date.AddHours(12).ToString("yyyy-MM-ddTHH:mm:ss")

Write-Host "  Periode: $startTime -> $endTime" -ForegroundColor Gray

try {
    $conflictUrl = "$API_GATEWAY/api/v1/reservations/conflicts/resource?resourceId=$resourceId&startTime=$startTime&endTime=$endTime"
    $conflictResponse = Invoke-RestMethod -Uri $conflictUrl -Method Get -Headers $headers

    if ($conflictResponse.hasConflicts) {
        Write-Host "  WARN $($conflictResponse.total) conflit(s) detecte(s)" -ForegroundColor Yellow
        foreach ($conflict in $conflictResponse.data) {
            Write-Host "    - Reservation #$($conflict.id): $($conflict.title)" -ForegroundColor Gray
        }
        Write-Host "  -> Decalage de la reservation a demain 14h-16h pour eviter les conflits" -ForegroundColor Cyan
        $startTime = (Get-Date).AddDays(1).Date.AddHours(14).ToString("yyyy-MM-ddTHH:mm:ss")
        $endTime   = (Get-Date).AddDays(1).Date.AddHours(16).ToString("yyyy-MM-ddTHH:mm:ss")
    } else {
        Write-Host "  OK Aucun conflit detecte" -ForegroundColor Green
    }
} catch {
    Write-Host "  WARN Impossible de verifier les conflits" -ForegroundColor Yellow
    Write-Host "  Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# ============================================
# ETAPE 6: Creer la reservation
# ============================================
Write-Host "[6/6] Creation de la reservation..." -ForegroundColor Yellow

$reservationBody = @{
    resourceId        = $resourceId
    userId            = $userId
    title             = "Test Reservation $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
    description       = "Reservation de test creee automatiquement"
    startTime         = $startTime
    endTime           = $endTime
    type              = "COURSE"
    status            = "PENDING"
    expectedAttendees = 30
    setupTime         = 10
    cleanupTime       = 10
} | ConvertTo-Json

Write-Host "  Payload envoye:" -ForegroundColor Gray
Write-Host $reservationBody -ForegroundColor DarkGray
Write-Host ""

try {
    $createResponse = Invoke-RestMethod -Uri "$API_GATEWAY/api/v1/reservations" `
        -Method Post `
        -Headers $headers `
        -Body $reservationBody

    Write-Host "  OK RESERVATION CREEE AVEC SUCCES!" -ForegroundColor Green
    Write-Host ""
    Write-Host "  Details:" -ForegroundColor Cyan
    Write-Host "  ID:          $($createResponse.data.id)"          -ForegroundColor White
    Write-Host "  Titre:       $($createResponse.data.title)"        -ForegroundColor White
    Write-Host "  Ressource:   $($createResponse.data.resourceId)"   -ForegroundColor White
    Write-Host "  Utilisateur: $($createResponse.data.userId)"       -ForegroundColor White
    Write-Host "  Statut:      $($createResponse.data.status)"       -ForegroundColor White
    Write-Host "  Type:        $($createResponse.data.type)"         -ForegroundColor White
    Write-Host "  Debut:       $($createResponse.data.startTime)"    -ForegroundColor White
    Write-Host "  Fin:         $($createResponse.data.endTime)"      -ForegroundColor White
    Write-Host "  Duree:       $($createResponse.data.formattedDuration)" -ForegroundColor White
    Write-Host ""

    $reservationId = $createResponse.data.id

    # Verification
    Write-Host "[BONUS] Verification de la reservation creee..." -ForegroundColor Yellow
    try {
        $getResponse = Invoke-RestMethod -Uri "$API_GATEWAY/api/v1/reservations/$reservationId" `
            -Method Get -Headers $headers
        Write-Host "  OK Reservation recuperee - Statut: $($getResponse.data.status)" -ForegroundColor Green
    } catch {
        Write-Host "  WARN Impossible de recuperer la reservation" -ForegroundColor Yellow
    }

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  TEST TERMINE AVEC SUCCES" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Commandes utiles:" -ForegroundColor Yellow
    Write-Host "  Approuver:" -ForegroundColor Gray
    Write-Host "  curl -X PATCH -H `"Authorization: Bearer TOKEN`" `"$API_GATEWAY/api/v1/reservations/$reservationId/approve?approvedBy=$userId`"" -ForegroundColor DarkGray
    Write-Host ""
    Write-Host "  Annuler:" -ForegroundColor Gray
    Write-Host "  curl -X PATCH -H `"Authorization: Bearer TOKEN`" `"$API_GATEWAY/api/v1/reservations/$reservationId/cancel?cancelledBy=$userId&reason=Test`"" -ForegroundColor DarkGray
    Write-Host ""

} catch {
    Write-Host "  FAIL ECHEC DE LA CREATION" -ForegroundColor Red
    Write-Host ""
    Write-Host "  Details de l'erreur:" -ForegroundColor Yellow

    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "  Code HTTP: $statusCode" -ForegroundColor Red

        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            $reader.Close()
            Write-Host "  Reponse serveur:" -ForegroundColor Red
            Write-Host $responseBody -ForegroundColor DarkRed

            # Tenter de parser le JSON pour afficher proprement
            try {
                $parsed = $responseBody | ConvertFrom-Json
                if ($parsed.message) {
                    Write-Host ""
                    Write-Host "  Message: $($parsed.message)" -ForegroundColor Red
                }
            } catch {}
        } catch {
            Write-Host "  Impossible de lire la reponse" -ForegroundColor Red
        }
    } else {
        Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    }

    Write-Host ""
    Write-Host "  Suggestions:" -ForegroundColor Yellow
    Write-Host "  1. Verifiez que tous les services sont demarres" -ForegroundColor Gray
    Write-Host "  2. Verifiez les logs: docker logs reservation-service" -ForegroundColor Gray
    Write-Host "  3. Verifiez que la base de donnees est accessible" -ForegroundColor Gray
    Write-Host "  4. Verifiez que la ressource ID $resourceId existe dans resource-service" -ForegroundColor Gray
    Write-Host "  5. Testez directement: curl -X GET http://localhost:8086/api/v1/salles" -ForegroundColor Gray
    Write-Host ""

    exit 1
}
