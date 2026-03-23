import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { GroupesManagementService } from '../../core/services/groupes-management.service';

export interface Classe {
  id: number;
  code: string;
  nom: string;
  ecole: string;
  sigleEcole: string;
  couleurEcole: string;
  filiere: string;
  niveau: string;
  effectif: number;
  effectifMax: number;
  delegue: string;
  salle: string;
  enabled: boolean;
}

@Component({
  selector: 'app-classes',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './classes.html',
  styleUrl: './classes.css'
})
export class ClassesComponent implements OnInit {

  searchQuery  = '';
  filterEcole  = '';
  filterNiveau = '';

  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;

  editingClasse:  Classe | null = null;
  viewingClasse:  Classe | null = null;
  classeToDelete: Classe | null = null;

  currentDate = ''; currentTime = '';
  showSuccess = false; successMessage = '';
  loading = false;

  constructor(private groupesService: GroupesManagementService) {}

  ecoles = [
    { sigle: 'SJI',       nom: 'Saint Jean Ingénieur',                    couleur: '#1D4ED8' },
    { sigle: 'SJM',       nom: 'Saint Jean Management',                   couleur: '#15803D' },
    { sigle: 'PRÉPAVOGT', nom: 'Prépavogt',                                couleur: '#DC2626' },
    { sigle: 'CPGE',      nom: 'Classes Préparatoires aux Grandes Écoles', couleur: '#7C3AED' },
  ];

  filieresByEcole: Record<string, string[]> = {
    'SJI':       ['Génie Informatique','Génie Civil','Génie Électrique','Génie Mécanique','Génie Télécom','Génie Biomédical'],
    'SJM':       ['Management des Entreprises','Finance & Comptabilité','Marketing','Commerce International','Ressources Humaines','Logistique'],
    'PRÉPAVOGT': ['Mathématiques','Physique','Chimie','Sciences de la Vie'],
    'CPGE':      ['Mathématiques','Physique','Chimie','Lettres & Sciences Humaines','Droit des Affaires'],
  };

  niveauxByEcole: Record<string, string[]> = {
    'SJI':       ['L1','L2','L3','M1','M2'],
    'SJM':       ['L1','L2','L3','M1','M2'],
    'PRÉPAVOGT': ['Prépa 1','Prépa 2'],
    'CPGE':      ['CPGE 1','CPGE 2'],
  };

  get allNiveaux(): string[] {
    return [...new Set(Object.values(this.niveauxByEcole).flat())];
  }

  get filteredFilieres(): string[] {
    return this.newClasse.sigleEcole ? (this.filieresByEcole[this.newClasse.sigleEcole] || []) : [];
  }

  get filteredFilieresEdit(): string[] {
    return this.editClasseData.sigleEcole ? (this.filieresByEcole[this.editClasseData.sigleEcole] || []) : [];
  }

  get filteredNiveauxNew(): string[] {
    return this.newClasse.sigleEcole ? (this.niveauxByEcole[this.newClasse.sigleEcole] || []) : [];
  }

  get filteredNiveauxEdit(): string[] {
    return this.editClasseData.sigleEcole ? (this.niveauxByEcole[this.editClasseData.sigleEcole] || []) : [];
  }

