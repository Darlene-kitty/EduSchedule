#!/bin/bash
# ============================================================
# EduSchedule — Setup initial EC2 Ubuntu (t3.medium x86_64)
# À exécuter UNE SEULE FOIS après création de l'instance
# Usage: bash aws/setup-ec2.sh
# ============================================================

set -e

echo "=== [1/6] Mise à jour système ==="
sudo apt-get update -y && sudo apt-get upgrade -y

echo "=== [2/6] Installation Docker (x86_64) ==="
if ! command -v docker &>/dev/null; then
  sudo apt-get install -y ca-certificates curl gnupg
  sudo install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
    | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  sudo chmod a+r /etc/apt/keyrings/docker.gpg
  echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu \
    $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  sudo apt-get update -y
  sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
  sudo usermod -aG docker ubuntu
  sudo systemctl enable docker
  sudo systemctl start docker
  echo "Docker installé."
else
  echo "Docker déjà installé : $(docker --version)"
fi

echo "=== [3/6] Configuration swap 4 GB ==="
# t3.medium a 4 GB RAM — le swap évite les OOM kills au démarrage simultané des services
if [ ! -f /swapfile ]; then
  sudo fallocate -l 4G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
  # Préférer la RAM, n'utiliser le swap qu'en dernier recours
  echo 'vm.swappiness=10'           | sudo tee -a /etc/sysctl.conf
  echo 'vm.overcommit_memory=1'     | sudo tee -a /etc/sysctl.conf
  sudo sysctl -p
  echo "Swap 4 GB activé."
else
  echo "Swap déjà configuré : $(swapon --show)"
fi

echo "=== [4/6] Optimisations kernel pour Java/Docker ==="
cat <<'EOF' | sudo tee /etc/sysctl.d/99-eduschedule.conf
# Connexions réseau
net.core.somaxconn=65535
net.ipv4.tcp_max_syn_backlog=65535
# Fichiers ouverts
fs.file-max=1000000
# Mémoire partagée
kernel.shmmax=2147483648
EOF
sudo sysctl --system

echo "=== [5/6] Création répertoire application ==="
sudo mkdir -p /opt/eduschedule
sudo chown ubuntu:ubuntu /opt/eduschedule

echo "=== [6/6] Vérification ==="
echo ""
echo "Architecture : $(uname -m)"
echo "RAM disponible :"
free -h
echo ""
echo "Swap :"
swapon --show
echo ""
echo "Docker :"
docker --version
docker compose version
echo ""
echo "=== Setup terminé ==="
echo ""
echo "Prochaines étapes :"
echo "  1. Déconnecte-toi et reconnecte-toi (pour activer docker sans sudo)"
echo "  2. Crée /opt/eduschedule/.env (voir .env.example)"
echo "  3. cd /opt/eduschedule && bash aws/deploy.sh"
