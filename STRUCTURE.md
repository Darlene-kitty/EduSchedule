# Structure du Projet IUSJC Planning 2025

## Vue d'ensemble

Projet transversal ISI 2025-2026 - Système de gestion d'emplois du temps basé sur une architecture microservices.

## Arborescence Complète

```
iusjc-planning-2025/
├── pom.xml                              ← Parent Maven
├── docker-compose.yml                   ← Orchestration Docker
├── init-db.sql                          ← Script d'initialisation MySQL
├── README.md
├── MIGRATION_MYSQL.md
├── STRUCTURE.md
│
├── .github/
│   └── workflows/
│       └── ci.yml                       ← Pipeline CI/CD
│
├── eureka-server/                       ← Service Discovery (8761)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/eureka/
│       └── resources/
│           └── application.properties
│
├── api-gateway/                         ← API Gateway (8080)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/gateway/
│       └── resources/
│           └── application.properties
│
├── user-service/                        ← Authentification & Autorisation
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/userservice/
│       └── resources/
│           └── application.properties   (DB: authdb)
│
├── school-service/                      ← Écoles/Filières/Groupes
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/schoolservice/
│       └── resources/
│           └── application.properties   (DB: schooldb)
│
├── resource-service/                    ← Gestion des ressources
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/resource/
│       └── resources/
│           └── application.properties   (DB: resourcedb + Redis)
│
├── room-service/                        ← Gestion des salles
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/roomservice/
│       └── resources/
│           └── application.properties   (DB: roomdb)
│
├── course-service/                      ← Cours & supports pédagogiques
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/course/
│       └── resources/
│           └── application.properties   (DB: coursedb)
│
├── scheduling-service/                  ← Génération emplois du temps
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/scheduling/
│       └── resources/
│           └── application.properties   (DB: schedulingdb)
│
├── reservation-service/                 ← Réservations
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/reservation/
│       └── resources/
│           └── application.properties   (DB: reservationdb + RabbitMQ)
│
├── event-service/                       ← Gestion des événements
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/eventservice/
│       └── resources/
│           └── application.properties   (DB: eventdb)
│
├── notification-service/                ← Notifications (Email/SMS)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/notification/
│       └── resources/
│           └── application.properties   (RabbitMQ + SMTP)
│
├── reporting-service/                   ← Rapports PDF/Excel
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/reporting/
│       └── resources/
│           └── application.properties   (DB: reportingdb)
│
├── ent-integration-service/             ← Intégration ENT
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/cm/iusjc/ent/
│       └── resources/
│           └── application.properties
│
└── frontend-thymeleaf/                  ← Interface Web (8090)
    ├── pom.xml
    ├── Dockerfile
    └── src/main/
        ├── java/cm/iusjc/frontend/
        └── resources/
            ├── application.properties
            ├── templates/
            │   └── index.html           (Thymeleaf + Tailwind CSS)
            └── static/
```

## Services et Ports

| Service | Port | Base de données | Description |
|---------|------|-----------------|-------------|
| eureka-server | 8761 | - | Service Discovery |
| api-gateway | 8080 | - | Point d'entrée API |
| frontend-thymeleaf | 8090 | - | Interface Web |
| user-service | dynamique | authdb | Authentification |
| school-service | dynamique | schooldb | Écoles/Filières |
| resource-service | dynamique | resourcedb | Ressources |
| room-service | dynamique | roomdb | Salles |
| course-service | dynamique | coursedb | Cours |
| scheduling-service | dynamique | schedulingdb | Emplois du temps |
| reservation-service | dynamique | reservationdb | Réservations |
| event-service | dynamique | eventdb | Événements |
| notification-service | dynamique | - | Notifications |
| reporting-service | dynamique | reportingdb | Rapports |
| ent-integration-service | dynamique | - | Intégration ENT |

## Infrastructure

- **MySQL 8.0** (port 3306) - 9 bases de données
- **Redis 7** (port 6379) - Cache
- **RabbitMQ 3** (ports 5672, 15672) - Messaging

## Convention de Nommage

- **Noms de services Eureka**: MAJUSCULES avec tirets (ex: USER-SERVICE)
- **Packages Java**: minuscules (ex: cm.iusjc.userservice)
- **Bases de données**: minuscules avec suffixe 'db' (ex: authdb)
- **Images Docker**: darlene4/iusjc-[service]:latest

## Technologies

- Java 17
- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- MySQL 8.0
- Thymeleaf + Tailwind CSS
- Docker & Docker Compose
- Maven

## Commandes Utiles

```bash
# Build tous les services
mvn clean package -DskipTests

# Lancer l'infrastructure complète
docker-compose up --build

# Lancer un service spécifique
docker-compose up [service-name]

# Voir les logs d'un service
docker-compose logs -f [service-name]

# Arrêter tous les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v
```

## URLs d'Accès

- Frontend: http://localhost:8090
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- RabbitMQ Management: http://localhost:15672 (iusjc/iusjc2024)
- MySQL: localhost:3306 (iusjc/iusjc2024)
