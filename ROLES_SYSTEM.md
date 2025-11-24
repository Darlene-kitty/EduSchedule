# 👥 Système de Rôles - EduSchedule IUSJC

## 🎯 Rôles Disponibles

Le système EduSchedule IUSJC utilise **2 rôles principaux** :

### 1. 👨‍💼 ADMIN (Administrateur)
**Accès complet au système**

**Permissions :**
- ✅ Gestion des utilisateurs (CRUD)
- ✅ Gestion des salles et ressources
- ✅ Gestion des cours et filières
- ✅ Création et modification des emplois du temps
- ✅ Résolution des conflits
- ✅ Génération de rapports
- ✅ Configuration système
- ✅ Gestion des notifications

**Cas d'usage :**
- Directeur des études
- Responsable administratif
- Personnel IT

### 2. 👨‍🏫 TEACHER (Enseignant)
**Accès limité à la consultation et gestion de ses cours**

**Permissions :**
- ✅ Consultation de l'emploi du temps
- ✅ Consultation de ses cours
- ✅ Consultation des salles disponibles
- ✅ Réservation de salles (avec validation admin)
- ✅ Consultation du calendrier
- ✅ Réception de notifications
- ❌ Modification des emplois du temps
- ❌ Gestion des utilisateurs
- ❌ Accès aux rapports administratifs

**Cas d'usage :**
- Enseignants permanents
- Enseignants vacataires
- Chargés de cours

## 🚫 Rôle Supprimé

### ~~STUDENT (Étudiant)~~
**Ce rôle a été supprimé du système.**

**Raison :** Les étudiants accèdent aux emplois du temps via l'ENT (Espace Numérique de Travail) externe. Ils n'ont pas besoin de compte dans le système de gestion interne.

**Accès étudiant :** Via l'ENT → API publique → Consultation en lecture seule

## 🔐 Matrice des Permissions

| Fonctionnalité | ADMIN | TEACHER |
|----------------|-------|---------|
| **Emplois du Temps** |
| Consulter | ✅ | ✅ |
| Créer | ✅ | ❌ |
| Modifier | ✅ | ❌ |
| Supprimer | ✅ | ❌ |
| **Cours** |
| Consulter | ✅ | ✅ (ses cours) |
| Créer | ✅ | ❌ |
| Modifier | ✅ | ❌ |
| Supprimer | ✅ | ❌ |
| **Salles** |
| Consulter | ✅ | ✅ |
| Réserver | ✅ | ✅ (avec validation) |
| Gérer | ✅ | ❌ |
| **Utilisateurs** |
| Consulter | ✅ | ❌ |
| Créer | ✅ | ❌ |
| Modifier | ✅ | ❌ |
| Supprimer | ✅ | ❌ |
| **Rapports** |
| Consulter | ✅ | ❌ |
| Générer | ✅ | ❌ |
| Exporter | ✅ | ❌ |
| **Notifications** |
| Recevoir | ✅ | ✅ |
| Envoyer | ✅ | ❌ |
| **Conflits** |
| Consulter | ✅ | ✅ (ses conflits) |
| Résoudre | ✅ | ❌ |

## 📝 Comptes de Test

### Administrateur
```
Email: admin@iusjc.cm
Mot de passe: password
Rôle: ADMIN
```

### Enseignant 1
```
Email: dupont@iusjc.cm
Mot de passe: password
Rôle: TEACHER
```

### Enseignant 2
```
Email: martin@iusjc.cm
Mot de passe: password
Rôle: TEACHER
```

## 🔄 Migration depuis l'ancien système

Si vous aviez des comptes STUDENT dans l'ancien système :

1. **Suppression automatique** : Les comptes STUDENT seront ignorés
2. **Pas de migration** : Les étudiants n'ont plus besoin de compte
3. **Accès via ENT** : Les étudiants consultent via l'ENT externe

## 🎨 Interface Utilisateur

### Page de Connexion
- **Titre** : "EduSchedule IUSJC"
- **Sous-titre** : "Gestion des Emplois du Temps - Enseignants & Administration"
- **Boutons de connexion rapide** :
  - 👤 Administrateur
  - 👨‍🏫 Enseignant

### Dashboard
- **Admin** : Vue complète avec statistiques et gestion
- **Teacher** : Vue simplifiée avec emploi du temps et cours

## 🔧 Configuration Technique

### Backend (Spring Security)
```java
// user-service/src/main/java/cm/iusjc/userservice/entity/User.java
@Column(nullable = false, length = 20)
private String role; // ADMIN, TEACHER
```

### Frontend (TypeScript)
```typescript
// frontend/contexts/auth-context.tsx
export type UserRole = "admin" | "teacher"
```

### Base de données
```sql
-- init-db.sql
INSERT INTO roles (name) VALUES ('ADMIN'), ('TEACHER');
```

## 📚 Documentation Associée

- [Architecture du Système](./ARCHITECTURE.md)
- [Guide d'Authentification](./INTEGRATION_AUTHENTIFICATION.md)
- [API Documentation](./docs/API_INTEGRATION.md)
