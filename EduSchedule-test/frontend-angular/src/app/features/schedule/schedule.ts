import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { ScheduleManagementService } from '../../core/services/schedule-management.service';
import { CoursesManagementService } from '../../core/services/courses-management.service';
import { RoomsManagementService, Room } from '../../core/services/rooms-management.service';
import { UsersManagementService } from '../../core/services/users-management.service';
import { AuthService } from '../../core/services/auth.service';
import {
  TimetableGenerationService,
  SchedulingRequest,
  GenerationJob,
  ScheduleSlot
} from '../../core/services/timetable-generation.service';

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
}

export interface Conflit {
  type: 'enseignant' | 'salle' | 'classe';
  message: string;
  seances: number[];
}

@Component({
  selector: 'app-schedules',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './schedule.html',
  styleUrl: './schedule.css'
})
export class SchedulesComponent implements OnInit {
  private scheduleService   = inject(ScheduleManagementService);
  private timetableSvc      = inject(TimetableGenerationService);
  private roomSvc           = inject(RoomsManagementService);
  private coursesSvc        = inject(CoursesManagementService);
  private usersSvc          = inject(UsersManagementService);
  private authService       = inject(AuthService);

  isTeacher = false;
  currentTeacherName = '';

  // ── Génération automatique ──
  showGenPanel   = false;
  genRooms       = signal<Room[]>([]);
  genSelectedRooms = signal<number[]>([]);
  genRequest: SchedulingRequest = { schoolId: 1, semester: 'S1', level: 'L1', maxHoursPerDay: 6 };
  genAlgo: 'ford-fulkerson' | 'edmonds-karp' = 'edmonds-karp';
  genJob         = signal<GenerationJob | null>(null);
  isGenerating   = signal(false);
  isConfirming   = signal(false);
  confirmMsg     = signal('');
  genErrorMsg    = signal('');
  readonly genLevels    = ['L1', 'L2', 'L3', 'M1', 'M2'];
  readonly genSemesters = ['S1', 'S2'];

  // Créneaux générés superposés sur la grille
  generatedSlots = signal<ScheduleSlot[]>([]);

  get progressColor(): string {
    const s = this.genJob()?.status;
    if (s === 'COMPLETED') return '#22c55e';
    if (s === 'PARTIAL')   return '#f59e0b';
    if (s === 'FAILED')    return '#ef4444';
    return '#3b82f6';
  }

  toggleGenRoom(id: number): void {
    const cur = this.genSelectedRooms();
    this.genSelectedRooms.set(cur.includes(id) ? cur.filter(r => r !== id) : [...cur, id]);
  }

  isGenRoomSelected(id: number): boolean { return this.genSelectedRooms().includes(id); }

  launchGeneration(): void {
    if (!this.genRequest.schoolId) { this.genErrorMsg.set('Veuillez saisir un ID école.'); return; }
    this.genErrorMsg.set('');
    this.isGenerating.set(true);
    this.genJob.set({ jobId: '', status: 'PENDING', progress: 0 });
    this.generatedSlots.set([]);

    const payload: SchedulingRequest = {
      ...this.genRequest,
      algorithm: this.genAlgo,
      roomIds: this.genSelectedRooms().length ? this.genSelectedRooms() : undefined
    };

    this.timetableSvc.generate(payload).subscribe({
      next: jobId => {
        this.timetableSvc.pollUntilDone(jobId).subscribe({
          next: status => {
            this.genJob.set(status);
            if (status.slots) this.generatedSlots.set(status.slots);
          },
          error: err => { this.genErrorMsg.set('Erreur : ' + err.message); this.isGenerating.set(false); },
          complete: () => this.isGenerating.set(false)
        });
      },
      error: err => { this.genErrorMsg.set('Erreur : ' + err.message); this.isGenerating.set(false); }
    });
  }

  confirmGeneration(): void {
    const j = this.genJob();
    if (!j?.jobId) return;
    if (j.status !== 'COMPLETED' && j.status !== 'PARTIAL') {
      return;
    }
    this.isConfirming.set(true);
    const today = new Date();
    const monday = new Date(today);
    monday.setDate(today.getDate() - ((today.getDay() + 6) % 7));
    const weekStart = monday.toISOString().split('T')[0];
    this.timetableSvc.confirm(j.jobId, this.genRequest.schoolId, 'current-user', weekStart).subscribe({
      next: res => {
        this.confirmMsg.set(res.message + ' (' + res.savedSlots + ' créneaux)');
        this.isConfirming.set(false);
        // Intégrer les créneaux générés dans la grille principale
        const dayMap: Record<string, string> = {
          LUNDI:'Lundi', MARDI:'Mardi', MERCREDI:'Mercredi', JEUDI:'Jeudi', VENDREDI:'Vendredi', SAMEDI:'Samedi'
        };
        const newSeances: Seance[] = this.generatedSlots().map((slot, i) => ({
          id: Date.now() + i,
          matiere: slot.courseName,
          codeMatiere: slot.courseCode,
          type: 'CM' as Seance['type'],
          enseignant: slot.teacherName || 'À définir',
          enseignantId: slot.teacherId,
          ecole: '', sigleEcole: '', couleurEcole: '#1D4ED8',
          classe: slot.level,
          filiere: '', niveau: slot.level,
          salle: slot.roomName,
          jour: dayMap[slot.dayOfWeek] || slot.dayOfWeek,
          heureDebut: slot.startTime,
          heureFin: slot.endTime,
          couleur: '#1D4ED8',
          semaine: this.semaineActive
        }));
        this.seances = [...this.seances, ...newSeances];
        this.toast(`${newSeances.length} séances importées depuis la génération automatique`);
        this.showGenPanel = false;
      },
      error: err => { this.confirmMsg.set('Erreur : ' + err.message); this.isConfirming.set(false); }
    });
  }

