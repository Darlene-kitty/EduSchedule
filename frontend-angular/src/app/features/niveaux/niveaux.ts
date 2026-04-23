import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { NiveauxManagementService } from '../../core/services/niveaux-management.service';
import { FilieresManagementService, FiliereBackend } from '../../core/services/filieres-management.service';

export interface Niveau {
  id: number;
  code: string;
  nom: string;
  type: 'Licence' | 'Master' | 'Doctorat' | 'Préparatoire' | 'CPGE';
  ecoles: string[];       // écoles où ce niveau existe
  filieres: string[];     // filières associées
  annee: number;          // année dans le cycle (1, 2, 3...)
  description: string;
  enabled: boolean;
}

@Component({
  selector: 'app-niveaux',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './niveaux.html',
  styleUrl: './niveaux.css'
})
export class NiveauxComponent implements OnInit {

  searchQuery  = '';
  filterType   = '';
  filterEcole  = '';

  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;

  editingNiveau:  Niveau | null = null;
  viewingNiveau:  Niveau | null = null;
  niveauToDelete: Niveau | null = null;

  currentDate = ''; currentTime = '';
  showSuccess = false; successMessage = '';
  loading = false;

  constructor(private niveauxService: NiveauxManagementService, private filieresService: FilieresManagementService) {}

  types: Niveau['type'][] = ['Licence', 'Master', 'Doctorat', 'Préparatoire', 'CPGE'];

  ecoles = [
    { sigle: 'SJI',       nom: 'Saint Jean Ingénieur',                     couleur: '#1D4ED8' },
    { sigle: 'SJM',       nom: 'Saint Jean Management',                    couleur: '#15803D' },
    { sigle: 'PRÉPAVOGT', nom: 'Prépavogt',                                 couleur: '#DC2626' },
    { sigle: 'CPGE',      nom: 'Classes Préparatoires aux Grandes Écoles',  couleur: '#7C3AED' },
  ];

  allFilieres: FiliereBackend[] = [];

  niveaux: Niveau[] = [];

  emptyNiveau = (): Omit<Niveau, 'id'> => ({
    code: '', nom: '', type: 'Licence', ecoles: [], filieres: [], annee: 1, description: '', enabled: true
  });

  newNiveau: Omit<Niveau, 'id'>      = this.emptyNiveau();
  editNiveauData: Omit<Niveau, 'id'> = this.emptyNiveau();

