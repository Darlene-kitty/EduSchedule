Write-Host "=== Generation d'un nouveau token ===" -ForegroundColor Green

$headers = @{ "Content-Type" = "application/json" }

# Demander un nouveau token pour Darlene
Write-Host "Demande d'un nouveau token pour Darlene..." -ForegroundColor Cyan

$forgotPasswordData = @{
    email = "jpriscille.kouang@gmail.com"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/forgot-password" -Method POST -Body $forgotPasswordData -Headers $headers
    Write-Host "✅ Nouveau token genere!" -ForegroundColor Green
    Write-Host "Message: $($response.message)" -ForegroundColor Yellow
    Write-Host "`n📧 Verifiez votre boite email: jpriscille.kouang@gmail.com" -ForegroundColor Cyan
    Write-Host "Un nouvel email avec un token frais a ete envoye" -ForegroundColor Yellow
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Instructions ===" -ForegroundColor Green
Write-Host "1. Verifiez votre boite email" -ForegroundColor Yellow
Write-Host "2. Copiez le nouveau token de l'email" -ForegroundColor Yellow
Write-Host "3. Utilisez: .\test-reset-complete.ps1" -ForegroundColor Yellow
Write-Host "4. Ou cliquez directement sur le lien dans l'email" -ForegroundColor Yellow