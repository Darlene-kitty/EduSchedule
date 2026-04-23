# ============================================================
# EduSchedule — Démarrage avec Cloudflare Tunnel
# ============================================================
# Usage: .\cloudflare\start-with-tunnel.ps1
# ============================================================

Write-Host "=== EduSchedule — Démarrage ===" -ForegroundColor Cyan

# 1. Démarrer les services Docker
Write-Host "`n[1/3] Démarrage des services Docker..." -ForegroundColor Yellow
docker compose up -d

# Attendre que les services essentiels soient prêts
Write-Host "Attente du démarrage (60s)..." -ForegroundColor Gray
Start-Sleep -Seconds 60

# Vérifier l'état
$running = docker compose ps --format json | ConvertFrom-Json | Where-Object { $_.State -eq "running" }
Write-Host "Services actifs : $($running.Count)" -ForegroundColor Green

# 2. Démarrer le tunnel Cloudflare
Write-Host "`n[2/3] Démarrage du tunnel Cloudflare..." -ForegroundColor Yellow
Write-Host "Config : cloudflare/config.yml" -ForegroundColor Gray

# Lancer cloudflared en arrière-plan
Start-Process -FilePath "cloudflared" `
    -ArgumentList "tunnel", "--config", "cloudflare/config.yml", "run" `
    -WindowStyle Minimized

Start-Sleep -Seconds 5

# 3. Afficher les URLs
Write-Host "`n[3/3] Application disponible :" -ForegroundColor Green
Write-Host ""
Write-Host "  Frontend  : https://app.tondomaine.com" -ForegroundColor Cyan
Write-Host "  API       : https://api.tondomaine.com" -ForegroundColor Cyan
Write-Host "  WebSocket : wss://ws.tondomaine.com" -ForegroundColor Cyan
Write-Host ""
Write-Host "Appuyer sur Ctrl+C pour arrêter le tunnel." -ForegroundColor Gray
Write-Host "Pour arrêter Docker : docker compose down" -ForegroundColor Gray
