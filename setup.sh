#!/bin/bash

# ============================================
# IUSJC Planning 2025 - Script de Configuration
# ============================================

set -e

echo "🚀 Configuration du projet IUSJC Planning 2025"
echo "=============================================="
echo ""

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Vérifier les prérequis
echo "📋 Vérification des prérequis..."
echo ""

# Vérifier Docker
if command -v docker &> /dev/null; then
    print_success "Docker installé : $(docker --version)"
else
    print_error "Docker n'est pas installé"
    exit 1
fi

# Vérifier Docker Compose
if command -v docker-compose &> /dev/null; then
    print_success "Docker Compose installé : $(docker-compose --version)"
else
    print_error "Docker Compose n'est pas installé"
    exit 1
fi

# Vérifier Java
if command -v java &> /dev/null; then
    print_success "Java installé : $(java -version 2>&1 | head -n 1)"
else
    print_warning "Java n'est pas installé (nécessaire pour le build Maven)"
fi

# Vérifier Maven
if command -v mvn &> /dev/null; then
    print_success "Maven installé : $(mvn -version | head -n 1)"
else
    print_warning "Maven n'est pas installé (nécessaire pour le build)"
fi

echo ""
echo "📝 Configuration des variables d'environnement..."
echo ""

# Vérifier si .env existe
if [ -f .env ]; then
    print_warning ".env existe déjà"
    read -p "Voulez-vous le remplacer ? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cp .env.example .env
        print_success ".env créé depuis .env.example"
    else
        print_warning ".env conservé"
    fi
else
    cp .env.example .env
    print_success ".env créé depuis .env.example"
fi

echo ""
echo "🔐 Génération des secrets..."
echo ""

# Générer JWT Secret (si openssl disponible)
if command -v openssl &> /dev/null; then
    JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
    
    # Remplacer dans .env (compatible Mac et Linux)
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s|JWT_SECRET=.*|JWT_SECRET=$JWT_SECRET|g" .env
    else
        sed -i "s|JWT_SECRET=.*|JWT_SECRET=$JWT_SECRET|g" .env
    fi
    
    print_success "JWT Secret généré"
else
    print_warning "OpenSSL non disponible, JWT Secret par défaut conservé"
fi

echo ""
echo "🐳 Configuration Docker..."
echo ""

# Vérifier si des conteneurs existent déjà
if [ "$(docker ps -aq -f name=iusjc-planning)" ]; then
    print_warning "Des conteneurs IUSJC Planning existent déjà"
    read -p "Voulez-vous les supprimer ? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down -v
        print_success "Conteneurs supprimés"
    fi
fi

echo ""
echo "📦 Build Maven..."
echo ""

read -p "Voulez-vous builder les services Maven maintenant ? (Y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Nn]$ ]]; then
    if command -v mvn &> /dev/null; then
        mvn clean package -DskipTests
        print_success "Build Maven terminé"
    else
        print_error "Maven n'est pas installé"
    fi
else
    print_warning "Build Maven ignoré"
fi

echo ""
echo "🚀 Démarrage des services..."
echo ""

read -p "Voulez-vous démarrer les services Docker maintenant ? (Y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Nn]$ ]]; then
    docker-compose up -d
    print_success "Services démarrés"
    
    echo ""
    echo "⏳ Attente du démarrage complet (30 secondes)..."
    sleep 30
    
    echo ""
    echo "🔍 Vérification des services..."
    echo ""
    
    # Vérifier Eureka
    if curl -s http://localhost:8761 > /dev/null; then
        print_success "Eureka Server : http://localhost:8761"
    else
        print_warning "Eureka Server non accessible"
    fi
    
    # Vérifier API Gateway
    if curl -s http://localhost:8080/actuator/health > /dev/null; then
        print_success "API Gateway : http://localhost:8080"
    else
        print_warning "API Gateway non accessible"
    fi
    
    # Vérifier Frontend
    if curl -s http://localhost:8090 > /dev/null; then
        print_success "Frontend : http://localhost:8090"
    else
        print_warning "Frontend non accessible"
    fi
    
    # Vérifier RabbitMQ
    if curl -s http://localhost:15672 > /dev/null; then
        print_success "RabbitMQ Management : http://localhost:15672"
    else
        print_warning "RabbitMQ Management non accessible"
    fi
    
else
    print_warning "Démarrage Docker ignoré"
fi

echo ""
echo "=============================================="
echo "✅ Configuration terminée !"
echo "=============================================="
echo ""
echo "📚 Prochaines étapes :"
echo ""
echo "1. Vérifier Eureka (14 services) :"
echo "   http://localhost:8761"
echo ""
echo "2. Tester l'API Gateway :"
echo "   curl http://localhost:8080/actuator/health"
echo ""
echo "3. Voir les logs :"
echo "   docker-compose logs -f"
echo ""
echo "4. Arrêter les services :"
echo "   docker-compose down"
echo ""
echo "📖 Documentation :"
echo "   - README.md"
echo "   - ENV_CONFIGURATION.md"
echo "   - ARCHITECTURE_OPTIMISEE.md"
echo "   - COMMANDES_RAPIDES.md"
echo ""
echo "🎉 Bon développement !"
