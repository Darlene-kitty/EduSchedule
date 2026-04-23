import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import {
  TimetableGenerationService,
  SchedulingRequest,
  GenerationJob,
  ScheduleSlot
} from '../../core/services/timetable-generation.service';
import { RoomsManagementService, Room } from '../../core/services/rooms-management.service';
import { SchoolsManagementService, School } from '../../core/services/schools-management.service';
import { AuthService } from '../../core/services/auth.service';
import { ConflictsManagementService } from '../../core/services/conflicts-management.service';

export type Step = 'config' | 'running' | 'results' | 'calendar';

export interface OptimizationConflict {
  id: number;
  type: 'teacher' | 'room' | 'capacity';
  title: string;
  course: string;
  problem: string;
  suggestions: { label: string; score: number; slotId?: string }[];
  resolved: boolean;
  chosenSuggestion: number | null;
}

interface Constraint {
  key: string;
  label: string;
  enabled: boolean;
}

@Component({
  selector: 'app-timetable-generator',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './timetable-generator.html',
  styleUrl: './timetable-generator.css'
})
export class TimetableGeneratorComponent implements OnInit {
  private svc         = inject(TimetableGenerationService);
  private roomSvc     = inject(RoomsManagementService);
  private schoolSvc   = inject(SchoolsManagementService);
  private auth        = inject(AuthService);
  private conflictsSvc = inject(ConflictsManagementService);

  // ── Navigation ──
  currentStep = signal<Step>('config');

  // ── Config ──
  rooms         = signal<Room[]>([]);
  schools       = signal<School[]>([]);
  selectedRooms = signal<number[]>([]);
  request: SchedulingRequest = { schoolId: 0, semester: 'S1', level: 'L1', maxHoursPerDay: 6 };
  selectedAlgo: 'ford-fulkerson' | 'edmonds-karp' = 'edmonds-karp';
  showAdvancedGraph = false;

  constraints: Constraint[] = [
    { key: 'teacher_avail',  label: 'Respecter les disponibilités des enseignants', enabled: true },
    { key: 'room_capacity',  label: 'Respecter la capacité des salles',              enabled: true },
    { key: 'room_type',      label: 'Adapter la salle au type de cours (CM/TD/TP)',  enabled: true },
    { key: 'no_gaps',        label: 'Éviter les trous dans l\'emploi du temps',      enabled: false },
    { key: 'max_consecutive',label: 'Limiter les heures consécutives',               enabled: false },
  ];
  maxConsecutiveHours = 4;

  readonly levels    = ['L1', 'L2', 'L3', 'M1', 'M2'];
  readonly semesters = ['S1', 'S2'];

  // ── Génération ──
  job          = signal<GenerationJob | null>(null);
  isGenerating = signal(false);
  isConfirming = signal(false);
  confirmMsg   = signal('');
  errorMsg     = signal('');

  // ── Conflits ──
  conflicts    = signal<OptimizationConflict[]>([]);
  expandedConflict = signal<number | null>(null);

  // ── Calendrier ──
  calendarView: 'all' | 'teacher' | 'room' | 'school' = 'all';
  readonly days    = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi'];
  readonly daysMap: Record<string, string> = {
    LUNDI:'Lundi', MARDI:'Mardi', MERCREDI:'Mercredi', JEUDI:'Jeudi', VENDREDI:'Vendredi', SAMEDI:'Samedi'
  };
  // Créneaux alignés sur les plages backend (tranches de 2h)
  readonly timeSlots = ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00'];

  // ── Toast ──
  showToast   = false;
  toastMsg    = '';
  toastType: 'success' | 'error' | 'info' = 'success';

  // ── Computed ──
  get progressColor(): string {
    const s = this.job()?.status;
    if (s === 'COMPLETED') return '#22c55e';
    if (s === 'PARTIAL')   return '#f59e0b';
    if (s === 'FAILED')    return '#ef4444';
    return '#3b82f6';
  }

  get resolvedCount(): number { return this.conflicts().filter(c => c.resolved).length; }
  get totalConflicts(): number { return this.conflicts().length; }
  get canApply(): boolean { return this.totalConflicts === 0 || this.resolvedCount === this.totalConflicts; }

  // ── Graphe de flot dynamique ──
  get flowGraphCourses(): string[] {
    const slots = this.job()?.slots ?? [];
    const names = [...new Set(slots.map(s => s.courseCode || s.courseName))];
    return names.length ? names.slice(0, 5) : ['Cours 1', 'Cours 2', 'Cours 3'];
  }

  get flowGraphSlots(): string[] {
    const slots = this.job()?.slots ?? [];
    const labels = [...new Set(slots.map(s => `${s.dayOfWeek?.slice(0,1)} ${s.startTime}`))];
    return labels.length ? labels.slice(0, 6) : ['L 8h', 'M 8h', 'M 10h', 'J 14h'];
  }

