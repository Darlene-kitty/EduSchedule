##############################################################
#  add-teacher-availabilities.ps1
#  Ajoute des disponibilités en masse pour plusieurs enseignants
#  via l'API /api/teacher-availability/bulk
#
#  Usage:
#    .\add-teacher-availabilities.ps1
#    .\add-teacher-availabilities.ps1 -BaseUrl "http://localhost:8080" -Username "admin" -Password "admin123"
##############################################################

param(
    [string]$BaseUrl  = "http://localhost:8080",
    [string]$Username = "admin",
    [string]$Password = "admin123"
)

# ─────────────────────────────────────────────
# Couleurs pour la lisibilité
# ─────────────────────────────────────────────
function Write-Ok    ($msg) { Write-Host "  [OK]  $msg" -ForegroundColor Green  }
function Write-Err   ($msg) { Write-Host "  [ERR] $msg" -ForegroundColor Red    }
function Write-Info  ($msg) { Write-Host "  [..] $msg"  -ForegroundColor Cyan   }
function Write-Title ($msg) {
    Write-Host ""
    Write-Host "══════════════════════════════════════════════" -ForegroundColor Yellow
    Write-Host "  $msg" -ForegroundColor Yellow
    Write-Host "══════════════════════════════════════════════" -ForegroundColor Yellow
}

# ─────────────────────────────────────────────
# Helpers HTTP (curl.exe)
# ─────────────────────────────────────────────
function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token,
        [string]$Body
    )
    $url  = "$BaseUrl$Path"
    $args = @("-s", "-w", "`n__STATUS__%{http_code}", "-X", $Method, $url)

    if ($Token) {
        $args += @("-H", "Authorization: Bearer $Token")
    }
    if ($Body) {
        $tmp = [System.IO.Path]::GetTempFileName()
        Set-Content $tmp $Body -Encoding utf8 -NoNewline
        $args += @("-H", "Content-Type: application/json", "-d", "@$tmp")
    }

    $raw    = & curl.exe @args
    $parts  = $raw -split "`n__STATUS__"
    $status = $parts[-1].Trim()
    $json   = ($parts[0..($parts.Length - 2)] -join "`n").Trim()

    if ($Body -and (Test-Path $tmp)) { Remove-Item $tmp -Force }

    return [PSCustomObject]@{ Status = $status; Body = $json }
}

# ─────────────────────────────────────────────
# Authentification
# ─────────────────────────────────────────────
Write-Title "AUTHENTIFICATION"
Write-Info "Login en tant que '$Username' sur $BaseUrl ..."

