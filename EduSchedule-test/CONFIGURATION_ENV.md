# Configuration des Variables d'Environnement (.env)

## 📋 Vue d'ensemble

Ce projet utilise un fichier `.env` pour centraliser toutes les variables d'environnement. Cette approche permet de :

- ✅ Centraliser la configuration
- ✅ Éviter les valeurs codées en dur
- ✅ Faciliter le déploiement
- ✅ Sécuriser les informations sensibles

## 🔧 Configuration mise en place

### 1. Dépendance ajoutée

```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 2. Chargement automatique

Le fichier `.env` est chargé automatiquement au démarrage de chaque service Spring Boot grâce à :

- **Classe principale modifiée** : `UserServiceApplication.java`
- **Chargement dans le bloc static** : Avant le démarrage de Spring Boot
- **Configuration globale** : `DotEnvConfig.java` (optionnel)

### 3. Utilisation dans application.properties

```properties
# Syntaxe : ${VARIABLE_NAME:valeur_par_defaut}
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/iusjcdb}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:iusjc}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:iusjc2025}
jwt.secret=${JWT_SECRET:default-secret-key}
```

## 📁 Structure des fichiers

```
EduSchedule/
├── .env                    # Variables d'environnement (NE PAS COMMITER)
├── .env.example           # Exemple de configuration
├── user-service/
│   └── src/main/java/cm/iusjc/userservice/
│       └── UserServiceApplication.java  # Chargement du .env
└── src/main/java/cm/iusjc/config/
    ├── DotEnvConfig.java          # Configuration globale
    └── DevelopmentConfig.java     # Affichage des variables en dev
```

## 🚀 Comment utiliser

### 1. Créer le fichier .env

```bash
# Copier l'exemple
cp .env.example .env

# Éditer avec vos valeurs
notepad .env
```

### 2. Configurer les variables importantes

```env
# Base de données
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/iusjcdb
SPRING_DATASOURCE_USERNAME=iusjc
SPRING_DATASOURCE_PASSWORD=votre_mot_de_passe

# JWT
JWT_SECRET=votre_cle_secrete_jwt_forte

# Email
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=votre_email@gmail.com
MAIL_PASSWORD=votre_mot_de_passe_app

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
```

### 3. Démarrer les services

```bash
# Option 1: Script personnalisé
./test-user-service.bat

# Option 2: Maven classique
cd user-service
mvn spring-boot:run

# Option 3: Avec profil de développement
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🔍 Vérification du chargement

Quand vous démarrez un service, vous devriez voir :

```
✅ [USER-SERVICE] Fichier .env chargé (45 variables)
```

En mode développement, vous verrez aussi :

```
============================================================
🔧 MODE DÉVELOPPEMENT - Variables d'environnement
============================================================
  URL de la base de données        : jdbc:mysql://localhost:3306/iusjcdb
  Utilisateur DB                   : iusjc
  Clé JWT                          : your***here
  Serveur SMTP                     : smtp.gmail.com
  Email SMTP                       : votre_email@gmail.com
  Eureka Server                    : http://localhost:8761/eureka/
  Profil actif                     : dev
============================================================
✅ Application prête !
============================================================
```

## 🛠️ Scripts disponibles

- `test-user-service.bat` : Test rapide du user-service
- `load-env-and-start.bat` : Chargement manuel des variables + démarrage

## ⚠️ Sécurité

- **JAMAIS** commiter le fichier `.env` dans Git
- Utiliser `.env.example` pour documenter les variables nécessaires
- Utiliser des valeurs par défaut sécurisées dans `application.properties`
- Changer les clés secrètes en production

## 🐛 Dépannage

### Problème : Variables non chargées

1. Vérifier que le fichier `.env` existe à la racine
2. Vérifier la syntaxe (pas d'espaces autour du `=`)
3. Redémarrer le service
4. Vérifier les logs de démarrage

### Problème : Erreur de compilation

1. Vérifier que la dépendance `dotenv-java` est présente
2. Faire un `mvn clean install`
3. Vérifier la version Java (17 requis)

### Problème : Service ne démarre pas

1. Vérifier la base de données MySQL
2. Vérifier les ports (8081 pour user-service)
3. Vérifier Eureka Server (port 8761)

## 📚 Variables importantes

| Variable | Description | Exemple |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | URL de la base de données | `jdbc:mysql://localhost:3306/iusjcdb` |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur DB | `iusjc` |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe DB | `iusjc2025` |
| `JWT_SECRET` | Clé secrète JWT | `your-secret-key-here` |
| `MAIL_HOST` | Serveur SMTP | `smtp.gmail.com` |
| `MAIL_USERNAME` | Email SMTP | `votre@email.com` |
| `MAIL_PASSWORD` | Mot de passe email | `mot_de_passe_app` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | URL Eureka | `http://localhost:8761/eureka/` |

## ✅ Avantages de cette approche

1. **Centralisation** : Toutes les variables au même endroit
2. **Flexibilité** : Facile de changer entre environnements
3. **Sécurité** : Pas de secrets dans le code
4. **Simplicité** : Syntaxe standard `.env`
5. **Compatibilité** : Fonctionne avec Docker, Kubernetes, etc.

Cette configuration vous permet de gérer facilement tous vos environnements (dev, test, prod) avec un seul fichier de configuration !