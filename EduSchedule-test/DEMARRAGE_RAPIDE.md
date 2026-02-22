# 🚀 Guide de Démarrage Rapide - EduSchedule

## Option 1 : Démarrage Sans Docker (Recommandé pour le développement)

### Prérequis
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### Étapes

#### 1. Initialiser la base de données
```bash
# Via MySQL Workbench (recommandé)
# Ouvrir et exécuter init-db.sql

# OU via script batch
.\init-databases.bat
```

#### 2. Compiler tous les services
```bash
mvn clean install -DskipTests
```

#### 3. Démarrer les services d'infrastructure
```bash
# Terminal 1 - Config Server
cd config-server
mvn spring-boot:run

# Terminal 2 - Eureka Server
cd eureka-server
mvn spring-boot:run

# Terminal 3 - API Gateway
cd api-gateway
mvn spring-boot:run
```

#### 4. Démarrer les microservices
```bash
# Terminal 4 - User Service
cd user-service
mvn spring-boot:run

# Terminal 5 - School Service
cd school-service
mvn spring-boot:run

# Terminal 6 - Course Service
cd course-service
mvn spring-boot:run

# Terminal 7 - Room Service
cd room-service
mvn spring-boot:run

# Terminal 8 - Reservation Service
cd reservation-service
mvn spring-boot:run

# Terminal 9 - Scheduling Service
cd scheduling-service
mvn spring-boot:run

# Terminal 10 - Notification Service
cd notification-service
mvn spring-boot:run
```

#### 5. Démarrer le Frontend
```bash
cd frontend
npm install --legacy-peer-deps
npm run dev
```

### URLs des Services
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888

---

## Option 2 : Démarrage Avec Docker (Sans Frontend)

### Étapes

#### 1. Build des services Java
```bash
mvn clean package -DskipTests
```

#### 2. Démarrer avec Docker Compose (sans frontend)
```bash
.\start-without-frontend.bat
```

#### 3. Démarrer le frontend séparément
```bash
cd frontend
npm install --legacy-peer-deps
npm run dev
```

---

## Option 3 : Démarrage Complet avec Docker

⚠️ **Attention**: Le build du frontend dans Docker peut prendre 10-15 minutes

### Étapes

#### 1. Build des services Java
```bash
mvn clean package -DskipTests
```

#### 2. Build et démarrer tous les services
```bash
docker-compose up --build
```

---

## Vérification du Démarrage

### 1. Vérifier Eureka
Ouvrir http://localhost:8761 et vérifier que tous les services sont enregistrés

### 2. Tester l'API Gateway
```bash
curl http://localhost:8080/actuator/health
```

### 3. Tester un service
```bash
curl http://localhost:8080/api/schools/health
```

### 4. Accéder au Frontend
Ouvrir http://localhost:3000

---

## Ordre de Démarrage Recommandé

1. **MySQL** (base de données)
2. **Redis** (cache)
3. **RabbitMQ** (messaging)
4. **Config Server** (configuration centralisée)
5. **Eureka Server** (service discovery)
6. **API Gateway** (routage)
7. **Services métier** (user, school, course, room, reservation, scheduling, notification)
8. **Frontend** (interface utilisateur)

---

## Dépannage

### Erreur "Port already in use"
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Ou changer le port dans application.yml
```

### Erreur "Connection refused" à MySQL
- Vérifier que MySQL est démarré
- Vérifier les credentials dans application.yml
- Vérifier que la base de données existe

### Erreur "Service not found" dans Eureka
- Attendre 30-60 secondes pour l'enregistrement
- Vérifier les logs du service
- Vérifier la configuration eureka dans application.yml

### Frontend ne démarre pas
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install --legacy-peer-deps
npm run dev
```

### Build Docker trop lent
- Utiliser l'Option 2 (sans frontend dans Docker)
- Augmenter la mémoire allouée à Docker
- Utiliser le cache Docker

---

## Scripts Utiles

### Windows
- `start-without-frontend.bat` - Démarrer sans frontend
- `init-databases.bat` - Initialiser les bases de données
- `build-all-services.bat` - Compiler tous les services
- `stop-all-services.bat` - Arrêter tous les services

### Commandes Maven
```bash
# Compiler sans tests
mvn clean install -DskipTests

# Compiler un service spécifique
mvn clean install -pl user-service -DskipTests

# Lancer les tests
mvn test

# Nettoyer
mvn clean
```

### Commandes Docker
```bash
# Voir les logs
docker-compose logs -f [service-name]

# Redémarrer un service
docker-compose restart [service-name]

# Arrêter tous les services
docker-compose down

# Supprimer les volumes
docker-compose down -v
```

---

## Prochaines Étapes

1. ✅ Vérifier que tous les services sont UP dans Eureka
2. ✅ Créer un utilisateur admin via l'API
3. ✅ Créer une école
4. ✅ Créer des salles
5. ✅ Créer des cours
6. ✅ Tester les réservations
7. ✅ Tester les notifications

---

## Support

Pour plus d'informations, consultez :
- `README.md` - Documentation générale
- `docs/ARCHITECTURE.md` - Architecture du système
- `GUIDE_INIT_DATABASE.md` - Guide d'initialisation de la base de données
