#!/bin/bash
# Script pour arrêter tous les services backend

echo "========================================"
echo "Arrêt des Services Backend"
echo "========================================"
echo ""

# Fonction pour arrêter un service
stop_service() {
    local service_name=$1
    local pid_file="logs/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo "[$service_name] Arrêt du processus $pid..."
            kill $pid
            rm "$pid_file"
            echo "   ✅ Arrêté"
        else
            echo "[$service_name] Processus déjà arrêté"
            rm "$pid_file"
        fi
    else
        echo "[$service_name] Pas de fichier PID trouvé"
    fi
}

# Arrêter les services dans l'ordre inverse
stop_service "api-gateway"
stop_service "user-service"
stop_service "config-server"
stop_service "eureka-server"

echo ""
echo "✅ Tous les services ont été arrêtés"
echo ""
