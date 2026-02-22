import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

export interface TimeSlot { day: string; dayLabel: string; startTime: string; endTime: string; }
export interface TeacherAvailability {
  id: number; teacherName: string; effectiveDate: string; endDate?: string;
  slots: TimeSlot[]; status: 'active' | 'inactive' | 'pending';
  maxHoursPerDay: number; maxHoursPerWeek: number; notes?: string;
}

@Component({
  selector: 'app-teacher-availability',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './teacher-availability.html',
  styleUrl: './teacher-availability.css'
})
export class TeacherAvailabilityComponent implements OnInit {
  currentDate = ''; currentTime = '';
  searchQuery = '';
  selectedDay = 'all';
  viewMode: 'calendar' | 'list' = 'calendar';
  isAddModalOpen = false;
  editingAvailability: TeacherAvailability | null = null;

  days = [
    { key: 'MONDAY',    label: 'Lundi'    },
    { key: 'TUESDAY',   label: 'Mardi'    },
    { key: 'WEDNESDAY', label: 'Mercredi' },
    { key: 'THURSDAY',  label: 'Jeudi'    },
    { key: 'FRIDAY',    label: 'Vendredi' },
  ];

  hours = ['08:00','09:00','10:00','11:00','12:00','13:00','14:00','15:00','16:00','17:00','18:00'];

  availabilities: TeacherAvailability[] = [
    {
      id: 1, teacherName: 'Dr. Martin Dupont', effectiveDate: '2025-10-21', status: 'active',
      maxHoursPerDay: 8, maxHoursPerWeek: 40, notes: 'Disponible toute la semaine',
      slots: [
        { day: 'MONDAY',    dayLabel: 'Lundi',    startTime: '08:00', endTime: '12:00' },
        { day: 'MONDAY',    dayLabel: 'Lundi',    startTime: '14:00', endTime: '18:00' },
        { day: 'TUESDAY',   dayLabel: 'Mardi',    startTime: '09:00', endTime: '12:00' },
        { day: 'WEDNESDAY', dayLabel: 'Mercredi', startTime: '08:00', endTime: '16:00' },
        { day: 'THURSDAY',  dayLabel: 'Jeudi',    startTime: '10:00', endTime: '18:00' },
        { day: 'FRIDAY',    dayLabel: 'Vendredi', startTime: '08:00', endTime: '12:00' },
      ]
    },
    {
      id: 2, teacherName: 'Prof. Sophie Bernard', effectiveDate: '2025-10-21', status: 'active',
      maxHoursPerDay: 6, maxHoursPerWeek: 30, notes: 'Indisponible le mercredi matin',
      slots: [
        { day: 'MONDAY',   dayLabel: 'Lundi',   startTime: '10:00', endTime: '16:00' },
        { day: 'TUESDAY',  dayLabel: 'Mardi',   startTime: '08:00', endTime: '18:00' },
        { day: 'THURSDAY', dayLabel: 'Jeudi',   startTime: '09:00', endTime: '15:00' },
        { day: 'FRIDAY',   dayLabel: 'Vendredi',startTime: '10:00', endTime: '16:00' },
      ]
    },
    {
      id: 3, teacherName: 'Dr. Claire Dubois', effectiveDate: '2025-10-21', status: 'pending',
      maxHoursPerDay: 4, maxHoursPerWeek: 20,
      slots: [
        { day: 'MONDAY',    dayLabel: 'Lundi',    startTime: '09:00', endTime: '13:00' },
        { day: 'WEDNESDAY', dayLabel: 'Mercredi', startTime: '14:00', endTime: '18:00' },
        { day: 'FRIDAY',    dayLabel: 'Vendredi', startTime: '09:00', endTime: '13:00' },
      ]
    },
  ];

