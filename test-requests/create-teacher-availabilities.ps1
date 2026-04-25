$BASE   = "http://localhost:8080"
$passes = 0
$echecs = 0

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  CREATION DISPONIBILITES ENSEIGNANTS" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# ── Authentification ─────────────────────────────────────────────────────────
$loginBody = '{"username":"admin","password":"admin123"}'
try {
    $loginResp = Invoke-RestMethod -Uri "$BASE/api/auth/login" -Method POST `
        -ContentType "application/json" -Body $loginBody
    $TOKEN = $loginResp.token
    Write-Host "[AUTH] Login OK" -ForegroundColor Green
} catch {
    Write-Host "[AUTH] Login echoue : $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{ Authorization = "Bearer $TOKEN"; "Content-Type" = "application/json" }

# ── Fonction helper ───────────────────────────────────────────────────────────
function Add-Availability {
    param([string]$label, [hashtable]$body)
    $json = $body | ConvertTo-Json
    try {
        $resp = Invoke-RestMethod -Uri "$BASE/api/teacher-availability" `
            -Method POST -Headers $headers -Body $json
        Write-Host "  [PASS] $label (id=$($resp.id))" -ForegroundColor Green
        $script:passes++
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        # Lire le corps de l'erreur pour diagnostiquer
        $errBody = ""
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errBody = $reader.ReadToEnd()
        } catch {}
        Write-Host "  [FAIL] $label - HTTP $status $errBody" -ForegroundColor Red
        $script:echecs++
    }
}

# ═════════════════════════════════════════════════════════════════════════════
# ENSEIGNANT 1 (teacherId = 1)
# ═════════════════════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "--- Enseignant ID=1 ---" -ForegroundColor Yellow

Add-Availability "Lundi 08h-12h AVAILABLE" @{
    teacherId=1; dayOfWeek="MONDAY"; startTime="08:00:00"; endTime="12:00:00"
    availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Lundi matin"
}
Add-Availability "Lundi 14h-17h PREFERRED" @{
    teacherId=1; dayOfWeek="MONDAY"; startTime="14:00:00"; endTime="17:00:00"
    availabilityType="PREFERRED"; recurring=$true; priority=2; notes="Lundi apres-midi prefere"
}
Add-Availability "Mardi 08h-12h AVAILABLE" @{
    teacherId=1; dayOfWeek="TUESDAY"; startTime="08:00:00"; endTime="12:00:00"
    availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Mardi matin"
}
Add-Availability "Mercredi UNAVAILABLE" @{
    teacherId=1; dayOfWeek="WEDNESDAY"; startTime="08:00:00"; endTime="18:00:00"
    availabilityType="UNAVAILABLE"; recurring=$true; priority=1; notes="Indisponible mercredi"
}
Add-Availability "Vendredi 09h-13h AVAILABLE" @{
    teacherId=1; dayOfWeek="FRIDAY"; startTime="09:00:00"; endTime="13:00:00"
    availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Vendredi matin"
}

# ═════════════════════════════════════════════════════════════════════════════
# ENSEIGNANT 2 (teacherId = 2)
# ═════════════════════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "--- Enseignant ID=2 ---" -ForegroundColor Yellow

Add-Availability "Mardi 10h-14h AVAILABLE" @{
    teacherId=2; dayOfWeek="TUESDAY"; startTime="10:00:00"; endTime="14:00:00"
    availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Mardi milieu journee"
}
Add-Availability "Jeudi 08h-12h PREFERRED" @{
    teacherId=2; dayOfWeek="THURSDAY"; startTime="08:00:00"; endTime="12:00:00"
    availabilityType="PREFERRED"; recurring=$true; priority=2; notes="Jeudi matin prefere"
}
Add-Availability "Jeudi 14h-18h AVAILABLE" @{
    teacherId=2; dayOfWeek="THURSDAY"; startTime="14:00:00"; endTime="18:00:00"
    availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Jeudi apres-midi"
}
Add-Availability "Samedi UNAVAILABLE" @{
    teacherId=2; dayOfWeek="SATURDAY"; startTime="08:00:00"; endTime="18:00:00"
    availabilityType="UNAVAILABLE"; recurring=$true; priority=1; notes="Indisponible samedi"
}

# ═════════════════════════════════════════════════════════════════════════════
# ENSEIGNANT 3 (teacherId = 3)
# ═════════════════════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "--- Enseignant ID=3 ---" -ForegroundColor Yellow

Add-Availability "Lundi 14h-18h AVAILABLE" @{
    teacherId=3; dayOfWeek="MONDAY"; startTime="14:00:00"; endTime="18:00:00"
    availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Lundi apres-midi"
}
Add-Availability "Mercredi 08h-12h PREFERRED" @{
    teacherId=3; dayOfWeek="WEDNESDAY"; startTime="08:00:00"; endTime="12:00:00"
    availabilityType="PREFERRED"; recurring=$true; priority=2; notes="Mercredi matin prefere"
}
Add-Availability "Mercredi 14h-17h AVAILABLE" @{
    teacherId=3; dayOfWeek="WEDNESDAY"; startTime="14:00:00"; endTime="17:00:00"
    availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Mercredi apres-midi"
}
Add-Availability "Vendredi UNAVAILABLE" @{
    teacherId=3; dayOfWeek="FRIDAY"; startTime="08:00:00"; endTime="18:00:00"
    availabilityType="UNAVAILABLE"; recurring=$true; priority=1; notes="Indisponible vendredi"
}

# ═════════════════════════════════════════════════════════════════════════════
# ENSEIGNANT 4 (teacherId = 4)
# ═════════════════════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "--- Enseignant ID=4 ---" -ForegroundColor Yellow

Add-Availability "Lundi 08h-10h AVAILABLE" @{
    teacherId=4; dayOfWeek="MONDAY"; startTime="08:00:00"; endTime="10:00:00"
    availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Lundi debut matin"
}
Add-Availability "Lundi 10h-12h PREFERRED" @{
    teacherId=4; dayOfWeek="MONDAY"; startTime="10:00:00"; endTime="12:00:00"
    availabilityType="PREFERRED"; recurring=$true; priority=2; notes="Lundi fin matin prefere"
}
Add-Availability "Mardi 14h-18h AVAILABLE" @{
    teacherId=4; dayOfWeek="TUESDAY"; startTime="14:00:00"; endTime="18:00:00"
    availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Mardi apres-midi"
}
Add-Availability "Jeudi 08h-12h AVAILABLE" @{
    teacherId=4; dayOfWeek="THURSDAY"; startTime="08:00:00"; endTime="12:00:00"
    availabilityType="AVAILABLE"; recurring=$true; priority=1; notes="Jeudi matin"
}
Add-Availability "Vendredi 14h-16h PREFERRED" @{
    teacherId=4; dayOfWeek="FRIDAY"; startTime="14:00:00"; endTime="16:00:00"
    availabilityType="PREFERRED"; recurring=$true; priority=2; notes="Vendredi apres-midi prefere"
}

# ═════════════════════════════════════════════════════════════════════════════
# RESUME
# ═════════════════════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  RESUME" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Passes  : $passes" -ForegroundColor Green
Write-Host "Echoues : $echecs" -ForegroundColor Red
Write-Host "Total   : $($passes + $echecs)"
