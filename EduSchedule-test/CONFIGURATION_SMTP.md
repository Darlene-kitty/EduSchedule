# 📧 Configuration SMTP - EduSchedule

## 🎯 Vue d'Ensemble

Le service de notification utilise SMTP pour envoyer des emails. Cette configuration est essentielle pour:
- Notifications d'emploi du temps
- Alertes de changements
- Confirmations d'inscription
- Réinitialisation de mot de passe

## ✅ Configuration Actuelle

### Variables d'Environnement (.env)
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=jacky.kouang@saintjeaningenieur.org
MAIL_PASSWORD=uvkb ltoz uuhx tizv
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

### Configuration Spring (application.properties)
```properties
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 🔧 Configuration Gmail

### Option 1: Mot de Passe d'Application (Recommandé)

1. **Activer la validation en 2 étapes**
   - Allez sur: https://myaccount.google.com/security
   - Activez "Validation en 2 étapes"

2. **Créer un mot de passe d'application**
   - Allez sur: https://myaccount.google.com/apppasswords
   - Sélectionnez "Autre (nom personnalisé)"
   - Entrez "EduSchedule"
   - Copiez le mot de passe généré (16 caractères)

3. **Mettre à jour .env**
   ```bash
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=votre-email@gmail.com
   MAIL_PASSWORD=xxxx xxxx xxxx xxxx  # Mot de passe d'application
   ```

### Option 2: Accès Moins Sécurisé (Non Recommandé)

⚠️ **Attention:** Cette méthode est moins sécurisée et peut être désactivée par Google.

1. Allez sur: https://myaccount.google.com/lesssecureapps
2. Activez "Autoriser les applications moins sécurisées"
3. Utilisez votre mot de passe Gmail normal

## 🔧 Autres Fournisseurs SMTP

### Microsoft Outlook / Office 365
```bash
MAIL_HOST=smtp.office365.com
MAIL_PORT=587
MAIL_USERNAME=votre-email@outlook.com
MAIL_PASSWORD=votre-mot-de-passe
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

### Yahoo Mail
```bash
MAIL_HOST=smtp.mail.yahoo.com
MAIL_PORT=587
MAIL_USERNAME=votre-email@yahoo.com
MAIL_PASSWORD=mot-de-passe-application
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

### SendGrid
```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=votre-api-key-sendgrid
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

### Mailgun
```bash
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=postmaster@votre-domaine.mailgun.org
MAIL_PASSWORD=votre-mot-de-passe-mailgun
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

### SMTP Local (pour tests)
```bash
# Utiliser MailHog pour les tests locaux
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS_ENABLE=false
```

## 🧪 Test de Configuration SMTP

### Méthode 1: Via l'API

```bash
# Créer une notification email
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "recipient": "destinataire@example.com",
    "subject": "Test Email EduSchedule",
    "message": "Ceci est un email de test",
    "type": "EMAIL"
  }'
```

### Méthode 2: Via le Service

Créez un fichier de test: `notification-service/src/test/java/cm/iusjc/notification/EmailTest.java`

```java
@SpringBootTest
class EmailTest {
    
    @Autowired
    private EmailService emailService;
    
    @Test
    void testSendEmail() {
        emailService.sendEmail(
            "destinataire@example.com",
            "Test Email",
            "Ceci est un email de test depuis EduSchedule"
        );
    }
}
```

### Méthode 3: Logs

Vérifiez les logs du notification-service:
```bash
# Rechercher les logs d'envoi d'email
tail -f logs/notification-service.log | grep "Email sent"
```

## 🐛 Résolution de Problèmes

### Erreur: "Authentication failed"

**Cause:** Identifiants incorrects ou accès bloqué

**Solutions:**
1. Vérifiez MAIL_USERNAME et MAIL_PASSWORD
2. Pour Gmail: Utilisez un mot de passe d'application
3. Vérifiez que la validation en 2 étapes est activée
4. Vérifiez les paramètres de sécurité du compte

### Erreur: "Connection timeout"

**Cause:** Port bloqué ou serveur SMTP inaccessible

**Solutions:**
1. Vérifiez le MAIL_HOST et MAIL_PORT
2. Vérifiez votre firewall
3. Testez la connexion:
   ```bash
   telnet smtp.gmail.com 587
   ```

### Erreur: "Could not connect to SMTP host"

**Cause:** Configuration réseau ou proxy

**Solutions:**
1. Vérifiez votre connexion Internet
2. Vérifiez les paramètres proxy
3. Essayez un autre port (465 pour SSL)

### Erreur: "Mail server connection failed"

**Cause:** Configuration SSL/TLS incorrecte

**Solutions:**
1. Pour port 587: Utilisez STARTTLS
   ```bash
   MAIL_SMTP_STARTTLS_ENABLE=true
   ```
2. Pour port 465: Utilisez SSL
   ```bash
   MAIL_PORT=465
   MAIL_SMTP_SSL_ENABLE=true
   ```

### Emails non reçus

**Vérifications:**
1. Vérifiez le dossier spam/courrier indésirable
2. Vérifiez les logs du service:
   ```bash
   grep "Email sent" logs/notification-service.log
   ```
3. Vérifiez le statut dans la base de données:
   ```sql
   SELECT * FROM notifications WHERE status = 'FAILED';
   ```

## 📊 Monitoring SMTP

### Vérifier les Notifications Envoyées

```sql
-- Notifications réussies
SELECT COUNT(*) FROM notifications 
WHERE type = 'EMAIL' AND status = 'SENT';

