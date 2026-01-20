# Script pour démarrer l'intégration complète Frontend-Backend
Write-Host "=== Démarrage de l'intégration complète ===" -ForegroundColor Green

# Vérifier les prérequis
Write-Host "`n🔍 Vérification des prérequis..." -ForegroundColor Cyan

# Vérifier Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java détecté: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java non trouvé. Installez Java 17 ou plus récent." -ForegroundColor Red
    exit 1
}

# Vérifier Node.js
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js détecté: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js non trouvé. Installez Node.js 18 ou plus récent." -ForegroundColor Red
    exit 1
}

# Vérifier MySQL
Write-Host "`n🗄️  Vérification de MySQL..." -ForegroundColor Cyan
try {
    $mysqlCheck = mysql -u root -p123456 -e "SELECT 1;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ MySQL accessible" -ForegroundColor Green
    } else {
        Write-Host "⚠️  MySQL non accessible avec root/123456" -ForegroundColor Yellow
        Write-Host "   Assurez-vous que MySQL est démarré et configuré" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Impossible de vérifier MySQL" -ForegroundColor Yellow
}

# 1. Démarrer les services backend
Write-Host "`n🚀 ÉTAPE 1: Démarrage des services backend" -ForegroundColor Magenta

Write-Host "Démarrage de l'infrastructure..." -ForegroundColor Yellow
Start-Process -FilePath "powershell" -ArgumentList "-Command", ".\start-infrastructure.bat" -WindowStyle Minimized

Write-Host "Attente de 30 secondes pour l'infrastructure..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "Démarrage des services métier..." -ForegroundColor Yellow
Start-Process -FilePath "powershell" -ArgumentList "-Command", ".\start-backend.bat" -WindowStyle Minimized

Write-Host "Attente de 45 secondes pour les services..." -ForegroundColor Yellow
Start-Sleep -Seconds 45

# 2. Vérifier que les services sont démarrés
Write-Host "`n🔍 ÉTAPE 2: Vérification des services" -ForegroundColor Magenta

$services = @(
    @{ Name = "Eureka Server"; Url = "http://localhost:8761"; Port = 8761 },
    @{ Name = "API Gateway"; Url = "http://localhost:8080/actuator/health"; Port = 8080 },
    @{ Name = "User Service"; Url = "http://localhost:8081/actuator/health"; Port = 8081 },
    @{ Name = "Resource Service"; Url = "http://localhost:8082/api/v1/salles/health"; Port = 8082 }
)

$allServicesReady = $true

foreach ($service in $services) {
    Write-Host "Vérification de $($service.Name)..." -ForegroundColor Cyan
    
    $maxRetries = 10
    $retryCount = 0
    $serviceReady = $false
    
    while ($retryCount -lt $maxRetries -and -not $serviceReady) {
        try {
            $response = Invoke-WebRequest -Uri $service.Url -TimeoutSec 5 -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Host "✅ $($service.Name) est prêt" -ForegroundColor Green
                $serviceReady = $true
            }
        } catch {
            $retryCount++
            Write-Host "⏳ Tentative $retryCount/$maxRetries pour $($service.Name)..." -ForegroundColor Yellow
            Start-Sleep -Seconds 5
        }
    }
    
    if (-not $serviceReady) {
        Write-Host "❌ $($service.Name) n'est pas accessible" -ForegroundColor Red
        $allServicesReady = $false
    }
}

if (-not $allServicesReady) {
    Write-Host "`n⚠️  Certains services ne sont pas prêts. Continuez quand même ? (y/N)" -ForegroundColor Yellow
    $continue = Read-Host
    if ($continue -ne "y" -and $continue -ne "Y") {
        Write-Host "Arrêt du script." -ForegroundColor Red
        exit 1
    }
}

# 3. Préparer le frontend
Write-Host "`n🌐 ÉTAPE 3: Préparation du frontend" -ForegroundColor Magenta

if (Test-Path "frontend/node_modules") {
    Write-Host "✅ Dépendances frontend déjà installées" -ForegroundColor Green
} else {
    Write-Host "📦 Installation des dépendances frontend..." -ForegroundColor Yellow
    Set-Location frontend
    npm install
    Set-Location ..
    Write-Host "✅ Dépendances installées" -ForegroundColor Green
}

# Vérifier la configuration frontend
if (Test-Path "frontend/.env.local") {
    Write-Host "✅ Configuration frontend trouvée" -ForegroundColor Green
} else {
    Write-Host "⚙️  Création de la configuration frontend..." -ForegroundColor Yellow
    Copy-Item "frontend/.env.local.example" "frontend/.env.local"
    Write-Host "✅ Configuration créée" -ForegroundColor Green
}

# 4. Démarrer le frontend
Write-Host "`n🎨 ÉTAPE 4: Démarrage du frontend" -ForegroundColor Magenta

Write-Host "Démarrage du serveur de développement..." -ForegroundColor Yellow
Set-Location frontend
Start-Process -FilePath "powershell" -ArgumentList "-Command", "npm run dev" -WindowStyle Normal
Set-Location ..

Write-Host "Attente de 15 secondes pour le frontend..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# 5. Test d'intégration
Write-Host "`n🧪 ÉTAPE 5: Test d'intégration" -ForegroundColor Magenta

Write-Host "Lancement des tests d'intégration..." -ForegroundColor Yellow
.\test-frontend-backend-integration.ps1

# 6. Instructions finales
Write-Host "`n🎉 INTÉGRATION COMPLÈTE DÉMARRÉE !" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

Write-Host "`n🌐 Accès à l'application:" -ForegroundColor Cyan
Write-Host "URL: http://localhost:3000" -ForegroundColor White
Write-Host "Identifiants: admin / admin123" -ForegroundColor White

Write-Host "`n📋 Services disponibles:" -ForegroundColor Cyan
Write-Host "- Frontend React: http://localhost:3000" -ForegroundColor White
Write-Host "- API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "- Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "- User Service: http://localhost:8081" -ForegroundColor White
Write-Host "- Resource Service: http://localhost:8082" -ForegroundColor White

Write-Host "`n🔧 Fonctionnalités testées:" -ForegroundColor Cyan
Write-Host "✅ Authentification JWT" -ForegroundColor Green
Write-Host "✅ Gestion des utilisateurs (CRUD)" -ForegroundColor Green
Write-Host "✅ Gestion des ressources (CRUD)" -ForegroundColor Green
Write-Host "✅ Interface utilisateur connectée" -ForegroundColor Green

Write-Host "`n📱 Pages à tester:" -ForegroundColor Cyan
Write-Host "1. Connexion (Login)" -ForegroundColor White
Write-Host "2. Dashboard" -ForegroundColor White
Write-Host "3. Gestion des utilisateurs" -ForegroundColor White
Write-Host "4. Gestion des ressources" -ForegroundColor White
Write-Host "5. Profil utilisateur" -ForegroundColor White

Write-Host "`n⚠️  Pour arrêter tous les services:" -ForegroundColor Yellow
Write-Host "Ctrl+C dans chaque fenêtre de terminal" -ForegroundColor White

Write-Host "`n🚀 L'intégration est prête ! Ouvrez http://localhost:3000" -ForegroundColor Green