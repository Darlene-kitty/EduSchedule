#!/bin/bash
# ============================================================
# EduSchedule — Setup initial EC2 (Ubuntu)
# Usage: bash aws/setup-ec2.sh
# À exécuter UNE SEULE FOIS après création de l'instance
# ============================================================

set -e

echo "=== [1/5] Mise à jour système ==="
sudo apt-get update -y && sudo apt-get upgrade -y

echo "=== [2/5] Installation Docker ==="
if ! command -v docker &>/dev/null; then
  sudo apt-get install -y ca-certificates curl gnupg
  sudo install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  sudo chmod a+r /etc/apt/keyrings/docker.gpg
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" \
    | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  sudo apt-get update -y
  sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
  sudo usermod -aG docker ubuntu
  sudo systemctl enable docker
  sudo systemctl start docker
  echo "Docker installé."
else
  echo "Docker déjà installé."
fi

echo "=== [3/5] Configuration swap 4GB ==="
if [ ! -f /swapfile ]; then
  sudo fallocate -l 4G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
  # Réduire swappiness pour préférer la RAM
  echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf
  sudo sysctl -p
  echo "Swap 4GB activé."
else
  echo "Swap déjà configuré."
fi

echo "=== [4/5] Création répertoire application ==="
sudo mkdir -p /opt/eduschedule
sudo chown ubuntu:ubuntu /opt/eduschedule

echo "=== [5/5] Vérification ==="
echo "RAM disponible :"
free -h
echo ""
echo "Docker version :"
docker --version
docker compose version
echo ""
echo "=== Setup terminé ==="
echo "Prochaine étape : créer /opt/eduschedule/.env avec les variables d'environnement"
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
