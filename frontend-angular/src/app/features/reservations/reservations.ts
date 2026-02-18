import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../../environments/environment';

export enum ReservationStatus {
  APPROVED = 'APPROVED',
  PENDING = 'PENDING',
  CANCELLED = 'CANCELLED',
  REJECTED = 'REJECTED'
}

export enum ReservationType {
  COURSE = 'COURSE',
  MEETING = 'MEETING',
  EVENT = 'EVENT',
  MAINTENANCE = 'MAINTENANCE'
}

export interface Reservation {
  id: number;
  purpose: string;
  type: ReservationType;
  status: ReservationStatus;
  resourceId: number;
  userId: number;
  startDateTime: string;
  endDateTime: string;
}

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  templateUrl: './reservations.html',
  styleUrl: './reservations.css'
})
export class ReservationsComponent implements OnInit {
  reservations: Reservation[] = [];
  filteredReservations: Reservation[] = [];
  currentItems: Reservation[] = [];
  loading = true;
  error: string | null = null;
  message = '';
  isError = false;

  searchQuery = '';
  selectedStatus = 'all';
  currentPage = 1;
  itemsPerPage = 12;
  totalPages = 1;
  totalItems = 0;

  statuses = Object.values(ReservationStatus);
  ReservationStatus = ReservationStatus;

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.loadReservations(); }

  loadReservations(): void {
    this.loading = true;
    this.error = null;

    const mock: Reservation[] = [
      { id: 1, purpose: 'Cours Maths', type: ReservationType.COURSE, status: ReservationStatus.APPROVED, resourceId: 1, userId: 1, startDateTime: '2025-10-20T08:00:00', endDateTime: '2025-10-20T10:00:00' },
      { id: 2, purpose: 'Réunion pédagogique', type: ReservationType.MEETING, status: ReservationStatus.PENDING, resourceId: 2, userId: 2, startDateTime: '2025-10-21T14:00:00', endDateTime: '2025-10-21T16:00:00' },
      { id: 3, purpose: 'Conférence IA', type: ReservationType.EVENT, status: ReservationStatus.PENDING, resourceId: 3, userId: 3, startDateTime: '2025-10-22T10:00:00', endDateTime: '2025-10-22T12:00:00' },
      { id: 4, purpose: 'Maintenance Labo', type: ReservationType.MAINTENANCE, status: ReservationStatus.CANCELLED, resourceId: 4, userId: 1, startDateTime: '2025-10-23T08:00:00', endDateTime: '2025-10-23T12:00:00' },
      { id: 5, purpose: 'Cours Physique', type: ReservationType.COURSE, status: ReservationStatus.APPROVED, resourceId: 1, userId: 2, startDateTime: '2025-10-24T10:00:00', endDateTime: '2025-10-24T12:00:00' },
      { id: 6, purpose: 'Séminaire', type: ReservationType.EVENT, status: ReservationStatus.REJECTED, resourceId: 5, userId: 4, startDateTime: '2025-10-25T09:00:00', endDateTime: '2025-10-25T11:00:00' },
    ];

    this.reservations = mock;
    this.applyFilters();
    this.loading = false;
  }

  applyFilters(): void {
    let result = this.reservations.filter(r => {
      const matchesStatus = this.selectedStatus === 'all' || r.status === this.selectedStatus;
      const matchesSearch = !this.searchQuery ||
        r.purpose?.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        r.id.toString().includes(this.searchQuery);
      return matchesStatus && matchesSearch;
    });
    this.filteredReservations = result;
    this.totalItems = result.length;
    this.totalPages = Math.ceil(result.length / this.itemsPerPage);
    this.currentPage = 1;
    this.updateCurrentItems();
  }

  updateCurrentItems(): void {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    this.currentItems = this.filteredReservations.slice(start, start + this.itemsPerPage);
  }

  onSearchChange(): void { this.applyFilters(); }
  onStatusChange(): void { this.applyFilters(); }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.updateCurrentItems();
  }

  nextPage(): void { this.goToPage(this.currentPage + 1); }
  prevPage(): void { this.goToPage(this.currentPage - 1); }

  get hasPrevPage(): boolean { return this.currentPage > 1; }
  get hasNextPage(): boolean { return this.currentPage < this.totalPages; }

  get pages(): number[] {
    const pages: number[] = [];
    const start = Math.max(2, this.currentPage - 2);
    const end = Math.min(this.totalPages - 1, this.currentPage + 2);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  get totalApproved(): number { return this.reservations.filter(r => r.status === ReservationStatus.APPROVED).length; }
  get totalPending(): number { return this.reservations.filter(r => r.status === ReservationStatus.PENDING).length; }
  get totalCancelled(): number { return this.reservations.filter(r => r.status === ReservationStatus.CANCELLED).length; }

  approveReservation(id: number): void {
    this.http.put(`${environment.apiUrl}/reservations/${id}/approve`, { userId: 1 }).subscribe({
      next: () => { this.isError = false; this.message = 'Réservation approuvée'; this.loadReservations(); },
      error: (err) => { this.isError = true; this.message = err.message; }
    });
  }

  cancelReservation(id: number): void {
    const reason = prompt('Raison de l\'annulation (optionnel):');
    this.http.put(`${environment.apiUrl}/reservations/${id}/cancel`, { userId: 1, reason }).subscribe({
      next: () => { this.isError = false; this.message = 'Réservation annulée'; this.loadReservations(); },
      error: (err) => { this.isError = true; this.message = err.message; }
    });
  }

  formatDateTime(dateTime: string): string {
    return new Date(dateTime).toLocaleString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'APPROVED':  return 'badge-green';
      case 'PENDING':   return 'badge-yellow';
      case 'CANCELLED': return 'badge-gray';
      case 'REJECTED':  return 'badge-red';
      default:          return 'badge-gray';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'APPROVED':  return 'Approuvée';
      case 'PENDING':   return 'En attente';
      case 'CANCELLED': return 'Annulée';
      case 'REJECTED':  return 'Rejetée';
      default:          return status;
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'APPROVED':  return 'check_circle';
      case 'PENDING':   return 'schedule';
      case 'CANCELLED': return 'cancel';
      case 'REJECTED':  return 'cancel';
      default:          return 'help';
    }
  }

  getTypeClass(type: string): string {
    switch (type) {
      case 'COURSE':      return 'badge-blue';
      case 'MEETING':     return 'badge-purple';
      case 'EVENT':       return 'badge-orange';
      case 'MAINTENANCE': return 'badge-gray';
      default:            return 'badge-gray';
    }
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'COURSE':      return 'Cours';
      case 'MEETING':     return 'Réunion';
      case 'EVENT':       return 'Événement';
      case 'MAINTENANCE': return 'Maintenance';
      default:            return type;
    }
  }
}