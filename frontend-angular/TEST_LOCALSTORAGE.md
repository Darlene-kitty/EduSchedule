# Guide de test du LocalStorage

## 🧪 Tests à effectuer

### 1. Test de connexion et persistance

**Étapes :**
1. Ouvrir l'application en mode incognito
2. Aller sur `/login`
3. Entrer n'importe quel username et password
4. Cocher "Se souvenir de moi"
5. Cliquer sur "Se connecter"

**Vérifications :**
- ✅ Redirection vers `/dashboard`
- ✅ Ouvrir DevTools > Application > Local Storage
- ✅ Vérifier la présence de :
  - `token` : "mock-jwt-token-..."
  - `user` : objet JSON avec username, email, etc.
  - `savedEmail` : l'email entré

**Test de persistance :**
6. Rafraîchir la page (F5)
7. Vérifier que vous restez connecté
8. Vérifier que le nom d'utilisateur s'affiche dans le header

### 2. Test de modification du profil

**Étapes :**
1. Se connecter
2. Aller sur `/profile`
3. Cliquer sur "Modifier"
4. Changer le nom, email, téléphone
5. Cliquer sur "Enregistrer"

**Vérifications :**
- ✅ Message "Profil mis à jour avec succès"
- ✅ Ouvrir DevTools > Local Storage
- ✅ Vérifier que `user` contient les nouvelles données
- ✅ Rafraîchir la page
- ✅ Vérifier que les modifications sont toujours là

### 3. Test des préférences

**Étapes :**
1. Se connecter
2. Aller sur `/profile`
3. Onglet "Préférences"
4. Modifier :
   - Notifications email
   - Notifications push
   - Langue
5. Cliquer sur "Sauvegarder"

**Vérifications :**
- ✅ Message "Préférences sauvegardées"
- ✅ Ouvrir DevTools > Local Storage
- ✅ Vérifier la présence de `preferences` avec les bonnes valeurs
- ✅ Rafraîchir la page
- ✅ Vérifier que les préférences sont conservées

### 4. Test de gestion des utilisateurs

**Étapes :**
1. Se connecter
2. Aller sur `/users`
3. Cliquer sur "Ajouter un utilisateur"
4. Remplir le formulaire
5. Cliquer sur "Ajouter"

**Vérifications :**
- ✅ L'utilisateur apparaît dans la liste
- ✅ Ouvrir DevTools > Local Storage
- ✅ Vérifier la présence de `usersList` (array)
- ✅ Rafraîchir la page
- ✅ Vérifier que l'utilisateur est toujours là

**Test de modification :**
6. Cliquer sur "Modifier" sur un utilisateur
7. Changer des informations
8. Sauvegarder

**Vérifications :**
- ✅ Les modifications sont visibles
- ✅ `usersList` dans localStorage est mis à jour
- ✅ Rafraîchir : les modifications persistent

**Test de suppression :**
9. Cliquer sur "Supprimer" sur un utilisateur
10. Confirmer

**Vérifications :**
- ✅ L'utilisateur disparaît de la liste
- ✅ `usersList` dans localStorage ne contient plus cet utilisateur
- ✅ Rafraîchir : l'utilisateur reste supprimé

### 5. Test de déconnexion

**Étapes :**
1. Se connecter
2. Vérifier que le localStorage contient des données
3. Cliquer sur "Déconnexion"

**Vérifications :**
- ✅ Redirection vers `/login`
- ✅ Ouvrir DevTools > Local Storage
- ✅ Vérifier que `token` et `user` sont supprimés
- ✅ `savedEmail` reste (si "Se souvenir de moi" était coché)
- ✅ Essayer d'accéder à `/dashboard` : redirection vers `/login`

### 6. Test du cache

**Étapes :**
1. Se connecter
2. Aller sur `/dashboard`
3. Ouvrir DevTools > Local Storage
4. Chercher les clés commençant par `cache_`

**Vérifications :**
- ✅ Présence de `cache_dashboardStats` (si implémenté)
- ✅ Vérifier la structure : `{ data, timestamp, ttl }`
- ✅ Rafraîchir : le cache est utilisé

### 7. Test multi-onglets

**Étapes :**
1. Se connecter dans l'onglet 1
2. Ouvrir un nouvel onglet (onglet 2)
3. Aller sur l'application dans l'onglet 2

