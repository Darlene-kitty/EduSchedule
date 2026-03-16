# Intégration du LocalStorage dans Angular

## Vue d'ensemble

Le localStorage a été intégré dans l'application Angular via une architecture de services centralisés pour gérer de manière cohérente et sécurisée les données côté client.

## Services créés

### 1. StorageService (`storage.service.ts`)
Service principal pour gérer toutes les interactions avec le localStorage.

**Fonctionnalités:**
- Gestion sécurisée du localStorage avec vérification SSR
- Sérialisation/désérialisation automatique JSON
- Méthodes génériques et spécifiques
- Gestion des erreurs

**Méthodes principales:**
```typescript
setItem(key: string, value: any): void
getItem<T>(key: string): T | null
removeItem(key: string): void
clear(): void
setToken(token: string): void
getToken(): string | null
setUser(user: any): void
getUser(): any
isAuthenticated(): boolean
```

### 2. CacheService (`cache.service.ts`)
Service pour gérer le cache des données avec TTL (Time To Live).

**Fonctionnalités:**
- Cache avec expiration automatique
- Gestion du TTL par élément
- Nettoyage du cache expiré
- Préfixage automatique des clés

**Méthodes principales:**
```typescript
set<T>(key: string, data: T, ttl?: number): void
get<T>(key: string): T | null
remove(key: string): void
has(key: string): boolean
clearAll(): void
```

### 3. PreferencesService (`preferences.service.ts`)
Service pour gérer les préférences utilisateur.

**Fonctionnalités:**
- Gestion du thème (light/dark)
- Préférences de langue
- Notifications
- Vue du calendrier

**Méthodes principales:**
```typescript
getPreferences(): UserPreferences
updatePreferences(preferences: Partial<UserPreferences>): void
resetPreferences(): void
setTheme(theme: 'light' | 'dark'): void
```

## Services mis à jour

### AuthService
- Utilise StorageService pour gérer le token et l'utilisateur
- Charge automatiquement l'utilisateur au démarrage
- Synchronise les données avec le localStorage
- Gère la connexion/déconnexion avec persistance

### UserService
- Intégré avec StorageService
- Charge l'utilisateur depuis le localStorage au démarrage
- Synchronise les modifications avec le localStorage

### AuthInterceptor
- Utilise StorageService pour récupérer le token
- Nettoie le localStorage en cas d'erreur 401

## Composants mis à jour

### LoginComponent
- Utilise AuthService pour la connexion
- Sauvegarde l'email si "Se souvenir de moi" est coché
- Gestion des erreurs améliorée

### RegisterComponent
- Utilise AuthService pour l'inscription
- Sauvegarde automatique des données utilisateur
- Gestion des erreurs améliorée

### ProfileComponent
- Charge les données depuis le localStorage au démarrage
- Sauvegarde les modifications dans le localStorage
- Gère les préférences utilisateur

### DashboardComponent
- Charge les données utilisateur
- Cache les statistiques du dashboard
- Affiche le nom de l'utilisateur connecté

### HeaderComponent
- Affiche les informations de l'utilisateur connecté
- Récupère les données depuis AuthService
- Fonction de déconnexion intégrée

## Structure des données dans le localStorage

```javascript
{
  // Authentification
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  
  // Utilisateur
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "admin"
  },
  
  // Préférences
  "preferences": {
    "theme": "light",
    "language": "fr",
    "notifications": true,
    "emailNotifications": true,
    "calendarView": "week"
  },
  
  // Email sauvegardé
  "savedEmail": "john@example.com",
  
  // Cache (avec préfixe cache_)
  "cache_dashboardStats": {
    "data": [...],
    "timestamp": 1234567890,
    "ttl": 3600000
  }
}
```

## Utilisation dans les composants

### Exemple 1: Sauvegarder des données
```typescript
import { inject } from '@angular/core';
import { StorageService } from '@core/services';

export class MyComponent {
  private storageService = inject(StorageService);

  saveData() {
    this.storageService.setItem('myKey', { data: 'value' });
  }
}
```

### Exemple 2: Récupérer des données
```typescript
ngOnInit() {
  const data = this.storageService.getItem<MyType>('myKey');
  if (data) {
    this.myData = data;
  }
}
```

### Exemple 3: Utiliser le cache
```typescript
import { CacheService } from '@core/services';

export class MyComponent {
  private cacheService = inject(CacheService);

  loadData() {
    // Vérifier le cache d'abord
    let data = this.cacheService.get<MyData>('myData');
    
    if (!data) {
      // Charger depuis l'API
      this.apiService.getData().subscribe(response => {
        data = response;
        // Sauvegarder dans le cache pour 1 heure
        this.cacheService.set('myData', data, 3600000);
      });
    }
  }
}
```

### Exemple 4: Gérer les préférences
```typescript
import { PreferencesService } from '@core/services';

export class SettingsComponent {
  private preferencesService = inject(PreferencesService);

  changeTheme(theme: 'light' | 'dark') {
    this.preferencesService.setTheme(theme);
  }

  updateNotifications(enabled: boolean) {
    this.preferencesService.updatePreferences({
      notifications: enabled
    });
  }
}
```

## Bonnes pratiques

1. **Toujours utiliser StorageService** au lieu d'accéder directement à localStorage
2. **Vérifier SSR**: Les services gèrent automatiquement le SSR
3. **Utiliser le cache** pour les données qui changent peu
4. **Définir un TTL** approprié pour le cache
5. **Nettoyer les données** lors de la déconnexion
6. **Typer les données** avec TypeScript pour la sécurité

## Sécurité

- Ne jamais stocker de mots de passe en clair
- Les tokens sont stockés de manière sécurisée
- Nettoyage automatique lors de la déconnexion
- Vérification SSR pour éviter les erreurs

## Tests

Pour tester l'intégration:

1. Se connecter → Vérifier que le token et l'utilisateur sont sauvegardés
2. Rafraîchir la page → Vérifier que l'utilisateur reste connecté
3. Modifier le profil → Vérifier que les changements persistent
4. Se déconnecter → Vérifier que les données sont nettoyées
5. Cocher "Se souvenir de moi" → Vérifier que l'email est sauvegardé

## Prochaines étapes

- Ajouter le chiffrement pour les données sensibles
- Implémenter la synchronisation avec IndexedDB pour les grandes données
- Ajouter des événements de synchronisation entre onglets
- Créer un système de migration pour les changements de structure
