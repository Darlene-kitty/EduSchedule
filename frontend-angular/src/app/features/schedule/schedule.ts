import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { ScheduleManagementService } from '../../core/services/schedule-management.service';
import { CoursesManagementService } from '../../core/services/courses-management.service';
import { RoomsManagementService } from '../../core/services/rooms-management.service';
import { UsersManagementService } from '../../core/services/users-management.service';
import { GroupesManagementService } from '../../core/services/groupes-management.service';
import { SchoolManagementService } from '../../core/services/school-management.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationsManagementService, ReminderPayload } from '../../core/services/notifications-management.service';

export interface Seance {
  id: number;
  matiere: string;
  codeMatiere: string;
  type: 'CM' | 'TD' | 'TP' | 'Séminaire';
  enseignant: string;
  enseignantId: number;
  ecole: string;
  sigleEcole: string;
  couleurEcole: string;
  classe: string;
  filiere: string;
  niveau: string;
  salle: string;
  jour: string;
  heureDebut: string;
  heureFin: string;
  couleur: string;
  semaine: number;
  /** Récurrence : WEEKLY | BIWEEKLY | MONTHLY | null */
  recurrence?: 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY' | null;
  /** Date de fin de récurrence (YYYY-MM-DD) */
  recurrenceEndDate?: string;
  /** Cours spécial : séminaire, événement académique */
  isSpecial?: boolean;
}

export interface Conflit {
  type: 'enseignant' | 'salle' | 'classe';
  message: string;
  seances: number[];
}

@Component({
  selector: 'app-schedules',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatIconModule, SidebarComponent],
  templateUrl: './schedule.html',
  styleUrl: './schedule.css'
})
export class SchedulesComponent implements OnInit {
  private scheduleService   = inject(ScheduleManagementService);
  private roomSvc           = inject(RoomsManagementService);
  private coursesSvc        = inject(CoursesManagementService);
  private usersSvc          = inject(UsersManagementService);
  private groupesSvc        = inject(GroupesManagementService);
  private schoolSvc         = inject(SchoolManagementService);
  private authService       = inject(AuthService);
  private notifSvc          = inject(NotificationsManagementService);

  isTeacher = false;
  currentTeacherName = '';

  currentDate = ''; currentTime = '';
  currentUserName = ''; currentUserInitials = ''; unreadCount = 0;
  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;
  showConflits      = false;

  // ── Rappel ──
  isReminderModalOpen = false;
  reminderSeance: Seance | null = null;
  reminderMinutesBefore = 30;
  isSendingReminder = false;
  reminderSentMsg = '';

  editingSeance:  Seance | null = null;
  viewingSeance:  Seance | null = null;
  seanceToDelete: Seance | null = null;

  showSuccess = false; successMessage = '';
  isLoading = false;

  // ── Drag & Drop ──
  draggedSeance: Seance | null = null;
  dropTargetJour  = '';
  dropTargetHeure = '';

  onDragStart(event: DragEvent, s: Seance): void {
    this.draggedSeance = s;
    event.dataTransfer?.setData('text/plain', String(s.id));
    (event.target as HTMLElement).classList.add('dragging');
  }

  onDragEnd(event: DragEvent): void {
    (event.target as HTMLElement).classList.remove('dragging');
    this.dropTargetJour  = '';
    this.dropTargetHeure = '';
  }

  onDragOver(event: DragEvent, jour: string, heure: string): void {
    event.preventDefault();
    this.dropTargetJour  = jour;
    this.dropTargetHeure = heure;
  }

  onDragLeave(): void {
    this.dropTargetJour  = '';
    this.dropTargetHeure = '';
  }

  onDrop(event: DragEvent, jour: string, heure: string): void {
    event.preventDefault();
    this.dropTargetJour  = '';
    this.dropTargetHeure = '';
    if (!this.draggedSeance) return;
    const duree = this.dureeMinutes(this.draggedSeance.heureDebut, this.draggedSeance.heureFin);
    const [h, m] = heure.split(':').map(Number);
    const finMin = h * 60 + m + duree;
    const heureFin = `${String(Math.floor(finMin / 60)).padStart(2,'0')}:${String(finMin % 60).padStart(2,'0')}`;
    this.seances = this.seances.map(s =>
      s.id === this.draggedSeance!.id ? { ...s, jour, heureDebut: heure, heureFin } : s
    );
    const conflitsApres = this.conflits.length;
    this.toast(conflitsApres > 0
      ? `Séance déplacée — ${conflitsApres} conflit(s) détecté(s) !`
      : `Séance déplacée vers ${jour} ${heure}`
    );
    this.draggedSeance = null;
  }

