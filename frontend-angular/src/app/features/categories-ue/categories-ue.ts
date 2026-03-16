import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

export interface CategorieUE {
  id: number;
  code: string;
  nom: string;
  type: 'Fondamentale' | 'Transversale' | 'Professionnelle' | 'Optionnelle';
  ecoles: string[];
  filieres: string[];
  niveaux: string[];
  credits: number;
  volumeHoraire: number; // heures/semestre
  coefficient: number;
  description: string;
  couleur: string;
  enabled: boolean;
}

@Component({
  selector: 'app-categories-ue',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './categories-ue.html',
  styleUrl: './categories-ue.css'
})
export class CategoriesUeComponent implements OnInit {

  searchQuery  = '';
  filterType   = '';
  filterEcole  = '';

  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;

  editingCat:  CategorieUE | null = null;
  viewingCat:  CategorieUE | null = null;
  catToDelete: CategorieUE | null = null;

  currentDate = ''; currentTime = '';
  showSuccess = false; successMessage = '';

  types: CategorieUE['type'][] = ['Fondamentale', 'Transversale', 'Professionnelle', 'Optionnelle'];

  typeColors: Record<string, string> = {
    'Fondamentale':   '#1D4ED8',
    'Transversale':   '#15803D',
    'Professionnelle':'#7C3AED',
    'Optionnelle':    '#EA580C',
  };

  ecoles = [
    { sigle: 'SJI',       nom: 'Saint Jean Ingénieur',                    couleur: '#1D4ED8' },
    { sigle: 'SJM',       nom: 'Saint Jean Management',                   couleur: '#15803D' },
    { sigle: 'PRÉPAVOGT', nom: 'Prépavogt',                                couleur: '#DC2626' },
    { sigle: 'CPGE',      nom: 'Classes Préparatoires aux Grandes Écoles', couleur: '#7C3AED' },
  ];

  allFilieres = [
    'Génie Informatique','Génie Civil','Génie Électrique','Génie Mécanique','Génie Télécom','Génie Biomédical',
    'Management des Entreprises','Finance & Comptabilité','Marketing','Commerce International','Ressources Humaines','Logistique',
    'Mathématiques','Physique','Chimie','Sciences de la Vie','Lettres & Sciences Humaines','Droit des Affaires',
  ];

  allNiveaux = ['L1','L2','L3','M1','M2','Prépa 1','Prépa 2','CPGE 1','CPGE 2'];

  categories: CategorieUE[] = [
    { id:1,  code:'MATH',   nom:'Mathématiques',              type:'Fondamentale',    ecoles:['SJI','PRÉPAVOGT','CPGE'], filieres:['Génie Informatique','Génie Civil','Génie Électrique','Génie Mécanique','Mathématiques','Physique'], niveaux:['L1','L2','L3','Prépa 1','Prépa 2','CPGE 1','CPGE 2'], credits:6, volumeHoraire:60, coefficient:3, description:'Analyse, algèbre, probabilités et statistiques appliquées aux sciences de l\'ingénieur.', couleur:'#1D4ED8', enabled:true },
    { id:2,  code:'PHYS',   nom:'Physique',                   type:'Fondamentale',    ecoles:['SJI','PRÉPAVOGT','CPGE'], filieres:['Génie Électrique','Génie Mécanique','Physique','Chimie'], niveaux:['L1','L2','Prépa 1','Prépa 2','CPGE 1'], credits:4, volumeHoraire:45, coefficient:2, description:'Mécanique, électromagnétisme, thermodynamique et optique.', couleur:'#1D4ED8', enabled:true },
    { id:3,  code:'INFO',   nom:'Informatique',               type:'Fondamentale',    ecoles:['SJI','SJM'],             filieres:['Génie Informatique','Management des Entreprises','Finance & Comptabilité'], niveaux:['L1','L2','L3'], credits:5, volumeHoraire:50, coefficient:3, description:'Algorithmique, programmation, bases de données et systèmes d\'information.', couleur:'#1D4ED8', enabled:true },
    { id:4,  code:'ELEC',   nom:'Électronique',               type:'Fondamentale',    ecoles:['SJI'],                   filieres:['Génie Électrique','Génie Télécom','Génie Biomédical'], niveaux:['L2','L3','M1'], credits:4, volumeHoraire:40, coefficient:2, description:'Circuits électroniques, composants actifs et passifs, traitement du signal.', couleur:'#1D4ED8', enabled:true },
    { id:5,  code:'GEST',   nom:'Gestion & Management',       type:'Fondamentale',    ecoles:['SJM'],                   filieres:['Management des Entreprises','Finance & Comptabilité','Marketing','Commerce International','Ressources Humaines','Logistique'], niveaux:['L1','L2','L3','M1','M2'], credits:6, volumeHoraire:55, coefficient:3, description:'Principes de gestion, organisation des entreprises et management stratégique.', couleur:'#15803D', enabled:true },
    { id:6,  code:'COMPTA', nom:'Comptabilité & Finance',     type:'Fondamentale',    ecoles:['SJM'],                   filieres:['Finance & Comptabilité','Management des Entreprises'], niveaux:['L1','L2','L3','M1'], credits:5, volumeHoraire:50, coefficient:3, description:'Comptabilité générale, analytique, finance d\'entreprise et audit.', couleur:'#15803D', enabled:true },
    { id:7,  code:'DROIT',  nom:'Droit des Affaires',         type:'Transversale',    ecoles:['SJM','CPGE'],            filieres:['Droit des Affaires','Commerce International','Management des Entreprises'], niveaux:['L2','L3','M1','CPGE 1','CPGE 2'], credits:3, volumeHoraire:30, coefficient:2, description:'Droit commercial, droit des contrats, droit du travail et droit OHADA.', couleur:'#15803D', enabled:true },
    { id:8,  code:'LANG',   nom:'Langues & Communication',    type:'Transversale',    ecoles:['SJI','SJM','PRÉPAVOGT','CPGE'], filieres:[], niveaux:['L1','L2','L3','M1','M2','Prépa 1','Prépa 2','CPGE 1','CPGE 2'], credits:2, volumeHoraire:30, coefficient:1, description:'Français, anglais technique et communication professionnelle.', couleur:'#15803D', enabled:true },
    { id:9,  code:'ENTREP', nom:'Entrepreneuriat',            type:'Transversale',    ecoles:['SJI','SJM'],             filieres:[], niveaux:['L3','M1','M2'], credits:2, volumeHoraire:20, coefficient:1, description:'Création d\'entreprise, business plan, innovation et gestion de projet.', couleur:'#15803D', enabled:true },
    { id:10, code:'STAGE',  nom:'Stage & Projet',             type:'Professionnelle', ecoles:['SJI','SJM'],             filieres:[], niveaux:['L3','M1','M2'], credits:8, volumeHoraire:0, coefficient:4, description:'Stage en entreprise, projet de fin d\'études et mémoire professionnel.', couleur:'#7C3AED', enabled:true },
    { id:11, code:'SPORT',  nom:'Sport & Développement',      type:'Optionnelle',     ecoles:['SJI','SJM','PRÉPAVOGT','CPGE'], filieres:[], niveaux:['L1','L2','L3','Prépa 1','Prépa 2'], credits:1, volumeHoraire:20, coefficient:1, description:'Activités sportives et développement personnel.', couleur:'#EA580C', enabled:true },
    { id:12, code:'CHIM',   nom:'Chimie',                     type:'Fondamentale',    ecoles:['PRÉPAVOGT','CPGE'],      filieres:['Chimie','Sciences de la Vie'], niveaux:['Prépa 1','Prépa 2','CPGE 1','CPGE 2'], credits:4, volumeHoraire:40, coefficient:2, description:'Chimie générale, organique et inorganique.', couleur:'#1D4ED8', enabled:true },
  ];

