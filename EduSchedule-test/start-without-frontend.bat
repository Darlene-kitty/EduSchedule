@echo off
echo ========================================
echo Demarrage EduSchedule (sans frontend)
echo ========================================
echo.

echo Demarrage des services avec Docker Compose...
echo (Le frontend sera demarre separement)
echo.

docker-compose up -d mysql redis rabbitmq config-server eureka-server api-gateway user-service school-service course-service room-service reservation-service scheduling-service notification-service

echo.
echo ========================================
echo Services demarres!
echo ========================================
echo.
echo Services disponibles:
echo - MySQL: localhost:3306
echo - Redis: localhost:6379
echo - RabbitMQ: localhost:15672
echo - Eureka: http://localhost:8761
echo - API Gateway: http://localhost:8080
echo.
echo Pour demarrer le frontend separement:
echo   cd frontend
echo   npm run dev
echo.
pause
