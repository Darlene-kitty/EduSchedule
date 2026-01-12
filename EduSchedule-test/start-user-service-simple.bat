@echo off
echo ========================================
echo Demarrage User Service Simplifie
echo ========================================

cd user-service

echo Configuration H2 activee...
echo Demarrage avec profil dev...

mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Dserver.port=8081 -Deureka.client.enabled=false"

pause