  classes: Classe[] = [
    { id:1,  code:'GI-L1-A', nom:'GI L1 Groupe A', ecole:'Saint Jean Ingénieur',  sigleEcole:'SJI', couleurEcole:'#1D4ED8', filiere:'Génie Informatique',         niveau:'L1', effectif:42, effectifMax:50, delegue:'Jean Kamga',      salle:'Amphi A', enabled:true },
    { id:2,  code:'GI-L1-B', nom:'GI L1 Groupe B', ecole:'Saint Jean Ingénieur',  sigleEcole:'SJI', couleurEcole:'#1D4ED8', filiere:'Génie Informatique',         niveau:'L1', effectif:38, effectifMax:50, delegue:'Alice Ngo',        salle:'Salle 12', enabled:true },
    { id:3,  code:'GI-L2-A', nom:'GI L2 Groupe A', ecole:'Saint Jean Ingénieur',  sigleEcole:'SJI', couleurEcole:'#1D4ED8', filiere:'Génie Informatique',         niveau:'L2', effectif:35, effectifMax:45, delegue:'Paul Essama',      salle:'Salle 8',  enabled:true },
    { id:4,  code:'GC-L1-A', nom:'GC L1 Groupe A', ecole:'Saint Jean Ingénieur',  sigleEcole:'SJI', couleurEcole:'#1D4ED8', filiere:'Génie Civil',                niveau:'L1', effectif:40, effectifMax:50, delegue:'Marie Fouda',      salle:'Amphi B', enabled:true },
    { id:5,  code:'ME-L1-A', nom:'ME L1 Groupe A', ecole:'Saint Jean Management', sigleEcole:'SJM', couleurEcole:'#15803D', filiere:'Management des Entreprises', niveau:'L1', effectif:45, effectifMax:55, delegue:'Cédric Owona',     salle:'Salle 20', enabled:true },
    { id:6,  code:'ME-L1-B', nom:'ME L1 Groupe B', ecole:'Saint Jean Management', sigleEcole:'SJM', couleurEcole:'#15803D', filiere:'Management des Entreprises', niveau:'L1', effectif:43, effectifMax:55, delegue:'Lucie Abena',      salle:'Salle 21', enabled:true },
    { id:7,  code:'FC-L2-A', nom:'FC L2 Groupe A', ecole:'Saint Jean Management', sigleEcole:'SJM', couleurEcole:'#15803D', filiere:'Finance & Comptabilité',     niveau:'L2', effectif:30, effectifMax:40, delegue:'Robert Nganou',    salle:'Salle 15', enabled:true },
    { id:8,  code:'PV-P1-A', nom:'Prépa 1 Groupe A',ecole:'Prépavogt',            sigleEcole:'PRÉPAVOGT', couleurEcole:'#DC2626', filiere:'Mathématiques',        niveau:'Prépa 1', effectif:25, effectifMax:30, delegue:'Hélène Biya', salle:'Salle 5',  enabled:true },
    { id:9,  code:'PV-P2-A', nom:'Prépa 2 Groupe A',ecole:'Prépavogt',            sigleEcole:'PRÉPAVOGT', couleurEcole:'#DC2626', filiere:'Physique',             niveau:'Prépa 2', effectif:22, effectifMax:30, delegue:'Eric Mendo',   salle:'Salle 6',  enabled:true },
    { id:10, code:'CG-C1-A', nom:'CPGE 1 MPSI',    ecole:'Classes Préparatoires aux Grandes Écoles', sigleEcole:'CPGE', couleurEcole:'#7C3AED', filiere:'Mathématiques', niveau:'CPGE 1', effectif:20, effectifMax:25, delegue:'Sophie Tchoupo', salle:'Salle 2', enabled:true },
    { id:11, code:'CG-C2-A', nom:'CPGE 2 MP',      ecole:'Classes Préparatoires aux Grandes Écoles', sigleEcole:'CPGE', couleurEcole:'#7C3AED', filiere:'Physique',      niveau:'CPGE 2', effectif:18, effectifMax:25, delegue:'Marc Vogt',      salle:'Salle 3', enabled:true },
  ];

  emptyClasse = (): Omit<Classe, 'id'> => ({
    code:'', nom:'', ecole:'', sigleEcole:'', couleurEcole:'#1D4ED8',
    filiere:'', niveau:'', effectif:0, effectifMax:50, delegue:'', salle:'', enabled:true
  });

