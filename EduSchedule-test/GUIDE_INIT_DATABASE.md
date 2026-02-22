# Guide d'Initialisation de la Base de Données

## Option 1 : Via Script Batch (Recommandé)

1. Exécutez le fichier `init-databases.bat`
2. Entrez votre mot de passe MySQL root
3. Le script créera automatiquement toutes les bases de données et tables

```batch
.\init-databases.bat
```

## Option 2 : Via MySQL Workbench (Interface Graphique)

### Étape 1 : Ouvrir MySQL Workbench
1. Lancez MySQL Workbench
2. Connectez-vous à votre serveur MySQL local

### Étape 2 : Créer les bases de données
1. Ouvrez le fichier `init-db.sql`
2. Cliquez sur l'icône éclair (⚡) pour exécuter
3. Vérifiez que les bases de données sont créées

### Étape 3 : Créer les tables pour chaque service

Pour **school-service** :
1. Sélectionnez la base `eduschedule_school`
2. Ouvrez `school-service-tables.sql`
3. Exécutez le script

Pour **course-service** :
1. Sélectionnez la base `eduschedule_course`
2. Ouvrez `course-service-tables.sql`
3. Exécutez le script

Pour **reservation-service** :
1. Sélectionnez la base `eduschedule_reservation`
2. Ouvrez `reservation-service-tables.sql`
3. Exécutez le script

Pour **scheduling-service** :
1. Sélectionnez la base `eduschedule_scheduling`
2. Ouvrez `scheduling-service-tables.sql`
3. Exécutez le script

## Option 3 : Via Ligne de Commande PowerShell

```powershell
# Définir le chemin MySQL
$mysql = "C:\Program Files\MySQL\MySQL Workbench 8.0 CE\mysql.exe"

# Demander le mot de passe
$password = Read-Host "Mot de passe MySQL root" -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
$pwd = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

# Exécuter les scripts
Write-Host "Création des bases de données..."
Get-Content init-db.sql | & $mysql -u root -p$pwd

Write-Host "Création des tables school-service..."
Get-Content school-service-tables.sql | & $mysql -u root -p$pwd eduschedule_school

Write-Host "Création des tables course-service..."
Get-Content course-service-tables.sql | & $mysql -u root -p$pwd eduschedule_course

Write-Host "Création des tables reservation-service..."
Get-Content reservation-service-tables.sql | & $mysql -u root -p$pwd eduschedule_reservation

Write-Host "Création des tables scheduling-service..."
Get-Content scheduling-service-tables.sql | & $mysql -u root -p$pwd eduschedule_scheduling

Write-Host "Terminé!"
```

## Option 4 : Laisser Spring Boot créer les tables automatiquement

Si vous préférez, vous pouvez configurer Spring Boot pour créer automatiquement les tables :

Dans chaque fichier `application.yml` ou `application.properties` :

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ou 'create' pour recréer à chaque démarrage
```

⚠️ **Attention** : Cette option est pratique pour le développement mais **NON recommandée en production**.

## Vérification

Après l'initialisation, vérifiez que tout est en place :

```sql
-- Lister toutes les bases de données
SHOW DATABASES;

-- Vérifier les tables de chaque base
USE eduschedule_school;
SHOW TABLES;

USE eduschedule_course;
SHOW TABLES;

USE eduschedule_reservation;
SHOW TABLES;

USE eduschedule_scheduling;
SHOW TABLES;
```

## Bases de Données Créées

- `eduschedule_user` - Gestion des utilisateurs
- `eduschedule_school` - Gestion des écoles
- `eduschedule_course` - Gestion des cours
- `eduschedule_room` - Gestion des salles
- `eduschedule_reservation` - Gestion des réservations
- `eduschedule_scheduling` - Gestion des emplois du temps
- `eduschedule_notification` - Gestion des notifications
- `eduschedule_maintenance` - Gestion de la maintenance
- `eduschedule_reporting` - Gestion des rapports
- `eduschedule_integration` - Gestion des intégrations

## Dépannage

### Erreur "Access denied"
- Vérifiez votre mot de passe MySQL
- Vérifiez que l'utilisateur root a les permissions nécessaires

### Erreur "mysql command not found"
- Vérifiez que MySQL est installé
- Ajoutez MySQL au PATH système
- Ou utilisez le chemin complet vers mysql.exe

### Erreur "Database already exists"
- Normal si vous réexécutez le script
- Les scripts utilisent `CREATE DATABASE IF NOT EXISTS`
