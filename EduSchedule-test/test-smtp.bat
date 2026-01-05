@echo off
echo ========================================
echo Test Configuration SMTP - EduSchedule
echo ========================================
echo.

set API_URL=http://localhost:8080

echo [1/3] Verification de la configuration SMTP dans .env...
findstr /C:"MAIL_HOST" .env >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Configuration SMTP trouvee
    findstr "MAIL_HOST MAIL_USERNAME" .env
) else (
    echo [ERREUR] Configuration SMTP non trouvee dans .env
    echo.
    echo Ajoutez les variables suivantes dans .env:
    echo MAIL_HOST=smtp.gmail.com
    echo MAIL_PORT=587
    echo MAIL_USERNAME=votre-email@gmail.com
    echo MAIL_PASSWORD=votre-mot-de-passe-app
    pause
    exit /b 1
)
echo.

echo [2/3] Verification du Notification Service...
curl -s %API_URL%/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] API Gateway accessible
) else (
    echo [ERREUR] API Gateway non accessible
    echo Demarrez l'API Gateway sur le port 8080
    pause
    exit /b 1
)
echo.

echo [3/3] Envoi d'un email de test...
echo.
set /p EMAIL="Entrez l'adresse email de destination: "

if "%EMAIL%"=="" (
    echo [ERREUR] Adresse email requise
    pause
    exit /b 1
)

echo.
echo Envoi de l'email de test a %EMAIL%...
echo.

curl -X POST %API_URL%/api/v1/notifications/test-email ^
  -H "Content-Type: application/json" ^
  -d "{\"recipient\":\"%EMAIL%\",\"subject\":\"Test SMTP EduSchedule\",\"message\":\"Ceci est un email de test. Si vous recevez cet email, la configuration SMTP fonctionne correctement!\"}"

echo.
echo.
echo ========================================
echo Test termine
echo ========================================
echo.
echo Verifications:
echo 1. Verifiez votre boite de reception: %EMAIL%
echo 2. Verifiez le dossier spam/courrier indesirable
echo 3. Verifiez les logs du notification-service
echo.
echo Si l'email n'est pas recu:
echo - Verifiez la configuration SMTP dans .env
echo - Verifiez que le notification-service est demarre
echo - Consultez CONFIGURATION_SMTP.md pour plus d'aide
echo.
pause
