# 🔧 Configuration des Variables d'Environnement

## 📋 Vue d'ensemble

Le projet utilise un fichier `.env` pour centraliser toutes les variables d'environnement. Cela permet de :
- ✅ Gérer facilement la configuration
- ✅ Séparer les secrets du code
- ✅ Faciliter le déploiement
- ✅ Éviter les valeurs en dur dans le code

## 🚀 Installation

### 1. Copier le fichier exemple

```bash
cp .env.example .env
```

### 2. Modifier les valeurs

Ouvrir `.env` et ajuster les valeurs selon votre environnement.

## 📝 Variables Disponibles

### MySQL Configuration

```env
MYSQL_ROOT_PASSWORD=root              # Mot de passe root MySQL
MYSQL_DATABASE=iusjcdb                # Nom de la base de données
MYSQL_USER=iusjc                      # Utilisateur MySQL
MYSQL_PASSWORD=iusjc2024              # Mot de passe utilisateur
MYSQL_HOST=mysql                      # Hôte MySQL (nom du service Docker)
MYSQL_PORT=3306                       # Port MySQL
```

**Utilisation :**
- Services : user-service, scheduling-service, notification-service
- Docker Compose : service mysql

### Redis Configuration

```env
REDIS_HOST=redis                      # Hôte Redis
REDIS_PORT=6379                       # Port Redis
```

**Utilisation :**
- Services : scheduling-service
- Docker Compose : service redis

### RabbitMQ Configuration

```env
RABBITMQ_HOST=rabbitmq                # Hôte RabbitMQ
RABBITMQ_PORT=5672                    # Port AMQP
RABBITMQ_MANAGEMENT_PORT=15672        # Port Management UI
RABBITMQ_DEFAULT_USER=iusjc           # Utilisateur RabbitMQ
RABBITMQ_DEFAULT_PASS=iusjc2024       # Mot de passe RabbitMQ
```

**Utilisation :**
- Services : scheduling-service, notification-service
- Docker Compose : service rabbitmq
- Management UI : http://localhost:15672

### Eureka Configuration

```env
EUREKA_SERVER_HOST=eureka-server      # Hôte Eureka
EUREKA_SERVER_PORT=8761               # Port Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

**Utilisation :**
- Tous les microservices
- Dashboard : http://localhost:8761

### API Gateway Configuration

```env
API_GATEWAY_PORT=8080                 # Port API Gateway
```

**Utilisation :**
- Service : api-gateway
- URL : http://localhost:8080

### Frontend Configuration

```env
FRONTEND_PORT=8090                    # Port Frontend
```

**Utilisation :**
- Service : frontend-thymeleaf
- URL : http://localhost:8090

### Spring DataSource Configuration

```env
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/iusjcdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=iusjc
SPRING_DATASOURCE_PASSWORD=iusjc2024
SPRING_DATASOURCE_DRIVER=com.mysql.cj.jdbc.Driver
```

**Utilisation :**
- Services avec base de données (user, scheduling, notification)

### JPA/Hibernate Configuration

```env
SPRING_JPA_HIBERNATE_DDL_AUTO=validate    # validate, update, create, create-drop
SPRING_JPA_SHOW_SQL=true                  # Afficher les requêtes SQL
SPRING_JPA_DIALECT=org.hibernate.dialect.MySQLDialect
```

**Modes DDL-AUTO :**
- `validate` : Valide le schéma (PRODUCTION)
- `update` : Met à jour le schéma (DÉVELOPPEMENT)
- `create` : Crée le schéma à chaque démarrage
- `create-drop` : Crée et supprime le schéma

### JWT Configuration

```env
JWT_SECRET=your-secret-key-here       # Clé secrète JWT (256 bits minimum)
JWT_EXPIRATION=86400000               # Durée de validité en ms (24h)
```

**⚠️ IMPORTANT :**
- Générer une clé forte pour la production
- Ne jamais commiter la vraie clé dans Git
- Utiliser au minimum 256 bits

**Générer une clé :**
```bash
# Linux/Mac
openssl rand -base64 64

# Windows PowerShell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

### Mail Configuration

