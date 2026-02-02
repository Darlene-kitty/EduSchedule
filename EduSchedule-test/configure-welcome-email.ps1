#!/usr/bin/env pwsh

Write-Host "=== Configuration du système d'email de bienvenue ===" -ForegroundColor Green

# Fonction pour configurer les variables SMTP
function Configure-SMTPSettings {
    Write-Host "`n--- Configuration SMTP ---" -ForegroundColor Yellow
    
    # Lire le fichier .env existant
    $envFile = ".env"
    $envContent = @()
    
    if (Test-Path $envFile) {
        $envContent = Get-Content $envFile
        Write-Host "✅ Fichier .env existant trouvé" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Fichier .env non trouvé, création..." -ForegroundColor Yellow
    }
    
    # Demander les informations SMTP
    Write-Host "`nConfiguration SMTP pour l'envoi d'emails :" -ForegroundColor Cyan
    
    $smtpHost = Read-Host "SMTP Host (défaut: smtp.gmail.com)"
    if ([string]::IsNullOrEmpty($smtpHost)) { $smtpHost = "smtp.gmail.com" }
    
    $smtpPort = Read-Host "SMTP Port (défaut: 587)"
    if ([string]::IsNullOrEmpty($smtpPort)) { $smtpPort = "587" }
    
    $smtpUsername = Read-Host "SMTP Username (email d'envoi)"
    $smtpPassword = Read-Host "SMTP Password (mot de passe d'application)" -AsSecureString
    $smtpPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($smtpPassword))
    
    # Mettre à jour ou ajouter les variables SMTP
    $smtpVars = @{
        "MAIL_HOST" = $smtpHost
        "MAIL_PORT" = $smtpPort
        "MAIL_USERNAME" = $smtpUsername
        "MAIL_PASSWORD" = $smtpPasswordPlain
        "MAIL_SMTP_AUTH" = "true"
        "MAIL_SMTP_STARTTLS_ENABLE" = "true"
    }
    
    foreach ($var in $smtpVars.GetEnumerator()) {
        $pattern = "^$($var.Key)="
        $newLine = "$($var.Key)=$($var.Value)"
        
        $found = $false
        for ($i = 0; $i -lt $envContent.Count; $i++) {
            if ($envContent[$i] -match $pattern) {
                $envContent[$i] = $newLine
                $found = $true
                break
            }
        }
        
        if (-not $found) {
            $envContent += $newLine
        }
    }
    
    # Sauvegarder le fichier .env
    $envContent | Out-File -FilePath $envFile -Encoding UTF8
    Write-Host "✅ Configuration SMTP sauvegardée dans .env" -ForegroundColor Green
}

# Fonction pour vérifier les templates d'email
function Verify-EmailTemplates {
    Write-Host "`n--- Vérification des templates d'email ---" -ForegroundColor Yellow
    
    $templatePath = "user-service/src/main/resources/templates"
    $templates = @(
        "welcome-email.html",
        "teacher-welcome-email.html", 
        "admin-welcome-email.html"
    )
    
    foreach ($template in $templates) {
        $fullPath = Join-Path $templatePath $template
        if (Test-Path $fullPath) {
            Write-Host "✅ Template trouvé: $template" -ForegroundColor Green
        } else {
            Write-Host "❌ Template manquant: $template" -ForegroundColor Red
        }
    }
}

# Fonction pour vérifier les dépendances Maven
function Verify-MavenDependencies {
    Write-Host "`n--- Vérification des dépendances Maven ---" -ForegroundColor Yellow
    
    $pomFile = "user-service/pom.xml"
    if (Test-Path $pomFile) {
        $pomContent = Get-Content $pomFile -Raw
        
        if ($pomContent -match "spring-boot-starter-mail") {
            Write-Host "✅ Dépendance spring-boot-starter-mail trouvée" -ForegroundColor Green
        } else {
            Write-Host "❌ Dépendance spring-boot-starter-mail manquante" -ForegroundColor Red
        }
        
        if ($pomContent -match "spring-boot-starter-thymeleaf") {
            Write-Host "✅ Dépendance spring-boot-starter-thymeleaf trouvée" -ForegroundColor Green
        } else {
            Write-Host "❌ Dépendance spring-boot-starter-thymeleaf manquante" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Fichier pom.xml non trouvé" -ForegroundColor Red
    }
}

# Fonction pour afficher les instructions Gmail
function Show-GmailInstructions {
    Write-Host "`n--- Instructions pour Gmail ---" -ForegroundColor Yellow
    Write-Host "Pour utiliser Gmail comme serveur SMTP :" -ForegroundColor White
    Write-Host "1. Activez la validation en 2 étapes sur votre compte Google" -ForegroundColor White
    Write-Host "2. Générez un mot de passe d'application :" -ForegroundColor White
    Write-Host "   - Allez sur https://myaccount.google.com/security" -ForegroundColor White
    Write-Host "   - Cliquez sur 'Mots de passe d'application'" -ForegroundColor White
    Write-Host "   - Générez un mot de passe pour 'Mail'" -ForegroundColor White
    Write-Host "3. Utilisez ce mot de passe d'application (pas votre mot de passe Gmail)" -ForegroundColor White
    Write-Host "4. Configuration recommandée :" -ForegroundColor White
    Write-Host "   - Host: smtp.gmail.com" -ForegroundColor White
    Write-Host "   - Port: 587" -ForegroundColor White
    Write-Host "   - Username: votre.email@gmail.com" -ForegroundColor White
    Write-Host "   - Password: mot de passe d'application généré" -ForegroundColor White
}

# Exécution principale
Write-Host "Configuration du système d'email de bienvenue..." -ForegroundColor Cyan

# 1. Vérifier les templates
Verify-EmailTemplates

# 2. Vérifier les dépendances Maven
Verify-MavenDependencies

# 3. Afficher les instructions Gmail
Show-GmailInstructions

# 4. Configurer SMTP
$configure = Read-Host "`nVoulez-vous configurer les paramètres SMTP maintenant ? (y/N)"
if ($configure -eq "y" -or $configure -eq "Y") {
    Configure-SMTPSettings
}

Write-Host "`n=== Configuration terminée ===" -ForegroundColor Green
Write-Host "Prochaines étapes :" -ForegroundColor White
Write-Host "1. Redémarrez le user-service pour prendre en compte la configuration" -ForegroundColor White
Write-Host "2. Testez avec: .\test-welcome-email.ps1" -ForegroundColor White
Write-Host "3. Vérifiez les emails dans les boîtes de réception" -ForegroundColor White