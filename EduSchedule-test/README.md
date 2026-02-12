# 🎓 EduSchedule - IUSJC
## Système de Gestion des Emplois du Temps Intelligent
### Institut Universitaire Saint Jean - Cameroun

Plateforme complète de gestion d'emploi du temps pour établissements d'enseignement avec architecture microservices Spring Boot, frontend React/Next.js, et intelligence artificielle intégrée.

## 🚀 Démarrage Rapide

### Démarrage Complet (Recommandé)
```bash
# Windows - Tous les microservices
.\start-services-complets.bat
```

### Démarrage Services Opérationnels (Optimisé)
```bash
# Windows - Services essentiels uniquement
.\start-services-operationnels.bat
```

### Démarrage Backend Seulement
```bash
# Backend sans frontend (pour développement)
.\start-backend-only.bat
```

### Construction Préalable (Optionnel)
```bash
# Construction de tous les services
.\build-all-services.bat
```

### Accès aux Services
- **🌐 Frontend**: http://localhost:3000
- **🔗 API Gateway**: http://localhost:8080
- **📊 Eureka Dashboard**: http://localhost:8761
- **🐰 RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **📈 Grafana**: http://localhost:3001 (admin/admin)
- **🔍 Prometheus**: http://localhost:9090

## 📋 Prérequis

- **Docker & Docker Compose** (version 20.10+)
- **Node.js** 18+ avec npm/pnpm
- **Java** 17+ (pour développement backend)
- **MySQL** 8.0+ (ou utiliser Docker)
- **Git** pour le versioning

## 🏗️ Architecture Microservices

### Services Backend (Spring Boot) - 17 Services
- **🔐 User Service** (8081) - Authentification, gestion utilisateurs
- **🏫 School Service** (8083) - Gestion multi-établissements
- **📚 Course Service** (8084) - Cours et groupes d'étudiants
- **🏢 Resource Service** (8082) - Gestion des salles et ressources
- **🏢 Room Service** (8090) - Gestion avancée des salles
- **📋 Reservation Service** (8086) - Réservations et conflits
- **⏰ Scheduling Service** (8085) - Génération d'emplois du temps
- **👨‍🏫 Teacher Availability Service** (8092) - Disponibilités enseignants
- **📅 Calendar Service** (8089) - Intégration Google Calendar
- **🔔 Notification Service** (8087) - Notifications multi-canal
- **📊 Reporting Service** (8088) - Rapports et analytics
- **🤖 AI Service** (8093) - Intelligence artificielle prédictive
- **📅 Event Service** (8091) - Gestion des événements
- **🔧 Maintenance Service** (8094) - Monitoring et maintenance
- **🔗 ENT Integration Service** (8095) - Intégration ENT
- **⚙️ Config Server** (8888) - Configuration centralisée
- **🌐 API Gateway** (8080) - Point d'entrée unique

### Frontend (React/Next.js)
- **Interface moderne** avec Tailwind CSS
- **Composants réutilisables** avec shadcn/ui
- **Gestion d'état** avec Context API
- **Authentification** JWT avec refresh tokens
- **Responsive design** mobile-first

### Infrastructure
- **🔍 Service Discovery**: Eureka Server (8761)
- **🌐 API Gateway**: Spring Cloud Gateway (8080)
- **⚙️ Configuration**: Config Server (8888)
- **💾 Base de données**: MySQL 8.0 (3306)
- **⚡ Cache**: Redis (6379)
- **📨 Messaging**: RabbitMQ (5672/15672)
- **📊 Monitoring**: Zipkin (9411)
- **🐳 Containerisation**: Docker & Docker Compose

## ✅ Fonctionnalités Complètes

### 🔐 Authentification & Sécurité
- ✅ **Authentification JWT complète** avec refresh tokens
- ✅ **Système de mot de passe oublié** avec emails HTML
- ✅ **Validation par email** avec tokens sécurisés
- ✅ **Gestion des rôles** (Admin, Enseignant, Étudiant)
- ✅ **Permissions granulaires** par fonctionnalité
- ✅ **Sessions sécurisées** avec expiration automatique

