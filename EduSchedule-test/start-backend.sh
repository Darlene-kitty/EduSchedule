#!/bin/bash
# Script de démarrage des services backend - Linux/Mac

echo "========================================"
echo "Démarrage des Services Backend"
echo "========================================"
echo ""

echo "IMPORTANT: Ce script démarre les services dans des terminaux séparés"
echo "Assurez-vous que MySQL est déjà démarré!"
echo ""
read -p "Appuyez sur Entrée pour continuer..."

# Fonction pour démarrer un service dans un nouveau terminal
start_service() {
    local service_name=$1
    local service_dir=$2
    local wait_time=$3
    
    echo ""
    echo "[$service_name] Démarrage..."
    
    # Détection du système et du terminal
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        osascript -e "tell app \"Terminal\" to do script \"cd $(pwd)/$service_dir && mvn spring-boot:run\""
    elif command -v gnome-terminal &> /dev/null; then
        # Linux avec GNOME
        gnome-terminal -- bash -c "cd $service_dir && mvn spring-boot:run; exec bash"
    elif command -v xterm &> /dev/null; then
        # Linux avec xterm
        xterm -e "cd $service_dir && mvn spring-boot:run" &
    else
        echo "⚠️  Terminal non détecté. Démarrez manuellement:"
        echo "   cd $service_dir && mvn spring-boot:run"
        return
    fi
    
    echo "   Attente de $wait_time secondes..."
    sleep $wait_time
}

# Démarrage des services
echo ""
echo "[1/4] Démarrage d'Eureka Server..."
start_service "Eureka Server" "eureka-server" 30

echo ""
echo "[2/4] Démarrage du Config Server..."
start_service "Config Server" "config-server" 20

echo ""
echo "[3/4] Démarrage du User Service..."
start_service "User Service" "user-service" 30

echo ""
echo "[4/4] Démarrage de l'API Gateway..."
start_service "API Gateway" "api-gateway" 20

echo ""
echo "========================================"
echo "Tous les services sont en cours de démarrage!"
echo "========================================"
echo ""
echo "Vérifications:"
echo "- Eureka: http://localhost:8761"
echo "- API Gateway: http://localhost:8080/actuator/health"
echo ""
echo "Pour démarrer le frontend:"
echo "  cd frontend"
echo "  npm run dev"
echo ""
echo "Pour tester la connexion:"
echo "  http://localhost:3000/test-connection"
echo ""
