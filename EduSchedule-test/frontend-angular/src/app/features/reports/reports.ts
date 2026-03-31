import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { DashboardService } from '../../core/services/dashboard.service';
import { ReportingService, ReportDTO, ReportRequest, ReportType, ReportFormat } from '../../core/services/reporting.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './reports.html',
  styleUrl: './reports.css'
})
export class ReportsComponent implements OnInit, AfterViewInit {
  private dashboardService = inject(DashboardService);
  private reportingService = inject(ReportingService);
  private authService      = inject(AuthService);

  currentDate = ''; currentTime = '';
  selectedPeriod = 'Cette semaine';

  // Génération
  isGenerating = false;
  showToast = false; toastMsg = ''; toastError = false;

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

  // Historique
  recentReports: ReportDTO[] = [];
  isLoadingHistory = false;

  // Stats dashboard
  stats: { label: string; value: string; change: string; icon: string }[] = [];
  barData: { day: string; value: number; max: number }[] = [];
  pieData: { label: string; value: number; color: string }[] = [];
  reportRows: { room: string; occupancy: string; courses: number; hours: string; status: string }[] = [];

  @ViewChild('barCanvas') barCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('pieCanvas') pieCanvas!: ElementRef<HTMLCanvasElement>;

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadDashboardStats();
    this.loadReportHistory();
  }

  ngAfterViewInit(): void { this.drawBarChart(); this.drawPieChart(); }

  // ── Stats ──────────────────────────────────────────────────────────────────

  loadDashboardStats(): void {
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = [
          { label: "Taux d'occupation moyen", value: `${data.occupancyRate}%`,        change: 'Temps réel', icon: 'trending_up' },
          { label: 'Réservations actives',    value: String(data.activeReservations), change: 'Temps réel', icon: 'calendar_today' },
          { label: 'Salles actives',          value: String(data.totalRooms),         change: `${data.activeUsers} utilisateurs`, icon: 'meeting_room' },
        ];
        this.barData = [
          { day: 'Lun', value: Math.min(100, Math.round(data.occupancyRate * 0.9)), max: 100 },
          { day: 'Mar', value: Math.min(100, Math.round(data.occupancyRate * 1.1)), max: 100 },
          { day: 'Mer', value: Math.min(100, Math.round(data.occupancyRate * 0.8)), max: 100 },
          { day: 'Jeu', value: Math.min(100, Math.round(data.occupancyRate * 1.2)), max: 100 },
          { day: 'Ven', value: Math.min(100, Math.round(data.occupancyRate)),       max: 100 },
        ];
        setTimeout(() => { this.drawBarChart(); this.drawPieChart(); }, 50);
      },
      error: () => { this.stats = []; this.barData = []; }
    });
    this.dashboardService.getOccupancyByBuilding().subscribe({
      next: (buildings) => {
        this.pieData = (buildings || []).map((b, i) => ({
          label: b.name, value: b.percentage,
          color: ['#1D4ED8','#15803D','#F59E0B','#3B82F6','#7C3AED'][i % 5]
        }));
        this.reportRows = (buildings || []).map(b => ({
          room: b.name, occupancy: `${b.percentage}%`, courses: 0, hours: '—', status: 'Actif'
        }));
        setTimeout(() => this.drawPieChart(), 50);
      },
      error: () => { this.pieData = []; this.reportRows = []; }
    });
  }

  // ── Historique ─────────────────────────────────────────────────────────────

  loadReportHistory(): void {
    this.isLoadingHistory = true;
    const userId = this.authService.getUser()?.id;
    const obs = userId ? this.reportingService.getMyReports(userId) : this.reportingService.getAll();
    obs.subscribe({
      next: (res) => { this.recentReports = (res.content ?? []).slice(0, 10); this.isLoadingHistory = false; },
      error: () => { this.recentReports = []; this.isLoadingHistory = false; }
    });
  }

  // ── Génération ─────────────────────────────────────────────────────────────

  generateReport(): void {
    if (this.isGenerating) return;
    this.isGenerating = true;
    const request: ReportRequest = {
      ...this.reportForm,
      title: this.reportForm.title || ReportingService.typeLabel(this.reportForm.type),
    };
    this.reportingService.generate(request).subscribe({
      next: (report) => {
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

  exportPdf(): void {
    const printWindow = window.open('', '_blank', 'width=900,height=700');
    if (!printWindow) return;
    printWindow.document.write(this.buildPdfHtml());
    printWindow.document.close();
    printWindow.onload = () => setTimeout(() => { printWindow.focus(); printWindow.print(); printWindow.close(); }, 500);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  typeLabel(t: ReportType): string { return ReportingService.typeLabel(t); }
  statusLabel(s: string): string   { return ReportingService.statusLabel(s as any); }
  statusClass(s: string): string   { return ReportingService.statusClass(s as any); }
  formatIcon(f: ReportFormat): string {
    return f === 'PDF' ? 'picture_as_pdf' : f === 'EXCEL' ? 'table_chart' : 'description';
  }

  private toast(msg: string, error = false): void {
    this.toastMsg = msg; this.toastError = error; this.showToast = true;
    setTimeout(() => this.showToast = false, 4500);
  }

  private defaultStartDate(): string {
    const d = new Date(); d.setDate(d.getDate() - 7);
    return d.toISOString().split('T')[0];
  }

  // ── Charts ─────────────────────────────────────────────────────────────────

  drawBarChart(): void {
    const canvas = this.barCanvas?.nativeElement; if (!canvas) return;
    const ctx = canvas.getContext('2d'); if (!ctx) return;
    const w = canvas.width = canvas.offsetWidth; const h = canvas.height = 260;
    ctx.clearRect(0, 0, w, h);
    if (!this.barData.length) return;
    const p = { top: 20, bottom: 40, left: 40, right: 20 };
    const cW = w - p.left - p.right; const cH = h - p.top - p.bottom;
    const barW = (cW / this.barData.length) * 0.5; const gap = cW / this.barData.length;
    [0, 25, 50, 75, 100].forEach(v => {
      const y = p.top + cH - (v / 100) * cH;
      ctx.strokeStyle = '#E5E7EB'; ctx.lineWidth = 1;
      ctx.beginPath(); ctx.moveTo(p.left, y); ctx.lineTo(w - p.right, y); ctx.stroke();
      ctx.fillStyle = '#9CA3AF'; ctx.font = '11px sans-serif'; ctx.fillText(`${v}%`, 0, y + 4);
    });
    this.barData.forEach((d, i) => {
      const x = p.left + i * gap + (gap - barW) / 2;
      const bH = (d.value / 100) * cH; const y = p.top + cH - bH;
      ctx.fillStyle = '#E5E7EB'; ctx.beginPath(); (ctx as any).roundRect?.(x, p.top, barW, cH, 4); ctx.fill();
      ctx.fillStyle = '#15803D'; ctx.beginPath(); (ctx as any).roundRect?.(x, y, barW, bH, 4); ctx.fill();
      ctx.fillStyle = '#6B7280'; ctx.font = '12px sans-serif'; ctx.textAlign = 'center';
      ctx.fillText(d.day, x + barW / 2, h - 10);
    });
  }

  drawPieChart(): void {
    const canvas = this.pieCanvas?.nativeElement; if (!canvas) return;
    const ctx = canvas.getContext('2d'); if (!ctx) return;
    const w = canvas.width = canvas.offsetWidth; const h = canvas.height = 260;
    ctx.clearRect(0, 0, w, h);
    if (!this.pieData.length) return;
    const cx = w / 2 - 30; const cy = h / 2; const r = Math.min(cx, cy) - 20;
    const total = this.pieData.reduce((s, d) => s + d.value, 0);
    let startAngle = -Math.PI / 2;
    this.pieData.forEach(d => {
      const angle = (d.value / total) * 2 * Math.PI;
      ctx.beginPath(); ctx.moveTo(cx, cy); ctx.arc(cx, cy, r, startAngle, startAngle + angle);
      ctx.closePath(); ctx.fillStyle = d.color; ctx.fill();
      ctx.strokeStyle = 'white'; ctx.lineWidth = 2; ctx.stroke();
      startAngle += angle;
    });
  }

  private buildPdfHtml(): string {
    const now = new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' });
    const rows = this.reportRows.map(r =>
      `<tr><td>${r.room}</td><td>${r.occupancy}</td><td>${r.courses}</td><td>${r.hours}</td><td style="color:#15803D;font-weight:600">${r.status}</td></tr>`
    ).join('');
    const statsHtml = this.stats.map(s =>
      `<div style="flex:1;background:#F0FDF4;border-radius:10px;padding:16px;text-align:center"><p style="font-size:1.8rem;font-weight:700;color:#15803D;margin:0 0 4px">${s.value}</p><p style="font-size:0.8rem;color:#374151;margin:0 0 4px;font-weight:600">${s.label}</p></div>`
    ).join('');
    return `<!DOCTYPE html><html lang="fr"><head><meta charset="UTF-8"><title>Rapport EduSchedule</title><style>*{box-sizing:border-box;margin:0;padding:0}body{font-family:-apple-system,sans-serif;color:#111827;background:white;padding:40px}table{width:100%;border-collapse:collapse}th{background:#F3F4F6;padding:10px 14px;text-align:left;font-size:0.78rem;text-transform:uppercase;color:#6B7280}td{padding:12px 14px;border-bottom:1px solid #F3F4F6;font-size:0.875rem}@media print{body{padding:20px}}</style></head><body><div style="display:flex;justify-content:space-between;padding-bottom:24px;border-bottom:2px solid #15803D;margin-bottom:28px"><h1 style="font-size:1.6rem;font-weight:700">Rapport — ${this.selectedPeriod}</h1><div style="text-align:right;font-size:0.8rem;color:#6B7280"><p>Généré le ${now}</p></div></div><div style="display:flex;gap:16px;margin-bottom:28px">${statsHtml}</div><div style="background:#F9FAFB;border-radius:12px;padding:20px"><h2 style="font-size:1rem;font-weight:600;margin-bottom:16px">Détail par salle</h2><table><thead><tr><th>Salle</th><th>Occupation</th><th>Cours</th><th>Heures</th><th>Statut</th></tr></thead><tbody>${rows}</tbody></table></div></body></html>`;
  }
}
