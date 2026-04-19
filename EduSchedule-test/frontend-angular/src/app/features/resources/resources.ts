import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { ResourcesManagementService, EquipmentDashboard, EquipmentAlerte, MaintenanceRecord } from '../../core/services/resources-management.service';

export interface Assignment { room: string; course: string; }
export interface Resource {
  id: number; name: string; category: string; icon: string;
  available: number; total: number; location: string; assignments: Assignment[];
}
export interface ImportedResource { name: string; category: string; location: string; total: number; valid: boolean; errors: string[]; }

@Component({
  selector: 'app-resources',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './resources.html',
  styleUrl: './resources.css'
})
export class ResourcesComponent implements OnInit {
  private resourcesService = inject(ResourcesManagementService);
  searchQuery = ''; activeTab: 'inventaire' | 'equipement' = 'inventaire';
  currentDate = ''; currentTime = '';
  isModalOpen = false;
  isImportModalOpen = false;
  importStep: 1 | 2 = 1;
  importFile: File | null = null;
  isDragging = false;
  isImportLoading = false;
  importParseError: string | null = null;
  importPreviewRows: ImportedResource[] = [];
  showImportToast = false;
  importedCount = 0;
  validImportCount: number = 0;
  importPreviewFilter: 'all' | 'valid' | 'invalid' = 'all';
  importColumns = ['Nom ressource', 'Catégorie', 'Emplacement', 'Quantité totale'];

  resources: Resource[] = [];

  // Données tableau de bord équipements
  equipmentDashboard: EquipmentDashboard | null = null;
  equipmentAlertes: EquipmentAlerte[] = [];
  isLoadingDashboard = false;

  newResource = { name: '', category: '', location: '', total: 1 };

  readonly equipmentNames = [
    'Projecteur HD', 'Projecteur 4K', 'Écran de projection',
    'Tableau blanc interactif', 'Ordinateur portable', 'Ordinateur fixe',
    'Imprimante', 'Scanner', 'Webcam HD', 'Microphone', 'Enceinte',
    'Amplificateur', 'Câble HDMI', 'Câble VGA', 'Multiprise',
    'Climatiseur', 'Ventilateur', 'Tableau noir', 'Tableau blanc'
  ];

  readonly availableLocations = [
    'Bâtiment A - Réserve', 'Bâtiment A - Salle 101', 'Bâtiment A - Salle 102',
    'Bâtiment B - Réserve', 'Bâtiment B - Salle 201', 'Bâtiment B - Salle 202',
    'Bâtiment C - Lab informatique', 'Bâtiment C - Réserve',
    'Amphithéâtre A', 'Amphithéâtre B', 'Salle des professeurs', 'Administration'
  ];

  ngOnInit(): void { 
    this.updateDateTime(); 
    setInterval(() => this.updateDateTime(), 1000);
    this.loadResources();
  }

