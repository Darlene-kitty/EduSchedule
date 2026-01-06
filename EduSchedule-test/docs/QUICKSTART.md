# 🚀 Guide de Démarrage Rapide - EduSchedule

Guide complet pour démarrer le projet EduSchedule avec le frontend React et le backend Spring Boot.

## 📋 Prérequis

- Docker et Docker Compose
- Node.js 18+ et npm/pnpm
- Java 17+
- Maven 3.8+

## 🏃 Démarrage en 3 étapes

### 1. Démarrer le Backend (Docker)

```bash
# À la racine du projet
docker-compose up -d
```

Cela démarre:
- MySQL (port 3306)
- Redis (port 6379)
- RabbitMQ (port 5672, management 15672)
- Eureka Server (port 8761)
- Config Server (port 8888)
- API Gateway (port 8080)
- Tous les microservices

**Vérifier que tout fonctionne:**
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- RabbitMQ Management: http://localhost:15672 (guest/guest)

### 2. Configurer le Frontend

```bash
cd frontend

# Installer les dépendances
npm install

# Créer le fichier de configuration
cat > .env.local << EOF
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_API_TIMEOUT=30000
EOF
```

### 3. Démarrer le Frontend

```bash
# Toujours dans le dossier frontend
npm run dev
```

Le frontend sera disponible sur: **http://localhost:3000**

## 🎯 Tester l'application

### 1. Créer un compte

1. Aller sur http://localhost:3000/register
2. Remplir le formulaire:
   - Email: `admin@test.com`
   - Mot de passe: `password123`
   - Nom: `Admin Test`
   - Rôle: `Administrateur`
3. Cliquer sur "S'inscrire"

### 2. Se connecter

1. Aller sur http://localhost:3000/login
2. Utiliser les identifiants créés
3. Cocher "Se souvenir de moi" (optionnel)
4. Cliquer sur "Se connecter"

### 3. Explorer les fonctionnalités

Une fois connecté, vous pouvez:

#### ✅ Fonctionnalités disponibles (Backend implémenté)

- **Authentification**
  - Connexion / Déconnexion
  - Inscription
  - Mot de passe oublié
  - Vérification d'email

- **Gestion des utilisateurs**
  - Lister les utilisateurs
  - Créer un utilisateur
  - Modifier un utilisateur
  - Supprimer un utilisateur
  - Rechercher des utilisateurs

- **Gestion des ressources**
  - Lister les salles
  - Créer une salle
  - Modifier une salle
  - Supprimer une salle
  - Rechercher des salles disponibles

#### ⏳ Fonctionnalités en cours (Backend à implémenter)

- **Gestion des cours**
  - Interface disponible
  - Backend à connecter

- **Gestion des réservations**
  - Interface disponible
  - Backend à connecter

- **Emploi du temps**
  - Interface disponible
  - Backend à connecter

- **Notifications**
  - Interface disponible
  - Backend à connecter

- **Rapports**
  - Interface disponible
  - Backend à connecter

## 📁 Structure du projet

```
eduschedule/
├── api-gateway/              # API Gateway (Spring Cloud Gateway)
├── config-server/            # Config Server (Spring Cloud Config)
├── eureka-server/            # Service Discovery (Eureka)
├── user-service/             # Service de gestion des utilisateurs ✅
├── resource-service/         # Service de gestion des ressources ✅
├── course-service/           # Service de gestion des cours ⏳
├── reservation-service/      # Service de réservations ⏳
├── scheduling-service/       # Service d'emplois du temps ⏳
├── notification-service/     # Service de notifications ⏳
├── reporting-service/        # Service de rapports ⏳
├── frontend/                 # Frontend React/Next.js ✅
│   ├── lib/                 # Configuration API
│   ├── services/            # Services API
│   ├── components/          # Composants React
│   ├── contexts/            # Contextes (Auth, Toast)
│   └── hooks/               # Hooks personnalisés
└── docker-compose.yml       # Configuration Docker
```

## 🔧 Configuration

### Backend

Les variables d'environnement sont dans `.env`:

```env
# Base de données
MYSQL_DATABASE=iusjcdb
MYSQL_USER=iusjc
MYSQL_PASSWORD=iusjc2025

# JWT
JWT_SECRET=iusjc-secret-key-2025-eduschedule-system-planning-microservices-architecture
JWT_EXPIRATION=86400000

# API Gateway
API_GATEWAY_PORT=8080
```

### Frontend

