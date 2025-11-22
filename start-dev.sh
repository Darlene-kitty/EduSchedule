#!/bin/bash

# Script Bash pour démarrer l'environnement de développement EduSchedule
# Usage: ./start-dev.sh

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  EduSchedule - Démarrage Dev${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# Vérifier si Docker est installé
echo -e "${YELLOW}Vérification de Docker...${NC}"
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version)
    echo -e "${GREEN}✓ Docker trouvé: $DOCKER_VERSION${NC}"
else
    echo -e "${RED}✗ Docker n'est pas installé ou n'est pas dans le PATH${NC}"
    echo -e "${YELLOW}  Veuillez installer Docker: https://www.docker.com/get-docker${NC}"
    exit 1
fi

# Vérifier si Node.js est installé
echo -e "${YELLOW}Vérification de Node.js...${NC}"
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    echo -e "${GREEN}✓ Node.js trouvé: $NODE_VERSION${NC}"
else
    echo -e "${RED}✗ Node.js n'est pas installé ou n'est pas dans le PATH${NC}"
    echo -e "${YELLOW}  Veuillez installer Node.js: https://nodejs.org/${NC}"
    exit 1
fi

echo ""
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Étape 1: Démarrage du Backend${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# Démarrer Docker Compose
echo -e "${YELLOW}Démarrage des services Docker...${NC}"
docker-compose up -d

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Services Docker démarrés avec succès${NC}"
else
    echo -e "${RED}✗ Erreur lors du démarrage des services Docker${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}Attente du démarrage des services (30 secondes)...${NC}"
sleep 30

echo ""
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Étape 2: Configuration du Frontend${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# Aller dans le dossier frontend
cd frontend

# Vérifier si node_modules existe
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}Installation des dépendances npm...${NC}"
    npm install
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Dépendances installées avec succès${NC}"
    else
        echo -e "${RED}✗ Erreur lors de l'installation des dépendances${NC}"
        cd ..
        exit 1
    fi
else
    echo -e "${GREEN}✓ Dépendances déjà installées${NC}"
fi

# Vérifier si .env.local existe
if [ ! -f ".env.local" ]; then
    echo -e "${YELLOW}Création du fichier .env.local...${NC}"
    cat > .env.local << EOF
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_API_TIMEOUT=30000
EOF
    echo -e "${GREEN}✓ Fichier .env.local créé${NC}"
else
    echo -e "${GREEN}✓ Fichier .env.local existe déjà${NC}"
fi

echo ""
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Étape 3: Démarrage du Frontend${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

echo -e "${YELLOW}Démarrage du serveur de développement Next.js...${NC}"
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  ✓ Environnement prêt !${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${CYAN}Services disponibles:${NC}"
echo -e "  - Frontend:          http://localhost:3000"
echo -e "  - API Gateway:       http://localhost:8080"
echo -e "  - Eureka Dashboard:  http://localhost:8761"
echo -e "  - RabbitMQ:          http://localhost:15672 (guest/guest)"
echo ""
echo -e "${YELLOW}Pour arrêter les services:${NC}"
echo -e "  1. Ctrl+C pour arrêter le frontend"
echo -e "  2. docker-compose down pour arrêter le backend"
echo ""
echo -e "${YELLOW}Appuyez sur Ctrl+C pour arrêter le frontend...${NC}"
echo ""

# Démarrer le serveur de développement
npm run dev

# Retourner au dossier racine
cd ..
