Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   CORRECTION BASE DE DONNEES SCHEDULING" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "Création des tables pour scheduling-service..." -ForegroundColor Yellow

try {
    # Exécuter le script SQL
    $result = mysql -u iusjc -piusjc2025 -h localhost -e "source scheduling-service-tables.sql"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ Tables scheduling-service créées avec succès !" -ForegroundColor Green
        Write-Host ""
        Write-Host "Tables créées :" -ForegroundColor Green
        Write-Host "- schedules" -ForegroundColor White
        Write-Host "- time_slots" -ForegroundColor White
        Write-Host ""
        
        # Vérifier les tables
        Write-Host "Vérification des tables..." -ForegroundColor Yellow
        mysql -u iusjc -piusjc2025 -h localhost -e "USE iusjcdb; SHOW TABLES LIKE '%schedule%'; SHOW TABLES LIKE '%time_slot%';"
        
    } else {
        Write-Host ""
        Write-Host "❌ Erreur lors de la création des tables" -ForegroundColor Red
        Write-Host "Vérifiez que MySQL est démarré et accessible" -ForegroundColor Red
        Write-Host ""
    }
} catch {
    Write-Host ""
    Write-Host "❌ Erreur : $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Vérifiez que MySQL est installé et accessible" -ForegroundColor Red
    Write-Host ""
}

Write-Host "Appuyez sur une touche pour continuer..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")