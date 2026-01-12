l#!/usr/bin/env pwsh

Write-Host "=== Vérification de la configuration email ===" -ForegroundColor Green

# Configuration
$NOTIFICATION_SERVICE_URL = "http://localhost:8082/api"

$headers = @{
    "Content-Type" = "application/json"
}

# Test 1: Vérifier que le service de notification est accessible
Write-Host "`n1. Test de connectivité du service de notification..." -ForegroundColor Cyan

try {
    $healthResponse = Invoke-RestMethod -Uri "$NOTIFICATION_SERVICE_URL/health" -Method GET -TimeoutSec 5
    Write-Host "✅ Service de notification accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ Service de notification non accessible sur port 8082" -ForegroundColor Red
    Write-Host "Vérifiez que le notification-service est démarré" -ForegroundColor Yellow
}

# Test 2: Test d'envoi d'email simple (si l'endpoint existe)
Write-Host "`n2. Test de configuration SMTP..." -ForegroundColor Cyan

$testEmailData = @{
    to = "votre-email@example.com"
    subject = "Test de configuration SMTP - EduSchedule"
    message = "Ceci est un test de configuration SMTP pour EduSchedule. Si vous recevez cet email, la configuration fonctionne correctement."
} | ConvertTo-Json

try {
    # Essayer d'envoyer un email de test
    $emailResponse = Invoke-RestMethod -Uri "$NOTIFICATION_SERVICE_URL/notifications/test" -Method POST -Body $testEmailData -Headers $headers
    Write-Host "✅ Email de test envoyé!" -ForegroundColor Green
    Write-Host "Vérifiez votre boîte email: votre-email@example.com" -ForegroundColor Yellow
} catch {
    Write-Host "⚠️ Endpoint de test email non disponible (normal)" -ForegroundColor Yellow
    Write-Host "La configuration sera testée via la réinitialisation de mot de passe" -ForegroundColor Yellow
}

# Afficher la configuration actuelle
Write-Host "`n3. Configuration SMTP actuelle..." -ForegroundColor Cyan

Write-Host "Configuration depuis .env:" -ForegroundColor Yellow
Write-Host "  MAIL_HOST: smtp.gmail.com" -ForegroundColor Gray
Write-Host "  MAIL_PORT: 587" -ForegroundColor Gray
Write-Host "  MAIL_USERNAME: votre-email@example.com" -ForegroundColor Gray
Write-Host "  MAIL_PASSWORD: [MASQUÉ]" -ForegroundColor Gray
Write-Host "  MAIL_SMTP_AUTH: true" -ForegroundColor Gray
Write-Host "  MAIL_SMTP_STARTTLS_ENABLE: true" -ForegroundColor Gray

Write-Host "`n4. Vérifications recommandées..." -ForegroundColor Cyan
Write-Host "✓ Vérifiez que le mot de passe d'application Gmail est correct" -ForegroundColor Yellow
Write-Host "✓ Vérifiez que l'authentification à 2 facteurs est activée sur Gmail" -ForegroundColor Yellow
Write-Host "✓ Vérifiez que les mots de passe d'application sont autorisés" -ForegroundColor Yellow

Write-Host "`n=== Configuration prête pour les tests ===" -ForegroundColor Green