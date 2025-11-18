# 🚀 Commandes Rapides - IUSJC Planning 2025

## 🏗️ Build & Démarrage

```bash
# Build Maven (tous les services)
mvn clean package -DskipTests

# Build un service spécifique
cd user-service && mvn clean package -DskipTests

# Démarrer tout avec Docker
docker-compose up --build

# Démarrer en arrière-plan
docker-compose up -d

# Démarrer un service spécifique
docker-compose up user-service

# Arrêter tout
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v
```

## 🔍 Monitoring & Logs

```bash
# Voir les logs de tous les services
docker-compose logs -f

# Voir les logs d'un service spécifique
docker-compose logs -f user-service
docker-compose logs -f scheduling-service
docker-compose logs -f notification-service

# Voir les services en cours
docker-compose ps

# Voir l'utilisation des ressources
docker stats
```

## 🌐 URLs d'Accès

```bash
# Frontend Web
http://localhost:8090

# API Gateway
http://localhost:8080

# Eureka Dashboard (14 services)
http://localhost:8761

# RabbitMQ Management
http://localhost:15672
# Login: iusjc / iusjc2024

# MySQL
mysql -h localhost -P 3306 -u iusjc -piusjc2024 iusjcdb
```

## 🧪 Tests API

### user-service

```bash
# Créer un utilisateur
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@iusjc.cm",
    "password": "password123",
    "role": "STUDENT"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'

# Lister les utilisateurs
curl http://localhost:8080/api/users

# Obtenir un utilisateur
curl http://localhost:8080/api/users/1
```

### scheduling-service

```bash
# Créer un emploi du temps
curl -X POST http://localhost:8080/api/schedules \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Cours de Java Avancé",
    "description": "Microservices avec Spring Boot",
    "startTime": "2025-01-20T08:00:00",
    "endTime": "2025-01-20T10:00:00",
    "room": "A101",
    "teacher": "Prof. Dupont",
    "course": "Java",
    "groupName": "ISI 4A"
  }'

# Lister les emplois du temps
curl http://localhost:8080/api/schedules

# Obtenir un emploi du temps
curl http://localhost:8080/api/schedules/1

# Emplois du temps par enseignant
curl http://localhost:8080/api/schedules/teacher/1

# Emplois du temps par groupe
curl http://localhost:8080/api/schedules/group/ISI%204A
```

### notification-service

```bash
# Lister les notifications
curl http://localhost:8080/api/notifications

# Obtenir une notification
curl http://localhost:8080/api/notifications/1

# Notifications par destinataire
curl http://localhost:8080/api/notifications/recipient/student1@iusjc.cm
```

## 🗄️ Base de Données

### Connexion MySQL

```bash
# Via Docker
docker exec -it iusjc-planning-2025-mysql-1 mysql -u iusjc -piusjc2024 iusjcdb

# Via client local
mysql -h localhost -P 3306 -u iusjc -piusjc2024 iusjcdb
```

### Requêtes Utiles

```sql
-- Voir toutes les tables
SHOW TABLES;

-- Compter les utilisateurs
SELECT COUNT(*) FROM users;

-- Voir les emplois du temps
SELECT * FROM schedules ORDER BY start_time DESC LIMIT 10;

-- Voir les notifications
SELECT * FROM notifications ORDER BY created_at DESC LIMIT 10;

-- Statistiques
SELECT 
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM schedules) as total_schedules,
    (SELECT COUNT(*) FROM notifications) as total_notifications;

-- Utilisateurs par rôle
SELECT role, COUNT(*) as count FROM users GROUP BY role;

-- Emplois du temps par jour
SELECT DATE(start_time) as date, COUNT(*) as count 
FROM schedules 
GROUP BY DATE(start_time) 
ORDER BY date DESC;
```

## 📦 Redis

```bash
# Connexion Redis
docker exec -it iusjc-planning-2025-redis-1 redis-cli

# Voir toutes les clés
KEYS *

# Voir une clé spécifique
GET schedule:1

# Voir le TTL d'une clé
TTL schedule:1

# Supprimer toutes les clés (ATTENTION!)
FLUSHALL

# Statistiques
INFO stats
```

## 🐰 RabbitMQ

