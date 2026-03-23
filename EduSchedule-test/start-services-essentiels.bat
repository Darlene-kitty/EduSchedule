@echo off
echo ========================================
echo  DEMARRAGE MINIMAL - SERVICES ESSENTIELS
echo  (user + school + course + room + gateway)
echo ========================================
echo.

set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/iusjcdb?useSSL=false^&serverTimezone=UTC^&allowPublicKeyRetrieval=true
set SPRING_DATASOURCE_USERNAME=iusjc
set SPRING_DATASOURCE_PASSWORD=iusjc2025
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
set SPRING_PROFILES_ACTIVE=dev
set JVM_OPTS=-Xms48m -Xmx180m -XX:MetaspaceSize=48m -XX:MaxMetaspaceSize=112m -XX:+UseSerialGC -XX:TieredStopAtLevel=1
set MVN_RUN=mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JVM_OPTS%"

REM ── INFRA DOCKER ───────────────────────────────────────────────────────────
echo [INFRA] Redis + RabbitMQ via Docker...
docker compose up -d redis rabbitmq
timeout /t 10 /nobreak > nul

REM ── 1. CONFIG SERVER ───────────────────────────────────────────────────────
echo [1/7] Config Server (port 8888)...
cd config-server
start "Config Server :8888" cmd /k "set SPRING_PROFILES_ACTIVE=native && %MVN_RUN%"
cd ..
echo Attente 20s pour Config Server...
timeout /t 20 /nobreak > nul

REM ── 2. EUREKA ──────────────────────────────────────────────────────────────
echo [2/7] Eureka Server (port 8761)...
cd eureka-server
start "Eureka :8761" cmd /k "%MVN_RUN%"
cd ..
echo Attente 35s pour Eureka...
timeout /t 35 /nobreak > nul

REM ── 3. USER SERVICE ────────────────────────────────────────────────────────
echo [3/7] User Service (port 8081)...
cd user-service
start "User :8081" cmd /k "%MVN_RUN%"
cd ..
timeout /t 25 /nobreak > nul

REM ── 3. SCHOOL SERVICE ──────────────────────────────────────────────────────
echo [3/6] School Service (port 8083)...
cd school-service
start "School :8083" cmd /k "%MVN_RUN%"
cd ..
timeout /t 20 /nobreak > nul

REM ── 4. COURSE SERVICE ──────────────────────────────────────────────────────
echo [4/6] Course Service (port 8084)...
cd course-service
start "Course :8084" cmd /k "%MVN_RUN%"
cd ..
timeout /t 20 /nobreak > nul

REM ── 5. ROOM SERVICE ────────────────────────────────────────────────────────
echo [5/6] Room Service (port 8095)...
cd room-service
start "Room :8095" cmd /k "%MVN_RUN%"
cd ..
timeout /t 20 /nobreak > nul

REM ── 6. API GATEWAY ─────────────────────────────────────────────────────────
echo [6/6] API Gateway (port 8080)...
cd api-gateway
start "Gateway :8080" cmd /k "%MVN_RUN%"
cd ..
timeout /t 25 /nobreak > nul

REM ── FRONTEND ───────────────────────────────────────────────────────────────
echo [FRONT] Angular (port 4200)...
cd frontend-angular
start "Angular :4200" cmd /k "ng serve"
cd ..

echo.
echo ========================================
echo  SERVICES ESSENTIELS DEMARRES
echo ========================================
echo   Eureka   : http://localhost:8761
echo   Gateway  : http://localhost:8080
echo   Angular  : http://localhost:4200
echo.
echo   admin    / admin123
echo   teacher1 / admin123
echo.
echo ~6 services x 200MB = ~1.2GB JVM
echo.
pause