  resetGeneration(): void {
    this.genJob.set(null);
    this.isGenerating.set(false);
    this.isConfirming.set(false);
    this.confirmMsg.set('');
    this.genErrorMsg.set('');
    this.generatedSlots.set([]);
  }

  getGeneratedSlotsCreneau(jour: string, heure: string): ScheduleSlot[] {
    const dayMap: Record<string, string> = {
      Lundi:'LUNDI', Mardi:'MARDI', Mercredi:'MERCREDI', Jeudi:'JEUDI', Vendredi:'VENDREDI', Samedi:'SAMEDI'
    };
    return this.generatedSlots().filter(s => s.dayOfWeek === dayMap[jour] && s.startTime === heure);
  }

  currentDate = ''; currentTime = '';
  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;
  showConflits      = false;

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

  ecoles = [
    { sigle:'SJI',       nom:'Saint Jean Ingénieur',                    couleur:'#1D4ED8' },
    { sigle:'SJM',       nom:'Saint Jean Management',                   couleur:'#15803D' },
    { sigle:'PRÉPAVOGT', nom:'Prépavogt',                                couleur:'#DC2626' },
    { sigle:'CPGE',      nom:'Classes Préparatoires aux Grandes Écoles', couleur:'#7C3AED' },
  ];

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
    couleur:'#1D4ED8', semaine:1
  });

  newSeance: Omit<Seance,'id'>     = this.emptySeance();
  editSeanceData: Omit<Seance,'id'> = this.emptySeance();

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);

    // Détecter le rôle
    const user = this.authService.getUser();
    this.isTeacher = this.authService.isTeacher();
    this.currentTeacherName = user?.name || user?.username || '';

    this.loadSchedule();
    this.loadReferenceData();
  }

  private loadReferenceData(): void {
    // Enseignants depuis user-service
    this.usersSvc.getUsers().subscribe({
      next: users => {
        this.enseignants = users
          .filter(u => (u.role || '').toUpperCase().includes('TEACHER'))
          .map(u => ({
            id: u.id,
            nom: u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || '',
            ecoles: [],
            specialite: u.department || ''
          }));
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
        // Groupes/classes uniques
        const groupsSeen = new Set<string>();
        this.classes = courses
          .filter(c => c.group && !groupsSeen.has(c.group) && groupsSeen.add(c.group))
          .map(c => ({ code: c.group ?? '', nom: c.group ?? '', ecole: '', filiere: c.name, niveau: c.level }));
      },
      error: () => {}
    });

    // Salles depuis resource-service
    this.roomSvc.getRooms().subscribe({
      next: rooms => { this.salles = rooms.map(r => r.name); },
      error: () => {}
    });

    // Salles dispo pour la génération
    this.roomSvc.getAvailableRooms().subscribe({
      next: data => { this.genRooms.set(data); this.genSelectedRooms.set(data.map(r => r.id)); },
      error: () => {}
    });
  }

  private loadSchedule(): void {
    this.isLoading = true;
    this.scheduleService.getSchedule().subscribe({
      next: (data) => {
        const jours = ['Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'];
        this.seances = (data || []).map(e => ({
          id: e.id,
          matiere: e.courseName || e.title || 'À définir',
          codeMatiere: String(e.courseId || ''),
          type: 'CM' as Seance['type'],
          enseignant: e.teacher || 'À définir',
          enseignantId: 0,
          ecole: '',
          sigleEcole: '',
          couleurEcole: e.color || '#1D4ED8',
          classe: e.group || '',
          filiere: '',
          niveau: e.level || '',
          salle: e.room || 'À définir',
          jour: typeof e.dayOfWeek === 'number' ? (jours[e.dayOfWeek] || 'Lundi') : 'Lundi',
          heureDebut: e.startTime || '08:00',
          heureFin: e.endTime || '10:00',
          couleur: e.color || '#1D4ED8',
          semaine: 1
        }));
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
      const matchEns    = !this.filterEnseignant || s.enseignantId === +this.filterEnseignant;
      const matchSem    = s.semaine === this.semaineActive;
      // Un enseignant ne voit que ses propres séances
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
        if (a.enseignantId === b.enseignantId)
          result.push({ type:'enseignant', message:`${a.enseignant} a deux cours en même temps (${a.jour} ${a.heureDebut}) : ${a.matiere} (${a.sigleEcole}) et ${b.matiere} (${b.sigleEcole})`, seances:[a.id, b.id] });
        if (a.salle === b.salle)
          result.push({ type:'salle', message:`La salle ${a.salle} est réservée deux fois le ${a.jour} à ${a.heureDebut} : ${a.matiere} et ${b.matiere}`, seances:[a.id, b.id] });
        if (a.classe === b.classe)
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
      color: this.newSeance.couleur
    } as any).subscribe({
      next: () => { this.closeAddModal(); this.loadSchedule(); this.toast('Séance ajoutée avec succès !'); },
      error: (err) => alert(err?.error?.message || 'Erreur lors de l\'ajout')
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
      color: this.editSeanceData.couleur
    } as any).subscribe({
      next: () => { this.closeEditModal(); this.loadSchedule(); this.toast('Séance modifiée !'); },
      error: (err) => alert(err?.error?.message || 'Erreur lors de la modification')
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
