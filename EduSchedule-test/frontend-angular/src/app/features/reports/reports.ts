import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { DashboardService } from '../../core/services/dashboard.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './reports.html',
  styleUrl: './reports.css'
})
export class ReportsComponent implements OnInit, AfterViewInit {
  private dashboardService = inject(DashboardService);

  currentDate = ''; currentTime = '';
  selectedPeriod = 'Cette semaine';
  isExporting = false;
  showExportToast = false;

  @ViewChild('barCanvas') barCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('pieCanvas') pieCanvas!: ElementRef<HTMLCanvasElement>;

  stats = [
    { label: "Taux d'occupation moyen", value: '72%',   change: '+5% vs semaine précédente', icon: 'trending_up' },
    { label: 'Heures de cours/semaine', value: '384h',  change: '+8h vs semaine précédente',  icon: 'calendar_today' },
    { label: 'Salles actives',          value: '18/24', change: '75% vs semaine précédente',  icon: 'article' },
  ];

  barData = [
    { day: 'Lun', value: 18, max: 24 },
    { day: 'Mar', value: 20, max: 24 },
    { day: 'Mer', value: 16, max: 24 },
    { day: 'Jeu', value: 22, max: 24 },
    { day: 'Ven', value: 19, max: 24 },
  ];

  pieData = [
    { label: 'Bâtiment A',   value: 75, color: '#1D4ED8' },
    { label: 'Bâtiment B',   value: 68, color: '#15803D' },
    { label: 'Bâtiment C',   value: 52, color: '#F59E0B' },
    { label: 'Laboratoires', value: 85, color: '#3B82F6' },
  ];

