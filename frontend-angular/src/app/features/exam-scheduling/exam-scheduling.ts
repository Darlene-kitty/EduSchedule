import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { ExamSchedulingService, ExamSchedulingRequest, ExamSchedulingResult, ExamSlot } from '../../core/services/exam-scheduling.service';

@Component({
  selector: 'app-exam-scheduling',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './exam-scheduling.html',
  styleUrl: './exam-scheduling.css'
})
export class ExamSchedulingComponent implements OnInit {
  private service = inject(ExamSchedulingService);

  currentDate = ''; currentTime = '';
  isGenerating = false;
  result: ExamSchedulingResult | null = null;
  activeTab: 'config' | 'results' | 'conflicts' = 'config';

  request: ExamSchedulingRequest = {
    schoolId: 1,
    semester: 'S1',
    levels: ['L1', 'L2', 'L3'],
    sessionStart: '',
    sessionEnd: '',
    defaultDurationMinutes: 120,
    availableSlots: ['08:00-10:00', '10:30-12:30', '14:00-16:00'],
    maxExamsPerDayPerLevel: 2,
    respectTeacherAvailability: true
  };

  allLevels = ['L1', 'L2', 'L3', 'M1', 'M2'];
  allSemesters = ['S1', 'S2'];

  defaultSlots = ['08:00-10:00', '10:30-12:30', '14:00-16:00', '16:30-18:30'];
  selectedSlots = new Set<string>(['08:00-10:00', '10:30-12:30', '14:00-16:00']);

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    // Dates par défaut : 3 semaines à partir d'aujourd'hui
    const start = new Date(); start.setDate(start.getDate() + 21);
    const end   = new Date(start); end.setDate(end.getDate() + 14);
    this.request.sessionStart = start.toISOString().split('T')[0];
    this.request.sessionEnd   = end.toISOString().split('T')[0];
  }

  toggleLevel(level: string): void {
    const idx = this.request.levels.indexOf(level);
    if (idx >= 0) this.request.levels.splice(idx, 1);
    else this.request.levels.push(level);
  }

  isLevelSelected(level: string): boolean { return this.request.levels.includes(level); }

  toggleSlot(slot: string): void {
    if (this.selectedSlots.has(slot)) this.selectedSlots.delete(slot);
    else this.selectedSlots.add(slot);
    this.request.availableSlots = Array.from(this.selectedSlots);
  }

  isSlotSelected(slot: string): boolean { return this.selectedSlots.has(slot); }

  generate(): void {
    if (!this.request.sessionStart || !this.request.sessionEnd || this.request.levels.length === 0) return;
    this.isGenerating = true;
    this.result = null;
    this.service.generate(this.request).subscribe({
      next: (res) => {
        this.result = res;
        this.isGenerating = false;
        this.activeTab = res.scheduled > 0 ? 'results' : 'conflicts';
      },
      error: () => { this.isGenerating = false; }
    });
  }

  get scheduledByDate(): Map<string, ExamSlot[]> {
    const map = new Map<string, ExamSlot[]>();
    if (!this.result) return map;
    this.result.slots.forEach(s => {
      const key = s.date;
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(s);
    });
    return map;
  }

  get sortedDates(): string[] {
    return Array.from(this.scheduledByDate.keys()).sort();
  }

  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('fr-FR', { weekday:'long', day:'numeric', month:'long' });
  }

  getLevelColor(level: string): string {
    const colors: Record<string, string> = { L1:'#1D4ED8', L2:'#15803D', L3:'#7C3AED', M1:'#EA580C', M2:'#DC2626' };
    return colors[level] || '#6B7280';
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday:'long', day:'numeric', month:'long', year:'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour:'2-digit', minute:'2-digit' });
  }
}