### 👥 Gestion des Utilisateurs
- ✅ **CRUD complet** des utilisateurs
- ✅ **Profils détaillés** avec photos et informations
- ✅ **Gestion multi-écoles** pour les enseignants
- ✅ **Import/Export** en masse des utilisateurs
- ✅ **Historique des connexions** et audit

### 📅 Gestion des Disponibilités Enseignants
- ✅ **Calendrier interactif** de disponibilités
- ✅ **Créneaux d'1 heure** avec validation automatique
- ✅ **Types de disponibilité** (Disponible, Préféré, Indisponible)
- ✅ **Récurrence hebdomadaire** ou ponctuelle
- ✅ **Priorités** et notes personnalisées
- ✅ **Vue mensuelle/hebdomadaire** avec filtres

### 🏫 Gestion Multi-Établissements
- ✅ **Écoles multiples** avec configurations spécifiques
- ✅ **Affectation enseignants** par établissement
- ✅ **Horaires personnalisés** par école
- ✅ **Ressources partagées** entre établissements

### 📚 Cours & Groupes
- ✅ **Gestion complète des cours** avec métadonnées
- ✅ **Groupes d'étudiants** avec effectifs
- ✅ **Assignation enseignants** par cours
- ✅ **Prérequis** et dépendances entre cours

### 🏢 Ressources & Salles
- ✅ **Inventaire complet** des salles et équipements
- ✅ **Capacités** et caractéristiques techniques
- ✅ **Disponibilité en temps réel** des ressources
- ✅ **Maintenance** et réservations

### 📋 Réservations Intelligentes
- ✅ **Réservation de salles** avec suggestions automatiques
- ✅ **Détection de conflits** en temps réel
- ✅ **Résolution assistée** des conflits
- ✅ **Historique complet** des réservations

### ⏰ Emplois du Temps
- ✅ **Génération automatique** d'emplois du temps
- ✅ **Optimisation IA** des créneaux
- ✅ **Vue calendrier** interactive
- ✅ **Export PDF/Excel** des plannings
- ✅ **Synchronisation** Google Calendar

### 🔔 Notifications Avancées
- ✅ **Notifications en temps réel** (WebSocket)
- ✅ **Emails HTML** personnalisés
- ✅ **SMS** pour urgences (intégration Twilio)
- ✅ **Notifications push** web
- ✅ **Préférences utilisateur** personnalisables

### 📊 Rapports & Analytics
- ✅ **Tableaux de bord** interactifs
- ✅ **Métriques en temps réel** d'utilisation
- ✅ **Rapports personnalisés** avec filtres
- ✅ **Export** multi-formats (PDF, Excel, CSV)
- ✅ **Analytics prédictives** avec IA

### 🤖 Intelligence Artificielle
- ✅ **Prédiction de conflits** avant qu'ils surviennent
- ✅ **Optimisation automatique** des emplois du temps
- ✅ **Suggestions intelligentes** de créneaux
- ✅ **Analyse des tendances** d'utilisation
- ✅ **Recommandations** personnalisées

### 🔧 Administration & Monitoring
- ✅ **Interface d'administration** complète
- ✅ **Monitoring** en temps réel des services
- ✅ **Logs centralisés** avec recherche
- ✅ **Métriques de performance** (Prometheus/Grafana)
- ✅ **Alertes automatiques** en cas de problème

## 📚 Documentation Complète

### 🚀 Guides Essentiels
- **[LIRE_EN_PREMIER.md](./LIRE_EN_PREMIER.md)** - Point de départ essentiel
- **[GUIDE_DISPONIBILITES_ENSEIGNANTS.md](./GUIDE_DISPONIBILITES_ENSEIGNANTS.md)** - Gestion des disponibilités
- **[GUIDE_FONCTIONNALITES_AVANCEES.md](./GUIDE_FONCTIONNALITES_AVANCEES.md)** - Fonctionnalités avancées
- **[VERIFICATION_FONCTIONNALITES.md](./VERIFICATION_FONCTIONNALITES.md)** - Tests fonctionnels

