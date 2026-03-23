import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { RoomsManagementService, Room } from '../../core/services/rooms-management.service';

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
  private roomsService = inject(RoomsManagementService);
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

  resources: Resource[] = [
    { id: 1, name: 'Projecteur HD',       category: 'Projecteur',   icon: 'monitor',  available: 5, total: 8,  location: 'Bâtiment A - Réserve', assignments: [{ room: 'A101', course: 'Mathématiques' }, { room: 'A102', course: 'Physique' }, { room: 'B203', course: 'Chimie' }] },
    { id: 2, name: 'Ordinateur portable', category: 'Informatique', icon: 'laptop',   available: 8, total: 12, location: 'Lab C - Stockage',      assignments: [{ room: 'Lab C305', course: 'TP Informatique' }, { room: 'Amphi A', course: 'Conférence' }] },
    { id: 3, name: 'Microphone sans fil', category: 'Audio',        icon: 'mic',      available: 4, total: 6,  location: 'Amphi - Régie',         assignments: [{ room: 'Amphi A', course: 'Séminaire' }, { room: 'Amphi B', course: 'Conférence' }] },
    { id: 4, name: 'Tableau interactif',  category: 'Affichage',    icon: 'tv',       available: 3, total: 5,  location: 'Bâtiment B - Réserve', assignments: [{ room: 'B101', course: 'TD Maths' }, { room: 'B102', course: 'TD Physique' }] },
    { id: 5, name: 'Caméra de cours',     category: 'Vidéo',        icon: 'videocam', available: 2, total: 4,  location: 'Réunion - Stockage',    assignments: [{ room: 'Amphi A', course: 'Cours en ligne' }] },
    { id: 6, name: 'Enceinte Bluetooth',  category: 'Audio',        icon: 'speaker',  available: 6, total: 8,  location: 'Bâtiment C - Réserve', assignments: [{ room: 'C201', course: 'Événement' }, { room: 'C202', course: 'Réunion' }] },
  ];

  newResource = { name: '', category: '', location: '', total: 1 };

  ngOnInit(): void { 
    this.updateDateTime(); 
    setInterval(() => this.updateDateTime(), 1000);
    this.loadResources();
  }

  private loadResources(): void {
    this.roomsService.getRooms().subscribe({
      next: (rooms) => {
        if (rooms && rooms.length > 0) {
          this.resources = rooms.map(r => ({
            id: r.id,
            name: r.name,
            category: r.type || 'Salle',
            icon: 'meeting_room',
            available: r.status === 'available' ? 1 : 0,
            total: 1,
            location: r.building || '',
            assignments: []
          }));
        }
      },
      error: () => { /* garde les données démo */ }
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
  setTab(tab: 'inventaire' | 'equipement'): void { this.activeTab = tab; }
  openModal(): void { this.newResource = { name: '', category: '', location: '', total: 1 }; this.isModalOpen = true; }
  closeModal(): void { this.isModalOpen = false; }
  handleAdd(): void {
    this.resources = [...this.resources, { id: Date.now(), name: this.newResource.name, category: this.newResource.category, icon: 'devices', available: this.newResource.total, total: this.newResource.total, location: this.newResource.location, assignments: [] }];
    this.closeModal();
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
    this.validImportRows.forEach(r => { this.resources.push({ id: Date.now() + Math.random(), name: r.name, category: r.category, icon: 'devices', available: r.total, total: r.total, location: r.location || 'À définir', assignments: [] }); });
    this.importedCount = this.validImportRows.length;
    this.closeImportModal(); this.showImportToast = true;
    setTimeout(() => this.showImportToast = false, 4000);
  }
  downloadTemplate(): void {
    const csv = '\uFEFF' + '"Nom ressource";"Catégorie";"Emplacement";"Quantité totale"\n"Projecteur HD";"Projecteur";"Bâtiment A - Réserve";"8"\n"Laptop Dell";"Informatique";"Lab C - Stockage";"12"';
    const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' })); a.download = 'modele_ressources.csv'; a.click();
  }
  formatSize(b: number): string { if (b < 1024) return `${b} o`; if (b < 1048576) return `${(b/1024).toFixed(1)} Ko`; return `${(b/1048576).toFixed(1)} Mo`; }
  getAssignmentColor(i: number): string { return ['assign-green','assign-blue','assign-purple','assign-orange'][i % 4]; }
  getAvailabilityColor(r: Resource): string { const ratio = r.available / r.total; return ratio >= 0.6 ? 'avail-green' : ratio >= 0.3 ? 'avail-yellow' : 'avail-red'; }
}