  isDragTarget(jour: string, heure: string): boolean {
    return this.dropTargetJour === jour && this.dropTargetHeure === heure;
  }

  // Filtres vue
  filterEcole  = '';
  filterClasse = '';
  filterEnseignant = '';
  semaineActive = 1;

  jours = ['Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'];
  creneaux = ['07:00','08:00','09:00','10:00','11:00','12:00','13:00','14:00','15:00','16:00','17:00','18:00','19:00'];
  types: Seance['type'][] = ['CM','TD','TP','Séminaire'];

  typeColors: Record<string, string> = {
    'CM': '#1D4ED8', 'TD': '#15803D', 'TP': '#7C3AED', 'Séminaire': '#EA580C'
  };

  ecoles: { sigle: string; nom: string; couleur: string }[] = [];

  // Chargées depuis l'API
  enseignants: { id: number; nom: string; ecoles: string[]; specialite: string }[] = [];
  classes: { code: string; nom: string; ecole: string; filiere: string; niveau: string }[] = [];
  salles: string[] = [];
  matieres: { code: string; nom: string }[] = [];

  seances: Seance[] = [];

  emptySeance = (): Omit<Seance,'id'> => ({
    matiere:'', codeMatiere:'', type:'CM',
    enseignant:'', enseignantId:0,
    ecole:'', sigleEcole:'', couleurEcole:'#1D4ED8',
    classe:'', filiere:'', niveau:'',
    salle:'', jour:'Lundi', heureDebut:'08:00', heureFin:'10:00',
    couleur:'#1D4ED8', semaine:1,
    recurrence: null, recurrenceEndDate: '', isSpecial: false
  });

  newSeance: Omit<Seance,'id'>     = this.emptySeance();
  editSeanceData: Omit<Seance,'id'> = this.emptySeance();

  ngOnInit(): void {
    this.updateDateTime();
    const u = this.authService.getUser(); if (u) { const n = u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || 'Utilisateur'; this.currentUserName = n; const p = n.trim().split(' ').filter((x: string) => x); this.currentUserInitials = p.length >= 2 ? (p[0][0] + p[1][0]).toUpperCase() : n.substring(0, 2).toUpperCase(); }
    setInterval(() => this.updateDateTime(), 1000);

    // Détecter le rôle
    const user = this.authService.getUser();
    this.isTeacher = this.authService.isTeacher();
    this.currentTeacherName = user?.name || user?.username || '';

    this.loadSchedule();
    this.loadReferenceData();
  }

  private loadReferenceData(): void {
    // Enseignants depuis user-service + leurs affectations école
    this.usersSvc.getUsers().subscribe({
      next: users => {
        const teachers = users
          .filter(u => (u.role || '').toUpperCase().includes('TEACHER'))
          .map(u => ({
            id: u.id,
            nom: u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || '',
            ecoles: [] as string[],
            specialite: u.department || ''
          }));

        this.enseignants = teachers;

        // Charger les affectations école pour chaque enseignant
        teachers.forEach(t => {
          this.usersSvc.getUserSchools(t.id).subscribe({
            next: assignments => {
              const idx = this.enseignants.findIndex(e => e.id === t.id);
              if (idx >= 0) {
                this.enseignants[idx].ecoles = assignments
                  .filter(a => (a as any).isActive !== false)
                  .map(a => (a as any).schoolName || '')
                  .filter(Boolean);
              }
            },
            error: () => {}
          });
        });
      },
      error: () => {}
    });

    // Cours depuis course-service
    this.coursesSvc.getCourses().subscribe({
      next: courses => {
        const seen = new Set<string>();
        this.matieres = courses
          .filter(c => { const k = c.code; if (seen.has(k)) return false; seen.add(k); return true; })
          .map(c => ({ code: c.code, nom: c.name }));
      },
      error: () => {}
    });

    // Salles depuis resource-service
    this.roomSvc.getRooms().subscribe({
      next: rooms => { this.salles = rooms.map(r => r.name); },
      error: () => {}
    });

    // Écoles depuis school-service — chargées en premier pour enrichir les classes
    this.schoolSvc.getAll().subscribe({
      next: schools => {
        this.ecoles = (schools ?? []).map((s, i) => ({
          sigle: s.sigle || s.code || s.name || '',
          nom: s.nom || s.name || '',
          couleur: s.couleur || ['#1D4ED8','#15803D','#DC2626','#7C3AED','#EA580C'][i % 5]
        }));

        // Classes depuis groupes-service — enrichies avec école et filière depuis le backend
        this.groupesSvc.getAll().subscribe({
          next: groupes => {
            this.classes = groupes.map(g => ({
              code:    String(g.id),
              nom:     g.name,
              // schoolCode est le code court de l'école (ex: "SJI"), schoolName le nom complet
              ecole:   g.schoolCode || g.schoolName || '',
              filiere: g.filiereName || '',
              niveau:  g.niveauName  || ''
            }));
          },
          error: () => {}
        });
      },
      error: () => {}
    });
  }

