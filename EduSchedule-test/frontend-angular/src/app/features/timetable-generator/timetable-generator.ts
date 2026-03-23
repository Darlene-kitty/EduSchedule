import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  TimetableGenerationService,
  SchedulingRequest,
  GenerationJob,
  ScheduleSlot
} from '../../core/services/timetable-generation.service';
import { RoomsManagementService, Room } from '../../core/services/rooms-management.service';

@Component({
  selector: 'app-timetable-generator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './timetable-generator.html',
  styleUrl: './timetable-generator.css'
})
export class TimetableGeneratorComponent implements OnInit {
  private svc   = inject(TimetableGenerationService);
  private roomSvc = inject(RoomsManagementService);

  // Données
  rooms         = signal<Room[]>([]);
  selectedRooms = signal<number[]>([]);

  // Formulaire
  request: SchedulingRequest = {
    schoolId: 0,
    semester: 'S1',
    level: 'L1',
    maxHoursPerDay: 6
  };

  // État
  job        = signal<GenerationJob | null>(null);
  isGenerating = signal(false);
  isConfirming = signal(false);
  confirmMsg   = signal('');
  errorMsg   = signal('');

  readonly days      = ['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI'];
  readonly levels    = ['L1', 'L2', 'L3', 'M1', 'M2'];
  readonly semesters = ['S1', 'S2'];

  ngOnInit(): void {
    this.roomSvc.getAvailableRooms().subscribe({
      next: data => {
        this.rooms.set(data);
        // Toutes les salles sélectionnées par défaut
        this.selectedRooms.set(data.map(r => r.id));
      },
      error: () => {
        // Fallback silencieux : l'algo utilisera les salles par défaut
      }
    });
  }

  toggleRoom(id: number): void {
    const current = this.selectedRooms();
    this.selectedRooms.set(
      current.includes(id) ? current.filter(r => r !== id) : [...current, id]
    );
  }

  isRoomSelected(id: number): boolean {
    return this.selectedRooms().includes(id);
  }

  generate(): void {
    if (!this.request.schoolId) {
      this.errorMsg.set('Veuillez saisir un ID école.');
      return;
    }
    if (this.selectedRooms().length === 0) {
      this.errorMsg.set('Sélectionnez au moins une salle.');
      return;
    }

    this.errorMsg.set('');
    this.isGenerating.set(true);
    this.job.set({ jobId: '', status: 'PENDING', progress: 0 });

    const payload: SchedulingRequest = {
      ...this.request,
      roomIds: this.selectedRooms()
    };

    this.svc.generate(payload).subscribe({
      next: jobId => {
        this.svc.pollUntilDone(jobId).subscribe({
          next:     status => this.job.set(status),
          error:    err    => { this.errorMsg.set('Erreur polling : ' + err.message); this.isGenerating.set(false); },
          complete: ()     => this.isGenerating.set(false)
        });
      },
      error: err => {
        this.errorMsg.set('Erreur : ' + err.message);
        this.isGenerating.set(false);
      }
    });
  }

  reset(): void {
    this.job.set(null);
    this.isGenerating.set(false);
    this.isConfirming.set(false);
    this.confirmMsg.set('');
    this.errorMsg.set('');
  }

  confirm(): void {
    const j = this.job();
    if (!j?.jobId) return;

    this.isConfirming.set(true);
    this.confirmMsg.set('');

    // Lundi de la semaine courante comme référence
    const today = new Date();
    const monday = new Date(today);
    monday.setDate(today.getDate() - ((today.getDay() + 6) % 7));
    const weekStart = monday.toISOString().split('T')[0];

    this.svc.confirm(j.jobId, this.request.schoolId, 'current-user', weekStart).subscribe({
      next: res => {
        this.confirmMsg.set(res.message + ' (' + res.savedSlots + ' créneaux)');
        this.isConfirming.set(false);
      },
      error: err => {
        this.confirmMsg.set('Erreur : ' + err.message);
        this.isConfirming.set(false);
      }
    });
  }

  slotsByDay(day: string): ScheduleSlot[] {
    return this.job()?.slots?.filter(s => s.dayOfWeek === day) ?? [];
  }

  get progressColor(): string {
    const s = this.job()?.status;
    if (s === 'COMPLETED') return '#22c55e';
    if (s === 'PARTIAL')   return '#f59e0b';
    if (s === 'FAILED')    return '#ef4444';
    return '#3b82f6';
  }
}