### 🔧 Documentation Technique
- **[PLAN_IMPLEMENTATION_FONCTIONNALITES.md](./PLAN_IMPLEMENTATION_FONCTIONNALITES.md)** - Plan d'implémentation
- **[INTEGRATION_FRONTEND_BACKEND.md](./INTEGRATION_FRONTEND_BACKEND.md)** - Intégration complète

### 💻 Documentation Frontend
- **[frontend/README.md](./frontend/README.md)** - Documentation frontend complète
- **[frontend/docs/INTEGRATION_SUMMARY.md](./frontend/docs/INTEGRATION_SUMMARY.md)** - Résumé intégration
- **[frontend/docs/MIGRATION_GUIDE.md](./frontend/docs/MIGRATION_GUIDE.md)** - Guide de migration

## 🔧 Configuration Avancée

### Variables d'Environnement Backend
```env
# Base de données
MYSQL_DATABASE=iusjcdb
MYSQL_USER=iusjc
MYSQL_PASSWORD=iusjc2025
MYSQL_ROOT_PASSWORD=root2025

# Services
API_GATEWAY_PORT=8080
EUREKA_PORT=8761
USER_SERVICE_PORT=8081
CALENDAR_SERVICE_PORT=8082

# Email (SMTP)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password

# JWT
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRATION=86400000

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

### Configuration Frontend
```typescript
// frontend/lib/api-config.ts
export const API_CONFIG = {
  BASE_URL: 'http://localhost:8080',
  TIMEOUT: 30000,
  RETRY_ATTEMPTS: 3,
  WEBSOCKET_URL: 'ws://localhost:8080/ws'
}
```

## 🛠️ Scripts de Gestion

### Démarrage des Services
```bash
# Démarrage complet (tous les microservices)
.\start-services-complets.bat

# Services opérationnels optimisés
.\start-services-operationnels.bat

# Backend seulement (pour développement)
.\start-backend-only.bat

# Développement avec hot-reload
.\start-all-dev.bat
```

### Construction et Tests
```bash
# Construction de tous les services
.\build-all-services.bat

# Test complet du système
.\test-all-services-complete.ps1

# Test des services essentiels
.\test-all-services.ps1

# Test rapide
.\test-complete.ps1
```

### Gestion des Utilisateurs
```bash
# Créer un utilisateur admin
.\create-admin-user.ps1
```

### Maintenance
```bash
# Arrêter tous les services
.\stop-all-services.bat

# Vérifier l'état des services
.\check-services-status.bat

# Diagnostiquer la connectivité API
.\diagnose-api-connectivity.ps1
```

## 🐛 Dépannage & Support

### Problèmes Courants

#### Backend ne démarre pas
```bash
# Vérifier les logs
docker-compose logs -f

# Redémarrer les services
docker-compose restart

# Nettoyer et redémarrer
docker-compose down
docker-compose up -d
```

#### Frontend ne se connecte pas
1. **Vérifier l'API Gateway**: http://localhost:8080/actuator/health
2. **Vérifier la configuration**: `frontend/lib/api-config.ts`
3. **Console navigateur**: F12 > Console pour les erreurs
4. **Nettoyer le cache**: Ctrl+Shift+R

#### Erreur 401 Unauthorized
1. **Se déconnecter/reconnecter** dans l'interface
2. **Nettoyer le localStorage**: F12 > Application > Local Storage > Clear
3. **Vérifier le token**: Peut être expiré
4. **Redémarrer le user-service**: `docker-compose restart user-service`

#### Base de données inaccessible
```bash
# Vérifier MySQL
docker-compose logs mysql

