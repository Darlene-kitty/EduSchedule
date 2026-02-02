# Système d'Email de Bienvenue

## Vue d'ensemble

Le système d'email de bienvenue envoie automatiquement des emails personnalisés aux nouveaux utilisateurs lors de leur création, avec des templates différents selon leur rôle.

## Fonctionnalités

### Types d'emails de bienvenue

1. **Email standard** (STUDENT) - `welcome-email.html`
   - Message de bienvenue général
   - Présentation des fonctionnalités de base
   - Lien de connexion

2. **Email enseignant** (TEACHER) - `teacher-welcome-email.html`
   - Message spécialisé pour les enseignants
   - Fonctionnalités pédagogiques
   - Lien vers la gestion des disponibilités
   - Instructions pour définir les créneaux

3. **Email administrateur** (ADMIN) - `admin-welcome-email.html`
   - Message pour les administrateurs
   - Privilèges et responsabilités
   - Lien vers le panneau d'administration
   - Notes de sécurité

### Personnalisation

Chaque email inclut :
- Nom et prénom de l'utilisateur
- Rôle spécifique
- Liens directs vers les fonctionnalités pertinentes
- Design responsive et professionnel

## Configuration

### 1. Configuration SMTP

Dans le fichier `.env` :

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre.email@gmail.com
MAIL_PASSWORD=mot_de_passe_application
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

### 2. Configuration Gmail

1. Activez la validation en 2 étapes
2. Générez un mot de passe d'application :
   - Allez sur https://myaccount.google.com/security
   - Cliquez sur "Mots de passe d'application"
   - Générez un mot de passe pour "Mail"
3. Utilisez ce mot de passe dans `MAIL_PASSWORD`

### 3. Variables d'application

Dans `application.properties` :

```properties
app.name=EduSchedule
app.frontend.url=http://localhost:3000
spring.mail.username=${MAIL_USERNAME:}
```

## Structure technique

### Services

- **WelcomeEmailService** : Service principal pour l'envoi d'emails
  - `sendWelcomeEmail(User user)` : Email standard
  - `sendTeacherWelcomeEmail(User user)` : Email enseignant
  - `sendAdminWelcomeEmail(User user)` : Email administrateur

### Templates Thymeleaf

Localisation : `user-service/src/main/resources/templates/`

- `welcome-email.html` : Template standard
- `teacher-welcome-email.html` : Template enseignant
- `admin-welcome-email.html` : Template administrateur

### Intégration

L'envoi d'email est intégré dans `UserService.createUser()` :

```java
// Envoyer l'email de bienvenue approprié selon le rôle
switch (savedUser.getRole().toUpperCase()) {
    case "TEACHER":
        welcomeEmailService.sendTeacherWelcomeEmail(savedUser);
        break;
    case "ADMIN":
        welcomeEmailService.sendAdminWelcomeEmail(savedUser);
        break;
    default:
        welcomeEmailService.sendWelcomeEmail(savedUser);
        break;
}
```

## Base de données

### Nouvelles colonnes

La table `users` a été étendue avec :

```sql
ALTER TABLE users 
ADD COLUMN first_name VARCHAR(50) NULL,
ADD COLUMN last_name VARCHAR(50) NULL;
```

### Migration

Utilisez le script `update-user-table-welcome.sql` pour mettre à jour la base existante.

## Tests

### Script de configuration

```bash
.\configure-welcome-email.ps1
```

Configure automatiquement :
- Variables SMTP
- Vérification des templates
- Instructions Gmail

### Script de test

```bash
.\test-welcome-email.ps1
```

Teste :
- Création d'utilisateurs avec différents rôles
- Envoi automatique d'emails
- Vérification de la configuration

## Dépendances Maven

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

## Gestion des erreurs

- Les erreurs d'envoi d'email n'interrompent pas la création d'utilisateur
- Logs détaillés pour le débogage
- Gestion gracieuse des échecs SMTP

## Sécurité

- Utilisation de mots de passe d'application pour Gmail
- Chiffrement TLS pour les communications SMTP
- Validation des adresses email
- Pas d'exposition des mots de passe dans les logs

## Personnalisation avancée

### Ajouter un nouveau type d'email

1. Créer un nouveau template HTML dans `templates/`
2. Ajouter une méthode dans `WelcomeEmailService`
3. Mettre à jour la logique dans `UserService.createUser()`

### Modifier les templates

Les templates utilisent Thymeleaf avec les variables :
- `${user.firstName}` et `${user.lastName}`
- `${user.email}` et `${user.role}`
- `${appName}` et `${frontendUrl}`
- `${loginUrl}`, `${availabilityUrl}`, `${adminUrl}`

## Monitoring

### Logs

Les logs incluent :
- Succès/échec d'envoi d'email
- Type d'email envoyé
- Utilisateur concerné

### Métriques

Surveillez :
- Taux de succès d'envoi d'emails
- Temps de réponse SMTP
- Erreurs de configuration

## Dépannage

### Problèmes courants

1. **Email non reçu**
   - Vérifiez les spams
   - Validez la configuration SMTP
   - Consultez les logs du service

2. **Erreur d'authentification**
   - Utilisez un mot de passe d'application
   - Vérifiez la validation en 2 étapes

3. **Template non trouvé**
   - Vérifiez l'emplacement des fichiers HTML
   - Redémarrez le service après modification

### Commandes de diagnostic

```bash
# Vérifier la configuration
.\diagnose-api-connectivity.ps1

# Tester l'envoi d'email
.\test-welcome-email.ps1

# Vérifier les logs
docker logs user-service
```

## Évolutions futures

- Support multilingue des templates
- Templates personnalisables par établissement
- Statistiques d'ouverture d'emails
- Intégration avec des services d'emailing tiers
- Templates pour d'autres événements (réinitialisation mot de passe, etc.)