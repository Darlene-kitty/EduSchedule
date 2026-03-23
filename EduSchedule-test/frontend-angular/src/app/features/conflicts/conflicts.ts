import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { ScheduleManagementService, ScheduleEntry } from '../../core/services/schedule-management.service';

export interface ConflictItem { name: string; professor: string; students: number; }
export interface Solution { room: string; available: boolean; }
export interface Conflict {
  id: number; title: string; description: string;
  priority: 'urgent' | 'normal'; date: string; time: string;
  items: ConflictItem[]; solutions: Solution[]; resolved: boolean;
}

@Component({
  selector: 'app-conflicts',
  standalone: true,
  imports: [CommonModule, MatIconModule, SidebarComponent],
  templateUrl: './conflicts.html',
  styleUrl: './conflicts.css'
})
export class ConflictsComponent implements OnInit {
  private scheduleService = inject(ScheduleManagementService);

  currentDate = ''; currentTime = '';
  isLoading = false;

  stats = [
    { label: 'Conflits actifs',      value: '0',   color: 'red'    },
    { label: "Résolus aujourd'hui",  value: '0',   color: 'green'  },
    { label: 'Haute priorité',       value: '0',   color: 'orange' },
    { label: 'Taux de résolution',   value: '—',   color: 'blue'   },
  ];

  conflicts: Conflict[] = [];

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadConflicts();
  }

  loadConflicts(): void {
    this.isLoading = true;
    // On charge toutes les séances et on détecte les conflits localement
    this.scheduleService.getSchedule().subscribe({
      next: (entries) => {
        this.conflicts = this.detectConflicts(entries);
        this.updateStats();
        this.isLoading = false;
      },
      error: () => {
        // fallback démo
        this.conflicts = this.getDemoConflicts();
        this.updateStats();
        this.isLoading = false;
      }
    });
  }

  private detectConflicts(entries: ScheduleEntry[]): Conflict[] {
    const result: Conflict[] = [];
    for (let i = 0; i < entries.length; i++) {
      for (let j = i + 1; j < entries.length; j++) {
        const a = entries[i], b = entries[j];
        if (a.dayOfWeek !== b.dayOfWeek) continue;
        if (!this.overlaps(a.startTime, a.endTime, b.startTime, b.endTime)) continue;
        if (a.room && a.room === b.room) {
          result.push({
            id: result.length + 1,
            title: `Double réservation — ${a.room}`,
            description: `Deux cours planifiés dans la même salle au même horaire`,
            priority: 'urgent',
            date: a.dayOfWeek != null ? String(a.dayOfWeek) : '',
            time: `${a.startTime} - ${a.endTime}`,
            items: [
              { name: a.courseName || a.title || `Cours #${a.id}`, professor: a.teacher || '', students: 0 },
              { name: b.courseName || b.title || `Cours #${b.id}`, professor: b.teacher || '', students: 0 },
            ],
            solutions: [],
            resolved: false
          });
        }
      }
    }
    return result;
  }

  private overlaps(s1: string, e1: string, s2: string, e2: string): boolean {
    return s1 < e2 && s2 < e1;
  }

  private updateStats(): void {
    const active = this.conflicts.filter(c => !c.resolved).length;
    const urgent = this.conflicts.filter(c => c.priority === 'urgent' && !c.resolved).length;
    this.stats[0].value = String(active);
    this.stats[2].value = String(urgent);
  }

  private getDemoConflicts(): Conflict[] {
    return [
      {
        id: 1, title: 'Double réservation de la salle B203',
        description: 'Deux cours planifiés au même horaire',
        priority: 'urgent', date: 'Lundi 21 Oct 2025', time: '14:00 - 16:00',
        items: [
          { name: 'Physique Quantique', professor: 'Prof. Bernard', students: 30 },
          { name: 'TD Mathématiques',   professor: 'Dr. Dupont',    students: 25 },
        ],
        solutions: [
          { room: 'Salle A102', available: true },
          { room: 'Salle B104', available: true },
        ],
        resolved: false
      },
      {
        id: 2, title: 'Conflit de ressource - Projecteur HD',
        description: 'Même équipement demandé par deux salles',
        priority: 'normal', date: 'Mercredi 23 Oct 2025', time: '10:00 - 12:00',
        items: [
          { name: 'Chimie Organique',    professor: 'Prof. Laurent', students: 35 },
          { name: 'Biologie Cellulaire', professor: 'Dr. Martin',    students: 28 },
        ],
        solutions: [{ room: 'Projecteur B disponible', available: true }],
        resolved: false
      },
    ];
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  applySolution(conflict: Conflict, solution: Solution): void {
    conflict.resolved = true;
    this.conflicts = this.conflicts.filter(c => c.id !== conflict.id);
    this.updateStats();
  }
}