-- Notifications échouées
SELECT * FROM notifications 
WHERE type = 'EMAIL' AND status = 'FAILED'
ORDER BY created_at DESC;

-- Statistiques par jour
SELECT DATE(created_at) as date, 
       COUNT(*) as total,
       SUM(CASE WHEN status = 'SENT' THEN 1 ELSE 0 END) as sent,
       SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed
FROM notifications
WHERE type = 'EMAIL'
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

### Logs à Surveiller

```bash
# Succès
grep "Email sent to:" logs/notification-service.log

# Échecs
grep "Failed to send email" logs/notification-service.log

# Erreurs SMTP
grep "SMTPException" logs/notification-service.log
```

## 🔒 Sécurité

### Bonnes Pratiques

1. **Ne jamais commiter les credentials**
   ```bash
   # Ajoutez .env au .gitignore
   echo ".env" >> .gitignore
   ```

2. **Utiliser des variables d'environnement**
   ```bash
   # En production, définissez les variables système
   export MAIL_USERNAME=votre-email@gmail.com
   export MAIL_PASSWORD=votre-mot-de-passe-app
   ```

3. **Utiliser des mots de passe d'application**
   - Plus sécurisé que le mot de passe principal
   - Peut être révoqué sans changer le mot de passe principal

4. **Limiter les permissions**
   - Utilisez un compte dédié pour l'envoi d'emails
   - Ne donnez que les permissions nécessaires

5. **Chiffrer les communications**
   - Toujours utiliser TLS/SSL
   - Port 587 avec STARTTLS ou port 465 avec SSL

## 📝 Configuration pour Production

### Variables d'Environnement Système

```bash
# Linux/Mac
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=noreply@iusjc.cm
export MAIL_PASSWORD=votre-mot-de-passe-app

# Windows
setx MAIL_HOST "smtp.gmail.com"
setx MAIL_PORT "587"
setx MAIL_USERNAME "noreply@iusjc.cm"
setx MAIL_PASSWORD "votre-mot-de-passe-app"
```

### Docker

```yaml
# docker-compose.yml
notification-service:
  environment:
    - MAIL_HOST=smtp.gmail.com
    - MAIL_PORT=587
    - MAIL_USERNAME=${MAIL_USERNAME}
    - MAIL_PASSWORD=${MAIL_PASSWORD}
```

### Kubernetes

```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: smtp-credentials
type: Opaque
stringData:
  mail-username: noreply@iusjc.cm
  mail-password: votre-mot-de-passe-app

---
# deployment.yaml
env:
  - name: MAIL_USERNAME
    valueFrom:
      secretKeyRef:
        name: smtp-credentials
        key: mail-username
  - name: MAIL_PASSWORD
    valueFrom:
      secretKeyRef:
        name: smtp-credentials
        key: mail-password
```

## 🧪 Test avec MailHog (Développement)

MailHog est un serveur SMTP de test qui capture les emails sans les envoyer.

### Installation

```bash
# Docker
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Ou télécharger depuis: https://github.com/mailhog/MailHog/releases
```

### Configuration

```bash
# .env.local pour développement
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS_ENABLE=false
```

### Interface Web

Accédez à: http://localhost:8025

Tous les emails envoyés seront visibles dans l'interface.

## ✅ Checklist de Configuration

- [ ] Variables SMTP configurées dans .env
- [ ] Mot de passe d'application créé (Gmail)
- [ ] Validation en 2 étapes activée (Gmail)
- [ ] Test d'envoi d'email réussi
- [ ] Logs vérifiés (pas d'erreurs)
- [ ] Emails reçus (vérifier spam)
- [ ] Configuration sécurisée (pas de credentials dans le code)
- [ ] MailHog configuré pour développement (optionnel)

## 📚 Ressources

- [Spring Boot Mail](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)
- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
- [MailHog](https://github.com/mailhog/MailHog)
- [SendGrid Documentation](https://docs.sendgrid.com/)
- [Mailgun Documentation](https://documentation.mailgun.com/)

## 🆘 Support

En cas de problème:
1. Vérifiez les logs: `logs/notification-service.log`
2. Testez la connexion SMTP: `telnet smtp.gmail.com 587`
3. Vérifiez la base de données: `SELECT * FROM notifications WHERE status = 'FAILED'`
4. Consultez la documentation du fournisseur SMTP