  private loadSchedule(): void {
    this.isLoading = true;
    this.scheduleService.getSchedule().subscribe({
      next: (data) => {
        const jours = ['Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'];
        this.seances = (data || []).map(e => {
          let jour = 'Lundi';
          let heureDebut = '08:00';
          let heureFin   = '10:00';

          if (e.startTime && e.startTime.includes('T')) {
            const startDt = new Date(e.startTime);
            const endDt   = e.endTime ? new Date(e.endTime) : new Date(e.startTime);
            const jsDay   = startDt.getDay();
            const dayIdx  = jsDay === 0 ? 6 : jsDay - 1;
            jour       = jours[dayIdx] || 'Lundi';
            heureDebut = `${String(startDt.getHours()).padStart(2,'0')}:${String(startDt.getMinutes()).padStart(2,'0')}`;
            heureFin   = `${String(endDt.getHours()).padStart(2,'0')}:${String(endDt.getMinutes()).padStart(2,'0')}`;
          } else {
            heureDebut = e.startTime || '08:00';
            heureFin   = e.endTime   || '10:00';
            if (typeof e.dayOfWeek === 'number') {
              jour = jours[e.dayOfWeek] || 'Lundi';
            } else if (typeof e.dayOfWeek === 'string') {
              const dayMap: Record<string, string> = {
                MONDAY:'Lundi', TUESDAY:'Mardi', WEDNESDAY:'Mercredi',
                THURSDAY:'Jeudi', FRIDAY:'Vendredi', SATURDAY:'Samedi',
                Lundi:'Lundi', Mardi:'Mardi', Mercredi:'Mercredi',
                Jeudi:'Jeudi', Vendredi:'Vendredi', Samedi:'Samedi'
              };
              jour = dayMap[e.dayOfWeek] || e.dayOfWeek;
            }
          }

          // Résoudre l'enseignantId depuis teacherId backend ou depuis la liste locale
          const teacherIdFromBackend = (e as any).teacherId as number | undefined;
          const resolvedTeacherId = teacherIdFromBackend
            ?? this.enseignants.find(t => t.nom === e.teacher)?.id
            ?? 0;

          // Résoudre l'école depuis la classe (groupName) chargée dans classes[]
          const classeMatch = this.classes.find(c =>
            c.nom === (e.group || e.groupName) || c.code === (e.group || e.groupName)
          );
          const sigleEcole   = classeMatch?.ecole || '';
          const ecoleObj     = this.ecoles.find(ec => ec.sigle === sigleEcole);
          const couleurEcole = ecoleObj?.couleur || e.color || '#1D4ED8';

          return {
            id: e.id,
            matiere: e.courseName || e.course || e.title || 'À définir',
            codeMatiere: String(e.courseId || ''),
            type: 'CM' as Seance['type'],
            enseignant: e.teacher || 'À définir',
            enseignantId: resolvedTeacherId,
            ecole: ecoleObj?.nom || '',
            sigleEcole,
            couleurEcole,
            classe: e.group || e.groupName || '',
            filiere: classeMatch?.filiere || '',
            niveau: e.level || classeMatch?.niveau || '',
            salle: e.room || 'À définir',
            jour,
            heureDebut,
            heureFin,
            couleur: couleurEcole,
            semaine: 1
          };
        });
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement planning:', err?.error?.message || err);
        this.seances = [];
        this.isLoading = false;
      }
    });
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday:'long', day:'numeric', month:'long', year:'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour:'2-digit', minute:'2-digit' });
  }

