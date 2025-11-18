# ============================================
# IUSJC Planning 2025 - Validation de Configuration
# ============================================

Write-Host "🔍 Validation de la Configuration" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

$errors = 0
$warnings = 0
$success = 0

function Test-Item {
    param($Name, $Condition, $ErrorMessage)
    
    Write-Host -NoNewline "Vérification: $Name... "
    
    if ($Condition) {
        Write-Host "✅ OK" -ForegroundColor Green
        $script:success++
    } else {
        Write-Host "❌ ERREUR" -ForegroundColor Red
        Write-Host "   $ErrorMessage" -ForegroundColor Yellow
        $script:errors++
    }
}

function Test-Warning {
    param($Name, $Condition, $WarningMessage)
    
    Write-Host -NoNewline "Vérification: $Name... "
    
    if ($Condition) {
        Write-Host "✅ OK" -ForegroundColor Green
        $script:success++
    } else {
        Write-Host "⚠️  AVERTISSEMENT" -ForegroundColor Yellow
        Write-Host "   $WarningMessage" -ForegroundColor Yellow
        $script:warnings++
    }
}

# 1. Fichiers de configuration
Write-Host "📁 Fichiers de Configuration" -ForegroundColor Cyan
Write-Host ""

Test-Item ".env" (Test-Path .env) "Fichier .env manquant. Exécutez: Copy-Item .env.example .env"
Test-Item ".env.example" (Test-Path .env.example) "Fichier .env.example manquant"
Test-Item "docker-compose.yml" (Test-Path docker-compose.yml) "Fichier docker-compose.yml manquant"
Test-Item "init-db.sql" (Test-Path init-db.sql) "Fichier init-db.sql manquant"
Test-Item "pom.xml" (Test-Path pom.xml) "Fichier pom.xml manquant"

Write-Host ""

# 2. Structure des services
Write-Host "📦 Structure des Services" -ForegroundColor Cyan
Write-Host ""

$services = @(
    "eureka-server",
    "api-gateway",
    "user-service",
    "school-service",
    "resource-service",
    "room-service",
    "course-service",
    "scheduling-service",
    "reservation-service",
    "event-service",
    "notification-service",
    "reporting-service",
    "ent-integration-service",
    "frontend-thymeleaf"
)

foreach ($service in $services) {
    $pomPath = "$service/pom.xml"
    $appPropsPath = "$service/src/main/resources/application.properties"
    
    Test-Item "$service/pom.xml" (Test-Path $pomPath) "POM manquant pour $service"
    
    if ($service -ne "eureka-server" -and $service -ne "api-gateway") {
        Test-Item "$service/application.properties" (Test-Path $appPropsPath) "application.properties manquant pour $service"
    }
}

Write-Host ""

# 3. Contenu du fichier .env
Write-Host "🔧 Variables d'Environnement" -ForegroundColor Cyan
Write-Host ""

if (Test-Path .env) {
    $envContent = Get-Content .env -Raw
    
    $requiredVars = @(
        "MYSQL_DATABASE",
        "MYSQL_USER",
        "MYSQL_PASSWORD",
        "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE",
        "SPRING_DATASOURCE_URL"
    )
    
    foreach ($var in $requiredVars) {
        $found = $envContent -match "$var="
        Test-Item "Variable $var" $found "Variable $var manquante dans .env"
    }
    
    # Vérifier que iusjcdb est utilisé
    $usesIusjcdb = $envContent -match "iusjcdb"
    Test-Item "Base de données unique (iusjcdb)" $usesIusjcdb "La base de données devrait être 'iusjcdb'"
}

Write-Host ""

# 4. Outils installés
Write-Host "🛠️  Outils Installés" -ForegroundColor Cyan
Write-Host ""

Test-Warning "Docker" (Get-Command docker -ErrorAction SilentlyContinue) "Docker n'est pas installé. Télécharger: https://www.docker.com/products/docker-desktop/"
Test-Warning "Java" (Get-Command java -ErrorAction SilentlyContinue) "Java n'est pas installé. Télécharger: https://adoptium.net/"
Test-Warning "Maven" (Get-Command mvn -ErrorAction SilentlyContinue) "Maven n'est pas installé. Télécharger: https://maven.apache.org/download.cgi"

Write-Host ""

# 5. Vérifier les versions
Write-Host "📌 Versions" -ForegroundColor Cyan
Write-Host ""

if (Get-Command java -ErrorAction SilentlyContinue) {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "Java: $javaVersion"
    
    if ($javaVersion -match '17|18|19|20|21') {
        Write-Host '   ✅ Version Java compatible' -ForegroundColor Green
        $script:success++
    } else {
        Write-Host '   ⚠️  Java 17+ recommandé' -ForegroundColor Yellow
        $script:warnings++
    }
}

if (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mavenVersion = mvn -version | Select-Object -First 1
    Write-Host "Maven: $mavenVersion"
}

if (Get-Command docker -ErrorAction SilentlyContinue) {
    $dockerVersion = docker --version
    Write-Host "Docker: $dockerVersion"
}

Write-Host ""

# 6. Documentation
Write-Host "📚 Documentation" -ForegroundColor Cyan
Write-Host ""

$docs = @(
    "README.md",
    "ARCHITECTURE_OPTIMISEE.md",
    "CHANGEMENTS_APPLIQUES.md",
    "COMMANDES_RAPIDES.md",
    "ENV_CONFIGURATION.md",
    "GUIDE_TEST_MANUEL.md"
)

foreach ($doc in $docs) {
    Test-Item $doc (Test-Path $doc) "Documentation $doc manquante"
}

Write-Host ""

# Résumé
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "📊 Résumé de la Validation" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Succès: $success" -ForegroundColor Green
Write-Host "⚠️  Avertissements: $warnings" -ForegroundColor Yellow
Write-Host "❌ Erreurs: $errors" -ForegroundColor Red
Write-Host ""

if ($errors -eq 0 -and $warnings -eq 0) {
    Write-Host "🎉 Configuration parfaite !" -ForegroundColor Green
    Write-Host ""
    Write-Host "Prochaines étapes:" -ForegroundColor Cyan
    Write-Host "1. Build Maven: mvn clean package -DskipTests"
    Write-Host "2. Démarrer Docker: docker compose up -d"
    Write-Host "3. Vérifier Eureka: http://localhost:8761"
    Write-Host ""
    exit 0
} elseif ($errors -eq 0) {
    Write-Host "✅ Configuration valide avec quelques avertissements" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Les avertissements n'empêchent pas le fonctionnement." -ForegroundColor Yellow
    Write-Host "Installez les outils manquants si nécessaire." -ForegroundColor Yellow
    Write-Host ""
    exit 0
} else {
    Write-Host "Configuration incomplete" -ForegroundColor Red
    Write-Host ""
    Write-Host "Corrigez les erreurs avant de continuer." -ForegroundColor Red
    Write-Host "Consultez GUIDE_TEST_MANUEL.md pour plus d aide." -ForegroundColor Yellow
    Write-Host ""
    exit 1
}
