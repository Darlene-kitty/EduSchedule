# 🧪 Guide de Test Manuel - IUSJC Planning 2025

## ⚠️ Prérequis

Avant de commencer, assurez-vous d'avoir installé :

### Windows
1. **Docker Desktop** : https://www.docker.com/products/docker-desktop/
2. **Java 17** : https://adoptium.net/
3. **Maven** : https://maven.apache.org/download.cgi

### Linux/Mac
```bash
# Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Java 17
sudo apt install openjdk-17-jdk  # Ubuntu/Debian
brew install openjdk@17          # Mac

# Maven
sudo apt install maven           # Ubuntu/Debian
brew install maven               # Mac
```

---

## 📋 Checklist de Vérification

### 1. Vérifier les Fichiers de Configuration

```bash
# Windows PowerShell
Get-ChildItem -Name .env, docker-compose.yml, init-db.sql, pom.xml

# Linux/Mac
ls -la .env docker-compose.yml init-db.sql pom.xml
```

**Résultat attendu :** Tous les fichiers doivent exister

---

### 2. Vérifier les Installations

#### Windows PowerShell
```powershell
# Docker
docker --version
docker compose version

# Java
java -version

# Maven
mvn -version
```

#### Linux/Mac
```bash
# Docker
docker --version
docker compose version

# Java
java -version

# Maven
mvn -version
```

**Résultats attendus :**
- Docker : version 20.10+
- Docker Compose : version 2.0+
- Java : version 17+
- Maven : version 3.8+

---

## 🚀 Étapes de Test

### Étape 1 : Configuration Initiale

#### Windows PowerShell
```powershell
# Vérifier/Créer .env
if (!(Test-Path .env)) {
    Copy-Item .env.example .env
    Write-Host "✅ .env créé"
}

# Vérifier le contenu
Get-Content .env | Select-String "MYSQL_DATABASE"
```

#### Linux/Mac
```bash
# Vérifier/Créer .env
if [ ! -f .env ]; then
    cp .env.example .env
    echo "✅ .env créé"
fi

# Vérifier le contenu
grep MYSQL_DATABASE .env
```

**Résultat attendu :** `MYSQL_DATABASE=iusjcdb`

---

### Étape 2 : Build Maven

#### Windows PowerShell
```powershell
# Build tous les services
mvn clean package -DskipTests

# Vérifier les JARs créés
Get-ChildItem -Recurse -Filter "*.jar" | Where-Object { $_.Directory.Name -eq "target" }
```

#### Linux/Mac
```bash
# Build tous les services
mvn clean package -DskipTests

# Vérifier les JARs créés
find . -name "*.jar" -path "*/target/*"
```

**Résultat attendu :** 14 fichiers JAR créés (un par service)

**Durée estimée :** 2-5 minutes

---

### Étape 3 : Démarrer l'Infrastructure

#### Windows PowerShell
```powershell
# Démarrer tous les services
docker compose up -d

# Vérifier les conteneurs
docker compose ps
```

#### Linux/Mac
```bash
# Démarrer tous les services
docker compose up -d

# Vérifier les conteneurs
docker compose ps
```

**Résultat attendu :** Tous les services avec status "Up"

**Durée estimée :** 1-2 minutes

---

### Étape 4 : Attendre le Démarrage Complet

```bash
# Attendre 30 secondes
# Windows PowerShell
Start-Sleep -Seconds 30

# Linux/Mac
sleep 30
```

**Important :** Les microservices prennent du temps à s'enregistrer dans Eureka

---

### Étape 5 : Vérifier Eureka Server

#### Navigateur
Ouvrir : http://localhost:8761

