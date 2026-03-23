# Frontière : scheduling-service vs course-service

## Rôles distincts

### course-service (port 8084)
Responsable de :
- La définition des cours (`courses`) et groupes (`course_groups`)
- L'algorithme de génération automatique Ford-Fulkerson (`/api/v1/timetable/generate`)
- La persistance des emplois du temps **générés** dans `generated_schedules`
- L'export vers le `calendar-service` après confirmation

**Quand l'utiliser :** génération automatique d'un emploi du temps optimal à partir des contraintes (salles, enseignants, créneaux).

---

### scheduling-service (port 8085)
Responsable de :
- Le CRUD manuel des séances planifiées (`schedules`)
- La gestion des créneaux horaires (`time_slots`)
- La consultation, modification, annulation de séances individuelles
- Les endpoints `/api/v1/schedules/**` et `/api/v1/timeslots/**`

**Quand l'utiliser :** affichage de l'emploi du temps courant, modifications manuelles ponctuelles, consultation par enseignant/salle/groupe.

---

## Flux typique

```
[Admin] → timetable-generator (frontend)
    → POST /api/v1/timetable/generate   (course-service, Ford-Fulkerson)
    → GET  /api/v1/timetable/status/:id (polling)
    → POST /api/v1/timetable/:id/confirm
        → sauvegarde dans generated_schedules (course-service)
        → sync vers calendar-service

[Enseignant/Admin] → schedule page (frontend)
    → GET /api/v1/schedules             (scheduling-service, CRUD)
    → modifications manuelles ponctuelles
```

## Ce qu'il ne faut PAS faire
- Ne pas dupliquer la logique Ford-Fulkerson dans le scheduling-service
- Ne pas appeler le scheduling-service depuis le timetable-generator
- Ne pas stocker les `generated_schedules` dans la table `schedules` du scheduling-service
  (ce sont deux représentations différentes : l'une est le résultat de l'algo, l'autre est le planning opérationnel)

## Évolution future possible
Si besoin, ajouter un endpoint dans le `scheduling-service` pour **importer** un emploi du temps
confirmé depuis le `course-service` (via un event RabbitMQ ou un appel REST), afin d'avoir
une vue unifiée dans le CRUD manuel.