$loginBody = "{`"username`":`"$Username`",`"password`":`"$Password`"}"
$loginResp  = Invoke-Api -Method "POST" -Path "/api/auth/login" -Body $loginBody

if ($loginResp.Status -ne "200") {
    Write-Err "Login échoué (HTTP $($loginResp.Status))"
    Write-Host $loginResp.Body
    exit 1
}

$TOKEN = try { ($loginResp.Body | ConvertFrom-Json).token } catch { $null }
if (-not $TOKEN) {
    Write-Err "Token introuvable dans la réponse"
    Write-Host $loginResp.Body
    exit 1
}
Write-Ok "Authentifié — token: $($TOKEN.Substring(0, [Math]::Min(40,$TOKEN.Length)))..."

# ─────────────────────────────────────────────
# Définition des disponibilités par enseignant
#
# Structure de chaque entrée :
#   teacherId       : ID de l'enseignant (Long)
#   dayOfWeek       : MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY | SATURDAY | SUNDAY
#   startTime       : "HH:mm:ss"
#   endTime         : "HH:mm:ss"
#   availabilityType: AVAILABLE | PREFERRED | UNAVAILABLE
#   isRecurring     : true | false
#   priorityLevel   : 1 (haute) → 5 (basse)
#   notes           : texte libre
# ─────────────────────────────────────────────

# Enseignants cibles (IDs issus du DataInitializer)
# admin=1, teacher1=2, teacher2=3, teacher3=4, teacher4=5,
# teacher5=6, teacher6=7, teacher7=8, teacher8=9, teacher9=10

$availabilities = @(

    # ── Alain Mbarga (teacher1, id=2) ──────────────────────────
    @{ teacherId=2; dayOfWeek="MONDAY";    startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Lundi matin - cours magistraux" }
    @{ teacherId=2; dayOfWeek="MONDAY";    startTime="14:00:00"; endTime="17:00:00"; availabilityType="PREFERRED";   isRecurring=$true;  priorityLevel=2; notes="Lundi après-midi - TD préféré" }
    @{ teacherId=2; dayOfWeek="TUESDAY";   startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Mardi matin" }
    @{ teacherId=2; dayOfWeek="WEDNESDAY"; startTime="08:00:00"; endTime="18:00:00"; availabilityType="UNAVAILABLE"; isRecurring=$true;  priorityLevel=1; notes="Mercredi - indisponible (recherche)" }
    @{ teacherId=2; dayOfWeek="THURSDAY";  startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Jeudi matin" }
    @{ teacherId=2; dayOfWeek="FRIDAY";    startTime="08:00:00"; endTime="10:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Vendredi matin tôt" }

    # ── Pierre Essama (teacher2, id=3) ─────────────────────────
    @{ teacherId=3; dayOfWeek="MONDAY";    startTime="10:00:00"; endTime="13:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Lundi - disponible à partir de 10h" }
    @{ teacherId=3; dayOfWeek="TUESDAY";   startTime="08:00:00"; endTime="18:00:00"; availabilityType="PREFERRED";   isRecurring=$true;  priorityLevel=1; notes="Mardi - journée complète préférée" }
    @{ teacherId=3; dayOfWeek="WEDNESDAY"; startTime="14:00:00"; endTime="18:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=2; notes="Mercredi après-midi" }
    @{ teacherId=3; dayOfWeek="THURSDAY";  startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Jeudi matin" }
    @{ teacherId=3; dayOfWeek="FRIDAY";    startTime="08:00:00"; endTime="18:00:00"; availabilityType="UNAVAILABLE"; isRecurring=$true;  priorityLevel=1; notes="Vendredi - indisponible" }

    # ── Samuel Nkoa (teacher3, id=4) ───────────────────────────
    @{ teacherId=4; dayOfWeek="MONDAY";    startTime="08:00:00"; endTime="18:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Lundi - toute la journée" }
    @{ teacherId=4; dayOfWeek="TUESDAY";   startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Mardi matin" }
    @{ teacherId=4; dayOfWeek="WEDNESDAY"; startTime="08:00:00"; endTime="12:00:00"; availabilityType="PREFERRED";   isRecurring=$true;  priorityLevel=2; notes="Mercredi matin - préféré" }
    @{ teacherId=4; dayOfWeek="THURSDAY";  startTime="14:00:00"; endTime="18:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Jeudi après-midi" }
    @{ teacherId=4; dayOfWeek="FRIDAY";    startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Vendredi matin" }

    # ── Marie Ateba (teacher4, id=5) ───────────────────────────
    @{ teacherId=5; dayOfWeek="MONDAY";    startTime="08:00:00"; endTime="12:00:00"; availabilityType="PREFERRED";   isRecurring=$true;  priorityLevel=1; notes="Lundi matin - préféré" }
    @{ teacherId=5; dayOfWeek="TUESDAY";   startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Mardi matin" }
    @{ teacherId=5; dayOfWeek="TUESDAY";   startTime="14:00:00"; endTime="17:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=2; notes="Mardi après-midi" }
    @{ teacherId=5; dayOfWeek="WEDNESDAY"; startTime="08:00:00"; endTime="18:00:00"; availabilityType="UNAVAILABLE"; isRecurring=$true;  priorityLevel=1; notes="Mercredi - indisponible" }
    @{ teacherId=5; dayOfWeek="THURSDAY";  startTime="08:00:00"; endTime="18:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Jeudi - toute la journée" }
    @{ teacherId=5; dayOfWeek="FRIDAY";    startTime="10:00:00"; endTime="14:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=2; notes="Vendredi milieu de journée" }

    # ── Robert Nganou (teacher5, id=6) ─────────────────────────
    @{ teacherId=6; dayOfWeek="MONDAY";    startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Lundi matin" }
    @{ teacherId=6; dayOfWeek="MONDAY";    startTime="14:00:00"; endTime="18:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Lundi après-midi" }
    @{ teacherId=6; dayOfWeek="TUESDAY";   startTime="08:00:00"; endTime="18:00:00"; availabilityType="UNAVAILABLE"; isRecurring=$true;  priorityLevel=1; notes="Mardi - indisponible (autre établissement)" }
    @{ teacherId=6; dayOfWeek="WEDNESDAY"; startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Mercredi matin" }
    @{ teacherId=6; dayOfWeek="THURSDAY";  startTime="08:00:00"; endTime="12:00:00"; availabilityType="PREFERRED";   isRecurring=$true;  priorityLevel=1; notes="Jeudi matin - préféré" }
    @{ teacherId=6; dayOfWeek="FRIDAY";    startTime="08:00:00"; endTime="18:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Vendredi - toute la journée" }

    # ── Paul Essomba (teacher6, id=7) ──────────────────────────
    @{ teacherId=7; dayOfWeek="MONDAY";    startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Lundi matin" }
    @{ teacherId=7; dayOfWeek="TUESDAY";   startTime="14:00:00"; endTime="18:00:00"; availabilityType="PREFERRED";   isRecurring=$true;  priorityLevel=2; notes="Mardi après-midi - préféré" }
    @{ teacherId=7; dayOfWeek="WEDNESDAY"; startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Mercredi matin" }
    @{ teacherId=7; dayOfWeek="WEDNESDAY"; startTime="14:00:00"; endTime="18:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Mercredi après-midi" }
    @{ teacherId=7; dayOfWeek="THURSDAY";  startTime="08:00:00"; endTime="18:00:00"; availabilityType="UNAVAILABLE"; isRecurring=$true;  priorityLevel=1; notes="Jeudi - indisponible" }
    @{ teacherId=7; dayOfWeek="FRIDAY";    startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE";   isRecurring=$true;  priorityLevel=1; notes="Vendredi matin" }
)

# ─────────────────────────────────────────────
# Regroupement par enseignant et envoi bulk
# ─────────────────────────────────────────────
Write-Title "AJOUT DES DISPONIBILITÉS"

# Grouper par teacherId
$grouped = $availabilities | Group-Object { $_.teacherId }

$totalCreated = 0
$totalFailed  = 0

foreach ($group in $grouped) {
    $tid   = $group.Name
    $slots = $group.Group
    Write-Info "Enseignant ID=$tid — $($slots.Count) créneaux à envoyer..."

    # Construire le JSON du tableau
    $jsonItems = $slots | ForEach-Object {
        $recurring = if ($_.isRecurring) { "true" } else { "false" }
        $notes     = $_.notes -replace '"', '\"'
        "{`"teacherId`":$($_.teacherId),`"dayOfWeek`":`"$($_.dayOfWeek)`",`"startTime`":`"$($_.startTime)`",`"endTime`":`"$($_.endTime)`",`"availabilityType`":`"$($_.availabilityType)`",`"isRecurring`":$recurring,`"priorityLevel`":$($_.priorityLevel),`"notes`":`"$notes`"}"
    }
    $bulkJson = "[" + ($jsonItems -join ",") + "]"

    $resp = Invoke-Api -Method "POST" -Path "/api/teacher-availability/bulk" -Token $TOKEN -Body $bulkJson

    if ($resp.Status -eq "201" -or $resp.Status -eq "200") {
        $created = try { ($resp.Body | ConvertFrom-Json).Count } catch { "?" }
        Write-Ok "Enseignant ID=$tid — $created créneaux créés (HTTP $($resp.Status))"
        if ($created -is [int]) { $totalCreated += $created }
    } else {
        Write-Err "Enseignant ID=$tid — Échec (HTTP $($resp.Status))"
        Write-Host "    $($resp.Body)" -ForegroundColor DarkRed
        $totalFailed++
    }
}

