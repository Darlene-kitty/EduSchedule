import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

export interface Conflict {
  id: string;
  type: 'Salle' | 'Professeur' | 'Groupe' | 'Ressource';
  severity: 'Critique' | 'Élevée' | 'Moyenne';
  description: string;
  details: {
    course1: string;
    course2: string;
    time: string;
    date: string;
    location?: string;
    professor?: string;
    group?: string;
  };
  status: 'Non résolu' | 'En cours' | 'Résolu';
  detectedAt: string;
}

@Component({
  selector: 'app-conflicts',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './conflicts.html',
  styleUrl: './conflicts.css'
})
export class ConflictsComponent {
  selectedStatus = 'all';
  isResolveModalOpen = false;
  selectedConflict: Conflict | null = null;

  statuses = ['all', 'Non résolu', 'En cours', 'Résolu'];

  conflicts: Conflict[] = [
    {
      id: '1',
      type: 'Salle',
      severity: 'Critique',
      description: 'Conflit de réservation de salle',
      details: {
        course1: 'Mathématiques (L1-G1)',
        course2: 'Algèbre (L2-G2)',
        time: '10:00-12:00',
        date: '2025-10-21',
        location: 'A101'
      },
      status: 'Non résolu',
      detectedAt: 'Il y a 12 min'
    },
    {
      id: '2',
      type: 'Professeur',
      severity: 'Critique',
      description: 'Professeur assigné à deux cours simultanés',
      details: {
        course1: 'Physique (L1-G1)',
        course2: 'TP Physique (L1-G2)',
        time: '14:00-16:00',
        date: '2025-10-20',
        professor: 'Dr. Laurent'
      },
      status: 'En cours',
      detectedAt: 'Il y a 1h'
    },
    {
      id: '3',
      type: 'Groupe',
      severity: 'Élevée',
      description: "Groupe d'étudiants avec cours simultanés",
      details: {
        course1: 'Analyse (L2-G1)',
        course2: 'Statistiques (L2-G1)',
        time: '08:00-10:00',
        date: '2025-10-22',
        group: 'L2-G1'
      },
      status: 'Non résolu',
      detectedAt: 'Il y a 2h'
    }
  ];

  get filteredConflicts(): Conflict[] {
    return this.conflicts.filter(c =>
      this.selectedStatus === 'all' || c.status === this.selectedStatus
    );
  }

  get totalUnresolved(): number { return this.conflicts.filter(c => c.status === 'Non résolu').length; }
  get totalInProgress(): number { return this.conflicts.filter(c => c.status === 'En cours').length; }
  get totalResolved(): number { return this.conflicts.filter(c => c.status === 'Résolu').length; }

  setStatus(status: string): void { this.selectedStatus = status; }

  openResolveModal(conflict: Conflict): void {
    this.selectedConflict = conflict;
    this.isResolveModalOpen = true;
  }

  closeModal(): void {
    this.isResolveModalOpen = false;
    this.selectedConflict = null;
  }

  resolveConflict(conflict: Conflict): void {
    this.conflicts = this.conflicts.map(c =>
      c.id === conflict.id ? { ...c, status: 'Résolu' as const } : c
    );
    this.closeModal();
  }

  ignoreConflict(id: string): void {
    if (!confirm('Ignorer ce conflit ?')) return;
    this.conflicts = this.conflicts.filter(c => c.id !== id);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR', {
      day: 'numeric', month: 'long', year: 'numeric'
    });
  }

  getSeverityClass(severity: string): string {
    switch (severity) {
      case 'Critique': return 'severity-critical';
      case 'Élevée':   return 'severity-high';
      case 'Moyenne':  return 'severity-medium';
      default:         return 'severity-low';
    }
  }

  getTypeClass(type: string): string {
    switch (type) {
      case 'Salle':      return 'badge-blue';
      case 'Professeur': return 'badge-purple';
      case 'Groupe':     return 'badge-green';
      case 'Ressource':  return 'badge-cyan';
      default:           return 'badge-gray';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Résolu':      return 'badge-green';
      case 'En cours':    return 'badge-yellow';
      case 'Non résolu':  return 'badge-red';
      default:            return 'badge-gray';
    }
  }

  getStatusFilterLabel(s: string): string {
    return s === 'all' ? 'Tous' : s;
  }
}