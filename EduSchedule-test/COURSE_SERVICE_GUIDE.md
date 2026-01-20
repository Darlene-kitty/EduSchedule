# 🎓 Guide de Démarrage - Course Service

## 🚀 Démarrage Rapide

### Option 1: Configuration complète (première fois)
```powershell
# Crée les tables et démarre le service
.\setup-course-service.ps1
```

### Option 2: Démarrage simple (tables déjà créées)
```powershell
# Démarre juste le service
.\start-course-service.ps1
```

### Option 3: Démarrage manuel
```powershell
# 1. Aller dans le répertoire
cd course-service

# 2. Démarrer le service
mvn spring-boot:run
```

## 🧪 Test du Service

Dans un autre terminal :
```powershell
# Tester le service
.\test-course-service.ps1
```

## 📊 Endpoints Disponibles

### Cours
- `GET /api/v1/courses` - Liste tous les cours
- `POST /api/v1/courses` - Créer un cours
- `GET /api/v1/courses/{id}` - Récupérer un cours par ID
- `GET /api/v1/courses/code/{code}` - Récupérer par code
- `PUT /api/v1/courses/{id}` - Modifier un cours
- `DELETE /api/v1/courses/{id}` - Supprimer un cours
- `GET /api/v1/courses/department/{dept}` - Cours par département
- `GET /api/v1/courses/teacher/{id}` - Cours par enseignant
- `GET /api/v1/courses/search?query=...` - Recherche textuelle

### Groupes de Cours
- `GET /api/v1/course-groups` - Liste tous les groupes
- `POST /api/v1/course-groups` - Créer un groupe
- `GET /api/v1/course-groups/{id}` - Récupérer un groupe
- `GET /api/v1/course-groups/course/{id}` - Groupes par cours
- `GET /api/v1/course-groups/available` - Groupes disponibles
- `PUT /api/v1/course-groups/{id}` - Modifier un groupe
- `DELETE /api/v1/course-groups/{id}` - Supprimer un groupe

### Santé
- `GET /api/v1/courses/health` - Santé du service
- `GET /actuator/health` - Actuator Spring Boot

## 📝 Exemples d'Utilisation

### Créer un cours
```json
POST /api/v1/courses
{
  "name": "Programmation Web",
  "code": "INF102",
  "description": "Introduction au développement web",
  "credits": 6,
  "duration": 120,
  "department": "Informatique",
  "level": "L1",
  "semester": "S2",
  "teacherId": 1,
  "maxStudents": 40
}
```

### Créer un groupe
```json
POST /api/v1/course-groups
{
  "courseId": 1,
  "groupName": "TD1",
  "type": "TD",
  "maxStudents": 20,
  "teacherId": 1
}
```

## 🔧 Configuration

### Base de Données
- **Host**: localhost:3306
- **Database**: iusjcdb
- **User**: iusjc
- **Password**: iusjc2025

### Service
- **Port**: 8084
- **Eureka**: Enregistré automatiquement
- **Health Check**: http://localhost:8084/actuator/health

## 📋 Tables Créées

### `courses`
- Informations principales des cours
- Codes uniques (ex: INF101, MAT201)
- Départements, niveaux, semestres
- Enseignants responsables

### `course_groups`
- Groupes par cours (TD, TP, COURS, EXAMEN)
- Capacités et inscriptions actuelles
- Enseignants par groupe

## 🎯 Fonctionnalités

### ✅ Implémenté
- CRUD complet des cours
- Gestion des groupes de cours
- Recherche et filtrage avancés
- Validation des données
- Communication avec User Service
- Enregistrement Eureka
- Monitoring et santé

### 🔄 Intégrations
- **User Service**: Récupération des noms d'enseignants
- **Eureka Server**: Service discovery
- **MySQL**: Persistance des données

## 🚨 Dépannage

### Service ne démarre pas
1. Vérifier que MySQL est démarré
2. Vérifier les identifiants de base de données
3. Vérifier que le port 8084 est libre
4. Vérifier que Eureka Server est démarré (port 8761)

### Erreurs de base de données
1. Exécuter `.\setup-course-service.ps1` pour recréer les tables
2. Vérifier les permissions MySQL
3. Vérifier la connectivité réseau

### Tests échouent
1. Attendre que le service soit complètement démarré
2. Vérifier que les données d'exemple sont insérées
3. Vérifier les logs du service

## 📊 Statut

- ✅ **Entités**: Course, CourseGroup
- ✅ **Repositories**: Requêtes JPA complètes
- ✅ **Services**: Logique métier implémentée
- ✅ **Controllers**: API REST complète
- ✅ **Configuration**: Base de données, Eureka
- ✅ **Tests**: Scripts de validation
- ✅ **Documentation**: Guide complet

**Le Course Service est 100% fonctionnel !** 🎉