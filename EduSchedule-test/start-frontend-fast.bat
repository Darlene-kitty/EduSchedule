@echo off
echo === Demarrage rapide du frontend ===

cd frontend

echo Nettoyage du cache Next.js...
rmdir /s /q .next 2>nul
rmdir /s /q node_modules\.cache 2>nul

echo Demarrage de Next.js avec optimisations...
set NODE_OPTIONS=--max-old-space-size=4096
npm run dev

pause