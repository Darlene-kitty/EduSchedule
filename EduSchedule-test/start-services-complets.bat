@echo off
echo ========================================
echo DEMARRAGE SERVICES COMPLETS
echo EduSchedule - Version Complete avec Services Additionnels
echo ========================================
echo.

echo Demarrage de tous les services operationnels + services additionnels...
echo Services inclus: Config Server, Eureka, User, Course, Reservation, Scheduling, Notification, Resource, School, Room, Calendar, Teacher Availability, Maintenance, API Gateway, Frontend
echo.

echo [1/13] Demarrage du Config Server...
cd config-server
start "Config Server" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 25 secondes pour Config Server...
timeout /t 25 /nobreak

echo.
echo [2/13] Demarrage d'Eureka Server...
cd eureka-server
start "Eureka Server" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 30 secondes pour Eureka...
timeout /t 30 /nobreak

echo.
echo [3/13] Demarrage du User Service...
cd user-service
start "User Service" cmd /k "mvn spring-boot:run -Dspring-boot.run.profiles=dev"
cd ..
echo Attente de 25 secondes...
timeout /t 25 /nobreak

echo.
echo [4/13] Demarrage du Course Service...
cd course-service
start "Course Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [5/13] Demarrage du Room Service...
cd room-service
start "Room Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [6/13] Demarrage du Reservation Service...
cd reservation-service
start "Reservation Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [7/13] Demarrage du Scheduling Service...
cd scheduling-service
start "Scheduling Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [8/13] Demarrage du Calendar Service...
cd calendar-service
start "Calendar Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [9/13] Demarrage du Notification Service...
cd notification-service
start "Notification Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [10/13] Demarrage du Resource Service...
cd resource-service
start "Resource Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [11/13] Demarrage du School Service...
cd school-service
start "School Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [12/14] Demarrage du Teacher Availability Service...
cd teacher-availability-service
start "Teacher Availability Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [13/14] Demarrage du Maintenance Service...
cd maintenance-service
start "Maintenance Service" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 20 secondes...
timeout /t 20 /nobreak

echo.
echo [14/14] Demarrage de l'API Gateway...
cd api-gateway
start "API Gateway" cmd /k "mvn spring-boot:run"
cd ..
echo Attente de 25 secondes...
timeout /t 25 /nobreak

echo.
echo [BONUS] Demarrage du Frontend...
cd frontend
start "Frontend React" cmd /k "npm run dev"
cd ..
echo Attente de 15 secondes...
timeout /t 15 /nobreak

echo.
echo ========================================
echo SERVICES COMPLETS DEMARRES !
echo ========================================
echo.
echo Services Infrastructure:
echo - Config Server:      http://localhost:8888/actuator/health
echo - Eureka Server:      http://localhost:8761
echo.
echo Services Backend (13 services):
echo - User Service:       http://localhost:8081/actuator/health
echo - Course Service:     http://localhost:8084/actuator/health
echo - Room Service:       http://localhost:8088/actuator/health
echo - Reservation:        http://localhost:8085/actuator/health
echo - Scheduling:         http://localhost:8086/actuator/health
echo - Calendar Service:   http://localhost:8090/actuator/health
echo - Notification:       http://localhost:8082/actuator/health
echo - Resource Service:   http://localhost:8083/actuator/health
echo - School Service:     http://localhost:8087/actuator/health
echo - Teacher Availability: http://localhost:8089/actuator/health
echo - Maintenance:        http://localhost:8091/actuator/health
echo - API Gateway:        http://localhost:8080/actuator/health
echo.
echo Frontend:
echo - Application Web:    http://localhost:3000
echo.
echo APIs Teacher Availability (via API Gateway):
echo - Health Check:       http://localhost:8080/api/teacher-availability/health
echo - Statistiques:       http://localhost:8080/api/teacher-availability/stats
echo - Creer disponibilite: POST http://localhost:8080/api/teacher-availability
echo - Lister par enseignant: GET http://localhost:8080/api/teacher-availability/teacher/{id}
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
echo Fonctionnalites disponibles:
echo - Gestion des disponibilites enseignants (Teacher Availability Service + User Service)
echo - Suggestions de salles optimales (Room Service)
echo - Integration calendrier Google (Calendar Service)
echo - Planification automatisee (Scheduling Service)
echo - Maintenance systeme (Maintenance Service)
echo - Configuration centralisee (Config Server)
echo - Reservations avancees (Reservation Service)
echo.
echo Scripts de test:
echo   .\test-all-services.ps1     - Test complet
echo   .\test-complete.ps1         - Test integration
echo   .\test-advanced-features.ps1 - Test fonctionnalites avancees
echo   .\test-teacher-availability-reservation.ps1 - Test Teacher Availability + Reservation
echo.
echo Note: Attendez 3-4 minutes que tous les services
echo       s'enregistrent dans Eureka avant de tester.
echo.
echo Services non inclus:
echo - Reporting Service (skeleton seulement)
echo - AI Service (fonctionnalites experimentales)
echo - ENT Integration Service (en developpement)
echo.
echo Note importante:
echo - Teacher Availability Service ET User Service sont tous deux demarres
echo - Teacher Availability Service (port 8089) pour fonctionnalites avancees
echo - User Service (port 8081) pour compatibilite et authentification
echo - API Gateway route /api/teacher-availability/** vers Teacher Availability Service
echo - API Gateway route /api/users/** et /api/auth/** vers User Service
echo.
pause