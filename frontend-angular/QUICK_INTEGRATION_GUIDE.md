# 🚀 Guide Rapide d'Intégration LocalStorage

## Pour intégrer le localStorage dans une page en 5 minutes

### Étape 1 : Identifier le service correspondant

| Page | Service à utiliser |
|------|-------------------|
| Users | `UsersManagementService` |
| Courses | `CoursesManagementService` |
| Rooms | `RoomsManagementService` |
| Reservations | `ReservationsManagementService` |
| Events | `EventsManagementService` |
| Resources | `ResourcesManagementService` |
| Notifications | `NotificationsManagementService` |
| Schedule | `ScheduleManagementService` |
| Calendar | `ScheduleManagementService` + `EventsManagementService` |

### Étape 2 : Copier-coller ce code

```typescript
// 1. Ajouter l'import en haut du fichier
import { Component, OnInit, inject } from '@angular/core';
import { XxxManagementService } from '../../core/services';

// 2. Dans la classe du composant
export class MyComponent implements OnInit {
  // Injecter le service
  private xxxService = inject(XxxManagementService);
  
  // Vos données
  items: Xxx[] = [];

  ngOnInit() {
    this.loadItems();
  }

  // 3. Charger les données
  private loadItems() {
    this.xxxService.getXxx().subscribe(items => {
      this.items = items;
    });
  }

  // 4. Remplacer vos méthodes CRUD

  // AVANT (sans persistance)
  addItem() {
    this.items = [...this.items, newItem];
  }

  // APRÈS (avec persistance)
  addItem() {
    this.xxxService.addXxx(newItem).subscribe();
  }

  // AVANT
  updateItem(id) {
    this.items = this.items.map(i => i.id === id ? updated : i);
  }

  // APRÈS
  updateItem(id, data) {
    this.xxxService.updateXxx(id, data).subscribe();
  }

  // AVANT
  deleteItem(id) {
    this.items = this.items.filter(i => i.id !== id);
  }

  // APRÈS
  deleteItem(id) {
    this.xxxService.deleteXxx(id).subscribe();
  }
}
```

### Étape 3 : Tester

1. Ajouter un élément
2. Appuyer sur F5 (rafraîchir)
3. ✅ L'élément est toujours là !

## Exemples concrets

### Exemple 1 : Page Rooms

```typescript
import { Component, OnInit, inject } from '@angular/core';
import { RoomsManagementService } from '../../core/services';

export class RoomsComponent implements OnInit {
  private roomsService = inject(RoomsManagementService);
  rooms: Room[] = [];

  ngOnInit() {
    this.roomsService.getRooms().subscribe(rooms => {
      this.rooms = rooms;
    });
  }

  addRoom(room) {
    this.roomsService.addRoom({
      name: room.name,
      building: room.building,
      capacity: room.capacity,
      type: room.type,
      equipment: room.equipment
    }).subscribe();
  }

  updateRoom(id, data) {
    this.roomsService.updateRoom(id, data).subscribe();
  }

  deleteRoom(id) {
    this.roomsService.deleteRoom(id).subscribe();
  }
}
```

### Exemple 2 : Page Events

```typescript
import { Component, OnInit, inject } from '@angular/core';
import { EventsManagementService } from '../../core/services';

export class EventsComponent implements OnInit {
  private eventsService = inject(EventsManagementService);
  events: Event[] = [];
  upcomingEvents: Event[] = [];

  ngOnInit() {
    this.eventsService.getEvents().subscribe(events => {
      this.events = events;
    });
    
    this.upcomingEvents = this.eventsService.getUpcomingEvents();
  }

  addEvent(event) {
    this.eventsService.addEvent({
      title: event.title,
      description: event.description,
      date: event.date,
      startTime: event.startTime,
      endTime: event.endTime,
      location: event.location,
      organizer: event.organizer,
      type: event.type
    }).subscribe();
  }
}
```

### Exemple 3 : Page Notifications

```typescript
import { Component, OnInit, inject } from '@angular/core';
import { NotificationsManagementService } from '../../core/services';

export class NotificationsComponent implements OnInit {
  private notificationsService = inject(NotificationsManagementService);
  notifications: Notification[] = [];
  unreadCount = 0;

  ngOnInit() {
    this.notificationsService.getNotifications().subscribe(notifications => {
      this.notifications = notifications;
    });
    
    this.unreadCount = this.notificationsService.getUnreadCount();
  }

  markAsRead(id: number) {
    this.notificationsService.markAsRead(id).subscribe(() => {
      this.unreadCount = this.notificationsService.getUnreadCount();
    });
  }

  markAllAsRead() {
    this.notificationsService.markAllAsRead().subscribe(() => {
      this.unreadCount = 0;
    });
  }
}
```

## Checklist rapide

Pour chaque page :

- [ ] Importer le service
- [ ] Injecter avec `inject()`
- [ ] Charger les données dans `ngOnInit()`
- [ ] Remplacer les opérations locales par des appels au service
- [ ] Tester avec F5

## Astuces

### 1. Gestion des erreurs

```typescript
addItem(item) {
  this.xxxService.addXxx(item).subscribe({
    next: () => {
      console.log('Succès !');
      this.showSuccessMessage();
    },
    error: (err) => {
      console.error('Erreur:', err.message);
      this.showErrorMessage(err.message);
    }
  });
}
```

### 2. Loading state

```typescript
loading = false;

loadItems() {
  this.loading = true;
  this.xxxService.getXxx().subscribe({
    next: (items) => {
      this.items = items;
      this.loading = false;
    },
    error: () => {
      this.loading = false;
    }
  });
}
```

### 3. Recherche locale

```typescript
searchQuery = '';

get filteredItems() {
  if (!this.searchQuery) return this.items;
  
  const query = this.searchQuery.toLowerCase();
  return this.items.filter(item =>
    item.name.toLowerCase().includes(query) ||
    item.description?.toLowerCase().includes(query)
  );
}
```

## Services disponibles

Tous les services sont dans `src/app/core/services/` :

- ✅ `storage.service.ts` - Gestion du localStorage
- ✅ `auth.service.ts` - Authentification
- ✅ `users-management.service.ts` - Utilisateurs
- ✅ `courses-management.service.ts` - Cours
- ✅ `rooms-management.service.ts` - Salles
- ✅ `reservations-management.service.ts` - Réservations
- ✅ `events-management.service.ts` - Événements
- ✅ `resources-management.service.ts` - Ressources
- ✅ `notifications-management.service.ts` - Notifications
- ✅ `schedule-management.service.ts` - Emploi du temps
- ✅ `preferences.service.ts` - Préférences
- ✅ `cache.service.ts` - Cache

## Import simplifié

Au lieu de :
```typescript
import { XxxService } from '../../core/services/xxx.service';
```

Utilisez :
```typescript
import { XxxService } from '@core/services';
```

(Si configuré dans `tsconfig.json`)

## Besoin d'aide ?

Consultez :
- `LOCALSTORAGE_ALL_PAGES.md` - Guide complet
- `TEST_LOCALSTORAGE.md` - Tests
- `LOCALSTORAGE_INTEGRATION.md` - Documentation technique

---

**C'est tout ! En 5 minutes, votre page persiste ses données. 🎉**
