# Test de connexion

## Mode développement activé

Le service AuthService est maintenant configuré en mode développement avec une simulation de l'API.

## Comment tester la connexion

1. **Accéder à la page de connexion**
   - URL: `http://localhost:4200/login`

2. **Entrer n'importe quelles informations**
   - Username: n'importe quel texte (ex: `admin`)
   - Password: n'importe quel texte (ex: `password`)
   - Cliquer sur "Se connecter"

3. **Vérification**
   - Vous devriez être redirigé vers `/dashboard`
   - Les données sont sauvegardées dans le localStorage
   - L'utilisateur reste connecté après rafraîchissement

## Données simulées

Lors de la connexion, les données suivantes sont créées :

```json
{
  "token": "mock-jwt-token-1234567890",
  "user": {
    "id": 1,
    "username": "admin",
    "name": "admin",
    "email": "admin@edu.com",
    "role": "admin"
  }
}
```

## Vérifier le localStorage

Ouvrez la console du navigateur (F12) et tapez :

```javascript
// Voir le token
localStorage.getItem('token')

// Voir l'utilisateur
JSON.parse(localStorage.getItem('user'))

// Voir toutes les données
console.table(Object.entries(localStorage))
```

## Test d'inscription

1. **Accéder à la page d'inscription**
   - URL: `http://localhost:4200/register`

2. **Remplir le formulaire**
   - Nom: votre nom
   - Email: votre email
   - Téléphone: votre téléphone
   - Rôle: choisir un rôle
   - Mot de passe: minimum 8 caractères
   - Confirmer le mot de passe

3. **Vérification**
   - Vous devriez être redirigé vers `/dashboard`
   - Les données sont sauvegardées

## Test de déconnexion

1. Cliquer sur le bouton de déconnexion (dans le header ou le profil)
2. Vous devriez être redirigé vers `/login`
3. Le localStorage devrait être nettoyé

## Test de persistance

1. Se connecter
2. Rafraîchir la page (F5)
3. Vous devriez rester connecté
4. Naviguer vers une autre page protégée
5. Tout devrait fonctionner

## Activer l'API réelle

Quand votre backend est prêt, modifiez `auth.service.ts` :

1. Commentez le code de simulation
2. Décommentez le code avec `this.apiService.post()`
3. Configurez l'URL de l'API dans `environments/environment.ts`

## Problèmes courants

### "Cannot read property of undefined"
- Vérifiez que tous les services sont bien injectés
- Vérifiez la console pour les erreurs

### Redirection infinie
- Vérifiez que le guard `authGuard` fonctionne
- Vérifiez que `isAuthenticated()` retourne bien `true` après connexion

### Données non sauvegardées
- Ouvrez la console et vérifiez les erreurs
- Vérifiez que le localStorage n'est pas désactivé dans le navigateur

## Commandes utiles

```bash
# Démarrer l'application
cd frontend-angular
npm start

# Nettoyer le cache
npm run clean

# Rebuild
npm run build
```
