# Création de l'utilisateur admin final
Write-Host "=== Création de l'utilisateur admin final ===" -ForegroundColor Green

$headers = @{
    'Content-Type' = 'application/json'
    'Origin' = 'http://localhost:3000'
}

# Créer un admin avec un nom différent
$adminUser = @{
    username = "superadmin"
    email = "superadmin@iusjc.cm"
    password = "admin123"
    role = "ADMIN"
} | ConvertTo-Json

Write-Host "Création de l'utilisateur superadmin..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" -Method POST -Headers $headers -Body $adminUser
    
    Write-Host "Utilisateur admin créé avec succès!" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
    
    # Test de connexion
    Write-Host "`n=== Test de connexion admin ===" -ForegroundColor Green
    
    $loginData = @{
        username = "superadmin"
        password = "admin123"
    } | ConvertTo-Json
    
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers $headers -Body $loginData
    
    Write-Host "Connexion admin réussie!" -ForegroundColor Green
    Write-Host "Status: $($loginResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Admin Token: $($loginResponse.Content)" -ForegroundColor Cyan
    
    Write-Host "`n🎉 SUCCÈS! Utilisez ces credentials dans votre frontend:" -ForegroundColor Green
    Write-Host "Username: superadmin" -ForegroundColor Yellow
    Write-Host "Password: admin123" -ForegroundColor Yellow
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "Status: $statusCode" -ForegroundColor Yellow
    
    # Lire le contenu de l'erreur
    try {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Erreur: $errorContent" -ForegroundColor Red
    } catch {
        Write-Host "Erreur: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Fin du script ===" -ForegroundColor Green