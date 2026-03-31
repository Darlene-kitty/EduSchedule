import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { EquipmentManagementService } from '../../core/services/equipment-management.service';

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

@Component({
  selector: 'app-equipment',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './equipment.html',
  styleUrl: './equipment.css'
})
export class EquipmentComponent implements OnInit {

  constructor(private equipmentService: EquipmentManagementService) {}

  searchQuery  = '';
  filterType   = '';
  filterEcole  = '';
  filterEtat   = '';

  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;

  editingMat:  Materiel | null = null;
  viewingMat:  Materiel | null = null;
  matToDelete: Materiel | null = null;

  currentDate = ''; currentTime = '';
  showSuccess = false; successMessage = '';

  etats: Materiel['etat'][] = ['Bon état','Usagé','En panne','En maintenance'];

  etatColors: Record<string, string> = {
    'Bon état':       '#15803D',
    'Usagé':          '#EA580C',
    'En panne':       '#DC2626',
    'En maintenance': '#7C3AED',
  };

  typesMateriel = [
    { code:'INFO',  nom:'Informatique',         icone:'computer',            couleur:'#1D4ED8' },
    { code:'PROJ',  nom:'Projection',           icone:'videocam',            couleur:'#7C3AED' },
    { code:'AUDIO', nom:'Audio & Sonorisation', icone:'speaker',             couleur:'#EA580C' },
    { code:'LABO',  nom:'Équipement de Labo',   icone:'science',             couleur:'#DC2626' },
    { code:'IMPR',  nom:'Impression',           icone:'print',               couleur:'#0891B2' },
    { code:'MOBI',  nom:'Mobilier',             icone:'chair',               couleur:'#4B5563' },
    { code:'ELEC',  nom:'Électronique',         icone:'electrical_services', couleur:'#15803D' },
    { code:'CAM',   nom:'Caméra & Photo',       icone:'camera_alt',          couleur:'#DB2777' },
  ];

  ecoles = ['SJI','SJM','PRÉPAVOGT','CPGE','Commun'];

