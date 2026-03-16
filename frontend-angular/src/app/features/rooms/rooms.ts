import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

export interface Salle {
  id: number;
  code: string;
  nom: string;
  type: 'Amphithéâtre' | 'Salle de cours' | 'Salle TP' | 'Salle informatique' | 'Laboratoire' | 'Salle de réunion';
  ecole: string;
  batiment: string;
  etage: string;
  capacite: number;
  equipements: string[];
  disponible: boolean;
  enabled: boolean;
}

@Component({
  selector: 'app-rooms',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './rooms.html',
  styleUrl: './rooms.css'
})
export class Rooms implements OnInit {

  searchQuery  = '';
  filterType   = '';
  filterEcole  = '';
  viewMode: 'table' | 'grid' = 'table';

  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;

  editingSalle:  Salle | null = null;
  viewingSalle:  Salle | null = null;
  salleToDelete: Salle | null = null;

  currentDate = ''; currentTime = '';
  showSuccess = false; successMessage = '';

  types: Salle['type'][] = ['Amphithéâtre','Salle de cours','Salle TP','Salle informatique','Laboratoire','Salle de réunion'];

  typeIcons: Record<string, string> = {
    'Amphithéâtre':      'theater_comedy',
    'Salle de cours':    'school',
    'Salle TP':          'science',
    'Salle informatique':'computer',
    'Laboratoire':       'biotech',
    'Salle de réunion':  'groups',
  };

  typeColors: Record<string, string> = {
    'Amphithéâtre':      '#1D4ED8',
    'Salle de cours':    '#15803D',
    'Salle TP':          '#7C3AED',
    'Salle informatique':'#0891B2',
    'Laboratoire':       '#DC2626',
    'Salle de réunion':  '#EA580C',
  };

  ecoles = ['SJI','SJM','PRÉPAVOGT','CPGE','Commun'];

  allEquipements = [
    'Vidéoprojecteur','Tableau blanc','Tableau noir','Climatisation',
    'Sonorisation','Wifi','Ordinateurs','Microscopes','Oscilloscopes',
    'Imprimante 3D','Caméra','Écran interactif','Prises électriques',
  ];

  salles: Salle[] = [
    { id:1,  code:'AMPH-A',  nom:'Amphithéâtre A',      type:'Amphithéâtre',      ecole:'Commun',     batiment:'Bât. Principal', etage:'RDC',    capacite:300, equipements:['Vidéoprojecteur','Sonorisation','Climatisation','Wifi'],                    disponible:true,  enabled:true },
    { id:2,  code:'AMPH-B',  nom:'Amphithéâtre B',      type:'Amphithéâtre',      ecole:'Commun',     batiment:'Bât. Principal', etage:'RDC',    capacite:200, equipements:['Vidéoprojecteur','Sonorisation','Climatisation'],                           disponible:false, enabled:true },
    { id:3,  code:'S-101',   nom:'Salle 101',           type:'Salle de cours',    ecole:'SJI',        batiment:'Bât. A',         etage:'1er',    capacite:50,  equipements:['Vidéoprojecteur','Tableau blanc','Climatisation','Wifi'],                    disponible:true,  enabled:true },
    { id:4,  code:'S-102',   nom:'Salle 102',           type:'Salle de cours',    ecole:'SJI',        batiment:'Bât. A',         etage:'1er',    capacite:50,  equipements:['Vidéoprojecteur','Tableau blanc','Climatisation'],                           disponible:true,  enabled:true },
    { id:5,  code:'S-201',   nom:'Salle 201',           type:'Salle de cours',    ecole:'SJM',        batiment:'Bât. B',         etage:'2ème',   capacite:55,  equipements:['Vidéoprojecteur','Tableau blanc','Climatisation','Wifi'],                    disponible:true,  enabled:true },
    { id:6,  code:'S-202',   nom:'Salle 202',           type:'Salle de cours',    ecole:'SJM',        batiment:'Bât. B',         etage:'2ème',   capacite:55,  equipements:['Vidéoprojecteur','Tableau blanc'],                                           disponible:false, enabled:true },
    { id:7,  code:'TP-INFO1',nom:'Salle Info 1',        type:'Salle informatique', ecole:'SJI',       batiment:'Bât. C',         etage:'RDC',    capacite:30,  equipements:['Ordinateurs','Vidéoprojecteur','Climatisation','Wifi','Imprimante 3D'],      disponible:true,  enabled:true },
    { id:8,  code:'TP-INFO2',nom:'Salle Info 2',        type:'Salle informatique', ecole:'SJI',       batiment:'Bât. C',         etage:'RDC',    capacite:30,  equipements:['Ordinateurs','Vidéoprojecteur','Climatisation','Wifi'],                      disponible:true,  enabled:true },
    { id:9,  code:'TP-ELEC', nom:'Salle TP Électrique', type:'Salle TP',          ecole:'SJI',        batiment:'Bât. C',         etage:'1er',    capacite:20,  equipements:['Oscilloscopes','Tableau blanc','Prises électriques','Climatisation'],        disponible:true,  enabled:true },
    { id:10, code:'LABO-CHM',nom:'Laboratoire Chimie',  type:'Laboratoire',       ecole:'PRÉPAVOGT',  batiment:'Bât. D',         etage:'RDC',    capacite:25,  equipements:['Microscopes','Tableau noir','Climatisation'],                               disponible:true,  enabled:true },
    { id:11, code:'LABO-PHY',nom:'Laboratoire Physique',type:'Laboratoire',       ecole:'PRÉPAVOGT',  batiment:'Bât. D',         etage:'1er',    capacite:25,  equipements:['Oscilloscopes','Microscopes','Tableau noir','Prises électriques'],           disponible:false, enabled:true },
    { id:12, code:'S-CPGE1', nom:'Salle CPGE 1',        type:'Salle de cours',    ecole:'CPGE',       batiment:'Bât. E',         etage:'RDC',    capacite:25,  equipements:['Tableau noir','Vidéoprojecteur','Climatisation','Wifi'],                     disponible:true,  enabled:true },
    { id:13, code:'S-CPGE2', nom:'Salle CPGE 2',        type:'Salle de cours',    ecole:'CPGE',       batiment:'Bât. E',         etage:'1er',    capacite:25,  equipements:['Tableau noir','Vidéoprojecteur','Climatisation'],                            disponible:true,  enabled:true },
    { id:14, code:'CONF',    nom:'Salle de Conférence', type:'Salle de réunion',  ecole:'Commun',     batiment:'Bât. Principal', etage:'2ème',   capacite:40,  equipements:['Vidéoprojecteur','Écran interactif','Sonorisation','Climatisation','Wifi'],  disponible:true,  enabled:true },
  ];