**Vérifications :**
- ✅ L'utilisateur est déjà connecté dans l'onglet 2
- ✅ Les données sont synchronisées

**Test de modification :**
4. Dans l'onglet 1, modifier le profil
5. Dans l'onglet 2, rafraîchir

**Vérifications :**
- ✅ Les modifications sont visibles dans l'onglet 2

### 8. Test de "Se souvenir de moi"

**Test avec la case cochée :**
1. Se déconnecter
2. Aller sur `/login`
3. Entrer email : `test@example.com`
4. Cocher "Se souvenir de moi"
5. Se connecter
6. Se déconnecter
7. Retourner sur `/login`

**Vérifications :**
- ✅ L'email `test@example.com` est pré-rempli
- ✅ La case "Se souvenir de moi" est cochée

**Test sans la case cochée :**
8. Décocher "Se souvenir de moi"
9. Changer l'email
10. Se connecter
11. Se déconnecter
12. Retourner sur `/login`

**Vérifications :**
- ✅ L'email n'est pas pré-rempli (ou ancien email)

## 🔍 Inspection du LocalStorage

Pour voir toutes les données stockées :

```javascript
// Dans la console DevTools
console.table(Object.entries(localStorage));
```

Pour voir une clé spécifique :

```javascript
// Voir l'utilisateur
console.log(JSON.parse(localStorage.getItem('user')));

// Voir le token
console.log(localStorage.getItem('token'));

// Voir les préférences
console.log(JSON.parse(localStorage.getItem('preferences')));

// Voir la liste des utilisateurs
console.log(JSON.parse(localStorage.getItem('usersList')));
```

## 🐛 Problèmes courants

### L'utilisateur n'est pas persisté après rafraîchissement

**Solution :**
- Vérifier que `AuthService.constructor()` appelle `loadUserFromStorage()`
- Vérifier que le token est bien dans le localStorage
- Vérifier la console pour des erreurs

### Les modifications ne sont pas sauvegardées

**Solution :**
- Vérifier que le service appelle bien `StorageService.setItem()`
- Vérifier qu'il n'y a pas d'erreur dans la console
- Vérifier que le mode mock est activé (pas d'appel API qui échoue)

### Le localStorage est vide

**Solution :**
- Vérifier que vous n'êtes pas en mode navigation privée avec restrictions
- Vérifier que le localStorage n'est pas désactivé dans le navigateur
- Vérifier la console pour des erreurs de sécurité

## ✅ Checklist complète

- [ ] Connexion sauvegarde le token et l'utilisateur
- [ ] Rafraîchissement conserve la session
- [ ] Modification du profil persiste
- [ ] Préférences sont sauvegardées
- [ ] Ajout d'utilisateur persiste
- [ ] Modification d'utilisateur persiste
- [ ] Suppression d'utilisateur persiste
- [ ] Déconnexion nettoie les données
- [ ] "Se souvenir de moi" fonctionne
- [ ] Multi-onglets synchronisé
- [ ] Cache fonctionne avec TTL

## 📊 Structure attendue du LocalStorage

```json
{
  "token": "mock-jwt-token-1234567890",
  "user": {
    "id": 1,
    "username": "admin",
    "name": "admin",
    "email": "admin@edu.com",
    "role": "admin"
  },
  "preferences": {
    "theme": "light",
    "language": "fr",
    "notifications": true,
    "emailNotifications": true,
    "calendarView": "week"
  },
  "savedEmail": "admin@edu.com",
  "usersList": [
    {
      "id": 1,
      "name": "Admin Système",
      "email": "admin@edu.com",
      "role": "admin",
      "phone": "01 23 45 67 89",
      "department": "Administration",
      "status": "active",
      "createdAt": "2024-01-15"
    }
  ],
  "cache_dashboardStats": {
    "data": [...],
    "timestamp": 1234567890,
    "ttl": 3600000
  }
}
```

## 🎯 Résultat attendu

Après tous les tests, vous devriez avoir :
- Une session persistante qui survit aux rafraîchissements
- Des modifications qui sont sauvegardées automatiquement
- Des données qui persistent entre les onglets
- Un nettoyage propre lors de la déconnexion
- Une expérience utilisateur fluide sans perte de données
