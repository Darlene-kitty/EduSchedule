# Script pour créer un utilisateur admin
Write-Host "Création d'un utilisateur admin..." -ForegroundColor Yellow

# Données de l'utilisateur admin
$adminUser = @{
    username = "admin"
    email = "admin@iusjc.cm"
    password = "admin123"
    role = "ADMIN"
} | ConvertTo-Json

# Créer l'utilisateur via l'API
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $adminUser -ContentType "application/json"
    Write-Host "✅ Utilisateur admin créé avec succès!" -ForegroundColor Green
    Write-Host "Username: admin" -ForegroundColor Cyan
    Write-Host "Password: admin123" -ForegroundColor Cyan
    Write-Host "Email: admin@iusjc.cm" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Erreur lors de la création de l'utilisateur:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    # Essayer de créer via le User Service directement
    Write-Host "Tentative via User Service directement..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8096/api/users" -Method POST -Body $adminUser -ContentType "application/json"
        Write-Host "✅ Utilisateur admin créé via User Service!" -ForegroundColor Green
    } catch {
        Write-Host "❌ Échec également via User Service:" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Vous pouvez maintenant tester la connexion avec:" -ForegroundColor White
Write-Host "- Username: admin" -ForegroundColor Gray
Write-Host "- Password: admin123" -ForegroundColor Gray
Write-Host ""
Write-Host "Page de test: http://localhost:3000/test-connection" -ForegroundColor Cyan