import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { EquipmentManagementService } from '../../core/services/equipment-management.service';

export interface TypeMateriel {
  id: number;
  code: string;
  nom: string;
  icone: string;
  couleur: string;
  description: string;
  nombreMateriel: number;
  enabled: boolean;
}

@Component({
  selector: 'app-equipment-types',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './equipment-types.html',
  styleUrl: './equipment-types.css'
})
export class EquipmentTypesComponent implements OnInit {

  constructor(private equipmentService: EquipmentManagementService) {}

  searchQuery = '';
  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;

  editingType:  TypeMateriel | null = null;
  viewingType:  TypeMateriel | null = null;
  typeToDelete: TypeMateriel | null = null;

  currentDate = ''; currentTime = '';
  showSuccess = false; successMessage = '';

  icones = [
    'computer','laptop','tablet_android','phone_android','tv','videocam',
    'print','scanner','camera_alt','mic','speaker','headphones',
    'science','biotech','precision_manufacturing','build','electrical_services',
    'chair','table_restaurant','meeting_room','inventory_2','devices',
  ];

  couleurs = [
    { label:'Bleu',    value:'#1D4ED8' },
    { label:'Vert',    value:'#15803D' },
    { label:'Violet',  value:'#7C3AED' },
    { label:'Orange',  value:'#EA580C' },
    { label:'Rouge',   value:'#DC2626' },
    { label:'Cyan',    value:'#0891B2' },
    { label:'Rose',    value:'#DB2777' },
    { label:'Gris',    value:'#4B5563' },
  ];

  types: TypeMateriel[] = [
    { id:1,  code:'INFO',   nom:'Informatique',          icone:'computer',                couleur:'#1D4ED8', description:'Ordinateurs, laptops, tablettes et périphériques informatiques.',          nombreMateriel:45, enabled:true },
    { id:2,  code:'PROJ',   nom:'Projection',            icone:'videocam',                couleur:'#7C3AED', description:'Vidéoprojecteurs, écrans de projection et systèmes d\'affichage.',         nombreMateriel:18, enabled:true },
    { id:3,  code:'AUDIO',  nom:'Audio & Sonorisation',  icone:'speaker',                 couleur:'#EA580C', description:'Microphones, enceintes, amplificateurs et systèmes de sonorisation.',      nombreMateriel:12, enabled:true },
    { id:4,  code:'LABO',   nom:'Équipement de Labo',    icone:'science',                 couleur:'#DC2626', description:'Microscopes, oscilloscopes, générateurs de signaux et instruments de mesure.', nombreMateriel:30, enabled:true },
    { id:5,  code:'IMPR',   nom:'Impression',            icone:'print',                   couleur:'#0891B2', description:'Imprimantes, scanners, photocopieurs et traceurs.',                         nombreMateriel:10, enabled:true },
    { id:6,  code:'MOBI',   nom:'Mobilier',              icone:'chair',                   couleur:'#4B5563', description:'Tables, chaises, tableaux blancs et mobilier de salle.',                    nombreMateriel:200, enabled:true },
    { id:7,  code:'ELEC',   nom:'Électronique',          icone:'electrical_services',     couleur:'#15803D', description:'Composants électroniques, alimentations et équipements de mesure électrique.', nombreMateriel:25, enabled:true },
    { id:8,  code:'CAM',    nom:'Caméra & Photo',        icone:'camera_alt',              couleur:'#DB2777', description:'Caméras, appareils photo, webcams et équipements de captation vidéo.',      nombreMateriel:8,  enabled:true },
  ];

  emptyType = (): Omit<TypeMateriel, 'id' | 'nombreMateriel'> => ({
    code:'', nom:'', icone:'devices', couleur:'#1D4ED8', description:'', enabled:true
  });

  newType: Omit<TypeMateriel, 'id' | 'nombreMateriel'>     = this.emptyType();
  editTypeData: Omit<TypeMateriel, 'id' | 'nombreMateriel'> = this.emptyType();

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadTypes();
  }

  private loadTypes(): void {
    this.equipmentService.getAllTypes().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.types = data.map(t => ({
            id: t.id, code: t.code, nom: t.nom,
            icone: t.icone ?? 'devices', couleur: t.couleur ?? '#1D4ED8',
            description: t.description ?? '', nombreMateriel: 0, enabled: t.active
          }));
        }
      },
      error: () => {} // garde les données démo
    });
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  get filteredTypes(): TypeMateriel[] {
    if (!this.searchQuery) return this.types;
    const q = this.searchQuery.toLowerCase();
    return this.types.filter(t => t.nom.toLowerCase().includes(q) || t.code.toLowerCase().includes(q));
  }

  get totalMateriel(): number { return this.types.reduce((s, t) => s + t.nombreMateriel, 0); }

  toast(msg: string): void {
    this.successMessage = msg; this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  openViewModal(t: TypeMateriel): void { this.viewingType = t; this.isViewModalOpen = true; }
  closeViewModal(): void               { this.isViewModalOpen = false; this.viewingType = null; }

  openAddModal(): void  { this.newType = this.emptyType(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAdd(): void {
    const dto = { code: this.newType.code, nom: this.newType.nom, icone: this.newType.icone, couleur: this.newType.couleur, description: this.newType.description, active: this.newType.enabled };
    this.equipmentService.createType(dto).subscribe({
      next: (created) => {
        if (created) { this.loadTypes(); }
        else {
          const id = this.types.length ? Math.max(...this.types.map(t => t.id)) + 1 : 1;
          this.types = [...this.types, { id, nombreMateriel: 0, ...this.newType }];
        }
        this.closeAddModal(); this.toast('Type de matériel ajouté !');
      },
      error: () => {
        const id = this.types.length ? Math.max(...this.types.map(t => t.id)) + 1 : 1;
        this.types = [...this.types, { id, nombreMateriel: 0, ...this.newType }];
        this.closeAddModal(); this.toast('Type de matériel ajouté !');
      }
    });
  }

  openEditModal(t: TypeMateriel): void {
    this.editingType = t;
    this.editTypeData = { code: t.code, nom: t.nom, icone: t.icone, couleur: t.couleur, description: t.description, enabled: t.enabled };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingType = null; }
  handleEdit(): void {
    if (!this.editingType) return;
    const id = this.editingType.id;
    const dto = { code: this.editTypeData.code, nom: this.editTypeData.nom, icone: this.editTypeData.icone, couleur: this.editTypeData.couleur, description: this.editTypeData.description, active: this.editTypeData.enabled };
    this.equipmentService.updateType(id, dto).subscribe({
      next: () => { this.loadTypes(); },
      error: () => { this.types = this.types.map(t => t.id === id ? { ...t, ...this.editTypeData } : t); }
    });
    this.closeEditModal(); this.toast('Type modifié !');
  }

  openDeleteModal(t: TypeMateriel): void { this.typeToDelete = t; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void               { this.isDeleteModalOpen = false; this.typeToDelete = null; }
  confirmDelete(): void {
    if (!this.typeToDelete) return;
    const id = this.typeToDelete.id;
    this.equipmentService.deleteType(id).subscribe({
      next: () => { this.loadTypes(); },
      error: () => { this.types = this.types.filter(t => t.id !== id); }
    });
    this.closeDeleteModal(); this.toast('Type supprimé.');
  }
}
