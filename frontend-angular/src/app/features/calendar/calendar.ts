import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

export interface CalendarEvent {
  id: string;
  title: string;
  date: string;
  startTime: string;
  endTime: string;
  room: string;
  professor: string;
  group: string;
  type: 'Cours' | 'Examen' | 'Réunion' | 'Événement';
}

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './calendar.html',
  styleUrl: './calendar.css'
})
export class CalendarComponent {
  currentDate = new Date(2025, 9, 18);
  selectedDate: Date | null = null;

  weekDays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  legendTypes = ['Cours', 'Examen', 'Réunion', 'Événement'];

  events: CalendarEvent[] = [
    { id: '1', title: 'Mathématiques', date: '2025-10-20', startTime: '08:00', endTime: '10:00', room: 'A101', professor: 'Dr. Martin', group: 'L1-G1', type: 'Cours' },
    { id: '2', title: 'Physique', date: '2025-10-20', startTime: '10:00', endTime: '12:00', room: 'B203', professor: 'Dr. Laurent', group: 'L1-G1', type: 'Cours' },
    { id: '3', title: 'Examen Algèbre', date: '2025-10-21', startTime: '09:00', endTime: '11:00', room: 'C301', professor: 'Prof. Dubois', group: 'L2-G2', type: 'Examen' },
    { id: '4', title: 'Réunion pédagogique', date: '2025-10-22', startTime: '14:00', endTime: '16:00', room: 'Salle réunion', professor: 'Admin Système', group: 'Tous', type: 'Réunion' },
    { id: '5', title: 'Conférence IA', date: '2025-10-23', startTime: '10:00', endTime: '12:00', room: 'Amphi A', professor: 'Prof. Richard', group: 'M1', type: 'Événement' },
  ];

  get monthName(): string {
    return this.currentDate.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
  }

  get daysInMonth(): number {
    return new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 0).getDate();
  }

  get emptyDays(): number[] {
    const firstDay = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), 1).getDay();
    const adjusted = firstDay === 0 ? 6 : firstDay - 1;
    return Array.from({ length: adjusted });
  }

  get daysArray(): number[] {
    return Array.from({ length: this.daysInMonth }, (_, i) => i + 1);
  }

  get selectedDateLabel(): string {
    if (!this.selectedDate) return 'Sélectionnez une date';
    return this.selectedDate.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' });
  }

  get selectedDateEvents(): CalendarEvent[] {
    if (!this.selectedDate) return [];
    return this.getEventsForDay(this.selectedDate.getDate());
  }

  prevMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    this.selectedDate = null;
  }

  nextMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    this.selectedDate = null;
  }

  goToToday(): void { this.currentDate = new Date(); this.selectedDate = null; }

  selectDate(day: number): void {
    this.selectedDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), day);
  }

  getEventsForDay(day: number): CalendarEvent[] {
    const dateStr = `${this.currentDate.getFullYear()}-${String(this.currentDate.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    return this.events.filter(e => e.date === dateStr);
  }

  isSelected(day: number): boolean {
    return this.selectedDate?.getDate() === day &&
           this.selectedDate?.getMonth() === this.currentDate.getMonth() &&
           this.selectedDate?.getFullYear() === this.currentDate.getFullYear();
  }

  isToday(day: number): boolean {
    const today = new Date();
    return day === today.getDate() &&
           this.currentDate.getMonth() === today.getMonth() &&
           this.currentDate.getFullYear() === today.getFullYear();
  }

  getTypeClass(type: string): string {
    switch (type) {
      case 'Cours':     return 'badge-blue';
      case 'Examen':    return 'badge-red';
      case 'Réunion':   return 'badge-purple';
      case 'Événement': return 'badge-orange';
      default:          return 'badge-gray';
    }
  }

  getLegendClass(type: string): string {
    switch (type) {
      case 'Cours':     return 'dot-blue';
      case 'Examen':    return 'dot-red';
      case 'Réunion':   return 'dot-purple';
      case 'Événement': return 'dot-orange';
      default:          return 'dot-gray';
    }
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'Cours':     return 'menu_book';
      case 'Examen':    return 'assignment';
      case 'Réunion':   return 'groups';
      case 'Événement': return 'event';
      default:          return 'calendar_today';
    }
  }
}