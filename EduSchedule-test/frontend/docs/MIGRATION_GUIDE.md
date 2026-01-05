# Guide de Migration - Intégration API

Ce guide explique comment migrer les composants existants pour utiliser les vrais services API.

## Étapes de migration

### 1. Identifier les données mockées

Recherchez dans vos composants:
- Les tableaux de données statiques (ex: `const users = [...]`)
- Les fonctions simulées (ex: `await new Promise(resolve => setTimeout(resolve, 1000))`)
- Les données en dur

### 2. Importer les services nécessaires

```typescript
// Avant
import { useState } from "react"

// Après
import { useState, useEffect } from "react"
import { useQuery, useMutation } from "@/hooks/use-api"
import { userService } from "@/services"
```

### 3. Remplacer les données statiques par des appels API

#### Avant (données mockées):
```typescript
const users = [
  { id: "1", name: "John Doe", email: "john@example.com" },
  { id: "2", name: "Jane Smith", email: "jane@example.com" },
]
```

#### Après (données API):
```typescript
const { data: usersResponse, isLoading, execute: fetchUsers } = useQuery(
  userService.getUsers
)

useEffect(() => {
  fetchUsers(0, 50) // page, size
}, [])

const users = usersResponse?.content || []
```

### 4. Implémenter les mutations CRUD

#### CREATE (Ajouter)
```typescript
const { execute: createItem, isLoading: isCreating } = useMutation(
  itemService.createItem,
  {
    successMessage: "Élément créé avec succès",
    onSuccess: () => {
      setIsModalOpen(false)
      fetchItems() // Recharger la liste
    }
  }
)

const handleCreate = async (data) => {
  await createItem(data)
}
```

#### READ (Lire)
```typescript
const { data: items, isLoading, execute: fetchItems } = useQuery(
  itemService.getItems
)

useEffect(() => {
  fetchItems()
}, [])
```

#### UPDATE (Mettre à jour)
```typescript
const { execute: updateItem, isLoading: isUpdating } = useMutation(
  itemService.updateItem,
  {
    successMessage: "Élément mis à jour avec succès",
    onSuccess: () => {
      setIsEditModalOpen(false)
      fetchItems()
    }
  }
)

const handleUpdate = async (id, data) => {
  await updateItem(id, data)
}
```

#### DELETE (Supprimer)
```typescript
const { execute: deleteItem, isLoading: isDeleting } = useMutation(
  itemService.deleteItem,
  {
    successMessage: "Élément supprimé avec succès",
    onSuccess: () => {
      setIsDeleteModalOpen(false)
      fetchItems()
    }
  }
)

const handleDelete = async (id) => {
  await deleteItem(id)
}
```

### 5. Gérer les états de chargement

```typescript
{isLoading ? (
  <div className="text-center py-12">
    <p className="text-gray-500">Chargement...</p>
  </div>
) : items.length === 0 ? (
  <div className="text-center py-12">
    <p className="text-gray-500">Aucun élément trouvé</p>
  </div>
) : (
  // Afficher les données
  items.map(item => <ItemCard key={item.id} item={item} />)
)}
```

### 6. Gérer les erreurs

Les erreurs sont automatiquement gérées par le hook `useApi` et affichées via des toasts. Pour une gestion personnalisée:

```typescript
const { execute: createItem } = useMutation(
  itemService.createItem,
  {
    onError: (error) => {
      console.error('Erreur personnalisée:', error)
      // Logique personnalisée
    }
  }
)
```

## Exemples de migration par composant

### Users View

Voir le fichier `components/users-view-example.tsx` pour un exemple complet.

**Changements principaux:**
1. Remplacer le tableau `users` par `useQuery(userService.getUsers)`
2. Ajouter `useMutation` pour create, update, delete
3. Implémenter `useEffect` pour charger les données au montage
4. Gérer les états de chargement

### Resources View

```typescript
// Importer le service
import { resourceService } from "@/services"

// Récupérer les salles
const { data: salles, isLoading, execute: fetchSalles } = useQuery(
  resourceService.getSalles
)

useEffect(() => {
  fetchSalles()
}, [])

// Créer une salle
const { execute: createSalle } = useMutation(
  resourceService.createSalle,
  {
    successMessage: "Salle créée avec succès",
    onSuccess: () => fetchSalles()
  }
)
```

