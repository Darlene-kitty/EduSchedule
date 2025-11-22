# 🏗️ Architecture EduSchedule

## Vue d'Ensemble

EduSchedule est un système de gestion de planning académique basé sur une architecture microservices.

### Stack Technique
- **Java 17** + **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.1** (Config Server, Gateway, Eureka)
- **MySQL 8.0** + **Redis** + **RabbitMQ**
- **Docker** + **Docker Compose**
- **JWT** + **Spring Security**

---

## Microservices

### Services Principaux

#### 1. Config Server (Port 8888)
- Centralisation de la configuration
- Git backend pour versioning
- Refresh dynamique

#### 2. API Gateway (Port 8080)
- Point d'entrée unique
- Routage intelligent
- Authentification JWT
- Rate limiting

#### 3. User Service (Port 8081)
- Authentification & Autorisation
- Gestion des utilisateurs
- JWT tokens
- Rôles: ADMIN, TEACHER, STUDENT

#### 4. Resource Service (Port 8082)
- Gestion des salles
- Gestion des équipements
- Cache Redis

### Services Additionnels

- **Eureka Server** (8761) - Service Discovery
- **School Service** - Écoles/Filières/Groupes
- **Course Service** - Cours & Supports
- **Scheduling Service** - Emplois du temps
- **Reservation Service** - Réservations
- **Event Service** - Événements
- **Notification Service** - Notifications
- **Reporting Service** - Rapports
- **ENT Integration Service** - Intégration ENT
- **Frontend** (8090) - Interface web

---

## Base de Données

### Structure
```
iusjcdb (MySQL 8.0)
├── users
├── roles
├── refresh_tokens
├── password_reset_tokens
├── email_verification_tokens
├── salles
├── equipements
└── ... (autres tables)
```

### Stratégie
- **1 seule base MySQL** partagée entre tous les services
- Schémas logiquement séparés par préfixe de table
- Optimisation pour développement rapide

---

## Sécurité

### Authentification
- JWT (JSON Web Tokens)
- Access Token: 24h
- Refresh Token: 7 jours
- Remember Me: 30 jours

### Autorisation
- RBAC (Role-Based Access Control)
- 3 rôles: ADMIN, TEACHER, STUDENT
- Spring Security + @PreAuthorize

### Endpoints Publics
- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/refresh
- POST /api/auth/forgot-password
- POST /api/auth/reset-password
- GET /api/auth/verify-email

### Endpoints Protégés
- Tous les autres endpoints nécessitent un JWT valide
- Vérification des rôles via @PreAuthorize

---

## Configuration

### Variables d'Environnement (.env)

```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=iusjcdb
DB_USER=root
DB_PASSWORD=root

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Services Ports
CONFIG_SERVER_PORT=8888
EUREKA_SERVER_PORT=8761
API_GATEWAY_PORT=8080
USER_SERVICE_PORT=8081
RESOURCE_SERVICE_PORT=8082

# Email (optionnel)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@example.com
MAIL_PASSWORD=your-password
```

---

## Déploiement

### Prérequis
- Java 17
- Maven 3.8+
- Docker & Docker Compose

### Ordre de Démarrage

1. **Infrastructure**
   ```bash
   docker-compose up -d mysql redis rabbitmq
   ```

2. **Config Server**
   ```bash
   cd config-server
   mvn spring-boot:run
   ```

3. **Eureka Server** (optionnel)
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```

4. **API Gateway**
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

5. **Services Métier**
   ```bash
   cd user-service && mvn spring-boot:run
   cd resource-service && mvn spring-boot:run
   ```

### Avec Docker Compose (Tout en un)
```bash
docker-compose up -d
```

---

## Endpoints Principaux

### Authentification
```
POST   /api/auth/register          - Inscription
POST   /api/auth/login             - Connexion
POST   /api/auth/refresh           - Rafraîchir token
POST   /api/auth/logout            - Déconnexion
GET    /api/auth/me                - Profil utilisateur
POST   /api/auth/forgot-password   - Mot de passe oublié
POST   /api/auth/reset-password    - Réinitialiser mot de passe
GET    /api/auth/verify-email      - Vérifier email
```

### Utilisateurs (ADMIN)
```
GET    /api/users                  - Liste utilisateurs
GET    /api/users/{id}             - Détails utilisateur
PUT    /api/users/{id}             - Modifier utilisateur
DELETE /api/users/{id}             - Supprimer utilisateur
```

### Ressources
```
GET    /api/v1/salles              - Liste salles
GET    /api/v1/salles/{id}         - Détails salle
POST   /api/v1/salles              - Créer salle
PUT    /api/v1/salles/{id}         - Modifier salle
DELETE /api/v1/salles/{id}         - Supprimer salle
```

---

## Compilation

### Service Individuel
```bash
cd user-service
mvn clean compile -DskipTests
```

### Tous les Services
```bash
mvn clean compile -DskipTests
```

### Package
```bash
mvn clean package -DskipTests
```

---

## Tests

### Test Authentification
```bash
# Inscription
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"test123","role":"STUDENT"}'

# Connexion
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'

# Profil (avec token)
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Monitoring

### Health Checks
```
GET /actuator/health           - État du service
GET /actuator/info             - Informations
GET /actuator/metrics          - Métriques
```

### Eureka Dashboard
```
http://localhost:8761
```

### Config Server
```
http://localhost:8888/application/default
```

---

## Développement

### Structure d'un Service
```
service-name/
├── src/main/java/cm/iusjc/service/
│   ├── controller/          - REST Controllers
│   ├── service/             - Business Logic
│   ├── repository/          - Data Access
│   ├── entity/              - JPA Entities
│   ├── dto/                 - Data Transfer Objects
│   ├── config/              - Configuration
│   ├── filter/              - Filters
│   └── exception/           - Exception Handlers
├── src/main/resources/
│   ├── application.properties
│   └── bootstrap.properties
└── pom.xml
```

### Bonnes Pratiques
- Utiliser les DTOs pour les API
- Validation avec @Valid
- Gestion d'erreurs centralisée
- Logging avec Slf4j
- Documentation avec Swagger (à venir)

---

## Troubleshooting

### Port déjà utilisé
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Base de données inaccessible
```bash
docker-compose restart mysql
docker-compose logs mysql
```

### Service ne démarre pas
```bash
# Vérifier les logs
docker-compose logs service-name

# Rebuild
mvn clean install -DskipTests
```

---

## Roadmap

### Phase 1 - Infrastructure ✅
- Config Server
- API Gateway
- Eureka Server
- Base de données

### Phase 2 - Authentification ✅
- User Service
- JWT
- Refresh Tokens
- Remember Me
- Password Reset
- Email Verification

### Phase 3 - Services Métier ⏳
- Resource Service
- Scheduling Service
- Notification Service

### Phase 4 - Frontend ⏳
- Interface web Thymeleaf
- Dashboard
- Gestion planning

### Phase 5 - Optimisation ⏳
- Cache Redis
- Message Queue RabbitMQ
- Monitoring
- Tests automatisés

---

## Support

Pour toute question ou problème, consulter la documentation ou créer une issue.
