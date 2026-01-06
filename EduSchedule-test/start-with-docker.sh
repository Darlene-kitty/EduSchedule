#!/bin/bash
# Démarrage avec Docker (pas besoin de Java 17 local)

echo "========================================"
echo "Démarrage avec Docker"
echo "========================================"
echo ""

echo "1. Arrêt des conteneurs existants..."
docker-compose down

echo ""
echo "2. Construction des images..."
mvn clean package -DskipTests -Dmaven.compiler.source=11 -Dmaven.compiler.target=11 || {
    echo "⚠️  Erreur de compilation. Utilisation des JARs existants..."
}

echo ""
echo "3. Démarrage des services essentiels..."
docker-compose up -d mysql

echo "   Attente de MySQL (20 secondes)..."
sleep 20

echo ""
echo "4. Démarrage d'Eureka Server..."
docker-compose up -d eureka-server

echo "   Attente d'Eureka (30 secondes)..."
sleep 30

echo ""
echo "5. Démarrage du User Service..."
docker-compose up -d user-service

echo "   Attente du User Service (30 secondes)..."
sleep 30

echo ""
echo "6. Démarrage de l'API Gateway..."
docker-compose up -d api-gateway

echo "   Attente de l'API Gateway (20 secondes)..."
sleep 20

echo ""
echo "========================================"
echo "✅ Services démarrés avec Docker"
echo "========================================"
echo ""
echo "📊 Statut:"
docker-compose ps

echo ""
echo "🌐 URLs:"
echo "  - Eureka:      http://localhost:8761"
echo "  - API Gateway: http://localhost:8080"
echo ""
echo "📝 Logs:"
echo "  docker-compose logs -f api-gateway"
echo "  docker-compose logs -f user-service"
echo ""
echo "🧪 Test:"
echo "  curl http://localhost:8080/actuator/health"
echo ""