### Courses View

```typescript
import { courseService } from "@/services"

const { data: courses, isLoading, execute: fetchCourses } = useQuery(
  courseService.getCourses
)

useEffect(() => {
  fetchCourses()
}, [])
```

### Reservations View

```typescript
import { reservationService } from "@/services"

const { data: reservations, isLoading, execute: fetchReservations } = useQuery(
  reservationService.getReservations
)

useEffect(() => {
  fetchReservations()
}, [])
```

### Schedule View

```typescript
import { scheduleService } from "@/services"

const { data: schedules, isLoading, execute: fetchSchedules } = useQuery(
  scheduleService.getSchedules
)

useEffect(() => {
  fetchSchedules()
}, [])
```

### Notifications View

```typescript
import { notificationService } from "@/services"

const { data: notifications, isLoading, execute: fetchNotifications } = useQuery(
  notificationService.getNotifications
)

useEffect(() => {
  fetchNotifications()
}, [])

// Marquer comme lu
const { execute: markAsRead } = useMutation(
  notificationService.markAsRead,
  {
    onSuccess: () => fetchNotifications()
  }
)
```

## Checklist de migration

Pour chaque composant:

- [ ] Identifier les données mockées
- [ ] Importer les services appropriés
- [ ] Importer les hooks `useQuery` et `useMutation`
- [ ] Remplacer les données statiques par des appels API
- [ ] Implémenter les mutations CRUD
- [ ] Ajouter les états de chargement
- [ ] Tester les fonctionnalités
- [ ] Gérer les cas d'erreur
- [ ] Vérifier la pagination si nécessaire
- [ ] Optimiser les re-renders

## Composants à migrer

### Priorité Haute (Backend disponible)
- [x] `auth-context.tsx` - ✅ Migré
- [ ] `login-view.tsx` - Utilise déjà auth-context
- [ ] `register-view.tsx` - Utilise déjà auth-context
- [ ] `users-view.tsx` - Exemple créé dans `users-view-example.tsx`
- [ ] `resources-view.tsx` - À migrer avec resourceService

### Priorité Moyenne (Backend à implémenter)
- [ ] `courses-view.tsx` - Utiliser courseService
- [ ] `reservations-view.tsx` - Utiliser reservationService
- [ ] `schedule-view.tsx` - Utiliser scheduleService
- [ ] `calendar-view.tsx` - Utiliser scheduleService

### Priorité Basse (Backend à implémenter)
- [ ] `notifications-view.tsx` - Utiliser notificationService
- [ ] `reports-view.tsx` - Utiliser reportService
- [ ] `conflicts-view.tsx` - Utiliser scheduleService.getConflicts

## Bonnes pratiques

1. **Toujours gérer les états de chargement**
   ```typescript
   {isLoading && <LoadingSpinner />}
   ```

2. **Gérer les listes vides**
   ```typescript
   {!isLoading && items.length === 0 && <EmptyState />}
   ```

3. **Recharger après les mutations**
   ```typescript
   onSuccess: () => fetchItems()
   ```

4. **Utiliser des messages de succès clairs**
   ```typescript
   successMessage: "Opération réussie"
   ```

5. **Nettoyer les états lors de la fermeture des modals**
   ```typescript
   onClose={() => {
     setIsModalOpen(false)
     setSelectedItem(null)
   }}
   ```

6. **Optimiser les re-renders avec useMemo/useCallback si nécessaire**
   ```typescript
   const filteredItems = useMemo(() => 
     items.filter(item => item.name.includes(searchQuery)),
     [items, searchQuery]
   )
   ```

## Dépannage

### Problème: Les données ne se chargent pas
- Vérifier que le backend est démarré
- Vérifier l'URL de l'API dans `.env.local`
- Vérifier la console pour les erreurs CORS
- Vérifier que le token JWT est valide

### Problème: Erreur 401 Unauthorized
- Vérifier que l'utilisateur est connecté
- Vérifier que le token n'est pas expiré
- Implémenter le rafraîchissement automatique du token

### Problème: Les mutations ne fonctionnent pas
- Vérifier le format des données envoyées
- Vérifier les logs du backend
- Vérifier que les endpoints existent

### Problème: Performance lente
- Implémenter la pagination
- Utiliser React Query ou SWR pour le cache
- Optimiser les re-renders avec React.memo
