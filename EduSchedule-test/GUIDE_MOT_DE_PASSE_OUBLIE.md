# 🔐 Guide du Système de Mot de Passe Oublié - EduSchedule

## 🎯 Vue d'Ensemble

Le système EduSchedule dispose maintenant d'un système complet de réinitialisation de mot de passe qui permet aux utilisateurs de récupérer l'accès à leur compte en toute sécurité.

## ✅ Fonctionnalités Implémentées

### Backend (User Service)
- ✅ **Entité PasswordResetToken** - Gestion des tokens de réinitialisation
- ✅ **Service PasswordResetService** - Logique métier complète
- ✅ **Service EmailService** - Envoi d'emails HTML avec templates
- ✅ **Endpoints API** - 3 nouveaux endpoints sécurisés
- ✅ **Validation de sécurité** - Tokens expirables et à usage unique
- ✅ **Nettoyage automatique** - Tâche planifiée pour supprimer les tokens expirés
- ✅ **Templates d'email** - Emails HTML professionnels avec branding IUSJC

### Frontend (React/Next.js)
- ✅ **Page "Mot de passe oublié"** - Interface utilisateur intuitive
- ✅ **Page "Réinitialiser mot de passe"** - Validation en temps réel
- ✅ **Indicateur de force du mot de passe** - Aide l'utilisateur à créer un mot de passe sécurisé
- ✅ **Validation des tokens** - Vérification automatique de la validité
- ✅ **Gestion d'erreurs** - Messages d'erreur clairs et informatifs
- ✅ **Lien depuis la page de connexion** - Accès facile à la fonctionnalité

## 🔄 Flux Utilisateur Complet

### 1. Demande de Réinitialisation
1. L'utilisateur va sur `/login`
2. Clique sur "Mot de passe oublié ?"
3. Redirigé vers `/forgot-password`
4. Saisit son adresse email
5. Clique sur "Envoyer le lien de réinitialisation"

### 2. Traitement Backend
1. Le système vérifie si l'email existe
2. Génère un token sécurisé (32 bytes, Base64 URL-safe)
3. Sauvegarde le token avec expiration (24h par défaut)
4. Envoie un email HTML avec le lien de réinitialisation
5. Retourne une réponse générique (pour la sécurité)

### 3. Email de Réinitialisation
L'utilisateur reçoit un email contenant :
- **Branding IUSJC** - Logo et couleurs de l'institut
- **Lien sécurisé** - Token unique intégré dans l'URL
- **Instructions claires** - Étapes à suivre
- **Informations de sécurité** - Durée de validité, conseils
- **Design responsive** - Compatible mobile et desktop

### 4. Réinitialisation du Mot de Passe
1. L'utilisateur clique sur le lien dans l'email
2. Redirigé vers `/reset-password?token=...`
3. Le système valide automatiquement le token
4. Si valide : formulaire de nouveau mot de passe
5. Si invalide : message d'erreur avec lien pour recommencer

### 5. Validation et Sécurité
- **Force du mot de passe** - Indicateur visuel en temps réel
- **Critères de sécurité** - 8+ caractères, majuscule, chiffre, caractère spécial
- **Confirmation** - Double saisie pour éviter les erreurs
- **Validation côté client et serveur**

### 6. Confirmation
1. Mot de passe mis à jour dans la base de données (hashé)
2. Token marqué comme utilisé
3. Email de confirmation envoyé
4. Redirection vers la page de connexion

## 🛠️ Configuration Technique

### Variables d'Environnement

```bash
# Configuration SMTP (déjà configurée)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=votre-mot-de-passe-app

# Configuration de l'application
FRONTEND_URL=http://localhost:3000
PASSWORD_RESET_TOKEN_EXPIRY_HOURS=24
```

### Endpoints API

#### 1. Demander une Réinitialisation
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "utilisateur@example.com"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Si cet email existe dans notre système, vous recevrez un lien de réinitialisation."
}
```

#### 2. Valider un Token
```http
GET /api/auth/reset-password/validate?token=TOKEN_VALUE
```

**Réponse :**
```json
{
  "success": true,
  "message": "Token valide"
}
```

#### 3. Réinitialiser le Mot de Passe
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "TOKEN_VALUE",
  "newPassword": "NouveauMotDePasse123!",
  "confirmPassword": "NouveauMotDePasse123!"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Votre mot de passe a été modifié avec succès. Vous pouvez maintenant vous connecter."
}
```

## 🔒 Sécurité Implémentée

### Génération de Tokens
- **Algorithme** : SecureRandom + Base64 URL-safe
- **Taille** : 32 bytes (256 bits)
- **Format** : URL-safe sans padding
- **Unicité** : Garantie par la taille et l'algorithme

### Validation et Expiration
- **Durée de vie** : 24 heures (configurable)
- **Usage unique** : Token marqué comme utilisé après réinitialisation
- **Validation** : Vérification de l'expiration et de l'usage

