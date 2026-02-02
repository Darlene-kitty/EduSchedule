# Guide des Disponibilités des Enseignants - EduSchedule

## 🎯 Vue d'ensemble

Le système de gestion des disponibilités permet aux enseignants de définir leurs créneaux de disponibilité et aux administrateurs de les gérer de manière centralisée. Cette fonctionnalité est essentielle pour l'assignation automatique des cours et la prévention des conflits d'horaires.

## 🚀 Comment ajouter une disponibilité

### Méthode 1 : Créneau unique via le bouton "Ajouter"

1. **Accéder à la page** : Cliquez sur "Disponibilités" dans le menu latéral
2. **Cliquer sur "Ajouter"** : Bouton vert en haut à droite de la page
3. **Sélectionner un créneau rapide** : 
   - 08h-09h, 09h-10h, 10h-11h, 11h-12h
   - 14h-15h, 15h-16h, 16h-17h, 17h-18h
4. **Ou saisir l'heure de début** : L'heure de fin est calculée automatiquement (+1h)
5. **Configurer les options** et valider

### Méthode 2 : Créneaux multiples via "En masse"

1. **Cliquer sur "En masse"** : Bouton à côté de "Ajouter"
2. **Sélectionner les jours** : Lundi à Dimanche (boutons rapides disponibles)
3. **Choisir les créneaux horaires** : Grille visuelle des créneaux d'1h
4. **Configurer le type** et valider
5. **Toutes les combinaisons** jour/créneau sont créées automatiquement

### Méthode 3 : Via le calendrier hebdomadaire

1. **Passer en vue calendrier** : Cliquez sur l'icône grille
2. **Cliquer sur le "+"** dans la colonne du jour souhaité
3. **Le formulaire s'ouvre** avec le jour pré-sélectionné

### Méthode 4 : Créneaux rapides prédéfinis

Dans le formulaire d'ajout, utilisez les boutons de créneaux rapides :
- **08h-09h** : Premier créneau du matin
- **09h-10h** : Deuxième créneau du matin
- **10h-11h** : Troisième créneau du matin
- **11h-12h** : Dernier créneau avant pause
- **14h-15h** : Premier créneau après-midi
- **15h-16h** : Deuxième créneau après-midi
- **16h-17h** : Troisième créneau après-midi
- **17h-18h** : Dernier créneau de la journée

> **Note importante** : Tous les créneaux durent exactement **1 heure**. La pause déjeuner (12h-14h) est automatiquement exclue.

## 📋 Formulaire d'ajout détaillé

### Champs obligatoires

1. **Jour de la semaine** : Sélectionnez le jour (Lundi à Dimanche)
2. **Heure de début** : Format HH:MM (ex: 08:00)
3. **Heure de fin** : Format HH:MM (ex: 18:00)
4. **Type de disponibilité** :
   - ✅ **Disponible** : Créneau libre pour les cours
   - ⭐ **Préféré** : Créneau privilégié
   - ❌ **Indisponible** : Créneau bloqué
   - 🔒 **Occupé** : Cours existant
   - 🚫 **Bloqué** : Congé, réunion, etc.

### Champs optionnels

5. **Priorité** :
   - **Élevée (1)** : Créneau préféré en priorité
   - **Normale (2)** : Créneau acceptable
   - **Faible (3)** : Créneau si nécessaire uniquement

6. **Récurrence** :
   - ✅ **Activé** : Se répète chaque semaine
   - ❌ **Désactivé** : Disponibilité ponctuelle

7. **Date spécifique** : Si récurrence désactivée, choisir la date exacte

8. **Notes** : Commentaires ou précisions

## 🎨 Interface utilisateur

### Vue Calendrier
- **Affichage hebdomadaire** : 7 colonnes pour les jours de la semaine
- **Cartes de disponibilité** : Chaque créneau affiché avec couleurs et icônes
- **Actions rapides** : Modifier/Supprimer au survol
- **Bouton "+"** : Ajouter directement pour un jour

