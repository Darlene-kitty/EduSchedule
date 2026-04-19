import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';
import { ScheduleManagementService } from '../../core/services/schedule-management.service';
import { ICalExportService } from '../../core/services/ical-export.service';
import { NotificationsManagementService, Notification as ApiNotification } from '../../core/services/notifications-management.service';

interface SeanceEtudiant {
  id: number;
  matiere: string;
  type: string;
  enseignant: string;
  salle: string;
  groupe: string;
  jour: string;
  heureDebut: string;
  heureFin: string;
  couleur: string;
}

@Component({
  selector: 'app-student-schedule',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  templateUrl: './student-schedule.html',
  styleUrl: './student-schedule.css'
})
export class StudentScheduleComponent implements OnInit {
  private authService     = inject(AuthService);
  private scheduleService = inject(ScheduleManagementService);
  private icalSvc         = inject(ICalExportService);
  private notifService    = inject(NotificationsManagementService);

  currentUser: any = null;
  currentDate = '';
  currentTime = '';
  isLoading   = false;

  // Onglet actif
  activeTab: 'edt' | 'notifications' | 'profil' = 'edt';

  // Notifications
  notifications: ApiNotification[] = [];
  isLoadingNotifs = false;

  get unreadCount(): number {
    return this.notifications.filter(n => !n.read && n.status !== 'READ').length;
  }

  semaineActive = 1;
  readonly jours    = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
  readonly creneaux = ['07:00','08:00','09:00','10:00','11:00','12:00','13:00','14:00','15:00','16:00','17:00','18:00','19:00'];

  readonly typeColors: Record<string, string> = {
    CM: '#1D4ED8', TD: '#15803D', TP: '#7C3AED', Séminaire: '#EA580C'
  };

  seances: SeanceEtudiant[] = [];

  // Toast
  showToast = false;
  toastMsg  = '';

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.currentUser = this.authService.getUser();
    this.loadSchedule();
    this.loadNotifications();
  }

  private loadNotifications(): void {
    this.isLoadingNotifs = true;
    this.notifService.getNotifications().subscribe({
      next: data => { this.notifications = data || []; this.isLoadingNotifs = false; },
      error: () => { this.notifications = []; this.isLoadingNotifs = false; }
    });
  }

  markAllRead(): void {
    this.notifications.filter(n => !n.read).forEach(n => {
      this.notifService.markAsRead(n.id).subscribe();
      n.read = true;
    });
  }

  dismissNotif(id: number): void {
    this.notifService.deleteNotification(id).subscribe();
    this.notifications = this.notifications.filter(n => n.id !== id);
  }

  getNotifIcon(type: string): string {
    if (type?.includes('warning') || type?.includes('CONFLICT')) return 'warning';
    if (type?.includes('success') || type?.includes('approved')) return 'check_circle';
    if (type?.includes('error')   || type?.includes('rejected')) return 'cancel';
    return 'notifications';
  }

  getNotifColor(type: string): string {
    if (type?.includes('warning') || type?.includes('CONFLICT')) return '#F59E0B';
    if (type?.includes('success') || type?.includes('approved')) return '#10B981';
    if (type?.includes('error')   || type?.includes('rejected')) return '#EF4444';
    return '#3B82F6';
  }

  private loadSchedule(): void {
    this.isLoading = true;

    // Groupe de l'étudiant connecté (stocké dans son profil JWT)
    const studentGroup: string = this.currentUser?.group
      || this.currentUser?.classe
      || this.currentUser?.groupName
      || '';

    this.scheduleService.getSchedule().subscribe({
      next: data => {
        const jours = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
        const all = (data || []).map(e => ({
          id: e.id,
          matiere:    e.courseName || e.title || 'Cours',
          type:       'CM',
          enseignant: e.teacher || '',
          salle:      e.room    || '',
          groupe:     e.group   || '',
          jour:       typeof e.dayOfWeek === 'number' ? (jours[e.dayOfWeek] || 'Lundi') : 'Lundi',
          heureDebut: e.startTime || '08:00',
          heureFin:   e.endTime   || '10:00',
          couleur:    e.color     || '#1D4ED8',
        }));

        // Filtrer par groupe si l'étudiant en a un, sinon afficher tout
        this.seances = studentGroup
          ? all.filter(s => !s.groupe || s.groupe === studentGroup)
          : all;

        this.isLoading = false;
      },
      error: () => { this.seances = []; this.isLoading = false; }
    });
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  getSeancesCreneau(jour: string, heure: string): SeanceEtudiant[] {
    return this.seances.filter(s => s.jour === jour && s.heureDebut === heure);
  }

  getSeancesJour(jour: string): SeanceEtudiant[] {
    return this.seances.filter(s => s.jour === jour);
  }

  dureeMinutes(debut: string, fin: string): number {
    const [dh, dm] = debut.split(':').map(Number);
    const [fh, fm] = fin.split(':').map(Number);
    return (fh * 60 + fm) - (dh * 60 + dm);
  }

  getSeanceHeight(s: SeanceEtudiant): string {
    return `${Math.max(this.dureeMinutes(s.heureDebut, s.heureFin) - 8, 40)}px`;
  }

  get totalSeancesSemaine(): number { return this.seances.length; }

  get sallesUniques(): number {
    return new Set(this.seances.map(s => s.salle).filter(Boolean)).size;
  }

  get prochainCours(): SeanceEtudiant | null {
    const now = new Date();
    const jourActuel = ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'][now.getDay()];
    const heureActuelle = `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`;
    return this.seances.find(s => s.jour === jourActuel && s.heureDebut >= heureActuelle) ?? null;
  }

  exportIcal(): void {
    if (!this.seances.length) { this.toast('Aucune séance à exporter.'); return; }
    const events = this.seances.map(s => ({
      uid:         `student-${s.id}@iusjc.cm`,
      summary:     `[${s.type}] ${s.matiere}`,
      description: `Enseignant: ${s.enseignant}\\nGroupe: ${s.groupe}`,
      location:    s.salle,
      dtstart:     this.icalSvc.seanceToICalDate(s.jour, s.heureDebut, this.semaineActive - 1),
      dtend:       this.icalSvc.seanceToICalDate(s.jour, s.heureFin,   this.semaineActive - 1),
    }));
    this.icalSvc.exportToIcal(events, 'mon-edt-iusjc.ics');
    this.toast(`${events.length} séance(s) exportée(s)`);
  }

  getUserName(): string {
    return this.currentUser?.name || this.currentUser?.username || 'Étudiant';
  }

  getUserInitials(): string {
    return this.getUserName().split(' ').map((n: string) => n[0]).slice(0, 2).join('').toUpperCase();
  }

  toast(msg: string): void {
    this.toastMsg = msg; this.showToast = true;
    setTimeout(() => this.showToast = false, 3500);
  }

  logout(): void {
    this.authService.logout();
  }
}
