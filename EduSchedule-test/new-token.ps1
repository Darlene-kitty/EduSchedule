Write-Host "=== Nouveau token ===" -ForegroundColor Green

$headers = @{ "Content-Type" = "application/json" }

$forgotPasswordData = @{
    email = "jpriscille.kouang@gmail.com"
} | ConvertTo-Json

Write-Host "Demande d'un nouveau token..." -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/forgot-password" -Method POST -Body $forgotPasswordData -Headers $headers
    Write-Host "✅ Token genere!" -ForegroundColor Green
    Write-Host "Message: $($response.message)" -ForegroundColor Yellow
    Write-Host "Verifiez votre email!" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== Fin ===" -ForegroundColor Green