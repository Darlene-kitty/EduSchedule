import { AuthService } from '../../core/services/auth.service';
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { SchoolManagementService } from '../../core/services/school-management.service';
import { FilieresManagementService } from '../../core/services/filieres-management.service';
import { NiveauxManagementService } from '../../core/services/niveaux-management.service';

export interface School {
  id: number;
  sigle: string;
  nom: string;
  directeur: string;
  email: string;
  telephone: string;
  description: string;
  filieres: string[];
  niveaux: string[];
  couleur: string;
  enabled: boolean;
  salles: number;
  laboratoires: number;
  amphi: number;
}

@Component({
  selector: 'app-schools',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './schools.html',
  styleUrl: './schools.css'
})
export class SchoolsComponent implements OnInit {
  private schoolService  = inject(SchoolManagementService);
  private filieresSvc    = inject(FilieresManagementService);
  private niveauxSvc     = inject(NiveauxManagementService);
  private authService = inject(AuthService);

  searchQuery = '';
  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;
  isLoading = false;

  editingSchool:  School | null = null;
  viewingSchool:  School | null = null;
  schoolToDelete: School | null = null;

  currentDate = ''; currentTime = '';
  currentUserName = ''; currentUserInitials = ''; unreadCount = 0;
  showSuccess = false; successMessage = '';

  // Toutes les filières disponibles dans l'institution — chargées depuis le backend
  allFilieres: string[] = [
    'Génie Informatique', 'Génie Civil', 'Génie Électrique', 'Génie Mécanique',
    'Génie Télécom', 'Génie Biomédical',
    'Management des Entreprises', 'Finance & Comptabilité', 'Marketing',
    'Commerce International', 'Ressources Humaines', 'Logistique',
    'Mathématiques', 'Physique', 'Chimie', 'Sciences de la Vie',
    'Lettres & Sciences Humaines', 'Droit des Affaires'
  ];

  allNiveaux: string[] = ['L1', 'L2', 'L3', 'M1', 'M2', 'Prépa 1', 'Prépa 2', 'CPGE 1', 'CPGE 2'];

  couleurs = [
    { label: 'Bleu',   value: '#1D4ED8' },
    { label: 'Vert',   value: '#15803D' },
    { label: 'Rouge',  value: '#DC2626' },
    { label: 'Violet', value: '#7C3AED' },
    { label: 'Orange', value: '#EA580C' },
    { label: 'Cyan',   value: '#0891B2' },
  ];

  schools: School[] = [];



  emptySchool = (): Omit<School, 'id'> => ({
    sigle: '', nom: '', directeur: '', email: '', telephone: '',
    description: '', filieres: [], niveaux: [], couleur: '#1D4ED8', enabled: true,
    salles: 0, laboratoires: 0, amphi: 0
  });

  newSchool: Omit<School, 'id'> = this.emptySchool();
  editSchoolData: Omit<School, 'id'> = this.emptySchool();

  ngOnInit(): void {
    this.updateDateTime();
    const u = this.authService.getUser(); if (u) { const n = u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || 'Utilisateur'; this.currentUserName = n; const p = n.trim().split(' ').filter((x: string) => x); this.currentUserInitials = p.length >= 2 ? (p[0][0] + p[1][0]).toUpperCase() : n.substring(0, 2).toUpperCase(); }
    setInterval(() => this.updateDateTime(), 1000);
    this.loadSchools();
    this.loadReferenceData();
  }

  private loadReferenceData(): void {
    // Charger les filières depuis le backend (noms uniques)
    this.filieresSvc.getAll().subscribe({
      next: (filieres) => {
        const names = filieres.map(f => f.name || (f as any).nom || '').filter(Boolean);
        if (names.length > 0) {
          // Dédupliquer et trier
          this.allFilieres = [...new Set(names)].sort();
        }
      },
      error: () => {} // garder les valeurs par défaut
    });
    // Charger les niveaux depuis le backend (codes uniques)
    this.niveauxSvc.getAll().subscribe({
      next: (niveaux) => {
        const codes = niveaux.map(n => n.code || n.name || '').filter(Boolean);
        if (codes.length > 0) {
          this.allNiveaux = [...new Set(codes)].sort();
        }
      },
      error: () => {} // garder les valeurs par défaut
    });
  }

