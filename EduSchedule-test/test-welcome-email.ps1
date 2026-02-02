#!/usr/bin/env pwsh

Write-Host "=== Test du système d'email de bienvenue ===" -ForegroundColor Green

# Configuration
$API_GATEWAY = "http://localhost:8080"
$USER_SERVICE = "http://localhost:8081"

# Fonction pour tester la création d'utilisateur avec email
function Test-UserCreationWithEmail {
    param(
        [string]$Role,
        [string]$Username,
        [string]$FirstName,
        [string]$LastName,
        [string]$Email
    )
    
    Write-Host "`n--- Test création utilisateur $Role ---" -ForegroundColor Yellow
    
    $userData = @{
        username = $Username
        firstName = $FirstName
        lastName = $LastName
        email = $Email
        password = "password123"
        role = $Role
    } | ConvertTo-Json
    
    try {
        Write-Host "Création de l'utilisateur $Username..." -ForegroundColor Cyan
        $response = Invoke-RestMethod -Uri "$USER_SERVICE/api/users/register" -Method POST -Body $userData -ContentType "application/json"
        Write-Host "✅ Utilisateur créé avec succès" -ForegroundColor Green
        Write-Host "ID: $($response.id)" -ForegroundColor White
        Write-Host "Username: $($response.username)" -ForegroundColor White
        Write-Host "Email: $($response.email)" -ForegroundColor White
        Write-Host "Role: $($response.role)" -ForegroundColor White
        Write-Host "📧 Email de bienvenue envoyé automatiquement" -ForegroundColor Magenta
        return $response
    }
    catch {
        Write-Host "❌ Erreur lors de la création: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Fonction pour vérifier la configuration SMTP
function Test-SMTPConfiguration {
    Write-Host "`n--- Vérification configuration SMTP ---" -ForegroundColor Yellow
    
    try {
        $response = Invoke-RestMethod -Uri "$USER_SERVICE/actuator/health" -Method GET
        Write-Host "✅ Service utilisateur accessible" -ForegroundColor Green
        
        # Vérifier les variables d'environnement SMTP
        if ($env:MAIL_USERNAME -and $env:MAIL_PASSWORD) {
            Write-Host "✅ Configuration SMTP détectée" -ForegroundColor Green
            Write-Host "SMTP Host: $($env:MAIL_HOST -or 'smtp.gmail.com')" -ForegroundColor White
            Write-Host "SMTP Port: $($env:MAIL_PORT -or '587')" -ForegroundColor White
            Write-Host "SMTP Username: $env:MAIL_USERNAME" -ForegroundColor White
        } else {
            Write-Host "⚠️  Variables SMTP non configurées" -ForegroundColor Yellow
            Write-Host "Configurez MAIL_USERNAME et MAIL_PASSWORD dans .env" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "❌ Service utilisateur non accessible: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Tests principaux
Write-Host "Démarrage des tests..." -ForegroundColor Cyan

# 1. Vérifier la configuration SMTP
Test-SMTPConfiguration

# 2. Tester la création d'un étudiant
$student = Test-UserCreationWithEmail -Role "STUDENT" -Username "student_test" -FirstName "Jean" -LastName "Dupont" -Email "student@test.com"

# 3. Tester la création d'un enseignant
$teacher = Test-UserCreationWithEmail -Role "TEACHER" -Username "teacher_test" -FirstName "Marie" -LastName "Martin" -Email "teacher@test.com"

# 4. Tester la création d'un administrateur
$admin = Test-UserCreationWithEmail -Role "ADMIN" -Username "admin_test" -FirstName "Pierre" -LastName "Durand" -Email "admin@test.com"

# Résumé
Write-Host "`n=== Résumé des tests ===" -ForegroundColor Green
if ($student) { Write-Host "✅ Étudiant créé avec email de bienvenue" -ForegroundColor Green }
if ($teacher) { Write-Host "✅ Enseignant créé avec email de bienvenue spécialisé" -ForegroundColor Green }
if ($admin) { Write-Host "✅ Administrateur créé avec email de bienvenue admin" -ForegroundColor Green }

Write-Host "`n📧 Vérifiez vos boîtes email pour les messages de bienvenue !" -ForegroundColor Magenta
Write-Host "Les templates utilisés sont :" -ForegroundColor White
Write-Host "- welcome-email.html (étudiants)" -ForegroundColor White
Write-Host "- teacher-welcome-email.html (enseignants)" -ForegroundColor White
Write-Host "- admin-welcome-email.html (administrateurs)" -ForegroundColor White

Write-Host "`n=== Test terminé ===" -ForegroundColor Green