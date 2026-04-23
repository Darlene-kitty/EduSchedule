import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import {
  ReportingService,
  ReportDTO, ReportRequest, ReportType, ReportFormat,
  StatisticsDTO, ScheduledReportConfig
} from '../../core/services/reporting.service';
import { AuthService } from '../../core/services/auth.service';

type ActiveTab = 'overview' | 'rooms' | 'teachers' | 'schools' | 'export';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './reports.html',
  styleUrl: './reports.css'
})
export class ReportsComponent implements OnInit, AfterViewInit {
  private reportingService = inject(ReportingService);
  private authService      = inject(AuthService);

  currentDate = ''; currentTime = '';

  // ── Tabs ──────────────────────────────────────────────────────────────────
  activeTab: ActiveTab = 'overview';

  setTab(tab: ActiveTab): void {
    this.activeTab = tab;
    setTimeout(() => this.drawAllCharts(), 120);
  }

  // ── Statistics ────────────────────────────────────────────────────────────
  isLoading = false;
  statsData: StatisticsDTO | null = null;
  scheduledConfigs: ScheduledReportConfig[] = [];
  isLoadingScheduled = false;

  readonly CHART_COLORS = [
    '#1D4ED8','#15803D','#F59E0B','#7C3AED',
    '#EF4444','#06B6D4','#84CC16','#F97316','#EC4899','#6366F1'
  ];

  @ViewChild('barRoomCanvas')     barRoomCanvas!:     ElementRef<HTMLCanvasElement>;
  @ViewChild('barTeacherCanvas')  barTeacherCanvas!:  ElementRef<HTMLCanvasElement>;
  @ViewChild('pieSchoolCanvas')   pieSchoolCanvas!:   ElementRef<HTMLCanvasElement>;
  @ViewChild('pieRoomTypeCanvas') pieRoomTypeCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('barAvailCanvas')    barAvailCanvas!:    ElementRef<HTMLCanvasElement>;

  get kpiCards(): { label: string; value: string; icon: string; color: string }[] {
    if (!this.statsData) return [];
    return [
      { label: 'Utilisateurs',      value: String(this.statsData.totalUsers),       icon: 'people',          color: '#1D4ED8' },
      { label: 'Cours',             value: String(this.statsData.totalCourses),      icon: 'menu_book',       color: '#15803D' },
      { label: 'Réservations',      value: String(this.statsData.totalReservations), icon: 'event_available', color: '#F59E0B' },
      { label: 'Salles',            value: String(this.statsData.totalRooms || this.statsData.totalResources), icon: 'meeting_room', color: '#7C3AED' },
      { label: 'Taux occupation',   value: `${Math.round(this.statsData.averageRoomOccupancy || 0)}%`,        icon: 'trending_up',  color: '#EF4444' },
      { label: 'Utilisation cours', value: `${Math.round(this.statsData.averageCourseUtilization || 0)}%`,   icon: 'bar_chart',    color: '#06B6D4' },
    ];
  }

  get topTeachers()    { return (this.statsData?.coursesByTeacher  || []).slice(0, 10); }
  get roomUsage()      { return (this.statsData?.roomUsageDetails  || []).slice(0, 15); }
  get schoolStats()    { return  this.statsData?.schoolStatistics  || []; }
  get teacherWorkload(){ return  this.statsData?.teacherWorkload   || []; }

  workloadClass(s: string): string {
    if (s === 'overloaded')    return 'badge-red';
    if (s === 'underutilized') return 'badge-yellow';
    return 'badge-green';
  }
  workloadLabel(s: string): string {
    if (s === 'overloaded')    return 'Surchargé';
    if (s === 'underutilized') return 'Sous-utilisé';
    return 'Normal';
  }
  occupancyClass(rate: number): string {
    if (rate >= 80) return 'badge-red';
    if (rate >= 50) return 'badge-yellow';
    return 'badge-green';
  }
  mapEntries(obj: Record<string, number> | undefined): { key: string; value: number }[] {
    if (!obj) return [];
    return Object.entries(obj).map(([key, value]) => ({ key, value })).sort((a, b) => b.value - a.value);
  }

