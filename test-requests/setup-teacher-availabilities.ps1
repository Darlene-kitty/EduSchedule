# Script pour enregistrer des disponibilités variées pour les enseignants L1/S1
# À exécuter avant de tester la génération d'emploi du temps

$TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJyZW1lbWJlck1lIjpmYWxzZSwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NzQzMzM0ODIsImV4cCI6MTc3NDQxOTg4Mn0.c-lfw42yTFLC-yQtOo18v5HCs6jU8wi9Vf4KOTrJ_4I"
$BASE_URL = "http://localhost:8080"

Write-Host "=== Configuration des disponibilités des enseignants ===" -ForegroundColor Cyan
Write-Host ""

# Teacher 6 (MATH) - Disponible Lundi, Mardi, Mercredi matin
Write-Host "Teacher 6 (MATH) - Lundi à Mercredi matin..." -ForegroundColor Yellow
@(
    @{ teacherId=6; schoolId=1; dayOfWeek="MONDAY";    startTime="08:00"; endTime="12:00"; availabilityType="AVAILABLE" }
    @{ teacherId=6; schoolId=1; dayOfWeek="TUESDAY";   startTime="08:00"; endTime="12:00"; availabilityType="AVAILABLE" }
    @{ teacherId=6; schoolId=1; dayOfWeek="WEDNESDAY"; startTime="08:00"; endTime="12:00"; availabilityType="AVAILABLE" }
) | ForEach-Object {
    $body = $_ | ConvertTo-Json
    try {
        Invoke-RestMethod -Uri "$BASE_URL/api/teacher-availability" -Method POST -Body $body -ContentType "application/json" -Headers @{Authorization="Bearer $TOKEN"} | Out-Null
        Write-Host "  ✓ $($_.dayOfWeek) $($_.startTime)-$($_.endTime)" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Erreur: $_" -ForegroundColor Red
    }
}

# Teacher 7 (PHYS) - Disponible Mardi, Jeudi, Vendredi
Write-Host "Teacher 7 (PHYS) - Mardi, Jeudi, Vendredi..." -ForegroundColor Yellow
@(
    @{ teacherId=7; schoolId=1; dayOfWeek="TUESDAY";  startTime="08:00"; endTime="18:00"; availabilityType="AVAILABLE" }
    @{ teacherId=7; schoolId=1; dayOfWeek="THURSDAY"; startTime="08:00"; endTime="18:00"; availabilityType="AVAILABLE" }
    @{ teacherId=7; schoolId=1; dayOfWeek="FRIDAY";   startTime="08:00"; endTime="18:00"; availabilityType="AVAILABLE" }
) | ForEach-Object {
    $body = $_ | ConvertTo-Json
    try {
        Invoke-RestMethod -Uri "$BASE_URL/api/teacher-availability" -Method POST -Body $body -ContentType "application/json" -Headers @{Authorization="Bearer $TOKEN"} | Out-Null
        Write-Host "  ✓ $($_.dayOfWeek) $($_.startTime)-$($_.endTime)" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Erreur: $_" -ForegroundColor Red
    }
}

# Teacher 1 (INFO) - Disponible Lundi après-midi, Mercredi, Jeudi
Write-Host "Teacher 1 (INFO) - Lundi PM, Mercredi, Jeudi..." -ForegroundColor Yellow
@(
    @{ teacherId=1; schoolId=1; dayOfWeek="MONDAY";    startTime="14:00"; endTime="18:00"; availabilityType="AVAILABLE" }
    @{ teacherId=1; schoolId=1; dayOfWeek="WEDNESDAY"; startTime="08:00"; endTime="18:00"; availabilityType="AVAILABLE" }
    @{ teacherId=1; schoolId=1; dayOfWeek="THURSDAY";  startTime="08:00"; endTime="18:00"; availabilityType="AVAILABLE" }
) | ForEach-Object {
    $body = $_ | ConvertTo-Json
    try {
        Invoke-RestMethod -Uri "$BASE_URL/api/teacher-availability" -Method POST -Body $body -ContentType "application/json" -Headers @{Authorization="Bearer $TOKEN"} | Out-Null
        Write-Host "  ✓ $($_.dayOfWeek) $($_.startTime)-$($_.endTime)" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Erreur: $_" -ForegroundColor Red
    }
}

# Teacher 9 (LANG) - Disponible Lundi, Mercredi, Vendredi
Write-Host "Teacher 9 (LANG) - Lundi, Mercredi, Vendredi..." -ForegroundColor Yellow
@(
    @{ teacherId=9; schoolId=1; dayOfWeek="MONDAY";    startTime="08:00"; endTime="18:00"; availabilityType="AVAILABLE" }
    @{ teacherId=9; schoolId=1; dayOfWeek="WEDNESDAY"; startTime="08:00"; endTime="18:00"; availabilityType="AVAILABLE" }
    @{ teacherId=9; schoolId=1; dayOfWeek="FRIDAY";    startTime="08:00"; endTime="18:00"; availabilityType="AVAILABLE" }
) | ForEach-Object {
    $body = $_ | ConvertTo-Json
    try {
        Invoke-RestMethod -Uri "$BASE_URL/api/teacher-availability" -Method POST -Body $body -ContentType "application/json" -Headers @{Authorization="Bearer $TOKEN"} | Out-Null
        Write-Host "  ✓ $($_.dayOfWeek) $($_.startTime)-$($_.endTime)" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Erreur: $_" -ForegroundColor Red
    }
}

# Teacher 3 (CHIM) - Disponible Mardi, Jeudi
Write-Host "Teacher 3 (CHIM) - Mardi, Jeudi..." -ForegroundColor Yellow
@(
    @{ teacherId=3; schoolId=1; dayOfWeek="TUESDAY";  startTime="08:00"; endTime="18:00"; availabilityType="AVAILABLE" }
    @{ teacherId=3; schoolId=1; dayOfWeek="THURSDAY"; startTime="08:00"; endTime="18:00"; availabilityType="AVAILABLE" }
) | ForEach-Object {
    $body = $_ | ConvertTo-Json
    try {
        Invoke-RestMethod -Uri "$BASE_URL/api/teacher-availability" -Method POST -Body $body -ContentType "application/json" -Headers @{Authorization="Bearer $TOKEN"} | Out-Null
        Write-Host "  ✓ $($_.dayOfWeek) $($_.startTime)-$($_.endTime)" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Erreur: $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== Disponibilités configurées avec succès ===" -ForegroundColor Green
Write-Host ""
Write-Host "Résumé des contraintes:" -ForegroundColor Cyan
Write-Host "  • MATH (T6):  Lun-Mer matin uniquement" -ForegroundColor White
Write-Host "  • PHYS (T7):  Mar, Jeu, Ven toute la journée" -ForegroundColor White
Write-Host "  • INFO (T1):  Lun PM, Mer, Jeu toute la journée" -ForegroundColor White
Write-Host "  • LANG (T9):  Lun, Mer, Ven toute la journée" -ForegroundColor White
Write-Host "  • CHIM (T3):  Mar, Jeu toute la journée" -ForegroundColor White
Write-Host ""
Write-Host "Tu peux maintenant tester la génération depuis le frontend!" -ForegroundColor Green
