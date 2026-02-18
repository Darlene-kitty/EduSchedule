import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../../environments/environment';

export interface Resource {
  id: number;
  nom: string;
  code: string;
  type: string;
  capacite: number;
  batiment?: string;
  etage?: number;
  disponible: boolean;
}

export interface CreateResourceRequest {
  nom: string;
  code: string;
  type: string;
  capacite: number;
  batiment?: string;
  etage?: number;
  disponible: boolean;
}

@Component({
  selector: 'app-resources',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  templateUrl: './resources.html',
  styleUrl: './resources.css'
})
export class ResourcesComponent implements OnInit {
  resources: Resource[] = [];
  filteredResources: Resource[] = [];
  loading = true;
  error: string | null = null;
  message = '';
  isError = false;

  searchQuery = '';
  selectedType = 'all';
  isAddModalOpen = false;

  newResource: CreateResourceRequest = {
    nom: '', code: '', type: 'SALLE', capacite: 30, disponible: true
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.loadResources(); }

  loadResources(): void {
    this.loading = true;
    const mockResources: Resource[] = [
      { id: 1, nom: 'Amphithéâtre A', code: 'AMPHI-A', type: 'AMPHITHEATRE', capacite: 200, batiment: 'Bâtiment A', etage: 0, disponible: true },
      { id: 2, nom: 'Salle 101', code: 'SALLE-101', type: 'SALLE', capacite: 40, batiment: 'Bâtiment B', etage: 1, disponible: true },
      { id: 3, nom: 'Labo Informatique', code: 'LAB-INFO', type: 'LABORATOIRE', capacite: 25, batiment: 'Bâtiment C', etage: 2, disponible: false },
      { id: 4, nom: 'Salle 203', code: 'SALLE-203', type: 'SALLE', capacite: 35, batiment: 'Bâtiment B', etage: 2, disponible: true },
      { id: 5, nom: 'Amphithéâtre B', code: 'AMPHI-B', type: 'AMPHITHEATRE', capacite: 150, batiment: 'Bâtiment A', etage: 0, disponible: false },
      { id: 6, nom: 'Labo Physique', code: 'LAB-PHY', type: 'LABORATOIRE', capacite: 20, batiment: 'Bâtiment D', etage: 1, disponible: true },
    ];
    this.resources = mockResources;
    this.applyFilters();
    this.loading = false;
  }

  applyFilters(): void {
    this.filteredResources = this.resources.filter(r => {
      const matchesSearch =
        r.nom.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        r.type.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        r.code.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchesType = this.selectedType === 'all' || r.type === this.selectedType;
      return matchesSearch && matchesType;
    });
  }

  onSearchChange(): void { this.applyFilters(); }
  onTypeChange(type: string): void { this.selectedType = type; this.applyFilters(); }

  get uniqueTypes(): string[] {
    return Array.from(new Set(this.resources.map(r => r.type)));
  }

  get totalDisponible(): number { return this.resources.filter(r => r.disponible).length; }
  get totalIndisponible(): number { return this.resources.filter(r => !r.disponible).length; }
  get totalCapacite(): number { return this.resources.reduce((sum, r) => sum + r.capacite, 0); }

  openAddModal(): void {
    this.newResource = { nom: '', code: '', type: 'SALLE', capacite: 30, disponible: true };
    this.isAddModalOpen = true;
  }

  closeModal(): void { this.isAddModalOpen = false; }

  handleAddResource(): void {
    this.http.post<Resource>(`${environment.apiUrl}/resources`, this.newResource).subscribe({
      next: (resource) => {
        this.resources = [...this.resources, resource];
        this.applyFilters();
        this.isError = false;
        this.message = 'Ressource créée avec succès';
        this.closeModal();
      },
      error: (err) => {
        this.isError = true;
        this.message = err.message || 'Erreur lors de la création';
      }
    });
  }

  handleDeleteResource(id: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette ressource ?')) return;
    this.http.delete(`${environment.apiUrl}/resources/${id}`).subscribe({
      next: () => {
        this.resources = this.resources.filter(r => r.id !== id);
        this.applyFilters();
        this.isError = false;
        this.message = 'Ressource supprimée avec succès';
      },
      error: (err) => {
        this.isError = true;
        this.message = err.message || 'Erreur lors de la suppression';
      }
    });
  }

  getStatusClass(disponible: boolean): string { return disponible ? 'badge-green' : 'badge-red'; }
  getStatusLabel(disponible: boolean): string { return disponible ? 'Disponible' : 'Indisponible'; }

  getTypeClass(type: string): string {
    const t = type.toUpperCase();
    if (t.includes('AMPHI')) return 'badge-purple';
    if (t.includes('SALLE')) return 'badge-blue';
    if (t.includes('LAB'))   return 'badge-cyan';
    return 'badge-gray';
  }

  getTypeIcon(type: string): string {
    const t = type.toUpperCase();
    if (t.includes('AMPHI')) return 'monitor';
    if (t.includes('LAB'))   return 'computer';
    return 'meeting_room';
  }
}