@echo off
echo ========================================
echo Arret de tous les services
echo ========================================

echo Fermeture des fenetres de services...

:: Fermer les services par nom de fenetre
taskkill /FI "WINDOWTITLE:Eureka Server*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE:User Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE:Notification Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE:Resource Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE:Course Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE:Reservation Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE:Scheduling Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE:School Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE:API Gateway*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE:Frontend React*" /T /F >nul 2>&1

:: Fermer les processus Java sur les ports specifiques
echo Arret des processus Java...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8761') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8096') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8082') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8083') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8084') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8085') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8086') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8087') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :3000') do taskkill /PID %%a /F >nul 2>&1

echo.
echo ========================================
echo Tous les services ont ete arretes !
echo ========================================
echo.
echo Ports liberes:
echo - 8761 (Eureka)
echo - 8096 (User Service)
echo - 8082 (Notification)
echo - 8083 (Resource)
echo - 8084 (Course)
echo - 8085 (Reservation)
echo - 8086 (Scheduling)
echo - 8087 (School)
echo - 8080 (API Gateway)
echo - 3000 (Frontend)
echo.
pause