@echo off
echo ========================================
echo Test des API EduSchedule
echo ========================================
echo.

set API_URL=http://localhost:8080

echo [Test 1] Health Check API Gateway
curl -s %API_URL%/actuator/health
echo.
echo.

echo [Test 2] CORS Preflight - Port 3000
curl -s -X OPTIONS %API_URL%/api/auth/login ^
  -H "Origin: http://localhost:3000" ^
  -H "Access-Control-Request-Method: POST" ^
  -v
echo.
echo.

echo [Test 3] CORS Preflight - Port 3001
curl -s -X OPTIONS %API_URL%/api/auth/login ^
  -H "Origin: http://localhost:3001" ^
  -H "Access-Control-Request-Method: POST" ^
  -v
echo.
echo.

echo [Test 4] Login avec username (devrait echouer avec 401)
curl -s -X POST %API_URL%/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"test\",\"password\":\"test\"}"
echo.
echo.

echo [Test 5] Register (structure correcte)
curl -s -X POST %API_URL%/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"test.user\",\"email\":\"test@example.com\",\"password\":\"password123\",\"role\":\"TEACHER\"}"
echo.
echo.

echo ========================================
echo Tests termines
echo ========================================
echo.
echo Si vous voyez des erreurs CORS, verifiez:
echo 1. API Gateway est demarre
echo 2. User Service est demarre
echo 3. Configuration CORS dans SecurityConfig.java
echo.
pause
