import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { CategoriesUeManagementService } from '../../core/services/categories-ue-management.service';

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

  constructor(private categoriesUeService: CategoriesUeManagementService) {}

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

  categories: CategorieUE[] = [];

  emptyCategorie = (): Omit<CategorieUE, 'id'> => ({
    code:'', nom:'', type:'Fondamentale', ecoles:[], filieres:[], niveaux:[],
    credits:3, volumeHoraire:30, coefficient:2, description:'', couleur:'#1D4ED8', enabled:true
  });

  newCat: Omit<CategorieUE, 'id'>     = this.emptyCategorie();
  editCatData: Omit<CategorieUE, 'id'> = this.emptyCategorie();

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadCategories();
  }

  private loadCategories(): void {
    this.categoriesUeService.getAll().subscribe({
      next: (data) => {
        this.categories = (data ?? []).map(c => ({
          id: c.id,
          code: c.code,
          nom: c.nom,
          type: this.mapType(c.type),
          ecoles: [],
          filieres: [],
          niveaux: [],
          credits: c.credits ?? 3,
          volumeHoraire: c.volumeHoraire ?? 30,
          coefficient: c.coefficient ?? 2,
          description: c.description ?? '',
          couleur: c.couleur ?? '#1D4ED8',
          enabled: c.active
        }));
      },
      error: () => {}
    });
  }

  private mapType(type: string): CategorieUE['type'] {
    const map: Record<string, CategorieUE['type']> = {
      FONDAMENTALE: 'Fondamentale', TRANSVERSALE: 'Transversale',
      PROFESSIONNELLE: 'Professionnelle', OPTIONNELLE: 'Optionnelle'
    };
    return map[type] ?? 'Fondamentale';
  }

  private reverseMapType(type: CategorieUE['type']): string {
    const map: Record<string, string> = {
      'Fondamentale': 'FONDAMENTALE', 'Transversale': 'TRANSVERSALE',
      'Professionnelle': 'PROFESSIONNELLE', 'Optionnelle': 'OPTIONNELLE'
    };
    return map[type] ?? 'FONDAMENTALE';
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
    const payload = {
      code: this.newCat.code, nom: this.newCat.nom,
      type: this.reverseMapType(this.newCat.type),
      credits: this.newCat.credits, volumeHoraire: this.newCat.volumeHoraire,
      coefficient: this.newCat.coefficient, description: this.newCat.description,
      couleur: this.newCat.couleur, active: this.newCat.enabled
    };
    this.categoriesUeService.create(payload).subscribe({
      next: () => { this.loadCategories(); this.closeAddModal(); this.toast('Catégorie UE ajoutée !'); },
      error: (err) => { this.closeAddModal(); this.toast(err?.error?.message || 'Erreur lors de l\'ajout.'); }
    });
  }

  openEditModal(c: CategorieUE): void {
    this.editingCat = c;
    this.editCatData = { ...c, ecoles: [...c.ecoles], filieres: [...c.filieres], niveaux: [...c.niveaux] };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingCat = null; }
  handleEdit(): void {
    if (!this.editingCat) return;
    const id = this.editingCat.id;
    const payload = {
      code: this.editCatData.code, nom: this.editCatData.nom,
      type: this.reverseMapType(this.editCatData.type),
      credits: this.editCatData.credits, volumeHoraire: this.editCatData.volumeHoraire,
      coefficient: this.editCatData.coefficient, description: this.editCatData.description,
      couleur: this.editCatData.couleur, active: this.editCatData.enabled
    };
    this.categoriesUeService.update(id, payload).subscribe({
      next: () => { this.loadCategories(); this.closeEditModal(); this.toast('Catégorie UE modifiée !'); },
      error: (err) => { this.closeEditModal(); this.toast(err?.error?.message || 'Erreur lors de la modification.'); }
    });
  }

  openDeleteModal(c: CategorieUE): void { this.catToDelete = c; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void              { this.isDeleteModalOpen = false; this.catToDelete = null; }
  confirmDelete(): void {
    if (!this.catToDelete) return;
    const idToDelete = this.catToDelete.id;
    this.categoriesUeService.delete(idToDelete).subscribe({
      next: () => {
        this.categories = this.categories.filter(c => c.id !== idToDelete);
        this.closeDeleteModal(); this.toast('Catégorie supprimée.');
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors de la suppression.';
        this.closeDeleteModal(); this.toast(msg);
      }
    });
  }
}
