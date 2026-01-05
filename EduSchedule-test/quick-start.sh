#!/bin/bash
# Démarrage rapide - Gère MySQL existant

echo "========================================"
echo "Démarrage Rapide EduSchedule"
echo "========================================"
echo ""

# 1. Gérer MySQL
echo "1. Vérification de MySQL..."
if docker ps -a | grep -q mysql-eduschedule; then
    echo "   MySQL existe déjà"
    if ! docker ps | grep -q mysql-eduschedule; then
        echo "   Démarrage de MySQL..."
        docker start mysql-eduschedule
        sleep 15
    else
        echo "   MySQL déjà démarré"
    fi
else
    echo "   Création de MySQL..."
    docker run -d --name mysql-eduschedule \
      -e MYSQL_ROOT_PASSWORD=root \
      -e MYSQL_DATABASE=iusjcdb \
      -e MYSQL_USER=iusjc \
      -e MYSQL_PASSWORD=iusjc2025 \
      -p 3306:3306 \
      mysql:8.0
    sleep 20
fi
echo "   ✅ MySQL prêt"

# 2. Arrêter les anciens services
echo ""
echo "2. Nettoyage..."
docker-compose down 2>/dev/null
pkill -f "spring-boot:run" 2>/dev/null

# 3. Démarrer avec Docker Compose
echo ""
echo "3. Démarrage des services..."
docker-compose up -d eureka-server
echo "   Attente d'Eureka (30s)..."
sleep 30

docker-compose up -d user-service
echo "   Attente du User Service (30s)..."
sleep 30

docker-compose up -d api-gateway
echo "   Attente de l'API Gateway (20s)..."
sleep 20

# 4. Vérification
echo ""
echo "========================================"
echo "✅ Démarrage terminé"
echo "========================================"
echo ""
echo "📊 Statut des services:"
docker-compose ps

echo ""
echo "🧪 Test de l'API Gateway:"
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo "   ✅ API Gateway fonctionne"
else
    echo "   ⚠️  API Gateway pas encore prêt, attendez 1 minute"
fi

echo ""
echo "🌐 URLs:"
echo "   - Eureka:      http://localhost:8761"
echo "   - API Gateway: http://localhost:8080"
echo "   - Frontend:    Démarrez avec: cd frontend && npm run dev"
echo ""
echo "📝 Logs:"
echo "   docker-compose logs -f api-gateway"
echo ""
echo "🛑 Arrêter:"
echo "   docker-compose down"
echo ""
