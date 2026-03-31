import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { Subscription } from 'rxjs';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { ScheduleManagementService } from '../../core/services/schedule-management.service';
import { UsersManagementService } from '../../core/services/users-management.service';
import { AuthService } from '../../core/services/auth.service';
import { WebSocketService } from '../../core/services/websocket.service';

export interface CalendarEvent {
  id: number; subject: string; room: string; professor: string;
  type: 'CM' | 'TD' | 'TP' | 'Séminaire'; color: string;
  dayIndex: number; startHour: number; endHour: number;
  date?: Date;
}

type ViewMode = 'Semaine' | 'Jour' | 'Mois';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './calendar.html',
  styleUrl: './calendar.css'
})
export class CalendarComponent implements OnInit, OnDestroy {
  private scheduleService = inject(ScheduleManagementService);
  private usersSvc        = inject(UsersManagementService);
  private authService     = inject(AuthService);
  private wsSvc           = inject(WebSocketService);

  private wsSub?: Subscription;

  isTeacher = false;
  currentTeacherName = '';
  teachers: string[] = [];

  currentDate = ''; currentTime = '';
  selectedView: ViewMode = 'Semaine';
  selectedTeacher = 'Tous les enseignants';
  isLoading = false;

  isSyncing = false; showSyncToast = false; syncMessage = '';

  // Navigation
  today = new Date();
  currentWeekStart = this.getMonday(new Date());
  currentDayDate   = new Date();
  currentMonthDate = new Date();

  days = this.buildWeekDays(this.getMonday(new Date()));
  hours = [7,8,9,10,11,12,13,14,15,16,17,18];

  // Mois
  monthWeeks: { date: Date; isCurrentMonth: boolean; isToday: boolean }[][] = [];

  events: CalendarEvent[] = [];

