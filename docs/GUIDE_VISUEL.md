# 🎨 Guide Visuel - EduSchedule

## 🚀 Démarrage en 3 Étapes

```
┌─────────────────────────────────────────────────────────────┐
│                    ÉTAPE 1: BACKEND                         │
│                                                             │
│  Windows:                    Linux/Mac:                     │
│  .\start-dev.ps1            ./start-dev.sh                  │
│                                                             │
│  OU manuellement:                                           │
│  docker-compose up -d                                       │
│                                                             │
│  ✓ MySQL, Redis, RabbitMQ                                  │
│  ✓ Eureka, Config Server                                   │
│  ✓ API Gateway                                             │
│  ✓ User Service, Resource Service                          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    ÉTAPE 2: FRONTEND                        │
│                                                             │
│  cd frontend                                                │
│  npm install                                                │
│  npm run dev                                                │
│                                                             │
│  ✓ Next.js démarré sur http://localhost:3000              │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    ÉTAPE 3: UTILISER                        │
│                                                             │
│  1. Ouvrir http://localhost:3000                           │
│  2. Créer un compte (/register)                            │
│  3. Se connecter (/login)                                  │
│  4. Explorer les fonctionnalités                           │
└─────────────────────────────────────────────────────────────┘
```

## 🏗️ Architecture Simplifiée

```
┌──────────────────────────────────────────────────────────────┐
│                         FRONTEND                             │
│                    React/Next.js (Port 3000)                 │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Login   │  │  Users   │  │Resources │  │ Courses  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │           Services API (8 services)                │    │
│  │  auth • user • resource • course • reservation     │    │
│  │  schedule • notification • report                  │    │
│  └────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
                            ↓ HTTP/REST
┌──────────────────────────────────────────────────────────────┐
│                      API GATEWAY                             │
│                  Spring Cloud Gateway (Port 8080)            │
│                                                              │
│  • Routage des requêtes                                     │
│  • Configuration CORS                                       │
│  • Load balancing                                           │
└──────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│                    MICROSERVICES                             │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ User Service │  │Resource Svc  │  │Course Service│     │
│  │   (8081)     │  │   (8082)     │  │   (8083)     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │Reservation   │  │Scheduling    │  │Notification  │     │
│  │   (8084)     │  │   (8085)     │  │   (8086)     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└──────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE                            │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  MySQL   │  │  Redis   │  │ RabbitMQ │  │  Eureka  │   │
│  │  (3306)  │  │  (6379)  │  │  (5672)  │  │  (8761)  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└──────────────────────────────────────────────────────────────┘
```

## 📊 État des Fonctionnalités

```
┌─────────────────────────────────────────────────────────────┐
│                  FONCTIONNALITÉS                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ✅ OPÉRATIONNEL (Backend + Frontend)                      │
│  ├─ Authentification (login, register, logout)             │
│  ├─ Gestion des utilisateurs (CRUD complet)                │
│  └─ Gestion des ressources/salles (CRUD complet)           │
│                                                             │
│  🔄 INTERFACE PRÊTE (Backend à implémenter)                │
│  ├─ Gestion des cours                                      │
│  ├─ Gestion des réservations                               │
│  ├─ Emplois du temps                                       │
│  ├─ Notifications                                          │
│  └─ Rapports                                               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 🔌 Flux d'une Requête API

```
┌─────────────────────────────────────────────────────────────┐
│  1. UTILISATEUR CLIQUE SUR "CRÉER UTILISATEUR"             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  2. COMPOSANT REACT                                         │
│     const { execute } = useMutation(userService.createUser) │
│     await execute({ email, password, nom, prenom, role })   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  3. SERVICE API (userService)                               │
│     POST /user-service/api/users                            │
│     Headers: { Authorization: "Bearer <token>" }            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  4. API CLIENT (apiClient)                                  │
│     - Ajoute le token JWT                                   │
│     - Gère le timeout                                       │
│     - Envoie la requête HTTP                                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  5. API GATEWAY (localhost:8080)                            │
│     - Vérifie CORS                                          │
│     - Route vers user-service                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  6. USER SERVICE                                            │
│     - Valide le token JWT                                   │
│     - Valide les données                                    │
│     - Sauvegarde en base de données                         │
│     - Retourne l'utilisateur créé                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  7. RETOUR AU FRONTEND                                      │
│     - Hook affiche un toast de succès                       │
│     - Recharge la liste des utilisateurs                    │
│     - Ferme le modal                                        │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Structure des Fichiers Importants

