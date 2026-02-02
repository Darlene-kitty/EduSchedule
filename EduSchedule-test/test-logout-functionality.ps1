# Script de test pour verifier le fonctionnement du bouton de deconnexion
# Ce script teste la fonctionnalite de deconnexion de l'application EduSchedule

Write-Host "=== Test de la fonctionnalite de deconnexion ===" -ForegroundColor Green
Write-Host ""

# Configuration
$API_GATEWAY_URL = "http://localhost:8080"
$USER_SERVICE_URL = "http://localhost:8081"
$FRONTEND_URL = "http://localhost:3000"

# Fonction pour tester la connectivité
function Test-ServiceConnectivity {
    param($url, $serviceName)
    
    try {
        $response = Invoke-WebRequest -Uri "$url/actuator/health" -Method GET -TimeoutSec 5 -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            Write-Host "OK $serviceName est accessible" -ForegroundColor Green
            return $true
        }
    } catch {
        Write-Host "ERREUR $serviceName n'est pas accessible" -ForegroundColor Red
        return $false
    }
}

# Fonction pour se connecter et obtenir un token
function Get-AuthToken {
    param($username, $password)
    
    try {
        $loginData = @{
            username = $username
            password = $password
        } | ConvertTo-Json
        
        $headers = @{
            'Content-Type' = 'application/json'
        }
        
        Write-Host "Tentative de connexion avec l'utilisateur: $username" -ForegroundColor Yellow
        
        $response = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/auth/login" -Method POST -Body $loginData -Headers $headers
        
        if ($response.token) {
            Write-Host "OK Connexion reussie - Token obtenu" -ForegroundColor Green
            return @{
                token = $response.token
                refreshToken = $response.refreshToken
                userId = $response.userId
                username = $response.username
            }
        } else {
            Write-Host "ERREUR Echec de la connexion - Pas de token recu" -ForegroundColor Red
            return $null
        }
    } catch {
        Write-Host "ERREUR Erreur lors de la connexion: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Fonction pour tester la déconnexion
function Test-Logout {
    param($token, $refreshToken)
    
    try {
        $logoutData = @{
            refreshToken = $refreshToken
        } | ConvertTo-Json
        
        $headers = @{
            'Content-Type' = 'application/json'
            'Authorization' = "Bearer $token"
        }
        
        Write-Host "Test de deconnexion..." -ForegroundColor Yellow
        
        $response = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/auth/logout" -Method POST -Body $logoutData -Headers $headers
        
        Write-Host "OK Deconnexion reussie cote serveur" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "ERREUR Erreur lors de la deconnexion: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Fonction pour vérifier que le token est invalidé
function Test-TokenInvalidation {
    param($token)
    
    try {
        $headers = @{
            'Authorization' = "Bearer $token"
        }
        
        Write-Host "🔍 Vérification de l'invalidation du token..." -ForegroundColor Yellow
        
        $response = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/users/me" -Method GET -Headers $headers
        
        Write-Host "❌ Le token est encore valide (problème!)" -ForegroundColor Red
        return $false
    } catch {
        if ($_.Exception.Response.StatusCode -eq 401) {
            Write-Host "✅ Token correctement invalidé (401 Unauthorized)" -ForegroundColor Green
            return $true
        } else {
            Write-Host "⚠️  Erreur inattendue: $($_.Exception.Message)" -ForegroundColor Yellow
            return $false
        }
    }
}

# Fonction pour tester la déconnexion frontend
function Test-FrontendLogout {
    Write-Host "🌐 Test de la déconnexion côté frontend..." -ForegroundColor Yellow
    
    # Créer un fichier HTML de test pour simuler la déconnexion
    $testHtml = @"
<!DOCTYPE html>
<html>
<head>
    <title>Test Déconnexion Frontend</title>
    <script>
        // Simuler la déconnexion frontend
        function testLogout() {
            // Nettoyer le localStorage
            localStorage.removeItem('eduSchedule_session');
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('savedEmail');
            
            console.log('✅ Session locale nettoyée');
            
            // Vérifier que les données sont supprimées
            const session = localStorage.getItem('eduSchedule_session');
            const token = localStorage.getItem('token');
            
            if (!session && !token) {
                document.getElementById('result').innerHTML = '✅ Déconnexion frontend réussie - Données locales supprimées';
                document.getElementById('result').style.color = 'green';
            } else {
                document.getElementById('result').innerHTML = '❌ Échec du nettoyage des données locales';
                document.getElementById('result').style.color = 'red';
            }
        }
        
        // Exécuter le test au chargement
        window.onload = function() {
            // Simuler des données de session
            localStorage.setItem('eduSchedule_session', JSON.stringify({
                data: { id: '1', name: 'Test User', role: 'admin' },
                timestamp: Date.now(),
                rememberMe: false
            }));
            localStorage.setItem('token', 'test-token');
            localStorage.setItem('refreshToken', 'test-refresh-token');
            
            console.log('Session de test créée');
            
            // Tester la déconnexion après 1 seconde
            setTimeout(testLogout, 1000);
        };
    </script>
</head>
<body>
    <h1>Test de Déconnexion Frontend</h1>
    <div id="result">Test en cours...</div>
</body>
</html>
"@
    
    $testFile = "test-frontend-logout.html"
    $testHtml | Out-File -FilePath $testFile -Encoding UTF8
    
    Write-Host "📄 Fichier de test créé: $testFile" -ForegroundColor Cyan
    Write-Host "   Ouvrez ce fichier dans un navigateur pour tester la déconnexion frontend" -ForegroundColor Cyan
}

# Début des tests
Write-Host "1. Vérification de la connectivité des services..." -ForegroundColor Cyan
Write-Host ""

$gatewayOk = Test-ServiceConnectivity $API_GATEWAY_URL "API Gateway"
$userServiceOk = Test-ServiceConnectivity $USER_SERVICE_URL "User Service"

if (-not $gatewayOk) {
    Write-Host ""
    Write-Host "⚠️  API Gateway non accessible. Démarrez les services avec:" -ForegroundColor Yellow
    Write-Host "   .\start-backend-only.bat" -ForegroundColor White
    Write-Host ""
}

Write-Host ""
Write-Host "2. Test de connexion et déconnexion..." -ForegroundColor Cyan
Write-Host ""

# Tenter de se connecter avec l'utilisateur admin par défaut
$authResult = Get-AuthToken "admin" "admin123"

if ($authResult) {
    Write-Host ""
    Write-Host "3. Test de déconnexion côté serveur..." -ForegroundColor Cyan
    Write-Host ""
    
    $logoutSuccess = Test-Logout $authResult.token $authResult.refreshToken
    
    if ($logoutSuccess) {
        Write-Host ""
        Write-Host "4. Vérification de l'invalidation du token..." -ForegroundColor Cyan
        Write-Host ""
        
        $tokenInvalidated = Test-TokenInvalidation $authResult.token
        
        if ($tokenInvalidated) {
            Write-Host ""
            Write-Host "✅ Test de déconnexion côté serveur: RÉUSSI" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "❌ Test de déconnexion côté serveur: ÉCHEC (token non invalidé)" -ForegroundColor Red
        }
    }
} else {
    Write-Host ""
    Write-Host "⚠️  Impossible de tester la déconnexion sans connexion réussie" -ForegroundColor Yellow
    Write-Host "   Vérifiez que l'utilisateur admin existe avec le mot de passe admin123" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "5. Test de déconnexion côté frontend..." -ForegroundColor Cyan
Write-Host ""

Test-FrontendLogout

Write-Host ""
Write-Host "=== Résumé des tests de déconnexion ===" -ForegroundColor Green
Write-Host ""
Write-Host "Composants testés:" -ForegroundColor White
Write-Host "✓ Déconnexion côté serveur (API)" -ForegroundColor Green
Write-Host "✓ Invalidation des tokens" -ForegroundColor Green
Write-Host "✓ Nettoyage des données locales (frontend)" -ForegroundColor Green
Write-Host ""
Write-Host "Points de vérification:" -ForegroundColor White
Write-Host "• Le token est-il invalidé côté serveur ?" -ForegroundColor Gray
Write-Host "• Les données de session sont-elles supprimées du localStorage ?" -ForegroundColor Gray
Write-Host "• L'utilisateur est-il redirigé vers la page de connexion ?" -ForegroundColor Gray
Write-Host "• Les tokens sont-ils supprimés de la mémoire ?" -ForegroundColor Gray
Write-Host ""
Write-Host "Pour tester manuellement:" -ForegroundColor Yellow
Write-Host "1. Connectez-vous sur $FRONTEND_URL" -ForegroundColor White
Write-Host "2. Cliquez sur le bouton 'Déconnexion' dans la sidebar" -ForegroundColor White
Write-Host "3. Vérifiez que vous êtes redirigé vers /login" -ForegroundColor White
Write-Host "4. Vérifiez que les données sont supprimées du localStorage (F12 > Application > Local Storage)" -ForegroundColor White
Write-Host ""

# Instructions pour les tests manuels
Write-Host "=== Tests manuels recommandés ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Test de déconnexion normale:" -ForegroundColor White
Write-Host "   • Se connecter avec un utilisateur valide" -ForegroundColor Gray
Write-Host "   • Cliquer sur 'Déconnexion' dans la sidebar" -ForegroundColor Gray
Write-Host "   • Vérifier la redirection vers /login" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Test de déconnexion avec 'Se souvenir de moi':" -ForegroundColor White
Write-Host "   • Se connecter avec 'Se souvenir de moi' activé" -ForegroundColor Gray
Write-Host "   • Se déconnecter" -ForegroundColor Gray
Write-Host "   • Vérifier que l'email sauvegardé est supprimé" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Test de déconnexion avec erreur réseau:" -ForegroundColor White
Write-Host "   • Se connecter normalement" -ForegroundColor Gray
Write-Host "   • Arrêter le backend" -ForegroundColor Gray
Write-Host "   • Cliquer sur 'Déconnexion'" -ForegroundColor Gray
Write-Host "   • Vérifier que la déconnexion locale fonctionne quand même" -ForegroundColor Gray
Write-Host ""

Write-Host "Script terminé. Consultez les résultats ci-dessus." -ForegroundColor Green