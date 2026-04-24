import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { TeacherAvailabilityManagementService, TeacherAvailabilityEntry } from '../../core/services/teacher-availability-management.service';
import { AuthService } from '../../core/services/auth.service';
import { UsersManagementService } from '../../core/services/users-management.service';
import { AppConfigService } from '../../core/services/app-config.service';

export type TimeSlot = import('../../core/services/teacher-availability-management.service').TimeSlot;
export type TeacherAvailability = TeacherAvailabilityEntry;

@Component({
  selector: 'app-teacher-availability',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './teacher-availability.html',
  styleUrl: './teacher-availability.css'
})
export class TeacherAvailabilityComponent implements OnInit {
  private availabilityService = inject(TeacherAvailabilityManagementService);
  private authService         = inject(AuthService);
  private usersSvc            = inject(UsersManagementService);
  private configSvc           = inject(AppConfigService);

  // Liste des enseignants pour le select admin
  teacherList: { id: number; name: string }[] = [];

  isTeacher = false;
  currentTeacherName = '';

  currentDate = ''; currentTime = '';
  searchQuery = '';
  selectedDay = 'all';
  selectedTeacher = 'all';
  viewMode: 'calendar' | 'list' = 'calendar';
  isAddModalOpen = false;
  isLoading = false;
  editingAvailability: TeacherAvailability | null = null;

  /** Peuplés depuis le backend via AppConfigService */
  days:  { key: string; label: string }[] = [];
  hours: string[] = [];

  availabilities: TeacherAvailability[] = [];

  newSlots: TimeSlot[] = [];
  newForm = { teacherName: '', effectiveDate: '', maxHoursPerDay: 8, maxHoursPerWeek: 40, status: 'active', notes: '' };

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);

    const user = this.authService.getUser();
    this.isTeacher = this.authService.isTeacher();
    this.currentTeacherName = user?.name || user?.username || '';

    this.loadAvailabilities();

    // Charger jours et heures depuis le backend
    this.configSvc.getConfig().subscribe(cfg => {
      this.days  = cfg.workDays ?? [];
      // Filtrer les heures pour n'afficher que 08:00–18:00 (sans 07:00 et 19:00)
      this.hours = (cfg.workHours ?? []).filter(h => h >= '08:00' && h <= '18:00');
    });

    // Charger la liste des enseignants pour le select (admin uniquement)
    if (!this.isTeacher) {
      this.usersSvc.getUsers().subscribe({
        next: users => {
          this.teacherList = users
            .filter(u => (u.role || '').toUpperCase().includes('TEACHER'))
            .map(u => ({ id: u.id, name: u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || '' }))
            .filter(t => t.name);
        },
        error: () => {}
      });
    }
  }

  loadAvailabilities(): void {
    this.isLoading = true;
    this.availabilityService.getAll().subscribe({
      next: (data) => {
        const all = data || [];
        // Un enseignant ne voit que ses propres disponibilités
        this.availabilities = this.isTeacher
          ? all.filter(a => a.teacherName === this.currentTeacherName)
          : all;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement disponibilités:', err?.error?.message || err);
        this.availabilities = [];
        this.isLoading = false;
      }
    });
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

  get teacherNames(): string[] {
    return [...new Set(this.availabilities.map(a => a.teacherName))].sort();
  }

  get filteredAvailabilities(): TeacherAvailability[] {
    return this.availabilities.filter(a => {
      const matchSearch = !this.searchQuery ||
        a.teacherName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        (a.notes || '').toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchDay = this.selectedDay === 'all' || a.slots.some(s => s.day === this.selectedDay);
      const matchTeacher = this.isTeacher || this.selectedTeacher === 'all' || a.teacherName === this.selectedTeacher;
      return matchSearch && matchDay && matchTeacher;
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
    this.newForm = {
      teacherName: this.isTeacher ? this.currentTeacherName : '',
      effectiveDate: '', maxHoursPerDay: 8, maxHoursPerWeek: 40, status: 'active', notes: ''
    };
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
      const id = this.editingAvailability.id;
      this.availabilityService.update(id, { ...this.newForm, slots: this.newSlots, status: this.newForm.status as any }).subscribe({
        next: (updated) => {
          const idx = this.availabilities.findIndex(a => a.id === id);
          if (idx >= 0) this.availabilities[idx] = updated;
          this.closeModal();
        },
        error: (err) => alert(err?.error?.message || 'Erreur lors de la modification')
      });
    } else {
      this.availabilityService.create({ ...this.newForm, slots: this.newSlots, status: this.newForm.status as any }).subscribe({
        next: (created) => { this.availabilities.push(created); this.closeModal(); },
        error: (err) => alert(err?.error?.message || 'Erreur lors de la création')
      });
    }
  }

  deleteAvailability(id: number): void {
    if (confirm('Supprimer cette disponibilité ?')) {
      this.availabilityService.delete(id).subscribe({
        next: () => { this.availabilities = this.availabilities.filter(a => a.id !== id); },
        error: (err) => alert(err?.error?.message || 'Erreur lors de la suppression')
      });
    }
  }
}