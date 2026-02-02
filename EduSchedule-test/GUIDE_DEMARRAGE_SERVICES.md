# Guide de Démarrage des Services - EduSchedule

## 🎯 Réponse à votre Question

**Le frontend utilise le USER SERVICE** via l'API Gateway. Les APIs de disponibilités des enseignants sont implémentées dans le User Service, pas dans le Teacher Availability Service séparé.

**Configuration Frontend :**
```typescript
// frontend/lib/api/teacher-availability.ts
const API_BASE_URL = 'http://localhost:8080' // API Gateway
// Routes: /api/teacher-availability/* → User Service
```

## 🚀 Scripts de Démarrage Disponibles

### 1. Script Minimal (Test de Base)
```bash
.\start-services-operationnels.bat
```
**Services inclus :**
- ✅ Eureka Server (8761)
- ✅ User Service (8081) - **CONTIENT LES APIs DE DISPONIBILITÉS**
- ✅ Course Service (8084)
- ✅ Reservation Service (8085)
- ✅ Scheduling Service (8086)
- ✅ Notification Service (8082)
- ✅ Resource Service (8083)
- ✅ School Service (8087)
- ✅ API Gateway (8080)
- ✅ Frontend (3000)

**Parfait pour :** Tester les créneaux d'1 heure et fonctionnalités de base

### 2. Script Complet (Intégration Avancée)
```bash
.\start-services-complets.bat
```
**Services additionnels :**
- ✅ Config Server (8888) - Configuration centralisée
- ✅ Room Service (8088) - Suggestions de salles
- ✅ Calendar Service (8090) - Synchronisation calendrier
- ✅ Maintenance Service (8091) - Maintenance système
- ✅ Tous les services du script opérationnel

**Parfait pour :** Test complet avec toutes les intégrations

### 3. Script Backend Seulement
```bash
.\start-backend-only.bat
```
**Services inclus :** Tous les services backend sans frontend
**Usage :** Démarrage manuel du frontend avec `cd frontend && npm run dev`

## 🔗 Architecture des APIs de Disponibilités

```
Frontend (3000)
    ↓
API Gateway (8080)
    ↓
User Service (8081) ← APIs de disponibilités ici !
    ↓
Base de données H2/MySQL
```

**Routes disponibles :**
- `POST /api/teacher-availability` - Créer disponibilité
- `GET /api/teacher-availability/teacher/{id}` - Lister disponibilités
- `PUT /api/teacher-availability/{id}` - Modifier disponibilité
- `DELETE /api/teacher-availability/{id}` - Supprimer disponibilité
- `POST /api/teacher-availability/teacher/{id}/default` - Créer défaut

## 🎯 Recommandations par Cas d'Usage

### Pour Tester les Créneaux d'1 Heure
```bash
.\start-services-operationnels.bat
```
**Suffisant car :**
- User Service contient toutes les APIs nécessaires
- Frontend configuré pour utiliser User Service
- Pas besoin des services additionnels pour le test de base

### Pour Tester l'Intégration Complète
```bash
.\start-services-complets.bat
```
**Recommandé car :**
- Config Server : Configuration centralisée
- Room Service : Suggestions de salles optimales
- Calendar Service : Synchronisation Google Calendar
- Maintenance Service : Outils de maintenance

### Pour le Développement
```bash
.\start-backend-only.bat
# Puis dans un autre terminal :
cd frontend && npm run dev
```
**Avantage :** Contrôle séparé du frontend pour le développement

## ⚠️ Services à NE PAS Démarrer

### ❌ Teacher Availability Service
**Raison :** Doublon ! Les APIs sont dans User Service
**Problème :** Conflit de ports et routes

### ❌ AI Service
**Raison :** Fonctionnalités expérimentales non nécessaires

### ❌ Reporting Service
**Raison :** Skeleton seulement, pas opérationnel

## 🧪 Test des Fonctionnalités

### 1. Démarrer les Services
```bash
.\start-services-operationnels.bat
# OU
.\start-services-complets.bat
```

### 2. Attendre l'Initialisation
- **2-3 minutes** pour script opérationnel
- **3-4 minutes** pour script complet

### 3. Vérifier les Services
```bash
.\test-complete.ps1
```

### 4. Accéder à l'Application
- **Frontend :** http://localhost:3000
- **Connexion :** teacher1 / admin123
- **Page :** Disponibilités dans le menu

### 5. Tester les Créneaux d'1 Heure
- Clic "Ajouter" → Sélection créneau rapide
- Clic "En masse" → Sélection multiple
- Vérification calcul automatique heure de fin

## 📊 Ports Utilisés

| Service | Port | URL Health |
|---------|------|------------|
| Config Server | 8888 | http://localhost:8888/actuator/health |
| Eureka Server | 8761 | http://localhost:8761 |
| User Service | 8081 | http://localhost:8081/actuator/health |
| Notification | 8082 | http://localhost:8082/actuator/health |
| Resource | 8083 | http://localhost:8083/actuator/health |
| Course | 8084 | http://localhost:8084/actuator/health |
| Reservation | 8085 | http://localhost:8085/actuator/health |
| Scheduling | 8086 | http://localhost:8086/actuator/health |
| School | 8087 | http://localhost:8087/actuator/health |
| Room | 8088 | http://localhost:8088/actuator/health |
| Calendar | 8090 | http://localhost:8090/actuator/health |
| Maintenance | 8091 | http://localhost:8091/actuator/health |
| API Gateway | 8080 | http://localhost:8080/actuator/health |
| Frontend | 3000 | http://localhost:3000 |

## 🎉 Conclusion

**Pour vos tests de créneaux d'1 heure :**
1. Utilisez `.\start-services-operationnels.bat` (suffisant)
2. Le frontend utilise bien le **User Service** via l'API Gateway
3. Les services additionnels sont disponibles dans `.\start-services-complets.bat`
4. **Ne démarrez PAS** le Teacher Availability Service séparé

**Le système est prêt et fonctionnel !** ✅