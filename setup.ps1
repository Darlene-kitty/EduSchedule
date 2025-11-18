# ============================================
# IUSJC Planning 2025 - Script de Configuration (PowerShell)
# ============================================

Write-Host "🚀 Configuration du projet IUSJC Planning 2025" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

# Fonction pour afficher les messages
function Print-Success {
    param($Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Print-Warning {
    param($Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Print-Error {
    param($Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

# Vérifier les prérequis
Write-Host "📋 Vérification des prérequis..." -ForegroundColor Cyan
Write-Host ""

# Vérifier Docker
try {
    $dockerVersion = docker --version
    Print-Success "Docker installé : $dockerVersion"
} catch {
    Print-Error "Docker n'est pas installé"
    exit 1
}

# Vérifier Docker Compose
try {
    $composeVersion = docker-compose --version
    Print-Success "Docker Compose installé : $composeVersion"
} catch {
    Print-Error "Docker Compose n'est pas installé"
    exit 1
}

# Vérifier Java
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Print-Success "Java installé : $javaVersion"
} catch {
    Print-Warning "Java n'est pas installé (nécessaire pour le build Maven)"
}

# Vérifier Maven
try {
    $mavenVersion = mvn -version | Select-Object -First 1
    Print-Success "Maven installé : $mavenVersion"
} catch {
    Print-Warning "Maven n'est pas installé (nécessaire pour le build)"
}

Write-Host ""
Write-Host "📝 Configuration des variables d'environnement..." -ForegroundColor Cyan
Write-Host ""

# Vérifier si .env existe
if (Test-Path .env) {
    Print-Warning ".env existe déjà"
    $response = Read-Host "Voulez-vous le remplacer ? (y/N)"
    if ($response -eq "y" -or $response -eq "Y") {
        Copy-Item .env.example .env -Force
        Print-Success ".env créé depuis .env.example"
    } else {
        Print-Warning ".env conservé"
    }
} else {
    Copy-Item .env.example .env
    Print-Success ".env créé depuis .env.example"
}

Write-Host ""
Write-Host "🔐 Génération des secrets..." -ForegroundColor Cyan
Write-Host ""

# Générer JWT Secret
$bytes = New-Object byte[] 64
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$JWT_SECRET = [Convert]::ToBase64String($bytes)

# Remplacer dans .env
$envContent = Get-Content .env
$envContent = $envContent -replace "JWT_SECRET=.*", "JWT_SECRET=$JWT_SECRET"
$envContent | Set-Content .env

Print-Success "JWT Secret généré"

Write-Host ""
Write-Host "🐳 Configuration Docker..." -ForegroundColor Cyan
Write-Host ""

# Vérifier si des conteneurs existent déjà
$containers = docker ps -aq -f name=iusjc-planning
if ($containers) {
    Print-Warning "Des conteneurs IUSJC Planning existent déjà"
    $response = Read-Host "Voulez-vous les supprimer ? (y/N)"
    if ($response -eq "y" -or $response -eq "Y") {
        docker-compose down -v
        Print-Success "Conteneurs supprimés"
    }
}

Write-Host ""
Write-Host "📦 Build Maven..." -ForegroundColor Cyan
Write-Host ""

$response = Read-Host "Voulez-vous builder les services Maven maintenant ? (Y/n)"
if ($response -ne "n" -and $response -ne "N") {
    try {
        mvn clean package -DskipTests
        Print-Success "Build Maven terminé"
    } catch {
        Print-Error "Erreur lors du build Maven"
    }
} else {
    Print-Warning "Build Maven ignoré"
}

Write-Host ""
Write-Host "🚀 Démarrage des services..." -ForegroundColor Cyan
Write-Host ""

$response = Read-Host "Voulez-vous démarrer les services Docker maintenant ? (Y/n)"
if ($response -ne "n" -and $response -ne "N") {
    docker-compose up -d
    Print-Success "Services démarrés"
    
    Write-Host ""
    Write-Host "⏳ Attente du démarrage complet (30 secondes)..." -ForegroundColor Cyan
    Start-Sleep -Seconds 30
    
    Write-Host ""
    Write-Host "🔍 Vérification des services..." -ForegroundColor Cyan
    Write-Host ""
    
    # Vérifier Eureka
    try {
        $response = Invoke-WebRequest -Uri http://localhost:8761 -UseBasicParsing -TimeoutSec 5
        Print-Success "Eureka Server : http://localhost:8761"
    } catch {
        Print-Warning "Eureka Server non accessible"
    }
    
    # Vérifier API Gateway
    try {
        $response = Invoke-WebRequest -Uri http://localhost:8080/actuator/health -UseBasicParsing -TimeoutSec 5
        Print-Success "API Gateway : http://localhost:8080"
    } catch {
        Print-Warning "API Gateway non accessible"
    }
    
    # Vérifier Frontend
    try {
        $response = Invoke-WebRequest -Uri http://localhost:8090 -UseBasicParsing -TimeoutSec 5
        Print-Success "Frontend : http://localhost:8090"
    } catch {
        Print-Warning "Frontend non accessible"
    }
    
    # Vérifier RabbitMQ
    try {
        $response = Invoke-WebRequest -Uri http://localhost:15672 -UseBasicParsing -TimeoutSec 5
        Print-Success "RabbitMQ Management : http://localhost:15672"
    } catch {
        Print-Warning "RabbitMQ Management non accessible"
    }
    
} else {
    Print-Warning "Démarrage Docker ignoré"
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "✅ Configuration terminée !" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📚 Prochaines étapes :" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Vérifier Eureka (14 services) :"
Write-Host "   http://localhost:8761"
Write-Host ""
Write-Host "2. Tester l'API Gateway :"
Write-Host "   Invoke-WebRequest http://localhost:8080/actuator/health"
Write-Host ""
Write-Host "3. Voir les logs :"
Write-Host "   docker-compose logs -f"
Write-Host ""
Write-Host "4. Arrêter les services :"
Write-Host "   docker-compose down"
Write-Host ""
Write-Host "📖 Documentation :"
Write-Host "   - README.md"
Write-Host "   - ENV_CONFIGURATION.md"
Write-Host "   - ARCHITECTURE_OPTIMISEE.md"
Write-Host "   - COMMANDES_RAPIDES.md"
Write-Host ""
Write-Host "🎉 Bon développement !" -ForegroundColor Green
