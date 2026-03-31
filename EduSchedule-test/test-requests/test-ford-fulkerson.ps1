#!/usr/bin/env pwsh
# ============================================================
#  TEST FORD-FULKERSON — Emploi du temps automatique
#  1. Ajoute des disponibilités à 2 enseignants
#  2. Lance la génération (Edmonds-Karp BFS)
#  3. Poll le résultat et affiche l'emploi du temps
# ============================================================

$GATEWAY  = "http://localhost:8080"
$USER_SVC = "http://localhost:8081"   # user-service direct (teacher-availability)
$TMP      = "$env:TEMP\ff_test"
New-Item -ItemType Directory -Force -Path $TMP | Out-Null

# ── Helpers ─────────────────────────────────────────────────
function WriteJson($name, $obj) {
    $path = "$TMP\$name"
    $obj | ConvertTo-Json -Depth 5 | Set-Content $path -Encoding utf8 -NoNewline
    return $path
}

function Req($method, $base, $url, $token, $body) {
    $headers = @{ "Content-Type" = "application/json" }
    if ($token) { $headers["Authorization"] = "Bearer $token" }
    try {
        if ($body) {
            $resp = Invoke-RestMethod -Method $method -Uri "$base$url" -Headers $headers -Body ($body | ConvertTo-Json -Depth 5) -ErrorAction Stop
        } else {
            $resp = Invoke-RestMethod -Method $method -Uri "$base$url" -Headers $headers -ErrorAction Stop
        }
        return @{ ok = $true; data = $resp }
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        $msg    = $_.ErrorDetails.Message
        return @{ ok = $false; status = $status; error = $msg }
    }
}

function Banner($text) {
    Write-Host ""
    Write-Host ("=" * 55) -ForegroundColor Cyan
    Write-Host "  $text" -ForegroundColor Cyan
    Write-Host ("=" * 55) -ForegroundColor Cyan
}

function OK($label, $r) {
    if ($r.ok) {
        Write-Host "  [OK] $label" -ForegroundColor Green
        return $r.data
    } else {
        Write-Host "  [FAIL] $label — HTTP $($r.status)" -ForegroundColor Red
        Write-Host "         $($r.error)" -ForegroundColor DarkRed
        return $null
    }
}

# ============================================================
Banner "ÉTAPE 1 — Authentification"
# ============================================================
$loginBody = @{ username = "admin"; password = "admin123" }
$login = Req "POST" $GATEWAY "/api/auth/login" $null $loginBody
$TOKEN = OK "Login admin" $login
if (-not $TOKEN) { exit 1 }
$TOKEN = $TOKEN.token
Write-Host "  Token: $($TOKEN.Substring(0, [Math]::Min(50,$TOKEN.Length)))..." -ForegroundColor DarkGray

# ============================================================
Banner "ÉTAPE 2 — Vérification des enseignants"
# ============================================================
# DataInitializer cours L1/S1/schoolId=1 :
#   INFO  → teacherId=1  (admin, utilisé comme enseignant dans le seed)
#   MATH  → teacherId=6  (teacher5 = Robert Nganou)
#   PHYS  → teacherId=7  (teacher6 = Paul Essomba)
#   LANG  → teacherId=9  (teacher8 = Sylvie Biya)
#   CHIM  → teacherId=3  (teacher2 = Pierre Essama)
#
# On ajoute des disponibilités pour teacherId=6 (MATH) et teacherId=3 (CHIM)

$T1 = 6   # Enseignant MATH (Robert Nganou)
$T2 = 3   # Enseignant CHIM (Pierre Essama)

Write-Host "  Enseignant 1 : ID=$T1 (cours MATH, L1/S1 — Robert Nganou)" -ForegroundColor Yellow
Write-Host "  Enseignant 2 : ID=$T2 (cours CHIM, L1/S1 — Pierre Essama)" -ForegroundColor Yellow

