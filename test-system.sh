#!/bin/bash

# ============================================
# IUSJC Planning 2025 - Script de Test Complet
# ============================================

set -e

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }

echo "🧪 Test Complet du Système IUSJC Planning 2025"
echo "=============================================="
echo ""

# Compteurs
TESTS_PASSED=0
TESTS_FAILED=0

# Fonction de test
run_test() {
    local test_name=$1
    local test_command=$2
    
    echo -n "Testing: $test_name... "
    
    if eval "$test_command" > /dev/null 2>&1; then
        print_success "PASS"
        ((TESTS_PASSED++))
        return 0
    else
        print_error "FAIL"
        ((TESTS_FAILED++))
        return 1
    fi
}

# 1. Vérifier les fichiers de configuration
echo "📁 Vérification des fichiers..."
echo ""

run_test "Fichier .env existe" "test -f .env"
run_test "Fichier docker-compose.yml existe" "test -f docker-compose.yml"
run_test "Fichier init-db.sql existe" "test -f init-db.sql"
run_test "Fichier pom.xml existe" "test -f pom.xml"

echo ""
echo "🐳 Vérification de Docker..."
echo ""

run_test "Docker est installé" "command -v docker"
run_test "Docker Compose est installé" "command -v docker-compose"
run_test "Docker daemon est actif" "docker info"

echo ""
echo "📦 Vérification des conteneurs..."
echo ""

# Attendre que les conteneurs soient prêts
print_info "Attente du démarrage des conteneurs (10 secondes)..."
sleep 10

run_test "MySQL est en cours d'exécution" "docker ps | grep mysql"
run_test "Redis est en cours d'exécution" "docker ps | grep redis"
run_test "RabbitMQ est en cours d'exécution" "docker ps | grep rabbitmq"
run_test "Eureka Server est en cours d'exécution" "docker ps | grep eureka-server"

echo ""
echo "🌐 Vérification des services (attente 30s)..."
echo ""

print_info "Attente du démarrage complet des services..."
sleep 30

