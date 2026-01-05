@echo off
echo ========================================
echo Demarrage du Frontend EduSchedule
echo ========================================
echo.

cd frontend

echo Verification de node_modules...
if not exist "node_modules\" (
    echo Installation des dependances...
    call npm install
) else (
    echo Dependances deja installees
)

echo.
echo Demarrage du serveur de developpement...
echo.
echo Le frontend sera accessible sur: http://localhost:3000
echo Page de test: http://localhost:3000/test-connection
echo.

call npm run dev