### Vue Liste
- **Cartes détaillées** : Informations complètes de chaque disponibilité
- **Filtrage** : Par jour, type, recherche textuelle
- **Actions** : Modifier/Supprimer sur chaque carte

### Statistiques
- **Total disponibilités** : Nombre total de créneaux
- **Créneaux préférés** : Nombre de créneaux prioritaires
- **Heures/semaine** : Total d'heures disponibles par semaine
- **Indisponibilités** : Nombre de créneaux bloqués

## 🔧 Fonctionnalités avancées

### Disponibilités par défaut
- **Bouton "Défaut"** : Crée automatiquement des disponibilités Lundi-Vendredi 8h-18h
- **Gain de temps** : Évite de saisir manuellement chaque créneau
- **Personnalisable** : Modifiez ensuite selon vos besoins

### Gestion des conflits
- **Détection automatique** : Le système vérifie les chevauchements
- **Alerte** : Message d'erreur si conflit détecté
- **Prévention** : Impossible de créer des créneaux qui se chevauchent

### Recherche et filtrage
- **Recherche textuelle** : Dans les notes et jours de la semaine
- **Filtre par jour** : Afficher seulement un jour spécifique
- **Vue adaptative** : Calendrier ou liste selon préférence

## 📱 Utilisation mobile

L'interface est entièrement responsive :
- **Navigation tactile** : Swipe et tap optimisés
- **Formulaires adaptés** : Sélecteurs de temps natifs
- **Affichage compact** : Cartes empilées sur mobile

## 🎯 Cas d'usage typiques

### Pour un enseignant
1. **Première utilisation** : Cliquer sur "Défaut" puis personnaliser
2. **Ajout ponctuel** : Créer une disponibilité pour un jour spécifique
3. **Modification** : Cliquer sur une carte puis "Modifier"
4. **Blocage** : Marquer un créneau comme "Indisponible" pour congé

### Pour un administrateur
1. **Vue d'ensemble** : Consulter les disponibilités de tous les enseignants
2. **Planification** : Utiliser les créneaux préférés pour l'assignation
3. **Résolution de conflits** : Identifier les créneaux problématiques
4. **Optimisation** : Analyser les statistiques d'utilisation

## 🔄 Intégration avec le système

### Assignation automatique des cours
- Les créneaux "Disponible" et "Préféré" sont utilisés pour l'assignation
- La priorité influence l'ordre de sélection
- Les créneaux "Indisponible" sont exclus automatiquement

### Détection de conflits
- Vérification en temps réel lors de la planification
- Prise en compte des temps de déplacement entre écoles
- Alertes proactives pour les conflits potentiels

### Notifications
- Notification automatique lors de changements d'emploi du temps
- Alerte si assignation sur créneau non-préféré
- Rappels pour mettre à jour les disponibilités

## 🛠️ Dépannage

### Problèmes courants

**"Conflit détecté"**
- Vérifiez qu'il n'y a pas de chevauchement avec une autre disponibilité
- Supprimez ou modifiez la disponibilité existante d'abord

**"Erreur de sauvegarde"**
- Vérifiez votre connexion internet
- Assurez-vous que tous les champs obligatoires sont remplis
- Contactez l'administrateur si le problème persiste

**"Aucune disponibilité trouvée"**
- Cliquez sur "Défaut" pour créer des disponibilités de base
- Vérifiez les filtres de recherche
- Assurez-vous d'être connecté avec le bon compte

### Support technique
- **Documentation** : Consultez ce guide
- **Contact** : Contactez votre administrateur système
- **Formation** : Sessions de formation disponibles sur demande

## 📈 Bonnes pratiques

1. **Régularité** : Maintenez vos disponibilités à jour
2. **Précision** : Utilisez les notes pour des précisions importantes
3. **Anticipation** : Bloquez vos congés à l'avance
4. **Communication** : Informez l'administration des changements majeurs
5. **Vérification** : Consultez régulièrement votre emploi du temps généré

---

*Ce guide est mis à jour régulièrement. Pour la dernière version, consultez la documentation en ligne.*