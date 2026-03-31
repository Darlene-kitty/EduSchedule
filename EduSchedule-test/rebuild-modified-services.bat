@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   REBUILD - SERVICES MODIFIES
echo   reservation-service, notification-service,
echo   course-service, resource-service, frontend-angular
echo ============================================================

REM ── Vérifications préalables ──────────────────────────────────
call mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Maven introuvable. Ajoutez-le au PATH.
    pause & exit /b 1
)

call node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Node.js introuvable.
    pause & exit /b 1
)

call docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Docker introuvable.
    pause & exit /b 1
)

echo [OK] Maven, Node.js et Docker sont disponibles.
echo.

REM ── 1. Build Maven des services modifiés ──────────────────────
set SERVICES=reservation-service notification-service course-service resource-service

for %%s in (%SERVICES%) do (
    echo [BUILD] %%s ...
    cd %%s
    call mvn clean package -DskipTests -q
    if !errorlevel! neq 0 (
        echo [ERREUR] Build Maven echoue pour %%s
        cd ..
        pause & exit /b 1
    )
    echo [OK] %%s - JAR genere
    cd ..
)

echo.

REM ── 2. Install npm + build Angular ────────────────────────────
echo [BUILD] frontend-angular - installation des dependances...
cd frontend-angular
call npm install --legacy-peer-deps
if %errorlevel% neq 0 (
    echo [ERREUR] npm install echoue
    cd ..
    pause & exit /b 1
)
echo [OK] Dependances npm installees (sockjs-client, @stomp/stompjs inclus)
cd ..

echo.

REM ── 3. Docker build des images modifiées ──────────────────────
echo [DOCKER] Reconstruction des images Docker...

docker compose build --no-cache reservation-service notification-service course-service resource-service frontend-angular
if %errorlevel% neq 0 (
    echo [ERREUR] docker compose build echoue
    pause & exit /b 1
)

echo.
echo ============================================================
echo   BUILD TERMINE
echo ============================================================
echo.
echo Pour redemarrer uniquement les services modifies :
echo   docker compose up -d reservation-service notification-service course-service resource-service frontend-angular
echo.
echo Pour tout redemarrer :
echo   docker compose up -d
echo.
pause
