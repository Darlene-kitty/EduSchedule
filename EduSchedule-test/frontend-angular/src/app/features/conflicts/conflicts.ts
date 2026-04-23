import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import {
  ConflictsManagementService,
  PendingAdjustment,
  AlternativeSuggestion
} from '../../core/services/conflicts-management.service';
import { WebSocketService } from '../../core/services/websocket.service';

export interface ConflictItem { name: string; professor: string; students: number; }
export interface Solution { room: string; available: boolean; slotKey?: string; score?: number; }

export interface Conflict {
  id: number;
  slotId: number;
  title: string;
  description: string;
  priority: 'urgent' | 'normal';
  date: string;
  time: string;
  items: ConflictItem[];
  solutions: Solution[];
  resolved: boolean;
  loadingSuggestions: boolean;
}

@Component({
  selector: 'app-conflicts',
  standalone: true,
  imports: [CommonModule, MatIconModule, SidebarComponent],
  templateUrl: './conflicts.html',
  styleUrl: './conflicts.css'
})
export class ConflictsComponent implements OnInit {
  private conflictsSvc  = inject(ConflictsManagementService);
  private wsSvc         = inject(WebSocketService);

  currentDate = ''; currentTime = '';
  isLoading = false;

  stats = [
    { label: 'Conflits actifs',      value: '0', color: 'red'    },
    { label: "Résolus aujourd'hui",  value: '0', color: 'green'  },
    { label: 'Haute priorité',       value: '0', color: 'orange' },
    { label: 'Taux de résolution',   value: '—', color: 'blue'   },
  ];

  conflicts: Conflict[] = [];
  resolvedToday = 0;

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadConflicts();
    this.subscribeToLiveAlerts();
  }

  /** Charge les créneaux en conflit/relaxés depuis le backend */
  loadConflicts(): void {
    this.isLoading = true;
    this.conflictsSvc.getAllAdjustments().subscribe({
      next: (adjustments) => {
        this.conflicts = adjustments.map((adj, i) => this.mapAdjustmentToConflict(adj, i + 1));
        this.updateStats();
        this.isLoading = false;
        // Charger les suggestions pour chaque conflit
        this.conflicts.forEach(c => this.loadSuggestions(c));
      },
      error: () => {
        this.conflicts = [];
        this.updateStats();
        this.isLoading = false;
      }
    });
  }

  /** Charge les suggestions alternatives dynamiques pour un conflit */
  loadSuggestions(conflict: Conflict): void {
    if (!conflict.slotId) return;
    conflict.loadingSuggestions = true;
    this.conflictsSvc.getSuggestions(conflict.slotId).subscribe({
      next: (suggestions: AlternativeSuggestion[]) => {
        conflict.solutions = suggestions.map(s => ({
          room: s.label,
          available: s.fullyAvailable,
          slotKey: s.slotKey,
          score: s.score
        }));
        conflict.loadingSuggestions = false;
      },
      error: () => {
        conflict.loadingSuggestions = false;
      }
    });
  }

  /** Écoute les alertes de conflit en temps réel via WebSocket */
  private subscribeToLiveAlerts(): void {
    this.wsSvc.connect();
    this.wsSvc.notificationsOfType$('INTER_SCHOOL_CONFLICT_RELAXED').subscribe(() => {
      this.loadConflicts();
    });
    this.wsSvc.notificationsOfType$('INTER_SCHOOL_CONFLICT_REASSIGNED').subscribe(() => {
      this.loadConflicts();
    });
    this.wsSvc.notificationsOfType$('CONFLICT').subscribe(() => {
      this.loadConflicts();
    });
  }

  private mapAdjustmentToConflict(adj: PendingAdjustment, index: number): Conflict {
    const dayFr: Record<string, string> = {
      LUNDI: 'Lundi', MARDI: 'Mardi', MERCREDI: 'Mercredi',
      JEUDI: 'Jeudi', VENDREDI: 'Vendredi', SAMEDI: 'Samedi'
    };
    const isRelaxed = adj.status === 'RELAXED';
    return {
      id: index,
      slotId: adj.id,
      title: isRelaxed
        ? `Créneau maintenu par relaxation — ${adj.courseCode}`
        : `Conflit de disponibilité — ${adj.courseCode}`,
      description: adj.conflictReason || (isRelaxed
        ? 'Ce créneau est maintenu mais viole les disponibilités de l\'enseignant.'
        : 'Ce créneau est en conflit avec les disponibilités de l\'enseignant.'),
      priority: isRelaxed ? 'normal' : 'urgent',
      date: dayFr[adj.dayOfWeek] || adj.dayOfWeek,
      time: `${adj.startTime} – ${adj.endTime}`,
      items: [{
        name: adj.courseName,
        professor: `Enseignant #${adj.teacherId}`,
        students: 0
      }],
      solutions: [],
      resolved: false,
      loadingSuggestions: false
    };
  }

  private updateStats(): void {
    const active = this.conflicts.filter(c => !c.resolved).length;
    const urgent = this.conflicts.filter(c => c.priority === 'urgent' && !c.resolved).length;
    const total  = this.conflicts.length + this.resolvedToday;
    const rate   = total > 0 ? Math.round((this.resolvedToday / total) * 100) + '%' : '—';
    this.stats[0].value = String(active);
    this.stats[1].value = String(this.resolvedToday);
    this.stats[2].value = String(urgent);
    this.stats[3].value = rate;
  }

  applySolution(conflict: Conflict, solution: Solution): void {
    conflict.resolved = true;
    this.resolvedToday++;
    this.conflicts = this.conflicts.filter(c => c.id !== conflict.id);
    this.updateStats();
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', {
      weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }
}