  /* Détail tableau pour le PDF */
  reportRows = [
    { room: 'Salle A101', occupancy: '75%', courses: 12, hours: '36h', status: 'Actif' },
    { room: 'Salle A102', occupancy: '68%', courses: 10, hours: '30h', status: 'Actif' },
    { room: 'Salle B203', occupancy: '52%', courses: 8,  hours: '24h', status: 'Actif' },
    { room: 'Lab C305',   occupancy: '90%', courses: 15, hours: '45h', status: 'Actif' },
    { room: 'Amphi A',    occupancy: '85%', courses: 6,  hours: '18h', status: 'Actif' },
  ];

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadStats();
  }

  loadStats(): void {
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = [
          { label: "Taux d'occupation moyen", value: `${data.occupancyRate}%`, change: 'Données en temps réel', icon: 'trending_up' },
          { label: 'Réservations actives',    value: String(data.activeReservations), change: 'Données en temps réel', icon: 'calendar_today' },
          { label: 'Salles actives',          value: `${data.totalRooms}`, change: `${data.activeUsers} utilisateurs actifs`, icon: 'article' },
        ];
      },
      error: () => {} // garde les stats démo
    });
    this.dashboardService.getOccupancyByBuilding().subscribe({
      next: (buildings) => {
        if (buildings && buildings.length > 0) {
          this.pieData = buildings.map((b, i) => ({
            label: b.name,
            value: b.percentage,
            color: ['#1D4ED8','#15803D','#F59E0B','#3B82F6','#7C3AED'][i % 5]
          }));
        }
      },
      error: () => {}
    });
  }
  ngAfterViewInit(): void { this.drawBarChart(); this.drawPieChart(); }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  /* ─── Export PDF réel via fenêtre d'impression ─── */
  exportPdf(): void {
    this.isExporting = true;

    // Générer le HTML du rapport dans une nouvelle fenêtre
    const reportHtml = this.buildPdfHtml();
    const printWindow = window.open('', '_blank', 'width=900,height=700');
    if (!printWindow) { this.isExporting = false; return; }

    printWindow.document.write(reportHtml);
    printWindow.document.close();

    // Attendre le chargement puis déclencher l'impression
    printWindow.onload = () => {
      setTimeout(() => {
        printWindow.focus();
        printWindow.print();
        printWindow.close();
        this.isExporting = false;
        this.showExportToast = true;
        setTimeout(() => this.showExportToast = false, 4000);
      }, 500);
    };
  }

  private buildPdfHtml(): string {
    const now = new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' });
    const rows = this.reportRows.map(r => `
      <tr>
        <td>${r.room}</td>
        <td>${r.occupancy}</td>
        <td>${r.courses}</td>
        <td>${r.hours}</td>
        <td><span style="color:#15803D;font-weight:600">${r.status}</span></td>
      </tr>`).join('');

    const barBars = this.barData.map(d => `
      <div style="display:flex;flex-direction:column;align-items:center;gap:6px;flex:1">
        <span style="font-size:12px;font-weight:600;color:#15803D">${d.value}</span>
        <div style="width:40px;background:#E5E7EB;border-radius:4px;height:120px;display:flex;align-items:flex-end">
          <div style="width:100%;height:${Math.round((d.value/d.max)*120)}px;background:#15803D;border-radius:4px"></div>
        </div>
        <span style="font-size:11px;color:#6B7280">${d.day}</span>
      </div>`).join('');

    const statsHtml = this.stats.map(s => `
      <div style="flex:1;background:#F0FDF4;border-radius:10px;padding:16px;text-align:center">
        <p style="font-size:1.8rem;font-weight:700;color:#15803D;margin:0 0 4px">${s.value}</p>
        <p style="font-size:0.8rem;color:#374151;margin:0 0 4px;font-weight:600">${s.label}</p>
        <p style="font-size:0.72rem;color:#6B7280;margin:0">${s.change}</p>
      </div>`).join('');

    return `<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8">
<title>Rapport EduSchedule — ${this.selectedPeriod}</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; color: #111827; background: white; padding: 40px; }
  h1 { font-size: 1.6rem; font-weight: 700; color: #111827; }
  h2 { font-size: 1rem; font-weight: 600; color: #374151; margin-bottom: 16px; }
  table { width: 100%; border-collapse: collapse; margin-top: 8px; }
  th { background: #F3F4F6; padding: 10px 14px; text-align: left; font-size: 0.78rem; text-transform: uppercase; color: #6B7280; letter-spacing: 0.04em; }
  td { padding: 12px 14px; border-bottom: 1px solid #F3F4F6; font-size: 0.875rem; }
  tr:last-child td { border-bottom: none; }
  @media print { body { padding: 20px; } @page { margin: 1.5cm; } }
</style>
</head>
<body>
  <!-- En-tête -->
  <div style="display:flex;justify-content:space-between;align-items:flex-start;padding-bottom:24px;border-bottom:2px solid #15803D;margin-bottom:28px">
    <div>
      <div style="display:flex;align-items:center;gap:10px;margin-bottom:8px">
        <div style="width:38px;height:38px;background:#15803D;border-radius:8px;display:flex;align-items:center;justify-content:center">
          <span style="color:white;font-size:1.2rem">📅</span>
        </div>
        <span style="font-size:1.1rem;font-weight:700;color:#15803D">EduSchedule</span>
      </div>
      <h1>Rapport — ${this.selectedPeriod}</h1>
      <p style="font-size:0.85rem;color:#6B7280;margin-top:4px">Analyse de l'utilisation des salles et répartition des cours</p>
    </div>
    <div style="text-align:right;font-size:0.8rem;color:#6B7280">
      <p style="font-weight:600;color:#111827">Généré le ${now}</p>
      <p>EduSchedule — Admin Système</p>
    </div>
  </div>

  <!-- Stats -->
  <div style="display:flex;gap:16px;margin-bottom:28px">${statsHtml}</div>

  <!-- Graphique barres -->
  <div style="background:#F9FAFB;border-radius:12px;padding:20px;margin-bottom:24px">
    <h2>Occupation des salles par jour</h2>
    <div style="display:flex;align-items:flex-end;gap:8px;height:160px;padding:0 20px">${barBars}</div>
  </div>

  <!-- Tableau détaillé -->
  <div style="background:#F9FAFB;border-radius:12px;padding:20px;margin-bottom:24px">
    <h2>Détail par salle</h2>
    <table>
      <thead><tr><th>Salle</th><th>Taux d'occupation</th><th>Nb cours</th><th>Volume horaire</th><th>Statut</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>
  </div>

  <!-- Légende taux -->
  <div style="background:#F9FAFB;border-radius:12px;padding:20px">
    <h2>Taux d'occupation par bâtiment</h2>
    <div style="display:flex;flex-direction:column;gap:12px;margin-top:8px">
      ${this.pieData.map(d => `
      <div style="display:flex;align-items:center;gap:12px">
        <div style="width:14px;height:14px;border-radius:50%;background:${d.color};flex-shrink:0"></div>
        <span style="flex:1;font-size:0.875rem;color:#374151">${d.label}</span>
        <div style="flex:2;background:#E5E7EB;border-radius:99px;height:8px">
          <div style="width:${d.value}%;background:${d.color};border-radius:99px;height:100%"></div>
        </div>
        <span style="font-weight:600;font-size:0.875rem;color:#111827;min-width:36px;text-align:right">${d.value}%</span>
      </div>`).join('')}
    </div>
  </div>

  <div style="margin-top:32px;padding-top:16px;border-top:1px solid #E5E7EB;text-align:center;font-size:0.75rem;color:#9CA3AF">
    EduSchedule • Rapport généré automatiquement le ${now} • Période : ${this.selectedPeriod}
  </div>
</body>
</html>`;
  }

  /* ── Charts ── */
  drawBarChart(): void {
    const canvas = this.barCanvas?.nativeElement; if (!canvas) return;
    const ctx = canvas.getContext('2d'); if (!ctx) return;
    const w = canvas.width = canvas.offsetWidth; const h = canvas.height = 260;
    ctx.clearRect(0, 0, w, h);
    const p = { top: 20, bottom: 40, left: 40, right: 20 };
    const cW = w - p.left - p.right; const cH = h - p.top - p.bottom;
    const barW = (cW / this.barData.length) * 0.5; const gap = cW / this.barData.length; const maxV = 24;
    [0, 6, 12, 18, 24].forEach(v => {
      const y = p.top + cH - (v / maxV) * cH;
      ctx.strokeStyle = '#E5E7EB'; ctx.lineWidth = 1;
      ctx.beginPath(); ctx.moveTo(p.left, y); ctx.lineTo(w - p.right, y); ctx.stroke();
      ctx.fillStyle = '#9CA3AF'; ctx.font = '11px sans-serif'; ctx.fillText(String(v), 0, y + 4);
    });
    this.barData.forEach((d, i) => {
      const x = p.left + i * gap + (gap - barW) / 2;
      const bH = (d.value / maxV) * cH; const y = p.top + cH - bH;
      ctx.fillStyle = '#E5E7EB'; ctx.beginPath(); (ctx as any).roundRect(x, p.top, barW, cH, 4); ctx.fill();
      ctx.fillStyle = '#15803D'; ctx.beginPath(); (ctx as any).roundRect(x, y, barW, bH, 4); ctx.fill();
      ctx.fillStyle = '#6B7280'; ctx.font = '12px sans-serif'; ctx.textAlign = 'center';
      ctx.fillText(d.day, x + barW / 2, h - 10);
    });
  }

  drawPieChart(): void {
    const canvas = this.pieCanvas?.nativeElement; if (!canvas) return;
    const ctx = canvas.getContext('2d'); if (!ctx) return;
    const w = canvas.width = canvas.offsetWidth; const h = canvas.height = 260;
    ctx.clearRect(0, 0, w, h);
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
}