  readonly typeColors: Record<string, string> = {
    CM: '#1D4ED8', TD: '#15803D', TP: '#7C3AED', Séminaire: '#EA580C'
  };

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);

    const user = this.authService.getUser();
    this.isTeacher = this.authService.isTeacher();
    this.currentTeacherName = user?.name || user?.username || '';
    if (this.isTeacher) this.selectedTeacher = this.currentTeacherName;

    this.buildMonthGrid();
    this.loadSchedule();

    if (!this.isTeacher) {
      this.usersSvc.getUsers().subscribe({
        next: users => {
          this.teachers = ['Tous les enseignants', ...users
            .filter(u => (u.role || '').toUpperCase().includes('TEACHER'))
            .map(u => u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || '')
            .filter(Boolean)];
        },
        error: () => {}
      });
    }

    // WebSocket — rechargement automatique quand l'emploi du temps change
    this.wsSvc.connect();
    this.wsSub = this.wsSvc.scheduleChanges$.subscribe(() => {
      this.loadSchedule();
    });
  }

  ngOnDestroy(): void { this.wsSub?.unsubscribe(); }

  // ── Data ──────────────────────────────────────────────────────────────────

  loadSchedule(): void {
    this.isLoading = true;
    this.scheduleService.getSchedule().subscribe({
      next: (entries) => {
        const dayMap: Record<string, number> = {
          MONDAY:0, TUESDAY:1, WEDNESDAY:2, THURSDAY:3, FRIDAY:4, SATURDAY:5,
          Lundi:0, Mardi:1, Mercredi:2, Jeudi:3, Vendredi:4, Samedi:5
        };
        this.events = (entries || []).map(e => {
          const startH = e.startTime ? parseInt(e.startTime.split(':')[0]) : 8;
          const endH   = e.endTime   ? parseInt(e.endTime.split(':')[0])   : startH + 2;
          const typeKey = (e as any).type || 'CM';
          const dayIdx  = dayMap[e.dayOfWeek || ''] ?? 0;
          const eventDate = new Date(this.currentWeekStart);
          eventDate.setDate(eventDate.getDate() + dayIdx);
          return {
            id: e.id, subject: e.courseName || e.title || 'Cours',
            room: e.room || '', professor: e.teacher || '',
            type: typeKey as CalendarEvent['type'],
            color: this.typeColors[typeKey] || '#1D4ED8',
            dayIndex: dayIdx, startHour: startH, endHour: endH,
            date: eventDate
          };
        });
        this.isLoading = false;
      },
      error: () => { this.events = []; this.isLoading = false; }
    });
  }

  // ── Navigation ────────────────────────────────────────────────────────────

  prev(): void {
    if (this.selectedView === 'Semaine') {
      this.currentWeekStart = new Date(this.currentWeekStart);
      this.currentWeekStart.setDate(this.currentWeekStart.getDate() - 7);
      this.days = this.buildWeekDays(this.currentWeekStart);
    } else if (this.selectedView === 'Jour') {
      this.currentDayDate = new Date(this.currentDayDate);
      this.currentDayDate.setDate(this.currentDayDate.getDate() - 1);
    } else {
      this.currentMonthDate = new Date(this.currentMonthDate);
      this.currentMonthDate.setMonth(this.currentMonthDate.getMonth() - 1);
      this.buildMonthGrid();
    }
  }

  next(): void {
    if (this.selectedView === 'Semaine') {
      this.currentWeekStart = new Date(this.currentWeekStart);
      this.currentWeekStart.setDate(this.currentWeekStart.getDate() + 7);
      this.days = this.buildWeekDays(this.currentWeekStart);
    } else if (this.selectedView === 'Jour') {
      this.currentDayDate = new Date(this.currentDayDate);
      this.currentDayDate.setDate(this.currentDayDate.getDate() + 1);
    } else {
      this.currentMonthDate = new Date(this.currentMonthDate);
      this.currentMonthDate.setMonth(this.currentMonthDate.getMonth() + 1);
      this.buildMonthGrid();
    }
  }

  goToday(): void {
    this.currentWeekStart = this.getMonday(new Date());
    this.currentDayDate   = new Date();
    this.currentMonthDate = new Date();
    this.days = this.buildWeekDays(this.currentWeekStart);
    this.buildMonthGrid();
  }

  setView(v: ViewMode): void { this.selectedView = v; }

  // ── Labels ────────────────────────────────────────────────────────────────

  get periodLabel(): string {
    const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'long', year: 'numeric' };
    if (this.selectedView === 'Semaine') {
      const end = new Date(this.currentWeekStart);
      end.setDate(end.getDate() + 5);
      return `${this.currentWeekStart.toLocaleDateString('fr-FR', { day:'numeric', month:'long' })} – ${end.toLocaleDateString('fr-FR', opts)}`;
    }
    if (this.selectedView === 'Jour') {
      return this.currentDayDate.toLocaleDateString('fr-FR', { weekday:'long', day:'numeric', month:'long', year:'numeric' });
    }
    return this.currentMonthDate.toLocaleDateString('fr-FR', { month:'long', year:'numeric' });
  }

  // ── Filtered events ───────────────────────────────────────────────────────

  get filteredEvents(): CalendarEvent[] {
    if (this.selectedTeacher && this.selectedTeacher !== 'Tous les enseignants') {
      return this.events.filter(e => e.professor === this.selectedTeacher);
    }
    return this.events;
  }

  // Vue semaine
  getEventsForDayAndHour(dayIndex: number, hour: number): CalendarEvent[] {
    return this.filteredEvents.filter(e => e.dayIndex === dayIndex && e.startHour === hour);
  }

  // Vue jour
  getEventsForHour(hour: number): CalendarEvent[] {
    const d = this.currentDayDate;
    return this.filteredEvents.filter(e => {
      if (e.startHour !== hour) return false;
      if (!e.date) return true;
      return e.date.toDateString() === d.toDateString();
    });
  }

  // Vue mois
  getEventsForDate(date: Date): CalendarEvent[] {
    return this.filteredEvents.filter(e => e.date && e.date.toDateString() === date.toDateString());
  }

  getEventHeight(event: CalendarEvent): number { return (event.endHour - event.startHour) * 60; }

  // ── Sync ──────────────────────────────────────────────────────────────────

  synchronize(): void {
    if (this.isSyncing) return;
    this.isSyncing = true;
    this.loadSchedule();
    setTimeout(() => {
      this.isSyncing = false;
      this.syncMessage = 'Calendrier synchronisé';
      this.showSyncToast = true;
      setTimeout(() => this.showSyncToast = false, 4500);
    }, 1000);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday:'long', day:'numeric', month:'long', year:'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour:'2-digit', minute:'2-digit' });
  }

  private getMonday(d: Date): Date {
    const date = new Date(d);
    const day = date.getDay();
    const diff = day === 0 ? -6 : 1 - day;
    date.setDate(date.getDate() + diff);
    date.setHours(0,0,0,0);
    return date;
  }

  private buildWeekDays(monday: Date): { label: string; date: string; dateObj: Date; isToday: boolean }[] {
    const labels = ['Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'];
    const today = new Date(); today.setHours(0,0,0,0);
    return labels.map((label, i) => {
      const d = new Date(monday);
      d.setDate(d.getDate() + i);
      return { label, date: d.toLocaleDateString('fr-FR', { day:'numeric', month:'short' }), dateObj: d, isToday: d.getTime() === today.getTime() };
    });
  }

  private buildMonthGrid(): void {
    const year  = this.currentMonthDate.getFullYear();
    const month = this.currentMonthDate.getMonth();
    const first = new Date(year, month, 1);
    const last  = new Date(year, month + 1, 0);
    const today = new Date(); today.setHours(0,0,0,0);

    // Start from Monday of the first week
    const start = this.getMonday(first);
    this.monthWeeks = [];
    let week: { date: Date; isCurrentMonth: boolean; isToday: boolean }[] = [];
    const cur = new Date(start);

    while (cur <= last || week.length > 0) {
      week.push({ date: new Date(cur), isCurrentMonth: cur.getMonth() === month, isToday: cur.getTime() === today.getTime() });
      cur.setDate(cur.getDate() + 1);
      if (week.length === 7) { this.monthWeeks.push(week); week = []; }
      if (cur > last && week.length === 0) break;
    }
    if (week.length > 0) {
      while (week.length < 7) { week.push({ date: new Date(cur), isCurrentMonth: false, isToday: false }); cur.setDate(cur.getDate()+1); }
      this.monthWeeks.push(week);
    }
  }

  get monthName(): string {
    return this.currentMonthDate.toLocaleDateString('fr-FR', { month:'long', year:'numeric' });
  }

  isCurrentHour(hour: number): boolean {
    return new Date().getHours() === hour;
  }
}
