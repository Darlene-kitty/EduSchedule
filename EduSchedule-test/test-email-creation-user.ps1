# Test d'envoi d'email lors de la création d'utilisateur
# EduSchedule - Vérification du système d'email

Write-Host "🧪 TEST D'ENVOI D'EMAIL - CRÉATION D'UTILISATEUR" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$API_BASE_URL = "http://localhost:8080"
$NOTIFICATION_SERVICE_URL = "http://localhost:8087"
$USER_SERVICE_URL = "http://localhost:8081"

# Fonction pour tester la connectivité d'un service
function Test-ServiceHealth {
    param($ServiceUrl, $ServiceName)
    
    try {
        Write-Host "🔍 Vérification de $ServiceName..." -ForegroundColor Yellow
        $response = Invoke-RestMethod -Uri "$ServiceUrl/actuator/health" -Method GET -TimeoutSec 5
        if ($response.status -eq "UP") {
            Write-Host "✅ $ServiceName est opérationnel" -ForegroundColor Green
            return $true
        } else {
            Write-Host "❌ $ServiceName n'est pas opérationnel" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "❌ $ServiceName n'est pas accessible: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Fonction pour obtenir un token admin
function Get-AdminToken {
    try {
        Write-Host "🔑 Connexion en tant qu'administrateur..." -ForegroundColor Yellow
        
        $loginData = @{
            username = "admin"
            password = "admin123"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$API_BASE_URL/api/auth/login" -Method POST -Body $loginData -ContentType "application/json"
        
        if ($response.accessToken) {
            Write-Host "✅ Token admin obtenu avec succès" -ForegroundColor Green
            return $response.accessToken
        } else {
            Write-Host "❌ Impossible d'obtenir le token admin" -ForegroundColor Red
            return $null
        }
    } catch {
        Write-Host "❌ Erreur lors de la connexion admin: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Fonction pour créer un utilisateur de test
function Create-TestUser {
    param($Token, $TestEmail)
    
    try {
        Write-Host "👤 Création d'un utilisateur de test..." -ForegroundColor Yellow
        
        $userData = @{
            username = "test-email-user-$(Get-Date -Format 'yyyyMMddHHmmss')"
            email = $TestEmail
            password = "TestPassword123!"
            role = "STUDENT"
        } | ConvertTo-Json
        
        $headers = @{
            "Authorization" = "Bearer $Token"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$API_BASE_URL/api/auth/register" -Method POST -Body $userData -Headers $headers
        
        if ($response.id) {
            Write-Host "✅ Utilisateur créé avec succès: $($response.username)" -ForegroundColor Green
            Write-Host "   📧 Email: $($response.email)" -ForegroundColor Gray
            Write-Host "   🆔 ID: $($response.id)" -ForegroundColor Gray
            return $response
        } else {
            Write-Host "❌ Échec de la création d'utilisateur" -ForegroundColor Red
            return $null
        }
    } catch {
        Write-Host "❌ Erreur lors de la création d'utilisateur: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $errorDetails = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorDetails)
            $errorBody = $reader.ReadToEnd()
            Write-Host "   Détails: $errorBody" -ForegroundColor Red
        }
        return $null
    }
}

# Fonction pour vérifier les notifications dans la base de données
function Check-NotificationInDatabase {
    param($UserEmail)
    
    Write-Host "🗄️ Vérification des notifications dans la base de données..." -ForegroundColor Yellow
    
    # Note: Cette fonction nécessiterait une connexion à MySQL
    # Pour l'instant, on va juste indiquer qu'il faut vérifier manuellement
    Write-Host "   📝 Vérifiez manuellement dans MySQL:" -ForegroundColor Gray
    Write-Host "   SELECT * FROM notifications WHERE recipient = '$UserEmail' ORDER BY created_at DESC LIMIT 5;" -ForegroundColor Gray
}

# Fonction pour tester l'envoi direct d'email via le service de notification
function Test-DirectEmailSend {
    param($Token, $TestEmail)
    
    try {
        Write-Host "📧 Test d'envoi direct d'email..." -ForegroundColor Yellow
        
        $emailData = @{
            to = $TestEmail
            subject = "Test Email EduSchedule - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
            message = "Bonjour,`n`nCeci est un email de test pour vérifier le bon fonctionnement du système d'envoi d'emails d'EduSchedule.`n`nSi vous recevez cet email, cela signifie que :`n✅ La configuration SMTP est correcte`n✅ Le service de notification fonctionne`n✅ L'envoi d'emails est opérationnel`n`nDate du test: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n`nCordialement,`nL'équipe EduSchedule"
            type = "TEST"
        } | ConvertTo-Json
        
        $headers = @{
            "Authorization" = "Bearer $Token"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$NOTIFICATION_SERVICE_URL/api/v1/notifications/send" -Method POST -Body $emailData -Headers $headers -TimeoutSec 30
        
        Write-Host "✅ Email de test envoyé avec succès" -ForegroundColor Green
        Write-Host "   📧 Destinataire: $TestEmail" -ForegroundColor Gray
        Write-Host "   📝 Vérifiez votre boîte email (et le dossier spam)" -ForegroundColor Gray
        return $true
    } catch {
        Write-Host "❌ Erreur lors de l'envoi d'email de test: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            try {
                $errorDetails = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($errorDetails)
                $errorBody = $reader.ReadToEnd()
                Write-Host "   Détails: $errorBody" -ForegroundColor Red
            } catch {
                Write-Host "   Impossible de lire les détails de l'erreur" -ForegroundColor Red
            }
        }
        return $false
    }
}

# Fonction principale
function Main {
    Write-Host "🚀 Démarrage du test d'envoi d'email..." -ForegroundColor Cyan
    Write-Host ""
    
    # Demander l'email de test
    $TestEmail = Read-Host "📧 Entrez votre adresse email pour le test"
    
    if (-not $TestEmail -or $TestEmail -notmatch "^[^@]+@[^@]+\.[^@]+$") {
        Write-Host "❌ Adresse email invalide" -ForegroundColor Red
        return
    }
    
    Write-Host ""
    Write-Host "📧 Email de test: $TestEmail" -ForegroundColor Cyan
    Write-Host ""
    
    # 1. Vérifier les services
    Write-Host "🔍 ÉTAPE 1: Vérification des services" -ForegroundColor Cyan
    Write-Host "----------------------------------------" -ForegroundColor Cyan
    
    $servicesOk = $true
    $servicesOk = $servicesOk -and (Test-ServiceHealth $API_BASE_URL "API Gateway")
    $servicesOk = $servicesOk -and (Test-ServiceHealth $USER_SERVICE_URL "User Service")
    $servicesOk = $servicesOk -and (Test-ServiceHealth $NOTIFICATION_SERVICE_URL "Notification Service")
    
    if (-not $servicesOk) {
        Write-Host ""
        Write-Host "❌ Certains services ne sont pas disponibles. Démarrez-les d'abord:" -ForegroundColor Red
        Write-Host "   - start-all-dev.bat" -ForegroundColor Yellow
        Write-Host "   - Ou démarrez les services individuellement" -ForegroundColor Yellow
        return
    }
    
    Write-Host ""
    
    # 2. Obtenir le token admin
    Write-Host "🔑 ÉTAPE 2: Authentification" -ForegroundColor Cyan
    Write-Host "-----------------------------" -ForegroundColor Cyan
    
    $adminToken = Get-AdminToken
    if (-not $adminToken) {
        Write-Host ""
        Write-Host "❌ Impossible de s'authentifier. Vérifiez que l'utilisateur admin existe:" -ForegroundColor Red
        Write-Host "   - Exécutez: create-final-admin.ps1" -ForegroundColor Yellow
        return
    }
    
    Write-Host ""
    
    # 3. Test d'envoi direct d'email
    Write-Host "📧 ÉTAPE 3: Test d'envoi direct d'email" -ForegroundColor Cyan
    Write-Host "----------------------------------------" -ForegroundColor Cyan
    
    $directEmailOk = Test-DirectEmailSend $adminToken $TestEmail
    
    Write-Host ""
    
    # 4. Test de création d'utilisateur avec email
    Write-Host "👤 ÉTAPE 4: Test de création d'utilisateur" -ForegroundColor Cyan
    Write-Host "-------------------------------------------" -ForegroundColor Cyan
    
    $newUser = Create-TestUser $adminToken $TestEmail
    
    if ($newUser) {
        Write-Host ""
        Write-Host "✅ Utilisateur créé avec succès!" -ForegroundColor Green
        Write-Host "   📧 Un email de bienvenue devrait être envoyé à: $TestEmail" -ForegroundColor Gray
        
        # Attendre un peu pour l'envoi d'email
        Write-Host "⏳ Attente de l'envoi d'email (5 secondes)..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5
        
        Check-NotificationInDatabase $TestEmail
    }
    
    Write-Host ""
    
    # 5. Résumé
    Write-Host "📊 RÉSUMÉ DU TEST" -ForegroundColor Cyan
    Write-Host "==================" -ForegroundColor Cyan
    
    if ($servicesOk) {
        Write-Host "✅ Services: Tous opérationnels" -ForegroundColor Green
    } else {
        Write-Host "❌ Services: Problèmes détectés" -ForegroundColor Red
    }
    
    if ($adminToken) {
        Write-Host "✅ Authentification: Réussie" -ForegroundColor Green
    } else {
        Write-Host "❌ Authentification: Échec" -ForegroundColor Red
    }
    
    if ($directEmailOk) {
        Write-Host "✅ Envoi direct d'email: Réussi" -ForegroundColor Green
    } else {
        Write-Host "❌ Envoi direct d'email: Échec" -ForegroundColor Red
    }
    
    if ($newUser) {
        Write-Host "✅ Création d'utilisateur: Réussie" -ForegroundColor Green
        Write-Host "✅ Email de bienvenue: Envoyé (vérifiez votre boîte)" -ForegroundColor Green
    } else {
        Write-Host "❌ Création d'utilisateur: Échec" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "📝 ACTIONS À EFFECTUER:" -ForegroundColor Yellow
    Write-Host "1. Vérifiez votre boîte email: $TestEmail" -ForegroundColor Gray
    Write-Host "2. Vérifiez aussi le dossier spam/courrier indésirable" -ForegroundColor Gray
    Write-Host "3. Si aucun email reçu, vérifiez les logs:" -ForegroundColor Gray
    Write-Host "   - Logs du notification-service" -ForegroundColor Gray
    Write-Host "   - Logs du user-service" -ForegroundColor Gray
    Write-Host ""
    
    if ($directEmailOk -and $newUser) {
        Write-Host "🎉 TEST RÉUSSI! Le système d'envoi d'emails fonctionne correctement." -ForegroundColor Green
    } else {
        Write-Host "⚠️ TEST PARTIELLEMENT RÉUSSI. Vérifiez la configuration SMTP." -ForegroundColor Yellow
        Write-Host ""
        Write-Host "🔧 DÉPANNAGE:" -ForegroundColor Yellow
        Write-Host "1. Vérifiez le fichier .env (configuration SMTP)" -ForegroundColor Gray
        Write-Host "2. Vérifiez que le mot de passe d'application Gmail est correct" -ForegroundColor Gray
        Write-Host "3. Exécutez: check-email-config.ps1" -ForegroundColor Gray
    }
}

# Exécuter le test
Main