import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../../environments/environment';

export interface Schedule {
  id: number;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  status: string;
  teacher: string;
  room: string;
  course: string;
  groupName: string;
}

@Component({
  selector: 'app-schedules',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  templateUrl: './schedule.html',
  styleUrl: './schedule.css'
})
export class SchedulesComponent implements OnInit {
  schedules: Schedule[] = [];
  filteredSchedules: Schedule[] = [];
  loading = true;
  error: string | null = null;
  message = '';
  isError = false;

  searchQuery = '';
  selectedFilter = 'all';
  filters = ['all', 'today', 'week', 'month', 'active'];

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.loadSchedules(); }

  loadSchedules(): void {
    this.loading = true;
    const mock: Schedule[] = [
      { id: 1, title: 'Mathématiques', description: 'Cours L1', startTime: '2025-10-20T08:00:00', endTime: '2025-10-20T10:00:00', status: 'ACTIVE', teacher: 'Dr. Martin', room: 'A101', course: 'Maths', groupName: 'L1-G1' },
      { id: 2, title: 'Physique', description: 'Cours L1', startTime: '2025-10-20T10:00:00', endTime: '2025-10-20T12:00:00', status: 'ACTIVE', teacher: 'Dr. Laurent', room: 'B203', course: 'Physique', groupName: 'L1-G1' },
      { id: 3, title: 'Informatique', description: 'Cours M1', startTime: '2025-10-21T14:00:00', endTime: '2025-10-21T16:00:00', status: 'INACTIVE', teacher: 'Prof. Dubois', room: 'Lab B1', course: 'Info', groupName: 'M1-G1' },
      { id: 4, title: 'Chimie', description: 'Cours L2', startTime: '2025-10-22T09:00:00', endTime: '2025-10-22T11:00:00', status: 'ACTIVE', teacher: 'Dr. Sophie', room: 'C301', course: 'Chimie', groupName: 'L2-G2' },
    ];
    this.schedules = mock;
    this.applyFilters();
    this.loading = false;
  }

  applyFilters(): void {
    this.filteredSchedules = this.schedules.filter(s => {
      const matchesSearch =
        s.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        s.teacher.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        s.room.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchesFilter = this.selectedFilter === 'all' ||
        (this.selectedFilter === 'active' && s.status === 'ACTIVE');
      return matchesSearch && matchesFilter;
    });
  }

  onSearchChange(): void { this.applyFilters(); }
  onFilterChange(f: string): void { this.selectedFilter = f; this.applyFilters(); }

  getFilterLabel(f: string): string {
    switch (f) {
      case 'all':    return 'Tous';
      case 'today':  return "Aujourd'hui";
      case 'week':   return 'Cette semaine';
      case 'month':  return 'Ce mois';
      case 'active': return 'Actifs';
      default:       return f;
    }
  }

  deleteSchedule(id: number): void {
    if (!confirm('Supprimer ce créneau ?')) return;
    this.schedules = this.schedules.filter(s => s.id !== id);
    this.applyFilters();
    this.message = 'Créneau supprimé';
    this.isError = false;
  }

  getStatusClass(status: string): string {
    return status === 'ACTIVE' ? 'badge-green' : 'badge-gray';
  }

  getStatusLabel(status: string): string {
    return status === 'ACTIVE' ? 'Actif' : 'Inactif';
  }

  formatDate(dt: string): string {
    return new Date(dt).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  formatTime(dt: string): string {
    return new Date(dt).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  getDuration(start: string, end: string): string {
    const diff = (new Date(end).getTime() - new Date(start).getTime()) / 60000;
    return `${diff} min`;
  }

  get totalActive(): number { return this.schedules.filter(s => s.status === 'ACTIVE').length; }
  get uniqueTeachers(): number { return new Set(this.schedules.map(s => s.teacher)).size; }
  get uniqueRooms(): number { return new Set(this.schedules.map(s => s.room)).size; }
}