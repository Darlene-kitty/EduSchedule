# Configuration Teacher Availability Service + Reservation Service

## Vue d'ensemble

Ce document explique comment utiliser **Teacher Availability Service** et **Reservation Service** sans Docker, avec une configuration MySQL locale.

## Architecture Actuelle

### Services Principaux
- **Teacher Availability Service** (port 8089) - Service avancé de gestion des disponibilités
- **Reservation Service** (port 8085) - Service de gestion des réservations
- **API Gateway** (port 8080) - Routage des requêtes
- **Eureka Server** (port 8761) - Découverte de services

### Différences avec User Service

| Aspect | User Service | Teacher Availability Service |
|--------|-------------|------------------------------|
| Structure | Simple (dayOfWeek, startTime, endTime) | Complexe (TeacherAvailabilityDTO + TimeSlotDTO[]) |
| Fonctionnalités | Basiques | Avancées (conflits, statistiques, périodes) |
| Validation | Minimale | Complète avec détection de conflits |
| Cache | Non | Oui (Redis) |
| Périodes | Non supportées | Support complet (effectiveDate, endDate) |

## Configuration

### 1. Base de Données
```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/iusjcdb
spring.datasource.username=root
spring.datasource.password=root
```

### 2. API Gateway
Le routage est configuré pour Teacher Availability Service :
```properties
# Gateway Routes - Teacher Availability
spring.cloud.gateway.routes[10].id=teacher-availability
spring.cloud.gateway.routes[10].uri=lb://TEACHER-AVAILABILITY-SERVICE
spring.cloud.gateway.routes[10].predicates[0]=Path=/api/teacher-availability/**
```

### 3. Frontend
Le client API a été mis à jour pour correspondre à la structure du Teacher Availability Service :

#### Nouvelle Structure DTO
```typescript
interface TeacherAvailability {
  id?: number
  teacherId: number
  effectiveDate: string // YYYY-MM-DD
  endDate?: string
  availableSlots: TimeSlot[]
  status: AvailabilityStatus
  maxHoursPerDay?: number
  maxHoursPerWeek?: number
  // ... autres champs
}

interface TimeSlot {
  id?: number
  startTime: string // HH:mm
  endTime: string   // HH:mm
  dayOfWeek: DayOfWeek
  isRecurring?: boolean
}
```

## Démarrage des Services

### Option 1: Script Automatique
```bash
.\start-teacher-availability-reservation.bat
```

### Option 2: Démarrage Manuel
```bash
# 1. Eureka Server
cd eureka-server
mvn spring-boot:run

# 2. Teacher Availability Service
cd teacher-availability-service
mvn spring-boot:run

# 3. Reservation Service
cd reservation-service
mvn spring-boot:run

# 4. API Gateway
cd api-gateway
mvn spring-boot:run
```

## Test et Vérification

### Script de Test
```powershell
.\test-teacher-availability-reservation.ps1
```

### Tests Manuels

#### 1. Vérification des Services
```bash
# Eureka
curl http://localhost:8761/actuator/health

# Teacher Availability Service
curl http://localhost:8089/actuator/health
curl http://localhost:8089/api/teacher-availability/health

# Reservation Service
curl http://localhost:8085/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health
```

#### 2. Test du Routage API Gateway
```bash
# Via API Gateway
curl http://localhost:8080/api/teacher-availability/health
curl http://localhost:8080/api/teacher-availability/stats
```

## APIs Disponibles

### Teacher Availability Service

#### Endpoints Principaux
- `POST /api/teacher-availability` - Créer une disponibilité
- `GET /api/teacher-availability/teacher/{teacherId}` - Disponibilités d'un enseignant
- `GET /api/teacher-availability/teacher/{teacherId}/active` - Disponibilité active
- `GET /api/teacher-availability/teacher/{teacherId}/slots` - Créneaux disponibles
- `GET /api/teacher-availability/teacher/{teacherId}/check` - Vérifier disponibilité
- `POST /api/teacher-availability/conflicts/check` - Détecter conflits
- `GET /api/teacher-availability/stats` - Statistiques

