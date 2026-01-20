# Configuration des Ports - Microservices EduSchedule

## Ports Actuels

### Services Infrastructure
| Service | Port | Status | Description |
|---------|------|--------|-------------|
| Eureka Server | 8761 | ✅ Configuré | Service de découverte |
| Config Server | 8888 | ✅ Configuré | Serveur de configuration |
| API Gateway | 8080 | ✅ Configuré | Passerelle API |

### Services Métier - Ports Fixes
| Service | Port | Status | Description |
|---------|------|--------|-------------|
| User Service | 8081 | ✅ Configuré | Gestion des utilisateurs et authentification |
| Resource Service | 8082 | ✅ Configuré | Gestion des salles et équipements |

### Services Métier - Ports Dynamiques (port=0)
| Service | Port Suggéré | Status | Description |
|---------|--------------|--------|-------------|
| School Service | 8083 | 🔄 À configurer | Gestion des écoles, filières, groupes |
| Course Service | 8084 | 🔄 À configurer | Gestion des cours |
| Scheduling Service | 8085 | 🔄 À configurer | Gestion des emplois du temps |
| Reservation Service | 8086 | 🔄 À configurer | Gestion des réservations |
| Notification Service | 8087 | 🔄 À configurer | Service de notifications |
| Reporting Service | 8088 | 🔄 À configurer | Service de rapports |
| ENT Integration Service | 8089 | 🔄 À configurer | Intégration ENT |
| Room Service | 8090 | 🔄 À configurer | Service des salles (si différent de Resource) |
| Event Service | 8091 | 🔄 À configurer | Gestion des événements |

## Ports Externes
| Service | Port | Description |
|---------|------|-------------|
| Frontend React | 3000 | Interface utilisateur |
| MySQL Database | 3306 | Base de données |
| Redis | 6379 | Cache et sessions |
| RabbitMQ | 5672 | Messagerie |
| RabbitMQ Management | 15672 | Interface de gestion RabbitMQ |

## Recommandations

### 1. Ports Fixes vs Dynamiques
- **Ports fixes** : Recommandés pour les services critiques et ceux appelés directement
- **Ports dynamiques** : Appropriés pour les services découverts via Eureka

### 2. Services à Configurer en Ports Fixes
Les services suivants devraient avoir des ports fixes car ils sont essentiels :
- School Service (8083) - Service fondamental
- Course Service (8084) - Service fondamental  
- Scheduling Service (8085) - Service critique

### 3. Services Pouvant Rester Dynamiques
- Notification Service
- Reporting Service
- Event Service
- Room Service (si redondant avec Resource Service)

## Configuration Docker
Dans `docker-compose.yml`, les ports sont mappés comme suit :
- Services avec ports fixes : mapping direct (ex: 8081:8081)
- Services dynamiques : Eureka gère la découverte automatiquement

## Commandes de Vérification
```bash
# Vérifier les ports utilisés
netstat -an | findstr "808"

# Tester la connectivité des services
curl http://localhost:8761/eureka/apps
```