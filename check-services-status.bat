@echo off
echo ========================================
echo Verification du statut des services
echo ========================================
echo.

echo Verification des ports utilises:
echo.

:: Fonction pour verifier un port
set "check_port="

echo [Eureka Server - 8761]
netstat -an | findstr :8761 >nul
if %errorlevel%==0 (
    echo   Status: ACTIF
) else (
    echo   Status: INACTIF
)

echo.
echo [User Service - 8096]
netstat -an | findstr :8096 >nul
if %errorlevel%==0 (
    echo   Status: ACTIF
) else (
    echo   Status: INACTIF
)

echo.
echo [Notification Service - 8082]
netstat -an | findstr :8082 >nul
if %errorlevel%==0 (
    echo   Status: ACTIF
) else (
    echo   Status: INACTIF
)

echo.
echo [Resource Service - 8083]
netstat -an | findstr :8083 >nul
if %errorlevel%==0 (
    echo   Status: ACTIF
) else (
    echo   Status: INACTIF
)

echo.
echo [Course Service - 8084]
netstat -an | findstr :8084 >nul
if %errorlevel%==0 (
    echo   Status: ACTIF
) else (
    echo   Status: INACTIF
)

echo.
echo [Reservation Service - 8085]
netstat -an | findstr :8085 >nul
if %errorlevel%==0 (
    echo   Status: ACTIF
) else (
    echo   Status: INACTIF
)

echo.
echo [Scheduling Service - 8086]
netstat -an | findstr :8086 >nul
if %errorlevel%==0 (
    echo   Status: ACTIF
) else (
    echo   Status: INACTIF
)

echo.
echo [School Service - 8087]
netstat -an | findstr :8087 >nul
if %errorlevel%==0 (
    echo   Status: ACTIF
) else (
    echo   Status: INACTIF
)

echo.
echo [API Gateway - 8080]
netstat -an | findstr :8080 >nul
if %errorlevel%==0 (
    echo   Status: ACTIF
) else (
    echo   Status: INACTIF
)

echo.
echo [Frontend React - 3000]
netstat -an | findstr :3000 >nul
if %errorlevel%==0 (
    echo   Status: ACTIF
) else (
    echo   Status: INACTIF
)

echo.
echo ========================================
echo Liens de verification:
echo ========================================
echo - Eureka Dashboard: http://localhost:8761
echo - API Gateway Health: http://localhost:8080/actuator/health
echo - Frontend: http://localhost:3000
echo.
echo Pour tester les services:
echo   .\test-all-services.ps1
echo.
pause