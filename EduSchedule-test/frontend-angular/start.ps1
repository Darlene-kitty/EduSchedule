# Script de démarrage pour l'application Angular
Write-Host "🚀 Démarrage de l'application EduSchedule..." -ForegroundColor Green
Write-Host ""

# Vérifier si node_modules existe
if (-not (Test-Path "node_modules")) {
    Write-Host "📦 Installation des dépendances..." -ForegroundColor Yellow
    npm install
    Write-Host ""
}

Write-Host "✅ Lancement du serveur de développement..." -ForegroundColor Green
Write-Host ""
Write-Host "📱 L'application sera accessible sur: http://localhost:4200" -ForegroundColor Cyan
Write-Host ""
Write-Host "🔐 Pour vous connecter:" -ForegroundColor Cyan
Write-Host "   - Username: n'importe quoi (ex: admin)" -ForegroundColor White
Write-Host "   - Password: n'importe quoi (ex: password)" -ForegroundColor White
Write-Host ""
Write-Host "📚 Documentation disponible dans:" -ForegroundColor Cyan
Write-Host "   - README_CONNEXION.md" -ForegroundColor White
Write-Host "   - DEMARRAGE_RAPIDE.md" -ForegroundColor White
Write-Host ""
Write-Host "Appuyez sur Ctrl+C pour arrêter le serveur" -ForegroundColor Yellow
Write-Host ""

npm start