  // Selected filiere IDs for the modals
  newFiliereId: number | null = null;
  editFiliereId: number | null = null;

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadNiveaux();
    this.filieresService.getAll().subscribe(data => { this.allFilieres = data ?? []; });
  }

  loadNiveaux(): void {
    this.loading = true;
    this.niveauxService.getAll().subscribe({
      next: (data) => {
        this.niveaux = (data ?? []).map(n => ({
          id: n.id,
          code: n.code || '',
          nom: n.name,
          type: 'Licence' as Niveau['type'],
          ecoles: [],
          filieres: n.filiereName ? [n.filiereName] : [],
          annee: n.ordre ?? 1,
          description: '',
          enabled: n.active
        }));
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

  get filteredNiveaux(): Niveau[] {
    return this.niveaux.filter(n => {
      const q = this.searchQuery.toLowerCase();
      const matchSearch = !q || n.nom.toLowerCase().includes(q) || n.code.toLowerCase().includes(q);
      const matchType  = !this.filterType  || n.type  === this.filterType;
      const matchEcole = !this.filterEcole || n.ecoles.includes(this.filterEcole);
      return matchSearch && matchType && matchEcole;
    });
  }

  get typeStats(): { type: string; count: number }[] {
    return this.types.map(t => ({ type: t, count: this.niveaux.filter(n => n.type === t).length }));
  }

  getTypeClass(type: string): string {
    const map: Record<string, string> = {
      'Licence': 'type-licence', 'Master': 'type-master',
      'Doctorat': 'type-doctorat', 'Préparatoire': 'type-prepa', 'CPGE': 'type-cpge'
    };
    return map[type] || '';
  }

  getEcoleColor(sigle: string): string {
    return this.ecoles.find(e => e.sigle === sigle)?.couleur || '#6B7280';
  }

  toggleEcole(data: Omit<Niveau, 'id'>, sigle: string): void {
    const idx = data.ecoles.indexOf(sigle);
    if (idx >= 0) data.ecoles.splice(idx, 1); else data.ecoles.push(sigle);
  }
  hasEcoleNew(s: string): boolean  { return this.newNiveau.ecoles.includes(s); }
  hasEcoleEdit(s: string): boolean { return this.editNiveauData.ecoles.includes(s); }

  toggleFiliere(data: Omit<Niveau, 'id'>, f: string): void {
    const idx = data.filieres.indexOf(f);
    if (idx >= 0) data.filieres.splice(idx, 1); else data.filieres.push(f);
  }
  hasFiliereNew(f: string): boolean  { return this.newNiveau.filieres.includes(f); }
  hasFiliereEdit(f: string): boolean { return this.editNiveauData.filieres.includes(f); }

  toast(msg: string): void {
    this.successMessage = msg; this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  openViewModal(n: Niveau): void { this.viewingNiveau = n; this.isViewModalOpen = true; }
  closeViewModal(): void         { this.isViewModalOpen = false; this.viewingNiveau = null; }

  openAddModal(): void  { this.newNiveau = this.emptyNiveau(); this.newFiliereId = null; this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAddNiveau(): void {
    const payload = {
      name: this.newNiveau.nom,
      code: this.newNiveau.code,
      ordre: this.newNiveau.annee,
      filiereId: this.newFiliereId ?? undefined,
      active: this.newNiveau.enabled
    };
    this.niveauxService.create(payload).subscribe({
      next: () => { this.loadNiveaux(); this.closeAddModal(); this.toast('Niveau ajouté avec succès !'); },
      error: (err: any) => { this.closeAddModal(); this.toast(err?.error?.message || 'Erreur lors de l\'ajout.'); }
    });
  }

  openEditModal(n: Niveau): void {
    this.editingNiveau = n;
    this.editNiveauData = { ...n, ecoles: [...n.ecoles], filieres: [...n.filieres] };
    // Retrouver l'ID de la filière depuis le nom
    const found = this.allFilieres.find(f => f.name === n.filieres[0]);
    this.editFiliereId = found?.id ?? null;
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingNiveau = null; }
  handleEditNiveau(): void {
    if (!this.editingNiveau) return;
    const id = this.editingNiveau.id;
    const payload = {
      name: this.editNiveauData.nom,
      code: this.editNiveauData.code,
      ordre: this.editNiveauData.annee,
      filiereId: this.editFiliereId ?? undefined,
      active: this.editNiveauData.enabled
    };
    this.niveauxService.update(id, payload).subscribe({
      next: () => { this.loadNiveaux(); this.closeEditModal(); this.toast('Niveau modifié avec succès !'); },
      error: (err: any) => { this.closeEditModal(); this.toast(err?.error?.message || 'Erreur lors de la modification.'); }
    });
  }

  openDeleteModal(n: Niveau): void { this.niveauToDelete = n; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void         { this.isDeleteModalOpen = false; this.niveauToDelete = null; }
  confirmDelete(): void {
    if (!this.niveauToDelete) return;
    const id = this.niveauToDelete.id;
    this.niveauxService.delete(id).subscribe({
      next: () => { this.niveaux = this.niveaux.filter(n => n.id !== id); this.closeDeleteModal(); this.toast('Niveau supprimé.'); },
      error: (err: any) => { this.closeDeleteModal(); this.toast(err?.error?.message || 'Erreur lors de la suppression.'); }
    });
  }
}