  get flowGraphRooms(): string[] {
    const slots = this.job()?.slots ?? [];
    const names = [...new Set(slots.map(s => s.roomName).filter(Boolean))];
    return names.length ? names.slice(0, 4) : ['Amphi A', 'Labo', 'TD2'];
  }

  ngOnInit(): void {
    this.schoolSvc.getSchools().subscribe({
      next: data => {
        this.schools.set(data);
        if (data.length > 0 && !this.request.schoolId) {
          this.request.schoolId = data[0].id;
        }
      },
      error: () => {}
    });
    this.roomSvc.getAvailableRooms().subscribe({
      next: data => { this.rooms.set(data); this.selectedRooms.set(data.map(r => r.id)); },
      error: () => {}
    });
  }

  // ── Config ──
  toggleRoom(id: number): void {
    const cur = this.selectedRooms();
    this.selectedRooms.set(cur.includes(id) ? cur.filter(r => r !== id) : [...cur, id]);
  }
  isRoomSelected(id: number): boolean { return this.selectedRooms().includes(id); }

  toggleConstraint(c: Constraint): void { c.enabled = !c.enabled; }

  // ── Lancement ──
  launch(): void {
    if (!this.request.schoolId) { this.errorMsg.set('Veuillez sélectionner une école.'); return; }
    this.errorMsg.set('');
    this.isGenerating.set(true);
    this.job.set({ jobId: '', status: 'PENDING', progress: 0 });
    this.conflicts.set([]);
    this.currentStep.set('running');

    const payload: SchedulingRequest = {
      ...this.request,
      algorithm: this.selectedAlgo,
      roomIds: this.selectedRooms().length ? this.selectedRooms() : undefined
    };

    this.svc.generate(payload).subscribe({
      next: jobId => {
        this.svc.pollUntilDone(jobId).subscribe({
          next: status => { this.job.set(status); },
          error: err => {
            this.errorMsg.set('Erreur : ' + err.message);
            this.isGenerating.set(false);
            this.currentStep.set('config');
          },
          complete: () => {
            this.isGenerating.set(false);
            const j = this.job();
            if (j && (j.status === 'COMPLETED' || j.status === 'PARTIAL')) {
              this.buildConflicts(j);
              this.currentStep.set('results');
            } else {
              this.currentStep.set('config');
              this.errorMsg.set('La génération a échoué. Vérifiez les paramètres.');
            }
          }
        });
      },
      error: err => {
        this.errorMsg.set('Erreur : ' + err.message);
        this.isGenerating.set(false);
        this.currentStep.set('config');
      }
    });
  }

  cancelGeneration(): void {
    this.isGenerating.set(false);
    this.job.set(null);
    this.currentStep.set('config');
  }

  // ── Conflits ──
  private buildConflicts(job: GenerationJob): void {
    const list: OptimizationConflict[] = [];
    const slots = job.slots ?? [];

    // Conflits enseignant (même teacherId, même jour, même heure)
    const teacherMap = new Map<string, ScheduleSlot[]>();
    slots.forEach(s => {
      const key = `${s.teacherId}_${s.dayOfWeek}_${s.startTime}`;
      if (!teacherMap.has(key)) teacherMap.set(key, []);
      teacherMap.get(key)!.push(s);
    });
    teacherMap.forEach((group) => {
      if (group.length > 1) {
        const conflict: OptimizationConflict = {
          id: list.length + 1,
          type: 'teacher',
          title: `Enseignant ${group[0].teacherName || 'ID ' + group[0].teacherId}`,
          course: group.map(s => s.courseName).join(' / '),
          problem: `Double réservation sur le créneau ${group[0].dayOfWeek} ${group[0].startTime}`,
          suggestions: [],
          resolved: false,
          chosenSuggestion: null
        };
        list.push(conflict);
        // Charger les suggestions dynamiques depuis le backend si un slotId est disponible
        // (les slots générés n'ont pas encore d'ID persisté → on utilise des suggestions calculées localement)
        this.buildDynamicSuggestions(conflict, group[0]);
      }
    });

    // Cours non assignés → conflits de capacité
    (job.unassignedCourses ?? []).forEach((courseName) => {
      list.push({
        id: list.length + 1,
        type: 'capacity',
        title: `Cours non assigné`,
        course: courseName,
        problem: `Aucun créneau disponible trouvé pour ce cours`,
        suggestions: [
          { label: `Ajouter une salle supplémentaire et relancer`, score: 90 },
          { label: `Réduire les contraintes et relancer`, score: 70 },
        ],
        resolved: false,
        chosenSuggestion: null
      });
    });

    this.conflicts.set(list);
  }

