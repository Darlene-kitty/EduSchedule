#!/bin/bash
# ============================================================
# EduSchedule — Déploiement / Mise à jour (t3.medium x86_64)
# Usage: bash aws/deploy.sh
# ============================================================

set -e

APP_DIR="/opt/eduschedule"
COMPOSE_FILE="aws/docker-compose.prod.yml"
ENV_FILE="$APP_DIR/.env"

# ── Cloner si premier déploiement ────────────────────────────────────────
if [ ! -d "$APP_DIR/.git" ]; then
  echo "=== Premier déploiement : clonage du dépôt ==="
  sudo mkdir -p "$APP_DIR"
  sudo chown ubuntu:ubuntu "$APP_DIR"
  git clone https://github.com/Darlene-kitty/EduSchedule.git "$APP_DIR"
fi

cd "$APP_DIR"

# ── Vérifier le fichier .env ─────────────────────────────────────────────
if [ ! -f "$ENV_FILE" ]; then
  echo "ERREUR : $ENV_FILE introuvable."
  echo "Crée-le depuis .env.example : cp .env.example .env && nano .env"
  exit 1
fi

echo "=== [1/5] Pull des dernières modifications ==="
git pull origin main

echo "=== [2/5] Arrêt des services existants ==="
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" down --remove-orphans 2>/dev/null || true

echo "=== [3/5] Pull des images Docker Hub (linux/amd64) ==="
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" pull

echo "=== [4/5] Démarrage des services ==="
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d

echo "=== [5/5] Nettoyage des images inutilisées ==="
docker image prune -f

echo ""
echo "=== Déploiement terminé ==="
echo ""
echo "État des services :"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps

echo ""
echo "Mémoire utilisée :"
free -h

echo ""
echo "Logs en temps réel : docker compose -f $COMPOSE_FILE logs -f --tail=50"
