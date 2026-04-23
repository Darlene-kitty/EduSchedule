import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import {
  ReportingService,
  StatisticsDTO,
  TeacherStatistic,
  RoomUsageStatistic,
  SchoolStatistic,
  TeacherWorkloadStatistic,
  ScheduledReportConfig,
  ReportRequest
} from '../../core/services/reporting.service';

type ActiveTab = 'overview' | 'rooms' | 'teachers' | 'schools' | 'scheduled';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './statistics.html',
  styleUrl: './statistics.css'
})
export class StatisticsComponent implements OnInit, AfterViewInit {
  private reportingSvc = inject(ReportingService);

  currentDate = '';
  currentTime = '';
  showToast   = false;
  toastMsg    = '';
  toastError  = false;

  activeTab: ActiveTab = 'overview';
  isLoading           = false;
  isLoadingScheduled  = false;

  stats: StatisticsDTO | null = null;

  // Données dérivées
  topTeachers:     TeacherStatistic[]         = [];
  roomUsage:       RoomUsageStatistic[]        = [];
  schoolStats:     SchoolStatistic[]           = [];
  teacherWorkload: TeacherWorkloadStatistic[]  = [];
  scheduledConfigs: ScheduledReportConfig[]    = [];

  readonly CHART_COLORS = [
    '#1D4ED8', '#15803D', '#F59E0B', '#7C3AED',
    '#EF4444', '#06B6D4', '#84CC16', '#F97316', '#EC4899', '#6366F1'
  ];

  @ViewChild('barRoomCanvas')     barRoomCanvas!:     ElementRef<HTMLCanvasElement>;
  @ViewChild('barTeacherCanvas')  barTeacherCanvas!:  ElementRef<HTMLCanvasElement>;
  @ViewChild('pieSchoolCanvas')   pieSchoolCanvas!:   ElementRef<HTMLCanvasElement>;
  @ViewChild('pieRoomTypeCanvas') pieRoomTypeCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('barAvailCanvas')    barAvailCanvas!:    ElementRef<HTMLCanvasElement>;

  // ── KPI cards ─────────────────────────────────────────────────────────────

  get kpiCards(): { label: string; value: string; icon: string; color: string }[] {
    if (!this.stats) return [];
    return [
      { label: 'Utilisateurs',    value: String(this.stats.totalUsers),                                          icon: 'people',          color: '#1D4ED8' },
      { label: 'Cours',           value: String(this.stats.totalCourses),                                        icon: 'menu_book',       color: '#15803D' },
      { label: 'Réservations',    value: String(this.stats.totalReservations),                                   icon: 'event_available', color: '#F59E0B' },
      { label: 'Salles',          value: String(this.stats.totalRooms || this.stats.totalResources),             icon: 'meeting_room',    color: '#7C3AED' },
      { label: 'Taux occupation', value: `${Math.round(this.stats.averageRoomOccupancy || 0)}%`,                 icon: 'trending_up',     color: '#EF4444' },
      { label: 'Écoles',          value: String(this.stats.totalSchools),                                        icon: 'school',          color: '#06B6D4' },
    ];
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadStatistics();
  }

  ngAfterViewInit(): void {
    // Les charts seront dessinés après le chargement des données
  }

  setTab(tab: ActiveTab): void {
    this.activeTab = tab;
    if (tab === 'scheduled' && this.scheduledConfigs.length === 0) {
      this.loadScheduledConfigs();
    }
    setTimeout(() => this.drawAllCharts(), 150);
  }

  // ── Data loading ──────────────────────────────────────────────────────────

  loadStatistics(): void {
    this.isLoading = true;
    this.reportingSvc.getStatistics().subscribe({
      next: data => {
        this.stats          = data;
        this.topTeachers    = (data.coursesByTeacher   || []).slice(0, 10);
        this.roomUsage      = (data.roomUsageDetails   || []).sort((a, b) => b.occupancyRate - a.occupancyRate);
        this.schoolStats    = data.schoolStatistics    || [];
        this.teacherWorkload = data.teacherWorkload    || [];
        this.isLoading      = false;
        setTimeout(() => this.drawAllCharts(), 200);
      },
      error: () => {
        this.isLoading = false;
        this.toast('Impossible de charger les statistiques', true);
      }
    });
  }

  loadScheduledConfigs(): void {
    this.isLoadingScheduled = true;
    this.reportingSvc.getScheduledConfigs().subscribe({
      next: configs => { this.scheduledConfigs = configs; this.isLoadingScheduled = false; },
      error: () => { this.isLoadingScheduled = false; }
    });
  }

  triggerScheduledReport(config: ScheduledReportConfig): void {
    const req: ReportRequest = {
      type:   config.reportType as any,
      format: config.reportFormat as any,
      title:  config.name
    };
    this.reportingSvc.generate(req).subscribe({
      next: () => this.toast(`Rapport "${config.name}" généré !`),
      error: () => this.toast('Erreur lors de la génération', true)
    });
  }

  // ── Chart helpers ─────────────────────────────────────────────────────────

  drawAllCharts(): void {
    if (!this.stats) return;
    if (this.activeTab === 'overview') {
      this.drawPie(this.pieSchoolCanvas,   this.stats.coursesBySchool   || {});
      this.drawPie(this.pieRoomTypeCanvas, this.stats.coursesByRoomType || {});
    }
    if (this.activeTab === 'rooms') {
      this.drawBarRooms();
      this.drawBarAvailability();
    }
    if (this.activeTab === 'teachers') {
      this.drawBarTeachers();
    }
  }

