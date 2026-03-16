import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

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

  enseignants = [
    { id:1, nom:'Dr. Alain Mbarga',       ecoles:['SJI'],             specialite:'Génie Informatique' },
    { id:2, nom:'Dr. Pierre Essama',      ecoles:['SJI'],             specialite:'Génie Civil' },
    { id:3, nom:'Dr. Samuel Nkoa',        ecoles:['SJI','PRÉPAVOGT'], specialite:'Électronique' },
    { id:4, nom:'Prof. Marie-Claire Ateba',ecoles:['SJM'],            specialite:'Management' },
    { id:5, nom:'Dr. Robert Nganou',      ecoles:['SJM'],             specialite:'Finance' },
    { id:6, nom:'Dr. Paul Vogt Essomba',  ecoles:['PRÉPAVOGT','CPGE'],specialite:'Mathématiques' },
    { id:7, nom:'Dr. Hélène Mbarga',      ecoles:['CPGE'],            specialite:'Physique' },
    { id:8, nom:'Dr. Sylvie Biya',        ecoles:['SJM'],             specialite:'Marketing' },
    { id:9, nom:'Dr. Cédric Owona',       ecoles:['SJI','SJM'],       specialite:'Télécom & Réseaux' },
  ];

  classes = [
    { code:'GI-L1-A', nom:'GI L1 Groupe A', ecole:'SJI', filiere:'Génie Informatique', niveau:'L1' },
    { code:'GI-L1-B', nom:'GI L1 Groupe B', ecole:'SJI', filiere:'Génie Informatique', niveau:'L1' },
    { code:'GI-L2-A', nom:'GI L2 Groupe A', ecole:'SJI', filiere:'Génie Informatique', niveau:'L2' },
    { code:'GC-L1-A', nom:'GC L1 Groupe A', ecole:'SJI', filiere:'Génie Civil',        niveau:'L1' },
    { code:'ME-L1-A', nom:'ME L1 Groupe A', ecole:'SJM', filiere:'Management',         niveau:'L1' },
    { code:'ME-L1-B', nom:'ME L1 Groupe B', ecole:'SJM', filiere:'Management',         niveau:'L1' },
    { code:'FC-L2-A', nom:'FC L2 Groupe A', ecole:'SJM', filiere:'Finance',            niveau:'L2' },
    { code:'PV-P1-A', nom:'Prépa 1 Gr. A',  ecole:'PRÉPAVOGT', filiere:'Mathématiques',niveau:'Prépa 1' },
    { code:'CG-C1-A', nom:'CPGE 1 MPSI',    ecole:'CPGE', filiere:'Mathématiques',     niveau:'CPGE 1' },
  ];

  salles = [
    'Amphi A','Amphi B','Salle 101','Salle 102','Salle 201','Salle 202',
    'Salle Info 1','Salle Info 2','TP Électrique','Labo Chimie','Labo Physique',
    'Salle CPGE 1','Salle CPGE 2','Salle Conf.',
  ];

  matieres = [
    { code:'MATH',  nom:'Mathématiques' },
    { code:'PHYS',  nom:'Physique' },
    { code:'INFO',  nom:'Informatique' },
    { code:'ELEC',  nom:'Électronique' },
    { code:'GEST',  nom:'Gestion & Management' },
    { code:'COMPTA',nom:'Comptabilité & Finance' },
    { code:'DROIT', nom:'Droit des Affaires' },
    { code:'LANG',  nom:'Langues & Communication' },
    { code:'CHIM',  nom:'Chimie' },
    { code:'MKT',   nom:'Marketing' },
  ];

  seances: Seance[] = [
    { id:1,  matiere:'Mathématiques',       codeMatiere:'MATH',  type:'CM', enseignant:'Dr. Paul Vogt Essomba',  enseignantId:6, ecole:'PRÉPAVOGT', sigleEcole:'PRÉPAVOGT', couleurEcole:'#DC2626', classe:'PV-P1-A', filiere:'Mathématiques', niveau:'Prépa 1', salle:'Salle CPGE 1', jour:'Lundi',    heureDebut:'07:00', heureFin:'09:00', couleur:'#DC2626', semaine:1 },
    { id:2,  matiere:'Informatique',        codeMatiere:'INFO',  type:'CM', enseignant:'Dr. Alain Mbarga',       enseignantId:1, ecole:'SJI',       sigleEcole:'SJI',       couleurEcole:'#1D4ED8', classe:'GI-L1-A', filiere:'Génie Informatique', niveau:'L1', salle:'Salle 101', jour:'Lundi',    heureDebut:'08:00', heureFin:'10:00', couleur:'#1D4ED8', semaine:1 },
    { id:3,  matiere:'Gestion & Management',codeMatiere:'GEST',  type:'CM', enseignant:'Prof. Marie-Claire Ateba',enseignantId:4,ecole:'SJM',       sigleEcole:'SJM',       couleurEcole:'#15803D', classe:'ME-L1-A', filiere:'Management',    niveau:'L1', salle:'Salle 201', jour:'Lundi',    heureDebut:'08:00', heureFin:'10:00', couleur:'#15803D', semaine:1 },
    { id:4,  matiere:'Électronique',        codeMatiere:'ELEC',  type:'TD', enseignant:'Dr. Samuel Nkoa',        enseignantId:3, ecole:'SJI',       sigleEcole:'SJI',       couleurEcole:'#1D4ED8', classe:'GI-L2-A', filiere:'Génie Informatique', niveau:'L2', salle:'TP Électrique', jour:'Mardi',   heureDebut:'10:00', heureFin:'12:00', couleur:'#7C3AED', semaine:1 },
    { id:5,  matiere:'Physique',            codeMatiere:'PHYS',  type:'CM', enseignant:'Dr. Hélène Mbarga',      enseignantId:7, ecole:'CPGE',      sigleEcole:'CPGE',      couleurEcole:'#7C3AED', classe:'CG-C1-A', filiere:'Mathématiques', niveau:'CPGE 1', salle:'Salle CPGE 1', jour:'Mardi',   heureDebut:'07:00', heureFin:'09:00', couleur:'#7C3AED', semaine:1 },
    { id:6,  matiere:'Comptabilité',        codeMatiere:'COMPTA',type:'TD', enseignant:'Dr. Robert Nganou',      enseignantId:5, ecole:'SJM',       sigleEcole:'SJM',       couleurEcole:'#15803D', classe:'FC-L2-A', filiere:'Finance',       niveau:'L2', salle:'Salle 202', jour:'Mercredi', heureDebut:'14:00', heureFin:'16:00', couleur:'#15803D', semaine:1 },
    { id:7,  matiere:'Informatique',        codeMatiere:'INFO',  type:'TP', enseignant:'Dr. Alain Mbarga',       enseignantId:1, ecole:'SJI',       sigleEcole:'SJI',       couleurEcole:'#1D4ED8', classe:'GI-L1-B', filiere:'Génie Informatique', niveau:'L1', salle:'Salle Info 1', jour:'Jeudi',   heureDebut:'14:00', heureFin:'16:00', couleur:'#7C3AED', semaine:1 },
    { id:8,  matiere:'Mathématiques',       codeMatiere:'MATH',  type:'CM', enseignant:'Dr. Paul Vogt Essomba',  enseignantId:6, ecole:'CPGE',      sigleEcole:'CPGE',      couleurEcole:'#7C3AED', classe:'CG-C1-A', filiere:'Mathématiques', niveau:'CPGE 1', salle:'Salle CPGE 2', jour:'Jeudi',   heureDebut:'07:00', heureFin:'09:00', couleur:'#DC2626', semaine:1 },
    { id:9,  matiere:'Marketing',           codeMatiere:'MKT',   type:'CM', enseignant:'Dr. Sylvie Biya',        enseignantId:8, ecole:'SJM',       sigleEcole:'SJM',       couleurEcole:'#15803D', classe:'ME-L1-A', filiere:'Management',    niveau:'L1', salle:'Salle 201', jour:'Vendredi', heureDebut:'10:00', heureFin:'12:00', couleur:'#EA580C', semaine:1 },
    { id:10, matiere:'Langues',             codeMatiere:'LANG',  type:'TD', enseignant:'Dr. Cédric Owona',       enseignantId:9, ecole:'SJI',       sigleEcole:'SJI',       couleurEcole:'#1D4ED8', classe:'GI-L1-A', filiere:'Génie Informatique', niveau:'L1', salle:'Salle 102', jour:'Vendredi', heureDebut:'14:00', heureFin:'16:00', couleur:'#0891B2', semaine:1 },
  ];

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
      return matchEcole && matchClasse && matchEns && matchSem;
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
    const id = this.seances.length ? Math.max(...this.seances.map(s => s.id)) + 1 : 1;
    this.seances = [...this.seances, { id, ...this.newSeance }];
    this.closeAddModal();
    this.toast(this.conflits.length > 0 ? `Séance ajoutée — ${this.conflits.length} conflit(s) détecté(s) !` : 'Séance ajoutée avec succès !');
  }

  openEditModal(s: Seance): void {
    this.editingSeance = s;
    this.editSeanceData = { ...s };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingSeance = null; }
  handleEdit(): void {
    if (!this.editingSeance) return;
    this.seances = this.seances.map(s => s.id === this.editingSeance!.id ? { id: s.id, ...this.editSeanceData } : s);
    this.closeEditModal(); this.toast('Séance modifiée !');
  }

  openDeleteModal(s: Seance): void { this.seanceToDelete = s; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void         { this.isDeleteModalOpen = false; this.seanceToDelete = null; }
  confirmDelete(): void {
    if (!this.seanceToDelete) return;
    this.seances = this.seances.filter(s => s.id !== this.seanceToDelete!.id);
    this.closeDeleteModal(); this.toast('Séance supprimée.');
  }
}