```bash
# Management UI
http://localhost:15672
# Login: iusjc / iusjc2024

# CLI - Lister les queues
docker exec iusjc-planning-2025-rabbitmq-1 rabbitmqctl list_queues

# CLI - Lister les exchanges
docker exec iusjc-planning-2025-rabbitmq-1 rabbitmqctl list_exchanges

# CLI - Lister les bindings
docker exec iusjc-planning-2025-rabbitmq-1 rabbitmqctl list_bindings
```

## 🔧 Maintenance

### Nettoyer Docker

```bash
# Supprimer les conteneurs arrêtés
docker container prune

# Supprimer les images non utilisées
docker image prune

# Supprimer les volumes non utilisés
docker volume prune

# Tout nettoyer (ATTENTION!)
docker system prune -a --volumes
```

### Rebuild un service

```bash
# Rebuild et redémarrer un service
docker-compose up --build user-service

# Rebuild sans cache
docker-compose build --no-cache user-service
```

### Réinitialiser la base de données

```bash
# Arrêter tout
docker-compose down -v

# Redémarrer (init-db.sql sera réexécuté)
docker-compose up mysql
```

## 📊 Vérifications

### Vérifier que tout fonctionne

```bash
# 1. Vérifier Eureka (doit afficher 14 services)
curl http://localhost:8761/eureka/apps | grep "<application>"

# 2. Vérifier MySQL
docker exec iusjc-planning-2025-mysql-1 mysql -u iusjc -piusjc2024 -e "SHOW DATABASES;"

# 3. Vérifier Redis
docker exec iusjc-planning-2025-redis-1 redis-cli PING

# 4. Vérifier RabbitMQ
docker exec iusjc-planning-2025-rabbitmq-1 rabbitmqctl status

# 5. Tester l'API Gateway
curl http://localhost:8080/actuator/health

# 6. Tester user-service
curl http://localhost:8080/api/users

# 7. Tester scheduling-service
curl http://localhost:8080/api/schedules

# 8. Tester notification-service
curl http://localhost:8080/api/notifications
```

## 🐛 Debug

### Problèmes courants

```bash
# Port déjà utilisé
# Solution: Arrêter le processus ou changer le port dans docker-compose.yml

# Service ne démarre pas
docker-compose logs [service-name]

# Base de données non accessible
docker-compose restart mysql

# Eureka ne voit pas les services
# Attendre 30 secondes après le démarrage

# Cache Redis ne fonctionne pas
docker-compose restart redis

# RabbitMQ ne reçoit pas les messages
docker-compose logs rabbitmq
docker-compose logs scheduling-service
docker-compose logs notification-service
```

## 📝 Développement

### Ajouter une nouvelle entité

```bash
# 1. Créer l'entité JPA dans src/main/java/.../entity/
# 2. Créer le repository dans src/main/java/.../repository/
# 3. Créer le service dans src/main/java/.../service/
# 4. Créer le controller dans src/main/java/.../controller/
# 5. Rebuild le service
mvn clean package -DskipTests
docker-compose up --build [service-name]
```

### Ajouter une nouvelle table

```bash
# 1. Modifier init-db.sql
# 2. Supprimer le volume MySQL
docker-compose down -v
# 3. Redémarrer
docker-compose up mysql
```

## 🎯 Commandes pour la Démo

```bash
# 1. Démarrer tout
docker-compose up -d

# 2. Attendre 30 secondes

# 3. Ouvrir Eureka (montrer 14 services)
open http://localhost:8761

# 4. Créer un utilisateur
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","email":"demo@iusjc.cm","password":"demo123","role":"STUDENT"}'

# 5. Créer un emploi du temps
curl -X POST http://localhost:8080/api/schedules \
  -H "Content-Type: application/json" \
  -d '{"title":"Démo Planning","startTime":"2025-01-20T10:00:00","endTime":"2025-01-20T12:00:00","room":"B202"}'

# 6. Vérifier RabbitMQ (montrer le message)
open http://localhost:15672

# 7. Vérifier les notifications
curl http://localhost:8080/api/notifications

# 8. Montrer le cache Redis
docker exec -it iusjc-planning-2025-redis-1 redis-cli KEYS "*"
```

---

**Astuce :** Garde ce fichier ouvert dans un terminal pendant le développement ! 🚀