```env
MAIL_HOST=smtp.gmail.com              # Serveur SMTP
MAIL_PORT=587                         # Port SMTP
MAIL_USERNAME=your-email@gmail.com    # Email
MAIL_PASSWORD=your-app-password       # Mot de passe d'application
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

**Configuration Gmail :**
1. Activer l'authentification à 2 facteurs
2. Générer un mot de passe d'application
3. Utiliser ce mot de passe dans MAIL_PASSWORD

**Autres fournisseurs :**
- **Outlook :** smtp-mail.outlook.com:587
- **Yahoo :** smtp.mail.yahoo.com:587
- **SendGrid :** smtp.sendgrid.net:587

### Redis Cache Configuration

```env
SPRING_CACHE_TYPE=redis               # Type de cache
REDIS_TTL=3600                        # Durée de vie en secondes (1h)
```

### RabbitMQ Queue Configuration

```env
NOTIFICATION_QUEUE_NAME=notification-queue
NOTIFICATION_EXCHANGE_NAME=notification-exchange
NOTIFICATION_ROUTING_KEY=notification.#
SCHEDULE_QUEUE_NAME=schedule-notifications
SCHEDULE_EXCHANGE_NAME=schedule-exchange
SCHEDULE_ROUTING_KEY=schedule.#
```

### Logging Configuration

```env
LOGGING_LEVEL_ROOT=INFO               # Niveau de log global
LOGGING_LEVEL_CM_IUSJC=DEBUG          # Niveau de log pour le projet
```

**Niveaux disponibles :**
- `TRACE` : Très détaillé
- `DEBUG` : Débogage
- `INFO` : Informations
- `WARN` : Avertissements
- `ERROR` : Erreurs

### Actuator Configuration

```env
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
```

**Endpoints disponibles :**
- `health` : État de santé
- `info` : Informations
- `metrics` : Métriques
- `env` : Variables d'environnement
- `loggers` : Configuration des logs

### Docker Configuration

```env
DOCKER_REGISTRY=darlene4              # Registry Docker Hub
IMAGE_TAG=latest                      # Tag des images
```

### ENT Integration Configuration

```env
ENT_API_BASE_URL=http://localhost:9000    # URL de l'API ENT
ENT_API_TIMEOUT=30000                     # Timeout en ms
```

### Environment

```env
SPRING_PROFILES_ACTIVE=dev            # Profil Spring (dev, prod, test)
```

## 🔒 Sécurité

### Bonnes Pratiques

1. **Ne jamais commiter `.env`**
   ```bash
   # Vérifier que .env est dans .gitignore
   cat .gitignore | grep .env
   ```

2. **Utiliser `.env.example`**
   - Commiter `.env.example` avec des valeurs par défaut
   - Ne pas mettre de secrets dans `.env.example`

3. **Générer des secrets forts**
   ```bash
   # JWT Secret
   openssl rand -base64 64
   
   # Mot de passe MySQL
   openssl rand -base64 32
   ```

4. **Différencier les environnements**
   - `.env` : Développement local
   - `.env.production` : Production
   - `.env.test` : Tests

### Variables Sensibles

⚠️ **Ne jamais exposer publiquement :**
- `MYSQL_ROOT_PASSWORD`
- `MYSQL_PASSWORD`
- `JWT_SECRET`
- `MAIL_PASSWORD`
- `RABBITMQ_DEFAULT_PASS`

## 🐳 Utilisation avec Docker Compose

Docker Compose charge automatiquement le fichier `.env` :

```yaml
services:
  mysql:
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
```

### Vérifier les variables

```bash
# Afficher les variables chargées
docker-compose config

# Tester une variable spécifique
echo $MYSQL_DATABASE
```

## 🧪 Tests

### Vérifier la configuration

```bash
# 1. Vérifier que .env existe
test -f .env && echo "✅ .env existe" || echo "❌ .env manquant"

# 2. Vérifier les variables MySQL
docker-compose exec mysql mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} -e "SHOW DATABASES;"

# 3. Vérifier Redis
docker-compose exec redis redis-cli PING

# 4. Vérifier RabbitMQ
curl -u ${RABBITMQ_DEFAULT_USER}:${RABBITMQ_DEFAULT_PASS} http://localhost:15672/api/overview
```

## 📊 Environnements

### Développement Local

```env
SPRING_PROFILES_ACTIVE=dev
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
LOGGING_LEVEL_CM_IUSJC=DEBUG
```

### Production

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
LOGGING_LEVEL_CM_IUSJC=INFO
```

### Tests

```env
SPRING_PROFILES_ACTIVE=test
SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop
MYSQL_DATABASE=iusjcdb_test
```

## 🔄 Migration

### Depuis les valeurs en dur

Si vous aviez des valeurs en dur dans `application.properties` :

**Avant :**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/iusjcdb
spring.datasource.username=iusjc
spring.datasource.password=iusjc2024
```

**Après :**
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

## 🆘 Dépannage

### Problème : Variables non chargées

```bash
# Solution 1 : Redémarrer Docker Compose
docker-compose down
docker-compose up -d

# Solution 2 : Forcer le rechargement
docker-compose --env-file .env up -d
```

### Problème : Valeur incorrecte

```bash
# Vérifier la valeur chargée
docker-compose config | grep MYSQL_DATABASE

# Vérifier dans le conteneur
docker-compose exec user-service env | grep SPRING_DATASOURCE
```

### Problème : .env non trouvé

```bash
# Vérifier l'emplacement
ls -la .env

# Créer depuis l'exemple
cp .env.example .env
```

## 📚 Ressources

- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [Spring Boot External Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [12 Factor App - Config](https://12factor.net/config)

## ✅ Checklist

- [ ] Copier `.env.example` vers `.env`
- [ ] Modifier les valeurs sensibles
- [ ] Vérifier que `.env` est dans `.gitignore`
- [ ] Générer un JWT_SECRET fort
- [ ] Configurer les identifiants SMTP
- [ ] Tester le démarrage avec `docker-compose up`
- [ ] Vérifier les connexions aux services

---

**Note :** Ce fichier `.env` est pour le développement local. En production, utiliser des secrets managers (AWS Secrets Manager, Azure Key Vault, etc.)
