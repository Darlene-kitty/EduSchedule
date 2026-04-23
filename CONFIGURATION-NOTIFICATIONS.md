# 📧 Configuration des Notifications — EduSchedule

Ce guide explique comment configurer l'envoi d'emails (SMTP) et de SMS (Twilio) pour le système de notifications.

---

## 📬 Configuration SMTP (Emails)

### Option 1 : Gmail (Recommandé pour le développement)

#### Étape 1 : Activer la validation en 2 étapes
1. Allez sur https://myaccount.google.com/security
2. Cliquez sur **Validation en 2 étapes**
3. Suivez les instructions pour l'activer (si ce n'est pas déjà fait)

#### Étape 2 : Générer un mot de passe d'application
1. Allez sur https://myaccount.google.com/apppasswords
2. Dans "Sélectionner l'application", choisissez **Autre (nom personnalisé)**
3. Tapez `EduSchedule` et cliquez sur **Générer**
4. Google affiche un mot de passe à 16 caractères (ex: `abcd efgh ijkl mnop`)
5. **Copiez ce mot de passe** (vous ne pourrez plus le revoir)

#### Étape 3 : Mettre à jour le fichier `.env`
Ouvrez le fichier `.env` à la racine du projet et renseignez :

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre.email@gmail.com
MAIL_PASSWORD=abcdefghijklmnop
MAIL_FROM=noreply@iusjc.cm
```

⚠️ **Important** : Collez le mot de passe **SANS espaces** (enlevez les espaces entre les groupes de 4 lettres).

#### Étape 4 : Redémarrer le service
```bash
# Arrêter le notification-service
docker-compose stop notification-service

# Redémarrer avec les nouvelles variables
docker-compose up -d notification-service

# Vérifier les logs
docker-compose logs -f notification-service
```

Vous devriez voir dans les logs :
```
Email service configured with: votre.email@gmail.com
```

---

### Option 2 : Autre fournisseur SMTP

Pour utiliser un autre fournisseur (Outlook, SendGrid, Mailgun, etc.), modifiez dans `.env` :

#### Outlook / Office 365
```env
MAIL_HOST=smtp.office365.com
MAIL_PORT=587
MAIL_USERNAME=votre.email@outlook.com
MAIL_PASSWORD=votre_mot_de_passe
```

#### SendGrid
```env
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=votre_api_key_sendgrid
```

#### Mailgun
```env
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=postmaster@votre-domaine.mailgun.org
MAIL_PASSWORD=votre_mot_de_passe_mailgun
```

---

## 📱 Configuration SMS (Twilio)

### Étape 1 : Créer un compte Twilio
1. Allez sur https://www.twilio.com/try-twilio
2. Créez un compte (essai gratuit avec crédit de $15)
3. Vérifiez votre email et votre numéro de téléphone

### Étape 2 : Récupérer les identifiants
1. Connectez-vous à la [Console Twilio](https://console.twilio.com/)
2. Sur le **Dashboard**, notez :
   - **Account SID** : commence par `AC...` (ex: `ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`)
   - **Auth Token** : cliquez sur "Show" pour le révéler

### Étape 3 : Obtenir un numéro Twilio
1. Dans la console, allez dans **Phone Numbers → Manage → Buy a number**
2. Choisissez un pays (ex: États-Unis pour les tests)
3. Sélectionnez un numéro avec capacité **SMS**
4. Achetez le numéro (gratuit avec le crédit d'essai)
5. Notez le numéro au format international (ex: `+15551234567`)

### Étape 4 : Mettre à jour le fichier `.env`
```env
SMS_TWILIO_ENABLED=true
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=votre_auth_token_ici
TWILIO_FROM_NUMBER=+15551234567
TWILIO_SENDER_ID=EduSchedule
```

### Étape 5 : Redémarrer le service
```bash
docker-compose stop notification-service
docker-compose up -d notification-service
docker-compose logs -f notification-service
```

Vous devriez voir :
```
Twilio SMS service initialized (from: +15551234567)
```

---

## 🧪 Tester les notifications

### Test Email
Créez un fichier `test-email.sh` :
```bash
curl -X POST http://localhost:8087/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "destinataire@example.com",
    "subject": "Test EduSchedule",
    "message": "Ceci est un email de test.",
    "type": "EMAIL"
  }'
