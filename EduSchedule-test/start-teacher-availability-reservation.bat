@echo off
echo ========================================
echo DEMARRAGE TEACHER AVAILABILITY + RESERVATION
echo Services sans Docker - Configuration MySQL locale
echo ========================================
echo.

echo Services à démarrer:
echo - Eureka Server (port 8761)
echo - Teacher Availability Service (port 8089)
echo - Reservation Service (port 8085)
echo - API Gateway (port 8080)
echo.

echo [1/4] Démarrage d'Eureka Server...
cd eureka-server
start "Eureka Server" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 30 secondes pour Eureka...
timeout /t 30 /nobreak

echo.
echo [2/4] Démarrage du Teacher Availability Service...
cd teacher-availability-service
start "Teacher Availability Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 25 secondes...
timeout /t 25 /nobreak

echo.
echo [3/4] Démarrage du Reservation Service...
cd reservation-service
start "Reservation Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 25 secondes...
timeout /t 25 /nobreak

echo.
echo [4/4] Démarrage de l'API Gateway...
cd api-gateway
start "API Gateway" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo ========================================
echo SERVICES DÉMARRÉS !
echo ========================================
echo.
echo Services Infrastructure:
echo - Eureka Server:      http://localhost:8761
echo - API Gateway:        http://localhost:8080/actuator/health
echo.
echo Services Métier:
echo - Teacher Availability: http://localhost:8089/actuator/health
echo - Reservation Service:  http://localhost:8085/actuator/health
echo.
echo APIs disponibles via Gateway:
echo - Teacher Availability: http://localhost:8080/api/teacher-availability/health
echo - Reservations:         http://localhost:8080/api/v1/reservations/
echo.
echo Configuration requise:
echo - MySQL sur localhost:3306
echo - Base de données: iusjcdb
echo - Utilisateur: root / root
echo.
echo Test des services:
echo   .\test-teacher-availability-reservation.ps1
echo.
echo Note: Attendez 2-3 minutes que tous les services
echo       s'enregistrent dans Eureka avant de tester.
echo.
pause