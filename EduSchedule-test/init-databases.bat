@echo off
setlocal EnableDelayedExpansion
echo ========================================
echo  EduSchedule - Initialisation DB
echo ========================================
echo.

REM --- Détection automatique de mysql.exe ---
set MYSQL_EXE=
for %%P in (
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    "C:\Program Files\MySQL\MySQL Workbench 8.0 CE\mysql.exe"
    "C:\xampp\mysql\bin\mysql.exe"
    "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe"
) do (
    if exist %%P (
        set MYSQL_EXE=%%P
        goto :found
    )
)

REM Essai via PATH
where mysql >nul 2>&1
if %errorlevel% equ 0 (
    set MYSQL_EXE=mysql
    goto :found
)

echo [ERREUR] mysql.exe introuvable.
echo Ajoutez le dossier bin de MySQL au PATH ou editez ce script.
pause
exit /b 1

:found
echo [OK] MySQL trouve : %MYSQL_EXE%
echo.
echo Entrez le mot de passe root MySQL (laisser vide si aucun) :
set /p MYSQL_PASSWORD=

REM Construire l'option password
set PWD_OPT=
if not "%MYSQL_PASSWORD%"=="" set PWD_OPT=-p%MYSQL_PASSWORD%

echo.
echo [1/3] Creation de la base de donnees et de l'utilisateur...
%MYSQL_EXE% -u root %PWD_OPT% -e ^
"CREATE DATABASE IF NOT EXISTS iusjcdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; ^
CREATE USER IF NOT EXISTS 'iusjc'@'localhost' IDENTIFIED BY 'iusjc2025'; ^
GRANT ALL PRIVILEGES ON iusjcdb.* TO 'iusjc'@'localhost'; ^
FLUSH PRIVILEGES; ^
SELECT 'Base iusjcdb prete.' AS status;"
if %errorlevel% neq 0 (
    echo [ERREUR] Creation de la base echouee.
    pause & exit /b 1
)

echo.
echo [2/3] Creation des tables du scheduling-service...
%MYSQL_EXE% -u root %PWD_OPT% iusjcdb < scheduling-service-tables.sql
if %errorlevel% neq 0 (
    echo [ERREUR] scheduling-service-tables.sql a echoue.
    pause & exit /b 1
)

echo.
echo [3/3] Mise a jour des tables du school-service...
%MYSQL_EXE% -u root %PWD_OPT% iusjcdb < school-service-tables.sql
if %errorlevel% neq 0 (
    echo [ERREUR] school-service-tables.sql a echoue.
    pause & exit /b 1
)

echo.
echo ========================================
echo  Verification des tables crees
echo ========================================
%MYSQL_EXE% -u root %PWD_OPT% iusjcdb -e ^
"SELECT table_name, table_rows FROM information_schema.tables ^
 WHERE table_schema = 'iusjcdb' ORDER BY table_name;"

echo.
echo ========================================
echo  Initialisation terminee avec succes !
echo ========================================
echo.
echo Les services peuvent maintenant demarrer.
echo Les tables manquantes seront creees automatiquement
echo par Hibernate (ddl-auto=update) au premier demarrage.
echo.
pause
