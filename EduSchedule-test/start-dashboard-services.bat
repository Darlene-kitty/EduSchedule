@echo off
echo === Demarrage des services pour le tableau de bord ===

echo.
echo 1. Demarrage du service utilisateur (port 8081)...
start "User Service" cmd /k "cd user-service && mvn spring-boot:run"

echo.
echo 2. Attente de 10 secondes...
timeout /t 10 /nobreak

echo.
echo 3. Demarrage du service de reservation (port 8082)...
start "Reservation Service" cmd /k "cd reservation-service && mvn spring-boot:run"

echo.
echo 4. Attente de 10 secondes...
timeout /t 10 /nobreak

echo.
echo 5. Demarrage du service de ressources (port 8083)...
start "Resource Service" cmd /k "cd resource-service && mvn spring-boot:run"

echo.
echo === Services en cours de demarrage ===
echo User Service: http://localhost:8081
echo Reservation Service: http://localhost:8082 (contient les analytics)
echo Resource Service: http://localhost:8083
echo.
echo Attendez 30-60 secondes que tous les services soient prets.
echo Ensuite, demarrez le frontend avec: cd frontend && npm run dev
echo.
pause