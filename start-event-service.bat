@echo off
echo ========================================
echo  DEMARRAGE EVENT SERVICE (port 8089)
echo ========================================
echo.

REM Variables d'environnement
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/iusjcdb?useSSL=false^&serverTimezone=UTC^&allowPublicKeyRetrieval=true
set SPRING_DATASOURCE_USERNAME=iusjc
set SPRING_DATASOURCE_PASSWORD=iusjc2025
set EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
set SPRING_PROFILES_ACTIVE=dev

REM JVM options
set JVM_OPTS=-Xms48m -Xmx180m -XX:MetaspaceSize=48m -XX:MaxMetaspaceSize=112m -XX:+UseSerialGC -XX:TieredStopAtLevel=1

echo Demarrage du Event Service...
cd event-service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JVM_OPTS%"