# Nettoyer les disponibilités existantes pour ces enseignants
$existing1 = Req "GET" $USER_SVC "/api/teacher-availability/teacher/$T1" $TOKEN $null
if ($existing1.ok -and $existing1.data) {
    foreach ($a in $existing1.data) {
        Req "DELETE" $USER_SVC "/api/teacher-availability/$($a.id)" $TOKEN $null | Out-Null
    }
    Write-Host "  Disponibilités existantes supprimées pour T1" -ForegroundColor DarkGray
}
$existing2 = Req "GET" $USER_SVC "/api/teacher-availability/teacher/$T2" $TOKEN $null
if ($existing2.ok -and $existing2.data) {
    foreach ($a in $existing2.data) {
        Req "DELETE" $USER_SVC "/api/teacher-availability/$($a.id)" $TOKEN $null | Out-Null
    }
    Write-Host "  Disponibilités existantes supprimées pour T2" -ForegroundColor DarkGray
}

# ============================================================
Banner "ÉTAPE 3 — Ajout des disponibilités"
# ============================================================

# ── Enseignant 1 (INFO) ──────────────────────────────────────
Write-Host ""
Write-Host "  Enseignant $T1 (INFO) :" -ForegroundColor Yellow

$avail1 = @(
    @{ teacherId=$T1; schoolId=1; dayOfWeek="MONDAY";    startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Lundi matin" },
    @{ teacherId=$T1; schoolId=1; dayOfWeek="MONDAY";    startTime="14:00:00"; endTime="18:00:00"; availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Lundi après-midi" },
    @{ teacherId=$T1; schoolId=1; dayOfWeek="TUESDAY";   startTime="08:00:00"; endTime="12:00:00"; availabilityType="PREFERRED"; recurring=$true; priority=2; notes="Mardi matin (préféré)" },
    @{ teacherId=$T1; schoolId=1; dayOfWeek="THURSDAY";  startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Jeudi matin" },
    @{ teacherId=$T1; schoolId=1; dayOfWeek="FRIDAY";    startTime="14:00:00"; endTime="18:00:00"; availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Vendredi après-midi" }
)

$created1 = @()
foreach ($a in $avail1) {
    $r = Req "POST" $USER_SVC "/api/teacher-availability" $TOKEN $a
    $d = OK "  $($a.dayOfWeek) $($a.startTime)-$($a.endTime) [$($a.availabilityType)]" $r
    if ($d) { $created1 += $d }
}

# ── Enseignant 2 (CHIM) ──────────────────────────────────────
Write-Host ""
Write-Host "  Enseignant $T2 (CHIM) :" -ForegroundColor Yellow

$avail2 = @(
    @{ teacherId=$T2; schoolId=1; dayOfWeek="TUESDAY";   startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Mardi matin" },
    @{ teacherId=$T2; schoolId=1; dayOfWeek="WEDNESDAY";  startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Mercredi matin" },
    @{ teacherId=$T2; schoolId=1; dayOfWeek="WEDNESDAY";  startTime="14:00:00"; endTime="18:00:00"; availabilityType="PREFERRED"; recurring=$true; priority=2; notes="Mercredi après-midi (préféré)" },
    @{ teacherId=$T2; schoolId=1; dayOfWeek="THURSDAY";   startTime="14:00:00"; endTime="18:00:00"; availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Jeudi après-midi" },
    @{ teacherId=$T2; schoolId=1; dayOfWeek="FRIDAY";     startTime="08:00:00"; endTime="12:00:00"; availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Vendredi matin" }
)

$created2 = @()
foreach ($a in $avail2) {
    $r = Req "POST" $USER_SVC "/api/teacher-availability" $TOKEN $a
    $d = OK "  $($a.dayOfWeek) $($a.startTime)-$($a.endTime) [$($a.availabilityType)]" $r
    if ($d) { $created2 += $d }
}

Write-Host ""
Write-Host "  Résumé : $($created1.Count) créneaux pour T$T1, $($created2.Count) créneaux pour T$T2" -ForegroundColor Green

# ============================================================
Banner "ÉTAPE 4 — Vérification des disponibilités"
# ============================================================
$check1 = Req "GET" $USER_SVC "/api/teacher-availability/teacher/$T1" $TOKEN $null
$check2 = Req "GET" $USER_SVC "/api/teacher-availability/teacher/$T2" $TOKEN $null

if ($check1.ok) { Write-Host "  T$T1 : $($check1.data.Count) disponibilité(s) enregistrée(s)" -ForegroundColor Green }
if ($check2.ok) { Write-Host "  T$T2 : $($check2.data.Count) disponibilité(s) enregistrée(s)" -ForegroundColor Green }

# ============================================================
Banner "ÉTAPE 5 — Génération de l'emploi du temps (Edmonds-Karp)"
# ============================================================

# Créneaux disponibles couvrant les plages des 2 enseignants
$slots = @(
    "LUNDI_08:00_10:00",    "LUNDI_10:00_12:00",
    "LUNDI_14:00_16:00",    "LUNDI_16:00_18:00",
    "MARDI_08:00_10:00",    "MARDI_10:00_12:00",
    "MARDI_14:00_16:00",    "MARDI_16:00_18:00",
    "MERCREDI_08:00_10:00", "MERCREDI_10:00_12:00",
    "MERCREDI_14:00_16:00", "MERCREDI_16:00_18:00",
    "JEUDI_08:00_10:00",    "JEUDI_10:00_12:00",
    "JEUDI_14:00_16:00",    "JEUDI_16:00_18:00",
    "VENDREDI_08:00_10:00", "VENDREDI_10:00_12:00",
    "VENDREDI_14:00_16:00", "VENDREDI_16:00_18:00"
)

$genRequest = @{
    schoolId       = 1
    semester       = "S1"
    level          = "L1"
    availableSlots = $slots
    maxHoursPerDay = 6
    algorithm      = "edmonds-karp"
}

Write-Host "  Paramètres :" -ForegroundColor Yellow
Write-Host "    schoolId=$($genRequest.schoolId), level=$($genRequest.level), semester=$($genRequest.semester)"
Write-Host "    algorithm=$($genRequest.algorithm)"
Write-Host "    $($slots.Count) créneaux disponibles"
Write-Host ""

$gen = Req "POST" $GATEWAY "/api/v1/timetable/generate" $TOKEN $genRequest
$genData = OK "Lancement génération" $gen
if (-not $genData) { exit 1 }

$jobId = $genData.jobId
Write-Host "  jobId = $jobId" -ForegroundColor Cyan

# ============================================================
Banner "ÉTAPE 6 — Polling du résultat"
# ============================================================

$maxAttempts = 30
$attempt     = 0
$result      = $null

do {
    Start-Sleep -Seconds 2
    $attempt++
    $poll = Req "GET" $GATEWAY "/api/v1/timetable/status/$jobId" $TOKEN $null
    if ($poll.ok) {
        $jobData = $poll.data.data
        $status  = $jobData.status
        $progress = $jobData.progress
        Write-Host "  [Tentative $attempt/$maxAttempts] status=$status progress=$progress%" -ForegroundColor DarkGray
        if ($status -in @("COMPLETED", "PARTIAL", "FAILED")) {
            $result = $jobData
            break
        }
    } else {
        Write-Host "  [Tentative $attempt] Erreur polling" -ForegroundColor Red
    }
} while ($attempt -lt $maxAttempts)

if (-not $result) {
    Write-Host "  TIMEOUT — le job n'a pas terminé en temps voulu" -ForegroundColor Red
    exit 1
}

# ============================================================
Banner "ÉTAPE 7 — Résultats"
# ============================================================

$statusColor = if ($result.status -eq "COMPLETED") { "Green" } elseif ($result.status -eq "PARTIAL") { "Yellow" } else { "Red" }
Write-Host ""
Write-Host "  Statut         : $($result.status)" -ForegroundColor $statusColor
Write-Host "  Message        : $($result.message)"
Write-Host "  Flot max       : $($result.maxFlowValue) / $($result.totalDemand) heures"
Write-Host "  Temps          : $($result.generationTimeMs) ms"
Write-Host "  Créneaux placés: $($result.slots.Count)"

if ($result.unassignedCourses -and $result.unassignedCourses.Count -gt 0) {
    Write-Host ""
    Write-Host "  Cours non entièrement placés :" -ForegroundColor Yellow
    foreach ($u in $result.unassignedCourses) {
        Write-Host "    - $u" -ForegroundColor Yellow
    }
}

# ============================================================
Banner "ÉTAPE 8 — Emploi du temps généré"
# ============================================================

if ($result.slots -and $result.slots.Count -gt 0) {
    # Trier par jour puis heure
    $dayOrder = @{ LUNDI=1; MARDI=2; MERCREDI=3; JEUDI=4; VENDREDI=5; SAMEDI=6 }
    $sorted = $result.slots | Sort-Object {
        $d = $dayOrder[$_.dayOfWeek]
        if (-not $d) { $d = 9 }
        "$d_$($_.startTime)"
    }

    # Affichage tableau
    $header = "{0,-12} {1,-8} {2,-8} {3,-22} {4,-20} {5,-10}" -f "Jour","Début","Fin","Cours","Enseignant","Salle"
    Write-Host ""
    Write-Host $header -ForegroundColor Cyan
    Write-Host ("-" * 85) -ForegroundColor DarkGray

    foreach ($s in $sorted) {
        $teacher = if ($s.teacherName) { $s.teacherName } else { "T#$($s.teacherId)" }
        $room    = if ($s.roomName)    { $s.roomName }    else { "Salle $($s.roomId)" }
        $line = "{0,-12} {1,-8} {2,-8} {3,-22} {4,-20} {5,-10}" -f `
            $s.dayOfWeek, $s.startTime, $s.endTime, `
            "$($s.courseCode) $($s.courseName)".Substring(0, [Math]::Min(21,"$($s.courseCode) $($s.courseName)".Length)), `
            $teacher.Substring(0, [Math]::Min(19,$teacher.Length)), `
            $room
        Write-Host $line
    }

    Write-Host ""
    Write-Host "  Total : $($result.slots.Count) créneaux affectés" -ForegroundColor Green

    # Résumé par enseignant
    Write-Host ""
    Write-Host "  Répartition par enseignant :" -ForegroundColor Cyan
    $byTeacher = $result.slots | Group-Object { "T#$($_.teacherId)" }
    foreach ($g in $byTeacher) {
        Write-Host "    $($g.Name) : $($g.Count) créneau(x)"
    }

    # Vérification contraintes disponibilités
    Write-Host ""
    Write-Host "  Vérification contraintes disponibilités :" -ForegroundColor Cyan
    $violations = 0
    foreach ($s in $result.slots) {
        if ($s.teacherId -eq $T1) {
            # T1 (MATH) : LUNDI, MARDI matin, JEUDI, VENDREDI après-midi
            $allowedDays = @("LUNDI","MARDI","JEUDI","VENDREDI")
            if ($s.dayOfWeek -notin $allowedDays) {
                Write-Host "    [VIOLATION] T$T1 placé $($s.dayOfWeek) $($s.startTime) — hors disponibilité" -ForegroundColor Red
                $violations++
            }
        }
        if ($s.teacherId -eq $T2) {
            # T2 (CHIM) : MARDI, MERCREDI, JEUDI après-midi, VENDREDI matin
            $allowedDays = @("MARDI","MERCREDI","JEUDI","VENDREDI")
            if ($s.dayOfWeek -notin $allowedDays) {
                Write-Host "    [VIOLATION] T$T2 placé $($s.dayOfWeek) $($s.startTime) — hors disponibilité" -ForegroundColor Red
                $violations++
            }
        }
    }
    if ($violations -eq 0) {
        Write-Host "    Aucune violation détectée — contraintes respectées" -ForegroundColor Green
    } else {
        Write-Host "    $violations violation(s) détectée(s)" -ForegroundColor Red
    }

} else {
    Write-Host "  Aucun créneau généré." -ForegroundColor Red
}

# ============================================================
Banner "TERMINÉ"
# ============================================================
Write-Host ""
Write-Host "  Pour visualiser dans l'UI : http://localhost:4200/timetable-generator" -ForegroundColor Cyan
Write-Host "  jobId pour re-consulter   : $jobId" -ForegroundColor DarkGray
Write-Host ""