  loadSchools(): void {
    this.isLoading = true;
    this.schoolService.getAll().subscribe({
      next: (data) => {
        this.schools = (data ?? []).map(s => ({
          id: s.id,
          sigle: s.sigle || s.code || '',
          nom: s.nom || s.name || '',
          directeur: s.directeur || '',
          email: s.email || '',
          telephone: s.telephone || '',
          description: s.description || '',
          filieres: s.filieres || [],
          niveaux: s.niveaux || [],
          couleur: s.couleur || '#1D4ED8',
          enabled: s.enabled ?? s.active ?? true,
          salles: (s as any).salles || 0,
          laboratoires: (s as any).laboratoires || 0,
          amphi: (s as any).amphi || 0
        }));
        this.isLoading = false;
      },
      error: () => { this.schools = []; this.isLoading = false; }
    });
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  get filteredSchools(): School[] {
    if (!this.searchQuery) return this.schools;
    const q = this.searchQuery.toLowerCase();
    return this.schools.filter(s =>
      s.nom.toLowerCase().includes(q) ||
      s.sigle.toLowerCase().includes(q) ||
      s.directeur.toLowerCase().includes(q)
    );
  }

  getInitials(sigle: string): string { return sigle.substring(0, 3).toUpperCase(); }

  toast(msg: string): void {
    this.successMessage = msg;
    this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  /* ── Filières checkboxes ── */
  toggleFiliere(data: Omit<School, 'id'>, f: string): void {
    const idx = data.filieres.indexOf(f);
    if (idx >= 0) data.filieres.splice(idx, 1);
    else data.filieres.push(f);
  }
  hasFiliereNew(f: string): boolean  { return this.newSchool.filieres.includes(f); }
  hasFiliereEdit(f: string): boolean { return this.editSchoolData.filieres.includes(f); }

  toggleNiveau(data: Omit<School, 'id'>, n: string): void {
    const idx = data.niveaux.indexOf(n);
    if (idx >= 0) data.niveaux.splice(idx, 1);
    else data.niveaux.push(n);
  }
  hasNiveauNew(n: string): boolean  { return this.newSchool.niveaux.includes(n); }
  hasNiveauEdit(n: string): boolean { return this.editSchoolData.niveaux.includes(n); }

  /* ── Voir ── */
  openViewModal(s: School): void { this.viewingSchool = s; this.isViewModalOpen = true; }
  closeViewModal(): void         { this.isViewModalOpen = false; this.viewingSchool = null; }

  /* ── Ajout ── */
  openAddModal(): void  { this.newSchool = this.emptySchool(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAddSchool(): void {
    this.schoolService.create(this.newSchool).subscribe({
      next: (created) => {
        this.schools = [...this.schools, { id: created.id, ...this.newSchool }];
        this.closeAddModal();
        this.toast('École ajoutée avec succès !');
      },
      error: (err) => {
        console.error('Erreur création école:', err);
        alert('Erreur lors de la création de l\'école : ' + (err?.error?.message || 'vérifiez les données saisies.'));
      }
    });
  }

  /* ── Édition ── */
  openEditModal(s: School): void {
    this.editingSchool = s;
    this.editSchoolData = { ...s, filieres: [...s.filieres], niveaux: [...s.niveaux] };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingSchool = null; }
  handleEditSchool(): void {
    if (!this.editingSchool) return;
    this.schoolService.update(this.editingSchool.id, this.editSchoolData).subscribe({
      next: () => {
        this.schools = this.schools.map(s => s.id === this.editingSchool!.id ? { id: s.id, ...this.editSchoolData } : s);
        this.closeEditModal();
        this.toast('École modifiée avec succès !');
      },
      error: (err) => {
        console.error('Erreur modification école:', err);
        alert('Erreur lors de la modification de l\'école : ' + (err?.error?.message || 'vérifiez les données saisies.'));
      }
    });
  }

  /* ── Suppression ── */
  openDeleteModal(s: School): void { this.schoolToDelete = s; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void         { this.isDeleteModalOpen = false; this.schoolToDelete = null; }
  confirmDelete(): void {
    if (!this.schoolToDelete) return;
    const id = this.schoolToDelete.id;
    this.schoolService.delete(id).subscribe({
      next: () => { this.schools = this.schools.filter(s => s.id !== id); this.closeDeleteModal(); this.toast('École supprimée.'); },
      error: (err: any) => {
        console.error('Erreur suppression école:', err);
        this.closeDeleteModal();
        alert('Erreur lors de la suppression : ' + (err?.error?.message || 'une erreur est survenue.'));
      }
    });
  }
}