  emptyS = (): Omit<Salle, 'id'> => ({
    code:'', nom:'', type:'Salle de cours', ecole:'', batiment:'', etage:'',
    capacite:30, equipements:[], disponible:true, enabled:true
  });

  newSalle: Omit<Salle, 'id'>     = this.emptyS();
  editSalleData: Omit<Salle, 'id'> = this.emptyS();

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

  get filteredSalles(): Salle[] {
    return this.salles.filter(s => {
      const q = this.searchQuery.toLowerCase();
      const matchSearch = !q || s.nom.toLowerCase().includes(q) || s.code.toLowerCase().includes(q) || s.batiment.toLowerCase().includes(q);
      const matchType  = !this.filterType  || s.type  === this.filterType;
      const matchEcole = !this.filterEcole || s.ecole === this.filterEcole;
      return matchSearch && matchType && matchEcole;
    });
  }

  get disponiblesCount(): number  { return this.salles.filter(s => s.disponible && s.enabled).length; }
  get occupeesCount(): number     { return this.salles.filter(s => !s.disponible && s.enabled).length; }
  get totalCapacite(): number     { return this.salles.reduce((s, r) => s + r.capacite, 0); }

  getTypeIcon(type: string): string  { return this.typeIcons[type]  || 'meeting_room'; }
  getTypeColor(type: string): string { return this.typeColors[type] || '#6B7280'; }

  toggleEquip(data: Omit<Salle, 'id'>, e: string): void {
    const i = data.equipements.indexOf(e); if (i >= 0) data.equipements.splice(i, 1); else data.equipements.push(e);
  }
  hasEquipNew(e: string): boolean  { return this.newSalle.equipements.includes(e); }
  hasEquipEdit(e: string): boolean { return this.editSalleData.equipements.includes(e); }

  toast(msg: string): void {
    this.successMessage = msg; this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  openViewModal(s: Salle): void { this.viewingSalle = s; this.isViewModalOpen = true; }
  closeViewModal(): void        { this.isViewModalOpen = false; this.viewingSalle = null; }

  openAddModal(): void  { this.newSalle = this.emptyS(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAdd(): void {
    const id = this.salles.length ? Math.max(...this.salles.map(s => s.id)) + 1 : 1;
    this.salles = [...this.salles, { id, ...this.newSalle }];
    this.closeAddModal(); this.toast('Salle ajoutée avec succès !');
  }

  openEditModal(s: Salle): void {
    this.editingSalle = s;
    this.editSalleData = { ...s, equipements: [...s.equipements] };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingSalle = null; }
  handleEdit(): void {
    if (!this.editingSalle) return;
    this.salles = this.salles.map(s => s.id === this.editingSalle!.id ? { id: s.id, ...this.editSalleData } : s);
    this.closeEditModal(); this.toast('Salle modifiée avec succès !');
  }

  openDeleteModal(s: Salle): void { this.salleToDelete = s; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void        { this.isDeleteModalOpen = false; this.salleToDelete = null; }
  confirmDelete(): void {
    if (!this.salleToDelete) return;
    this.salles = this.salles.filter(s => s.id !== this.salleToDelete!.id);
    this.closeDeleteModal(); this.toast('Salle supprimée.');
  }
}
