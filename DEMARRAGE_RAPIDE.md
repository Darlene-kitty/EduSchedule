# Guide de Démarrage Rapide - EduSchedule

## 🚀 Démarrage en 5 Minutes

### Prérequis
- Java 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Git

### Étape 1: Démarrer MySQL
```bash
# Si vous utilisez Docker
docker run -d --name mysql-eduschedule \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=iusjcdb \
  -e MYSQL_USER=iusjc \
  -e MYSQL_PASSWORD=iusjc2025 \
  -p 3306:3306 \
  mysql:8.0

# Ou démarrez votre instance MySQL locale
```

### Étape 2: Initialiser la Base de Données
```bash
mysql -u iusjc -p iusjcdb < init-db.sql
# Mot de passe: iusjc2025
```

### Étape 3: Démarrer les Services Backend (dans l'ordre)

#### Terminal 1 - Eureka Server
```bash
cd eureka-server
mvn spring-boot:run
```
Attendez le message: `Started EurekaServerApplication`

#### Terminal 2 - Config Server (optionnel)
```bash
cd config-server
mvn spring-boot:run
```

#### Terminal 3 - User Service
```bash
cd user-service
mvn spring-boot:run
```
Attendez le message: `Started UserServiceApplication`

#### Terminal 4 - API Gateway
```bash
cd api-gateway
mvn spring-boot:run
```
Attendez le message: `Started ApiGatewayApplication`

### Étape 4: Démarrer le Frontend

#### Terminal 5 - Frontend Next.js
```bash
cd frontend
npm install
npm run dev
```

### Étape 5: Vérifier la Connexion

1. Ouvrez votre navigateur: `http://localhost:3000/test-connection`
2. Cliquez sur "Lancer les tests"
3. Tous les tests doivent être verts ✅

### Étape 6: Tester l'Application

1. Accédez à: `http://localhost:3000`
2. Cliquez sur "Se connecter"
3. Utilisez les identifiants par défaut:
   - **Username:** `admin`
   - **Password:** `admin123`

## 🔧 Vérification Rapide des Services

Exécutez le script de vérification:
```bash
check-services.bat
```

Ou vérifiez manuellement:

| Service | URL | Statut Attendu |
|---------|-----|----------------|
| Eureka | http://localhost:8761 | Page Eureka Dashboard |
| API Gateway Health | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| Frontend | http://localhost:3000 | Page d'accueil |

## 🐛 Résolution de Problèmes

### Problème: "Port already in use"
```bash
# Windows - Trouver et tuer le processus
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Problème: "Connection refused" à MySQL
1. Vérifiez que MySQL est démarré
2. Vérifiez les credentials dans `.env`
3. Testez la connexion:
```bash
mysql -u iusjc -p -h localhost -P 3306
```

### Problème: Services non enregistrés dans Eureka
1. Attendez 30 secondes après le démarrage
2. Vérifiez les logs du service
3. Vérifiez `application.properties` - `eureka.client.service-url.defaultZone`

### Problème: Erreur CORS
1. Vérifiez que l'API Gateway est démarré
2. Utilisez la page de test: `http://localhost:3000/test-connection`
3. Vérifiez les logs de l'API Gateway

## 📝 Commandes Utiles

### Nettoyer et Recompiler
```bash
# Backend
mvn clean install -DskipTests

# Frontend
cd frontend
rm -rf .next node_modules
npm install
```

### Voir les Logs
```bash
# Logs en temps réel
tail -f logs/application.log

# Logs Spring Boot
mvn spring-boot:run | tee logs/service.log
```

### Arrêter Tous les Services
```bash
# Windows
taskkill /F /IM java.exe
taskkill /F /IM node.exe

# Ou Ctrl+C dans chaque terminal
```

## 🎯 Prochaines Étapes

Une fois que tout fonctionne:

1. **Créer un compte utilisateur**
   - Allez sur `/register`
   - Remplissez le formulaire

2. **Explorer les fonctionnalités**
   - Gestion des salles
   - Création d'emplois du temps
   - Réservations

3. **Consulter la documentation**
   - `README.md` - Documentation complète
   - `CONNEXION_FIXES.md` - Détails des corrections
   - `QUICK_START.md` - Guide détaillé

## 🆘 Besoin d'Aide?

1. Consultez `CONNEXION_FIXES.md` pour les problèmes de connexion
2. Vérifiez les logs des services
3. Utilisez la page de diagnostic: `http://localhost:3000/test-connection`

## ✅ Checklist de Démarrage

- [ ] MySQL démarré et accessible
- [ ] Base de données `iusjcdb` créée et initialisée
- [ ] Eureka Server démarré (port 8761)
- [ ] User Service démarré et enregistré dans Eureka
- [ ] API Gateway démarré (port 8080)
- [ ] Frontend démarré (port 3000)
- [ ] Tests de connexion passés ✅
- [ ] Connexion réussie avec admin/admin123

Bon développement! 🚀
