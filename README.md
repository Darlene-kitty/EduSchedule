<<<<<<< HEAD
# IUSJC Planning 2025 - Projet Transversal ISI 2025-2026

## Architecture Microservices

### Stack Technique
- **Java 17** + **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.1** (Eureka, Gateway)
- **MySQL 8.0** + **Redis** + **RabbitMQ**
- **Thymeleaf** + **Tailwind CSS**(pour un debut)
- **Docker** + **Docker Compose**
- **OAuth2** + **JWT** + **RBAC**

### Microservices (14)
1. **eureka-server** (8761) - Service Discovery
2. **api-gateway** (8080) - Point d'entrée unique
3. **user-service** - Authentification & Autorisation
4. **school-service** - Gestion écoles/filières/groupes
5. **resource-service** - Gestion des ressources
6. **room-service** - Gestion des salles
7. **course-service** - Cours & supports
8. **scheduling-service** - Génération emplois du temps
9. **reservation-service** - Réservations
10. **event-service** - Gestion des événements
11. **notification-service** - Email/SMS/WebSocket
12. **reporting-service** - Rapports PDF/Excel
13. **ent-integration-service** - Intégration ENT
14. **frontend-thymeleaf** (8090) - Interface web

## 🎯 Architecture Optimisée

### Stratégie Niveau 4e Année
1. ✅ **14 microservices dans Eureka** → 20/20 Phase 4 + 3 points bonus
2. ✅ **1 seule base MySQL (iusjcdb)** → Gain de 2 mois de développement
3. ✅ **3 services "vrais"** → user-service, scheduling-service, notification-service

### Services Principaux (Développés)
- **user-service** ⭐ : Authentification JWT + Spring Security
- **scheduling-service** ⭐ : Emplois du temps + Redis + RabbitMQ
- **notification-service** ⭐ : Notifications asynchrones + SMTP

### Services Squelettes (Légers)
Les 9 autres services sont enregistrés dans Eureka mais sans DB (pour l'instant).

## Lancement

### Prérequis
- Docker & Docker Compose
- Java 17
- Maven 3.8+

### Configuration
```bash
# 1. Copier le fichier de configuration
cp .env.example .env

# 2. Modifier les valeurs si nécessaire (optionnel pour dev)
nano .env
```

### Démarrage complet
```bash
# Build tous les services
mvn clean package -DskipTests

# Lancer l'infrastructure
docker-compose up --build

# Vérifier que les 14 services sont dans Eureka
http://localhost:8761
```

**Note :** Le fichier `.env` contient toutes les variables d'environnement. Voir `ENV_CONFIGURATION.md` pour plus de détails.

### Accès
- **Frontend Web**: http://localhost:8090
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672 (iusjc/iusjc2024)
- **MySQL**: localhost:3306 (iusjc/iusjc2024)

## Base de Données

### MySQL Unique (iusjcdb)
Une seule base pour tous les services :
- Tables user-service (users, roles)
- Tables scheduling-service (schedules, time_slots)
- Tables notification-service (notifications)

**Connexion :**
```
Host: localhost:3306
Database: iusjcdb
User: iusjc
Password: iusjc2024
```

**Utilisateurs de test :**
- admin / password (ADMIN)
- teacher1 / password (TEACHER)

## CI/CD
GitHub Actions → Build → Push Docker Hub (darlene4/iusjc-*)

## Documentation
- `ARCHITECTURE_OPTIMISEE.md` : Stratégie et architecture détaillée
- `CHANGEMENTS_APPLIQUES.md` : Liste des modifications
- `STRUCTURE.md` : Structure complète du projet

## Structure du Projet
```
iusjc-planning-2025/
├── pom.xml                         ← Parent Maven
├── docker-compose.yml
├── .github/workflows/ci.yml
├── eureka-server/                  ← Service Discovery
├── api-gateway/                    ← API Gateway
├── user-service/                   ← Authentification
├── school-service/                 ← Écoles/Filières
├── resource-service/               ← Ressources
├── room-service/                   ← Salles
├── course-service/                 ← Cours
├── scheduling-service/             ← Emplois du temps
├── reservation-service/            ← Réservations
├── event-service/                  ← Événements
├── notification-service/           ← Notifications
├── reporting-service/              ← Rapports
├── ent-integration-service/        ← Intégration ENT
└── frontend-thymeleaf/             ← Interface Web (Thymeleaf + Tailwind)
```

## Diagramme
```
[Frontend:8090] → [Gateway:8080] → [Eureka:8761]
                        ↓
    ┌───────────────────┼───────────────────┐
    ↓                   ↓                   ↓
[User]            [School]            [Resource/Room]
    ↓                   ↓                   ↓
[Course]          [Scheduling]        [Reservation/Event]
    ↓                   ↓                   ↓
[Notification]    [Reporting]         [ENT Integration]
```
=======
"# EduSchedule - Gestion EDT & Salles IUSJC" 
>>>>>>> 05557e28d8cd303119551bee085611f321f0b3c9
