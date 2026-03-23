# EduSchedule - IUSJC
## Système de Gestion des Emplois du Temps
### Institut Universitaire Saint Jean - Cameroun

Plateforme de gestion d'emploi du temps pour établissements d'enseignement, basée sur une architecture microservices Spring Boot avec frontend Angular et intelligence artificielle intégrée.

---

## Prérequis

- **Java** 17+
- **Node.js** 18+ avec npm
- **Docker & Docker Compose** 20.10+
- **MariaDB / MySQL** 8.0+ (ou via Docker)
- **Maven** 3.8+

---

## Démarrage Rapide

### Avec Docker (recommandé)
```bash
cp .env.example .env
# Éditer .env avec vos valeurs
docker-compose up -d
```

### Sans Docker (développement local)
```bash
# 1. Démarrer les services essentiels (Eureka, Config, Gateway, User)
.\start-services-essentiels.bat

# 2. Démarrer les services opérationnels
.\start-services-operationnels.bat

# 3. Démarrer le frontend Angular
cd frontend-angular
npm install
npm start
```

> Note : le user-service utilise MariaDB (`jdbc:mariadb://`). Assurez-vous que la variable d'environnement système `SPRING_DATASOURCE_URL` n'est pas définie avec `jdbc:mysql://`, sinon elle prendra le dessus sur la configuration locale.

### Accès
| Service | URL |
|---------|-----|
| Frontend Angular | http://localhost:4200 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Management | http://localhost:15672 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

---

## Architecture

### Services Backend (Spring Boot)

| Service | Port | Rôle |
|---------|------|------|
| eureka-server | 8761 | Service Discovery |
| config-server | 8888 | Configuration centralisée |
| api-gateway | 8080 | Point d'entrée API, routage, CORS, JWT |
| user-service | 8081 | Authentification & gestion des utilisateurs |
| resource-service | 8082 | Ressources matérielles |
| school-service | 8083 | Établissements, filières, niveaux, groupes |
| course-service | 8084 | Cours & emplois du temps |
| scheduling-service | 8085 | Génération des plannings |
| reservation-service | 8086 | Réservations de salles |
| notification-service | 8087 | Notifications multi-canal (email, RabbitMQ) |
| reporting-service | 8091 | Rapports & analytics |
| teacher-availability-service | 8092 | Disponibilités enseignants |
| ai-service | 8093 | Analytics & prédictions IA |
| maintenance-service | 8094 | Monitoring & maintenance |
| room-service | 8095 | Gestion des salles |
| calendar-service | 8090 | Intégration Google Calendar / Outlook |
| event-service | 8089 | Événements académiques |
| ent-integration-service | dynamique | Intégration ENT externe |

### Frontend

