$baseUrl = "http://localhost:8082/api/v1/salles"

$salles = @(
    @{
        code      = "A101"
        name      = "Salle A101"
        batiment  = "Batiment A"
        etage     = "1"
        capacite  = 40
        type      = "SALLE_COURS"
        disponible = $true
        active    = $true
    },
    @{
        code      = "A102"
        name      = "Salle A102"
        batiment  = "Batiment A"
        etage     = "1"
        capacite  = 30
        type      = "SALLE_TD"
        disponible = $true
        active    = $true
    },
    @{
        code      = "B201"
        name      = "Amphi B201"
        batiment  = "Batiment B"
        etage     = "2"
        capacite  = 150
        type      = "AMPHITHEATRE"
        disponible = $true
        active    = $true
    },
    @{
        code      = "C301"
        name      = "Labo Informatique C301"
        batiment  = "Batiment C"
        etage     = "3"
        capacite  = 25
        type      = "LABORATOIRE"
        disponible = $true
        active    = $true
    }
)

$passes  = 0
$echecs  = 0

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  CREATION DE 4 SALLES - resource-service :8082" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

foreach ($salle in $salles) {
    $body = $salle | ConvertTo-Json
    try {
        $response = Invoke-RestMethod -Uri $baseUrl -Method POST `
            -ContentType "application/json" -Body $body
        Write-Host "[PASS] Salle creee : $($response.code) - $($response.name) (id=$($response.id))" -ForegroundColor Green
        $passes++
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        Write-Host "[FAIL] Echec creation $($salle.code) - HTTP $status : $($_.Exception.Message)" -ForegroundColor Red
        $echecs++
    }
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  RESUME" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Passes  : $passes" -ForegroundColor Green
Write-Host "Echoues : $echecs" -ForegroundColor Red
Write-Host "Total   : $($passes + $echecs)"
