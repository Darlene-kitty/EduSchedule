# Créer un utilisateur via l'API Gateway
Write-Host "Création d'un utilisateur admin via API Gateway..." -ForegroundColor Yellow

$adminUser = @{
    username = "admin"
    email = "admin@iusjc.cm"
    password = "admin123"
    role = "ADMIN"
} | ConvertTo-Json

Write-Host "Données à envoyer:" -ForegroundColor Cyan
Write-Host $adminUser -ForegroundColor Gray

try {
    Write-Host "Tentative de création via /api/auth/register..." -ForegroundColor Cyan
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $adminUser -ContentType "application/json"
    Write-Host "✅ Utilisateur créé avec succès!" -ForegroundColor Green
    Write-Host "ID: $($response.id)" -ForegroundColor White
    Write-Host "Username: $($response.username)" -ForegroundColor White
    Write-Host "Email: $($response.email)" -ForegroundColor White
    Write-Host "Role: $($response.role)" -ForegroundColor White
} catch {
    Write-Host "❌ Erreur lors de la création:" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
    
    # Essayer de lire la réponse d'erreur
    try {
        $errorStream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorStream)
        $errorBody = $reader.ReadToEnd()
        Write-Host "Détails: $errorBody" -ForegroundColor Red
    } catch {
        Write-Host "Impossible de lire les détails de l'erreur" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Maintenant, testez la connexion avec:" -ForegroundColor White
Write-Host "  .\test-login.ps1" -ForegroundColor Cyan