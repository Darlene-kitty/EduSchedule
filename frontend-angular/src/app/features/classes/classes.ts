import { AuthService } from '../../core/services/auth.service';
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { GroupesManagementService, AutoAffectationResult } from '../../core/services/groupes-management.service';
import { NiveauxManagementService, NiveauBackend } from '../../core/services/niveaux-management.service';
import { FilieresManagementService, FiliereBackend } from '../../core/services/filieres-management.service';
import { SchoolManagementService } from '../../core/services/school-management.service';
import { StudentsManagementService } from '../../core/services/students-management.service';

export interface Classe {
  id: number;
  code: string;
  nom: string;
  ecole: string;
  sigleEcole: string;
  couleurEcole: string;
  filiere: string;
  niveau: string;
  niveauId: number;       // kept from backend for reliable PUT
  effectif: number;       // real count from affectations
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

  isAddModalOpen        = false;
  isEditModalOpen       = false;
  isViewModalOpen       = false;
  isDeleteModalOpen     = false;
  isAutoAffectModalOpen = false;

  editingClasse:  Classe | null = null;
  viewingClasse:  Classe | null = null;
  classeToDelete: Classe | null = null;

  currentDate = ''; currentTime = '';
  currentUserName = ''; currentUserInitials = ''; unreadCount = 0;
  showSuccess = false; successMessage = '';
  loading = false;

  // ── Auto-affectation state ──────────────────────────────────────────────
  autoAffectNiveauId: number | null = null;
  autoAffectNiveauName = '';
  autoAffectForce = false;
  autoAffectLoading = false;
  autoAffectResult: AutoAffectationResult | null = null;
  /** All student IDs from user-service (role=STUDENT) */
  allStudentIds: number[] = [];
  allStudentsLoading = false;

  private groupesService  = inject(GroupesManagementService);
  private niveauxService  = inject(NiveauxManagementService);
  private filieresService = inject(FilieresManagementService);
  private schoolService   = inject(SchoolManagementService);
  private studentsService = inject(StudentsManagementService);
  private authService = inject(AuthService);

  niveaux: NiveauBackend[] = [];
  filieres: FiliereBackend[] = [];

  /** Écoles chargées depuis school-service */
  ecoles: { sigle: string; nom: string; couleur: string }[] = [];

  /** Filières indexées par sigle d'école — peuplées depuis le backend */
  filieresByEcole: Record<string, string[]> = {};

  get filteredFilieres(): string[] {
    return this.newClasse.sigleEcole ? (this.filieresByEcole[this.newClasse.sigleEcole] || []) : [];
  }

  get filteredFilieresEdit(): string[] {
    return this.editClasseData.sigleEcole ? (this.filieresByEcole[this.editClasseData.sigleEcole] || []) : [];
  }

  get allNiveaux(): string[] {
    return [...new Set(this.niveaux.map(n => n.name))];
  }

  classes: Classe[] = [];

  emptyClasse = (): Omit<Classe, 'id'> => ({
    code:'', nom:'', ecole:'', sigleEcole:'', couleurEcole:'#1D4ED8',
    filiere:'', niveau:'', niveauId: 0, effectif:0, effectifMax:50, delegue:'', salle:'', enabled:true
  });

  newClasse: Omit<Classe, 'id'>      = this.emptyClasse();
  editClasseData: Omit<Classe, 'id'> = this.emptyClasse();

