# Guide de Déploiement Docker - EduSchedule

## Prérequis

- Docker Desktop installé et en cours d'exécution
- Docker Compose v2.0+
- Maven 3.8+ (pour construire les JARs)
- Node.js 20+ (pour construire le frontend)
- Au moins 8 GB de RAM disponible
- Au moins 20 GB d'espace disque

## Architecture Docker

Le déploiement comprend :

### Services d'Infrastructure
- **MySQL 8.0** - Base de données principale (port 3306)
- **Redis 7** - Cache et sessions (port 6379)
- **RabbitMQ 3** - Message broker (ports 5672, 15672)
- **Eureka Server** - Service discovery (port 8761)
- **Zipkin** - Distributed tracing (port 9411)

### Services Core
- **Config Server** - Configuration centralisée (port 8888)
- **API Gateway** - Point d'entrée unique (port 8080)

### Microservices Métier
- **User Service** (8081)
- **Resource Service** (8082)
- **School Service** (8083)
- **Course Service** (8084)
- **Scheduling Service** (8085)
- **Reservation Service** (8086)
- **Notification Service** (8087)
- **Reporting Service** (8088)
- **Calendar Service** (8089)
- **Room Service** (8090)
- **Event Service** (8091)
- **Teacher Availability Service** (8092)
- **AI Service** (8093)
- **Maintenance Service** (8094)
- **ENT Integration Service** (8095)

### Frontend
- **Next.js Application** (port 3000)

## Étapes de Déploiement

### 1. Préparation

```powershell
# Vérifier que Docker est en cours d'exécution
docker info

# Vérifier les variables d'environnement
cat .env
```

### 2. Construction des Images

#### Option A : Construction automatique (recommandé)
```cmd
build-docker-images.bat
```

#### Option B : Construction manuelle

```powershell
# 1. Construire le frontend
cd frontend
npm install
npm run build
cd ..

# 2. Construire tous les services Spring Boot
mvn clean package -DskipTests

# 3. Construire les images Docker
docker-compose build
```

### 3. Démarrage des Services

```powershell
# Démarrer tous les services
docker-compose up -d

# Voir les logs en temps réel
docker-compose logs -f

# Voir les logs d'un service spécifique
docker-compose logs -f user-service
```

### 4. Vérification du Déploiement

#### Option A : Script automatique (recommandé)
```powershell
.\test-docker-deployment.ps1
```

#### Option B : Vérification manuelle

```powershell
# Vérifier l'état des conteneurs
docker-compose ps

# Vérifier les services d'infrastructure
curl http://localhost:8761  # Eureka
curl http://localhost:15672 # RabbitMQ Management
curl http://localhost:9411  # Zipkin

# Vérifier l'API Gateway
curl http://localhost:8080/actuator/health

# Vérifier le frontend
curl http://localhost:3000
```

### 5. Vérification de la Communication

```powershell
# Exécuter le script de vérification
.\verify-services-communication.ps1

# Vérifier l'enregistrement Eureka
curl http://localhost:8761/eureka/apps
```

## Ordre de Démarrage

Les services démarrent dans l'ordre suivant grâce aux dépendances Docker Compose :

1. **Infrastructure** : MySQL, Redis, RabbitMQ
2. **Service Discovery** : Eureka Server
3. **Configuration** : Config Server
4. **Gateway** : API Gateway
5. **Microservices** : Tous les services métier
6. **Frontend** : Application Next.js

## Temps de Démarrage Estimé

- Infrastructure : ~30 secondes
- Eureka Server : ~40 secondes
- Microservices : ~2-3 minutes (démarrage progressif)
- Frontend : ~20 secondes
- **Total : ~4-5 minutes**

## Commandes Utiles

### Gestion des Conteneurs

```powershell
# Arrêter tous les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v

# Redémarrer un service spécifique
docker-compose restart user-service

# Voir les ressources utilisées
docker stats

# Voir les logs des 100 dernières lignes
docker-compose logs --tail=100
```

### Debugging

```powershell
# Accéder au shell d'un conteneur
docker exec -it eduschedule-user-service sh

# Voir les variables d'environnement d'un conteneur
docker exec eduschedule-user-service env

# Inspecter un conteneur
docker inspect eduschedule-user-service

# Voir les réseaux
docker network ls
docker network inspect eduschedule_eduschedule-network
```

### Nettoyage

```powershell
# Supprimer les conteneurs arrêtés
docker container prune

# Supprimer les images non utilisées
docker image prune

# Supprimer les volumes non utilisés
docker volume prune

# Nettoyage complet (ATTENTION : supprime tout)
docker system prune -a --volumes
```

## URLs d'Accès

Une fois tous les services démarrés :

- **Frontend** : http://localhost:3000
- **API Gateway** : http://localhost:8080
- **Eureka Dashboard** : http://localhost:8761
- **RabbitMQ Management** : http://localhost:15672 (iusjc/iusjc2025)
- **Zipkin** : http://localhost:9411

## Résolution des Problèmes

### Problème : Un service ne démarre pas

```powershell
# Voir les logs du service
docker-compose logs service-name

# Vérifier l'état du conteneur
docker-compose ps service-name

# Redémarrer le service
docker-compose restart service-name
```

### Problème : Erreur de connexion à MySQL

```powershell
# Vérifier que MySQL est prêt
docker exec eduschedule-mysql mysqladmin ping -h localhost -uiusjc -piusjc2025

# Vérifier les logs MySQL
docker-compose logs mysql

# Recréer le conteneur MySQL
docker-compose down mysql
docker-compose up -d mysql
```

### Problème : Services non enregistrés dans Eureka

```powershell
# Attendre 1-2 minutes supplémentaires
# Vérifier les logs du service
docker-compose logs service-name

# Vérifier la configuration Eureka
docker exec service-name env | grep EUREKA
```

### Problème : Manque de mémoire

```powershell
# Augmenter la mémoire allouée à Docker Desktop
# Settings > Resources > Memory : minimum 8 GB

# Limiter les services démarrés
docker-compose up -d mysql redis rabbitmq eureka-server api-gateway user-service frontend
```

### Problème : Port déjà utilisé

```powershell
# Trouver le processus utilisant le port
netstat -ano | findstr :8080

# Arrêter le processus ou changer le port dans docker-compose.yml
```

## Configuration de Production

### 1. Sécurité

- Changer tous les mots de passe par défaut dans `.env`
- Générer une nouvelle clé JWT secrète
- Configurer HTTPS avec des certificats SSL
- Limiter l'exposition des ports

### 2. Performance

```yaml
# Ajouter des limites de ressources dans docker-compose.yml
services:
  user-service:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
```

### 3. Monitoring

- Activer les métriques Prometheus
- Configurer des alertes
- Mettre en place des health checks avancés

### 4. Backup

```powershell
# Backup de la base de données
docker exec eduschedule-mysql mysqldump -uiusjc -piusjc2025 iusjcdb > backup.sql

# Backup des volumes
docker run --rm -v eduschedule_mysql-data:/data -v ${PWD}:/backup alpine tar czf /backup/mysql-backup.tar.gz /data
```

## Mise à Jour

```powershell
# 1. Arrêter les services
docker-compose down

# 2. Mettre à jour le code
git pull

# 3. Reconstruire les images
build-docker-images.bat

# 4. Redémarrer les services
docker-compose up -d
```

## Support

Pour plus d'informations :
- Voir les logs : `docker-compose logs -f`
- Vérifier la santé : `.\verify-services-communication.ps1`
- Documentation : README.md