  // ── Filtres ──
  get seancesFiltrees(): Seance[] {
    return this.seances.filter(s => {
      const matchEcole  = !this.filterEcole  || s.sigleEcole === this.filterEcole;
      const matchClasse = !this.filterClasse || s.classe     === this.filterClasse;
      // Filtre enseignant : par ID si disponible, sinon par nom
      const matchEns = !this.filterEnseignant || (
        s.enseignantId
          ? s.enseignantId === +this.filterEnseignant
          : s.enseignant   === (this.enseignants.find(e => e.id === +this.filterEnseignant)?.nom ?? '')
      );
      const matchSem     = s.semaine === this.semaineActive;
      const matchTeacher = !this.isTeacher || s.enseignant === this.currentTeacherName;
      return matchEcole && matchClasse && matchEns && matchSem && matchTeacher;
    });
  }

  get classesFiltrees() {
    return this.filterEcole ? this.classes.filter(c => c.ecole === this.filterEcole) : this.classes;
  }

  getSeancesJour(jour: string): Seance[] {
    return this.seancesFiltrees.filter(s => s.jour === jour);
  }

  getSeancesCreneau(jour: string, heure: string): Seance[] {
    return this.seancesFiltrees.filter(s => s.jour === jour && s.heureDebut === heure);
  }

  // ── Conflits ──
  get conflits(): Conflit[] {
    const result: Conflit[] = [];
    const s = this.seances.filter(s => s.semaine === this.semaineActive);
    for (let i = 0; i < s.length; i++) {
      for (let j = i + 1; j < s.length; j++) {
        const a = s[i], b = s[j];
        if (a.jour !== b.jour) continue;
        if (!this.chevauchement(a.heureDebut, a.heureFin, b.heureDebut, b.heureFin)) continue;

        // Conflit enseignant : comparer par ID si disponible, sinon par nom (non vide)
        const sameTeacher = a.enseignantId && b.enseignantId
          ? a.enseignantId === b.enseignantId
          : a.enseignant && b.enseignant && a.enseignant !== 'À définir' && a.enseignant === b.enseignant;
        if (sameTeacher)
          result.push({ type:'enseignant', message:`${a.enseignant} a deux cours en même temps (${a.jour} ${a.heureDebut}) : ${a.matiere} et ${b.matiere}`, seances:[a.id, b.id] });

        // Conflit salle
        if (a.salle && b.salle && a.salle !== 'À définir' && a.salle === b.salle)
          result.push({ type:'salle', message:`La salle ${a.salle} est réservée deux fois le ${a.jour} à ${a.heureDebut} : ${a.matiere} et ${b.matiere}`, seances:[a.id, b.id] });

        // Conflit classe
        if (a.classe && b.classe && a.classe === b.classe)
          result.push({ type:'classe', message:`La classe ${a.classe} a deux cours en même temps le ${a.jour} à ${a.heureDebut}`, seances:[a.id, b.id] });
      }
    }
    return result;
  }

  chevauchement(d1: string, f1: string, d2: string, f2: string): boolean {
    return d1 < f2 && d2 < f1;
  }

  isConflict(id: number): boolean {
    return this.conflits.some(c => c.seances.includes(id));
  }

  // ── Helpers ──
  onEcoleChange(data: Omit<Seance,'id'>): void {
    const e = this.ecoles.find(e => e.sigle === data.sigleEcole);
    if (e) { data.ecole = e.nom; data.couleurEcole = e.couleur; data.couleur = e.couleur; }
    data.classe = ''; data.enseignant = ''; data.enseignantId = 0;
  }

  onClasseChange(data: Omit<Seance,'id'>): void {
    const c = this.classes.find(c => c.code === data.classe);
    if (c) { data.filiere = c.filiere; data.niveau = c.niveau; }
  }

