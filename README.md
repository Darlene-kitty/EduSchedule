# EduSchedule - Système de Gestion de Planning Académique

Système complet de gestion d'emploi du temps pour établissements d'enseignement avec architecture microservices Spring Boot et frontend React/Next.js.

## 🚀 Démarrage Rapide

### Windows
```powershell
.\start-dev.ps1
```

### Linux/Mac
```bash
chmod +x start-dev.sh
./start-dev.sh
```

### Accès
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

## 📋 Prérequis

- Docker & Docker Compose
- Node.js 18+
- npm ou pnpm

## 🏗️ Architecture

- **Backend**: Spring Boot (Microservices)
- **Frontend**: React/Next.js avec TypeScript
- **Base de données**: MySQL
- **Cache**: Redis
- **Messaging**: RabbitMQ
- **Service Discovery**: Eureka
- **API Gateway**: Spring Cloud Gateway

## ✅ Fonctionnalités

### Opérationnel
- ✅ Authentification complète (JWT)
- ✅ Gestion des utilisateurs (CRUD)
- ✅ Gestion des ressources/salles (CRUD)

### En Développement
- 🔄 Gestion des cours
- 🔄 Gestion des réservations
- 🔄 Emplois du temps
- 🔄 Notifications
- 🔄 Rapports

## 📚 Documentation

### Commencer
- **[START_HERE.md](./START_HERE.md)** - Point de départ
- **[docs/INTEGRATION_RESUME_FR.md](./docs/INTEGRATION_RESUME_FR.md)** - Résumé complet en français
- **[docs/QUICKSTART.md](./docs/QUICKSTART.md)** - Guide de démarrage détaillé

### Développement
- **[frontend/README.md](./frontend/README.md)** - Documentation frontend
- **[docs/GUIDE_VISUEL.md](./docs/GUIDE_VISUEL.md)** - Architecture visuelle
- **[docs/DOCUMENTATION_INDEX.md](./docs/DOCUMENTATION_INDEX.md)** - Index complet

## 🔧 Configuration

### Backend
Variables d'environnement dans `.env`:
```env
MYSQL_DATABASE=iusjcdb
MYSQL_USER=iusjc
MYSQL_PASSWORD=iusjc2025
API_GATEWAY_PORT=8080
```

### Frontend
Configuration dans `frontend/lib/api-config.ts`:
```typescript
const API_BASE_URL = 'http://localhost:8080'
```

## 🐛 Dépannage

### Backend ne démarre pas
```bash
docker-compose logs -f
docker-compose restart
```

### Frontend ne se connecte pas
1. Vérifier que l'API Gateway répond: http://localhost:8080
2. Vérifier `frontend/lib/api-config.ts`
3. Vérifier la console du navigateur (F12)

### Erreur 401 Unauthorized
1. Se déconnecter et se reconnecter
2. Nettoyer le localStorage (F12 > Application > Local Storage)

## 📊 État du Projet

- **Backend**: 3/8 services implémentés (38%)
- **Frontend**: 100% prêt (8/8 services créés)
- **Intégration**: Complète et fonctionnelle
- **Documentation**: Complète

Voir [docs/PROJECT_STATUS.md](./docs/PROJECT_STATUS.md) pour plus de détails.

## 🤝 Contribution

1. Créer une branche pour votre fonctionnalité
2. Commiter vos changements
3. Pousser vers la branche
4. Créer une Pull Request

## 📄 Licence

Ce projet est sous licence MIT.

---

**Pour plus d'informations, consultez [START_HERE.md](./START_HERE.md)**