# ─────────────────────────────────────────────
# Vérification : lire les disponibilités créées
# ─────────────────────────────────────────────
Write-Title "VÉRIFICATION"

$teacherIds = $availabilities | Select-Object -ExpandProperty teacherId -Unique | Sort-Object

foreach ($tid in $teacherIds) {
    $resp  = Invoke-Api -Method "GET" -Path "/api/teacher-availability/teacher/$tid" -Token $TOKEN
    $count = try { ($resp.Body | ConvertFrom-Json).Count } catch { "?" }
    if ($resp.Status -eq "200") {
        Write-Ok "Enseignant ID=$tid — $count disponibilité(s) en base (HTTP 200)"
    } else {
        Write-Err "Enseignant ID=$tid — Lecture échouée (HTTP $($resp.Status))"
    }
}

# ─────────────────────────────────────────────
# Résumé
# ─────────────────────────────────────────────
Write-Title "RÉSUMÉ"
Write-Host "  Enseignants traités : $($teacherIds.Count)" -ForegroundColor White
Write-Host "  Créneaux envoyés    : $($availabilities.Count)" -ForegroundColor White
if ($totalCreated -gt 0) {
    Write-Host "  Créneaux créés      : $totalCreated" -ForegroundColor Green
}
if ($totalFailed -gt 0) {
    Write-Host "  Groupes en erreur   : $totalFailed" -ForegroundColor Red
}
Write-Host ""