  newClasse: Omit<Classe, 'id'>     = this.emptyClasse();
  editClasseData: Omit<Classe, 'id'> = this.emptyClasse();

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadClasses();
  }

  loadClasses(): void {
    this.loading = true;
    this.groupesService.getAll().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.classes = data.map(g => ({
            id: g.id,
            code: g.code || '',
            nom: g.name,
            ecole: '',
            sigleEcole: '',
            couleurEcole: '#1D4ED8',
            filiere: '',
            niveau: g.niveauName || '',
            effectif: 0,
            effectifMax: g.capacite ?? 50,
            delegue: '',
            salle: '',
            enabled: g.active
          }));
        }
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  get filteredClasses(): Classe[] {
    return this.classes.filter(c => {
      const q = this.searchQuery.toLowerCase();
      const matchSearch = !q || c.nom.toLowerCase().includes(q) || c.code.toLowerCase().includes(q) || c.filiere.toLowerCase().includes(q);
      const matchEcole  = !this.filterEcole  || c.sigleEcole === this.filterEcole;
      const matchNiveau = !this.filterNiveau || c.niveau     === this.filterNiveau;
      return matchSearch && matchEcole && matchNiveau;
    });
  }

  get totalEffectif(): number { return this.classes.reduce((s, c) => s + c.effectif, 0); }

  getTauxRemplissage(c: Classe): number {
    return c.effectifMax > 0 ? Math.round((c.effectif / c.effectifMax) * 100) : 0;
  }

  getTauxClass(taux: number): string {
    if (taux >= 90) return 'taux-full';
    if (taux >= 70) return 'taux-high';
    if (taux >= 40) return 'taux-mid';
    return 'taux-low';
  }

  onEcoleChange(data: Omit<Classe, 'id'>): void {
    const found = this.ecoles.find(e => e.sigle === data.sigleEcole);
    if (found) { data.ecole = found.nom; data.couleurEcole = found.couleur; }
    data.filiere = ''; data.niveau = '';
  }

  toast(msg: string): void {
    this.successMessage = msg; this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  openViewModal(c: Classe): void { this.viewingClasse = c; this.isViewModalOpen = true; }
  closeViewModal(): void         { this.isViewModalOpen = false; this.viewingClasse = null; }

  openAddModal(): void  { this.newClasse = this.emptyClasse(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAddClasse(): void {
    const payload = {
      name: this.newClasse.nom,
      code: this.newClasse.code,
      capacite: this.newClasse.effectifMax,
      niveauId: 1, // TODO: lier au niveau sélectionné
      active: this.newClasse.enabled
    };
    this.groupesService.create(payload).subscribe({
      next: (created) => {
        if (created) { this.loadClasses(); }
        else {
          const id = this.classes.length ? Math.max(...this.classes.map(c => c.id)) + 1 : 1;
          this.classes = [...this.classes, { id, ...this.newClasse }];
        }
        this.closeAddModal(); this.toast('Classe ajoutée avec succès !');
      },
      error: () => {
        const id = this.classes.length ? Math.max(...this.classes.map(c => c.id)) + 1 : 1;
        this.classes = [...this.classes, { id, ...this.newClasse }];
        this.closeAddModal(); this.toast('Classe ajoutée avec succès !');
      }
    });
  }

  openEditModal(c: Classe): void {
    this.editingClasse = c;
    this.editClasseData = { ...c };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingClasse = null; }
  handleEditClasse(): void {
    if (!this.editingClasse) return;
    const payload = {
      name: this.editClasseData.nom,
      code: this.editClasseData.code,
      capacite: this.editClasseData.effectifMax,
      niveauId: 1,
      active: this.editClasseData.enabled
    };
    this.groupesService.update(this.editingClasse.id, payload).subscribe({
      next: (updated) => {
        if (updated) { this.loadClasses(); }
        else { this.classes = this.classes.map(c => c.id === this.editingClasse!.id ? { id: c.id, ...this.editClasseData } : c); }
        this.closeEditModal(); this.toast('Classe modifiée avec succès !');
      },
      error: () => {
        this.classes = this.classes.map(c => c.id === this.editingClasse!.id ? { id: c.id, ...this.editClasseData } : c);
        this.closeEditModal(); this.toast('Classe modifiée avec succès !');
      }
    });
  }

  openDeleteModal(c: Classe): void { this.classeToDelete = c; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void         { this.isDeleteModalOpen = false; this.classeToDelete = null; }
  confirmDelete(): void {
    if (!this.classeToDelete) return;
    this.groupesService.delete(this.classeToDelete.id).subscribe({
      next: () => {
        this.classes = this.classes.filter(c => c.id !== this.classeToDelete!.id);
        this.closeDeleteModal(); this.toast('Classe supprimée.');
      },
      error: () => {
        this.classes = this.classes.filter(c => c.id !== this.classeToDelete!.id);
        this.closeDeleteModal(); this.toast('Classe supprimée.');
      }
    });
  }
}
