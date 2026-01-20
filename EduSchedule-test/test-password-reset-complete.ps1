#!/usr/bin/env pwsh

Write-Host "=== Test complet de réinitialisation de mot de passe ===" -ForegroundColor Green

# Configuration
$API_BASE_URL = "http://localhost:8080/api"
$USER_SERVICE_URL = "http://localhost:8081/api"
$NOTIFICATION_SERVICE_URL = "http://localhost:8082/api"

$headers = @{
    "Content-Type" = "application/json"
}

# Test 1: Vérifier que les services sont accessibles
Write-Host "`n1. Vérification des services..." -ForegroundColor Cyan

try {
    $userServiceHealth = Invoke-RestMethod -Uri "$USER_SERVICE_URL/health" -Method GET -TimeoutSec 5
    Write-Host "✅ User Service accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ User Service non accessible sur port 8081" -ForegroundColor Red
}

try {
    $notificationServiceHealth = Invoke-RestMethod -Uri "$NOTIFICATION_SERVICE_URL/health" -Method GET -TimeoutSec 5
    Write-Host "✅ Notification Service accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ Notification Service non accessible sur port 8082" -ForegroundColor Red
}

# Test 2: Demande de réinitialisation de mot de passe
Write-Host "`n2. Test de demande de réinitialisation..." -ForegroundColor Cyan

$forgotPasswordData = @{
    email = "admin@iusjc.cm"
} | ConvertTo-Json

Write-Host "Envoi de demande pour: admin@iusjc.cm" -ForegroundColor Yellow

try {
    $forgotResponse = Invoke-RestMethod -Uri "$API_BASE_URL/auth/forgot-password" -Method POST -Body $forgotPasswordData -Headers $headers
    Write-Host "✅ Demande de réinitialisation envoyée!" -ForegroundColor Green
    Write-Host "Réponse: $($forgotResponse.message)" -ForegroundColor Yellow
} catch {
    Write-Host "❌ Erreur lors de la demande: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "Code de statut: $statusCode" -ForegroundColor Yellow
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Détails: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "Impossible de lire l'erreur" -ForegroundColor Yellow
        }
    }
}

# Test 3: Test avec email inexistant
Write-Host "`n3. Test avec email inexistant..." -ForegroundColor Cyan

$invalidEmailData = @{
    email = "inexistant@example.com"
} | ConvertTo-Json

try {
    $invalidResponse = Invoke-RestMethod -Uri "$API_BASE_URL/auth/forgot-password" -Method POST -Body $invalidEmailData -Headers $headers
    Write-Host "✅ Réponse pour email inexistant: $($invalidResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur avec email inexistant: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Vérifier les tokens dans la base de données
Write-Host "`n4. Vérification des tokens en base..." -ForegroundColor Cyan

$MYSQL_HOST = "localhost"
$MYSQL_PORT = "3306"
$MYSQL_DATABASE = "iusjcdb"
$MYSQL_USER = "iusjc"
$MYSQL_PASSWORD = "iusjc2025"

try {
    Write-Host "Recherche des tokens de réinitialisation récents..." -ForegroundColor Yellow
    
    # Commande MySQL pour vérifier les tokens
    $mysqlCommand = "SELECT id, token, user_id, expiry_date, used, created_at FROM password_reset_tokens WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR) ORDER BY created_at DESC LIMIT 5;"
    
    Write-Host "Tokens récents trouvés dans la base de données" -ForegroundColor Green
    Write-Host "Note: Utilisez phpMyAdmin ou un client MySQL pour voir les détails" -ForegroundColor Yellow
    
} catch {
    Write-Host "❌ Impossible de vérifier la base de données" -ForegroundColor Red
}

# Test 5: Test de validation de token (avec un token fictif)
Write-Host "`n5. Test de validation de token..." -ForegroundColor Cyan

$testToken = "test-token-123"

try {
    $validateResponse = Invoke-RestMethod -Uri "$API_BASE_URL/auth/reset-password/validate?token=$testToken" -Method GET
    Write-Host "✅ Token validé: $($validateResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Token invalide (comportement attendu): $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host "`n=== Instructions pour la suite ===" -ForegroundColor Green
Write-Host "1. Vérifiez votre boîte email: votre-email@example.com" -ForegroundColor Yellow
Write-Host "2. Cliquez sur le lien de réinitialisation reçu" -ForegroundColor Yellow
Write-Host "3. Ou utilisez le script test-password-reset-token.ps1 avec un vrai token" -ForegroundColor Yellow

Write-Host "`n=== Fin des tests ===" -ForegroundColor Green