  materiels: Materiel[] = [
    { id:1,  code:'MAT-001', nom:'Ordinateur Dell OptiPlex',    typeMateriel:'Informatique',         typeCode:'INFO',  typeCouleur:'#1D4ED8', typeIcone:'computer',            marque:'Dell',       modele:'OptiPlex 7090',    numeroSerie:'DL-7090-001', ecole:'SJI',       salle:'Salle Info 1', etat:'Bon état',       dateAcquisition:'2023-01-15', valeur:450000, description:'Ordinateur de bureau pour salle informatique.', enabled:true },
    { id:2,  code:'MAT-002', nom:'Ordinateur Dell OptiPlex',    typeMateriel:'Informatique',         typeCode:'INFO',  typeCouleur:'#1D4ED8', typeIcone:'computer',            marque:'Dell',       modele:'OptiPlex 7090',    numeroSerie:'DL-7090-002', ecole:'SJI',       salle:'Salle Info 1', etat:'Bon état',       dateAcquisition:'2023-01-15', valeur:450000, description:'Ordinateur de bureau pour salle informatique.', enabled:true },
    { id:3,  code:'MAT-003', nom:'Vidéoprojecteur Epson',       typeMateriel:'Projection',           typeCode:'PROJ',  typeCouleur:'#7C3AED', typeIcone:'videocam',            marque:'Epson',      modele:'EB-X51',           numeroSerie:'EP-X51-001',  ecole:'Commun',    salle:'Amphi A',      etat:'Bon état',       dateAcquisition:'2022-09-01', valeur:280000, description:'Vidéoprojecteur pour amphithéâtre.', enabled:true },
    { id:4,  code:'MAT-004', nom:'Vidéoprojecteur Epson',       typeMateriel:'Projection',           typeCode:'PROJ',  typeCouleur:'#7C3AED', typeIcone:'videocam',            marque:'Epson',      modele:'EB-X51',           numeroSerie:'EP-X51-002',  ecole:'SJM',       salle:'Salle 201',    etat:'En maintenance', dateAcquisition:'2022-09-01', valeur:280000, description:'Vidéoprojecteur en cours de maintenance.', enabled:true },
    { id:5,  code:'MAT-005', nom:'Microscope Optique',          typeMateriel:'Équipement de Labo',   typeCode:'LABO',  typeCouleur:'#DC2626', typeIcone:'science',             marque:'Olympus',    modele:'CX23',             numeroSerie:'OL-CX23-001', ecole:'PRÉPAVOGT', salle:'Labo Chimie',  etat:'Bon état',       dateAcquisition:'2021-06-10', valeur:350000, description:'Microscope optique binoculaire pour TP.', enabled:true },
    { id:6,  code:'MAT-006', nom:'Oscilloscope Numérique',      typeMateriel:'Équipement de Labo',   typeCode:'LABO',  typeCouleur:'#DC2626', typeIcone:'science',             marque:'Rigol',      modele:'DS1054Z',          numeroSerie:'RG-DS1054-01',ecole:'SJI',       salle:'TP Électrique',etat:'Bon état',       dateAcquisition:'2022-03-20', valeur:180000, description:'Oscilloscope 4 canaux 50MHz.', enabled:true },
    { id:7,  code:'MAT-007', nom:'Imprimante HP LaserJet',      typeMateriel:'Impression',           typeCode:'IMPR',  typeCouleur:'#0891B2', typeIcone:'print',               marque:'HP',         modele:'LaserJet Pro M404', numeroSerie:'HP-M404-001', ecole:'SJM',       salle:'Secrétariat',  etat:'Usagé',          dateAcquisition:'2020-11-05', valeur:120000, description:'Imprimante laser monochrome.', enabled:true },
    { id:8,  code:'MAT-008', nom:'Système de Sonorisation',     typeMateriel:'Audio & Sonorisation', typeCode:'AUDIO', typeCouleur:'#EA580C', typeIcone:'speaker',             marque:'Yamaha',     modele:'STAGEPAS 400BT',   numeroSerie:'YM-SP400-001',ecole:'Commun',    salle:'Amphi A',      etat:'Bon état',       dateAcquisition:'2023-04-12', valeur:320000, description:'Système de sonorisation portable pour amphithéâtre.', enabled:true },
    { id:9,  code:'MAT-009', nom:'Laptop Lenovo ThinkPad',      typeMateriel:'Informatique',         typeCode:'INFO',  typeCouleur:'#1D4ED8', typeIcone:'computer',            marque:'Lenovo',     modele:'ThinkPad E15',     numeroSerie:'LN-E15-001',  ecole:'SJI',       salle:'Salle 101',    etat:'Bon état',       dateAcquisition:'2023-08-20', valeur:520000, description:'Laptop pour enseignant.', enabled:true },
    { id:10, code:'MAT-010', nom:'Caméra Sony',                 typeMateriel:'Caméra & Photo',       typeCode:'CAM',   typeCouleur:'#DB2777', typeIcone:'camera_alt',          marque:'Sony',       modele:'ZV-E10',           numeroSerie:'SN-ZVE10-001',ecole:'Commun',    salle:'Salle Conf.',  etat:'Bon état',       dateAcquisition:'2023-02-14', valeur:250000, description:'Caméra pour enregistrement de cours.', enabled:true },
    { id:11, code:'MAT-011', nom:'Tableau Blanc Interactif',    typeMateriel:'Projection',           typeCode:'PROJ',  typeCouleur:'#7C3AED', typeIcone:'videocam',            marque:'Smart',      modele:'SBID-7275',        numeroSerie:'SM-7275-001', ecole:'CPGE',      salle:'Salle CPGE 1', etat:'Bon état',       dateAcquisition:'2022-07-01', valeur:680000, description:'Tableau blanc interactif 75 pouces.', enabled:true },
    { id:12, code:'MAT-012', nom:'Imprimante 3D',               typeMateriel:'Informatique',         typeCode:'INFO',  typeCouleur:'#1D4ED8', typeIcone:'computer',            marque:'Creality',   modele:'Ender 3 Pro',      numeroSerie:'CR-E3P-001',  ecole:'SJI',       salle:'Salle Info 1', etat:'En panne',       dateAcquisition:'2022-05-18', valeur:95000,  description:'Imprimante 3D FDM pour projets étudiants.', enabled:true },
  ];

