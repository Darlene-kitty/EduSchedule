import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

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
    const id = this.materiels.length ? Math.max(...this.materiels.map(m => m.id)) + 1 : 1;
    this.materiels = [...this.materiels, { id, ...this.newMat }];
    this.closeAddModal(); this.toast('Matériel ajouté !');
  }

  openEditModal(m: Materiel): void {
    this.editingMat = m;
    this.editMatData = { ...m };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingMat = null; }
  handleEdit(): void {
    if (!this.editingMat) return;
    this.materiels = this.materiels.map(m => m.id === this.editingMat!.id ? { id: m.id, ...this.editMatData } : m);
    this.closeEditModal(); this.toast('Matériel modifié !');
  }

  openDeleteModal(m: Materiel): void { this.matToDelete = m; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void           { this.isDeleteModalOpen = false; this.matToDelete = null; }
  confirmDelete(): void {
    if (!this.matToDelete) return;
    this.materiels = this.materiels.filter(m => m.id !== this.matToDelete!.id);
    this.closeDeleteModal(); this.toast('Matériel supprimé.');
  }
}
