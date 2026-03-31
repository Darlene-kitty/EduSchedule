$BASE = "http://localhost:8080"
$TMP = "$env:TEMP\ta_test"
New-Item -ItemType Directory -Force -Path $TMP | Out-Null

function WriteJson($filename, $content) {
    $path = "$TMP\$filename"
    Set-Content $path $content -Encoding utf8 -NoNewline
    return $path
}

function Req($method, $url, $token, $jsonFile) {
    $auth  = if ($token)    { @("-H", "Authorization: Bearer $token") } else { @() }
    $body  = if ($jsonFile) { @("-H", "Content-Type: application/json", "-d", "@$jsonFile") } else { @() }
    $resp  = curl.exe -s -w "`n__STATUS__%{http_code}" -X $method "$BASE$url" @auth @body
    $parts = $resp -split "`n__STATUS__"
    return @{ status = $parts[1].Trim(); body = $parts[0].Trim() }
}

function Show($label, $r) {
    Write-Host ""
    Write-Host "--- $label ---"
    Write-Host "HTTP $($r.status)"
    if ($r.body) {
        try { $r.body | ConvertFrom-Json | ConvertTo-Json -Depth 5 }
        catch { Write-Host $r.body }
    } else { Write-Host "(body vide)" }
}

function ShowCount($label, $r) {
    $count = try { ($r.body | ConvertFrom-Json).Count } catch { "?" }
    Write-Host ""
    Write-Host "--- $label ---"
    Write-Host "HTTP $($r.status) | $count elements retournes"
}

# =========================================================
# LOGIN
# =========================================================
$f = WriteJson "login.json" '{"username":"admin","password":"admin123"}'
$login = Req "POST" "/api/auth/login" $null $f
$TOKEN = try { ($login.body | ConvertFrom-Json).token } catch { $null }

if (-not $TOKEN) {
    Write-Host "ERREUR: Login echoue (HTTP $($login.status))"
    Write-Host $login.body
    exit 1
}
Write-Host "LOGIN => HTTP $($login.status) | token: $($TOKEN.Substring(0, [Math]::Min(40,$TOKEN.Length)))..."

$TID = 1  # ID enseignant - ajuster si besoin

Write-Host ""
Write-Host "=========================================="
Write-Host "  TESTS DISPONIBILITES ENSEIGNANTS"
Write-Host "  teacher_id = $TID"
Write-Host "=========================================="

# =========================================================
Write-Host ""
Write-Host "========== [1] HEALTH CHECK =========="
# =========================================================
$r = Req "GET" "/api/teacher-availability/health" $TOKEN $null
Show "[H] Health check (attendu 200)" $r

# =========================================================
Write-Host ""
Write-Host "========== [2] CREATION UNITAIRE =========="
# =========================================================

# AVAILABLE - lundi matin
$f = WriteJson "c1.json" "{`"teacherId`":$TID,`"dayOfWeek`":`"MONDAY`",`"startTime`":`"08:00:00`",`"endTime`":`"12:00:00`",`"availabilityType`":`"AVAILABLE`",`"isRecurring`":true,`"priorityLevel`":1,`"notes`":`"Disponible lundi matin`"}"
$r = Req "POST" "/api/teacher-availability" $TOKEN $f
Show "[C1] CREATE AVAILABLE - lundi 08h-12h (attendu 201)" $r
$id1 = try { ($r.body | ConvertFrom-Json).id } catch { $null }

# PREFERRED - mardi apres-midi
$f = WriteJson "c2.json" "{`"teacherId`":$TID,`"dayOfWeek`":`"TUESDAY`",`"startTime`":`"14:00:00`",`"endTime`":`"18:00:00`",`"availabilityType`":`"PREFERRED`",`"isRecurring`":true,`"priorityLevel`":2,`"notes`":`"Creneau prefere mardi`"}"
$r = Req "POST" "/api/teacher-availability" $TOKEN $f
Show "[C2] CREATE PREFERRED - mardi 14h-18h (attendu 201)" $r
$id2 = try { ($r.body | ConvertFrom-Json).id } catch { $null }