  // ── Export ────────────────────────────────────────────────────────────────
  isGenerating = false;
  isLoadingHistory = false;
  recentReports: ReportDTO[] = [];

  reportForm: ReportRequest = {
    type:      'ROOM_OCCUPANCY',
    format:    'PDF',
    title:     "Rapport d'occupation des salles",
    startDate: this.defaultStartDate(),
    endDate:   new Date().toISOString().split('T')[0],
  };

  readonly reportTypes: { value: ReportType; label: string }[] = [
    { value: 'ROOM_OCCUPANCY',      label: 'Occupation des salles' },
    { value: 'RESERVATION_SUMMARY', label: 'Résumé des réservations' },
    { value: 'COURSE_UTILIZATION',  label: 'Utilisation des cours' },
    { value: 'USER_STATISTICS',     label: 'Statistiques utilisateurs' },
    { value: 'SCHEDULE_OVERVIEW',   label: "Vue d'ensemble EDT" },
    { value: 'RESOURCE_USAGE',      label: 'Utilisation des ressources' },
    { value: 'MONTHLY_SUMMARY',     label: 'Résumé mensuel' },
    { value: 'YEARLY_SUMMARY',      label: 'Résumé annuel' },
  ];

  readonly reportFormats: { value: ReportFormat; label: string; icon: string }[] = [
    { value: 'PDF',   label: 'PDF',   icon: 'picture_as_pdf' },
    { value: 'EXCEL', label: 'Excel', icon: 'table_chart' },
    { value: 'CSV',   label: 'CSV',   icon: 'description' },
  ];