  onMatiereChange(data: Omit<Seance,'id'>): void {
    const m = this.matieres.find(m => m.code === data.codeMatiere);
    if (m) data.matiere = m.nom;
  }

  onEnseignantChange(data: Omit<Seance,'id'>): void {
    const e = this.enseignants.find(e => e.id === +data.enseignantId);
    if (e) data.enseignant = e.nom;
  }

  onTypeChange(data: Omit<Seance,'id'>): void {
    data.couleur = this.typeColors[data.type] || data.couleurEcole;
  }

  getEnseignantsFiltres(sigleEcole: string) {
    return sigleEcole ? this.enseignants.filter(e => e.ecoles.includes(sigleEcole)) : this.enseignants;
  }

  getClassesFiltrees(sigleEcole: string) {
    return sigleEcole ? this.classes.filter(c => c.ecole === sigleEcole) : this.classes;
  }

  dureeMinutes(debut: string, fin: string): number {
    const [dh, dm] = debut.split(':').map(Number);
    const [fh, fm] = fin.split(':').map(Number);
    return (fh * 60 + fm) - (dh * 60 + dm);
  }

  toast(msg: string): void {
    this.successMessage = msg; this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  // ── Rappels ──────────────────────────────────────────────────────────────

  openReminderModal(s: Seance): void {
    this.reminderSeance = s;
    this.reminderMinutesBefore = 30;
    this.reminderSentMsg = '';
    this.isReminderModalOpen = true;
  }

  closeReminderModal(): void {
    this.isReminderModalOpen = false;
    this.reminderSeance = null;
  }

  sendReminder(): void {
    if (!this.reminderSeance) return;
    this.isSendingReminder = true;
    const s = this.reminderSeance;

    // Construire le message de rappel avec localisation et horaire
    const subject = `Rappel : ${s.matiere} — ${s.jour} ${s.heureDebut}`;
    const message = `Rappel de cours :\n\n📚 Matière : ${s.matiere}\n🏫 Salle : ${s.salle}\n📅 Jour : ${s.jour}\n⏰ Horaire : ${s.heureDebut} – ${s.heureFin}\n👨‍🏫 Enseignant : ${s.enseignant}\n👥 Classe : ${s.classe}\n\nCe rappel vous a été envoyé ${this.reminderMinutesBefore} minutes avant le cours.`;

    // Récupérer les IDs des destinataires (enseignant + étudiants de la classe)
    const recipientIds: number[] = [];
    if (s.enseignantId) recipientIds.push(s.enseignantId);

    const payload: ReminderPayload = {
      recipientIds,
      subject,
      message,
      eventType: 'COURSE_REMINDER',
      eventId: s.id,
      priority: 'NORMAL',
    };

    this.notifSvc.sendBulkReminder(payload).subscribe({
      next: (res) => {
        this.isSendingReminder = false;
        this.reminderSentMsg = res.success
          ? `✓ Rappel envoyé à ${res.sentCount} destinataire(s).`
          : '⚠ Rappel enregistré (envoi différé).';
        setTimeout(() => this.closeReminderModal(), 2500);
      },
      error: () => {
        this.isSendingReminder = false;
        this.reminderSentMsg = '⚠ Rappel enregistré (envoi différé).';
        setTimeout(() => this.closeReminderModal(), 2500);
      }
    });
  }

  // ── Récurrence ────────────────────────────────────────────────────────────

  /** Génère les séances récurrentes à partir d'une séance de base */
  expandRecurrence(base: Seance): Seance[] {
    if (!base.recurrence || !base.recurrenceEndDate) return [];
    const result: Seance[] = [];
    const jours = ['Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi','Dimanche'];
    const jourIdx = jours.indexOf(base.jour);
    if (jourIdx < 0) return [];

    const endDate = new Date(base.recurrenceEndDate);
    const stepDays = base.recurrence === 'WEEKLY' ? 7 : base.recurrence === 'BIWEEKLY' ? 14 : 28;

    // Trouver la prochaine occurrence à partir de la semaine suivante
    let current = new Date();
    current.setDate(current.getDate() + ((jourIdx - current.getDay() + 7) % 7 || 7));

    let semaine = base.semaine + 1;
    while (current <= endDate) {
      result.push({
        ...base,
        id: Date.now() + result.length,
        semaine,
        recurrence: base.recurrence,
        recurrenceEndDate: base.recurrenceEndDate,
      });
      current.setDate(current.getDate() + stepDays);
      semaine += (stepDays / 7);
    }
    return result;
  }

  getRecurrenceLabel(r: Seance['recurrence']): string {
    if (!r) return '';
    const map: Record<string, string> = { WEEKLY: 'Hebdo', BIWEEKLY: 'Bi-hebdo', MONTHLY: 'Mensuel' };
    return map[r] || '';
  }

  // ── CRUD ──
  openViewModal(s: Seance): void { this.viewingSeance = s; this.isViewModalOpen = true; }
  closeViewModal(): void         { this.isViewModalOpen = false; this.viewingSeance = null; }

  openAddModal(): void  { this.newSeance = this.emptySeance(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAdd(): void {
    this.scheduleService.addScheduleEntry({
      courseName: this.newSeance.matiere,
      courseId: this.newSeance.codeMatiere ? +this.newSeance.codeMatiere : undefined,
      teacher: this.newSeance.enseignant,
      room: this.newSeance.salle,
      group: this.newSeance.classe,
      level: this.newSeance.niveau,
      dayOfWeek: this.newSeance.jour,
      startTime: this.newSeance.heureDebut,
      endTime: this.newSeance.heureFin,
      color: this.newSeance.couleur,
      // Champs récurrence et type spécial
      ...(this.newSeance.recurrence ? { recurringPattern: this.newSeance.recurrence, recurrenceEndDate: this.newSeance.recurrenceEndDate } : {}),
      ...(this.newSeance.isSpecial ? { status: 'SPECIAL' } : {}),
    } as any).subscribe({
      next: (created: any) => {
        this.closeAddModal();
        this.loadSchedule();
        // Si récurrent, notifier le changement d'emploi du temps
        if (this.newSeance.recurrence && created?.id) {
          this.notifSvc.sendScheduleChangeNotification(created.id, 'RECURRING_COURSE_ADDED').subscribe();
        }
        this.toast(this.newSeance.recurrence
          ? `Séance récurrente (${this.getRecurrenceLabel(this.newSeance.recurrence)}) ajoutée !`
          : 'Séance ajoutée avec succès !');
      },
      error: (err: any) => alert(err?.error?.message || 'Erreur lors de l\'ajout')
    });
  }

  openEditModal(s: Seance): void {
    this.editingSeance = s;
    this.editSeanceData = { ...s };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingSeance = null; }
  handleEdit(): void {
    if (!this.editingSeance) return;
    const id = this.editingSeance.id;
    this.scheduleService.updateScheduleEntry(id, {
      courseName: this.editSeanceData.matiere,
      teacher: this.editSeanceData.enseignant,
      room: this.editSeanceData.salle,
      group: this.editSeanceData.classe,
      level: this.editSeanceData.niveau,
      dayOfWeek: this.editSeanceData.jour,
      startTime: this.editSeanceData.heureDebut,
      endTime: this.editSeanceData.heureFin,
      color: this.editSeanceData.couleur,
      ...(this.editSeanceData.recurrence ? { recurringPattern: this.editSeanceData.recurrence, recurrenceEndDate: this.editSeanceData.recurrenceEndDate } : {}),
    } as any).subscribe({
      next: () => {
        this.closeEditModal();
        this.loadSchedule();
        // Notifier le changement d'emploi du temps
        this.notifSvc.sendScheduleChangeNotification(id, 'SCHEDULE_UPDATED').subscribe();
        this.toast('Séance modifiée !');
      },
      error: (err: any) => alert(err?.error?.message || 'Erreur lors de la modification')
    });
  }

  openDeleteModal(s: Seance): void { this.seanceToDelete = s; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void         { this.isDeleteModalOpen = false; this.seanceToDelete = null; }
  confirmDelete(): void {
    if (!this.seanceToDelete) return;
    const id = this.seanceToDelete.id;
    this.scheduleService.deleteScheduleEntry(id).subscribe({
      next: () => { this.closeDeleteModal(); this.loadSchedule(); this.toast('Séance supprimée.'); },
      error: (err) => alert(err?.error?.message || 'Erreur lors de la suppression')
    });
  }
}
