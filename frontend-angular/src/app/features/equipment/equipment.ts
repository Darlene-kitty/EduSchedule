import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { EquipmentManagementService } from '../../core/services/equipment-management.service';
import { RoomsManagementService } from '../../core/services/rooms-management.service';
import { SalleMaterielService, SalleMateriel, DisponibiliteEquipement } from '../../core/services/salle-materiel.service';
import { AppConfigService } from '../../core/services/app-config.service';
import { SchoolManagementService } from '../../core/services/school-management.service';

export interface Materiel {
  id: number;
  code: string;
  nom: string;
  typeMateriel: string;
  typeCode: string;
  typeCouleur: string;
  typeIcone: string;
  marque: string;
  modele: string;
  numeroSerie: string;
  ecole: string;
  salle: string;
  etat: 'Bon état' | 'Usagé' | 'En panne' | 'En maintenance';
  dateAcquisition: string;
  valeur: number;
  description: string;
  enabled: boolean;
}

export interface SalleItem {
  id: number;
  code: string;
  name: string;
  type: string;
}

@Component({
  selector: 'app-equipment',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './equipment.html',
  styleUrl: './equipment.css'
})
export class EquipmentComponent implements OnInit {

  constructor(private equipmentService: EquipmentManagementService) {}
  private roomsSvc   = inject(RoomsManagementService);
  private salleSvc   = inject(SalleMaterielService);
  private configSvc  = inject(AppConfigService);
  private schoolsSvc = inject(SchoolManagementService);

  // ── Onglets ───────────────────────────────────────────────────────────────
  activeTab: 'inventaire' | 'salle' | 'disponibilite' = 'inventaire';

  availableSalles: string[] = [];
  salleItems: SalleItem[] = [];

  searchQuery = '';
  filterType  = '';
  filterEcole = '';
  filterEtat  = '';

  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;

  // ── Onglet Inventaire par salle ───────────────────────────────────────────
  selectedSalleId: number | null = null;
  inventaireSalle: SalleMateriel[] = [];
  isLoadingInventaire = false;
  isAddSalleMaterielOpen = false;
  newSalleMateriel = { materielId: 0, quantite: 1, requis: false, notes: '' };

  // ── Onglet Disponibilité ──────────────────────────────────────────────────
  dispSalleId: number | null = null;
  dispTypeCours = 'TD';
  dispDateDebut = '';
  dispDateFin   = '';
  dispResult: DisponibiliteEquipement | null = null;
  isCheckingDisp = false;
  typesCours: string[] = [];

  editingMat:  Materiel | null = null;
  viewingMat:  Materiel | null = null;
  matToDelete: Materiel | null = null;

  currentDate = '';
  currentTime = '';
  showSuccess = false;
  successMessage = '';

  etats: Materiel['etat'][] = ['Bon état', 'Usagé', 'En panne', 'En maintenance'];

  etatColors: Record<string, string> = {
    'Bon état':       '#15803D',
    'Usagé':          '#EA580C',
    'En panne':       '#DC2626',
    'En maintenance': '#7C3AED',
  };

  typesMateriel: { id?: number; code: string; nom: string; icone: string; couleur: string }[] = [];
  ecoles: string[] = [];

  private readonly defaultTypes = [
    { code: 'INFO',  nom: 'Informatique',         icone: 'computer',            couleur: '#1D4ED8' },
    { code: 'PROJ',  nom: 'Projection',           icone: 'videocam',            couleur: '#7C3AED' },
    { code: 'AUDIO', nom: 'Audio & Sonorisation', icone: 'speaker',             couleur: '#EA580C' },
    { code: 'LABO',  nom: 'Équipement de Labo',   icone: 'science',             couleur: '#DC2626' },
    { code: 'IMPR',  nom: 'Impression',           icone: 'print',               couleur: '#0891B2' },
    { code: 'MOBI',  nom: 'Mobilier',             icone: 'chair',               couleur: '#4B5563' },
    { code: 'ELEC',  nom: 'Électronique',         icone: 'electrical_services', couleur: '#15803D' },
    { code: 'CAM',   nom: 'Caméra & Photo',       icone: 'camera_alt',          couleur: '#DB2777' },
  ];

