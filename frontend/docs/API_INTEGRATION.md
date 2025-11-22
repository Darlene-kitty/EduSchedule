# Intégration API Backend - Frontend React

## Vue d'ensemble

Ce document décrit l'intégration entre le frontend React (Next.js) et le backend Spring Boot via l'API Gateway.

## Configuration

### Variables d'environnement

Créez un fichier `.env.local` à la racine du dossier `frontend`:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_API_TIMEOUT=30000
```

## Architecture

### Structure des fichiers

```
frontend/
├── lib/
│   ├── api-config.ts      # Configuration des endpoints API
│   └── api-client.ts      # Client HTTP centralisé
├── services/
│   ├── auth.service.ts    # Service d'authentification
│   ├── user.service.ts    # Service de gestion des utilisateurs
│   ├── resource.service.ts # Service de gestion des ressources
│   ├── course.service.ts   # Service de gestion des cours
│   ├── reservation.service.ts # Service de réservations
│   ├── schedule.service.ts # Service d'emplois du temps
│   ├── notification.service.ts # Service de notifications
│   ├── report.service.ts   # Service de rapports
│   └── index.ts           # Export centralisé
├── hooks/
│   └── use-api.ts         # Hook personnalisé pour les appels API
└── contexts/
    └── auth-context.tsx   # Contexte d'authentification (mis à jour)
```

## Services disponibles

### 1. Authentication Service (`auth.service.ts`)

**Fonctionnalités implémentées:**
- ✅ Connexion (login)
- ✅ Inscription (register)
- ✅ Déconnexion (logout)
- ✅ Mot de passe oublié (forgotPassword)
- ✅ Réinitialisation du mot de passe (resetPassword)
- ✅ Vérification d'email (verifyEmail)
- ✅ Récupération du profil (getProfile)
- ✅ Rafraîchissement du token (refreshToken)

**Exemple d'utilisation:**
```typescript
import { authService } from '@/services/auth.service'

// Connexion
const user = await authService.login({ email, password })

// Inscription
const newUser = await authService.register({
  email,
  password,
  nom: 'Dupont',
  prenom: 'Jean',
  role: 'STUDENT'
})
```

### 2. User Service (`user.service.ts`)

**Fonctionnalités implémentées:**
- ✅ Liste des utilisateurs avec pagination
- ✅ Récupération d'un utilisateur par ID
- ✅ Création d'utilisateur
- ✅ Mise à jour d'utilisateur
- ✅ Suppression d'utilisateur
- ✅ Recherche d'utilisateurs

**Exemple d'utilisation:**
```typescript
import { userService } from '@/services/user.service'

// Récupérer tous les utilisateurs
const users = await userService.getUsers(0, 10)

// Créer un utilisateur
const newUser = await userService.createUser({
  email: 'user@example.com',
  password: 'password123',
  nom: 'Dupont',
  prenom: 'Marie',
  role: 'TEACHER'
})
```

### 3. Resource Service (`resource.service.ts`)

**Fonctionnalités implémentées:**
- ✅ Gestion des ressources génériques
- ✅ Gestion spécifique des salles
- ✅ CRUD complet pour les ressources et salles
- ✅ Recherche de salles disponibles

**Exemple d'utilisation:**
```typescript
import { resourceService } from '@/services/resource.service'

// Récupérer toutes les salles
const salles = await resourceService.getSalles()

// Créer une salle
const newSalle = await resourceService.createSalle({
  nom: 'Salle A101',
  capacite: 30,
  batiment: 'Bâtiment A',
  etage: 1,
  equipements: ['Projecteur', 'Tableau blanc']
})
```

### 4. Course Service (`course.service.ts`)

**Fonctionnalités:**
- ✅ CRUD complet pour les cours
- ✅ Recherche de cours

**Note:** Backend à implémenter

### 5. Reservation Service (`reservation.service.ts`)

**Fonctionnalités:**
- ✅ CRUD complet pour les réservations
- ✅ Réservations par utilisateur
- ✅ Réservations par ressource

**Note:** Backend à implémenter

### 6. Schedule Service (`schedule.service.ts`)

**Fonctionnalités:**
- ✅ CRUD complet pour les emplois du temps
- ✅ Gestion des conflits
- ✅ Résolution de conflits

**Note:** Backend à implémenter

### 7. Notification Service (`notification.service.ts`)

**Fonctionnalités:**
- ✅ Liste des notifications
- ✅ Marquer comme lu
- ✅ Suppression de notifications

**Note:** Backend à implémenter

### 8. Report Service (`report.service.ts`)

**Fonctionnalités:**
- ✅ Génération de rapports
- ✅ Téléchargement de rapports PDF

**Note:** Backend à implémenter

## Utilisation dans les composants

### Avec le hook `useApi`

```typescript
import { useMutation, useQuery } from '@/hooks/use-api'
import { userService } from '@/services'

