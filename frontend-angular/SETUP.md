# EduSchedule Angular Frontend - Setup Instructions

## Installation

### 1. Installer Tailwind CSS

```bash
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init
```

### 2. Configurer Tailwind dans angular.json

Ouvrez `angular.json` et ajoutez dans `build.options.styles`:

```json
"styles": [
  "src/styles.css"
]
```

### 3. Lancer l'application

```bash
npm start
```

L'application sera accessible sur http://localhost:4200

## Structure du projet

```
src/
├── app/
│   ├── core/
│   │   ├── services/
│   │   │   ├── api.service.ts
│   │   │   ├── auth.service.ts
│   │   │   ├── course.service.ts
│   │   │   ├── user.service.ts
│   │   │   └── schedule.service.ts
│   │   ├── guards/
│   │   │   └── auth.guard.ts
│   │   └── interceptors/
│   │       └── auth.interceptor.ts
│   ├── features/
│   │   ├── login/
│   │   ├── dashboard/
│   │   └── courses/
│   └── app.routes.ts
└── environments/
    ├── environment.ts
    └── environment.prod.ts
```

## Configuration Backend

Assurez-vous que le backend tourne sur http://localhost:8080

Vous pouvez modifier l'URL dans `src/environments/environment.ts`

## Fonctionnalités implémentées

✅ Authentification (Login/Logout)
✅ Dashboard avec statistiques
✅ Gestion des cours (CRUD)
✅ Guards et Interceptors
✅ Services API
✅ Routing

## Prochaines étapes

Pour compléter le frontend, vous devez ajouter :
- Calendar component
- Schedule component  
- Users component
- Notifications
- Profile
- Et autres fonctionnalités selon vos besoins