  ngOnInit(): void {
    this.updateDateTime();
    const u = this.authService.getUser(); if (u) { const n = u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || 'Utilisateur'; this.currentUserName = n; const p = n.trim().split(' ').filter((x: string) => x); this.currentUserInitials = p.length >= 2 ? (p[0][0] + p[1][0]).toUpperCase() : n.substring(0, 2).toUpperCase(); }
    setInterval(() => this.updateDateTime(), 1000);

    // Charger les écoles d'abord, puis les filières indexées par école
    this.schoolService.getAll().subscribe({
      next: schools => {
        const palette = ['#1D4ED8','#15803D','#DC2626','#7C3AED','#EA580C'];
        this.ecoles = (schools ?? []).map((s, i) => ({
          sigle: s.sigle || s.code || s.name || '',
          nom:   s.nom   || s.name || '',
          couleur: s.couleur || palette[i % palette.length]
        }));

        // Charger les filières et les indexer par sigle d'école
        this.filieresService.getAll().subscribe({
          next: filieres => {
            this.filieres = filieres ?? [];
            this.filieresByEcole = {};
            filieres.forEach(f => {
              const school = schools.find(s => s.id === f.schoolId);
              const sigle  = school?.sigle || school?.code || school?.name || f.schoolName || '';
              if (sigle) {
                if (!this.filieresByEcole[sigle]) this.filieresByEcole[sigle] = [];
                if (!this.filieresByEcole[sigle].includes(f.name)) {
                  this.filieresByEcole[sigle].push(f.name);
                }
              }
            });
          },
          error: () => {}
        });
      },
      error: () => {}
    });

    this.niveauxService.getAll().subscribe({
      next: data => { this.niveaux = data || []; },
      error: () => {}
    });
    this.loadClasses();
  }

