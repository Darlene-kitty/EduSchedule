# Script pour créer un utilisateur admin via l'API
Write-Host "=== Création de l'utilisateur admin ===" -ForegroundColor Green

# Données de l'utilisateur admin
$adminUser = @{
    username = "Darlene"
    email = "jpriscille.kouang@gmail.com"
    password = "admin123"
    role = "ADMIN"
} | ConvertTo-Json

Write-Host "Tentative de création de l'utilisateur admin..." -ForegroundColor Yellow

try {
    $headers = @{
        'Content-Type' = 'application/json'
        'Origin' = 'http://localhost:3000'
    }
    
    # Essayer de créer l'utilisateur via l'endpoint de registration
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" -Method POST -Headers $headers -Body $adminUser
    
    Write-Host "Utilisateur admin créé avec succès!" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "Status: $statusCode" -ForegroundColor Yellow
    
    if ($statusCode -eq 400) {
        Write-Host "L'utilisateur existe peut-être déjà ou il y a un problème de validation." -ForegroundColor Yellow
        
        # Lire le contenu de l'erreur
        try {
            $errorResponse = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorResponse)
            $errorContent = $reader.ReadToEnd()
            Write-Host "Détails de l'erreur: $errorContent" -ForegroundColor Yellow
        } catch {
            Write-Host "Impossible de lire les détails de l'erreur"
        }
    } else {
        Write-Host "Erreur: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test de connexion avec l'utilisateur admin
Write-Host "`n=== Test de connexion admin ===" -ForegroundColor Green

$loginData = @{
    username = "admin@iusjc.cm"
    password = "admin123"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers $headers -Body $loginData
    
    Write-Host "Connexion admin réussie!" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "Status de connexion: $statusCode" -ForegroundColor Yellow
    
    # Lire le contenu de l'erreur
    try {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Erreur de connexion: $errorContent" -ForegroundColor Yellow
    } catch {
        Write-Host "Erreur de connexion: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Fin du script ===" -ForegroundColor Green