#### Exemple de Création de Disponibilité
```json
{
  "teacherId": 1,
  "effectiveDate": "2025-01-20",
  "endDate": "2025-06-30",
  "availableSlots": [
    {
      "startTime": "08:00",
      "endTime": "09:00",
      "dayOfWeek": "MONDAY",
      "isRecurring": true
    },
    {
      "startTime": "09:00",
      "endTime": "10:00",
      "dayOfWeek": "MONDAY",
      "isRecurring": true
    }
  ],
  "status": "ACTIVE",
  "maxHoursPerDay": 8,
  "maxHoursPerWeek": 40,
  "notes": "Disponibilités pour le semestre"
}
```

### Reservation Service

#### Endpoints Principaux
- `POST /api/v1/reservations` - Créer une réservation
- `GET /api/v1/reservations` - Lister les réservations
- `GET /api/v1/reservations/{id}` - Détails d'une réservation
- `PUT /api/v1/reservations/{id}` - Modifier une réservation
- `DELETE /api/v1/reservations/{id}` - Supprimer une réservation

## Fonctionnalités Avancées

### 1. Créneaux d'1 Heure
Le système est configuré pour des créneaux de 1 heure exactement :
- Validation automatique de la durée
- Exclusion automatique de la pause déjeuner (12h-14h)
- Génération automatique des créneaux standards (8h-12h, 14h-18h)

### 2. Détection de Conflits
- Vérification automatique des chevauchements
- Validation des contraintes horaires
- Messages d'erreur détaillés

### 3. Gestion des Périodes
- Support des dates d'effet et de fin
- Disponibilités temporaires ou permanentes
- Historique des modifications

## Intégration Frontend

### Composants Mis à Jour
- `teacher-availability-view.tsx` - Vue principale
- `add-availability-modal.tsx` - Ajout de disponibilités
- `time-slot-picker.tsx` - Sélection de créneaux
- `bulk-availability-modal.tsx` - Ajout en masse

### Utilitaires
```typescript
// Créer un créneau d'1 heure
const slot = availabilityUtils.createOneHourSlot("08:00", DayOfWeek.MONDAY)

// Générer les créneaux d'une journée
const daySlots = availabilityUtils.generateDaySlots(DayOfWeek.MONDAY)

// Vérifier l'heure du déjeuner
const isLunch = availabilityUtils.isLunchTime("12:00", "13:00")
```

## Prochaines Étapes

### 1. Tests d'Intégration
- [ ] Tester la création de disponibilités via le frontend
- [ ] Vérifier la synchronisation avec Reservation Service
- [ ] Tester les créneaux d'1 heure

### 2. Fonctionnalités à Ajouter au User Service
Si vous souhaitez revenir au User Service plus tard :
- Structure DTO complexe (TeacherAvailabilityDTO)
- Gestion des périodes (effectiveDate, endDate)
- Détection de conflits
- Cache Redis
- Validation avancée
- Statistiques et rapports

### 3. Configuration Avancée
- [ ] Configuration Redis pour le cache
- [ ] Configuration RabbitMQ pour les événements
- [ ] Monitoring et métriques
- [ ] Tests automatisés

## Dépannage

### Problèmes Courants

#### 1. Service ne démarre pas
```bash
# Vérifier les logs
cd teacher-availability-service
mvn spring-boot:run

# Vérifier MySQL
mysql -u root -p -e "SHOW DATABASES;"
```

#### 2. Erreur de routage API Gateway
```bash
# Vérifier l'enregistrement Eureka
curl http://localhost:8761/eureka/apps

# Vérifier la configuration Gateway
curl http://localhost:8080/actuator/gateway/routes
```

#### 3. Erreur de base de données
```sql
-- Créer la base si nécessaire
CREATE DATABASE IF NOT EXISTS iusjcdb;
USE iusjcdb;

-- Vérifier les tables
SHOW TABLES;
```

## Support

Pour plus d'informations :
- `INTEGRATION_FRONTEND_BACKEND.md` - Intégration complète
- `GUIDE_DISPONIBILITES_ENSEIGNANTS.md` - Guide utilisateur
- `ANALYSE_TEACHER_AVAILABILITY_SERVICES.md` - Analyse comparative