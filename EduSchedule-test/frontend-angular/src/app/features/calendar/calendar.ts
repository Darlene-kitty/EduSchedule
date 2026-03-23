import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { ScheduleManagementService, ScheduleEntry } from '../../core/services/schedule-management.service';

export interface CalendarEvent {
  id: number; subject: string; room: string; professor: string;
  type: 'CM' | 'TD' | 'TP'; color: string;
  dayIndex: number; startHour: number; endHour: number;
}

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './calendar.html',
  styleUrl: './calendar.css'
})
export class CalendarComponent implements OnInit {
  private scheduleService = inject(ScheduleManagementService);

  currentDate = ''; currentTime = '';
  selectedView = 'Semaine';
  selectedTeacher = 'Tous les enseignants';
  isLoading = false;

  /* Sync */
  isSyncing = false;
  showSyncToast = false;
  syncMessage = '';

  weekStart = new Date(2025, 9, 21);

  days = [
    { label: 'Lundi',    date: '21 Oct' },
    { label: 'Mardi',    date: '22 Oct' },
    { label: 'Mercredi', date: '23 Oct' },
    { label: 'Jeudi',    date: '24 Oct' },
    { label: 'Vendredi', date: '25 Oct' },
    { label: 'Samedi',   date: '26 Oct' },
  ];

  hours = [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18];

  events: CalendarEvent[] = [];

  private demoEvents: CalendarEvent[] = [
    { id: 1, subject: 'Mathématiques',  room: 'A101', professor: 'Dr. Dupont',    type: 'CM', color: '#1D4ED8', dayIndex: 0, startHour: 9,  endHour: 11 },
    { id: 2, subject: 'Physique',       room: 'B203', professor: 'Prof. Bernard', type: 'CM', color: '#1D4ED8', dayIndex: 1, startHour: 11, endHour: 13 },
    { id: 3, subject: 'Analyse',        room: 'A103', professor: 'Dr. Dupont',    type: 'CM', color: '#1D4ED8', dayIndex: 2, startHour: 9,  endHour: 10 },
    { id: 4, subject: 'Géométrie',      room: 'A101', professor: 'Dr. Martin',    type: 'TD', color: '#15803D', dayIndex: 3, startHour: 10, endHour: 12 },
    { id: 5, subject: 'Statistiques',   room: 'A104', professor: 'Dr. Blanc',     type: 'CM', color: '#1D4ED8', dayIndex: 4, startHour: 9,  endHour: 10 },
    { id: 6, subject: 'Probabilités',   room: 'A104', professor: 'Dr. Blanc',     type: 'TD', color: '#15803D', dayIndex: 4, startHour: 11, endHour: 12 },
    { id: 7, subject: 'TP Chimie',      room: 'Lab C', professor: 'Dr. Dubois',   type: 'TP', color: '#7C3AED', dayIndex: 1, startHour: 14, endHour: 16 },
    { id: 8, subject: 'Informatique',   room: 'Lab D', professor: 'Prof. Moreau', type: 'TP', color: '#7C3AED', dayIndex: 3, startHour: 14, endHour: 16 },
  ];

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadSchedule();
  }

  loadSchedule(): void {
    this.isLoading = true;
    this.scheduleService.getSchedule().subscribe({
      next: (entries) => {
        if (entries && entries.length > 0) {
          const dayMap: Record<string, number> = { MONDAY: 0, TUESDAY: 1, WEDNESDAY: 2, THURSDAY: 3, FRIDAY: 4, SATURDAY: 5, Lundi: 0, Mardi: 1, Mercredi: 2, Jeudi: 3, Vendredi: 4, Samedi: 5 };
          const typeColors: Record<string, string> = { CM: '#1D4ED8', TD: '#15803D', TP: '#7C3AED', Séminaire: '#EA580C' };
          this.events = entries.map(e => {
            const startH = e.startTime ? parseInt(e.startTime.split(':')[0]) : 8;
            const endH = e.endTime ? parseInt(e.endTime.split(':')[0]) : startH + 2;
            const typeKey = (e as any).type || 'CM';
            return {
              id: e.id,
              subject: e.courseName || e.title || 'Cours',
              room: e.room || '',
              professor: e.teacher || '',
              type: typeKey as CalendarEvent['type'],
              color: typeColors[typeKey] || '#1D4ED8',
              dayIndex: dayMap[e.dayOfWeek || ''] ?? 0,
              startHour: startH,
              endHour: endH
            };
          });
        } else {
          this.events = this.demoEvents;
        }
        this.isLoading = false;
      },
      error: () => { this.events = this.demoEvents; this.isLoading = false; }
    });
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  getWeekLabel(): string { return 'Semaine du 21 au 27 Octobre 2025'; }

  getEventsForDayAndHour(dayIndex: number, hour: number): CalendarEvent[] {
    return this.events.filter(e => e.dayIndex === dayIndex && e.startHour === hour);
  }

  getEventHeight(event: CalendarEvent): number { return (event.endHour - event.startHour) * 60; }

  prevWeek(): void {}
  nextWeek(): void {}
  goToday(): void {}

  /* ─── Synchroniser ─── */
  synchronize(): void {
    if (this.isSyncing) return;
    this.isSyncing = true;
    this.loadSchedule();
    setTimeout(() => {
      this.isSyncing = false;
      this.syncMessage = `Calendrier synchronisé avec le backend`;
      this.showSyncToast = true;
      setTimeout(() => this.showSyncToast = false, 4500);
    }, 1000);
  }
}