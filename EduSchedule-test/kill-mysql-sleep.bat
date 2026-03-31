@echo off
echo Killing MySQL Sleep connections...
"C:\Program Files\MySQL\MySQL Workbench 8.0 CE\mysql.exe" -u iusjc -piusjc2025 iusjcdb -e "SELECT CONCAT('KILL ',Id,';') FROM information_schema.processlist WHERE User='iusjc' AND Command='Sleep' INTO OUTFILE 'C:/tmp/kill_connections.sql';" 2>nul
echo Done
