# Frontend React - EduSchedule

Application frontend React/Next.js pour le système de gestion d'emploi du temps EduSchedule.

## 🔧 Test de Connexion Backend/Frontend

**IMPORTANT:** Avant de commencer, testez la connexion avec le backend:

1. Démarrez tous les services backend (voir `../DEMARRAGE_RAPIDE.md`)
2. Démarrez le frontend: `npm run dev`
3. Accédez à: **http://localhost:3000/test-connection**
4. Cliquez sur "Lancer les tests"

✅ Tous les tests doivent être verts avant de continuer.

## 🚀 Démarrage

```bash
# Installer les dépendances
npm install

# Démarrer en mode développement
npm run dev

# Build production
npm run build

# Démarrer en production
npm start
```

L'application sera disponible sur `http://localhost:3000`

## 📁 Structure

```
frontend/
├── app/                    # Pages Next.js (App Router)
├── components/            # Composants React
│   ├── ui/               # Composants UI de base
│   └── *-view.tsx        # Vues principales
├── contexts/             # Contextes React (Auth, Toast)
├── hooks/                # Hooks personnalisés
├── lib/                  # Configuration et utilitaires
│   ├── api-config.ts     # Configuration API
│   └── api-client.ts     # Client HTTP
├── services/             # Services API (8 services)
└── docs/                 # Documentation détaillée
```

## 🔌 Services API

### Connectés au Backend
- ✅ **AuthService** - Authentification
- ✅ **UserService** - Gestion des utilisateurs
- ✅ **ResourceService** - Gestion des ressources

### Prêts (Backend à implémenter)
- 🔄 **CourseService** - Gestion des cours
- 🔄 **ReservationService** - Réservations
- 🔄 **ScheduleService** - Emplois du temps
- 🔄 **NotificationService** - Notifications
- 🔄 **ReportService** - Rapports

## 💻 Utilisation

### Avec les Hooks

```typescript
import { userService } from '@/services'
import { useQuery, useMutation } from '@/hooks/use-api'

function MyComponent() {
  // Récupérer des données
  const { data, isLoading, execute } = useQuery(userService.getUsers)
  
  // Mutation
  const { execute: createUser } = useMutation(
    userService.createUser,
    {
      successMessage: "Utilisateur créé",
      onSuccess: () => execute()
    }
  )
  
  useEffect(() => {
    execute(0, 10) // page, size
  }, [])
  
  return (/* JSX */)
}
```

### Avec le Contexte d'Authentification

```typescript
import { useAuth } from '@/contexts/auth-context'

function MyComponent() {
  const { user, isAuthenticated, login, logout } = useAuth()
  
  const handleLogin = async () => {
    await login(email, password, rememberMe)
  }
  
  return (/* JSX */)
}
```

## 📚 Documentation Détaillée

- **[docs/API_INTEGRATION.md](./docs/API_INTEGRATION.md)** - Guide d'intégration API complet
- **[docs/MIGRATION_GUIDE.md](./docs/MIGRATION_GUIDE.md)** - Comment migrer les composants
- **[docs/INTEGRATION_SUMMARY.md](./docs/INTEGRATION_SUMMARY.md)** - Résumé technique
- **[components/users-view-example.tsx](./components/users-view-example.tsx)** - Exemple pratique

## ⚙️ Configuration

L'URL de l'API est configurée dans `lib/api-config.ts`:

```typescript
const API_BASE_URL = 'http://localhost:8080'
const API_TIMEOUT = 30000
```

## 🎨 Composants UI

Le projet utilise [shadcn/ui](https://ui.shadcn.com/):
- Button, Input, Card, Badge, etc.
- Thème personnalisable (light/dark)
- Composants accessibles (ARIA)

## 🔐 Authentification

- Tokens JWT stockés dans localStorage
- Gestion automatique dans les requêtes API
- Protection des routes avec AuthGuard
- Contexte global pour l'état utilisateur

## 🐛 Dépannage

### CORS Errors
- Vérifier que le backend est démarré
- Vérifier la configuration CORS dans l'API Gateway

### 401 Unauthorized
- Se reconnecter
- Nettoyer le localStorage

### Les données ne se chargent pas
- Ouvrir la console (F12)
- Vérifier l'onglet Network
- Vérifier que l'API répond

## 📖 Pour Aller Plus Loin

1. **Migrer un composant** → Voir `docs/MIGRATION_GUIDE.md`
2. **Comprendre l'API** → Voir `docs/API_INTEGRATION.md`
3. **Voir un exemple** → Voir `components/users-view-example.tsx`

---

**Pour plus d'informations, consultez la documentation dans le dossier `docs/`**
