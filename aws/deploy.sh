#!/bin/bash
# ============================================================
# EduSchedule — Déploiement / Mise à jour
# Usage: bash aws/deploy.sh
# ============================================================

set -e

APP_DIR="/opt/eduschedule"
cd "$APP_DIR"

echo "=== [1/4] Pull des dernières modifications ==="
git pull origin main

echo "=== [2/4] Arrêt des services ==="
docker compose down --remove-orphans 2>/dev/null || true

echo "=== [3/4] Pull des images Docker Hub ==="
docker compose pull

echo "=== [4/4] Démarrage des services ==="
docker compose up -d

echo ""
echo "=== Déploiement terminé ==="
echo "Vérification des services :"
docker compose ps
