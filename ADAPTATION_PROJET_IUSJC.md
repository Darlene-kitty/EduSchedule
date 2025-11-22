# 🎓 Adaptation au Projet IUSJC - Gestion des Salles et Emplois du Temps

## 📋 Contexte du Projet

### Institut Universitaire Saint Jean du Cameroun (IUSJC)

**Campus :** Eyang  
**Écoles :** 4 écoles sur le même campus
- SJI (Saint Jean Institute)
- SJM (Saint Jean Medical)
- PrepaVogt
- CPGE (Classes Préparatoires aux Grandes Écoles)

**Problématique :**
- Infrastructures partagées (salles, labos, amphithéâtres)
- Enseignants intervenant dans plusieurs écoles
- Chevauchements d'horaires fréquents
- Réservation des salles inefficace
- Conflits d'emplois du temps

---

## 🎯 Objectif du Système

Développer un système de gestion automatisé pour :
- Optimiser la réservation des infrastructures
- Faciliter la planification des cours
- Éviter les conflits d'horaires
- Améliorer l'utilisation des ressources

---

## 👥 Utilisateurs du Système

### Rôles Définis

#### 1. ADMIN (Administrateurs)
**Qui :** Personnel administratif de l'IUSJC

**Permissions :**
- ✅ Gestion complète des utilisateurs
- ✅ Gestion des écoles et filières
- ✅ Gestion des salles et équipements
- ✅ Création et modification des emplois du temps
- ✅ Gestion des réservations
- ✅ Accès aux rapports et statistiques
- ✅ Configuration du système

**Cas d'usage :**
- Créer les comptes enseignants
- Configurer les écoles et filières
- Gérer les salles et équipements
- Résoudre les conflits d'horaires
- Générer les rapports d'utilisation

#### 2. TEACHER (Enseignants)
**Qui :** Enseignants intervenant dans une ou plusieurs écoles

**Permissions :**
- ✅ Consultation de leur emploi du temps
- ✅ Consultation des salles disponibles
- ✅ Demande de réservation de salle
- ✅ Consultation des groupes d'étudiants
- ✅ Synchronisation avec calendrier personnel
- ✅ Réception de notifications

**Cas d'usage :**
- Consulter son emploi du temps
- Vérifier les disponibilités de salles
- Demander une réservation
- Recevoir des notifications de changements
- Synchroniser avec Google Calendar/Outlook

#### 3. STUDENT (Étudiants) - Via ENT
**Qui :** Étudiants de l'IUSJC

**Accès :** Via l'ENT (Environnement Numérique de Travail)

**Permissions :**
- ✅ Consultation de leur emploi du temps
- ✅ Consultation des salles de cours
- ✅ Réception de rappels
- ✅ Synchronisation avec calendrier

**Note :** Les étudiants n'ont PAS de compte dans le système de gestion.  
Ils consultent via l'ENT qui s'intègre au système.

---

## 🔐 Adaptations de l'Authentification

### Changements Appliqués

#### 1. Rôles Utilisateurs
**Avant :** ADMIN, TEACHER, STUDENT  
**Après :** ADMIN, TEACHER uniquement

**Raison :** Les étudiants accèdent via l'ENT, pas directement au système.

#### 2. Permissions
```
ADMIN:
  - Gestion complète du système
  - CRUD sur tous les modules
  - Accès aux rapports

TEACHER:
  - Lecture seule sur emplois du temps
  - Demande de réservation
  - Consultation des ressources
```

#### 3. Endpoints Sécurisés
```
/api/auth/**           → Public (login, register)
/api/users/**          → ADMIN uniquement
/api/schedules/**      → ADMIN (write), TEACHER (read)
/api/rooms/**          → ADMIN (write), TEACHER (read)
/api/schools/**        → ADMIN (write), TEACHER (read)
/api/reservations/**   → ADMIN (write), TEACHER (create/read)
```

---

## 🏗️ Architecture Adaptée

### Microservices

#### 1. user-service ✅ COMPLET
**Responsabilité :** Authentification et gestion des utilisateurs

**Entités :**
- User (ADMIN, TEACHER)
- Role

**Endpoints :**
- POST /api/auth/register (ADMIN crée les comptes)
- POST /api/auth/login
- GET /api/auth/me
- GET /api/users (ADMIN)

#### 2. school-service 📋 À DÉVELOPPER
**Responsabilité :** Gestion des écoles, filières et groupes

**Entités :**
- School (SJI, SJM, PrepaVogt, CPGE)
- Filiere (Informatique, Génie Civil, etc.)
- Groupe (ISI 4A, ISI 4B, etc.)

**Endpoints :**
- CRUD /api/schools
- CRUD /api/filieres
- CRUD /api/groupes

