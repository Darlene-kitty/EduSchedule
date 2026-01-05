# 🚀 Quick Start - EduSchedule IUSJC

## 📋 Prérequis
- Java 17
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+ (pour le frontend)

## ⚡ Démarrage Rapide

### Option 1 : Infrastructure + Services Manuels (Développement)

```bash
# 1. Configurer Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# 2. Lancer l'infrastructure Docker
docker-compose up -d

# 3. Attendre que MySQL soit prêt (30 secondes)
sleep 30

# 4. Lancer les microservices (dans des terminaux séparés)
cd config-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd resource-service && mvn spring-boot:run
cd scheduling-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run

# 5. Lancer le frontend
cd frontend && npm install && npm run dev
```

### Option 2 : Tout avec Docker (Production)

```bash
# 1. Configurer Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# 2. Build tous les services
mvn clean package -DskipTests

# 3. Lancer tout avec Docker
docker-compose -f docker-compose.full.yml up -d

# 4. Attendre le démarrage (60-90 secondes)
docker-compose -f docker-compose.full.yml logs -f
```

### Option 3 : Script Automatique (Windows)

```bash
.\start-all.bat
```

## 🌐 URLs d'Accès

### Frontend
- **Page d'accueil** : http://localhost:3000/welcome
- **Connexion** : http://localhost:3000/login
- **Dashboard** : http://localhost:3000

### Backend
- **API Gateway** : http://localhost:8080
- **Eureka Server** : http://localhost:8761
- **Config Server** : http://localhost:8888

### Infrastructure
- **MySQL** : localhost:3306
- **Redis** : localhost:6379
- **RabbitMQ Management** : http://localhost:15672 (iusjc/iusjc2025)
- **Zipkin** : http://localhost:9411

## 👤 Comptes de Test

### Administrateur
```
Email: admin@iusjc.cm
Mot de passe: password
```

### Enseignant
```
Email: dupont@iusjc.cm
Mot de passe: password
```

## 🔍 Vérification

### 1. Vérifier les conteneurs Docker
```bash
docker ps
```
Vous devriez voir : mysql, redis, rabbitmq, eureka-server, zipkin, frontend

### 2. Vérifier Eureka
Ouvrir http://localhost:8761
Tous les services doivent être **UP** (en vert)

### 3. Vérifier le Frontend
Ouvrir http://localhost:3000/welcome
Vous devriez voir la page d'accueil EduSchedule

### 4. Tester la connexion
1. Cliquer sur "Se connecter"
2. Utiliser admin@iusjc.cm / password
3. Vous devriez être redirigé vers le dashboard

## 🛑 Arrêter les Services

### Infrastructure uniquement
```bash
docker-compose down
```

### Tout (Docker complet)
```bash
docker-compose -f docker-compose.full.yml down
```

### Supprimer les volumes (reset complet)
```bash
docker-compose down -v
```

## 🐛 Dépannage

### Erreur "invalid target release: 17"
```bash
# Vérifier Java
java -version  # Doit afficher Java 17

# Configurer Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

### Erreur "Access denied for user 'iusjc'"
```bash
# Recréer MySQL avec les bons credentials
docker-compose down -v
docker-compose up -d mysql
sleep 30
```

### Port déjà utilisé
```bash
# Vérifier les ports utilisés
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac

# Arrêter le processus ou changer le port
```

### Services ne démarrent pas dans Eureka
```bash
# Vérifier les logs
docker-compose logs -f eureka-server

# Redémarrer les services
docker-compose restart
```

## 📚 Documentation

- [Architecture](./ARCHITECTURE.md)
- [Système de Rôles](./ROLES_SYSTEM.md)
- [Page d'Accueil](./frontend/WELCOME_PAGE.md)
- [API Documentation](./docs/API_INTEGRATION.md)

## 🎯 Prochaines Étapes

1. ✅ Lancer le système
2. ✅ Se connecter avec un compte test
3. ✅ Explorer le dashboard
4. ✅ Créer un emploi du temps
5. ✅ Gérer les salles et ressources
6. ✅ Consulter les rapports

## 💡 Conseils

- **Développement** : Utilisez l'Option 1 pour un rechargement rapide
- **Tests** : Utilisez l'Option 2 pour un environnement complet
- **Production** : Utilisez Docker avec des variables d'environnement sécurisées

## 🆘 Support

En cas de problème :
1. Vérifier les logs : `docker-compose logs -f`
2. Vérifier Eureka : http://localhost:8761
3. Consulter la documentation
4. Contacter l'équipe technique IUSJC