  private loadResources(): void {
    this.resourcesService.getResources().subscribe({
      next: (data) => {
        this.resources = data.map(r => ({
          id: r.id,
          name: r.name,
          category: r.type || 'Ressource',
          icon: 'devices',
          available: r.available ?? (r.status === 'available' ? (r.quantity ?? 1) : 0),
          total: r.quantity ?? 1,
          location: r.location || '',
          assignments: []
        }));
      },
      error: (err) => {
        console.error('Erreur chargement ressources:', err?.error?.message || err);
      }
    });
  }
  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }
  get filteredResources(): Resource[] {
    if (!this.searchQuery) return this.resources;
    const q = this.searchQuery.toLowerCase();
    return this.resources.filter(r => r.name.toLowerCase().includes(q) || r.category.toLowerCase().includes(q) || r.location.toLowerCase().includes(q));
  }
  setTab(tab: 'inventaire' | 'equipement'): void {
    this.activeTab = tab;
    if (tab === 'equipement') this.loadEquipmentDashboard();
  }

  loadEquipmentDashboard(): void {
    if (this.equipmentDashboard) return;
    this.isLoadingDashboard = true;
    this.resourcesService.getEquipmentDashboard().subscribe({
      next: (data) => { this.equipmentDashboard = data; this.isLoadingDashboard = false; },
      error: () => { this.isLoadingDashboard = false; }
    });
    this.resourcesService.getEquipmentAlertes().subscribe({
      next: (data) => { this.equipmentAlertes = data; }
    });
  }

  getAlerteClass(type: string): string {
    return type === 'PANNE_SANS_INTERVENTION' ? 'alerte-danger' : 'alerte-warning';
  }
  openModal(): void { this.newResource = { name: '', category: '', location: '', total: 1 }; this.isModalOpen = true; }
  closeModal(): void { this.isModalOpen = false; }
  handleAdd(): void {
    this.resourcesService.addResource({
      name: this.newResource.name,
      type: this.newResource.category,
      quantity: this.newResource.total,
      available: this.newResource.total,
      location: this.newResource.location
    }).subscribe({
      next: () => { this.closeModal(); this.loadResources(); },
      error: (err) => alert(err?.error?.message || 'Erreur lors de la création')
    });
  }
  openImportModal(): void {
    this.importStep = 1; this.importFile = null; this.isDragging = false;
    this.isImportLoading = false; this.importParseError = null;
    this.importPreviewRows = []; this.importPreviewFilter = 'all';
    this.isImportModalOpen = true;
  }
  closeImportModal(): void { this.isImportModalOpen = false; }
  onImportDragOver(e: DragEvent): void  { e.preventDefault(); this.isDragging = true; }
  onImportDragLeave(): void             { this.isDragging = false; }
  onImportDrop(e: DragEvent): void      { e.preventDefault(); this.isDragging = false; const f = e.dataTransfer?.files?.[0]; if (f) this.setImportFile(f); }
  onImportFileSelected(e: Event): void  { const f = (e.target as HTMLInputElement).files?.[0]; if (f) this.setImportFile(f); }
  removeImportFile(e: Event): void      { e.stopPropagation(); this.importFile = null; this.importParseError = null; }
  setImportFile(file: File): void {
    const ext = '.' + file.name.split('.').pop()?.toLowerCase();
    if (!['.csv', '.xlsx', '.xls'].includes(ext)) { this.importParseError = 'Format non supporté.'; return; }
    this.importFile = file; this.importParseError = null;
  }
  async parseImportFile(): Promise<void> {
    if (!this.importFile) return;
    this.isImportLoading = true; this.importParseError = null;
    try {
      const text = await this.importFile.text();
      const lines = text.replace(/^\uFEFF/, '').split('\n').filter(l => l.trim());
      if (lines.length < 2) { this.importParseError = 'Fichier vide.'; return; }
      const sep = lines[0].includes(';') ? ';' : ',';
      this.importPreviewRows = lines.slice(1).map(line => {
        const c = line.split(sep).map(x => x.replace(/^"|"$/g, '').trim());
        const errors: string[] = [];
        if (!c[0]) errors.push('Nom manquant'); if (!c[1]) errors.push('Catégorie manquante');
        return { name: c[0] || '', category: c[1] || '', location: c[2] || '', total: parseInt(c[3]) || 1, valid: errors.length === 0, errors };
      }).filter(r => r.name);
      if (!this.importPreviewRows.length) { this.importParseError = 'Aucune donnée valide.'; return; }
      this.importStep = 2;
    } catch { this.importParseError = 'Erreur de lecture.'; } finally { this.isImportLoading = false; }
  }
  get validImportRows()    { return this.importPreviewRows.filter(r => r.valid); }
  get invalidImportRows()  { return this.importPreviewRows.filter(r => !r.valid); }
  get filteredImportRows() {
    if (this.importPreviewFilter === 'valid')   return this.validImportRows;
    if (this.importPreviewFilter === 'invalid') return this.invalidImportRows;
    return this.importPreviewRows;
  }
  confirmImport(): void {
    const calls = this.validImportRows.map(r =>
      this.resourcesService.addResource({
        name: r.name,
        type: r.category,
        quantity: r.total,
        available: r.total,
        location: r.location || 'À définir'
      })
    );
    let done = 0;
    this.importedCount = calls.length;
    calls.forEach(obs => obs.subscribe({
      next: () => { done++; if (done === calls.length) { this.closeImportModal(); this.showImportToast = true; setTimeout(() => this.showImportToast = false, 4000); this.loadResources(); } },
      error: (err) => alert(err?.error?.message || 'Erreur lors de l\'import')
    }));
    if (!calls.length) { this.closeImportModal(); }
  }
  downloadTemplate(): void {
    const csv = '\uFEFF' + '"Nom ressource";"Catégorie";"Emplacement";"Quantité totale"\n"Projecteur HD";"Projecteur";"Bâtiment A - Réserve";"8"\n"Laptop Dell";"Informatique";"Lab C - Stockage";"12"';
    const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' })); a.download = 'modele_ressources.csv'; a.click();
  }
  formatSize(b: number): string { if (b < 1024) return `${b} o`; if (b < 1048576) return `${(b/1024).toFixed(1)} Ko`; return `${(b/1048576).toFixed(1)} Mo`; }
  getAssignmentColor(i: number): string { return ['assign-green','assign-blue','assign-purple','assign-orange'][i % 4]; }
  getAvailabilityColor(r: Resource): string { const ratio = r.available / r.total; return ratio >= 0.6 ? 'avail-green' : ratio >= 0.3 ? 'avail-yellow' : 'avail-red'; }
}