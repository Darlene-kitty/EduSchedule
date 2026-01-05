#!/bin/bash
# Script de démarrage pour GitHub Codespaces / Environnements sans GUI

echo "========================================"
echo "Démarrage des Services Backend"
echo "EduSchedule - GitHub Codespaces"
echo "========================================"
echo ""

# Créer un dossier pour les logs
mkdir -p logs

# Fonction pour démarrer un service en arrière-plan
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    
    echo "[$service_name] Démarrage sur port $port..."
    cd $service_dir
    nohup mvn spring-boot:run > ../logs/${service_name}.log 2>&1 &
    local pid=$!
    echo $pid > ../logs/${service_name}.pid
    echo "   PID: $pid"
    cd ..
}

# Vérifier si MySQL est accessible
echo "Vérification de MySQL..."
if ! nc -z localhost 3306 2>/dev/null; then
    echo "⚠️  MySQL n'est pas accessible sur le port 3306"
    echo "Démarrage de MySQL avec Docker..."
    docker run -d --name mysql-eduschedule \
      -e MYSQL_ROOT_PASSWORD=root \
      -e MYSQL_DATABASE=iusjcdb \
      -e MYSQL_USER=iusjc \
      -e MYSQL_PASSWORD=iusjc2025 \
      -p 3306:3306 \
      mysql:8.0
    echo "Attente du démarrage de MySQL (30 secondes)..."
    sleep 30
fi
echo "✅ MySQL accessible"
echo ""

# Démarrer les services
echo "[1/4] Démarrage d'Eureka Server..."
start_service "eureka-server" "eureka-server" "8761"
echo "   Attente de 30 secondes..."
sleep 30

echo ""
echo "[2/4] Démarrage du Config Server..."
start_service "config-server" "config-server" "8888"
echo "   Attente de 20 secondes..."
sleep 20

echo ""
echo "[3/4] Démarrage du User Service..."
start_service "user-service" "user-service" "random"
echo "   Attente de 30 secondes..."
sleep 30

echo ""
echo "[4/4] Démarrage de l'API Gateway..."
start_service "api-gateway" "api-gateway" "8080"
echo "   Attente de 20 secondes..."
sleep 20

echo ""
echo "========================================"
echo "✅ Tous les services sont démarrés!"
echo "========================================"
echo ""
echo "📊 Statut des services:"
echo ""

# Vérifier les services
check_service() {
    local name=$1
    local url=$2
    
    if curl -s "$url" > /dev/null 2>&1; then
        echo "✅ $name - OK"
    else
        echo "❌ $name - Non accessible"
    fi
}

check_service "Eureka Server    " "http://localhost:8761"
check_service "API Gateway      " "http://localhost:8080/actuator/health"

echo ""
echo "🌐 URLs disponibles:"
echo "  - Eureka:          http://localhost:8761"
echo "  - API Gateway:     http://localhost:8080"
echo "  - Health Check:    http://localhost:8080/actuator/health"
echo ""
echo "📝 Logs disponibles dans le dossier 'logs/':"
echo "  - tail -f logs/eureka-server.log"
echo "  - tail -f logs/user-service.log"
echo "  - tail -f logs/api-gateway.log"
echo ""
echo "🛑 Pour arrêter les services:"
echo "  ./stop-backend.sh"
echo ""
echo "🚀 Pour démarrer le frontend:"
echo "  cd frontend && npm run dev"
echo ""
echo "🧪 Pour tester:"
echo "  curl http://localhost:8080/actuator/health"
echo ""
