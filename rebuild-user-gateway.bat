@echo off
echo ========================================
echo  REBUILD CIBLE : 4 services modifies
echo    user-service    (primarySchoolId dans UserDTO)
echo    api-gateway     (route teacher-school-assignments)
echo    course-service  (teacherName dans CourseDTO)
echo    school-service  (couleur dans SchoolDTO)
echo ========================================
echo.
echo IMPORTANT : Ferme d'abord les fenetres de ces 4 services
echo avant de lancer ce script.
echo.
pause

set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/iusjcdb?useSSL=false^&serverTimezone=UTC^&allowPublicKeyRetrieval=true
set SPRING_DATASOURCE_USERNAME=iusjc
set SPRING_DATASOURCE_PASSWORD=iusjc2025
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
set SPRING_PROFILES_ACTIVE=dev
set JVM_OPTS=-Xms48m -Xmx180m -XX:MetaspaceSize=48m -XX:MaxMetaspaceSize=112m -XX:+UseSerialGC -XX:TieredStopAtLevel=1
set MVN_RUN=mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JVM_OPTS%"

echo [1/4] Build + demarrage User Service (port 8096)...
cd user-service
start "User Service :8096" cmd /k "mvn clean package -DskipTests -q && %MVN_RUN%"
cd ..
echo Attente 45s pour User Service...
timeout /t 45 /nobreak > nul

echo [2/4] Build + demarrage School Service (port 8083)...
cd school-service
start "School Service :8083" cmd /k "mvn clean package -DskipTests -q && %MVN_RUN%"
cd ..
echo Attente 30s pour School Service...
timeout /t 30 /nobreak > nul

echo [3/4] Build + demarrage Course Service (port 8084)...
cd course-service
start "Course Service :8084" cmd /k "mvn clean package -DskipTests -q && %MVN_RUN%"
cd ..
echo Attente 30s pour Course Service...
timeout /t 30 /nobreak > nul

echo [4/4] Build + demarrage API Gateway (port 8080)...
cd api-gateway
start "API Gateway :8080" cmd /k "mvn clean package -DskipTests -q && %MVN_RUN%"
cd ..
echo Attente 25s pour API Gateway...
timeout /t 25 /nobreak > nul

echo.
echo ========================================
echo  REBUILD TERMINE
echo ========================================
echo.
echo Verifiez les services :
echo   User Service   : http://localhost:8096/actuator/health
echo   School Service : http://localhost:8083/actuator/health
echo   Course Service : http://localhost:8084/actuator/health
echo   API Gateway    : http://localhost:8080/actuator/health
echo.
echo Verifiez Eureka : http://localhost:8761
echo.
echo NOTE : Si school-service etait deja demarre avec des donnees,
echo        la couleur ne sera pas mise a jour automatiquement
echo        (DataInitializer ne tourne qu'une fois).
echo        Pour forcer : vider la table schools en BDD.
echo.
pause
