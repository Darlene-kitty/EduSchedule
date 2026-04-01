@echo off
echo ========================================
echo  DEMARRAGE EDUSCHEDULE - DEV LOCAL
echo  MySQL Windows + Tous les services
echo ========================================
echo.
echo Prerequis : MySQL Windows sur port 3306
echo            Java 17+, Maven, Node.js, Angular CLI
echo            Docker Desktop demarre (pour Redis + RabbitMQ)
echo.

REM ── INFRAS DOCKER (Redis + RabbitMQ) ───────────────────────────────────────
echo [INFRA] Demarrage Redis et RabbitMQ via Docker...
docker compose up -d redis rabbitmq
echo Attente 15s pour Redis et RabbitMQ...
timeout /t 15 /nobreak > nul
echo.

REM ── Variables locales (override tout .env Docker) ─────────────────────────
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/iusjcdb?useSSL=false^&serverTimezone=UTC^&allowPublicKeyRetrieval=true
set SPRING_DATASOURCE_USERNAME=iusjc
set SPRING_DATASOURCE_PASSWORD=iusjc2025
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
set SPRING_PROFILES_ACTIVE=default
REM ── JVM flags limites memoire (7GB RAM, 16 services) ───────────────────────
set JVM_OPTS=-Xms48m -Xmx180m -XX:MetaspaceSize=48m -XX:MaxMetaspaceSize=112m -XX:+UseSerialGC -XX:TieredStopAtLevel=1
set MVN_RUN=mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JVM_OPTS%"

REM ── 1. CONFIG SERVER ───────────────────────────────────────────────────────
echo [1/16] Config Server (port 8888)...
netstat -ano | findstr ":8888" > nul 2>&1
if %errorlevel%==0 (
    echo [SKIP] Config Server deja en cours sur le port 8888.
    timeout /t 5 /nobreak > nul
) else (
    cd config-server
    start "Config Server :8888" cmd /k "set SPRING_PROFILES_ACTIVE=native && %MVN_RUN%"
    cd ..
    echo Attente 20s pour Config Server...
    timeout /t 20 /nobreak > nul
)

REM ── 2. EUREKA ──────────────────────────────────────────────────────────────
echo [2/16] Eureka Server (port 8761)...
netstat -ano | findstr ":8761" > nul 2>&1
if %errorlevel%==0 (
    echo [SKIP] Eureka deja en cours sur le port 8761.
    timeout /t 5 /nobreak > nul
) else (
    cd eureka-server
    start "Eureka Server :8761" cmd /k "%MVN_RUN%"
    cd ..
    echo Attente 30s pour Eureka...
    timeout /t 30 /nobreak > nul
)
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
set SPRING_PROFILES_ACTIVE=dev

REM ── 3. USER SERVICE ────────────────────────────────────────────────────────
echo [3/16] User Service (port 8081)...
cd user-service
start "User Service :8081" cmd /k "%MVN_RUN%"
cd ..
timeout /t 20 /nobreak > nul

REM ── 4. SCHOOL SERVICE ──────────────────────────────────────────────────────
echo [4/16] School Service (port 8083)...
cd school-service
start "School Service :8083" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 5. COURSE SERVICE ──────────────────────────────────────────────────────
echo [5/16] Course Service (port 8084)...
cd course-service
start "Course Service :8084" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 6. SCHEDULING SERVICE ──────────────────────────────────────────────────
echo [6/16] Scheduling Service (port 8085)...
cd scheduling-service
start "Scheduling Service :8085" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 7. RESERVATION SERVICE ─────────────────────────────────────────────────
echo [7/16] Reservation Service (port 8086)...
cd reservation-service
start "Reservation Service :8086" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 8. NOTIFICATION SERVICE ────────────────────────────────────────────────
echo [8/16] Notification Service (port 8087)...
cd notification-service
start "Notification Service :8087" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 9. REPORTING SERVICE ───────────────────────────────────────────────────
echo [9/16] Reporting Service (port 8091)...
cd reporting-service
start "Reporting Service :8091" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 10. CALENDAR SERVICE ───────────────────────────────────────────────────
echo [10/16] Calendar Service (port 8090)...
cd calendar-service
start "Calendar Service :8090" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 11. RESOURCE SERVICE ───────────────────────────────────────────────────
echo [11/16] Resource Service (port 8082)...
cd resource-service
start "Resource Service :8082" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 12. ROOM SERVICE ───────────────────────────────────────────────────────
echo [12/16] Room Service (port 8095)...
cd room-service
start "Room Service :8095" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 13. EVENT SERVICE ──────────────────────────────────────────────────────
echo [13/16] Event Service (port 8089)...
cd event-service
start "Event Service :8089" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 14. TEACHER AVAILABILITY SERVICE ───────────────────────────────────────
echo [14/16] Teacher Availability Service (port 8092)...
cd teacher-availability-service
start "Teacher Availability :8092" cmd /k "%MVN_RUN%"
cd ..
timeout /t 15 /nobreak > nul

REM ── 15. API GATEWAY ────────────────────────────────────────────────────────
echo [15/16] API Gateway (port 8080)...
cd api-gateway
start "API Gateway :8080" cmd /k "%MVN_RUN%"
cd ..
echo Attente 25s pour l'API Gateway...
timeout /t 25 /nobreak > nul

REM ── 16. FRONTEND ANGULAR ───────────────────────────────────────────────────
echo [16/16] Frontend Angular (port 4200)...
cd frontend-angular
start "Frontend Angular :4200" cmd /k "ng serve"
cd ..
timeout /t 10 /nobreak > nul

echo.
echo ========================================
echo  TOUS LES SERVICES DEMARRES !
echo ========================================
echo.
echo Infrastructure:
echo   Eureka Dashboard : http://localhost:8761
echo   API Gateway      : http://localhost:8080/actuator/health
echo.
echo Backend (ports 8081-8092):
echo   User Service         : http://localhost:8081/actuator/health
echo   Resource Service     : http://localhost:8082/actuator/health
echo   School Service       : http://localhost:8083/actuator/health
echo   Course Service       : http://localhost:8084/actuator/health
echo   Scheduling Service   : http://localhost:8085/actuator/health
echo   Reservation Service  : http://localhost:8086/actuator/health
echo   Notification Service : http://localhost:8087/actuator/health
echo   Reporting Service    : http://localhost:8091/actuator/health
echo   Calendar Service     : http://localhost:8090/actuator/health
echo   Room Service         : http://localhost:8095/actuator/health
echo   Event Service        : http://localhost:8089/actuator/health
echo   Teacher Availability : http://localhost:8092/actuator/health
echo.
echo Frontend:
echo   Angular App : http://localhost:4200
echo.
echo Comptes de test:
echo   admin    / admin123  (ADMIN   - acces complet)
echo   teacher1 / admin123  (TEACHER - acces limite)
echo   student1 / admin123  (STUDENT)
echo.
echo Base de donnees : MySQL Windows localhost:3306 / iusjcdb
echo Redis           : Docker localhost:6379
echo RabbitMQ        : Docker localhost:5672 (UI: http://localhost:15672)
echo.
echo Attendez 2-3 min que tous les services s'enregistrent dans Eureka.
echo Verifiez : http://localhost:8761
echo.
pause
