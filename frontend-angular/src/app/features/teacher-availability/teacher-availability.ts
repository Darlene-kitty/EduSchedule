import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../../environments/environment';

export enum DayOfWeek {
  MONDAY = 'MONDAY', TUESDAY = 'TUESDAY', WEDNESDAY = 'WEDNESDAY',
  THURSDAY = 'THURSDAY', FRIDAY = 'FRIDAY', SATURDAY = 'SATURDAY', SUNDAY = 'SUNDAY'
}

export enum AvailabilityStatus {
  ACTIVE = 'ACTIVE', INACTIVE = 'INACTIVE', PENDING = 'PENDING'
}

export interface TimeSlot {
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
}

export interface TeacherAvailability {
  id: number;
  teacherId: number;
  teacherName: string;
  effectiveDate: string;
  endDate: string;
  availableSlots: TimeSlot[];
  status: AvailabilityStatus;
  maxHoursPerDay: number;
  maxHoursPerWeek: number;
  notes: string;
}

@Component({
  selector: 'app-teacher-availability',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  templateUrl: './teacher-availability.html',
  styleUrl: './teacher-availability.css'
})
export class TeacherAvailabilityComponent implements OnInit {
  availabilities: TeacherAvailability[] = [];
  filteredAvailabilities: TeacherAvailability[] = [];
  loading = true;
  error: string | null = null;
  message = '';
  isError = false;

  searchQuery = '';
  selectedDay = 'all';
  viewMode: 'calendar' | 'list' = 'list';
  isModalOpen = false;
  editingId: number | null = null;

  days = Object.values(DayOfWeek);
  statuses = Object.values(AvailabilityStatus);

  hours = Array.from({ length: 11 }, (_, i) => `${String(i + 8).padStart(2, '0')}:00`);
  weekDays = [
    { key: DayOfWeek.MONDAY, label: 'Lundi' },
    { key: DayOfWeek.TUESDAY, label: 'Mardi' },
    { key: DayOfWeek.WEDNESDAY, label: 'Mercredi' },
    { key: DayOfWeek.THURSDAY, label: 'Jeudi' },
    { key: DayOfWeek.FRIDAY, label: 'Vendredi' },
  ];

  newAvailability: Partial<TeacherAvailability> = {
    teacherName: '', effectiveDate: '', endDate: '',
    status: AvailabilityStatus.ACTIVE,
    maxHoursPerDay: 8, maxHoursPerWeek: 40,
    notes: '', availableSlots: []
  };