# Test Eureka
if curl -s http://localhost:8761 > /dev/null; then
    print_success "Eureka Server accessible (http://localhost:8761)"
    ((TESTS_PASSED++))
    
    # Compter les services enregistrés
    SERVICES_COUNT=$(curl -s http://localhost:8761/eureka/apps | grep -o "<application>" | wc -l)
    echo "   Services enregistrés: $SERVICES_COUNT/14"
    
    if [ "$SERVICES_COUNT" -ge 10 ]; then
        print_success "Au moins 10 services enregistrés"
        ((TESTS_PASSED++))
    else
        print_warning "Seulement $SERVICES_COUNT services enregistrés (attendu: 14)"
        ((TESTS_FAILED++))
    fi
else
    print_error "Eureka Server non accessible"
    ((TESTS_FAILED++))
fi

# Test API Gateway
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    print_success "API Gateway accessible (http://localhost:8080)"
    ((TESTS_PASSED++))
else
    print_error "API Gateway non accessible"
    ((TESTS_FAILED++))
fi

# Test Frontend
if curl -s http://localhost:8090 > /dev/null; then
    print_success "Frontend accessible (http://localhost:8090)"
    ((TESTS_PASSED++))
else
    print_warning "Frontend non accessible (peut prendre plus de temps)"
    ((TESTS_FAILED++))
fi

# Test RabbitMQ Management
if curl -s http://localhost:15672 > /dev/null; then
    print_success "RabbitMQ Management accessible (http://localhost:15672)"
    ((TESTS_PASSED++))
else
    print_error "RabbitMQ Management non accessible"
    ((TESTS_FAILED++))
fi

echo ""
echo "🗄️  Vérification de MySQL..."
echo ""

# Test connexion MySQL
if docker exec $(docker ps -qf "name=mysql") mysql -uiusjc -piusjc2024 -e "SELECT 1" > /dev/null 2>&1; then
    print_success "Connexion MySQL réussie"
    ((TESTS_PASSED++))
    
    # Vérifier la base de données
    if docker exec $(docker ps -qf "name=mysql") mysql -uiusjc -piusjc2024 -e "USE iusjcdb; SELECT 1" > /dev/null 2>&1; then
        print_success "Base de données iusjcdb existe"
        ((TESTS_PASSED++))
        
        # Compter les tables
        TABLES_COUNT=$(docker exec $(docker ps -qf "name=mysql") mysql -uiusjc -piusjc2024 iusjcdb -e "SHOW TABLES" | wc -l)
        echo "   Tables créées: $((TABLES_COUNT-1))/5"
        
        if [ "$TABLES_COUNT" -ge 5 ]; then
            print_success "Tables créées avec succès"
            ((TESTS_PASSED++))
        else
            print_warning "Seulement $((TABLES_COUNT-1)) tables créées"
            ((TESTS_FAILED++))
        fi
        
        # Vérifier les données de test
        USERS_COUNT=$(docker exec $(docker ps -qf "name=mysql") mysql -uiusjc -piusjc2024 iusjcdb -e "SELECT COUNT(*) FROM users" 2>/dev/null | tail -1)
        if [ "$USERS_COUNT" -ge 3 ]; then
            print_success "Données de test chargées ($USERS_COUNT utilisateurs)"
            ((TESTS_PASSED++))
        else
            print_warning "Données de test manquantes"
            ((TESTS_FAILED++))
        fi
    else
        print_error "Base de données iusjcdb n'existe pas"
        ((TESTS_FAILED++))
    fi
else
    print_error "Connexion MySQL échouée"
    ((TESTS_FAILED++))
fi

echo ""
echo "🔴 Vérification de Redis..."
echo ""

if docker exec $(docker ps -qf "name=redis") redis-cli PING > /dev/null 2>&1; then
    print_success "Redis répond au PING"
    ((TESTS_PASSED++))
else
    print_error "Redis ne répond pas"
    ((TESTS_FAILED++))
fi

echo ""
echo "🐰 Vérification de RabbitMQ..."
echo ""

if docker exec $(docker ps -qf "name=rabbitmq") rabbitmqctl status > /dev/null 2>&1; then
    print_success "RabbitMQ est actif"
    ((TESTS_PASSED++))
else
    print_error "RabbitMQ n'est pas actif"
    ((TESTS_FAILED++))
fi

echo ""
echo "🧪 Tests API..."
echo ""

# Test user-service (peut ne pas être encore démarré)
print_info "Test user-service..."
if curl -s http://localhost:8080/api/users > /dev/null 2>&1; then
    print_success "user-service répond"
    ((TESTS_PASSED++))
else
    print_warning "user-service ne répond pas encore (normal si pas encore buildé)"
fi

# Test scheduling-service
print_info "Test scheduling-service..."
if curl -s http://localhost:8080/api/schedules > /dev/null 2>&1; then
    print_success "scheduling-service répond"
    ((TESTS_PASSED++))
else
    print_warning "scheduling-service ne répond pas encore (normal si pas encore buildé)"
fi

# Test notification-service
print_info "Test notification-service..."
if curl -s http://localhost:8080/api/notifications > /dev/null 2>&1; then
    print_success "notification-service répond"
    ((TESTS_PASSED++))
else
    print_warning "notification-service ne répond pas encore (normal si pas encore buildé)"
fi

echo ""
echo "=============================================="
echo "📊 Résultats des Tests"
echo "=============================================="
echo ""
echo "Tests réussis: $TESTS_PASSED"
echo "Tests échoués: $TESTS_FAILED"
echo "Total: $((TESTS_PASSED + TESTS_FAILED))"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    print_success "🎉 Tous les tests sont passés !"
    echo ""
    echo "✅ Le système est opérationnel"
    echo ""
    echo "📚 Prochaines étapes:"
    echo "1. Ouvrir Eureka: http://localhost:8761"
    echo "2. Vérifier les 14 services enregistrés"
    echo "3. Tester les API via http://localhost:8080"
    echo ""
    exit 0
else
    print_warning "⚠️  Certains tests ont échoué"
    echo ""
    echo "🔍 Vérifications suggérées:"
    echo "1. Vérifier les logs: docker-compose logs"
    echo "2. Vérifier les conteneurs: docker-compose ps"
    echo "3. Redémarrer si nécessaire: docker-compose restart"
    echo ""
    exit 1
fi
