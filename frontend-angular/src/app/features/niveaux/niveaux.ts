import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

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

  types: Niveau['type'][] = ['Licence', 'Master', 'Doctorat', 'Préparatoire', 'CPGE'];

  ecoles = [
    { sigle: 'SJI',       nom: 'Saint Jean Ingénieur',                     couleur: '#1D4ED8' },
    { sigle: 'SJM',       nom: 'Saint Jean Management',                    couleur: '#15803D' },
    { sigle: 'PRÉPAVOGT', nom: 'Prépavogt',                                 couleur: '#DC2626' },
    { sigle: 'CPGE',      nom: 'Classes Préparatoires aux Grandes Écoles',  couleur: '#7C3AED' },
  ];

  allFilieres = [
    'Génie Informatique', 'Génie Civil', 'Génie Électrique', 'Génie Mécanique', 'Génie Télécom', 'Génie Biomédical',
    'Management des Entreprises', 'Finance & Comptabilité', 'Marketing', 'Commerce International', 'Ressources Humaines', 'Logistique',
    'Mathématiques', 'Physique', 'Chimie', 'Sciences de la Vie', 'Lettres & Sciences Humaines', 'Droit des Affaires',
  ];

  niveaux: Niveau[] = [
    { id:1,  code:'L1', nom:'Licence 1',         type:'Licence',       ecoles:['SJI','SJM'],            filieres:['Génie Informatique','Génie Civil','Génie Électrique','Génie Mécanique','Génie Télécom','Génie Biomédical','Management des Entreprises','Finance & Comptabilité','Marketing','Commerce International','Ressources Humaines','Logistique'], annee:1, description:'Première année du cycle Licence. Acquisition des fondamentaux disciplinaires.', enabled:true },
    { id:2,  code:'L2', nom:'Licence 2',         type:'Licence',       ecoles:['SJI','SJM'],            filieres:['Génie Informatique','Génie Civil','Génie Électrique','Génie Mécanique','Génie Télécom','Génie Biomédical','Management des Entreprises','Finance & Comptabilité','Marketing','Commerce International','Ressources Humaines','Logistique'], annee:2, description:'Deuxième année du cycle Licence. Approfondissement des connaissances.', enabled:true },
    { id:3,  code:'L3', nom:'Licence 3',         type:'Licence',       ecoles:['SJI','SJM'],            filieres:['Génie Informatique','Génie Civil','Génie Électrique','Génie Mécanique','Génie Télécom','Génie Biomédical','Management des Entreprises','Finance & Comptabilité','Marketing','Commerce International','Ressources Humaines','Logistique'], annee:3, description:'Troisième année du cycle Licence. Spécialisation et projet de fin de cycle.', enabled:true },
    { id:4,  code:'M1', nom:'Master 1',          type:'Master',        ecoles:['SJI','SJM'],            filieres:['Génie Informatique','Génie Civil','Génie Électrique','Génie Mécanique','Génie Télécom','Génie Biomédical','Management des Entreprises','Finance & Comptabilité','Marketing','Commerce International','Ressources Humaines','Logistique'], annee:1, description:'Première année du cycle Master. Spécialisation avancée et initiation à la recherche.', enabled:true },
    { id:5,  code:'M2', nom:'Master 2',          type:'Master',        ecoles:['SJI','SJM'],            filieres:['Génie Informatique','Génie Civil','Génie Électrique','Génie Mécanique','Génie Télécom','Génie Biomédical','Management des Entreprises','Finance & Comptabilité','Marketing','Commerce International','Ressources Humaines','Logistique'], annee:2, description:'Deuxième année du cycle Master. Mémoire de recherche ou projet professionnel.', enabled:true },
    { id:6,  code:'P1', nom:'Prépa 1',           type:'Préparatoire',  ecoles:['PRÉPAVOGT'],            filieres:['Mathématiques','Physique','Chimie','Sciences de la Vie'], annee:1, description:'Première année de classe préparatoire. Renforcement intensif en sciences fondamentales.', enabled:true },
    { id:7,  code:'P2', nom:'Prépa 2',           type:'Préparatoire',  ecoles:['PRÉPAVOGT'],            filieres:['Mathématiques','Physique','Chimie','Sciences de la Vie'], annee:2, description:'Deuxième année de classe préparatoire. Préparation aux concours des grandes écoles.', enabled:true },
    { id:8,  code:'C1', nom:'CPGE 1',            type:'CPGE',          ecoles:['CPGE'],                 filieres:['Mathématiques','Physique','Chimie','Lettres & Sciences Humaines','Droit des Affaires'], annee:1, description:'Première année CPGE. Formation d\'excellence pour les concours nationaux et internationaux.', enabled:true },
    { id:9,  code:'C2', nom:'CPGE 2',            type:'CPGE',          ecoles:['CPGE'],                 filieres:['Mathématiques','Physique','Chimie','Lettres & Sciences Humaines','Droit des Affaires'], annee:2, description:'Deuxième année CPGE. Finalisation de la préparation aux grandes écoles.', enabled:true },
  ];

  emptyNiveau = (): Omit<Niveau, 'id'> => ({
    code: '', nom: '', type: 'Licence', ecoles: [], filieres: [], annee: 1, description: '', enabled: true
  });

  newNiveau: Omit<Niveau, 'id'>     = this.emptyNiveau();
  editNiveauData: Omit<Niveau, 'id'> = this.emptyNiveau();

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

  openAddModal(): void  { this.newNiveau = this.emptyNiveau(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAddNiveau(): void {
    const id = this.niveaux.length ? Math.max(...this.niveaux.map(n => n.id)) + 1 : 1;
    this.niveaux = [...this.niveaux, { id, ...this.newNiveau }];
    this.closeAddModal(); this.toast('Niveau ajouté avec succès !');
  }

  openEditModal(n: Niveau): void {
    this.editingNiveau = n;
    this.editNiveauData = { ...n, ecoles: [...n.ecoles], filieres: [...n.filieres] };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingNiveau = null; }
  handleEditNiveau(): void {
    if (!this.editingNiveau) return;
    this.niveaux = this.niveaux.map(n => n.id === this.editingNiveau!.id ? { id: n.id, ...this.editNiveauData } : n);
    this.closeEditModal(); this.toast('Niveau modifié avec succès !');
  }

  openDeleteModal(n: Niveau): void { this.niveauToDelete = n; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void         { this.isDeleteModalOpen = false; this.niveauToDelete = null; }
  confirmDelete(): void {
    if (!this.niveauToDelete) return;
    this.niveaux = this.niveaux.filter(n => n.id !== this.niveauToDelete!.id);
    this.closeDeleteModal(); this.toast('Niveau supprimé.');
  }
}
