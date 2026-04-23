@echo off
echo ========================================
echo   BUILD COMPLET - EDUSCHEDULE
echo   Construction de tous les services
echo ========================================

echo.
echo [INFO] Verification de Maven...
call mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Maven n'est pas installe ou non accessible
    echo Veuillez installer Maven et l'ajouter au PATH
    pause
    exit /b 1
)
echo [OK] Maven est installe

echo [INFO] Verification de Node.js...
call node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Node.js n'est pas installe
    echo Veuillez installer Node.js pour le frontend
    pause
    exit /b 1
)
echo [OK] Node.js est installe

echo.
echo [INFO] Construction du projet parent...
call mvn clean compile -DskipTests
if %errorlevel% neq 0 (
    echo [ERREUR] Echec de la construction du projet parent
    pause
    exit /b 1
)

echo.
echo [INFO] Construction des microservices individuels...

set services=eureka-server config-server api-gateway user-service resource-service school-service course-service scheduling-service reservation-service notification-service reporting-service calendar-service room-service event-service teacher-availability-service ai-service maintenance-service ent-integration-service

for %%s in (%services%) do (
    echo.
    echo [INFO] Construction de %%s...
    cd %%s
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo [ERREUR] Echec de la construction de %%s
        cd ..
        pause
        exit /b 1
    )
    cd ..
)

echo.
echo [INFO] Construction du frontend...
cd frontend
echo [INFO] Installation des dependances...
call npm install --legacy-peer-deps
if %errorlevel% neq 0 (
    echo [ERREUR] Echec de l'installation des dependances frontend
    cd ..
    pause
    exit /b 1
)

echo [INFO] Build du frontend...
call npm run build
if %errorlevel% neq 0 (
    echo [ERREUR] Echec du build frontend
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo ========================================
echo   BUILD TERMINE AVEC SUCCES !
echo ========================================
echo.
echo Tous les services ont ete construits:
echo.
echo 📦 MICROSERVICES BACKEND (17):
echo    - eureka-server
echo    - config-server  
echo    - api-gateway
echo    - user-service
echo    - resource-service
echo    - school-service
echo    - course-service
echo    - scheduling-service
echo    - reservation-service
echo    - notification-service
echo    - reporting-service
echo    - calendar-service
echo    - room-service
echo    - event-service
echo    - teacher-availability-service
echo    - ai-service
echo    - maintenance-service
echo    - ent-integration-service
echo.
echo 🌐 FRONTEND:
echo    - React/Next.js Application
echo.
echo Vous pouvez maintenant demarrer les services avec:
echo   start-all-services-complete.bat
echo.

pause