import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

export interface Room {
  id: number; name: string; building: string; capacity: number;
  type: string; equipments: string[];
  available: boolean; currentCourse?: string; currentTime?: string; nextReservation?: string;
}
export interface ImportedRoom { name: string; building: string; capacity: number; type: string; valid: boolean; errors: string[]; }

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './reservations.html',
  styleUrl: './reservations.css'
})
export class ReservationsComponent implements OnInit {
  searchQuery = ''; currentDate = ''; currentTime = '';
  isModalOpen = false;
  isImportModalOpen = false;
  importStep: 1 | 2 = 1;
  importFile: File | null = null;
  isDragging = false;
  isImportLoading = false;
  importParseError: string | null = null;
  importPreviewRows: ImportedRoom[] = [];
  showImportToast = false;
  importedCount = 0;
  importPreviewFilter: 'all' | 'valid' | 'invalid' = 'all';
  importColumns = ['Nom salle', 'Bâtiment', 'Capacité', 'Type'];

  newReservation = { roomId: '', date: '', startTime: '', endTime: '', purpose: '', type: 'Cours magistral' };

  rooms: Room[] = [
    { id: 1, name: 'Salle A101', building: 'Bâtiment A', capacity: 50, type: 'Cours magistral', equipments: ['Projecteur', 'Wifi', 'Tableau blanc'], available: true, nextReservation: '14:00 - Mathématiques' },
    { id: 2, name: 'Salle A102', building: 'Bâtiment A', capacity: 45, type: 'Cours magistral', equipments: ['Projecteur', 'Wifi'],                  available: false, currentCourse: 'Physique',      currentTime: '10:00-12:00' },
    { id: 3, name: 'Salle B203', building: 'Bâtiment B', capacity: 30, type: 'TD',              equipments: ['Tableau blanc', 'Wifi'],               available: true,  nextReservation: '16:00 - Chimie' },
    { id: 4, name: 'Lab C305',   building: 'Bâtiment C', capacity: 40, type: 'TP',              equipments: ['Projecteur', 'Wifi'],                  available: true },
    { id: 5, name: 'Lab D104',   building: 'Bâtiment D', capacity: 35, type: 'TP',              equipments: ['Projecteur'],                          available: false, currentCourse: 'Informatique', currentTime: '09:00-11:00' },
    { id: 6, name: 'Amphithéâtre A', building: 'Bâtiment Principal', capacity: 100, type: 'CM', equipments: ['Projecteur', 'Wifi', 'Microphone'],   available: true,  nextReservation: '10:00 - Conférence' },
  ];

  get validImportCount(): number {
  return this.validImportRows?.length || 0;
}

  ngOnInit(): void { this.updateDateTime(); setInterval(() => this.updateDateTime(), 1000); }
  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }
  get filteredRooms(): Room[] {
    if (!this.searchQuery) return this.rooms;
    const q = this.searchQuery.toLowerCase();
    return this.rooms.filter(r => r.name.toLowerCase().includes(q) || r.building.toLowerCase().includes(q) || r.type.toLowerCase().includes(q));
  }
  openModal(): void { this.newReservation = { roomId: '', date: '', startTime: '', endTime: '', purpose: '', type: 'Cours magistral' }; this.isModalOpen = true; }
  closeModal(): void { this.isModalOpen = false; }
  handleReserve(): void { this.closeModal(); }
  reserveRoom(room: Room): void { this.newReservation.roomId = room.id.toString(); this.isModalOpen = true; }

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
        if (!c[0]) errors.push('Nom manquant'); if (!c[1]) errors.push('Bâtiment manquant');
        const cap = parseInt(c[2]);
        if (isNaN(cap) || cap <= 0) errors.push('Capacité invalide');
        return { name: c[0] || '', building: c[1] || '', capacity: cap || 0, type: c[3] || 'Cours magistral', valid: errors.length === 0, errors };
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
    this.validImportRows.forEach(r => { this.rooms.push({ id: Date.now() + Math.random(), name: r.name, building: r.building, capacity: r.capacity, type: r.type, equipments: [], available: true }); });
    this.importedCount = this.validImportRows.length;
    this.closeImportModal(); this.showImportToast = true;
    setTimeout(() => this.showImportToast = false, 4000);
  }
  downloadTemplate(): void {
    const csv = '\uFEFF' + '"Nom salle";"Bâtiment";"Capacité";"Type"\n"Salle E101";"Bâtiment E";"35";"Cours magistral"\n"Lab E201";"Bâtiment E";"20";"TP"';
    const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' })); a.download = 'modele_salles.csv'; a.click();
  }
  formatSize(b: number): string { if (b < 1024) return `${b} o`; if (b < 1048576) return `${(b/1024).toFixed(1)} Ko`; return `${(b/1048576).toFixed(1)} Mo`; }
  getEquipmentIcon(e: string): string {
    switch (e.toLowerCase()) { case 'projecteur': return 'monitor'; case 'wifi': return 'wifi'; case 'tableau blanc': return 'crop_landscape'; case 'microphone': return 'mic'; default: return 'devices'; }
  }
  getTypeClass(type: string): string {
    switch (type) { case 'Cours magistral': return 'type-cm'; case 'TD': return 'type-td'; case 'TP': return 'type-tp'; default: return 'type-other'; }
  }
}