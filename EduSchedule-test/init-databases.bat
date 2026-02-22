@echo off
echo ========================================
echo Initialisation des bases de donnees
echo ========================================
echo.

set MYSQL_PATH="C:\Program Files\MySQL\MySQL Workbench 8.0 CE\mysql.exe"

echo Entrez le mot de passe root MySQL:
set /p MYSQL_PASSWORD=

echo.
echo [1/5] Execution de init-db.sql...
%MYSQL_PATH% -u root -p%MYSQL_PASSWORD% --default-character-set=utf8mb4 < init-db.sql
if %errorlevel% neq 0 (
    echo ERREUR lors de l'execution de init-db.sql
    pause
    exit /b 1
)

echo [2/5] Execution de school-service-tables.sql...
%MYSQL_PATH% -u root -p%MYSQL_PASSWORD% iusjcdb < school-service-tables.sql
if %errorlevel% neq 0 (
    echo ERREUR lors de l'execution de school-service-tables.sql
    pause
    exit /b 1
)

echo [3/5] Execution de course-service-tables.sql...
%MYSQL_PATH% -u root -p%MYSQL_PASSWORD% iusjcdb < course-service-tables.sql
if %errorlevel% neq 0 (
    echo ERREUR lors de l'execution de course-service-tables.sql
    pause
    exit /b 1
)

echo [4/5] Execution de reservation-service-tables.sql...
%MYSQL_PATH% -u root -p%MYSQL_PASSWORD% eduschedule_reservation < reservation-service-tables.sql
if %errorlevel% neq 0 (
    echo ERREUR lors de l'execution de reservation-service-tables.sql
    pause
    exit /b 1
)

echo [5/5] Execution de scheduling-service-tables.sql...
%MYSQL_PATH% -u root -p%MYSQL_PASSWORD% eduschedule_scheduling < scheduling-service-tables.sql
if %errorlevel% neq 0 (
    echo ERREUR lors de l'execution de scheduling-service-tables.sql
    pause
    exit /b 1
)

echo.
echo ========================================
echo Initialisation terminee avec succes!
echo ========================================
pause
