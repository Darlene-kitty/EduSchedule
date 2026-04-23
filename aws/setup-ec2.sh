#!/bin/bash
# ============================================================
# EduSchedule — Setup initial EC2 Ubuntu
# Exécuter une seule fois après création de l'instance
# Usage: bash setup-ec2.sh
# ============================================================

set -e

echo "=== [1/6] Mise à jour du système ==="
sudo apt-get update -y && sudo apt-get upgrade -y

echo "=== [2/6] Création du fichier SWAP (4 GB) ==="
# Critique pour t4g.medium (4 GB RAM) — le swap évite les OOM kills
if [ ! -f /swapfile ]; then
    sudo fallocate -l 4G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    # Réduire la tendance à utiliser le swap (0=jamais, 100=toujours)
    echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf
    sudo sysctl -p
    echo "Swap 4 GB configuré."
else
    echo "Swap déjà configuré."
fi

echo "=== [3/6] Installation Docker ==="
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
sudo systemctl enable docker
sudo systemctl start docker

echo "=== [4/6] Installation Docker Compose v2 ==="
sudo apt-get install -y docker-compose-plugin

echo "=== [5/6] Installation Nginx ==="
sudo apt-get install -y nginx certbot python3-certbot-nginx

echo "=== [6/6] Clonage du projet ==="
cd /opt
sudo git clone https://github.com/Darlene-kitty/EduSchedule.git eduschedule
sudo chown -R $USER:$USER /opt/eduschedule

echo ""
echo "=== Setup terminé ==="
echo "RAM disponible :"
free -h
echo ""
echo "Prochaines étapes :"
echo "  1. Déconnecte-toi et reconnecte-toi (pour activer docker sans sudo)"
echo "  2. cd /opt/eduschedule"
echo "  3. cp .env.example .env && nano .env"
echo "  4. bash aws/deploy.sh"
