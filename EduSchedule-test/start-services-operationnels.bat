@echo off
echo ========================================
echo DEMARRAGE SERVICES OPERATIONNELS
echo EduSchedule - Version Production
echo ========================================
echo.

echo Demarrage des services essentiels uniquement...
echo Services inclus: Eureka, User, Course, Reservation, Scheduling, Notification, Resource, School, API Gateway, Frontend
echo.

echo [1/10] Demarrage d'Eureka Server...
cd eureka-server
start "Eureka Server" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 30 secondes pour Eureka...
timeout /t 30 /nobreak

echo.
echo [2/10] Demarrage du User Service...
cd user-service
start "User Service" cmd /k "mvn spring-boot:run -Dspring-boot.run.profiles=dev"
cd ..
echo Attente de 25 secondes...
timeout /t 25 /nobreak

echo.
echo [3/10] Demarrage du Course Service...
cd course-service
start "Course Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [4/10] Demarrage du Reservation Service...
cd reservation-service
start "Reservation Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [5/10] Demarrage du Scheduling Service...
cd scheduling-service
start "Scheduling Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [6/10] Demarrage du Notification Service...
cd notification-service
start "Notification Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [7/10] Demarrage du Resource Service...
cd resource-service
start "Resource Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [8/10] Demarrage du School Service...
cd school-service
start "School Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [9/10] Demarrage de l'API Gateway...
cd api-gateway
start "API Gateway" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 25 secondes...
timeout /t 25 /nobreak

echo.
echo [10/10] Demarrage du Frontend...
cd frontend
start "Frontend React" cmd /k "npm run dev"
cd ..
echo Attente de 15 secondes...
timeout /t 15 /nobreak

echo.
echo ========================================
echo SERVICES OPERATIONNELS DEMARRES !
echo ========================================
echo.
echo Services Backend (8 services):
echo - Eureka Server:      http://localhost:8761
echo - User Service:       http://localhost:8081/actuator/health
echo - Course Service:     http://localhost:8084/actuator/health
echo - Reservation:        http://localhost:8085/actuator/health
echo - Scheduling:         http://localhost:8086/actuator/health
echo - Notification:       http://localhost:8082/actuator/health
echo - Resource Service:   http://localhost:8083/actuator/health
echo - School Service:     http://localhost:8087/actuator/health
echo - API Gateway:        http://localhost:8080/actuator/health
echo.
echo Frontend:
echo - Application Web:    http://localhost:3000
echo.
echo Base de donnees:
echo - H2 Console:         http://localhost:8081/h2-console
echo - MySQL (si config):  localhost:3306
echo.
echo Utilisateurs de test:
echo - admin / admin123 (ADMIN)
echo - teacher1 / admin123 (TEACHER)
echo - student1 / admin123 (STUDENT)
echo.
echo Scripts de test:
echo   .\test-all-services.ps1     - Test complet
echo   .\test-complete.ps1         - Test integration
echo.
echo Note: Attendez 2-3 minutes que tous les services
echo       s'enregistrent dans Eureka avant de tester.
echo.
echo Services non inclus (non operationnels):
echo - Teacher Availability Service (APIs integrees dans User Service)
echo - Reporting Service (skeleton seulement)
echo - AI Service (fonctionnalites experimentales)
echo - Config Server, Room Service, Calendar Service, Maintenance Service
echo   (utiliser start-services-complets.bat pour les inclure)
echo.
pause