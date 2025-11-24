@echo off
echo ========================================
echo Verification des Services EduSchedule
echo ========================================
echo.

echo [1/5] Verification de MySQL...
curl -s http://localhost:3306 >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] MySQL semble accessible
) else (
    echo [ERREUR] MySQL n'est pas accessible sur le port 3306
)
echo.

echo [2/5] Verification d'Eureka Server...
curl -s http://localhost:8761 >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Eureka Server est accessible
) else (
    echo [ERREUR] Eureka Server n'est pas accessible sur le port 8761
)
echo.

echo [3/5] Verification de l'API Gateway...
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] API Gateway est accessible
    curl -s http://localhost:8080/actuator/health
) else (
    echo [ERREUR] API Gateway n'est pas accessible sur le port 8080
)
echo.

echo [4/5] Verification du User Service via Gateway...
curl -s -X OPTIONS http://localhost:8080/api/auth/login -H "Origin: http://localhost:3000" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] User Service est accessible via Gateway
) else (
    echo [ERREUR] User Service n'est pas accessible
)
echo.

echo [5/5] Verification du Frontend...
curl -s http://localhost:3000 >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Frontend est accessible
) else (
    echo [ERREUR] Frontend n'est pas accessible sur le port 3000
)
echo.

echo ========================================
echo Verification terminee
echo ========================================
echo.
echo Pour plus de details, visitez:
echo http://localhost:3000/test-connection
echo.
pause