  // ── Toast ─────────────────────────────────────────────────────────────────
  showToast = false; toastMsg = ''; toastError = false;

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadStatistics();
    this.loadScheduledConfigs();
    this.loadReportHistory();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.drawAllCharts(), 350);
  }

  // ── Data loading ──────────────────────────────────────────────────────────
  loadStatistics(): void {
    this.isLoading = true;
    this.reportingService.getStatistics().subscribe({
      next: (data: StatisticsDTO) => {
        this.statsData = data as StatisticsDTO;
        this.isLoading = false;
        setTimeout(() => this.drawAllCharts(), 200);
      },
      error: () => { this.isLoading = false; this.toast('Impossible de charger les statistiques.', true); }
    });
  }

  loadScheduledConfigs(): void {
    this.isLoadingScheduled = true;
    this.reportingService.getScheduledConfigs().subscribe({
      next: (configs: ScheduledReportConfig[]) => { this.scheduledConfigs = configs; this.isLoadingScheduled = false; },
      error: () => { this.scheduledConfigs = []; this.isLoadingScheduled = false; }
    });
  }

  loadReportHistory(): void {
    this.isLoadingHistory = true;
    const userId = this.authService.getUser()?.id;
    const obs = userId ? this.reportingService.getMyReports(userId) : this.reportingService.getAll();
    obs.subscribe({
      next: (res: { content: ReportDTO[] }) => { this.recentReports = (res.content ?? []).slice(0, 10); this.isLoadingHistory = false; },
      error: () => { this.recentReports = []; this.isLoadingHistory = false; }
    });
  }

  // ── Export actions ────────────────────────────────────────────────────────
  generateReport(): void {
    if (this.isGenerating) return;
    this.isGenerating = true;
    const request: ReportRequest = {
      ...this.reportForm,
      title: this.reportForm.title || ReportingService.typeLabel(this.reportForm.type),
    };
    this.reportingService.generate(request).subscribe({
      next: (report: ReportDTO) => {
        this.isGenerating = false;
        if (report.status === 'FAILED') { this.toast('Erreur lors de la génération.', true); return; }
        this.toast(`Rapport "${report.title}" généré.`);
        this.recentReports = [report, ...this.recentReports].slice(0, 10);
        if (report.status === 'COMPLETED' && report.id) this.reportingService.downloadFile(report);
      },
      error: () => { this.isGenerating = false; this.toast('Erreur de connexion au service de rapports.', true); }
    });
  }

  downloadReport(report: ReportDTO): void {
    if (report.status !== 'COMPLETED') return;
    this.reportingService.downloadFile(report);
  }

  deleteReport(report: ReportDTO): void {
    this.reportingService.delete(report.id).subscribe({
      next: () => { this.recentReports = this.recentReports.filter(r => r.id !== report.id); },
      error: () => this.toast('Erreur lors de la suppression.', true)
    });
  }

  triggerScheduledReport(config: ScheduledReportConfig): void {
    const typeMap: Record<string, string> = {
      MONTHLY_SUMMARY: 'Résumé mensuel', ROOM_OCCUPANCY: 'Occupation des salles',
      USER_STATISTICS: 'Statistiques utilisateurs', YEARLY_SUMMARY: 'Résumé annuel',
      COURSE_UTILIZATION: 'Utilisation des cours'
    };
    this.reportingService.generate({
      type: config.reportType as ReportType,
      format: config.reportFormat as ReportFormat,
      title: typeMap[config.reportType] || config.name
    }).subscribe({
      next: (r: ReportDTO) => this.toast(`Rapport "${r.title}" généré avec succès.`),
      error: () => this.toast('Erreur lors de la génération.', true)
    });
  }

  // ── Charts ────────────────────────────────────────────────────────────────
  drawAllCharts(): void {
    if (this.activeTab === 'overview') {
      this.drawPieChart(this.pieSchoolCanvas,   this.mapEntries(this.statsData?.coursesBySchool));
      this.drawPieChart(this.pieRoomTypeCanvas, this.mapEntries(this.statsData?.coursesByRoomType));
    }
    if (this.activeTab === 'rooms') {
      this.drawBarChart(this.barRoomCanvas,
        (this.statsData?.roomUsageDetails || []).slice(0, 10).map(r => ({ label: r.roomName, value: r.occupancyRate })),
        '#1D4ED8', '%');
      this.drawBarChart(this.barAvailCanvas,
        Object.entries(this.statsData?.roomAvailabilityByHour || {}).map(([k, v]) => ({ label: k, value: v as number })),
        '#15803D', '%');
    }
    if (this.activeTab === 'teachers') {
      this.drawBarChart(this.barTeacherCanvas,
        (this.statsData?.coursesByTeacher || []).slice(0, 10).map(t => ({ label: t.teacherName.split(' ')[0], value: t.totalHours })),
        '#7C3AED', 'h');
    }
  }

  private drawBarChart(
    ref: ElementRef<HTMLCanvasElement> | undefined,
    data: { label: string; value: number }[],
    color: string, unit: string
  ): void {
    if (!ref?.nativeElement) return;
    const canvas = ref.nativeElement;
    const ctx = canvas.getContext('2d'); if (!ctx) return;
    const w = canvas.width = canvas.offsetWidth || 400;
    const h = canvas.height = 220;
    ctx.clearRect(0, 0, w, h);
    if (!data.length) {
      ctx.fillStyle = '#9CA3AF'; ctx.font = '13px sans-serif'; ctx.textAlign = 'center';
      ctx.fillText('Aucune donnée', w / 2, h / 2); return;
    }
    const p = { top: 20, bottom: 50, left: 45, right: 10 };
    const cW = w - p.left - p.right; const cH = h - p.top - p.bottom;
    const maxVal = Math.max(...data.map(d => d.value), 1);
    const barW = Math.max(8, (cW / data.length) * 0.55);
    const gap  = cW / data.length;
    [0, 0.25, 0.5, 0.75, 1].forEach(frac => {
      const y = p.top + cH - frac * cH;
      ctx.strokeStyle = '#E5E7EB'; ctx.lineWidth = 1;
      ctx.beginPath(); ctx.moveTo(p.left, y); ctx.lineTo(w - p.right, y); ctx.stroke();
      ctx.fillStyle = '#9CA3AF'; ctx.font = '10px sans-serif'; ctx.textAlign = 'right';
      ctx.fillText(`${Math.round(frac * maxVal)}${unit}`, p.left - 4, y + 3);
    });
    data.forEach((d, i) => {
      const x  = p.left + i * gap + (gap - barW) / 2;
      const bH = (d.value / maxVal) * cH;
      const y  = p.top + cH - bH;
      ctx.fillStyle = '#F3F4F6'; ctx.fillRect(x, p.top, barW, cH);
      ctx.fillStyle = color;     ctx.fillRect(x, y, barW, bH);
      ctx.fillStyle = '#6B7280'; ctx.font = '10px sans-serif'; ctx.textAlign = 'center';
      const lbl = d.label.length > 8 ? d.label.substring(0, 7) + '.' : d.label;
      ctx.fillText(lbl, x + barW / 2, h - 8);
      if (bH > 16) {
        ctx.fillStyle = 'white'; ctx.font = 'bold 9px sans-serif';
        ctx.fillText(`${Math.round(d.value)}${unit}`, x + barW / 2, y + 12);
      }
    });
  }

  private drawPieChart(
    ref: ElementRef<HTMLCanvasElement> | undefined,
    data: { key: string; value: number }[]
  ): void {
    if (!ref?.nativeElement) return;
    const canvas = ref.nativeElement;
    const ctx = canvas.getContext('2d'); if (!ctx) return;
    const w = canvas.width = canvas.offsetWidth || 300;
    const h = canvas.height = 200;
    ctx.clearRect(0, 0, w, h);
    if (!data.length) {
      ctx.fillStyle = '#9CA3AF'; ctx.font = '13px sans-serif'; ctx.textAlign = 'center';
      ctx.fillText('Aucune donnée', w / 2, h / 2); return;
    }
    const total = data.reduce((s, d) => s + d.value, 0) || 1;
    const cx = w * 0.38; const cy = h / 2; const r = Math.min(cx, cy) - 10;
    let angle = -Math.PI / 2;
    data.slice(0, 8).forEach((d, i) => {
      const slice = (d.value / total) * 2 * Math.PI;
      ctx.beginPath(); ctx.moveTo(cx, cy);
      ctx.arc(cx, cy, r, angle, angle + slice); ctx.closePath();
      ctx.fillStyle = this.CHART_COLORS[i % this.CHART_COLORS.length]; ctx.fill();
      ctx.strokeStyle = 'white'; ctx.lineWidth = 2; ctx.stroke();
      angle += slice;
    });
    const legendX = w * 0.72; let legendY = 20;
    data.slice(0, 8).forEach((d, i) => {
      ctx.fillStyle = this.CHART_COLORS[i % this.CHART_COLORS.length];
      ctx.fillRect(legendX - 16, legendY - 8, 10, 10);
      ctx.fillStyle = '#374151'; ctx.font = '10px sans-serif'; ctx.textAlign = 'left';
      const lbl = d.key.length > 12 ? d.key.substring(0, 11) + '.' : d.key;
      ctx.fillText(`${lbl} (${Math.round(d.value / total * 100)}%)`, legendX - 2, legendY);
      legendY += 18;
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  typeLabel(t: ReportType): string   { return ReportingService.typeLabel(t); }
  statusLabel(s: string): string     { return ReportingService.statusLabel(s as any); }
  statusClass(s: string): string     { return ReportingService.statusClass(s as any); }
  formatIcon(f: ReportFormat): string {
    return f === 'PDF' ? 'picture_as_pdf' : f === 'EXCEL' ? 'table_chart' : 'description';
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  private toast(msg: string, error = false): void {
    this.toastMsg = msg; this.toastError = error; this.showToast = true;
    setTimeout(() => this.showToast = false, 4500);
  }

  private defaultStartDate(): string {
    const d = new Date(); d.setDate(d.getDate() - 7);
    return d.toISOString().split('T')[0];
  }
}
