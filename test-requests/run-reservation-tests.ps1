$BASE = "http://localhost:8080"

function Req($method, $url, $token, $file) {
    $auth = if ($token) { @("-H", "Authorization: Bearer $token") } else { @() }
    $body = if ($file) { @("-H", "Content-Type: application/json", "-d", "@$file") } else { @() }
    $resp = curl.exe -s -w "`n__STATUS__%{http_code}" -X $method "$BASE$url" @auth @body
    $parts = $resp -split "`n__STATUS__"
    return @{ status = $parts[1].Trim(); body = $parts[0].Trim() }
}

function Show($label, $r) {
    Write-Host ""
    Write-Host "--- $label ---"
    Write-Host "HTTP $($r.status)"
    if ($r.body) {
        try { $r.body | ConvertFrom-Json | ConvertTo-Json -Depth 4 }
        catch { Write-Host $r.body }
    } else { Write-Host "(body vide)" }
}

# LOGIN
$login = Req "POST" "/api/auth/login" $null "test-requests/login.json"
$TOKEN = ($login.body | ConvertFrom-Json).token
Write-Host "LOGIN => HTTP $($login.status)"

Write-Host ""
Write-Host "========== RESERVATIONS CRUD TESTS =========="

# [R1] GET ALL
$r = Req "GET" "/api/v1/reservations" $TOKEN $null
$count = try { ($r.body | ConvertFrom-Json).Count } catch { "?" }
Write-Host ""
Write-Host "--- [R1] GET ALL RESERVATIONS ---"
Write-Host "HTTP $($r.status) | $count reservations"

# [R2] CREATE valide
$r = Req "POST" "/api/v1/reservations" $TOKEN "test-requests/create_reservation.json"
Show "[R2] CREATE RESERVATION (attendu 201)" $r
$newId = try { ($r.body | ConvertFrom-Json).data.id } catch { $null }

# [R3] CREATE sans champs obligatoires (attendu 400)
Set-Content "test-requests/_tmp.json" '{"title":"Sans resource"}' -Encoding utf8 -NoNewline
$r = Req "POST" "/api/v1/reservations" $TOKEN "test-requests/_tmp.json"
Show "[R3] CREATE sans resourceId/userId (attendu 400)" $r

# [R4] GET BY ID
if ($newId) {
    $r = Req "GET" "/api/v1/reservations/$newId" $TOKEN $null
    Show "[R4] GET BY ID=$newId (attendu 200)" $r
}

# [R5] GET BY ID inexistant
$r = Req "GET" "/api/v1/reservations/99999" $TOKEN $null
Show "[R5] GET ID inexistant (attendu 404)" $r

# [R6] UPDATE
if ($newId) {
    $r = Req "PUT" "/api/v1/reservations/$newId" $TOKEN "test-requests/update_reservation.json"
    Show "[R6] UPDATE id=$newId (attendu 200)" $r
}

# [R7] UPDATE inexistant
$r = Req "PUT" "/api/v1/reservations/99999" $TOKEN "test-requests/update_reservation.json"
Show "[R7] UPDATE inexistant (attendu 404)" $r

# [R8] APPROVE
if ($newId) {
    $r = Req "PATCH" "/api/v1/reservations/$newId/approve?approvedBy=1" $TOKEN $null
    Show "[R8] APPROVE id=$newId (attendu 200)" $r
}

# [R9] CANCEL
if ($newId) {
    $r = Req "PATCH" "/api/v1/reservations/$newId/cancel?cancelledBy=1&reason=Test+annulation" $TOKEN $null
    Show "[R9] CANCEL id=$newId (attendu 200)" $r
}

# [R10] GET PENDING
$r = Req "GET" "/api/v1/reservations/pending" $TOKEN $null
$count = try { ($r.body | ConvertFrom-Json).Count } catch { "?" }
Write-Host ""
Write-Host "--- [R10] GET PENDING ---"
Write-Host "HTTP $($r.status) | $count reservations en attente"

# [R11] GET BY USER
$r = Req "GET" "/api/v1/reservations/user/2" $TOKEN $null
$count = try { ($r.body | ConvertFrom-Json).Count } catch { "?" }
Write-Host ""
Write-Host "--- [R11] GET BY USER id=2 ---"
Write-Host "HTTP $($r.status) | $count reservations"

# [R12] DELETE
if ($newId) {
    $r = Req "DELETE" "/api/v1/reservations/$newId" $TOKEN $null
    Show "[R12] DELETE id=$newId (attendu 204/200)" $r
}

# [R13] DELETE inexistant
$r = Req "DELETE" "/api/v1/reservations/99999" $TOKEN $null
Show "[R13] DELETE inexistant (attendu 404)" $r

# [R14] CONFLIT - creer deux reservations sur la meme ressource/creneau
Set-Content "test-requests/_tmp.json" '{"resourceId":1,"userId":3,"title":"Conflit Test A","startTime":"2026-05-15T10:00:00","endTime":"2026-05-15T12:00:00","status":"PENDING","type":"COURSE","expectedAttendees":20}' -Encoding utf8 -NoNewline
$rA = Req "POST" "/api/v1/reservations" $TOKEN "test-requests/_tmp.json"
Show "[R14a] CREATE reservation A (attendu 201)" $rA
$idA = try { ($rA.body | ConvertFrom-Json).data.id } catch { $null }

Set-Content "test-requests/_tmp.json" '{"resourceId":1,"userId":4,"title":"Conflit Test B - meme creneau","startTime":"2026-05-15T11:00:00","endTime":"2026-05-15T13:00:00","status":"PENDING","type":"MEETING","expectedAttendees":15}' -Encoding utf8 -NoNewline
$rB = Req "POST" "/api/v1/reservations" $TOKEN "test-requests/_tmp.json"
Show "[R14b] CREATE reservation B conflit (attendu 409/400)" $rB

# Cleanup
if ($idA) { Req "DELETE" "/api/v1/reservations/$idA" $TOKEN $null | Out-Null }
Remove-Item "test-requests/_tmp.json" -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "[DONE] Tests reservations termines"