#### 3. room-service 📋 À DÉVELOPPER
**Responsabilité :** Gestion des salles et équipements

**Entités :**
- Room (Amphi, Salle, Labo)
- Equipment (Projecteur, Ordinateurs, etc.)

**Types de salles :**
- AMPHITHEATRE (conférences, cours magistraux)
- CLASSROOM (TD, cours normaux)
- LABORATORY (TP, expériences)

**Endpoints :**
- CRUD /api/rooms
- CRUD /api/equipments
- GET /api/rooms/available (disponibilités en temps réel)

#### 4. course-service 📋 À DÉVELOPPER
**Responsabilité :** Gestion des cours

**Entités :**
- Course (cours avec enseignant, filière, type)
- Material (supports de cours)

**Types de cours :**
- CM (Cours Magistral)
- TD (Travaux Dirigés)
- TP (Travaux Pratiques)

**Endpoints :**
- CRUD /api/courses
- GET /api/courses/teacher/{teacherId}
- GET /api/courses/filiere/{filiereId}

#### 5. scheduling-service ⭐ EN COURS
**Responsabilité :** Génération et gestion des emplois du temps

**Fonctionnalités :**
- ✅ CRUD emplois du temps
- ✅ Cache Redis
- ✅ Events RabbitMQ
- 📋 Détection de conflits
- 📋 Assignation automatique de salles
- 📋 Optimisation des horaires

**Endpoints :**
- CRUD /api/schedules
- GET /api/schedules/teacher/{teacherId}
- GET /api/schedules/group/{groupId}
- GET /api/schedules/room/{roomId}
- GET /api/schedules/school/{schoolId}
- POST /api/schedules/detect-conflicts
- POST /api/schedules/auto-assign-rooms

#### 6. reservation-service 📋 À DÉVELOPPER
**Responsabilité :** Réservation de salles

**Entités :**
- Reservation (salle, date, heure, demandeur)
- ReservationRequest (demande de réservation)

**Workflow :**
1. Enseignant demande une réservation
2. Système vérifie disponibilité
3. Admin valide ou refuse
4. Notification envoyée

**Endpoints :**
- POST /api/reservations/request (TEACHER)
- GET /api/reservations/pending (ADMIN)
- PUT /api/reservations/{id}/approve (ADMIN)
- PUT /api/reservations/{id}/reject (ADMIN)

#### 7. notification-service ⭐ EN COURS
**Responsabilité :** Notifications multi-canal

**Canaux :**
- ✅ Email (SMTP)
- 📋 SMS (Twilio)
- 📋 Push (WebSocket)
- 📋 In-app

**Types de notifications :**
- Changement d'emploi du temps
- Annulation de cours
- Changement de salle
- Rappel de cours
- Validation de réservation

#### 8. reporting-service 📋 À DÉVELOPPER
**Responsabilité :** Rapports et statistiques

**Rapports :**
- Taux d'occupation des salles
- Emplois du temps par école
- Emplois du temps par enseignant
- Conflits détectés
- Utilisation des équipements

**Endpoints :**
- GET /api/reports/room-usage
- GET /api/reports/teacher-schedule
- GET /api/reports/school-statistics
- GET /api/reports/conflicts

#### 9. ent-integration-service 📋 À DÉVELOPPER
**Responsabilité :** Intégration avec l'ENT

**Fonctionnalités :**
- Synchronisation des emplois du temps
- API pour consultation étudiants
- Webhooks pour mises à jour

**Endpoints :**
- GET /api/ent/schedules/student/{studentId}
- GET /api/ent/schedules/group/{groupId}
- POST /api/ent/webhook/sync

#### 10. frontend-thymeleaf 📋 À DÉVELOPPER
**Responsabilité :** Interface web

**Pages :**
- Login
- Dashboard (ADMIN/TEACHER)
- Gestion des écoles (ADMIN)
- Gestion des salles (ADMIN)
- Emplois du temps (ADMIN/TEACHER)
- Réservations (ADMIN/TEACHER)
- Rapports (ADMIN)
- Calendrier intégré

---

## 📊 Base de Données Adaptée

### Tables Créées

#### Existantes
- ✅ users
- ✅ roles
- ✅ schedules
- ✅ time_slots
- ✅ notifications