  emptyMat = (): Omit<Materiel, 'id'> => ({
    code:'', nom:'', typeMateriel:'', typeCode:'', typeCouleur:'#1D4ED8', typeIcone:'devices',
    marque:'', modele:'', numeroSerie:'', ecole:'', salle:'',
    etat:'Bon état', dateAcquisition:'', valeur:0, description:'', enabled:true
  });

  newMat: Omit<Materiel, 'id'>     = this.emptyMat();
  editMatData: Omit<Materiel, 'id'> = this.emptyMat();

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadMateriels();
  }

  private loadMateriels(): void {
    this.equipmentService.getAllMateriels().subscribe({
      next: (data) => {
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
      error: () => {} // garde les données démo
    });
  }

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
      const matchType  = !this.filterType  || m.typeCode  === this.filterType;
      const matchEcole = !this.filterEcole || m.ecole     === this.filterEcole;
      const matchEtat  = !this.filterEtat  || m.etat      === this.filterEtat;
      return matchSearch && matchType && matchEcole && matchEtat;
    });
  }

  get etatStats(): { etat: string; count: number; color: string }[] {
    return this.etats.map(e => ({ etat: e, count: this.materiels.filter(m => m.etat === e).length, color: this.etatColors[e] }));
  }

  get valeurTotale(): number { return this.materiels.reduce((s, m) => s + m.valeur, 0); }

  getEtatColor(etat: string): string { return this.etatColors[etat] || '#6B7280'; }

  formatValeur(v: number): string {
    return v.toLocaleString('fr-FR') + ' FCFA';
  }

  onTypeChange(data: Omit<Materiel, 'id'>): void {
    const found = this.typesMateriel.find(t => t.code === data.typeCode);
    if (found) { data.typeMateriel = found.nom; data.typeCouleur = found.couleur; data.typeIcone = found.icone; }
  }

  toast(msg: string): void {
    this.successMessage = msg; this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  openViewModal(m: Materiel): void { this.viewingMat = m; this.isViewModalOpen = true; }
  closeViewModal(): void           { this.isViewModalOpen = false; this.viewingMat = null; }

  openAddModal(): void  { this.newMat = this.emptyMat(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAdd(): void {
    const typeId = this.resolveTypeId(this.newMat.typeCode);
    const payload = {
      code: this.newMat.code, nom: this.newMat.nom,
      marque: this.newMat.marque, modele: this.newMat.modele,
      numeroSerie: this.newMat.numeroSerie, ecole: this.newMat.ecole,
      salle: this.newMat.salle, etat: this.reverseMapEtat(this.newMat.etat),
      dateAcquisition: this.newMat.dateAcquisition, valeur: this.newMat.valeur,
      description: this.newMat.description, active: this.newMat.enabled,
      ...(typeId ? { typeMaterielId: typeId } : {})
    };
    this.equipmentService.createMateriel(payload).subscribe({
      next: (created) => {
        if (created) { this.loadMateriels(); }
        else {
          const id = this.materiels.length ? Math.max(...this.materiels.map(m => m.id)) + 1 : 1;
          this.materiels = [...this.materiels, { id, ...this.newMat }];
        }
        this.closeAddModal(); this.toast('Matériel ajouté !');
      },
      error: () => {
        const id = this.materiels.length ? Math.max(...this.materiels.map(m => m.id)) + 1 : 1;
        this.materiels = [...this.materiels, { id, ...this.newMat }];
        this.closeAddModal(); this.toast('Matériel ajouté !');
      }
    });
  }

  openEditModal(m: Materiel): void {
    this.editingMat = m;
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
    this.closeEditModal(); this.toast('Matériel modifié !');
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
    this.closeDeleteModal(); this.toast('Matériel supprimé.');
  }
}
