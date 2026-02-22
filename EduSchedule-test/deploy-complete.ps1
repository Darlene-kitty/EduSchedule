# Script de deploiement complet automatise
param(
    [switch]$SkipBuild,
    [switch]$SkipTests,
    [switch]$CleanStart
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "EduSchedule - Deploiement Complet" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Etape 1: Verification des prerequis
Write-Host "[ETAPE 1/6] Verification des prerequis..." -ForegroundColor Yellow

# Verifier Docker
try {
    docker info | Out-Null
    Write-Host "[OK] Docker est operationnel" -ForegroundColor Green
}
catch {
    Write-Host "[ERREUR] Docker n'est pas en cours d'execution" -ForegroundColor Red
    exit 1
}

# Verifier Maven
try {
    mvn -version | Out-Null
    Write-Host "[OK] Maven est installe" -ForegroundColor Green
}
catch {
    Write-Host "[ERREUR] Maven n'est pas installe" -ForegroundColor Red
    exit 1
}

# Verifier Node.js
try {
    node --version | Out-Null
    Write-Host "[OK] Node.js est installe" -ForegroundColor Green
}
catch {
    Write-Host "[ERREUR] Node.js n'est pas installe" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Etape 2: Nettoyage (si demande)
if ($CleanStart) {
    Write-Host "[ETAPE 2/6] Nettoyage des conteneurs existants..." -ForegroundColor Yellow
    docker-compose down -v
    Write-Host "[OK] Nettoyage termine" -ForegroundColor Green
    Write-Host ""
}
else {
    Write-Host "[ETAPE 2/6] Nettoyage ignore (utiliser -CleanStart pour nettoyer)" -ForegroundColor Gray
    Write-Host ""
}

# Etape 3: Construction des services
if (-not $SkipBuild) {
    Write-Host "[ETAPE 3/6] Construction des services..." -ForegroundColor Yellow
    
    # Construire le frontend
    Write-Host "  Construction du frontend..." -ForegroundColor Cyan
    Push-Location frontend
    try {
        if (-not (Test-Path ".next")) {
            npm install --legacy-peer-deps
            npm run build
        }
        Write-Host "  [OK] Frontend construit" -ForegroundColor Green
    }
    catch {
        Write-Host "  [ERREUR] Echec de la construction du frontend" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Pop-Location
    
    # Construire les services Spring Boot
    Write-Host "  Construction des services Spring Boot..." -ForegroundColor Cyan
    try {
        if ($SkipTests) {
            mvn clean package -DskipTests
        }
        else {
            mvn clean package
        }
        Write-Host "  [OK] Services Spring Boot construits" -ForegroundColor Green
    }
    catch {
        Write-Host "  [ERREUR] Echec de la construction Maven" -ForegroundColor Red
        exit 1
    }
    
    Write-Host ""
}
else {
    Write-Host "[ETAPE 3/6] Construction ignoree (utiliser sans -SkipBuild pour construire)" -ForegroundColor Gray
    Write-Host ""
}

# Etape 4: Construction des images Docker
Write-Host "[ETAPE 4/6] Construction des images Docker..." -ForegroundColor Yellow
try {
    docker-compose build
    Write-Host "[OK] Images Docker construites" -ForegroundColor Green
}
catch {
    Write-Host "[ERREUR] Echec de la construction des images" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Etape 5: Demarrage des services
Write-Host "[ETAPE 5/6] Demarrage des services..." -ForegroundColor Yellow
try {
    docker-compose up -d
    Write-Host "[OK] Services demarres" -ForegroundColor Green
}
catch {
    Write-Host "[ERREUR] Echec du demarrage des services" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Etape 6: Verification du deploiement
Write-Host "[ETAPE 6/6] Verification du deploiement..." -ForegroundColor Yellow
Write-Host "Attente du demarrage des services (60 secondes)..." -ForegroundColor Gray
Start-Sleep -Seconds 60

# Verifier quelques services cles
$criticalServices = @(
    @{Name="Eureka"; Url="http://localhost:8761"},
    @{Name="API Gateway"; Url="http://localhost:8080/actuator/health"},
    @{Name="Frontend"; Url="http://localhost:3000"}
)

$allOk = $true
foreach ($service in $criticalServices) {
    try {
        $response = Invoke-WebRequest -Uri $service.Url -Method Get -TimeoutSec 5 -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            Write-Host "[OK] $($service.Name) est operationnel" -ForegroundColor Green
        }
    }
    catch {
        Write-Host "[WARN] $($service.Name) n'est pas encore pret" -ForegroundColor Yellow
        $allOk = $false
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "DEPLOIEMENT TERMINE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($allOk) {
    Write-Host "[SUCCES] Tous les services critiques sont operationnels!" -ForegroundColor Green
}
else {
    Write-Host "[INFO] Certains services sont encore en cours de demarrage" -ForegroundColor Yellow
    Write-Host "Veuillez patienter 2-3 minutes supplementaires" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "URLs d'acces:" -ForegroundColor Cyan
Write-Host "  - Frontend:          http://localhost:3000" -ForegroundColor White
Write-Host "  - API Gateway:       http://localhost:8080" -ForegroundColor White
Write-Host "  - Eureka Dashboard:  http://localhost:8761" -ForegroundColor White
Write-Host "  - RabbitMQ:          http://localhost:15672 (iusjc/iusjc2025)" -ForegroundColor White
Write-Host "  - Zipkin:            http://localhost:9411" -ForegroundColor White
Write-Host ""
Write-Host "Commandes utiles:" -ForegroundColor Cyan
Write-Host "  - Voir les logs:     docker-compose logs -f" -ForegroundColor White
Write-Host "  - Monitoring:        .\monitor-services.ps1" -ForegroundColor White
Write-Host "  - Verification:      .\verify-services-communication.ps1" -ForegroundColor White
Write-Host "  - Arreter:           docker-compose down" -ForegroundColor White
Write-Host ""
