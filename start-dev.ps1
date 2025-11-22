# Script PowerShell pour démarrer l'environnement de développement EduSchedule
# Usage: .\start-dev.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  EduSchedule - Démarrage Dev" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier si Docker est installé
Write-Host "Vérification de Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version
    Write-Host "✓ Docker trouvé: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker n'est pas installé ou n'est pas dans le PATH" -ForegroundColor Red
    Write-Host "  Veuillez installer Docker Desktop: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# Vérifier si Node.js est installé
Write-Host "Vérification de Node.js..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "✓ Node.js trouvé: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Node.js n'est pas installé ou n'est pas dans le PATH" -ForegroundColor Red
    Write-Host "  Veuillez installer Node.js: https://nodejs.org/" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Étape 1: Démarrage du Backend" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Démarrer Docker Compose
Write-Host "Démarrage des services Docker..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Services Docker démarrés avec succès" -ForegroundColor Green
} else {
    Write-Host "✗ Erreur lors du démarrage des services Docker" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Attente du démarrage des services (30 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Étape 2: Configuration du Frontend" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Aller dans le dossier frontend
Set-Location -Path "frontend"

# Vérifier si node_modules existe
if (-Not (Test-Path "node_modules")) {
    Write-Host "Installation des dépendances npm..." -ForegroundColor Yellow
    npm install
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Dépendances installées avec succès" -ForegroundColor Green
    } else {
        Write-Host "✗ Erreur lors de l'installation des dépendances" -ForegroundColor Red
        Set-Location -Path ".."
        exit 1
    }
} else {
    Write-Host "✓ Dépendances déjà installées" -ForegroundColor Green
}

# Vérifier si .env.local existe
if (-Not (Test-Path ".env.local")) {
    Write-Host "Création du fichier .env.local..." -ForegroundColor Yellow
    @"
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_API_TIMEOUT=30000
"@ | Out-File -FilePath ".env.local" -Encoding UTF8
    Write-Host "✓ Fichier .env.local créé" -ForegroundColor Green
} else {
    Write-Host "✓ Fichier .env.local existe déjà" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Étape 3: Démarrage du Frontend" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Démarrage du serveur de développement Next.js..." -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✓ Environnement prêt !" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Services disponibles:" -ForegroundColor Cyan
Write-Host "  - Frontend:          http://localhost:3000" -ForegroundColor White
Write-Host "  - API Gateway:       http://localhost:8080" -ForegroundColor White
Write-Host "  - Eureka Dashboard:  http://localhost:8761" -ForegroundColor White
Write-Host "  - RabbitMQ:          http://localhost:15672 (guest/guest)" -ForegroundColor White
Write-Host ""
Write-Host "Pour arrêter les services:" -ForegroundColor Yellow
Write-Host "  1. Ctrl+C pour arrêter le frontend" -ForegroundColor White
Write-Host "  2. docker-compose down pour arrêter le backend" -ForegroundColor White
Write-Host ""
Write-Host "Appuyez sur Ctrl+C pour arrêter le frontend..." -ForegroundColor Yellow
Write-Host ""

# Démarrer le serveur de développement
npm run dev

# Retourner au dossier racine
Set-Location -Path ".."