function UserComponent() {
  // Pour les requêtes GET
  const { data: users, isLoading, execute: fetchUsers } = useQuery(
    userService.getUsers
  )

  // Pour les mutations (POST, PUT, DELETE)
  const { execute: createUser, isLoading: isCreating } = useMutation(
    userService.createUser,
    {
      successMessage: 'Utilisateur créé avec succès',
      onSuccess: () => {
        fetchUsers() // Recharger la liste
      }
    }
  )

  // Utilisation
  useEffect(() => {
    fetchUsers()
  }, [])

  const handleCreate = async (data) => {
    await createUser(data)
  }

  return (
    // Votre JSX
  )
}
```

### Avec le contexte d'authentification

```typescript
import { useAuth } from '@/contexts/auth-context'

function LoginComponent() {
  const { login, isLoading, user } = useAuth()

  const handleLogin = async (email: string, password: string) => {
    try {
      await login(email, password, true) // rememberMe = true
      // Redirection automatique après connexion
    } catch (error) {
      console.error('Erreur de connexion:', error)
    }
  }

  return (
    // Votre JSX
  )
}
```

## Gestion des erreurs

Le client API gère automatiquement:
- ✅ Timeout des requêtes (30s par défaut)
- ✅ Erreurs HTTP avec messages personnalisés
- ✅ Gestion des tokens JWT
- ✅ Affichage des toasts d'erreur

## Gestion de l'authentification

### Tokens JWT

Les tokens sont automatiquement:
- Sauvegardés dans le localStorage après connexion
- Ajoutés aux headers des requêtes authentifiées
- Supprimés lors de la déconnexion

### Rafraîchissement automatique

Pour implémenter le rafraîchissement automatique des tokens:

```typescript
// Dans api-client.ts, ajouter un intercepteur
if (response.status === 401) {
  const newToken = await authService.refreshToken()
  // Réessayer la requête avec le nouveau token
}
```

## Prochaines étapes

### Backend à compléter:
1. ⏳ Course Service - Endpoints CRUD
2. ⏳ Reservation Service - Endpoints CRUD
3. ⏳ Scheduling Service - Endpoints CRUD + conflits
4. ⏳ Notification Service - Endpoints CRUD
5. ⏳ Reporting Service - Génération de rapports

### Frontend à compléter:
1. ⏳ Mettre à jour les composants pour utiliser les vrais services
2. ⏳ Implémenter le rafraîchissement automatique des tokens
3. ⏳ Ajouter la gestion du cache (React Query ou SWR)
4. ⏳ Implémenter les WebSockets pour les notifications en temps réel

## Tests

Pour tester l'intégration:

```bash
# Démarrer le backend
cd ..
docker-compose up -d

# Démarrer le frontend
cd frontend
npm run dev
```

L'application sera disponible sur `http://localhost:3000`
L'API Gateway sera disponible sur `http://localhost:8080`

## Dépannage

### CORS Issues
Si vous rencontrez des problèmes CORS, vérifiez la configuration dans l'API Gateway:

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        // Configuration CORS
    }
}
```

### Token expiré
Si le token expire, l'utilisateur sera automatiquement déconnecté. Implémentez le rafraîchissement automatique pour éviter cela.

### Timeout
Augmentez le timeout dans `.env.local` si nécessaire:
```env
NEXT_PUBLIC_API_TIMEOUT=60000  # 60 secondes
```