```

Exécutez :
```bash
bash test-email.sh
```

### Test SMS
Créez un fichier `test-sms.sh` :
```bash
curl -X POST http://localhost:8087/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "+237600000000",
    "subject": "Test SMS",
    "message": "Ceci est un SMS de test depuis EduSchedule.",
    "type": "SMS"
  }'
```

⚠️ **Compte Twilio d'essai** : vous ne pouvez envoyer des SMS qu'aux numéros vérifiés dans votre compte. Pour envoyer à n'importe quel numéro, vous devez passer à un compte payant.

---

## 🔍 Dépannage

### Emails ne partent pas

**Erreur : `Authentication failed`**
- Vérifiez que vous utilisez un **mot de passe d'application**, pas votre mot de passe Gmail normal
- Vérifiez que la validation en 2 étapes est activée
- Vérifiez qu'il n'y a **pas d'espaces** dans le mot de passe

**Erreur : `Could not connect to SMTP host`**
- Vérifiez votre connexion internet
- Vérifiez que le port 587 n'est pas bloqué par un pare-feu
- Essayez le port 465 avec SSL : `MAIL_PORT=465` et ajoutez `spring.mail.properties.mail.smtp.ssl.enable=true`

**Emails arrivent dans les spams**
- Configurez un domaine personnalisé avec SPF/DKIM (production)
- Utilisez un service professionnel (SendGrid, Mailgun) pour la production

### SMS ne partent pas

**Erreur : `Unable to create record: Authenticate`**
- Vérifiez que `TWILIO_ACCOUNT_SID` et `TWILIO_AUTH_TOKEN` sont corrects
- Vérifiez qu'il n'y a pas d'espaces avant/après les valeurs

**Erreur : `The 'To' number +237... is not a valid phone number`**
- Vérifiez le format international : `+237XXXXXXXXX` (9 chiffres après +237 pour le Cameroun)
- Avec un compte d'essai, le numéro doit être vérifié dans la console Twilio

**SMS n'arrivent pas**
- Vérifiez que le numéro Twilio a la capacité SMS activée
- Vérifiez le solde de votre compte Twilio
- Consultez les logs Twilio : https://console.twilio.com/monitor/logs/sms

---

## 💰 Coûts

### Gmail
- **Gratuit** pour un usage raisonnable (limite : ~500 emails/jour)
- Pour plus, utilisez Google Workspace ou un service dédié

### Twilio
- **Essai gratuit** : $15 de crédit
- **SMS sortants** : ~$0.0075 par SMS (États-Unis), ~$0.05 par SMS (Cameroun)
- **Numéro Twilio** : ~$1/mois
- Consultez les tarifs : https://www.twilio.com/sms/pricing

---

## 🚀 Recommandations Production

### Emails
- Utilisez un service professionnel : **SendGrid**, **Mailgun**, **Amazon SES**
- Configurez SPF, DKIM, DMARC pour éviter les spams
- Surveillez les taux de délivrabilité

### SMS
- Passez à un compte Twilio payant pour envoyer à tous les numéros
- Considérez des alternatives locales (ex: SMS API Cameroun)
- Implémentez un système de retry pour les échecs temporaires
- Ajoutez un rate limiting pour éviter les abus

---

## 📚 Ressources

- [Documentation Spring Mail](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)
- [Documentation Twilio SMS](https://www.twilio.com/docs/sms)
- [Mots de passe d'application Google](https://support.google.com/accounts/answer/185833)
- [Console Twilio](https://console.twilio.com/)

---

**Besoin d'aide ?** Consultez les logs du notification-service :
```bash
docker-compose logs -f notification-service
```