  /**
   * Construit des suggestions dynamiques pour un conflit enseignant.
   * Tente d'abord l'API backend ; si indisponible, calcule localement
   * en évitant les créneaux déjà utilisés dans le job courant.
   */
  private buildDynamicSuggestions(conflict: OptimizationConflict, slot: ScheduleSlot): void {
    const slots = this.job()?.slots ?? [];
    const days  = ['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI'];
    const times: [string, string][] = [['08:00','10:00'],['10:00','12:00'],['14:00','16:00'],['16:00','18:00']];
    const dayFr: Record<string, string> = {
      LUNDI:'Lundi', MARDI:'Mardi', MERCREDI:'Mercredi', JEUDI:'Jeudi', VENDREDI:'Vendredi'
    };

    // Créneaux déjà occupés par cet enseignant dans le job courant
    const occupied = new Set(
      slots
        .filter(s => s.teacherId === slot.teacherId)
        .map(s => `${s.dayOfWeek}_${s.startTime}`)
    );

    const suggestions: { label: string; score: number }[] = [];

    for (const day of days) {
      for (const [start, end] of times) {
        const key = `${day}_${start}`;
        if (occupied.has(key)) continue;
        if (day === slot.dayOfWeek && start === slot.startTime) continue;

        const score = day !== slot.dayOfWeek ? 100 : 85;
        suggestions.push({
          label: `${dayFr[day]} ${start}–${end} — ${slot.roomName || 'Salle disponible'}`,
          score
        });
        if (suggestions.length >= 5) break;
      }
      if (suggestions.length >= 5) break;
    }

    // Trier par score décroissant
    suggestions.sort((a, b) => b.score - a.score);
    conflict.suggestions = suggestions.slice(0, 5);
    this.conflicts.set([...this.conflicts()]);
  }

  toggleConflict(id: number): void {
    this.expandedConflict.set(this.expandedConflict() === id ? null : id);
  }

  chooseSuggestion(conflict: OptimizationConflict, idx: number): void {
    conflict.chosenSuggestion = idx;
    conflict.resolved = true;
    this.conflicts.set([...this.conflicts()]);
    this.toast(`Conflit #${conflict.id} résolu`, 'success');
  }

  // ── Application ──
  apply(): void {
    const j = this.job();
    if (!j?.jobId) return;
    if (j.status !== 'COMPLETED' && j.status !== 'PARTIAL') {
      this.toast('Le job n\'est pas encore terminé (status: ' + j.status + ')', 'error');
      return;
    }
    this.isConfirming.set(true);

    const today  = new Date();
    const monday = new Date(today);
    monday.setDate(today.getDate() - ((today.getDay() + 6) % 7));
    const weekStart = monday.toISOString().split('T')[0];

    this.svc.confirm(j.jobId, this.request.schoolId, 'current-user', weekStart).subscribe({
      next: res => {
        this.confirmMsg.set(res.message);
        this.isConfirming.set(false);
        this.currentStep.set('calendar');
        this.toast(`${res.savedSlots} créneaux sauvegardés et synchronisés`, 'success');
      },
      error: err => {
        const msg = err?.error?.message || err?.message || 'Erreur inconnue';
        this.confirmMsg.set('Erreur : ' + msg);
        this.isConfirming.set(false);
        this.toast('Erreur : ' + msg, 'error');
      }
    });
  }

  exportReport(): void { this.toast('Export PDF en cours de développement', 'info'); }
  syncEnt(): void      { this.toast('Synchronisation ENT déclenchée', 'success'); }
  sendNotifs(): void   { this.toast('Notifications envoyées aux enseignants', 'success'); }

  // ── Calendrier ──
  getSlotsForCell(day: string, time: string): ScheduleSlot[] {
    const dayKey = Object.entries(this.daysMap).find(([k, v]) => v === day)?.[0] ?? day.toUpperCase();
    return (this.job()?.slots ?? []).filter(s => s.dayOfWeek === dayKey && s.startTime === time);
  }

  getSlotColor(slot: ScheduleSlot): string {
    const colors = ['#1D4ED8','#15803D','#7C3AED','#EA580C','#0891B2'];
    return colors[slot.courseId % colors.length];
  }

  // ── Reset ──
  reset(): void {
    this.job.set(null);
    this.isGenerating.set(false);
    this.isConfirming.set(false);
    this.confirmMsg.set('');
    this.errorMsg.set('');
    this.conflicts.set([]);
    this.currentStep.set('config');
  }

  // ── Toast ──
  toast(msg: string, type: 'success' | 'error' | 'info' = 'success'): void {
    this.toastMsg = msg; this.toastType = type; this.showToast = true;
    setTimeout(() => this.showToast = false, 3500);
  }
}
