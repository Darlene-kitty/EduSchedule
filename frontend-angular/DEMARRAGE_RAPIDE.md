# 🚀 Démarrage Rapide - Frontend Angular

## Installation et lancement

```bash
cd frontend-angular
npm install
npm start
```

L'application sera accessible sur `http://localhost:4200`

## 🔐 Connexion (Mode Développement)

### Option 1: Connexion simple
1. Allez sur `http://localhost:4200/login`
2. Entrez n'importe quel username et password
3. Cliquez sur "Se connecter"
4. ✅ Vous êtes connecté et redirigé vers le dashboard

### Option 2: Inscription
1. Allez sur `http://localhost:4200/register`
2. Remplissez le formulaire
3. Cliquez sur "S'inscrire"
4. ✅ Vous êtes inscrit et connecté automatiquement

## 📱 Pages disponibles

### Pages publiques (sans connexion)
- `/welcome` - Page d'accueil
- `/login` - Connexion
- `/register` - Inscription
- `/forgot-password` - Mot de passe oublié

### Pages protégées (nécessitent une connexion)
- `/dashboard` - Tableau de bord
- `/profile` - Profil utilisateur
- `/users` - Gestion des utilisateurs
- `/courses` - Gestion des cours
- `/schedule` - Emplois du temps
- `/calendar` - Calendrier
- `/reservations` - Réservations
- `/resources` - Ressources
- `/rooms` - Salles
- `/conflicts` - Conflits
- `/notifications` - Notifications
- `/reports` - Rapports
- `/teacher-availability` - Disponibilités enseignants
- `/events` - Événements

## 🔧 Fonctionnalités implémentées

### ✅ Authentification
- Connexion avec simulation d'API
- Inscription avec simulation d'API
- Déconnexion
- Persistance de la session (localStorage)
- Guard de navigation pour les routes protégées
- Intercepteur HTTP pour ajouter le token

### ✅ Gestion des données
- StorageService pour le localStorage
- CacheService avec TTL
- PreferencesService pour les préférences utilisateur
- Synchronisation automatique entre composants

### ✅ Interface utilisateur
- Sidebar de navigation
- Header avec informations utilisateur
- Dashboard avec statistiques
- Profil utilisateur éditable
- Gestion des préférences

## 🧪 Tester l'intégration localStorage

### 1. Test de connexion
```javascript
// Dans la console du navigateur (F12)
localStorage.getItem('token')
// Devrait retourner: "mock-jwt-token-..."

JSON.parse(localStorage.getItem('user'))
// Devrait retourner l'objet utilisateur
```

### 2. Test de persistance
1. Connectez-vous
2. Rafraîchissez la page (F5)
3. Vous devriez rester connecté ✅

### 3. Test de déconnexion
1. Cliquez sur déconnexion
2. Vérifiez le localStorage:
```javascript
localStorage.getItem('token') // null
localStorage.getItem('user')  // null
```

### 4. Test des préférences
1. Allez sur `/profile`
2. Modifiez vos informations
3. Rafraîchissez la page
4. Les modifications sont conservées ✅

## 📦 Structure des services

```
core/
├── services/
│   ├── storage.service.ts      # Gestion du localStorage
│   ├── auth.service.ts         # Authentification
│   ├── user.service.ts         # Gestion utilisateurs
│   ├── cache.service.ts        # Cache avec TTL
│   ├── preferences.service.ts  # Préférences utilisateur
│   └── index.ts               # Export centralisé
├── guards/
│   └── auth.guard.ts          # Protection des routes
└── interceptors/
    └── auth.interceptor.ts    # Ajout du token HTTP
```

## 🔄 Passer en mode production (avec API réelle)

### 1. Configurer l'URL de l'API

Éditez `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### 2. Activer l'API dans AuthService

Dans `auth.service.ts`, décommentez le code API et commentez le code de simulation:

```typescript
// Commenter la simulation
/*
const mockResponse: AuthResponse = { ... };
setTimeout(() => { ... }, 500);
*/

// Décommenter l'appel API
this.apiService.post<AuthResponse>('/auth/login', credentials).subscribe({
  next: (response) => { ... },
  error: (error) => { ... }
});
```

### 3. Configurer le proxy (optionnel)

Le fichier `proxy.conf.json` est déjà configuré pour rediriger `/api` vers `http://localhost:8080`

## 🐛 Dépannage

### Problème: "Cannot navigate to /dashboard"
**Solution**: Vérifiez que vous êtes bien connecté
```javascript
localStorage.getItem('token') // Doit retourner un token
```

### Problème: "Page blanche après connexion"
**Solution**: Ouvrez la console (F12) et vérifiez les erreurs

### Problème: "Déconnecté après rafraîchissement"
**Solution**: Vérifiez que le localStorage fonctionne
```javascript
localStorage.setItem('test', 'value')
localStorage.getItem('test') // Doit retourner 'value'
```

### Problème: "Erreur de compilation"
**Solution**: 
```bash
rm -rf node_modules package-lock.json
npm install
npm start
```

## 📚 Documentation complète

- `LOCALSTORAGE_INTEGRATION.md` - Documentation détaillée de l'intégration
- `INTEGRATION_SUMMARY.md` - Résumé des modifications
- `TEST_LOGIN.md` - Guide de test de connexion

## 🎯 Prochaines étapes

1. ✅ Connexion fonctionnelle
2. ✅ Persistance des données
3. ✅ Navigation protégée
4. 🔄 Connecter à l'API backend réelle
5. 🔄 Ajouter la validation des formulaires
6. 🔄 Améliorer la gestion des erreurs
7. 🔄 Ajouter des tests unitaires

## 💡 Astuces

- Utilisez `Ctrl+Shift+I` (ou F12) pour ouvrir les DevTools
- Onglet "Application" > "Local Storage" pour voir les données
- Onglet "Network" pour voir les requêtes HTTP
- Onglet "Console" pour voir les logs et erreurs