  private drawPie(ref: ElementRef<HTMLCanvasElement> | undefined, data: Record<string, number>): void {
    if (!ref?.nativeElement) return;
    const canvas = ref.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const entries = Object.entries(data).filter(([, v]) => v > 0);
    if (entries.length === 0) return;

    const total = entries.reduce((s, [, v]) => s + v, 0);
    const cx = canvas.width / 2;
    const cy = canvas.height / 2;
    const r  = Math.min(cx, cy) - 20;

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    let startAngle = -Math.PI / 2;

    entries.forEach(([label, value], i) => {
      const slice = (value / total) * 2 * Math.PI;
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.arc(cx, cy, r, startAngle, startAngle + slice);
      ctx.closePath();
      ctx.fillStyle = this.CHART_COLORS[i % this.CHART_COLORS.length];
      ctx.fill();

      // Label
      const midAngle = startAngle + slice / 2;
      const lx = cx + (r * 0.65) * Math.cos(midAngle);
      const ly = cy + (r * 0.65) * Math.sin(midAngle);
      ctx.fillStyle = '#fff';
      ctx.font = 'bold 11px sans-serif';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      if (slice > 0.3) ctx.fillText(`${Math.round((value / total) * 100)}%`, lx, ly);

      startAngle += slice;
    });

    // Légende
    const legendX = 10;
    let legendY = canvas.height - entries.length * 18 - 5;
    entries.forEach(([label], i) => {
      ctx.fillStyle = this.CHART_COLORS[i % this.CHART_COLORS.length];
      ctx.fillRect(legendX, legendY, 12, 12);
      ctx.fillStyle = '#374151';
      ctx.font = '11px sans-serif';
      ctx.textAlign = 'left';
      ctx.fillText(label.length > 20 ? label.slice(0, 18) + '…' : label, legendX + 16, legendY + 9);
      legendY += 18;
    });
  }

  private drawBarRooms(): void {
    if (!this.barRoomCanvas?.nativeElement || this.roomUsage.length === 0) return;
    const canvas = this.barRoomCanvas.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const data   = this.roomUsage.slice(0, 12);
    const labels = data.map(r => r.roomName.length > 10 ? r.roomName.slice(0, 9) + '…' : r.roomName);
    const values = data.map(r => r.occupancyRate);
    this.drawBarChart(ctx, canvas, labels, values, 'Taux d\'occupation (%)');
  }

  private drawBarTeachers(): void {
    if (!this.barTeacherCanvas?.nativeElement || this.topTeachers.length === 0) return;
    const canvas = this.barTeacherCanvas.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const data   = this.topTeachers.slice(0, 10);
    const labels = data.map(t => t.teacherName.split(' ')[0]);
    const values = data.map(t => t.totalHours);
    this.drawBarChart(ctx, canvas, labels, values, 'Heures de cours');
  }

  private drawBarAvailability(): void {
    if (!this.barAvailCanvas?.nativeElement || !this.stats?.roomAvailabilityByHour) return;
    const canvas = this.barAvailCanvas.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const avail  = this.stats.roomAvailabilityByHour;
    const labels = Object.keys(avail).sort();
    const values = labels.map(k => avail[k]);
    this.drawBarChart(ctx, canvas, labels, values, 'Salles disponibles', '#15803D');
  }

  private drawBarChart(
    ctx: CanvasRenderingContext2D,
    canvas: HTMLCanvasElement,
    labels: string[],
    values: number[],
    yLabel: string,
    color = '#1D4ED8'
  ): void {
    const pad = { top: 20, right: 20, bottom: 50, left: 40 };
    const w = canvas.width  - pad.left - pad.right;
    const h = canvas.height - pad.top  - pad.bottom;
    const max = Math.max(...values, 1);

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Axes
    ctx.strokeStyle = '#E5E7EB';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(pad.left, pad.top);
    ctx.lineTo(pad.left, pad.top + h);
    ctx.lineTo(pad.left + w, pad.top + h);
    ctx.stroke();

    // Barres
    const barW = Math.max(4, (w / labels.length) - 4);
    labels.forEach((label, i) => {
      const barH = (values[i] / max) * h;
      const x    = pad.left + i * (w / labels.length) + (w / labels.length - barW) / 2;
      const y    = pad.top + h - barH;

      ctx.fillStyle = this.CHART_COLORS[i % this.CHART_COLORS.length] || color;
      ctx.beginPath();
      ctx.roundRect(x, y, barW, barH, 3);
      ctx.fill();

      // Label X
      ctx.fillStyle = '#6B7280';
      ctx.font = '10px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(label, x + barW / 2, pad.top + h + 14);

      // Valeur
      if (barH > 16) {
        ctx.fillStyle = '#fff';
        ctx.font = 'bold 10px sans-serif';
        ctx.fillText(String(Math.round(values[i])), x + barW / 2, y + 12);
      }
    });
  }

  // ── CSS helpers ───────────────────────────────────────────────────────────

  occupancyClass(rate: number): string {
    if (rate >= 80) return 'badge-red';
    if (rate >= 50) return 'badge-orange';
    return 'badge-green';
  }

  workloadClass(status: string): string {
    const s = (status || '').toUpperCase();
    if (s === 'OVERLOADED') return 'badge-red';
    if (s === 'NORMAL')     return 'badge-green';
    return 'badge-orange';
  }

  workloadLabel(status: string): string {
    const map: Record<string, string> = {
      OVERLOADED: 'Surchargé', NORMAL: 'Normal', UNDERLOADED: 'Sous-chargé', LIGHT: 'Léger'
    };
    return map[(status || '').toUpperCase()] ?? status;
  }

  // ── Utils ─────────────────────────────────────────────────────────────────

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  toast(msg: string, error = false): void {
    this.toastMsg   = msg;
    this.toastError = error;
    this.showToast  = true;
    setTimeout(() => this.showToast = false, 3500);
  }
}
