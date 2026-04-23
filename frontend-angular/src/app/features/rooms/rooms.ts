import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { RoomsManagementService } from '../../core/services/rooms-management.service';

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
  private roomsService = inject(RoomsManagementService);

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

  salles: Salle[] = [];

  emptyS = (): Omit<Salle, 'id'> => ({
    code:'', nom:'', type:'Salle de cours', ecole:'', batiment:'', etage:'',
    capacite:30, equipements:[], disponible:true, enabled:true
  });

  newSalle: Omit<Salle, 'id'>      = this.emptyS();
  editSalleData: Omit<Salle, 'id'> = this.emptyS();

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadSalles();
  }

  private loadSalles(): void {
    this.roomsService.getRooms().subscribe((rooms: any[]) => {
      this.salles = rooms.map(r => ({
        id: r.id,
        code: r.code ?? String(r.id),
        nom: r.name,
        type: (this.backendTypeToDisplay(r.type)) as Salle['type'],
        ecole: '',
        batiment: r.batiment ?? '',
        etage: r.etage ?? '',
        capacite: r.capacite ?? 0,
        equipements: [],
        disponible: r.disponible ?? true,
        enabled: r.active ?? true
      }));
    });
  }

  private backendTypeToDisplay(type: string): string {
    const map: Record<string, string> = {
      'AMPHITHEATRE':  'Amphithéâtre',
      'SALLE_COURS':   'Salle de cours',
      'SALLE_TP':      'Salle TP',
      'LABORATOIRE':   'Laboratoire',
      'SALLE_TD':      'Salle de réunion',
      'BIBLIOTHEQUE':  'Salle de cours',
    };
    return map[type] ?? 'Salle de cours';
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
    this.roomsService.addRoom({
      name: this.newSalle.nom,
      building: this.newSalle.batiment,
      capacity: this.newSalle.capacite,
      type: this.newSalle.type,
      equipment: this.newSalle.equipements,
      status: this.newSalle.disponible ? 'available' : 'occupied'
    }, this.newSalle.code).subscribe(() => {
      this.closeAddModal();
      this.toast('Salle ajoutée avec succès !');
      this.loadSalles();
    });
  }

  openEditModal(s: Salle): void {
    this.editingSalle = s;
    this.editSalleData = { ...s, equipements: [...s.equipements] };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingSalle = null; }
  handleEdit(): void {
    if (!this.editingSalle) return;
    this.roomsService.updateRoom(this.editingSalle.id, {
      name: this.editSalleData.nom,
      building: this.editSalleData.batiment,
      capacity: this.editSalleData.capacite,
      type: this.editSalleData.type,
      equipment: this.editSalleData.equipements,
      status: this.editSalleData.disponible ? 'available' : 'occupied'
    }, this.editSalleData.code).subscribe(() => {
      this.closeEditModal();
      this.toast('Salle modifiée avec succès !');
      this.loadSalles();
    });
  }

  openDeleteModal(s: Salle): void { this.salleToDelete = s; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void        { this.isDeleteModalOpen = false; this.salleToDelete = null; }
  confirmDelete(): void {
    if (!this.salleToDelete) return;
    this.roomsService.deleteRoom(this.salleToDelete.id).subscribe(() => {
      this.closeDeleteModal();
      this.toast('Salle supprimée.');
      this.loadSalles();
    });
  }
}
