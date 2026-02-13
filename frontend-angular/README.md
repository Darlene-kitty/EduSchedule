# Frontend Angular - EduSchedule

Frontend Angular pour le système de gestion des emplois du temps EduSchedule (IUSJC).

## 🎯 Objectif

Recréer l'intégralité du frontend React en Angular avec le même design, les mêmes fonctionnalités et la même navigation.

## ✅ État actuel

### Pages complètes (avec design et fonctionnalités)
- ✅ **Login** - Page de connexion avec animations
- ✅ **Welcome** - Page d'accueil avec présentation
- ✅ **Dashboard** - Tableau de bord avec statistiques

### Pages créées (structure de base)
- Register, Profile, Users, Courses
- Schedule, Calendar, Reservations
- Resources, Rooms, Conflicts
- Notifications, Reports
- Teacher Availability, Events

### Composants partagés
- ✅ **Sidebar** - Navigation latérale
- ✅ **Header** - En-tête avec recherche
- ✅ **AppLayout** - Layout réutilisable

## 🚀 Installation

```bash
# Installer les dépendances
npm install

# Lancer le serveur de développement
npm start

# Ou avec Angular CLI
ng serve
```

L'application sera accessible sur `http://localhost:4200`

## 📁 Structure du projet

```
frontend-angular/
├── src/
│   ├── app/
│   │   ├── core/                    # Services, guards, interceptors
│   │   │   ├── guards/
│   │   │   │   └── auth.guard.ts
│   │   │   ├── services/
│   │   │   └── interceptors/
│   │   ├── shared/                  # Composants partagés
│   │   │   └── components/
│   │   │       ├── sidebar/
│   │   │       ├── header/
│   │   │       └── app-layout/
│   │   ├── features/                # Pages de l'application
│   │   │   ├── login/
│   │   │   ├── welcome/
│   │   │   ├── dashboard/
│   │   │   ├── courses/
│   │   │   ├── users/
│   │   │   ├── schedule/
│   │   │   ├── calendar/
│   │   │   ├── reservations/
│   │   │   ├── resources/
│   │   │   ├── rooms/
│   │   │   ├── conflicts/
│   │   │   ├── notifications/
│   │   │   ├── reports/
│   │   │   ├── teacher-availability/
│   │   │   └── events/
│   │   ├── app.routes.ts            # Configuration des routes
│   │   ├── app.config.ts
│   │   └── app.ts
│   ├── styles.css                   # Styles globaux
│   └── index.html
├── tailwind.config.js               # Configuration Tailwind
├── angular.json
├── package.json
└── README.md
```

## 🎨 Design System

### Couleurs principales
- **Vert principal**: `#15803D` (primary-600)
- **Sidebar**: `#1F2937` (gris foncé)
- **Sidebar hover**: `#374151`
- **Accent**: `#FBBF24` (jaune/or)

### Composants Tailwind
Le projet utilise Tailwind CSS avec les classes utilitaires standard.

## 🔐 Authentification

Le guard `authGuard` protège les routes nécessitant une authentification.

```typescript
// Routes protégées
{
  path: 'dashboard',
  loadComponent: () => import('./features/dashboard/dashboard.component'),
  canActivate: [authGuard]
}
```

## 📱 Pages disponibles

### Publiques
- `/` - Redirection vers welcome
- `/welcome` - Page d'accueil
- `/login` - Connexion
- `/register` - Inscription

### Protégées (nécessitent authentification)
- `/dashboard` - Tableau de bord
- `/profile` - Profil utilisateur
- `/users` - Gestion des utilisateurs
- `/courses` - Gestion des cours
- `/schedule` - Emplois du temps
- `/calendar` - Calendrier
- `/reservations` - Réservations de salles
- `/resources` - Ressources pédagogiques
- `/rooms` - Gestion des salles
- `/conflicts` - Résolution des conflits
- `/notifications` - Centre de notifications
- `/reports` - Rapports et statistiques
- `/teacher-availability` - Disponibilités enseignants
- `/events` - Événements académiques

## 🛠️ Scripts utiles

```bash
# Générer toutes les pages (déjà fait)
powershell -ExecutionPolicy Bypass -File generate-pages.ps1

# Guide de copie du contenu React
powershell -ExecutionPolicy Bypass -File copy-react-content.ps1

# Build de production
npm run build

# Tests
npm test

# Linting
npm run lint
```

## 📝 Guide de développement

### Créer une nouvelle page

1. Générer le composant:
```bash
ng generate component features/ma-page --standalone
```

2. Utiliser le layout:
```typescript
import { AppLayoutComponent } from '../../shared/components/app-layout/app-layout.component';

@Component({
  imports: [CommonModule, AppLayoutComponent],
  // ...
})
```

3. Template:
```html
<app-layout activePage="ma-page" title="Mon Titre" subtitle="Sous-titre">
  <div header-actions>
    <!-- Actions du header -->
  </div>
  
  <!-- Contenu de la page -->
</app-layout>
```

### Adapter du code React

**React:**
```tsx
const [data, setData] = useState([]);

useEffect(() => {
  loadData();
}, []);

const loadData = async () => {
  const result = await api.getData();
  setData(result);
};
```

**Angular:**
```typescript
data: any[] = [];

ngOnInit() {
  this.loadData();
}

async loadData() {
  this.data = await this.api.getData();
}
```

## 🔗 API Backend

Le backend Spring Boot tourne sur `http://localhost:8080`

Endpoints principaux:
- `/api/auth/*` - Authentification
- `/api/courses/*` - Cours
- `/api/users/*` - Utilisateurs
- `/api/schedules/*` - Emplois du temps
- `/api/reservations/*` - Réservations

## 📚 Documentation

- [Guide d'implémentation](IMPLEMENTATION_GUIDE.md)
- [Documentation Angular](https://angular.dev)
- [Tailwind CSS](https://tailwindcss.com)
- [Frontend React source](../frontend/)

## 🤝 Contribution

1. Créer une branche pour votre fonctionnalité
2. Implémenter les changements
3. Tester localement
4. Créer une pull request

## 📄 Licence

© 2025 Institut Universitaire Saint Jean - Cameroun
