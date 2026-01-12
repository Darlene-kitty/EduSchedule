Write-Host "=== Generation token frais ===" -ForegroundColor Green

$headers = @{ "Content-Type" = "application/json" }

$forgotData = @{
    email = "jpriscille.kouang@gmail.com"
} | ConvertTo-Json

Write-Host "Demande nouveau token pour Darlene..." -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/forgot-password" -Method POST -Body $forgotData -Headers $headers
    Write-Host "✅ Token frais genere!" -ForegroundColor Green
    Write-Host "Message: $($response.message)" -ForegroundColor Yellow
    Write-Host "`n📧 Nouveau email envoye a: jpriscille.kouang@gmail.com" -ForegroundColor Cyan
    Write-Host "Utilisez le nouveau token de cet email" -ForegroundColor Yellow
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Instructions ===" -ForegroundColor Green
Write-Host "1. Verifiez votre boite email" -ForegroundColor Yellow
Write-Host "2. Copiez le NOUVEAU token" -ForegroundColor Yellow
Write-Host "3. Testez sur l'interface web avec le nouveau token" -ForegroundColor Yellow