# Recréer la base
docker-compose down
docker volume rm eduschedule_mysql_data
docker-compose up -d
```

#### Services ne communiquent pas
1. **Vérifier Eureka**: http://localhost:8761
2. **Vérifier les ports**: Voir CONFIGURATION_PORTS.md
3. **Redémarrer l'API Gateway**: `docker-compose restart api-gateway`

### Logs et Monitoring
```bash
# Logs en temps réel
docker-compose logs -f [service-name]

# Métriques Prometheus
curl http://localhost:9090/metrics

# Santé des services
curl http://localhost:8080/actuator/health
```

## 📊 État du Projet

### Services Backend
| Service | État | Fonctionnalités | Tests |
|---------|------|----------------|-------|
| 🔐 User Service | ✅ Complet | Auth, Profils, Multi-école | ✅ |
| 📅 Calendar Service | ✅ Complet | Google Calendar, Sync | ✅ |
| 🏫 School Service | ✅ Complet | Multi-établissements | ✅ |
| 📚 Course Service | ✅ Complet | Cours, Groupes | ✅ |
| 🏢 Room Service | ✅ Complet | Salles, Ressources | ✅ |
| 📋 Reservation Service | ✅ Complet | Réservations, Conflits | ✅ |
| ⏰ Scheduling Service | ✅ Complet | Emplois du temps | ✅ |
| 🔔 Notification Service | ✅ Complet | Multi-canal | ✅ |
| 🤖 AI Service | ✅ Complet | IA Prédictive | ✅ |
| 📊 Reporting Service | ✅ Complet | Rapports, Analytics | ✅ |
| 🔧 Maintenance Service | ✅ Complet | Monitoring | ✅ |

### Frontend
| Composant | État | Description |
|-----------|------|-------------|
| 🔐 Authentification | ✅ | Login, Register, Reset Password |
| 👤 Profils | ✅ | Gestion profils utilisateurs |
| 📅 Disponibilités | ✅ | Calendrier enseignants |
| 🏫 Multi-école | ✅ | Gestion établissements |
| 📚 Cours | ✅ | CRUD cours et groupes |
| 🏢 Ressources | ✅ | Gestion salles/équipements |
| 📋 Réservations | ✅ | Interface réservations |
| ⏰ Planning | ✅ | Emplois du temps |
| 🔔 Notifications | ✅ | Centre notifications |
| 📊 Rapports | ✅ | Tableaux de bord |
| 🤖 IA | ✅ | Interface analytics IA |

### Intégrations
- ✅ **Google Calendar** - Synchronisation bidirectionnelle
- ✅ **SMTP** - Emails HTML personnalisés
- ✅ **WebSocket** - Notifications temps réel
- ✅ **Redis** - Cache haute performance
- ✅ **RabbitMQ** - Messaging asynchrone
- ✅ **Prometheus/Grafana** - Monitoring avancé

## 🚀 Roadmap

### Version 2.0 (Q2 2025)
- 📱 **Application mobile** (React Native)
- 🌍 **Multi-langue** (FR, EN, ES)
- 🔗 **API publique** avec documentation OpenAPI
- 📊 **Dashboard avancé** avec widgets personnalisables
- 🤖 **IA améliorée** avec machine learning

### Version 2.1 (Q3 2025)
- 🎯 **Gamification** pour étudiants
- 📈 **Analytics avancées** avec prédictions
- 🔄 **Synchronisation** avec autres systèmes (Moodle, etc.)
- 🛡️ **Sécurité renforcée** (2FA, SSO)

## 🤝 Contribution

### Pour Contribuer
1. **Fork** le projet
2. **Créer une branche** (`git checkout -b feature/nouvelle-fonctionnalite`)
3. **Commiter** (`git commit -m 'Ajout nouvelle fonctionnalité'`)
4. **Push** (`git push origin feature/nouvelle-fonctionnalite`)
5. **Créer une Pull Request**

### Standards de Code
- **Backend**: Java 17, Spring Boot 3.x, Clean Architecture
- **Frontend**: TypeScript, React 18, Tailwind CSS
- **Tests**: JUnit 5, Jest, Cypress
- **Documentation**: Markdown, JSDoc, Javadoc

### Environnement de Développement
```bash
# Cloner le projet
git clone https://github.com/your-org/eduschedule.git
cd eduschedule