#### Nouvelles (ajoutées)
- ✅ schools (écoles)
- ✅ filieres (filières)
- ✅ groupes (groupes d'étudiants)
- ✅ rooms (salles)
- ✅ equipments (équipements)
- ✅ courses (cours)

#### Relations
```
schools → filieres → groupes
rooms → equipments
courses → filieres
schedules → courses, rooms, groupes, schools
```

---

## 🔄 Flux Métier Principaux

### 1. Création d'un Emploi du Temps

```
1. ADMIN crée un cours
   - Sélectionne école, filière, groupe
   - Sélectionne enseignant
   - Définit type (CM/TD/TP)

2. Système assigne automatiquement une salle
   - Vérifie capacité vs taille groupe
   - Vérifie type de salle (Amphi pour CM, Labo pour TP)
   - Vérifie disponibilité

3. Système détecte les conflits
   - Enseignant déjà occupé ?
   - Salle déjà réservée ?
   - Groupe déjà en cours ?

4. Si OK : Création + Notification
   - Sauvegarde en DB
   - Cache Redis
   - Event RabbitMQ
   - Email/SMS aux concernés

5. Si Conflit : Proposition d'alternatives
   - Autres salles disponibles
   - Autres créneaux horaires
```

### 2. Demande de Réservation (Enseignant)

```
1. TEACHER demande une réservation
   - Sélectionne salle
   - Sélectionne date/heure
   - Indique motif

2. Système vérifie disponibilité
   - Salle libre ?
   - Pas de conflit ?

3. Demande envoyée à ADMIN
   - Notification email
   - Apparaît dans dashboard

4. ADMIN valide ou refuse
   - Si validé : Réservation créée
   - Si refusé : Notification avec raison

5. Notification au TEACHER
   - Email de confirmation
   - Ajout au calendrier
```

### 3. Consultation via ENT (Étudiant)

```
1. Étudiant se connecte à l'ENT
   - Authentification ENT

2. ENT appelle ent-integration-service
   - GET /api/ent/schedules/student/{id}

3. Système retourne emploi du temps
   - Cours de la semaine
   - Salles assignées
   - Enseignants

4. Affichage dans l'ENT
   - Calendrier hebdomadaire
   - Détails des cours
   - Localisation des salles
```

---

## 🎯 Fonctionnalités Prioritaires

### Phase 1 (Semaine 1-2) - MVP
- [x] Authentification JWT (ADMIN, TEACHER)
- [x] Gestion des utilisateurs
- [x] CRUD emplois du temps basique
- [x] Notifications email
- [ ] Gestion des écoles et filières
- [ ] Gestion des salles

### Phase 2 (Semaine 3-4) - Fonctionnalités Clés
- [ ] Assignation automatique de salles
- [ ] Détection de conflits
- [ ] Réservation de salles
- [ ] Calendrier intégré
- [ ] Frontend Thymeleaf

### Phase 3 (Semaine 5-6) - Optimisation
- [ ] Optimisation des emplois du temps
- [ ] Rapports et statistiques
- [ ] Notifications SMS
- [ ] Synchronisation calendrier (Google, Outlook)

### Phase 4 (Semaine 7-8) - Intégration
- [ ] Intégration ENT
- [ ] Gestion des événements académiques
- [ ] Tests complets
- [ ] Déploiement VPS

---

## 📝 Données de Test Ajoutées

### Écoles
- SJI (Saint Jean Institute)
- SJM (Saint Jean Medical)
- PrepaVogt
- CPGE

### Filières (SJI)
- Informatique (Licence)
- Génie Civil (Licence)
- Électronique (Master)

### Groupes
- ISI 4A (35 étudiants)
- ISI 4B (30 étudiants)
- GC 3A (40 étudiants)

### Salles
- Amphi A (200 places)
- Salle A101 (40 places)
- Labo Info 1 (30 places)
- Salle TD 201 (35 places)

### Équipements
- Projecteurs
- Ordinateurs
- Tableaux blancs
- Baffles

### Utilisateurs
- admin / password (ADMIN)
- prof.dupont / password (TEACHER)
- prof.martin / password (TEACHER)

---

## ✅ Checklist d'Adaptation

- [x] Rôles adaptés (ADMIN, TEACHER uniquement)
- [x] Permissions ajustées
- [x] Base de données étendue (écoles, salles, etc.)
- [x] Données de test IUSJC
- [x] Documentation mise à jour
- [ ] Développer school-service
- [ ] Développer room-service
- [ ] Développer course-service
- [ ] Implémenter détection de conflits
- [ ] Implémenter assignation automatique
- [ ] Développer frontend Thymeleaf
- [ ] Intégrer avec ENT

---

## 🚀 Prochaines Étapes

1. **Build et test** du système actuel
2. **Développer school-service** (écoles, filières, groupes)
3. **Développer room-service** (salles, équipements)
4. **Améliorer scheduling-service** (conflits, auto-assignation)
5. **Développer frontend** Thymeleaf
6. **Intégrer ENT**

---

**Le système est maintenant adapté aux besoins spécifiques de l'IUSJC ! 🎓**
