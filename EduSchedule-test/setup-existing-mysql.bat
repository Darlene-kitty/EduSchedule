@echo off
echo ========================================
echo Configuration MySQL Existant
echo ========================================

echo Test de connexion a MySQL existant...
mysql -h localhost -P 3306 -u root -p -e "SHOW DATABASES;"

echo.
echo Si MySQL fonctionne, creez la base de donnees:
echo.
echo mysql -h localhost -P 3306 -u root -p
echo CREATE DATABASE IF NOT EXISTS iusjcdb;
echo USE iusjcdb;
echo SOURCE init-db.sql;
echo SOURCE insert-admin.sql;
echo EXIT;
echo.
echo ========================================
echo Ou utilisez MySQL Workbench/phpMyAdmin
echo ========================================
pause