import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService, OccupancyBuilding, UpcomingCourse, RecentActivity } from '../../core/services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatIconModule, SidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  private authService    = inject(AuthService);
  private dashboardService = inject(DashboardService);

  currentDate = '';
  currentTime = '';
  currentUser: any = null;
  private clockInterval: any;

  stats = [
    { label: 'Utilisateurs actifs',  value: '0', change: '—',  icon: 'people',         color: 'stat-blue'   },
    { label: 'Réservations actives', value: '0', change: '—',  icon: 'calendar_today',  color: 'stat-green'  },
    { label: 'Total salles',         value: '0', change: '—',  icon: 'meeting_room',    color: 'stat-purple' },
    { label: "Taux d'occupation",    value: '0%', change: '—', icon: 'bar_chart',       color: 'stat-orange' },
  ];

  occupancyBuildings: OccupancyBuilding[] = [];
  upcomingCourses: UpcomingCourse[]       = [];
  recentActivities: RecentActivity[]      = [];
  efficiencyScore = 0;
  coursesToday    = 0;

  // ── Chart canvas refs ──────────────────────────────────────────────────────
  @ViewChild('occupancyBarCanvas') occupancyBarCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('weekLineCanvas')     weekLineCanvas!:     ElementRef<HTMLCanvasElement>;
  @ViewChild('coursesPieCanvas')   coursesPieCanvas!:   ElementRef<HTMLCanvasElement>;

  // Données brutes pour les graphiques
  private weekReservations: number[] = [];
  private coursesByType: { label: string; value: number }[] = [];
  private chartsReady = false;

  private readonly CHART_COLORS = [
    '#3B82F6', '#10B981', '#8B5CF6', '#F97316',
    '#EF4444', '#06B6D4', '#84CC16', '#EC4899'
  ];

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.updateDateTime();
    this.clockInterval = setInterval(() => this.updateDateTime(), 1000);
    this.loadUserData();
    this.loadDashboardData();
  }

  ngAfterViewInit(): void {
    this.chartsReady = true;
    // Si les données sont déjà là (réponse rapide), on dessine
    if (this.occupancyBuildings.length) this.drawAllCharts();
  }

  ngOnDestroy(): void {
    clearInterval(this.clockInterval);
  }

  // ── Data loading ───────────────────────────────────────────────────────────

  private loadUserData(): void {
    this.authService.user$.subscribe(user => { this.currentUser = user; });
  }

  private loadDashboardData(): void {
    this.dashboardService.getStats().subscribe(stats => {
      this.stats = [
        { label: 'Utilisateurs actifs',  value: stats.activeUsers.toString(),        change: stats.trends.users,        icon: 'people',         color: 'stat-blue'   },
        { label: 'Réservations actives', value: stats.activeReservations.toString(), change: stats.trends.reservations, icon: 'calendar_today', color: 'stat-green'  },
        { label: 'Total salles',         value: stats.totalRooms.toString(),         change: stats.trends.rooms,        icon: 'meeting_room',   color: 'stat-purple' },
        { label: "Taux d'occupation",    value: stats.occupancyRate + '%',           change: stats.trends.occupancy,    icon: 'bar_chart',      color: 'stat-orange' },
      ];
      this.efficiencyScore = stats.efficiencyScore;
      this.coursesToday    = stats.coursesToday;

      // Simuler les réservations sur 7 jours à partir du total actif
      this.weekReservations = this.buildWeekSeries(stats.activeReservations);
      if (this.chartsReady) this.drawWeekLine();
    });

    this.dashboardService.getOccupancyByBuilding().subscribe(data => {
      this.occupancyBuildings = data;
      if (this.chartsReady) this.drawOccupancyBar();
    });

    this.dashboardService.getUpcomingCourses().subscribe(courses => {
      this.upcomingCourses = courses;
      // Construire la répartition par type de cours pour le pie
      const typeMap: Record<string, number> = {};
      courses.forEach(c => {
        const key = c.subject.split(' ')[0] || 'Autre';
        typeMap[key] = (typeMap[key] || 0) + 1;
      });
      this.coursesByType = Object.entries(typeMap).map(([label, value]) => ({ label, value }));
      if (this.chartsReady) this.drawCoursesPie();
    });

    this.dashboardService.getRecentActivities().subscribe(activities => {
      this.recentActivities = activities;
    });
  }

  /** Génère une série de 7 valeurs plausibles autour d'un total */
  private buildWeekSeries(total: number): number[] {
    const base = Math.max(1, Math.round(total / 7));
    const days = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
    // Variation ±40% autour de la base, avec un pic en milieu de semaine
    const weights = [0.8, 1.0, 1.2, 1.3, 1.1, 0.6, 0.3];
    return days.map((_, i) => Math.max(0, Math.round(base * weights[i])));
  }

  // ── Chart drawing ──────────────────────────────────────────────────────────

  private drawAllCharts(): void {
    this.drawOccupancyBar();
    this.drawWeekLine();
    this.drawCoursesPie();
  }

  /** Graphique en barres : occupation par bâtiment */
  private drawOccupancyBar(): void {
    const canvas = this.occupancyBarCanvas?.nativeElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const data = this.occupancyBuildings.length
      ? this.occupancyBuildings.map(b => ({ label: b.name, value: b.percentage }))
      : [{ label: 'Bât. A', value: 72 }, { label: 'Bât. B', value: 45 }, { label: 'Bât. C', value: 88 }, { label: 'Bât. D', value: 30 }];

    const dpr = window.devicePixelRatio || 1;
    const W   = canvas.offsetWidth  || 400;
    const H   = 200;
    canvas.width  = W * dpr;
    canvas.height = H * dpr;
    canvas.style.width  = W + 'px';
    canvas.style.height = H + 'px';
    ctx.scale(dpr, dpr);
    ctx.clearRect(0, 0, W, H);

    const p = { top: 16, bottom: 44, left: 36, right: 12 };
    const cW = W - p.left - p.right;
    const cH = H - p.top - p.bottom;
    const maxVal = 100;
    const barW   = Math.max(20, (cW / data.length) * 0.55);
    const gap    = cW / data.length;

    // Grille horizontale
    [0, 25, 50, 75, 100].forEach(v => {
      const y = p.top + cH - (v / maxVal) * cH;
      ctx.strokeStyle = '#F3F4F6';
      ctx.lineWidth   = 1;
      ctx.beginPath(); ctx.moveTo(p.left, y); ctx.lineTo(W - p.right, y); ctx.stroke();
      ctx.fillStyle = '#9CA3AF';
      ctx.font      = '10px system-ui';
      ctx.textAlign = 'right';
      ctx.fillText(`${v}%`, p.left - 4, y + 3);
    });

    data.forEach((d, i) => {
      const x  = p.left + i * gap + (gap - barW) / 2;
      const bH = (d.value / maxVal) * cH;
      const y  = p.top + cH - bH;

      // Fond de barre
      ctx.fillStyle = '#F3F4F6';
      ctx.beginPath();
      ctx.roundRect(x, p.top, barW, cH, 4);
      ctx.fill();

      // Barre colorée
      const color = d.value >= 80 ? '#EF4444' : d.value >= 50 ? '#3B82F6' : '#10B981';
      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.roundRect(x, y, barW, bH, 4);
      ctx.fill();

      // Valeur au-dessus
      ctx.fillStyle = '#374151';
      ctx.font      = 'bold 10px system-ui';
      ctx.textAlign = 'center';
      ctx.fillText(`${d.value}%`, x + barW / 2, y - 4);

      // Label en bas
      ctx.fillStyle = '#6B7280';
      ctx.font      = '10px system-ui';
      const lbl = d.label.length > 10 ? d.label.substring(0, 9) + '.' : d.label;
      ctx.fillText(lbl, x + barW / 2, H - 8);
    });
  }

  /** Graphique en ligne : réservations sur 7 jours */
  private drawWeekLine(): void {
    const canvas = this.weekLineCanvas?.nativeElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const labels = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
    const data   = this.weekReservations.length === 7
      ? this.weekReservations
      : [4, 7, 6, 9, 8, 3, 1];

    const dpr = window.devicePixelRatio || 1;
    const W   = canvas.offsetWidth  || 400;
    const H   = 200;
    canvas.width  = W * dpr;
    canvas.height = H * dpr;
    canvas.style.width  = W + 'px';
    canvas.style.height = H + 'px';
    ctx.scale(dpr, dpr);
    ctx.clearRect(0, 0, W, H);

    const p      = { top: 16, bottom: 36, left: 36, right: 16 };
    const cW     = W - p.left - p.right;
    const cH     = H - p.top - p.bottom;
    const maxVal = Math.max(...data, 1);
    const stepX  = cW / (data.length - 1);

    const xOf = (i: number) => p.left + i * stepX;
    const yOf = (v: number) => p.top + cH - (v / maxVal) * cH;

    // Grille
    [0, 0.25, 0.5, 0.75, 1].forEach(frac => {
      const y = p.top + cH - frac * cH;
      ctx.strokeStyle = '#F3F4F6';
      ctx.lineWidth   = 1;
      ctx.beginPath(); ctx.moveTo(p.left, y); ctx.lineTo(W - p.right, y); ctx.stroke();
      ctx.fillStyle = '#9CA3AF';
      ctx.font      = '10px system-ui';
      ctx.textAlign = 'right';
      ctx.fillText(String(Math.round(frac * maxVal)), p.left - 4, y + 3);
    });

    // Aire sous la courbe (dégradé)
    const grad = ctx.createLinearGradient(0, p.top, 0, p.top + cH);
    grad.addColorStop(0, 'rgba(59,130,246,0.25)');
    grad.addColorStop(1, 'rgba(59,130,246,0)');
    ctx.beginPath();
    ctx.moveTo(xOf(0), yOf(data[0]));
    data.forEach((v, i) => { if (i > 0) ctx.lineTo(xOf(i), yOf(v)); });
    ctx.lineTo(xOf(data.length - 1), p.top + cH);
    ctx.lineTo(xOf(0), p.top + cH);
    ctx.closePath();
    ctx.fillStyle = grad;
    ctx.fill();

    // Ligne
    ctx.beginPath();
    ctx.moveTo(xOf(0), yOf(data[0]));
    data.forEach((v, i) => { if (i > 0) ctx.lineTo(xOf(i), yOf(v)); });
    ctx.strokeStyle = '#3B82F6';
    ctx.lineWidth   = 2.5;
    ctx.lineJoin    = 'round';
    ctx.stroke();

    // Points
    data.forEach((v, i) => {
      ctx.beginPath();
      ctx.arc(xOf(i), yOf(v), 4, 0, Math.PI * 2);
      ctx.fillStyle   = '#3B82F6';
      ctx.fill();
      ctx.strokeStyle = 'white';
      ctx.lineWidth   = 2;
      ctx.stroke();
    });

    // Labels jours
    labels.forEach((lbl, i) => {
      ctx.fillStyle = '#6B7280';
      ctx.font      = '10px system-ui';
      ctx.textAlign = 'center';
      ctx.fillText(lbl, xOf(i), H - 6);
    });
  }

  /** Graphique circulaire : répartition des cours */
  private drawCoursesPie(): void {
    const canvas = this.coursesPieCanvas?.nativeElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const data = this.coursesByType.length
      ? this.coursesByType
      : [
          { label: 'CM',  value: 8 },
          { label: 'TD',  value: 12 },
          { label: 'TP',  value: 6 },
          { label: 'Exam', value: 3 },
        ];

    const dpr = window.devicePixelRatio || 1;
    const W   = canvas.offsetWidth  || 300;
    const H   = 200;
    canvas.width  = W * dpr;
    canvas.height = H * dpr;
    canvas.style.width  = W + 'px';
    canvas.style.height = H + 'px';
    ctx.scale(dpr, dpr);
    ctx.clearRect(0, 0, W, H);

    const total = data.reduce((s, d) => s + d.value, 0) || 1;
    const cx    = W * 0.38;
    const cy    = H / 2;
    const r     = Math.min(cx, cy) - 14;
    const rInner = r * 0.52; // donut

    let angle = -Math.PI / 2;
    data.forEach((d, i) => {
      const slice = (d.value / total) * 2 * Math.PI;
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.arc(cx, cy, r, angle, angle + slice);
      ctx.closePath();
      ctx.fillStyle = this.CHART_COLORS[i % this.CHART_COLORS.length];
      ctx.fill();
      angle += slice;
    });

    // Trou central (donut)
    ctx.beginPath();
    ctx.arc(cx, cy, rInner, 0, Math.PI * 2);
    ctx.fillStyle = 'white';
    ctx.fill();

    // Texte central
    ctx.fillStyle = '#111827';
    ctx.font      = 'bold 16px system-ui';
    ctx.textAlign = 'center';
    ctx.fillText(String(total), cx, cy + 2);
    ctx.fillStyle = '#9CA3AF';
    ctx.font      = '10px system-ui';
    ctx.fillText('cours', cx, cy + 16);

    // Légende
    const legendX = W * 0.70;
    let legendY   = H / 2 - (data.length * 18) / 2 + 8;
    data.forEach((d, i) => {
      ctx.fillStyle = this.CHART_COLORS[i % this.CHART_COLORS.length];
      ctx.beginPath();
      ctx.roundRect(legendX - 14, legendY - 8, 10, 10, 2);
      ctx.fill();
      ctx.fillStyle = '#374151';
      ctx.font      = '11px system-ui';
      ctx.textAlign = 'left';
      const pct = Math.round((d.value / total) * 100);
      ctx.fillText(`${d.label} (${pct}%)`, legendX, legendY);
      legendY += 20;
    });
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', {
      weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  getActivityClass(type: string): string {
    switch (type) {
      case 'success': return 'activity-success';
      case 'warning': return 'activity-warning';
      case 'error':   return 'activity-error';
      default:        return 'activity-success';
    }
  }

  getOccupancyColor(percentage: number): string {
    if (percentage >= 80) return '#EF4444';
    if (percentage >= 60) return '#3B82F6';
    return '#10B981';
  }

  getUserName(): string {
    return this.currentUser?.name || this.currentUser?.username || 'Utilisateur';
  }

  getUserInitials(): string {
    const name = this.getUserName();
    return name.split(' ').map((n: string) => n[0]).slice(0, 2).join('').toUpperCase();
  }

  refreshDashboard(): void {
    this.loadDashboardData();
  }
}