  loadClasses(): void {
    this.loading = true;
    this.groupesService.getAll().subscribe({
      next: (data) => {
        const palette = ['#1D4ED8','#15803D','#DC2626','#7C3AED','#EA580C'];
        this.classes = (data ?? []).map((g, i) => ({
          id:           g.id,
          code:         g.code || '',
          nom:          g.name,
          // École depuis le backend enrichi
          ecole:        g.schoolName  || '',
          sigleEcole:   g.schoolCode  || g.schoolName || '',
          couleurEcole: g.schoolCouleur || palette[i % palette.length],
          // Filière depuis le backend enrichi
          filiere:      g.filiereName || '',
          niveau:       g.niveauName  || '',
          niveauId:     g.niveauId,
          effectif:     g.effectif    ?? 0,
          effectifMax:  g.capacite    ?? 50,
          delegue:      '',
          salle:        '',
          enabled:      g.active
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

  get filteredClasses(): Classe[] {
    return this.classes.filter(c => {
      const q = this.searchQuery.toLowerCase();
      const matchSearch = !q || c.nom.toLowerCase().includes(q)
        || c.code.toLowerCase().includes(q)
        || c.filiere.toLowerCase().includes(q)
        || c.sigleEcole.toLowerCase().includes(q);
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
    data.filiere = ''; data.niveau = ''; data.niveauId = 0;
  }

  toast(msg: string): void {
    this.successMessage = msg; this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  // ── Vue ──────────────────────────────────────────────────────────────────
  openViewModal(c: Classe): void { this.viewingClasse = c; this.isViewModalOpen = true; }
  closeViewModal(): void         { this.isViewModalOpen = false; this.viewingClasse = null; }

  // ── Ajout ────────────────────────────────────────────────────────────────
  openAddModal(): void  { this.newClasse = this.emptyClasse(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }

  handleAddClasse(): void {
    if (!this.newClasse.nom) { alert('Le nom est obligatoire.'); return; }
    // Resolve niveauId from backend niveaux list (match by code, name, or case-insensitive code)
    const niveauId = this.niveaux.find(n =>
      n.code === this.newClasse.niveau ||
      n.name === this.newClasse.niveau ||
      n.code?.toLowerCase() === this.newClasse.niveau?.toLowerCase()
    )?.id ?? 0;
    if (!niveauId) {
      alert('Veuillez sélectionner un niveau valide.');
      return;
    }
    const payload = {
      name: this.newClasse.nom,
      code: this.newClasse.code,
      capacite: this.newClasse.effectifMax,
      niveauId,
      active: this.newClasse.enabled
    };
    this.groupesService.create(payload).subscribe({
      next: () => { this.loadClasses(); this.closeAddModal(); this.toast('Classe ajoutée avec succès !'); },
      error: (err: any) => { this.closeAddModal(); this.toast(err?.error?.message || 'Erreur lors de l\'ajout.'); }
    });
  }

  // ── Édition ──────────────────────────────────────────────────────────────
  openEditModal(c: Classe): void {
    this.editingClasse = c;
    // Pre-fill editClasseData; use niveauId to find the matching niveau code for the select
    const matchedNiveau = this.niveaux.find(n => n.id === c.niveauId);
    this.editClasseData = {
      ...c,
      niveau: matchedNiveau?.code ?? c.niveau
    };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingClasse = null; }

  handleEditClasse(): void {
    if (!this.editingClasse) return;
    const id = this.editingClasse.id;
    // Resolve niveauId: try to match by code or name from backend niveaux, fallback to stored niveauId
    const resolvedNiveauId = this.niveaux.find(n =>
      n.code === this.editClasseData.niveau || n.name === this.editClasseData.niveau
    )?.id ?? this.editingClasse.niveauId;
    const payload = {
      name: this.editClasseData.nom,
      code: this.editClasseData.code,
      capacite: this.editClasseData.effectifMax,
      niveauId: resolvedNiveauId,
      active: this.editClasseData.enabled
    };
    this.groupesService.update(id, payload).subscribe({
      next: () => { this.loadClasses(); this.closeEditModal(); this.toast('Classe modifiée avec succès !'); },
      error: (err: any) => { this.closeEditModal(); this.toast(err?.error?.message || 'Erreur lors de la modification.'); }
    });
  }

  // ── Suppression ──────────────────────────────────────────────────────────
  openDeleteModal(c: Classe): void { this.classeToDelete = c; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void         { this.isDeleteModalOpen = false; this.classeToDelete = null; }

  confirmDelete(): void {
    if (!this.classeToDelete) return;
    const id = this.classeToDelete.id;
    this.groupesService.delete(id).subscribe({
      next: () => { this.classes = this.classes.filter(c => c.id !== id); this.closeDeleteModal(); this.toast('Classe supprimée.'); },
      error: (err: any) => { this.closeDeleteModal(); this.toast(err?.error?.message || 'Erreur lors de la suppression.'); }
    });
  }

  // ── Auto-affectation ─────────────────────────────────────────────────────

  /**
   * Ouvre le modal d'auto-affectation pour un niveau donné.
   * Charge en parallèle la liste de tous les étudiants (role=STUDENT).
   */
  openAutoAffectModal(c: Classe): void {
    this.autoAffectNiveauId   = c.niveauId;
    this.autoAffectNiveauName = c.niveau;
    this.autoAffectForce      = false;
    this.autoAffectResult     = null;
    this.isAutoAffectModalOpen = true;

    // Charger les étudiants si pas encore fait
    if (this.allStudentIds.length === 0) {
      this.allStudentsLoading = true;
      this.studentsService.getStudents().subscribe({
        next: students => {
          this.allStudentIds = students.map(s => s.id);
          this.allStudentsLoading = false;
        },
        error: () => { this.allStudentsLoading = false; }
      });
    }
  }

  closeAutoAffectModal(): void {
    this.isAutoAffectModalOpen = false;
    this.autoAffectResult = null;
  }

  /**
   * Lance l'auto-affectation round-robin pour le niveau sélectionné.
   * Tous les étudiants du système sont distribués équitablement entre
   * les groupes actifs du niveau, dans la limite de leur capacité.
   */
  runAutoAffectation(): void {
    if (!this.autoAffectNiveauId || this.allStudentIds.length === 0) return;
    this.autoAffectLoading = true;
    this.autoAffectResult  = null;

    this.groupesService.autoAffecter(
      this.autoAffectNiveauId,
      this.allStudentIds,
      this.autoAffectForce
    ).subscribe({
      next: result => {
        this.autoAffectResult  = result;
        this.autoAffectLoading = false;
        if (result) {
          this.loadClasses(); // Refresh effectifs
          this.toast(result.message);
        }
      },
      error: (err: any) => {
        this.autoAffectLoading = false;
        this.toast(err?.error?.message || 'Erreur lors de l\'auto-affectation.');
      }
    });
  }

  /**
   * Compte les groupes actifs pour un niveau donné (pour affichage dans le modal).
   */
  getGroupesCountForNiveau(niveauId: number | null): number {
    if (!niveauId) return 0;
    return this.classes.filter(c => c.niveauId === niveauId && c.enabled).length;
  }
}