Les variables d'environnement sont dans `frontend/.env.local`:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_API_TIMEOUT=30000
```

## 🔌 Endpoints API disponibles

### Authentication
- `POST /user-service/api/auth/login` - Connexion
- `POST /user-service/api/auth/register` - Inscription
- `POST /user-service/api/auth/logout` - Déconnexion
- `POST /user-service/api/auth/forgot-password` - Mot de passe oublié
- `POST /user-service/api/auth/reset-password` - Réinitialiser le mot de passe
- `POST /user-service/api/auth/verify-email` - Vérifier l'email

### Users
- `GET /user-service/api/users` - Liste des utilisateurs
- `GET /user-service/api/users/{id}` - Détails d'un utilisateur
- `POST /user-service/api/users` - Créer un utilisateur
- `PUT /user-service/api/users/{id}` - Modifier un utilisateur
- `DELETE /user-service/api/users/{id}` - Supprimer un utilisateur
- `GET /user-service/api/users/profile` - Profil de l'utilisateur connecté

### Resources (Salles)
- `GET /resource-service/api/salles` - Liste des salles
- `GET /resource-service/api/salles/{id}` - Détails d'une salle
- `POST /resource-service/api/salles` - Créer une salle
- `PUT /resource-service/api/salles/{id}` - Modifier une salle
- `DELETE /resource-service/api/salles/{id}` - Supprimer une salle

## 🐛 Dépannage

### Le backend ne démarre pas

```bash
# Vérifier les logs
docker-compose logs -f

# Redémarrer les services
docker-compose restart

# Reconstruire les images
docker-compose up -d --build
```

### Le frontend ne se connecte pas au backend

1. Vérifier que l'API Gateway est accessible: http://localhost:8080
2. Vérifier le fichier `frontend/.env.local`
3. Vérifier la console du navigateur pour les erreurs CORS
4. Vérifier que la configuration CORS est correcte dans l'API Gateway

### Erreur 401 Unauthorized

1. Se déconnecter et se reconnecter
2. Nettoyer le localStorage du navigateur
3. Vérifier que le token JWT n'est pas expiré

### Base de données vide

```bash
# Réinitialiser la base de données
docker-compose down -v
docker-compose up -d

# Attendre que les services démarrent
sleep 30

# Vérifier que la base est initialisée
docker-compose exec mysql mysql -u iusjc -piusjc2025 iusjcdb -e "SHOW TABLES;"
```

## 📚 Documentation

- [Frontend README](./frontend/README.md) - Documentation du frontend
- [API Integration](./frontend/API_INTEGRATION.md) - Guide d'intégration API
- [Migration Guide](./frontend/MIGRATION_GUIDE.md) - Guide de migration
- [Architecture](./ARCHITECTURE.md) - Architecture du système

## 🔄 Workflow de développement

### 1. Développer une nouvelle fonctionnalité

```bash
# Créer une branche
git checkout -b feature/ma-fonctionnalite

# Développer...

# Tester localement
docker-compose up -d
cd frontend && npm run dev

# Commiter
git add .
git commit -m "feat: ajouter ma fonctionnalité"
git push origin feature/ma-fonctionnalite
```

### 2. Ajouter un nouveau service backend

1. Créer le service dans le backend
2. Ajouter les endpoints dans `frontend/lib/api-config.ts`
3. Créer le service dans `frontend/services/`
4. Mettre à jour les composants pour utiliser le service
5. Tester l'intégration

### 3. Migrer un composant vers l'API réelle

Voir [MIGRATION_GUIDE.md](./frontend/MIGRATION_GUIDE.md)

## 🎓 Prochaines étapes

1. **Implémenter les services backend manquants**
   - Course Service
   - Reservation Service
   - Scheduling Service
   - Notification Service
   - Reporting Service

2. **Migrer les composants frontend**
   - Suivre le guide de migration
   - Tester chaque composant
   - Gérer les cas d'erreur

3. **Ajouter des fonctionnalités avancées**
   - Notifications en temps réel (WebSocket)
   - Cache avec React Query
   - Optimisation des performances
   - Tests unitaires et d'intégration

4. **Déploiement**
   - Configuration pour la production
   - CI/CD avec GitHub Actions
   - Déploiement sur le cloud

## 🤝 Besoin d'aide ?

- Consulter la documentation dans `frontend/`
- Vérifier les logs: `docker-compose logs -f [service-name]`
- Ouvrir une issue sur GitHub

Bon développement ! 🚀