  newSlots: TimeSlot[] = [];
  newForm = { teacherName: '', effectiveDate: '', maxHoursPerDay: 8, maxHoursPerWeek: 40, status: 'active', notes: '' };

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  get stats() {
    return [
      { label: 'Total disponibilités', value: this.availabilities.length, icon: 'calendar_today', color: '#3B82F6' },
      { label: 'Créneaux actifs',      value: this.availabilities.filter(a => a.status === 'active').length, icon: 'star', color: '#15803D' },
      { label: 'Total créneaux',       value: this.availabilities.reduce((s,a) => s + a.slots.length, 0), icon: 'schedule', color: '#8B5CF6' },
      { label: 'Heures/semaine moy.',  value: Math.round(this.availabilities.reduce((s,a) => s + a.maxHoursPerWeek, 0) / (this.availabilities.length || 1)), icon: 'check_circle', color: '#F97316' },
    ];
  }

  get filteredAvailabilities(): TeacherAvailability[] {
    return this.availabilities.filter(a => {
      const matchSearch = !this.searchQuery ||
        a.teacherName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        (a.notes || '').toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchDay = this.selectedDay === 'all' || a.slots.some(s => s.day === this.selectedDay);
      return matchSearch && matchDay;
    });
  }

  getSlotsForDayAndHour(availability: TeacherAvailability, day: string, hour: string): boolean {
    return availability.slots.some(s => s.day === day && s.startTime <= hour && s.endTime > hour);
  }

  getCalendarSlots(day: string, hour: string): { name: string; color: string }[] {
    return this.filteredAvailabilities
      .filter(a => this.getSlotsForDayAndHour(a, day, hour))
      .map((a, i) => ({ name: a.teacherName.split(' ').pop() || '', color: ['#3B82F6','#15803D','#8B5CF6','#F97316'][i % 4] }));
  }

  getStatusLabel(status: string): string {
    return status === 'active' ? 'Actif' : status === 'inactive' ? 'Inactif' : 'En attente';
  }

  getStatusClass(status: string): string {
    return status === 'active' ? 'status-active' : status === 'inactive' ? 'status-inactive' : 'status-pending';
  }

  openAdd(): void {
    this.editingAvailability = null;
    this.newSlots = [{ day: 'MONDAY', dayLabel: 'Lundi', startTime: '08:00', endTime: '12:00' }];
    this.newForm = { teacherName: '', effectiveDate: '', maxHoursPerDay: 8, maxHoursPerWeek: 40, status: 'active', notes: '' };
    this.isAddModalOpen = true;
  }

  openEdit(a: TeacherAvailability): void {
    this.editingAvailability = a;
    this.newSlots = [...a.slots.map(s => ({ ...s }))];
    this.newForm = { teacherName: a.teacherName, effectiveDate: a.effectiveDate, maxHoursPerDay: a.maxHoursPerDay, maxHoursPerWeek: a.maxHoursPerWeek, status: a.status, notes: a.notes || '' };
    this.isAddModalOpen = true;
  }

  closeModal(): void { this.isAddModalOpen = false; }

  addSlot(): void {
    this.newSlots.push({ day: 'MONDAY', dayLabel: 'Lundi', startTime: '08:00', endTime: '12:00' });
  }

  removeSlot(i: number): void { this.newSlots.splice(i, 1); }

  updateSlotDay(slot: TimeSlot, day: string): void {
    slot.day = day;
    slot.dayLabel = this.days.find(d => d.key === day)?.label || day;
  }

  handleSave(): void {
    if (this.editingAvailability) {
      const idx = this.availabilities.findIndex(a => a.id === this.editingAvailability!.id);
      if (idx >= 0) this.availabilities[idx] = { ...this.editingAvailability, ...this.newForm, slots: this.newSlots, status: this.newForm.status as any };
    } else {
      this.availabilities.push({ id: Date.now(), ...this.newForm, slots: this.newSlots, status: this.newForm.status as any });
    }
    this.closeModal();
  }

  deleteAvailability(id: number): void {
    if (confirm('Supprimer cette disponibilité ?'))
      this.availabilities = this.availabilities.filter(a => a.id !== id);
  }
}