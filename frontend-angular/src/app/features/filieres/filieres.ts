import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { FilieresManagementService } from '../../core/services/filieres-management.service';
import { SchoolManagementService, SchoolEntry } from '../../core/services/school-management.service';

export interface Filiere {
  id: number;
  code: string;
  nom: string;
  ecole: string;
  sigleEcole: string;
  couleurEcole: string;
  responsable: string;
  description: string;
  niveaux: string[];
  duree: number; // en années
  enabled: boolean;
}

@Component({
  selector: 'app-filieres',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './filieres.html',
  styleUrl: './filieres.css'
})
export class FilieresComponent implements OnInit {

  searchQuery   = '';
  filterEcole   = '';
  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;

  editingFiliere:  Filiere | null = null;
  viewingFiliere:  Filiere | null = null;
  filiereToDelete: Filiere | null = null;

  currentDate = ''; currentTime = '';
  showSuccess = false; successMessage = '';
  loading = false;

  constructor(private filieresService: FilieresManagementService, private schoolService: SchoolManagementService) {}

  ecoles: SchoolEntry[] = [];

  // Fallback colors for schools without a color
  private schoolColors = ['#1D4ED8', '#15803D', '#DC2626', '#7C3AED', '#EA580C'];

  allNiveaux = ['L1', 'L2', 'L3', 'M1', 'M2', 'Prépa 1', 'Prépa 2', 'CPGE 1', 'CPGE 2'];

  filieres: Filiere[] = [];

  emptyFiliere = (): Omit<Filiere, 'id'> => ({
    code: '', nom: '', ecole: '', sigleEcole: '', couleurEcole: '#1D4ED8',
    responsable: '', description: '', niveaux: [], duree: 3, enabled: true
  });

  newFiliere: Omit<Filiere, 'id'>  = this.emptyFiliere();
  editFiliereData: Omit<Filiere, 'id'> = this.emptyFiliere();

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadFilieres();
    this.schoolService.getAll().subscribe(data => { this.ecoles = data ?? []; });
  }

  loadFilieres(): void {
    this.loading = true;
    this.filieresService.getAll().subscribe({
      next: (data) => {
        this.filieres = (data ?? []).map(f => ({
          id: f.id,
          code: f.code || '',
          nom: f.name,
          ecole: f.schoolName || '',
          sigleEcole: f.schoolName || '',
          couleurEcole: '#1D4ED8',
          responsable: '',
          description: f.description || '',
          niveaux: [],
          duree: 3,
          enabled: f.active
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

  get filteredFilieres(): Filiere[] {
    return this.filieres.filter(f => {
      const matchSearch = !this.searchQuery ||
        f.nom.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        f.code.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        f.responsable.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchEcole = !this.filterEcole || f.sigleEcole === this.filterEcole;
      return matchSearch && matchEcole;
    });
  }

  get ecoleStats(): { id: number; sigle: string; nom: string; couleur: string; count: number }[] {
    return this.ecoles.map((e, i) => ({
      id: e.id,
      sigle: e.sigle || e.code || e.name || '',
      nom: e.nom || e.name || '',
      couleur: e.couleur || this.schoolColors[i % this.schoolColors.length],
      count: this.filieres.filter(f => f.ecole === (e.nom || e.name || '')).length
    }));
  }

  getSchoolColor(index: number): string {
    return this.schoolColors[index % this.schoolColors.length];
  }

  newSchoolId: number | null = null;
  editSchoolId: number | null = null;

  toggleNiveau(data: Omit<Filiere, 'id'>, n: string): void {
    const idx = data.niveaux.indexOf(n);
    if (idx >= 0) data.niveaux.splice(idx, 1); else data.niveaux.push(n);
  }
  hasNiveauNew(n: string): boolean  { return this.newFiliere.niveaux.includes(n); }
  hasNiveauEdit(n: string): boolean { return this.editFiliereData.niveaux.includes(n); }

  toast(msg: string): void {
    this.successMessage = msg; this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  /* ── Voir ── */
  openViewModal(f: Filiere): void { this.viewingFiliere = f; this.isViewModalOpen = true; }
  closeViewModal(): void          { this.isViewModalOpen = false; this.viewingFiliere = null; }

  /* ── Ajout ── */
  openAddModal(): void  { this.newFiliere = this.emptyFiliere(); this.newSchoolId = null; this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAddFiliere(): void {
    const payload = {
      name: this.newFiliere.nom,
      code: this.newFiliere.code,
      description: this.newFiliere.description,
      schoolId: this.newSchoolId ?? undefined,
      active: this.newFiliere.enabled
    };
    this.filieresService.create(payload).subscribe({
      next: () => { this.loadFilieres(); this.closeAddModal(); this.toast('Filière ajoutée avec succès !'); },
      error: (err: any) => { this.closeAddModal(); this.toast(err?.error?.message || 'Erreur lors de l\'ajout.'); }
    });
  }

  /* ── Édition ── */
  openEditModal(f: Filiere): void {
    this.editingFiliere = f;
    this.editFiliereData = { ...f, niveaux: [...f.niveaux] };
    const found = this.ecoles.find(e => (e.nom || e.name) === f.ecole);
    this.editSchoolId = found?.id ?? null;
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingFiliere = null; }
  handleEditFiliere(): void {
    if (!this.editingFiliere) return;
    const id = this.editingFiliere.id;
    const payload = {
      name: this.editFiliereData.nom,
      code: this.editFiliereData.code,
      description: this.editFiliereData.description,
      schoolId: this.editSchoolId ?? undefined,
      active: this.editFiliereData.enabled
    };
    this.filieresService.update(id, payload).subscribe({
      next: () => { this.loadFilieres(); this.closeEditModal(); this.toast('Filière modifiée avec succès !'); },
      error: (err: any) => { this.closeEditModal(); this.toast(err?.error?.message || 'Erreur lors de la modification.'); }
    });
  }

  /* ── Suppression ── */
  openDeleteModal(f: Filiere): void { this.filiereToDelete = f; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void          { this.isDeleteModalOpen = false; this.filiereToDelete = null; }
  confirmDelete(): void {
    if (!this.filiereToDelete) return;
    const id = this.filiereToDelete.id;
    this.filieresService.delete(id).subscribe({
      next: () => { this.filieres = this.filieres.filter(f => f.id !== id); this.closeDeleteModal(); this.toast('Filière supprimée.'); },
      error: (err: any) => { this.closeDeleteModal(); this.toast(err?.error?.message || 'Erreur lors de la suppression.'); }
    });
  }
}