- **frontend-angular/** — Application Angular 17+ (frontend principal actif, port 4200)
- **frontend/** — Application React/Next.js (référence, non maintenue activement)

### Infrastructure

- **MariaDB / MySQL 8.0** — Base de données partagée `iusjcdb` (port 3306)
- **Redis** — Cache distribué (port 6379)
- **RabbitMQ** — Messaging asynchrone (ports 5672 / 15672)
- **Prometheus / Grafana** — Monitoring (ports 9090 / 3001)

---

## Configuration

### Variables d'environnement (`.env`)

Copier `.env.example` en `.env` et adapter les valeurs :

```env
# Base de données
MYSQL_DATABASE=iusjcdb
MYSQL_USER=iusjc
MYSQL_PASSWORD=iusjc2025
MYSQL_ROOT_PASSWORD=root

# JWT
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRATION=86400000

# Email (SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_DEFAULT_USER=iusjc
RABBITMQ_DEFAULT_PASS=iusjc2025
```

> Important : ne pas définir `SPRING_DATASOURCE_URL` avec `jdbc:mysql://` si vous utilisez le user-service (MariaDB driver). Utiliser `jdbc:mariadb://` ou laisser la valeur par défaut du `application.properties`.

---

## Scripts disponibles

### Démarrage
```bash
.\start-services-essentiels.bat      # Eureka + Config + Gateway + User
.\start-services-operationnels.bat   # Tous les services métier
.\stop-all-services.bat              # Arrêter tous les services
```

### Build
```bash
.\build-all-services.bat             # Build Maven de tous les services
# Skipper les tests si besoin :
mvn spring-boot:run -DskipTests
```

### Tests & Diagnostic
```bash
.\test-all-services-complete.ps1     # Test complet du système
.\test-all-services.ps1              # Test des services essentiels
.\test-complete.ps1                  # Test rapide
.\check-services-status.bat          # Vérifier l'état des services
.\diagnose-api-connectivity.ps1      # Diagnostiquer la connectivité API
```

### Administration
```bash
.\create-admin-user.ps1              # Créer un utilisateur admin
.\init-databases.bat                 # Initialiser les bases de données
```

---

## Dépannage

### user-service ne démarre pas — `Driver claims to not accept jdbcUrl`
Le driver MariaDB refuse une URL `jdbc:mysql://`. Cause : variable d'environnement système `SPRING_DATASOURCE_URL` définie avec le mauvais protocole.
```powershell
# Vérifier
$env:SPRING_DATASOURCE_URL
# Supprimer pour la session
Remove-Item Env:SPRING_DATASOURCE_URL
```

### Erreur de compilation tests — `package org.junit.jupiter.api does not exist`
La dépendance `spring-boot-starter-test` manque dans le `pom.xml` du service concerné. Ajouter :
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
Ou lancer sans les tests : `mvn spring-boot:run -DskipTests`

### Services ne démarrent pas (Docker)
```bash
docker-compose logs -f [service-name]
docker-compose restart [service-name]
```

### Base de données inaccessible
```bash
docker-compose down
docker volume rm eduschedule_mysql_data
docker-compose up -d
```

### Erreur 401 Unauthorized
1. Se déconnecter / reconnecter
2. Vider le localStorage : F12 > Application > Local Storage > Clear
3. Redémarrer le user-service : `docker-compose restart user-service`

### Services ne communiquent pas
1. Vérifier Eureka : http://localhost:8761
2. Redémarrer l'API Gateway : `docker-compose restart api-gateway`

---

## Structure du projet

```
eduschedule/
├── api-gateway/                      # Point d'entrée API (8080)
├── eureka-server/                    # Service Discovery (8761)
├── config-server/                    # Configuration centralisée (8888)
├── user-service/                     # Auth & Utilisateurs (8081)
├── resource-service/                 # Ressources matérielles (8082)
├── school-service/                   # Établissements, filières, groupes (8083)
├── course-service/                   # Cours & emplois du temps (8084)
├── scheduling-service/               # Génération des plannings (8085)
├── reservation-service/              # Réservations de salles (8086)
├── notification-service/             # Notifications (8087)
├── reporting-service/                # Rapports & analytics (8091)
├── teacher-availability-service/     # Disponibilités enseignants (8092)
├── ai-service/                       # Analytics IA (8093)
├── maintenance-service/              # Monitoring (8094)
├── room-service/                     # Gestion des salles (8095)
├── calendar-service/                 # Intégration calendriers (8090)
├── event-service/                    # Événements académiques (8089)
├── ent-integration-service/          # Intégration ENT (port dynamique)
├── integration-service/              # Service d'intégration générique
├── frontend-angular/                 # Frontend Angular (4200) ← actif
├── frontend/                         # Frontend React/Next.js (3000) ← référence
├── docker-compose.yml
├── pom.xml                           # POM parent Maven
├── .env                              # Variables d'environnement
└── .env.example                      # Template des variables
```

---

## Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/ma-fonctionnalite`)
3. Commiter (`git commit -m 'feat: description'`)
4. Push (`git push origin feature/ma-fonctionnalite`)
5. Créer une Pull Request

### Standards
- **Backend** : Java 17, Spring Boot 3.2.x, architecture microservices
- **Frontend** : TypeScript, Angular 17+, Tailwind CSS
- **Tests** : JUnit 5 (`spring-boot-starter-test`), Jasmine/Karma
- **Commits** : Conventional Commits
- **Base de données** : MariaDB/MySQL 8, base partagée `iusjcdb`

---

## Licence

MIT — © Institut Universitaire Saint Jean - Cameroun
