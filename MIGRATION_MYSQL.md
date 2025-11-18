# Migration PostgreSQL vers MySQL

## Changements effectués

### 1. Docker Compose
- Remplacement du service `postgres` par `mysql:8.0`
- Mise à jour de toutes les URLs JDBC pour utiliser MySQL
- Format: `jdbc:mysql://mysql:3306/[database]?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC`

### 2. Configuration des services
- Conversion de tous les fichiers `application.yml` en `application.properties`
- Mise à jour du driver JDBC: `com.mysql.cj.jdbc.Driver`
- Mise à jour du dialect Hibernate: `org.hibernate.dialect.MySQLDialect`

### 3. Dépendances Maven
- Remplacement de `org.postgresql:postgresql` par `com.mysql:mysql-connector-j`
- Mise à jour dans les POM files:
  - user-service
  - school-service
  - resource-service
  - (et tous les autres services avec base de données)

### 4. Script d'initialisation
- Mise à jour de `init-db.sql` avec la syntaxe MySQL
- Ajout des permissions pour l'utilisateur `iusjc`

## Services concernés
- user-service (authdb)
- school-service (schooldb)
- resource-service (resourcedb)
- course-service (coursedb)
- scheduling-service (schedulingdb)
- reservation-service (reservationdb)
- reporting-service (reportingdb)

## Configuration MySQL

### Connexion locale
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/[database]?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=iusjc
spring.datasource.password=iusjc2024
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### Connexion Docker
Les variables d'environnement dans docker-compose.yml gèrent automatiquement la configuration.

## Commandes utiles

### Démarrer MySQL seul
```bash
docker-compose up mysql
```

### Accéder à MySQL
```bash
docker exec -it [container_name] mysql -u iusjc -piusjc2024
```

### Lister les bases de données
```sql
SHOW DATABASES;
```

### Vérifier les tables d'une base
```sql
USE authdb;
SHOW TABLES;
```

## Notes importantes
- MySQL crée automatiquement les bases de données grâce au paramètre `createDatabaseIfNotExist=true`
- Le timezone est défini sur UTC pour éviter les problèmes de conversion de dates
- SSL est désactivé pour simplifier la configuration en développement