```
eduschedule/
│
├── 📄 start-dev.ps1              ← Script de démarrage Windows
├── 📄 start-dev.sh               ← Script de démarrage Linux/Mac
├── 📄 docker-compose.yml         ← Configuration Docker
├── 📄 QUICKSTART.md              ← Guide de démarrage
├── 📄 INTEGRATION_RESUME_FR.md   ← Résumé en français ⭐
│
├── 📁 frontend/
│   ├── 📄 .env.local             ← Configuration API
│   ├── 📄 README.md              ← Doc frontend
│   ├── 📄 API_INTEGRATION.md     ← Guide d'intégration ⭐
│   ├── 📄 MIGRATION_GUIDE.md     ← Guide de migration ⭐
│   │
│   ├── 📁 lib/
│   │   ├── api-config.ts         ← Endpoints API
│   │   └── api-client.ts         ← Client HTTP
│   │
│   ├── 📁 services/
│   │   ├── auth.service.ts       ← Service auth ✅
│   │   ├── user.service.ts       ← Service users ✅
│   │   ├── resource.service.ts   ← Service resources ✅
│   │   └── ...                   ← Autres services 🔄
│   │
│   ├── 📁 hooks/
│   │   └── use-api.ts            ← Hooks personnalisés
│   │
│   ├── 📁 contexts/
│   │   └── auth-context.tsx      ← Contexte auth ✅
│   │
│   └── 📁 components/
│       ├── users-view.tsx        ← Vue utilisateurs
│       ├── users-view-example.tsx ← Exemple migration ⭐
│       └── ...
│
└── 📁 api-gateway/
    └── 📁 src/main/java/.../config/
        └── CorsConfig.java       ← Config CORS ✅
```

## 🎯 Prochaines Actions

```
┌─────────────────────────────────────────────────────────────┐
│  POUR TESTER MAINTENANT                                     │
├─────────────────────────────────────────────────────────────┤
│  1. Démarrer: .\start-dev.ps1 (Windows)                    │
│  2. Ouvrir: http://localhost:3000                           │
│  3. Créer un compte                                         │
│  4. Tester les utilisateurs et ressources                   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  POUR DÉVELOPPER                                            │
├─────────────────────────────────────────────────────────────┤
│  1. Lire: frontend/MIGRATION_GUIDE.md                       │
│  2. Voir: frontend/components/users-view-example.tsx        │
│  3. Migrer: users-view.tsx                                  │
│  4. Tester: Créer/Modifier/Supprimer des utilisateurs      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  POUR IMPLÉMENTER LE BACKEND                                │
├─────────────────────────────────────────────────────────────┤
│  1. Créer les entités JPA                                   │
│  2. Créer les repositories                                  │
│  3. Créer les services                                      │
│  4. Créer les controllers                                   │
│  5. Les endpoints sont déjà définis dans api-config.ts     │
└─────────────────────────────────────────────────────────────┘
```

## 🆘 Aide Rapide

```
┌─────────────────────────────────────────────────────────────┐
│  PROBLÈME: Le backend ne démarre pas                        │
│  SOLUTION: docker-compose logs -f                           │
│            docker-compose restart                           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  PROBLÈME: Erreur CORS                                      │
│  SOLUTION: Vérifier api-gateway/config/CorsConfig.java      │
│            Redémarrer: docker-compose restart api-gateway   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  PROBLÈME: 401 Unauthorized                                 │
│  SOLUTION: Se déconnecter et se reconnecter                 │
│            Nettoyer le localStorage (F12 > Application)     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  PROBLÈME: Frontend ne se connecte pas                      │
│  SOLUTION: Vérifier http://localhost:8080                   │
│            Vérifier frontend/lib/api-config.ts              │
│            Vérifier la console du navigateur (F12)          │
└─────────────────────────────────────────────────────────────┘
```

## 📚 Documentation par Niveau

```
┌─────────────────────────────────────────────────────────────┐
│  DÉBUTANT                                                   │
│  ├─ QUICKSTART.md                                           │
│  ├─ INTEGRATION_RESUME_FR.md                                │
│  └─ Ce fichier (GUIDE_VISUEL.md)                           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  INTERMÉDIAIRE                                              │
│  ├─ frontend/README.md                                      │
│  ├─ frontend/MIGRATION_GUIDE.md                             │
│  └─ frontend/components/users-view-example.tsx             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  AVANCÉ                                                     │
│  ├─ frontend/API_INTEGRATION.md                             │
│  ├─ INTEGRATION_COMPLETE.md                                 │
│  ├─ PROJECT_STATUS.md                                       │
│  └─ ARCHITECTURE.md                                         │
└─────────────────────────────────────────────────────────────┘
```

## 🎉 Résumé

```
╔═════════════════════════════════════════════════════════════╗
║                                                             ║
║  ✅ INTÉGRATION COMPLÈTE ET FONCTIONNELLE                  ║
║                                                             ║
║  • 8 services API créés                                    ║
║  • 3 services connectés au backend                         ║
║  • Authentification opérationnelle                         ║
║  • Gestion utilisateurs opérationnelle                     ║
║  • Gestion ressources opérationnelle                       ║
║  • Documentation complète                                  ║
║  • Scripts de démarrage automatiques                       ║
║                                                             ║
║  🚀 PRÊT POUR LE DÉVELOPPEMENT                             ║
║                                                             ║
╚═════════════════════════════════════════════════════════════╝
```

---

**Commencez maintenant:** `.\start-dev.ps1` (Windows) ou `./start-dev.sh` (Linux/Mac)
