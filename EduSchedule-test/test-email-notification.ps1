# Test d'envoi d'email via le notification-service
# Usage: .\test-email-notification.ps1

Write-Host "🧪 Test d'envoi d'email via notification-service" -ForegroundColor Cyan
Write-Host ""

# Vérifier que le service est démarré
Write-Host "1️⃣ Vérification du service..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8087/actuator/health" -Method Get -ErrorAction Stop
    if ($health.status -eq "UP") {
        Write-Host "   ✅ notification-service est UP" -ForegroundColor Green
    } else {
        Write-Host "   ❌ notification-service n'est pas UP" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "   ❌ Impossible de joindre notification-service sur le port 8087" -ForegroundColor Red
    Write-Host "   Démarrez-le avec: docker-compose up -d notification-service" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "2️⃣ Envoi d'un email de test..." -ForegroundColor Yellow

# Email de destination (modifiez avec votre email)
$destinataire = Read-Host "Entrez l'email de destination (ex: votre.email@gmail.com)"

if ([string]::IsNullOrWhiteSpace($destinataire)) {
    Write-Host "   ❌ Email de destination requis" -ForegroundColor Red
    exit 1
}

$body = @{
    recipient = $destinataire
    subject = "🧪 Test EduSchedule - Notification Email"
    message = "Ceci est un email de test envoyé depuis le système EduSchedule.`n`nSi vous recevez ce message, la configuration SMTP fonctionne correctement ! ✅`n`nDate: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    type = "EMAIL"
    priority = "NORMAL"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8087/api/v1/notifications" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body `
        -ErrorAction Stop
    
    Write-Host "   ✅ Notification créée avec succès" -ForegroundColor Green
    Write-Host "   📧 Email envoyé à: $destinataire" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📬 Vérifiez votre boîte de réception (et les spams si besoin)" -ForegroundColor Yellow
    Write-Host ""
    
} catch {
    Write-Host "   ❌ Erreur lors de l'envoi" -ForegroundColor Red
    Write-Host "   Détails: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "💡 Vérifiez les logs du service:" -ForegroundColor Yellow
    Write-Host "   docker-compose logs notification-service | Select-String -Pattern 'mail'" -ForegroundColor Gray
    exit 1
}

Write-Host "3️⃣ Vérification des logs du service..." -ForegroundColor Yellow
Write-Host ""
docker-compose logs --tail=20 notification-service | Select-String -Pattern "mail|Email|SMTP" -CaseSensitive:$false

Write-Host ""
Write-Host "✅ Test terminé" -ForegroundColor Green
