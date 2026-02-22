# 🚀 Quick Start - Frontend Angular EduSchedule

## Installation rapide

```bash
# 1. Aller dans le dossier frontend-angular
cd frontend-angular

# 2. Installer les dépendances
npm install

# 3. Lancer le serveur de développement
npm start
```

L'application sera accessible sur **http://localhost:4200**

## 🎯 Pages à tester

### Pages publiques (accessibles sans connexion)

1. **Page d'accueil** - http://localhost:4200/
   - Hero section avec présentation
   - Features grid
   - Benefits section
   - Boutons vers login/register

2. **Login** - http://localhost:4200/login
   - Formulaire de connexion
   - Animations
   - Remember me
   - Liens vers register et forgot password

### Pages protégées (nécessitent connexion)

3. **Dashboard** - http://localhost:4200/dashboard
   - 4 cartes de statistiques
   - Score d'efficacité
   - Activités récentes
   - Prochains cours

Pour accéder aux pages protégées:
- Entrer n'importe quel username/password dans le login
- Cliquer sur "Se connecter"
- Vous serez redirigé vers le dashboard

## 🎨 Navigation

Une fois connecté, utilisez la **sidebar** pour naviguer:
- Tableau de bord
- Mon Profil
- Utilisateurs
- Écoles
- Emplois du temps
- Calendrier
- Disponibilités
- Réservation salles
- Ressources
- Cours & Groupes
- Conflits
- Notifications
- Rapports
- Analytics
- IA Prédictive
- Paramètres

## ✅ Ce qui fonctionne

### Complètement fonctionnel
- ✅ Navigation sidebar avec tous les menus
- ✅ Header avec recherche et notifications
- ✅ Page Welcome avec design complet
- ✅ Page Login avec formulaire fonctionnel
- ✅ Page Dashboard avec statistiques
- ✅ Routing entre toutes les pages
- ✅ Guard d'authentification
- ✅ Thème vert identique au React

### Structure de base créée
- ⚠️ Toutes les autres pages (Register, Profile, Users, Courses, etc.)
  - Structure créée
  - Layout en place
  - Contenu à implémenter

## 🔧 Commandes utiles

```bash
# Lancer en mode développement
npm start
# ou
ng serve

# Build de production
npm run build

# Lancer les tests
npm test

# Linting
npm run lint

# Générer un composant
ng generate component features/ma-page --standalone
```

## 📱 Test de responsive

L'application est responsive. Testez sur:
- Desktop (1920x1080)
- Tablet (768x1024)
- Mobile (375x667)

## 🎨 Thème

Le thème utilise les couleurs:
- **Vert**: #15803D (boutons, liens actifs)
- **Sidebar**: #1F2937 (fond gris foncé)
- **Jaune**: #FBBF24 (accents, logo)

## 📝 Prochaines étapes

1. Tester les 3 pages complètes (Welcome, Login, Dashboard)
2. Vérifier la navigation sidebar
3. Tester le responsive
4. Implémenter le contenu des autres pages (voir IMPLEMENTATION_GUIDE.md)

## 🐛 Problèmes courants

### Port déjà utilisé
```bash
# Changer le port
ng serve --port 4201
```

### Erreurs de compilation
```bash
# Nettoyer et réinstaller
rm -rf node_modules package-lock.json
npm install
```

### Tailwind ne fonctionne pas
```bash
# Vérifier que tailwind.config.js existe
# Vérifier que @import "tailwindcss" est dans styles.css
```

## 📞 Support

- Documentation complète: `README.md`
- Guide d'implémentation: `IMPLEMENTATION_GUIDE.md`
- État d'avancement: `STATUS.md`
- Résumé: `SUMMARY.md`

## 🎉 Enjoy!

Vous avez maintenant un frontend Angular fonctionnel avec le même design que le React!