**Résultat attendu :**
- Dashboard Eureka visible
- Section "Instances currently registered with Eureka"
- **14 services** enregistrés (peut prendre jusqu'à 2 minutes)

#### Ligne de commande
```bash
# Windows PowerShell
Invoke-WebRequest http://localhost:8761 -UseBasicParsing

# Linux/Mac
curl http://localhost:8761
```

---

### Étape 6 : Vérifier MySQL

#### Windows PowerShell
```powershell
# Connexion MySQL
docker exec -it (docker ps -qf "name=mysql") mysql -uiusjc -piusjc2024 iusjcdb

# Dans MySQL
SHOW TABLES;
SELECT COUNT(*) FROM users;
SELECT username, role FROM users;
exit
```

#### Linux/Mac
```bash
# Connexion MySQL
docker exec -it $(docker ps -qf "name=mysql") mysql -uiusjc -piusjc2024 iusjcdb

# Dans MySQL
SHOW TABLES;
SELECT COUNT(*) FROM users;
SELECT username, role FROM users;
exit
```

**Résultats attendus :**
- 5 tables : users, roles, schedules, time_slots, notifications
- 3 utilisateurs : admin, teacher1, student1

---

### Étape 7 : Vérifier Redis

#### Windows PowerShell
```powershell
docker exec -it (docker ps -qf "name=redis") redis-cli PING
```

#### Linux/Mac
```bash
docker exec -it $(docker ps -qf "name=redis") redis-cli PING
```

**Résultat attendu :** `PONG`

---

### Étape 8 : Vérifier RabbitMQ

#### Navigateur
Ouvrir : http://localhost:15672
- Username : `iusjc`
- Password : `iusjc2024`

**Résultat attendu :**
- Dashboard RabbitMQ visible
- Onglet "Queues" accessible

#### Ligne de commande
```bash
# Windows PowerShell
docker exec (docker ps -qf "name=rabbitmq") rabbitmqctl status

# Linux/Mac
docker exec $(docker ps -qf "name=rabbitmq") rabbitmqctl status
```

---

### Étape 9 : Vérifier API Gateway

#### Navigateur
Ouvrir : http://localhost:8080/actuator/health

**Résultat attendu :**
```json
{"status":"UP"}
```

#### Ligne de commande
```bash
# Windows PowerShell
Invoke-WebRequest http://localhost:8080/actuator/health

# Linux/Mac
curl http://localhost:8080/actuator/health
```

---

### Étape 10 : Vérifier Frontend

#### Navigateur
Ouvrir : http://localhost:8090

**Résultat attendu :**
- Page "IUSJC Planning 2025" visible
- Design avec Tailwind CSS

---

### Étape 11 : Tester les API

#### Test user-service

```bash
# Windows PowerShell
Invoke-WebRequest http://localhost:8080/api/users -UseBasicParsing

# Linux/Mac
curl http://localhost:8080/api/users
```

**Note :** Peut retourner 404 si le service n'est pas encore complètement démarré

#### Test scheduling-service

```bash
# Windows PowerShell
Invoke-WebRequest http://localhost:8080/api/schedules -UseBasicParsing

# Linux/Mac
curl http://localhost:8080/api/schedules
```

#### Test notification-service

```bash
# Windows PowerShell
Invoke-WebRequest http://localhost:8080/api/notifications -UseBasicParsing

# Linux/Mac
curl http://localhost:8080/api/notifications
```

---

### Étape 12 : Vérifier les Logs

#### Windows PowerShell
```powershell
# Tous les services
docker compose logs

# Service spécifique
docker compose logs user-service
docker compose logs scheduling-service
docker compose logs notification-service

# Suivre les logs en temps réel
docker compose logs -f
```

#### Linux/Mac
```bash
# Tous les services
docker compose logs

# Service spécifique
docker compose logs user-service
docker compose logs scheduling-service
docker compose logs notification-service

# Suivre les logs en temps réel
docker compose logs -f
```

---

## ✅ Validation Finale

### Checklist Complète

- [ ] ✅ .env existe et est configuré
- [ ] ✅ Build Maven réussi (14 JARs)
- [ ] ✅ Docker Compose démarré
- [ ] ✅ Eureka accessible (http://localhost:8761)
- [ ] ✅ 14 services enregistrés dans Eureka
- [ ] ✅ MySQL accessible avec 5 tables
- [ ] ✅ 3 utilisateurs de test dans MySQL
- [ ] ✅ Redis répond au PING
- [ ] ✅ RabbitMQ Management accessible (http://localhost:15672)
- [ ] ✅ API Gateway accessible (http://localhost:8080)
- [ ] ✅ Frontend accessible (http://localhost:8090)
- [ ] ✅ Aucune erreur dans les logs

---

## 🐛 Dépannage

### Problème : Services ne démarrent pas

```bash
# Vérifier les logs
docker compose logs

# Redémarrer un service
docker compose restart [service-name]

# Redémarrer tout
docker compose down
docker compose up -d
```

### Problème : Port déjà utilisé

```bash
# Windows PowerShell
Get-NetTCPConnection -LocalPort 8080,8761,3306,6379,5672,15672

# Linux/Mac
lsof -i :8080,8761,3306,6379,5672,15672

# Arrêter le processus ou changer le port dans docker-compose.yml
```

### Problème : MySQL ne démarre pas

```bash
# Supprimer le volume et redémarrer
docker compose down -v
docker compose up mysql -d
```

### Problème : Services non enregistrés dans Eureka

```bash
# Attendre 2 minutes supplémentaires
# Vérifier les logs du service
docker compose logs [service-name]

# Vérifier la configuration Eureka
docker compose logs eureka-server
```

### Problème : Build Maven échoue

```bash
# Nettoyer et rebuilder
mvn clean
mvn package -DskipTests -X  # Mode debug

# Vérifier Java
java -version  # Doit être 17+
```

---

## 📊 Résultats Attendus

### Infrastructure (4 services)
- ✅ MySQL (port 3306)
- ✅ Redis (port 6379)
- ✅ RabbitMQ (ports 5672, 15672)
- ✅ Eureka Server (port 8761)

### Microservices (14 services)
1. ✅ eureka-server
2. ✅ api-gateway
3. ✅ user-service
4. ✅ scheduling-service
5. ✅ notification-service
6. ✅ school-service
7. ✅ resource-service
8. ✅ room-service
9. ✅ course-service
10. ✅ reservation-service
11. ✅ event-service
12. ✅ reporting-service
13. ✅ ent-integration-service
14. ✅ frontend-thymeleaf

### Base de Données
- ✅ 1 base : iusjcdb
- ✅ 5 tables
- ✅ 3 utilisateurs de test

---

## 🎯 Commandes Rapides

```bash
# Démarrer
docker compose up -d

# Arrêter
docker compose down

# Voir les logs
docker compose logs -f

# Redémarrer
docker compose restart

# Nettoyer tout
docker compose down -v

# Rebuild
docker compose up --build
```

---

## 📞 Support

Si vous rencontrez des problèmes :

1. Vérifier les logs : `docker compose logs`
2. Consulter `COMMANDES_RAPIDES.md`
3. Consulter `ARCHITECTURE_OPTIMISEE.md`
4. Vérifier la configuration dans `.env`

---

**🎉 Bon test !**
