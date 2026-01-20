# Script pour créer un utilisateur avec un email unique
Write-Host "=== Création d'utilisateur avec email unique ===" -ForegroundColor Green

$headers = @{
    'Content-Type' = 'application/json'
    'Origin' = 'http://localhost:3000'
}

# Générer un timestamp pour rendre l'email unique
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$uniqueEmail = "jacky_$timestamp@gmail.com"

# Créer l'utilisateur avec un email unique
$newUser = @{
    username = "jacky_$timestamp"
    email = $uniqueEmail
    password = "jacky123"
    role = "ADMIN"
} | ConvertTo-Json

Write-Host "Création de l'utilisateur..." -ForegroundColor Cyan
Write-Host "Username: jacky_$timestamp" -ForegroundColor Yellow
Write-Host "Email: $uniqueEmail" -ForegroundColor Yellow
Write-Host "Password: jacky123" -ForegroundColor Yellow
Write-Host "Role: ADMIN" -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" -Method POST -Headers $headers -Body $newUser
    
    Write-Host "`n✅ Utilisateur créé avec succès!" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    
    $responseData = $response.Content | ConvertFrom-Json
    Write-Host "ID: $($responseData.id)" -ForegroundColor Cyan
    Write-Host "Username: $($responseData.username)" -ForegroundColor Cyan
    Write-Host "Email: $($responseData.email)" -ForegroundColor Cyan
    Write-Host "Role: $($responseData.role)" -ForegroundColor Cyan
    
    # Test de connexion
    Write-Host "`n=== Test de connexion ===" -ForegroundColor Green
    
    $loginData = @{
        username = "jacky_$timestamp"
        password = "jacky123"
    } | ConvertTo-Json
    
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers $headers -Body $loginData
    
    Write-Host "✅ Connexion réussie!" -ForegroundColor Green
    Write-Host "Status: $($loginResponse.StatusCode)" -ForegroundColor Green
    
    $loginResponseData = $loginResponse.Content | ConvertFrom-Json
    Write-Host "Token généré: $($loginResponseData.token.Substring(0, 50))..." -ForegroundColor Cyan
    
    Write-Host "`n📧 Email de bienvenue envoyé à: $uniqueEmail" -ForegroundColor Cyan
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ Erreur: Status $statusCode" -ForegroundColor Red
    
    try {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Détails: $errorContent" -ForegroundColor Red
    } catch {
        Write-Host "Détails: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Fin ===" -ForegroundColor Green