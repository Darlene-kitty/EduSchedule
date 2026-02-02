@echo off
echo ========================================
echo   CORRECTION BASE DE DONNEES SCHEDULING
echo ========================================

echo.
echo Creation des tables pour scheduling-service...

mysql -u iusjc -piusjc2025 -h localhost < scheduling-service-tables.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Tables scheduling-service creees avec succes !
    echo.
    echo Tables creees :
    echo - schedules
    echo - time_slots
    echo.
) else (
    echo.
    echo ❌ Erreur lors de la creation des tables
    echo Verifiez que MySQL est demarré et accessible
    echo.
)

pause