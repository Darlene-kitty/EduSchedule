@echo off
echo ========================================
echo Demarrage des Services Backend
echo ========================================
echo.

echo IMPORTANT: Ce script demarre les services dans des fenetres separees
echo Assurez-vous que MySQL est deja demarre!
echo.
pause

echo [1/4] Demarrage d'Eureka Server...
start "Eureka Server" cmd /k "cd eureka-server && mvn spring-boot:run"
echo Attente de 30 secondes pour Eureka...
timeout /t 30 /nobreak

echo.
echo [2/4] Demarrage du Config Server...
start "Config Server" cmd /k "cd config-server && mvn spring-boot:run"
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [3/4] Demarrage du User Service...
start "User Service" cmd /k "cd user-service && mvn spring-boot:run"
echo Attente de 30 secondes...
timeout /t 30 /nobreak

echo.
echo [4/4] Demarrage de l'API Gateway...
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo ========================================
echo Tous les services sont en cours de demarrage!
echo ========================================
echo.
echo Verifications:
echo - Eureka: http://localhost:8761
echo - API Gateway: http://localhost:8080/actuator/health
echo.
echo Pour demarrer le frontend:
echo   cd frontend
echo   npm run dev
echo.
echo Pour tester la connexion:
echo   http://localhost:3000/test-connection
echo.
pause
