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
| resource-service | 8082 | Ressources matérielles + suivi maintenance |
| school-service | 8083 | Établissements, filières, niveaux, groupes |
| course-service | 8084 | Cours, emplois du temps & planning examens |
| scheduling-service | 8085 | Génération des plannings |
| reservation-service | 8086 | Réservations de salles + approbation workflow |
| notification-service | 8087 | Notifications multi-canal (email, RabbitMQ, WebSocket) |
| reporting-service | 8088 | Rapports & analytics |
| calendar-service | 8089 | Intégration Google Calendar / Outlook |
| event-service | 8091 | Événements académiques |
| teacher-availability-service | 8092 | Disponibilités enseignants |
| ai-service | 8093 | Analytics & prédictions IA |
| maintenance-service | 8094 | Monitoring & maintenance |
| room-service | 8090 | Gestion des salles |
| ent-integration-service | 8095 | Intégration ENT externe |

### Frontend

- **frontend-angular/** — Application Angular 17+ (frontend principal actif, port 4200)
- **frontend/** — Application React/Next.js (référence, non maintenue activement)

### Infrastructure

- **MariaDB / MySQL 8.0** — Base de données partagée `iusjcdb` (port 3306)
- **Redis** — Cache distribué (port 6379)
- **RabbitMQ** — Messaging asynchrone (ports 5672 / 15672)
- **WebSocket (STOMP/SockJS)** — Temps réel via notification-service (port 8087/ws)

---

## Fonctionnalités implémentées

### Planification des emplois du temps
- Génération automatique via algorithmes Ford-Fulkerson / Edmonds-Karp
- Prise en compte des écoles multiples par enseignant (`TeacherSchoolAssignment`)
- Gestion des disponibilités avec détection de conflits inter-écoles et temps de trajet
- Vue hebdomadaire, journalière et mensuelle dans le calendrier Angular
- Synchronisation bidirectionnelle Google Calendar / Outlook

### Réservation de salles
- Workflow complet : PENDING → CONFIRMED / REJECTED / CANCELLED
- Suggestion automatique de salle via `RoomOptimizationService` (scoring multi-critères)
- UI d'approbation admin avec notifications automatiques
- Détection de conflits avant création
- Import CSV/Excel

### Notifications temps réel
- Email (SMTP) via `EmailService`
- In-app via `NotificationService` (historique, marquage lu)
- WebSocket STOMP/SockJS — push immédiat sur `/topic/notifications`, `/topic/reservations`, `/topic/schedule`
- Événements RabbitMQ → WebSocket câblés : création, approbation, rejet, annulation de réservation, changements d'EDT

### Gestion des équipements
- Inventaire complet avec états (BON_ETAT, USAGE, EN_PANNE, EN_MAINTENANCE)
- Historique de maintenance par matériel (`MaintenanceMateriel`)
- Suivi d'utilisation lié aux réservations (`UsageMateriel`)
- Alertes de maintenance préventive (seuil heures, délai sans intervention)
- Tableau de bord dédié équipements (`/api/v1/equipment-usage/dashboard`)

### Planning d'examens
- Génération automatique avec contraintes : pas deux examens du même niveau au même créneau, pas deux examens du même enseignant simultanément, max examens/jour/niveau configurable
- Endpoint : `POST /api/v1/exams/scheduling/generate`
- Interface Angular dédiée avec configuration, résultats par date et liste des conflits

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

> Important : ne pas définir `SPRING_DATASOURCE_URL` avec `jdbc:mysql://` si vous utilisez le user-service (MariaDB driver).

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
# Build complet (tous les services)
.\build-all-services.bat

# Rebuild ciblé — uniquement les services modifiés récemment
.\rebuild-modified-services.bat

# Skipper les tests si besoin
mvn spring-boot:run -DskipTests
```

### Tests & Diagnostic
```bash
.\test-all-services-complete.ps1     # Test complet du système
.\check-services-status.bat          # Vérifier l'état des services
.\diagnose-api-connectivity.ps1      # Diagnostiquer la connectivité API
```

### Administration
```bash
.\create-admin-user.ps1              # Créer un utilisateur admin
.\init-databases.bat                 # Initialiser les bases de données
```

---

## Rebuild des services modifiés

Le script `rebuild-modified-services.bat` reconstruit uniquement les 4 services Java modifiés + le frontend Angular, sans toucher aux autres.

```bash
.\rebuild-modified-services.bat
```

Ce qu'il fait :
1. `mvn clean package -DskipTests` sur `reservation-service`, `notification-service`, `course-service`, `resource-service`
2. `npm install` dans `frontend-angular` (installe `sockjs-client` et `@stomp/stompjs`)
3. `docker compose build --no-cache` sur les 5 images

Après le build, redémarrer uniquement les services concernés :
```bash
docker compose up -d reservation-service notification-service course-service resource-service frontend-angular
```

### Services modifiés dans cette mise à jour

| Service | Modifications |
|---------|--------------|
| `reservation-service` | `ReservationEventPublisher` (RabbitMQ), `RabbitMQConfig` (exchange + JSON converter), notifications câblées sur create/approve/reject/cancel |
| `notification-service` | `WebSocketConfig` (STOMP/SockJS), `WebSocketNotificationService`, `ScheduleEventListener` câblé WebSocket |
| `course-service` | `ExamSchedulingService` + `ExamSchedulingController` (`POST /api/v1/exams/scheduling/generate`) |
| `resource-service` | `UsageMateriel`, `MaintenanceMateriel`, `EquipmentUsageService`, `EquipmentUsageController` |
| `frontend-angular` | `WebSocketService`, `RoomSuggestionService`, `ExamSchedulingService`, vues calendrier (Jour/Mois), UI approbation réservations, planning examens |

### Nouvelles routes API Gateway

| Route | Service | Description |
|-------|---------|-------------|
| `/api/v1/exams/**` | course-service | Planning d'examens |
| `/api/v1/equipment-usage/**` | resource-service | Suivi utilisation équipements |
| `/ws/**` | notification-service | WebSocket STOMP/SockJS |

### Nouvelles pages Angular

| Route | Composant | Accès |
|-------|-----------|-------|
| `/exam-scheduling` | `ExamSchedulingComponent` | Admin |
| `/reservations` (onglet "En attente") | `ReservationsComponent` | Admin |
| `/calendar` (vues Jour/Mois) | `CalendarComponent` | Admin + Enseignant |

---

## WebSocket — Connexion client

Le frontend Angular se connecte automatiquement au WebSocket au chargement des pages `notifications`, `reservations` et `calendar`.

Pour se connecter manuellement depuis un autre client :

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8087/ws'),
  onConnect: () => {
    // Toutes les notifications
    client.subscribe('/topic/notifications', msg => console.log(JSON.parse(msg.body)));
    // Événements de réservation
    client.subscribe('/topic/reservations', msg => console.log(JSON.parse(msg.body)));
    // Changements d'emploi du temps
    client.subscribe('/topic/schedule', msg => console.log(JSON.parse(msg.body)));
    // Notifications d'un utilisateur spécifique
    client.subscribe('/topic/notifications/42', msg => console.log(JSON.parse(msg.body)));
  }
});
client.activate();
```

---

## Dépannage

### user-service ne démarre pas — `Driver claims to not accept jdbcUrl`
```powershell
Remove-Item Env:SPRING_DATASOURCE_URL
```

### reservation-service ne publie pas sur RabbitMQ
Vérifier que RabbitMQ est démarré et que les credentials correspondent :
```bash
docker compose logs rabbitmq
docker compose restart reservation-service
```

### WebSocket ne se connecte pas
1. Vérifier que `notification-service` est démarré : http://localhost:8087/actuator/health
2. Vérifier les CORS dans `WebSocketConfig` (allowedOriginPatterns = `*` en dev)
3. Tester SockJS directement : http://localhost:8087/ws/info

### Erreur de compilation tests — `package org.junit.jupiter.api does not exist`
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
Ou : `mvn spring-boot:run -DskipTests`

### Services ne démarrent pas (Docker)
```bash
docker compose logs -f [service-name]
docker compose restart [service-name]
```

### Base de données inaccessible
```bash
docker compose down
docker volume rm eduschedule_mysql_data
docker compose up -d
```

### Erreur 401 Unauthorized
1. Se déconnecter / reconnecter
2. Vider le localStorage : F12 > Application > Local Storage > Clear
3. `docker compose restart user-service`

---

## Structure du projet

```
eduschedule/
├── api-gateway/                      # Point d'entrée API (8080)
├── eureka-server/                    # Service Discovery (8761)
├── config-server/                    # Configuration centralisée (8888)
├── user-service/                     # Auth & Utilisateurs (8081)
├── resource-service/                 # Ressources + suivi maintenance (8082)
├── school-service/                   # Établissements, filières, groupes (8083)
├── course-service/                   # Cours, EDT & planning examens (8084)
├── scheduling-service/               # Génération des plannings (8085)
├── reservation-service/              # Réservations + workflow approbation (8086)
├── notification-service/             # Notifications + WebSocket (8087)
├── reporting-service/                # Rapports & analytics (8088)
├── calendar-service/                 # Intégration calendriers (8089)
├── event-service/                    # Événements académiques (8091)
├── teacher-availability-service/     # Disponibilités enseignants (8092)
├── ai-service/                       # Analytics IA (8093)
├── maintenance-service/              # Monitoring (8094)
├── room-service/                     # Gestion des salles (8090)
├── ent-integration-service/          # Intégration ENT (8095)
├── frontend-angular/                 # Frontend Angular (4200) ← actif
├── frontend/                         # Frontend React/Next.js (3000) ← référence
├── docker-compose.yml
├── pom.xml                           # POM parent Maven
├── build-all-services.bat            # Build complet
├── rebuild-modified-services.bat     # Rebuild ciblé (services modifiés)
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