  newSlot: TimeSlot = { dayOfWeek: DayOfWeek.MONDAY, startTime: '08:00', endTime: '12:00' };

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.loadAvailabilities(); }

  loadAvailabilities(): void {
    this.loading = true;
    const mock: TeacherAvailability[] = [
      {
        id: 1, teacherId: 1, teacherName: 'Dr. Martin',
        effectiveDate: '2025-10-01', endDate: '2025-12-31',
        availableSlots: [
          { dayOfWeek: DayOfWeek.MONDAY, startTime: '08:00', endTime: '12:00' },
          { dayOfWeek: DayOfWeek.MONDAY, startTime: '14:00', endTime: '18:00' },
          { dayOfWeek: DayOfWeek.WEDNESDAY, startTime: '08:00', endTime: '12:00' },
        ],
        status: AvailabilityStatus.ACTIVE,
        maxHoursPerDay: 8, maxHoursPerWeek: 35,
        notes: 'Disponible toute la semaine sauf vendredi après-midi'
      },
      {
        id: 2, teacherId: 2, teacherName: 'Dr. Laurent',
        effectiveDate: '2025-10-01', endDate: '2025-12-31',
        availableSlots: [
          { dayOfWeek: DayOfWeek.TUESDAY, startTime: '10:00', endTime: '14:00' },
          { dayOfWeek: DayOfWeek.THURSDAY, startTime: '08:00', endTime: '12:00' },
        ],
        status: AvailabilityStatus.ACTIVE,
        maxHoursPerDay: 6, maxHoursPerWeek: 30,
        notes: 'Préfère les matinées'
      },
      {
        id: 3, teacherId: 3, teacherName: 'Prof. Dubois',
        effectiveDate: '2025-10-01', endDate: '2025-12-31',
        availableSlots: [
          { dayOfWeek: DayOfWeek.FRIDAY, startTime: '08:00', endTime: '12:00' },
        ],
        status: AvailabilityStatus.INACTIVE,
        maxHoursPerDay: 4, maxHoursPerWeek: 20,
        notes: 'En congé partiel'
      },
    ];
    this.availabilities = mock;
    this.applyFilters();
    this.loading = false;
  }

  applyFilters(): void {
    this.filteredAvailabilities = this.availabilities.filter(a => {
      const matchesSearch = !this.searchQuery ||
        a.teacherName.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchesDay = this.selectedDay === 'all' ||
        a.availableSlots.some(s => s.dayOfWeek === this.selectedDay);
      return matchesSearch && matchesDay;
    });
  }

  onSearchChange(): void { this.applyFilters(); }
  onDayChange(day: string): void { this.selectedDay = day; this.applyFilters(); }

  openAddModal(): void {
    this.editingId = null;
    this.newAvailability = {
      teacherName: '', effectiveDate: '', endDate: '',
      status: AvailabilityStatus.ACTIVE,
      maxHoursPerDay: 8, maxHoursPerWeek: 40,
      notes: '', availableSlots: []
    };
    this.isModalOpen = true;
  }

  openEditModal(a: TeacherAvailability): void {
    this.editingId = a.id;
    this.newAvailability = { ...a, availableSlots: [...a.availableSlots] };
    this.isModalOpen = true;
  }

  closeModal(): void { this.isModalOpen = false; }

  addSlot(): void {
    if (!this.newAvailability.availableSlots) this.newAvailability.availableSlots = [];
    this.newAvailability.availableSlots.push({ ...this.newSlot });
  }

  removeSlot(index: number): void {
    this.newAvailability.availableSlots?.splice(index, 1);
  }

  saveAvailability(): void {
    if (this.editingId) {
      this.availabilities = this.availabilities.map(a =>
        a.id === this.editingId ? { ...a, ...this.newAvailability } as TeacherAvailability : a
      );
      this.message = 'Disponibilité modifiée avec succès';
    } else {
      const newItem: TeacherAvailability = {
        ...this.newAvailability as TeacherAvailability,
        id: Date.now(),
        teacherId: Date.now()
      };
      this.availabilities = [...this.availabilities, newItem];
      this.message = 'Disponibilité ajoutée avec succès';
    }
    this.isError = false;
    this.applyFilters();
    this.closeModal();
  }

  deleteAvailability(id: number): void {
    if (!confirm('Supprimer cette disponibilité ?')) return;
    this.availabilities = this.availabilities.filter(a => a.id !== id);
    this.applyFilters();
    this.message = 'Disponibilité supprimée';
    this.isError = false;
  }

  getSlotsForDay(teacherSlots: TimeSlot[], day: DayOfWeek): TimeSlot[] {
    return teacherSlots.filter(s => s.dayOfWeek === day);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':   return 'badge-green';
      case 'INACTIVE': return 'badge-gray';
      case 'PENDING':  return 'badge-yellow';
      default:         return 'badge-gray';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE':   return 'Actif';
      case 'INACTIVE': return 'Inactif';
      case 'PENDING':  return 'En attente';
      default:         return status;
    }
  }

  getDayLabel(day: string): string {
    const map: Record<string, string> = {
      MONDAY: 'Lundi', TUESDAY: 'Mardi', WEDNESDAY: 'Mercredi',
      THURSDAY: 'Jeudi', FRIDAY: 'Vendredi', SATURDAY: 'Samedi', SUNDAY: 'Dimanche'
    };
    return map[day] || day;
  }

  get totalSlots(): number {
    return this.availabilities.reduce((sum, a) => sum + a.availableSlots.length, 0);
  }

  get totalActive(): number {
    return this.availabilities.filter(a => a.status === AvailabilityStatus.ACTIVE).length;
  }
}