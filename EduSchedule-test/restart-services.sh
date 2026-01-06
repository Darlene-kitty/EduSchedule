#!/bin/bash
# Script pour recompiler et redémarrer les services après modification

echo "========================================"
echo "Recompilation et Redémarrage"
echo "========================================"
echo ""

# Arrêter les services existants
echo "1. Arrêt des services..."
if [ -f "stop-backend.sh" ]; then
    ./stop-backend.sh
else
    echo "   Arrêt manuel des processus Java..."
    pkill -f "spring-boot:run"
fi

echo ""
echo "2. Recompilation des services modifiés..."

# Recompiler API Gateway
echo "   - API Gateway..."
cd api-gateway
mvn clean compile -DskipTests -q
cd ..

# Recompiler User Service
echo "   - User Service..."
cd user-service
mvn clean compile -DskipTests -q
cd ..

echo ""
echo "3. Redémarrage des services..."
echo ""

# Créer le dossier logs si nécessaire
mkdir -p logs

# Fonction pour démarrer un service
start_service() {
    local service_name=$1
    local service_dir=$2
    
    echo "   [$service_name] Démarrage..."
    cd $service_dir
    nohup mvn spring-boot:run > ../logs/${service_name}.log 2>&1 &
    echo $! > ../logs/${service_name}.pid
    cd ..
}

# Démarrer Eureka (si pas déjà démarré)
if [ ! -f "logs/eureka-server.pid" ] || ! ps -p $(cat logs/eureka-server.pid) > /dev/null 2>&1; then
    start_service "eureka-server" "eureka-server"
    echo "   Attente de 30 secondes..."
    sleep 30
fi

# Démarrer User Service
start_service "user-service" "user-service"
echo "   Attente de 30 secondes..."
sleep 30

# Démarrer API Gateway
start_service "api-gateway" "api-gateway"
echo "   Attente de 20 secondes..."
sleep 20

echo ""
echo "========================================"
echo "✅ Services redémarrés avec CORS GitHub Codespaces"
echo "========================================"
echo ""
echo "🧪 Testez maintenant:"
echo "   1. Rafraîchissez votre page frontend"
echo "   2. Essayez de vous inscrire/connecter"
echo ""
echo "📝 Logs:"
echo "   tail -f logs/api-gateway.log"
echo "   tail -f logs/user-service.log"
echo ""
