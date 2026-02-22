@echo off
echo ========================================
echo Construction des images Docker EduSchedule
echo ========================================
echo.

REM Vérifier que Docker est en cours d'exécution
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Docker n'est pas en cours d'execution. Veuillez demarrer Docker Desktop.
    exit /b 1
)

echo [INFO] Docker est operationnel
echo.

REM Construire le frontend d'abord
echo ========================================
echo [1/17] Construction du Frontend...
echo ========================================
cd frontend
if not exist ".next" (
    echo [INFO] Build Next.js necessaire...
    call npm run build
    if errorlevel 1 (
        echo [ERREUR] Echec du build frontend
        cd ..
        exit /b 1
    )
)
cd ..
echo [OK] Frontend pret
echo.

REM Construire tous les services Spring Boot
echo ========================================
echo [2/17] Construction des services Spring Boot...
echo ========================================
echo [INFO] Compilation Maven de tous les services...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERREUR] Echec de la compilation Maven
    exit /b 1
)
echo [OK] Tous les JARs sont construits
echo.

REM Construire les images Docker
echo ========================================
echo Construction des images Docker...
echo ========================================

set SERVICES=eureka-server config-server api-gateway user-service resource-service school-service course-service scheduling-service reservation-service notification-service reporting-service calendar-service room-service event-service teacher-availability-service ai-service maintenance-service ent-integration-service

set COUNT=3
for %%s in (%SERVICES%) do (
    echo [!COUNT!/17] Construction de %%s...
    docker build -t eduschedule-%%s:latest ./%%s
    if errorlevel 1 (
        echo [ERREUR] Echec de la construction de %%s
        exit /b 1
    )
    echo [OK] %%s construit avec succes
    echo.
    set /a COUNT+=1
)

REM Construire l'image frontend
echo [17/17] Construction du frontend...
docker build -t eduschedule-frontend:latest ./frontend
if errorlevel 1 (
    echo [ERREUR] Echec de la construction du frontend
    exit /b 1
)
echo [OK] Frontend construit avec succes
echo.

echo ========================================
echo CONSTRUCTION TERMINEE AVEC SUCCES
echo ========================================
echo.
echo Images Docker creees:
docker images | findstr eduschedule
echo.
echo Pour demarrer tous les services:
echo   docker-compose up -d
echo.
echo Pour voir les logs:
echo   docker-compose logs -f
echo.