### Protection contre les Attaques
- **Énumération d'emails** : Réponse générique même si l'email n'existe pas
- **Brute force** : Tokens longs et aléatoires
- **Replay attacks** : Tokens à usage unique
- **Timing attacks** : Temps de réponse constant

### Nettoyage Automatique
- **Tâche planifiée** : Tous les jours à 2h du matin
- **Suppression** : Tokens expirés automatiquement supprimés
- **Performance** : Base de données maintenue propre

## 📧 Templates d'Email

### Email de Réinitialisation
- **Design professionnel** avec branding IUSJC
- **Responsive** pour mobile et desktop
- **Bouton d'action** proéminent
- **Lien de secours** si le bouton ne fonctionne pas
- **Informations de sécurité** et conseils

### Email de Confirmation
- **Confirmation** de la réinitialisation réussie
- **Horodatage** de la modification
- **Conseils de sécurité** pour l'avenir
- **Contact** en cas de problème

## 🧪 Tests et Validation

### Tests Backend
```bash
# Démarrer les services
./start-backend.sh

# Tester l'endpoint de demande
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'

# Vérifier les logs
tail -f logs/user-service.log | grep "Password reset"
```

### Tests Frontend
1. Aller sur http://localhost:3000/login
2. Cliquer sur "Mot de passe oublié ?"
3. Tester avec un email valide
4. Vérifier la réception de l'email
5. Cliquer sur le lien dans l'email
6. Tester la réinitialisation du mot de passe

### Tests d'Intégration
1. **Flux complet** : De la demande à la connexion
2. **Validation des tokens** : Tokens expirés, utilisés, invalides
3. **Sécurité** : Tentatives d'énumération, tokens malformés
4. **Performance** : Temps de réponse, charge

## 🚀 Déploiement

### Prérequis
- ✅ Configuration SMTP fonctionnelle
- ✅ Base de données MySQL avec tables créées
- ✅ Variables d'environnement configurées
- ✅ Frontend et backend déployés

### Vérifications Post-Déploiement
1. **SMTP** : Tester l'envoi d'emails
2. **Base de données** : Vérifier les tables `password_reset_tokens`
3. **Endpoints** : Tester les 3 nouveaux endpoints
4. **Frontend** : Vérifier les pages `/forgot-password` et `/reset-password`
5. **Logs** : Surveiller les erreurs dans les logs

## 📊 Monitoring et Maintenance

### Métriques à Surveiller
- **Demandes de réinitialisation** : Nombre par jour/heure
- **Taux de succès** : Réinitialisations réussies vs échouées
- **Temps de réponse** : Performance des endpoints
- **Erreurs SMTP** : Échecs d'envoi d'emails

### Logs Importants
```bash
# Succès
grep "Password reset token generated" logs/user-service.log
grep "Password reset successfully" logs/user-service.log
grep "Email sent successfully" logs/user-service.log

# Erreurs
grep "Failed to send email" logs/user-service.log
grep "Token validation failed" logs/user-service.log
grep "Password reset failed" logs/user-service.log
```

### Maintenance Régulière
- **Nettoyage automatique** : Vérifie que la tâche planifiée fonctionne
- **Surveillance SMTP** : Vérifier les quotas et limites
- **Mise à jour des templates** : Améliorer les emails si nécessaire

## 🔧 Personnalisation

### Modifier la Durée d'Expiration
```properties
# application.properties
app.password-reset.token-expiry-hours=48  # 48 heures au lieu de 24
```

### Personnaliser les Templates d'Email
Modifier les méthodes dans `PasswordResetService.java` :
- `buildPasswordResetEmailContent()`
- `buildPasswordResetConfirmationEmailContent()`

### Changer l'URL Frontend
```properties
# application.properties
app.frontend.url=https://eduschedule.iusjc.cm
```

## 🆘 Dépannage

### Problèmes Courants

#### 1. Emails non reçus
- Vérifier la configuration SMTP
- Contrôler les dossiers spam
- Vérifier les logs du service

#### 2. Token invalide
- Vérifier l'expiration (24h par défaut)
- Contrôler si le token a déjà été utilisé
- Vérifier l'URL complète du lien

#### 3. Erreurs de validation
- Contrôler la force du mot de passe
- Vérifier la correspondance des mots de passe
- Vérifier la connectivité réseau

### Support
En cas de problème :
1. Consulter les logs des services
2. Vérifier la configuration SMTP
3. Tester les endpoints manuellement
4. Contacter l'équipe de développement

## 📚 Ressources

- **Configuration SMTP** : `CONFIGURATION_SMTP.md`
- **Documentation API** : Endpoints dans `api-config.ts`
- **Code source** : 
  - Backend : `user-service/src/main/java/cm/iusjc/userservice/`
  - Frontend : `frontend/components/` et `frontend/app/`

---

**Le système de mot de passe oublié est maintenant entièrement fonctionnel et prêt pour la production !** 🎉