  materiels: Materiel[] = [];

  emptyMat = (): Omit<Materiel, 'id'> => ({
    code: '', nom: '', typeMateriel: '', typeCode: '', typeCouleur: '#1D4ED8', typeIcone: 'devices',
    marque: '', modele: '', numeroSerie: '', ecole: '', salle: '',
    etat: 'Bon état', dateAcquisition: '', valeur: 0, description: '', enabled: true
  });

  newMat: Omit<Materiel, 'id'>      = this.emptyMat();
  editMatData: Omit<Materiel, 'id'> = this.emptyMat();

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadMateriels();
    this.loadSalles();
    this.loadTypes();
    this.loadEcoles();

    // Charger les types de cours depuis le backend
    this.configSvc.getConfig().subscribe(cfg => {
      this.typesCours = cfg.courseTypes ?? [];
      if (this.typesCours.length) this.dispTypeCours = this.typesCours[1] ?? 'TD';
    });

    const now = new Date();
    this.dispDateDebut = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 8, 0).toISOString().slice(0, 16);
    this.dispDateFin   = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 10, 0).toISOString().slice(0, 16);
  }

  // ── Loaders ───────────────────────────────────────────────────────────────

  private loadTypes(): void {
    this.equipmentService.getAllTypes().subscribe({
      next: types => {
        if (types && types.length > 0) {
          this.typesMateriel = types.map((t: any) => ({
            id: t.id,
            code: t.code ?? String(t.id ?? ''),
            nom: t.nom ?? t.name ?? '',
            icone: t.icone ?? 'devices',
            couleur: t.couleur ?? '#6B7280'
          }));
        } else {
          this.typesMateriel = this.defaultTypes;
        }
      },
      error: () => { this.typesMateriel = this.defaultTypes; }
    });
  }

  private loadEcoles(): void {
    // Charger les écoles depuis school-service en priorité
    this.schoolsSvc.getAll().subscribe({
      next: schools => {
        const names = (schools ?? []).map(s => s.sigle || s.code || s.name || '').filter(Boolean);
        if (names.length > 0) {
          this.ecoles = names;
        } else {
          this.loadEcolesFromMateriels();
        }
      },
      error: () => this.loadEcolesFromMateriels()
    });
  }

  private loadEcolesFromMateriels(): void {
    this.equipmentService.getAllMateriels().subscribe({
      next: data => {
        const unique = [...new Set((data || []).map((m: any) => m.ecole).filter(Boolean))].sort() as string[];
        this.ecoles = unique.length > 0 ? unique : [];
      },
      error: () => { this.ecoles = []; }
    });
  }

  private loadSalles(): void {
    this.roomsSvc.getRooms().subscribe({
      next: rooms => {
        this.availableSalles = rooms.map(r => r.name).sort();
        this.salleItems = rooms.map(r => ({
          id: r.id,
          code: (r as any).code ?? '',
          name: r.name,
          type: r.type
        }));
      },
      error: () => {}
    });
  }

  private loadMateriels(): void {
    this.equipmentService.getAllMateriels().subscribe({
      next: data => {
        if (data && data.length > 0) {
          this.materiels = data.map(m => ({
            id: m.id,
            code: m.code,
            nom: m.nom,
            typeMateriel: m.typeMateriel?.nom ?? '',
            typeCode: m.typeMateriel?.code ?? '',
            typeCouleur: m.typeMateriel?.couleur ?? '#1D4ED8',
            typeIcone: m.typeMateriel?.icone ?? 'devices',
            marque: m.marque ?? '',
            modele: m.modele ?? '',
            numeroSerie: m.numeroSerie ?? '',
            ecole: m.ecole ?? '',
            salle: m.salle ?? '',
            etat: this.mapEtat(m.etat),
            dateAcquisition: m.dateAcquisition ?? '',
            valeur: m.valeur ?? 0,
            description: m.description ?? '',
            enabled: m.active
          }));
        }
      },
      error: () => {}
    });
  }

  // ── Onglet Inventaire par salle ───────────────────────────────────────────

  onSalleChange(): void {
    if (this.selectedSalleId) {
      this.loadInventaireSalle(this.selectedSalleId);
    } else {
      this.inventaireSalle = [];
    }
  }

  loadInventaireSalle(salleId: number): void {
    this.isLoadingInventaire = true;
    this.salleSvc.getInventaireSalle(salleId).subscribe({
      next: data => { this.inventaireSalle = data; this.isLoadingInventaire = false; },
      error: () => { this.inventaireSalle = []; this.isLoadingInventaire = false; }
    });
  }

  openAddSalleMateriel(): void {
    this.newSalleMateriel = { materielId: 0, quantite: 1, requis: false, notes: '' };
    this.isAddSalleMaterielOpen = true;
  }

  handleAddSalleMateriel(): void {
    if (!this.selectedSalleId || !this.newSalleMateriel.materielId) return;
    this.salleSvc.ajouterMaterielDansSalle(
      this.selectedSalleId,
      this.newSalleMateriel.materielId,
      this.newSalleMateriel.quantite,
      this.newSalleMateriel.requis,
      this.newSalleMateriel.notes
    ).subscribe({
      next: () => {
        this.isAddSalleMaterielOpen = false;
        this.loadInventaireSalle(this.selectedSalleId!);
        this.toast('Matériel ajouté à la salle !');
      },
      error: () => this.toast("Erreur lors de l'ajout.")
    });
  }

  retirerDeSalle(sm: SalleMateriel): void {
    if (!confirm(`Retirer "${sm.materiel.nom}" de cette salle ?`)) return;
    this.salleSvc.retirerMateriel(sm.id).subscribe({
      next: () => {
        this.loadInventaireSalle(this.selectedSalleId!);
        this.toast('Matériel retiré de la salle.');
      }
    });
  }

  getDispoColor(sm: SalleMateriel): string {
    if (sm.quantiteDisponible === 0) return '#DC2626';
    if (sm.quantiteDisponible < sm.quantiteTotale) return '#EA580C';
    return '#15803D';
  }

  totalDisponible(): number { return this.inventaireSalle.reduce((s, sm) => s + sm.quantiteDisponible, 0); }
  totalReserve(): number    { return this.inventaireSalle.reduce((s, sm) => s + sm.quantiteReservee, 0); }
  totalRequis(): number     { return this.inventaireSalle.filter(sm => sm.requis).length; }

  mapEtatDisplay(etat: string): Materiel['etat'] {
    const map: Record<string, Materiel['etat']> = {
      BON_ETAT: 'Bon état', USAGE: 'Usagé', EN_PANNE: 'En panne', EN_MAINTENANCE: 'En maintenance'
    };
    return map[etat] ?? 'Bon état';
  }

  // ── Onglet Disponibilité ──────────────────────────────────────────────────

  checkDisponibilite(): void {
    if (!this.dispSalleId || !this.dispDateDebut || !this.dispDateFin) return;
    this.isCheckingDisp = true;
    this.dispResult = null;
    this.salleSvc.verifierDisponibilite(
      this.dispSalleId,
      this.dispTypeCours,
      this.dispDateDebut + ':00',
      this.dispDateFin + ':00'
    ).subscribe({
      next: r => { this.dispResult = r; this.isCheckingDisp = false; },
      error: () => { this.isCheckingDisp = false; }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private mapEtat(etat: string): Materiel['etat'] {
    const map: Record<string, Materiel['etat']> = {
      BON_ETAT: 'Bon état', USAGE: 'Usagé', EN_PANNE: 'En panne', EN_MAINTENANCE: 'En maintenance'
    };
    return map[etat] ?? 'Bon état';
  }

  private reverseMapEtat(etat: Materiel['etat']): string {
    const map: Record<string, string> = {
      'Bon état': 'BON_ETAT', 'Usagé': 'USAGE', 'En panne': 'EN_PANNE', 'En maintenance': 'EN_MAINTENANCE'
    };
    return map[etat] ?? 'BON_ETAT';
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  get filteredMateriels(): Materiel[] {
    return this.materiels.filter(m => {
      const q = this.searchQuery.toLowerCase();
      const matchSearch = !q || m.nom.toLowerCase().includes(q) || m.code.toLowerCase().includes(q) || m.marque.toLowerCase().includes(q);
      const matchType  = !this.filterType  || m.typeCode === this.filterType;
      const matchEcole = !this.filterEcole || m.ecole    === this.filterEcole;
      const matchEtat  = !this.filterEtat  || m.etat     === this.filterEtat;
      return matchSearch && matchType && matchEcole && matchEtat;
    });
  }

  get etatStats(): { etat: string; count: number; color: string }[] {
    return this.etats.map(e => ({
      etat: e,
      count: this.materiels.filter(m => m.etat === e).length,
      color: this.etatColors[e]
    }));
  }

  get valeurTotale(): number { return this.materiels.reduce((s, m) => s + m.valeur, 0); }

  getEtatColor(etat: string): string { return this.etatColors[etat] || '#6B7280'; }

  formatValeur(v: number): string { return v.toLocaleString('fr-FR') + ' FCFA'; }

  onTypeChange(data: Omit<Materiel, 'id'>): void {
    const found = this.typesMateriel.find(t => t.code === data.typeCode);
    if (found) {
      data.typeMateriel = found.nom;
      data.typeCouleur  = found.couleur;
      data.typeIcone    = found.icone;
    }
  }

  toast(msg: string): void {
    this.successMessage = msg;
    this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  // ── Modals CRUD matériel ──────────────────────────────────────────────────

  openViewModal(m: Materiel): void { this.viewingMat = m; this.isViewModalOpen = true; }
  closeViewModal(): void           { this.isViewModalOpen = false; this.viewingMat = null; }

  openAddModal(): void  { this.newMat = this.emptyMat(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }

  handleAdd(): void {
    const payload = {
      code: this.newMat.code, nom: this.newMat.nom,
      marque: this.newMat.marque, modele: this.newMat.modele,
      numeroSerie: this.newMat.numeroSerie, ecole: this.newMat.ecole,
      salle: this.newMat.salle, etat: this.reverseMapEtat(this.newMat.etat),
      dateAcquisition: this.newMat.dateAcquisition, valeur: this.newMat.valeur,
      description: this.newMat.description, active: this.newMat.enabled
    };
    this.equipmentService.createMateriel(payload).subscribe({
      next: () => { this.loadMateriels(); this.closeAddModal(); this.toast('Matériel ajouté !'); },
      error: () => {
        const id = this.materiels.length ? Math.max(...this.materiels.map(m => m.id)) + 1 : 1;
        this.materiels = [...this.materiels, { id, ...this.newMat }];
        this.closeAddModal();
        this.toast('Matériel ajouté !');
      }
    });
  }

  openEditModal(m: Materiel): void {
    this.editingMat  = m;
    this.editMatData = { ...m };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingMat = null; }

  handleEdit(): void {
    if (!this.editingMat) return;
    const id = this.editingMat.id;
    const payload = {
      code: this.editMatData.code, nom: this.editMatData.nom,
      marque: this.editMatData.marque, modele: this.editMatData.modele,
      numeroSerie: this.editMatData.numeroSerie, ecole: this.editMatData.ecole,
      salle: this.editMatData.salle, etat: this.reverseMapEtat(this.editMatData.etat),
      dateAcquisition: this.editMatData.dateAcquisition, valeur: this.editMatData.valeur,
      description: this.editMatData.description, active: this.editMatData.enabled
    };
    this.equipmentService.updateMateriel(id, payload).subscribe({
      next: () => { this.loadMateriels(); },
      error: () => { this.materiels = this.materiels.map(m => m.id === id ? { id, ...this.editMatData } : m); }
    });
    this.closeEditModal();
    this.toast('Matériel modifié !');
  }

  openDeleteModal(m: Materiel): void { this.matToDelete = m; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void           { this.isDeleteModalOpen = false; this.matToDelete = null; }

  confirmDelete(): void {
    if (!this.matToDelete) return;
    const id = this.matToDelete.id;
    this.equipmentService.deleteMateriel(id).subscribe({
      next: () => { this.loadMateriels(); },
      error: () => { this.materiels = this.materiels.filter(m => m.id !== id); }
    });
    this.closeDeleteModal();
    this.toast('Matériel supprimé.');
  }
}
