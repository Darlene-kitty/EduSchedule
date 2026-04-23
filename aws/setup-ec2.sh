#!/bin/bash
# ============================================================
# EduSchedule — Setup initial EC2 Ubuntu
# Exécuter une seule fois après création de l'instance
# Usage: bash setup-ec2.sh
# ============================================================

set -e

echo "=== [1/5] Mise à jour du système ==="
sudo apt-get update -y && sudo apt-get upgrade -y

echo "=== [2/5] Installation Docker ==="
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
sudo systemctl enable docker
sudo systemctl start docker

echo "=== [3/5] Installation Docker Compose v2 ==="
sudo apt-get install -y docker-compose-plugin

echo "=== [4/5] Installation Nginx ==="
sudo apt-get install -y nginx certbot python3-certbot-nginx

echo "=== [5/5] Clonage du projet ==="
cd /opt
sudo git clone https://github.com/Darlene-kitty/EduSchedule.git eduschedule
sudo chown -R $USER:$USER /opt/eduschedule

echo ""
echo "=== Setup terminé ==="
echo "Prochaines étapes :"
echo "  1. cd /opt/eduschedule"
echo "  2. cp .env.example .env && nano .env"
echo "  3. bash aws/deploy.sh"
