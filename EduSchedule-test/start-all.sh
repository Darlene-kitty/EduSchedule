#!/bin/bash
# Script pour démarrer tous les services EduSchedule

echo "🛑 Arrêt des conteneurs existants..."
docker-compose down

echo "🔨 Construction des JARs Spring Boot..."
mvn clean package -DskipTests

echo "🐳 Construction et démarrage de tous les conteneurs..."
docker-compose up -d --build

echo "⏳ Attente du démarrage des services (30 secondes)..."
sleep 30

echo "📊 Statut des conteneurs:"
docker-compose ps

echo ""
echo "✅ Services démarrés!"
echo ""
echo "🌐 URLs disponibles:"
echo "  - Frontend:        http://localhost:3000"
echo "  - API Gateway:     http://localhost:8080"
echo "  - Eureka:          http://localhost:8761"
echo "  - RabbitMQ:        http://localhost:15672 (user: iusjc, pass: iusjc2025)"
echo "  - Zipkin:          http://localhost:9411"
echo ""
echo "📝 Pour voir les logs:"
echo "  docker-compose logs -f [service-name]"
echo ""
echo "🔍 Pour vérifier les services qui ont des problèmes:"
echo "  docker-compose logs api-gateway"
echo "  docker-compose logs user-service"
