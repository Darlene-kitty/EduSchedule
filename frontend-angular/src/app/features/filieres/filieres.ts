import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

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

  ecoles = [
    { sigle: 'SJI',      nom: 'Saint Jean Ingénieur',                       couleur: '#1D4ED8' },
    { sigle: 'SJM',      nom: 'Saint Jean Management',                      couleur: '#15803D' },
    { sigle: 'PRÉPAVOGT',nom: 'Prépavogt',                                   couleur: '#DC2626' },
    { sigle: 'CPGE',     nom: 'Classes Préparatoires aux Grandes Écoles',    couleur: '#7C3AED' },
  ];

  allNiveaux = ['L1', 'L2', 'L3', 'M1', 'M2', 'Prépa 1', 'Prépa 2', 'CPGE 1', 'CPGE 2'];

  filieres: Filiere[] = [
    { id:1,  code:'GI',   nom:'Génie Informatique',           ecole:'Saint Jean Ingénieur',                     sigleEcole:'SJI',       couleurEcole:'#1D4ED8', responsable:'Dr. Alain Mbarga',      description:'Formation en développement logiciel, réseaux, intelligence artificielle et systèmes embarqués.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:2,  code:'GC',   nom:'Génie Civil',                  ecole:'Saint Jean Ingénieur',                     sigleEcole:'SJI',       couleurEcole:'#1D4ED8', responsable:'Dr. Pierre Essama',     description:'Formation en construction, structures, géotechnique et gestion de projets BTP.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:3,  code:'GE',   nom:'Génie Électrique',             ecole:'Saint Jean Ingénieur',                     sigleEcole:'SJI',       couleurEcole:'#1D4ED8', responsable:'Dr. Samuel Nkoa',       description:'Formation en électronique, automatisme, énergie et systèmes de contrôle.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:4,  code:'GM',   nom:'Génie Mécanique',              ecole:'Saint Jean Ingénieur',                     sigleEcole:'SJI',       couleurEcole:'#1D4ED8', responsable:'Dr. Éric Fouda',        description:'Formation en conception mécanique, fabrication et maintenance industrielle.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:5,  code:'GT',   nom:'Génie Télécom',                ecole:'Saint Jean Ingénieur',                     sigleEcole:'SJI',       couleurEcole:'#1D4ED8', responsable:'Dr. Cédric Owona',      description:'Formation en télécommunications, réseaux mobiles et systèmes de transmission.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:6,  code:'GB',   nom:'Génie Biomédical',             ecole:'Saint Jean Ingénieur',                     sigleEcole:'SJI',       couleurEcole:'#1D4ED8', responsable:'Dr. Lucie Abena',       description:'Formation à l\'interface de l\'ingénierie et de la médecine.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:7,  code:'ME',   nom:'Management des Entreprises',   ecole:'Saint Jean Management',                    sigleEcole:'SJM',       couleurEcole:'#15803D', responsable:'Prof. Marie-Claire Ateba',description:'Formation en gestion d\'entreprise, stratégie et leadership.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:8,  code:'FC',   nom:'Finance & Comptabilité',       ecole:'Saint Jean Management',                    sigleEcole:'SJM',       couleurEcole:'#15803D', responsable:'Dr. Robert Nganou',     description:'Formation en comptabilité, audit, finance d\'entreprise et contrôle de gestion.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:9,  code:'MKT',  nom:'Marketing',                    ecole:'Saint Jean Management',                    sigleEcole:'SJM',       couleurEcole:'#15803D', responsable:'Dr. Sylvie Biya',       description:'Formation en marketing digital, communication et stratégie commerciale.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:10, code:'CI',   nom:'Commerce International',       ecole:'Saint Jean Management',                    sigleEcole:'SJM',       couleurEcole:'#15803D', responsable:'Dr. Jean Fotso',        description:'Formation en commerce extérieur, douanes et logistique internationale.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:11, code:'RH',   nom:'Ressources Humaines',          ecole:'Saint Jean Management',                    sigleEcole:'SJM',       couleurEcole:'#15803D', responsable:'Dr. Ange Mendo',        description:'Formation en gestion des talents, droit du travail et développement organisationnel.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:12, code:'LOG',  nom:'Logistique',                   ecole:'Saint Jean Management',                    sigleEcole:'SJM',       couleurEcole:'#15803D', responsable:'Dr. Hervé Tchoupo',     description:'Formation en supply chain, transport et gestion des stocks.', niveaux:['L1','L2','L3','M1','M2'], duree:5, enabled:true },
    { id:13, code:'PVSCI',nom:'Sciences (Prépavogt)',         ecole:'Prépavogt',                                sigleEcole:'PRÉPAVOGT', couleurEcole:'#DC2626', responsable:'Dr. Paul Vogt Essomba', description:'Préparation scientifique intensive aux concours des grandes écoles d\'ingénieurs.', niveaux:['Prépa 1','Prépa 2'], duree:2, enabled:true },
    { id:14, code:'CPGE1',nom:'MPSI / PCSI',                  ecole:'Classes Préparatoires aux Grandes Écoles', sigleEcole:'CPGE',      couleurEcole:'#7C3AED', responsable:'Dr. Hélène Mbarga',     description:'Classes préparatoires Maths-Physique et Physique-Chimie pour les grandes écoles.', niveaux:['CPGE 1','CPGE 2'], duree:2, enabled:true },
    { id:15, code:'CPGE2',nom:'ECG / HEC Prépa',              ecole:'Classes Préparatoires aux Grandes Écoles', sigleEcole:'CPGE',      couleurEcole:'#7C3AED', responsable:'Dr. Hélène Mbarga',     description:'Classes préparatoires économiques et commerciales voie générale.', niveaux:['CPGE 1','CPGE 2'], duree:2, enabled:true },
  ];

  emptyFiliere = (): Omit<Filiere, 'id'> => ({
    code: '', nom: '', ecole: '', sigleEcole: '', couleurEcole: '#1D4ED8',
    responsable: '', description: '', niveaux: [], duree: 3, enabled: true
  });

  newFiliere: Omit<Filiere, 'id'>  = this.emptyFiliere();
  editFiliereData: Omit<Filiere, 'id'> = this.emptyFiliere();

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

  get ecoleStats(): { sigle: string; nom: string; couleur: string; count: number }[] {
    return this.ecoles.map(e => ({
      ...e,
      count: this.filieres.filter(f => f.sigleEcole === e.sigle).length
    }));
  }

  onEcoleChange(data: Omit<Filiere, 'id'>): void {
    const found = this.ecoles.find(e => e.sigle === data.sigleEcole);
    if (found) { data.ecole = found.nom; data.couleurEcole = found.couleur; }
  }

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
  openAddModal(): void  { this.newFiliere = this.emptyFiliere(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAddFiliere(): void {
    const id = this.filieres.length ? Math.max(...this.filieres.map(f => f.id)) + 1 : 1;
    this.filieres = [...this.filieres, { id, ...this.newFiliere }];
    this.closeAddModal();
    this.toast('Filière ajoutée avec succès !');
  }

  /* ── Édition ── */
  openEditModal(f: Filiere): void {
    this.editingFiliere = f;
    this.editFiliereData = { ...f, niveaux: [...f.niveaux] };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingFiliere = null; }
  handleEditFiliere(): void {
    if (!this.editingFiliere) return;
    this.filieres = this.filieres.map(f => f.id === this.editingFiliere!.id ? { id: f.id, ...this.editFiliereData } : f);
    this.closeEditModal();
    this.toast('Filière modifiée avec succès !');
  }

  /* ── Suppression ── */
  openDeleteModal(f: Filiere): void { this.filiereToDelete = f; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void          { this.isDeleteModalOpen = false; this.filiereToDelete = null; }
  confirmDelete(): void {
    if (!this.filiereToDelete) return;
    this.filieres = this.filieres.filter(f => f.id !== this.filiereToDelete!.id);
    this.closeDeleteModal();
    this.toast('Filière supprimée.');
  }
}
