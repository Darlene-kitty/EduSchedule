$BASE = "http://localhost:8080"

function Req($method, $url, $token, $file) {
    $auth = if ($token) { @("-H", "Authorization: Bearer $token") } else { @() }
    $body = if ($file) { @("-H", "Content-Type: application/json", "-d", "@$file") } else { @() }
    $resp = curl.exe -s -w "`n__STATUS__%{http_code}" -X $method "$BASE$url" @auth @body
    $parts = $resp -split "`n__STATUS__"
    $body_out = $parts[0].Trim()
    $status = $parts[1].Trim()
    return @{ status = $status; body = $body_out }
}

function Show($label, $r) {
    Write-Host ""
    Write-Host "--- $label ---"
    Write-Host "HTTP $($r.status)"
    if ($r.body) {
        try { $r.body | ConvertFrom-Json | ConvertTo-Json -Depth 5 }
        catch { Write-Host $r.body }
    } else {
        Write-Host "(body vide)"
    }
}

# LOGIN
$login = Req "POST" "/api/auth/login" $null "test-requests/login.json"
$TOKEN = ($login.body | ConvertFrom-Json).token
Write-Host "LOGIN => HTTP $($login.status) | token: $($TOKEN.Substring(0,30))..."

# =========================================================
Write-Host ""
Write-Host "========== USERS TESTS =========="

# [U1] CREATE USER - nouveau
Set-Content "test-requests/_tmp.json" '{"username":"prof_new99","firstName":"Luc","lastName":"Bello","email":"luc.bello@test.cm","password":"pass123","role":"TEACHER"}' -Encoding utf8 -NoNewline
$r = Req "POST" "/api/users" $TOKEN "test-requests/_tmp.json"
Show "[U1] CREATE USER nouveau (attendu 201)" $r
$newUserId = try { ($r.body | ConvertFrom-Json).id } catch { $null }

# [U2] CREATE USER - doublon username (attendu 400)
$r = Req "POST" "/api/users" $TOKEN "test-requests/create_user.json"
Show "[U2] CREATE USER doublon username (attendu 400)" $r

# [U3] CREATE USER - body invalide sans email (attendu 400)
Set-Content "test-requests/_tmp.json" '{"username":"bad_user","password":"pass123","role":"TEACHER"}' -Encoding utf8 -NoNewline
$r = Req "POST" "/api/users" $TOKEN "test-requests/_tmp.json"
Show "[U3] CREATE USER sans email (attendu 400)" $r

# [U4] GET ALL USERS
$r = Req "GET" "/api/users" $TOKEN $null
$count = try { ($r.body | ConvertFrom-Json).Count } catch { "?" }
Write-Host ""
Write-Host "--- [U4] GET ALL USERS ---"
Write-Host "HTTP $($r.status) | $count users retournes"

# [U5] GET USER BY ID (id=12)
$r = Req "GET" "/api/users/12" $TOKEN $null
Show "[U5] GET USER BY ID=12 (attendu 200)" $r

# [U6] GET USER BY ID inexistant (attendu 404/500)
$r = Req "GET" "/api/users/99999" $TOKEN $null
Show "[U6] GET USER ID inexistant (attendu 404/500)" $r

# [U7] UPDATE USER (id=12)
$r = Req "PUT" "/api/users/12" $TOKEN "test-requests/update_user.json"
Show "[U7] UPDATE USER id=12 (attendu 200)" $r

# [U8] UPDATE USER - ID inexistant (attendu 400/404)
$r = Req "PUT" "/api/users/99999" $TOKEN "test-requests/update_user.json"
Show "[U8] UPDATE USER ID inexistant (attendu 400/404)" $r

# [U9] DELETE USER (le nouveau cree en U1)
if ($newUserId) {
    $r = Req "DELETE" "/api/users/$newUserId" $TOKEN $null
    Show "[U9] DELETE USER id=$newUserId (attendu 204)" $r
} else {
    Write-Host ""
    Write-Host "--- [U9] DELETE USER --- SKIP (creation U1 echouee)"
}

# [U10] DELETE USER inexistant (attendu 404/500)
$r = Req "DELETE" "/api/users/99999" $TOKEN $null
Show "[U10] DELETE USER inexistant (attendu 404/500)" $r

# =========================================================
Write-Host ""
Write-Host "========== STUDENTS TESTS =========="

# [S1] GET ALL STUDENTS
$r = Req "GET" "/api/students" $TOKEN $null
$count = try { ($r.body | ConvertFrom-Json).Count } catch { "?" }
Write-Host ""
Write-Host "--- [S1] GET ALL STUDENTS ---"
Write-Host "HTTP $($r.status) | $count etudiants retournes"

# [S2] CREATE STUDENT (role force STUDENT cote serveur)
$r = Req "POST" "/api/students" $TOKEN "test-requests/create_student.json"
Show "[S2] CREATE STUDENT (attendu 201)" $r
$newStudentId = try { ($r.body | ConvertFrom-Json).id } catch { $null }

# [S3] CREATE STUDENT doublon (attendu 400)
$r = Req "POST" "/api/students" $TOKEN "test-requests/create_student.json"
Show "[S3] CREATE STUDENT doublon (attendu 400)" $r

# [S4] GET STUDENT BY ID
if ($newStudentId) {
    $r = Req "GET" "/api/students/$newStudentId" $TOKEN $null
    Show "[S4] GET STUDENT id=$newStudentId (attendu 200)" $r
}

# [S5] GET STUDENT BY ID inexistant
$r = Req "GET" "/api/students/99999" $TOKEN $null
Show "[S5] GET STUDENT ID inexistant (attendu 404/500)" $r

# [S6] UPDATE STUDENT
if ($newStudentId) {
    $r = Req "PUT" "/api/students/$newStudentId" $TOKEN "test-requests/update_student.json"
    Show "[S6] UPDATE STUDENT id=$newStudentId (attendu 200)" $r
}

# [S7] UPDATE STUDENT inexistant
$r = Req "PUT" "/api/students/99999" $TOKEN "test-requests/update_student.json"
Show "[S7] UPDATE STUDENT inexistant (attendu 400/404)" $r

# [S8] DELETE STUDENT
if ($newStudentId) {
    $r = Req "DELETE" "/api/students/$newStudentId" $TOKEN $null
    Show "[S8] DELETE STUDENT id=$newStudentId (attendu 204)" $r
}

# [S9] DELETE STUDENT inexistant
$r = Req "DELETE" "/api/students/99999" $TOKEN $null
Show "[S9] DELETE STUDENT inexistant (attendu 404)" $r

# Cleanup
Remove-Item "test-requests/_tmp.json" -ErrorAction SilentlyContinue
Write-Host ""
Write-Host "[DONE] Tests termines"