# UNAVAILABLE - mercredi
$f = WriteJson "c3.json" "{`"teacherId`":$TID,`"dayOfWeek`":`"WEDNESDAY`",`"startTime`":`"08:00:00`",`"endTime`":`"18:00:00`",`"availabilityType`":`"UNAVAILABLE`",`"isRecurring`":true,`"priorityLevel`":1,`"notes`":`"Indisponible mercredi`"}"
$r = Req "POST" "/api/teacher-availability" $TOKEN $f
Show "[C3] CREATE UNAVAILABLE - mercredi (attendu 201)" $r
$id3 = try { ($r.body | ConvertFrom-Json).id } catch { $null }

# Erreur: sans teacherId (attendu 400)
$f = WriteJson "c4.json" "{`"dayOfWeek`":`"FRIDAY`",`"startTime`":`"09:00:00`",`"endTime`":`"11:00:00`",`"availabilityType`":`"AVAILABLE`"}"
$r = Req "POST" "/api/teacher-availability" $TOKEN $f
Show "[C4] CREATE sans teacherId (attendu 400)" $r

# Erreur: type invalide (attendu 400)
$f = WriteJson "c5.json" "{`"teacherId`":$TID,`"dayOfWeek`":`"FRIDAY`",`"startTime`":`"09:00:00`",`"endTime`":`"11:00:00`",`"availabilityType`":`"TYPE_INVALIDE`"}"
$r = Req "POST" "/api/teacher-availability" $TOKEN $f
Show "[C5] CREATE type invalide (attendu 400)" $r

