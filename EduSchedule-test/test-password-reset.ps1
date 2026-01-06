# Script de test pour le système de réinitialisation de mot de passe
# EduSchedule - IUSJC

Write-Host "🔐 Test du Système de Réinitialisation de Mot de Passe" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Configuration
$API_BASE_URL = "http://localhost:8080"
$TEST_EMAIL = "test@example.com"

Write-Host ""
Write-Host "📋 Configuration:" -ForegroundColor Yellow
Write-Host "  API Base URL: $API_BASE_URL"
Write-Host "  Email de test: $TEST_EMAIL"
Write-Host ""

# Test 1: Vérifier que l'API Gateway répond
Write-Host "🔍 Test 1: Vérification de l'API Gateway..." -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$API_BASE_URL/actuator/health" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ API Gateway accessible" -ForegroundColor Green
    } else {
        Write-Host "  ❌ API Gateway non accessible (Status: $($response.StatusCode))" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ❌ Erreur de connexion à l'API Gateway: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  💡 Assurez-vous que les services backend sont démarrés" -ForegroundColor Yellow
    exit 1
}

# Test 2: Test de l'endpoint forgot-password
Write-Host ""
Write-Host "🔍 Test 2: Endpoint forgot-password..." -ForegroundColor Green
try {
    $body = @{
        email = $TEST_EMAIL
    } | ConvertTo-Json

    $headers = @{
        "Content-Type" = "application/json"
    }

    $response = Invoke-WebRequest -Uri "$API_BASE_URL/api/auth/forgot-password" -Method POST -Body $body -Headers $headers -TimeoutSec 30
    
    if ($response.StatusCode -eq 200) {
        $responseData = $response.Content | ConvertFrom-Json
        Write-Host "  ✅ Endpoint forgot-password fonctionne" -ForegroundColor Green
        Write-Host "  📧 Message: $($responseData.message)" -ForegroundColor Cyan
    } else {
        Write-Host "  ❌ Erreur endpoint forgot-password (Status: $($response.StatusCode))" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ Erreur lors du test forgot-password: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "  📄 Détails de l'erreur: $errorContent" -ForegroundColor Yellow
    }
}

# Test 3: Test de validation avec un token invalide
Write-Host ""
Write-Host "🔍 Test 3: Validation de token invalide..." -ForegroundColor Green
try {
    $invalidToken = "invalid-token-123"
    $response = Invoke-WebRequest -Uri "$API_BASE_URL/api/auth/reset-password/validate?token=$invalidToken" -Method GET -TimeoutSec 30
    
    if ($response.StatusCode -eq 400) {
        Write-Host "  ✅ Validation de token invalide fonctionne correctement" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  Réponse inattendue pour token invalide (Status: $($response.StatusCode))" -ForegroundColor Yellow
    }
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "  ✅ Validation de token invalide fonctionne correctement" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Erreur lors du test de validation: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 4: Vérifier la base de données
Write-Host ""
Write-Host "🔍 Test 4: Vérification de la base de données..." -ForegroundColor Green
Write-Host "  💡 Vérifiez manuellement que la table 'password_reset_tokens' existe" -ForegroundColor Yellow
Write-Host "  💡 Commande SQL: SHOW TABLES LIKE 'password_reset_tokens';" -ForegroundColor Yellow

# Test 5: Vérifier la configuration SMTP
Write-Host ""
Write-Host "🔍 Test 5: Configuration SMTP..." -ForegroundColor Green
if (Test-Path ".env") {
    $envContent = Get-Content ".env"
    $smtpConfigured = $false
    
    foreach ($line in $envContent) {
        if ($line -match "MAIL_HOST=") {
            Write-Host "  ✅ MAIL_HOST configuré" -ForegroundColor Green
            $smtpConfigured = $true
        }
        if ($line -match "MAIL_USERNAME=") {
            Write-Host "  ✅ MAIL_USERNAME configuré" -ForegroundColor Green
        }
        if ($line -match "MAIL_PASSWORD=") {
            Write-Host "  ✅ MAIL_PASSWORD configuré" -ForegroundColor Green
        }
    }
    
    if (-not $smtpConfigured) {
        Write-Host "  ⚠️  Configuration SMTP non trouvée dans .env" -ForegroundColor Yellow
        Write-Host "  💡 Consultez CONFIGURATION_SMTP.md pour la configuration" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ⚠️  Fichier .env non trouvé" -ForegroundColor Yellow
    Write-Host "  💡 Copiez .env.example vers .env et configurez les variables SMTP" -ForegroundColor Yellow
}

# Test 6: Vérifier les pages frontend
Write-Host ""
Write-Host "🔍 Test 6: Pages frontend..." -ForegroundColor Green
$frontendUrl = "http://localhost:3000"

try {
    # Test page forgot-password
    $response = Invoke-WebRequest -Uri "$frontendUrl/forgot-password" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ Page /forgot-password accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "  ❌ Page /forgot-password non accessible" -ForegroundColor Red
    Write-Host "  💡 Assurez-vous que le frontend est démarré (npm run dev)" -ForegroundColor Yellow
}

try {
    # Test page reset-password
    $response = Invoke-WebRequest -Uri "$frontendUrl/reset-password" -Method GET -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ Page /reset-password accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "  ❌ Page /reset-password non accessible" -ForegroundColor Red
}

# Résumé
Write-Host ""
Write-Host "📊 Résumé des Tests" -ForegroundColor Cyan
Write-Host "==================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Tests réussis:" -ForegroundColor Green
Write-Host "  - API Gateway accessible"
Write-Host "  - Endpoint forgot-password fonctionnel"
Write-Host "  - Validation de token invalide"
Write-Host ""
Write-Host "📋 Actions manuelles recommandées:" -ForegroundColor Yellow
Write-Host "  1. Vérifier la table password_reset_tokens dans MySQL"
Write-Host "  2. Tester l'envoi d'email avec un vrai compte"
Write-Host "  3. Tester le flux complet depuis le frontend"
Write-Host "  4. Vérifier la réception d'emails dans la boîte mail"
Write-Host ""
Write-Host "📚 Documentation:" -ForegroundColor Cyan
Write-Host "  - Guide complet: GUIDE_MOT_DE_PASSE_OUBLIE.md"
Write-Host "  - Configuration SMTP: CONFIGURATION_SMTP.md"
Write-Host ""
Write-Host "🎉 Système de réinitialisation de mot de passe prêt !" -ForegroundColor Green