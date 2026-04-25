@echo off
REM ============================================================
REM rebuild-modified-services.bat
REM Rebuild uniquement les services modifies depuis le dernier commit
REM Services Java : school, scheduling, resource, api-gateway,
REM                 course, notification, reporting, event
REM Frontend      : frontend-angular
REM ============================================================

echo.
echo ===================================================
echo  REBUILD DES SERVICES MODIFIES
echo ===================================================
echo.

REM ── Verifier Maven ──────────────────────────────────────────
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Maven introuvable. Ajoute mvn au PATH.
    pause
    exit /b 1
)

REM ── Verifier Node/npm ───────────────────────────────────────
where npm >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] npm introuvable. Installe Node.js.
    pause
    exit /b 1
)

set FAILED=0
set BUILT=0

REM ============================================================
REM  SERVICES JAVA
REM ============================================================

echo [1/8] school-service  (GroupeDTO + AffectationDTO enrichis)
cd school-service
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 ( echo [ECHEC] school-service & set FAILED=1 ) else ( echo [OK] school-service & set /a BUILT+=1 )
cd ..

echo [2/8] scheduling-service  (teacherId dans Schedule/ScheduleDTO)
cd scheduling-service
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 ( echo [ECHEC] scheduling-service & set FAILED=1 ) else ( echo [OK] scheduling-service & set /a BUILT+=1 )
cd ..

echo [3/8] resource-service  (schoolId dans Salle)
cd resource-service
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 ( echo [ECHEC] resource-service & set FAILED=1 ) else ( echo [OK] resource-service & set /a BUILT+=1 )
cd ..

echo [4/8] api-gateway  (route /api/v1/etudiants/**)
cd api-gateway
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 ( echo [ECHEC] api-gateway & set FAILED=1 ) else ( echo [OK] api-gateway & set /a BUILT+=1 )
cd ..

echo [5/8] course-service  (assignGroup endpoint)
cd course-service
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 ( echo [ECHEC] course-service & set FAILED=1 ) else ( echo [OK] course-service & set /a BUILT+=1 )
cd ..

echo [6/8] notification-service
cd notification-service
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 ( echo [ECHEC] notification-service & set FAILED=1 ) else ( echo [OK] notification-service & set /a BUILT+=1 )
cd ..

echo [7/8] reporting-service
cd reporting-service
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 ( echo [ECHEC] reporting-service & set FAILED=1 ) else ( echo [OK] reporting-service & set /a BUILT+=1 )
cd ..

echo [8/8] event-service
cd event-service
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 ( echo [ECHEC] event-service & set FAILED=1 ) else ( echo [OK] event-service & set /a BUILT+=1 )
cd ..

REM ============================================================
REM  FRONTEND ANGULAR
REM ============================================================

echo.
echo [9/9] frontend-angular  (build production)
cd frontend-angular
call npm run build -- --configuration production
if %errorlevel% neq 0 ( echo [ECHEC] frontend-angular & set FAILED=1 ) else ( echo [OK] frontend-angular & set /a BUILT+=1 )
cd ..

REM ============================================================
REM  BILAN
REM ============================================================

echo.
echo ===================================================
echo  BILAN : %BUILT%/9 services rebuildes avec succes
echo ===================================================

if %FAILED% neq 0 (
    echo.
    echo [ATTENTION] Certains services ont echoue.
    echo Corrige les erreurs avant de pusher.
    pause
    exit /b 1
)

echo.
echo Tous les services sont prets.
echo Tu peux maintenant pusher : git push origin main
echo.
pause