# =========================================================
Write-Host ""
Write-Host "========== [3] BULK CREATE =========="
# =========================================================
$bulkJson = "[{`"teacherId`":$TID,`"dayOfWeek`":`"THURSDAY`",`"startTime`":`"08:00:00`",`"endTime`":`"10:00:00`",`"availabilityType`":`"AVAILABLE`",`"isRecurring`":true,`"priorityLevel`":1},{`"teacherId`":$TID,`"dayOfWeek`":`"THURSDAY`",`"startTime`":`"14:00:00`",`"endTime`":`"16:00:00`",`"availabilityType`":`"PREFERRED`",`"isRecurring`":true,`"priorityLevel`":2},{`"teacherId`":$TID,`"dayOfWeek`":`"FRIDAY`",`"startTime`":`"10:00:00`",`"endTime`":`"12:00:00`",`"availabilityType`":`"AVAILABLE`",`"isRecurring`":true,`"priorityLevel`":1}]"
$f = WriteJson "bulk.json" $bulkJson
$r = Req "POST" "/api/teacher-availability/bulk" $TOKEN $f
Show "[B1] BULK CREATE 3 creneaux (jeudi x2 + vendredi) (attendu 201)" $r
$bulkIds = try { ($r.body | ConvertFrom-Json) | ForEach-Object { $_.id } } catch { @() }

# =========================================================
Write-Host ""
Write-Host "========== [4] LECTURE =========="
# =========================================================

# Par ID existant
if ($id1) {
    $r = Req "GET" "/api/teacher-availability/$id1" $TOKEN $null
    Show "[R1] GET par ID=$id1 (attendu 200)" $r
}

# Par ID inexistant
$r = Req "GET" "/api/teacher-availability/99999" $TOKEN $null
Show "[R2] GET ID inexistant 99999 (attendu 404)" $r

# Toutes les disponibilites de l'enseignant
$r = Req "GET" "/api/teacher-availability/teacher/$TID" $TOKEN $null
ShowCount "[R3] GET toutes disponibilites enseignant $TID" $r

# Par jour - MONDAY
$r = Req "GET" "/api/teacher-availability/teacher/$TID/day/MONDAY" $TOKEN $null
ShowCount "[R4] GET disponibilites MONDAY enseignant $TID" $r

# Par jour - TUESDAY
$r = Req "GET" "/api/teacher-availability/teacher/$TID/day/TUESDAY" $TOKEN $null
ShowCount "[R5] GET disponibilites TUESDAY enseignant $TID" $r

# Slots disponibles - MONDAY
$r = Req "GET" "/api/teacher-availability/teacher/$TID/slots/MONDAY" $TOKEN $null
Show "[R6] GET slots disponibles MONDAY (attendu 200)" $r

# Slots preferes - TUESDAY
$r = Req "GET" "/api/teacher-availability/teacher/$TID/preferred/TUESDAY" $TOKEN $null
Show "[R7] GET slots PREFERRED TUESDAY (attendu 200)" $r

# Statistiques
$r = Req "GET" "/api/teacher-availability/teacher/$TID/stats" $TOKEN $null
Show "[R8] GET stats enseignant $TID (attendu 200)" $r

# =========================================================
Write-Host ""
Write-Host "========== [5] VERIFICATION DISPONIBILITE =========="
# =========================================================

# Lundi 09h00 -> dans le creneau AVAILABLE 08h-12h => true
$r = Req "GET" "/api/teacher-availability/teacher/$TID/check?dayOfWeek=MONDAY&time=09:00:00" $TOKEN $null
Show "[V1] CHECK MONDAY 09:00 (attendu true - dans AVAILABLE 08h-12h)" $r

# Mercredi 10h00 -> UNAVAILABLE => false
$r = Req "GET" "/api/teacher-availability/teacher/$TID/check?dayOfWeek=WEDNESDAY&time=10:00:00" $TOKEN $null
Show "[V2] CHECK WEDNESDAY 10:00 (attendu false - UNAVAILABLE)" $r

# Samedi -> aucun creneau => false
$r = Req "GET" "/api/teacher-availability/teacher/$TID/check?dayOfWeek=SATURDAY&time=10:00:00" $TOKEN $null
Show "[V3] CHECK SATURDAY 10:00 (attendu false - aucun creneau)" $r

# =========================================================
Write-Host ""
Write-Host "========== [6] DETECTION DE CONFLITS =========="
# =========================================================

# Chevauchement avec AVAILABLE lundi 08h-12h
$r = Req "POST" "/api/teacher-availability/check-conflicts?teacherId=$TID&dayOfWeek=MONDAY&startTime=09:00:00&endTime=11:00:00" $TOKEN $null
Show "[CF1] CHECK conflits MONDAY 09:00-11:00 (chevauchement attendu)" $r

# Pas de conflit - vendredi 15h-17h (hors creneaux)
$r = Req "POST" "/api/teacher-availability/check-conflicts?teacherId=$TID&dayOfWeek=FRIDAY&startTime=15:00:00&endTime=17:00:00" $TOKEN $null
Show "[CF2] CHECK conflits FRIDAY 15:00-17:00 (pas de conflit attendu)" $r

# Liste des conflits du lundi
$r = Req "GET" "/api/teacher-availability/teacher/$TID/conflicts/MONDAY" $TOKEN $null
Show "[CF3] GET liste conflits MONDAY enseignant $TID (attendu 200)" $r

# =========================================================
Write-Host ""
Write-Host "========== [7] MISE A JOUR =========="
# =========================================================

# UPDATE existant
if ($id1) {
    $f = WriteJson "u1.json" "{`"teacherId`":$TID,`"dayOfWeek`":`"MONDAY`",`"startTime`":`"07:30:00`",`"endTime`":`"13:00:00`",`"availabilityType`":`"PREFERRED`",`"isRecurring`":true,`"priorityLevel`":2,`"notes`":`"Mise a jour lundi matin etendu`"}"
    $r = Req "PUT" "/api/teacher-availability/$id1" $TOKEN $f
    Show "[U1] UPDATE id=$id1 (attendu 200)" $r
}

# UPDATE ID inexistant
$f = WriteJson "u2.json" "{`"teacherId`":$TID,`"dayOfWeek`":`"MONDAY`",`"startTime`":`"08:00:00`",`"endTime`":`"12:00:00`",`"availabilityType`":`"AVAILABLE`"}"
$r = Req "PUT" "/api/teacher-availability/99999" $TOKEN $f
Show "[U2] UPDATE ID inexistant 99999 (attendu 400/404)" $r

# =========================================================
Write-Host ""
Write-Host "========== [8] SUPPRESSION =========="
# =========================================================

# DELETE existant (id3 = UNAVAILABLE mercredi)
if ($id3) {
    $r = Req "DELETE" "/api/teacher-availability/$id3/teacher/$TID" $TOKEN $null
    Show "[D1] DELETE id=$id3 (attendu 204)" $r

    # Verification post-delete
    $r = Req "GET" "/api/teacher-availability/$id3" $TOKEN $null
    Show "[D2] GET apres DELETE id=$id3 (attendu 404)" $r
}

# DELETE ID inexistant
$r = Req "DELETE" "/api/teacher-availability/99999/teacher/$TID" $TOKEN $null
Show "[D3] DELETE ID inexistant 99999 (attendu 400/404)" $r

# =========================================================
Write-Host ""
Write-Host "========== NETTOYAGE =========="
# =========================================================
$toDelete = @($id1, $id2) + $bulkIds | Where-Object { $_ }
foreach ($id in $toDelete) {
    $r = Req "DELETE" "/api/teacher-availability/$id/teacher/$TID" $TOKEN $null
    Write-Host "  Suppression id=$id => HTTP $($r.status)"
}

# Supprimer fichiers temporaires
Remove-Item "$TMP" -Recurse -Force -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "=========================================="
Write-Host "  [DONE] Tests disponibilites termines"
Write-Host "=========================================="