# Configuration initiale
cp .env.example .env
# Éditer .env avec vos configurations

# Démarrage développement
.\start-all-dev.bat
```

## 📞 Support

### Contacts
- **Email**: support@iusjc.cm
- **Documentation**: Voir les fichiers .md du projet
- **Issues**: GitHub Issues pour bugs et suggestions

### Ressources
- **Wiki**: Documentation complète dans le projet
- **Scripts**: Nombreux scripts d'aide dans le répertoire racine
- **Logs**: Monitoring en temps réel avec Grafana

## 📄 Licence

Ce projet est sous licence **MIT**. Voir le fichier [LICENSE](./LICENSE) pour plus de détails.

---

## 🎯 Démarrage Immédiat

**Nouveau sur le projet ?** Commencez par :

1. **[LIRE_EN_PREMIER.md](./LIRE_EN_PREMIER.md)** - Comprendre le projet
2. **`.\build-all-services.bat`** - Construire les services (première fois)
3. **`.\start-services-complets.bat`** - Démarrer tous les services
4. **http://localhost:3000** - Accéder à l'interface

**Besoin d'aide ?** Consultez la documentation complète ci-dessus ou les guides disponibles dans le projet.

## 📁 Structure du Projet

```
eduschedule/
├── 📂 Services Backend (Spring Boot)
│   ├── api-gateway/              # Point d'entrée API (8080)
│   ├── eureka-server/            # Service Discovery (8761)
│   ├── config-server/            # Configuration centralisée (8888)
│   ├── user-service/             # Authentification & Utilisateurs (8081)
│   ├── school-service/           # Gestion établissements (8083)
│   ├── course-service/           # Cours & Groupes (8084)
│   ├── room-service/             # Salles & Ressources (8090)
│   ├── resource-service/         # Gestion ressources (8082)
│   ├── scheduling-service/       # Emplois du temps (8085)
│   ├── reservation-service/      # Réservations (8086)
│   ├── teacher-availability-service/ # Disponibilités (8092)
│   ├── calendar-service/         # Google Calendar (8089)
│   ├── notification-service/     # Notifications (8087)
│   ├── reporting-service/        # Rapports (8088)
│   ├── ai-service/               # Intelligence artificielle (8093)
│   ├── event-service/            # Événements (8091)
│   ├── maintenance-service/      # Monitoring (8094)
│   └── ent-integration-service/  # Intégration ENT (8095)
│
├── 📂 frontend/                  # Application React/Next.js
│   ├── components/               # Composants réutilisables
│   ├── lib/                      # Utilitaires & API
│   ├── pages/                    # Pages Next.js
│   └── public/                   # Assets statiques
│
├── 📂 Scripts & Configuration
│   ├── *.bat                     # Scripts Windows
│   ├── *.ps1                     # Scripts PowerShell
│   ├── *.sql                     # Scripts SQL
│   ├── docker-compose.yml        # Configuration Docker
│   ├── .env                      # Variables d'environnement
│   └── pom.xml                   # Configuration Maven parent
│
└── 📂 Documentation
    ├── README.md                 # Ce fichier
    ├── LIRE_EN_PREMIER.md        # Guide de démarrage
    ├── GUIDE_*.md                # Guides fonctionnels
    ├── VERIFICATION_*.md         # Guides de vérification
    └── PLAN_*.md                 # Plans d'implémentation
```

---

*Développé avec ❤️ pour l'Institut Universitaire Saint Jean - Cameroun*
