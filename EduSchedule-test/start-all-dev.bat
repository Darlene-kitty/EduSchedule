@echo off
echo ========================================
echo Demarrage Complet - Mode Developpement
echo ========================================

echo [1/4] Demarrage d'Eureka Server...
cd eureka-server
start "Eureka Server" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 30 secondes pour Eureka...
timeout /t 30 /nobreak

echo.
echo [2/4] Demarrage du User Service (H2)...
cd user-service
start "User Service (H2)" cmd /k "mvn spring-boot:run -Dspring-boot.run.profiles=dev"
cd ..
echo Attente de 30 secondes...
timeout /t 30 /nobreak

echo.
echo [3/4] Demarrage de l'API Gateway...
cd api-gateway
start "API Gateway" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [4/4] Verification des services...
echo.
echo ========================================
echo Services demarres !
echo ========================================
echo.
echo Verifications:
echo - Eureka: http://localhost:8761
echo - User Service H2: http://localhost:8081/h2-console
echo - API Gateway: http://localhost:8080/actuator/health
echo - Frontend: http://localhost:3000
echo.
echo Utilisateurs de test:
echo - admin / admin123 (ADMIN)
echo - teacher1 / admin123 (TEACHER)
echo.
echo Test de connexion:
echo   .\test-login.ps1
echo.
pause