  emptyCategorie = (): Omit<CategorieUE, 'id'> => ({
    code:'', nom:'', type:'Fondamentale', ecoles:[], filieres:[], niveaux:[],
    credits:3, volumeHoraire:30, coefficient:2, description:'', couleur:'#1D4ED8', enabled:true
  });

  newCat: Omit<CategorieUE, 'id'>     = this.emptyCategorie();
  editCatData: Omit<CategorieUE, 'id'> = this.emptyCategorie();

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  get filteredCategories(): CategorieUE[] {
    return this.categories.filter(c => {
      const q = this.searchQuery.toLowerCase();
      const matchSearch = !q || c.nom.toLowerCase().includes(q) || c.code.toLowerCase().includes(q);
      const matchType  = !this.filterType  || c.type  === this.filterType;
      const matchEcole = !this.filterEcole || c.ecoles.includes(this.filterEcole);
      return matchSearch && matchType && matchEcole;
    });
  }

  get typeStats(): { type: string; count: number; color: string }[] {
    return this.types.map(t => ({ type: t, count: this.categories.filter(c => c.type === t).length, color: this.typeColors[t] }));
  }

  getTypeColor(type: string): string { return this.typeColors[type] || '#6B7280'; }

  onTypeChange(data: Omit<CategorieUE, 'id'>): void {
    data.couleur = this.typeColors[data.type] || '#1D4ED8';
  }

  toggle(arr: string[], val: string): void {
    const i = arr.indexOf(val); if (i >= 0) arr.splice(i, 1); else arr.push(val);
  }

  hasEcoleNew(s: string): boolean    { return this.newCat.ecoles.includes(s); }
  hasEcoleEdit(s: string): boolean   { return this.editCatData.ecoles.includes(s); }
  hasFiliereNew(f: string): boolean  { return this.newCat.filieres.includes(f); }
  hasFiliereEdit(f: string): boolean { return this.editCatData.filieres.includes(f); }
  hasNiveauNew(n: string): boolean   { return this.newCat.niveaux.includes(n); }
  hasNiveauEdit(n: string): boolean  { return this.editCatData.niveaux.includes(n); }

  toast(msg: string): void {
    this.successMessage = msg; this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  openViewModal(c: CategorieUE): void { this.viewingCat = c; this.isViewModalOpen = true; }
  closeViewModal(): void              { this.isViewModalOpen = false; this.viewingCat = null; }

  openAddModal(): void  { this.newCat = this.emptyCategorie(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAdd(): void {
    const id = this.categories.length ? Math.max(...this.categories.map(c => c.id)) + 1 : 1;
    this.categories = [...this.categories, { id, ...this.newCat }];
    this.closeAddModal(); this.toast('Catégorie UE ajoutée !');
  }

  openEditModal(c: CategorieUE): void {
    this.editingCat = c;
    this.editCatData = { ...c, ecoles: [...c.ecoles], filieres: [...c.filieres], niveaux: [...c.niveaux] };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingCat = null; }
  handleEdit(): void {
    if (!this.editingCat) return;
    this.categories = this.categories.map(c => c.id === this.editingCat!.id ? { id: c.id, ...this.editCatData } : c);
    this.closeEditModal(); this.toast('Catégorie UE modifiée !');
  }

  openDeleteModal(c: CategorieUE): void { this.catToDelete = c; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void              { this.isDeleteModalOpen = false; this.catToDelete = null; }
  confirmDelete(): void {
    if (!this.catToDelete) return;
    this.categories = this.categories.filter(c => c.id !== this.catToDelete!.id);
    this.closeDeleteModal(); this.toast('Catégorie supprimée.');
  }
}
