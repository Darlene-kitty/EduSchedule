import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { EventsManagementService } from '../../core/services/events-management.service';

export interface AppEvent {
  id: number; title: string; description: string;
  status: 'Confirmé' | 'En attente' | 'Annulé';
  type: 'Conférence' | 'Séminaire' | 'Examen' | 'Événement';
  date: string; time: string; room: string; roomStatus: string;
  attendees: number; maxAttendees: number; fillRate: string;
  organizer: string; organizerLabel: string;
}

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './events.html',
  styleUrl: './events.css'
})
export class EventsComponent implements OnInit {
  private eventsService = inject(EventsManagementService);

  currentDate = ''; currentTime = '';
  searchQuery = '';

  isCreateModalOpen = false;
  isEditModalOpen   = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;
  isImportModalOpen = false;

  editingEvent:  AppEvent | null = null;
  viewingEvent:  AppEvent | null = null;
  eventToDelete: AppEvent | null = null;

  /* Import */
  importStep: 1 | 2 = 1;
  importFile: File | null = null;
  isDragging = false;
  isImportLoading = false;
  importParseError: string | null = null;
  importPreviewRows: { title: string; type: string; date: string; room: string; valid: boolean }[] = [];
  showImportToast = false;
  importedCount = 0;

  events: AppEvent[] = [
    { id: 1, title: 'Conférence Intelligence Artificielle', description: 'Conférence annuelle sur les avancées en IA avec des intervenants internationaux.', status: 'Confirmé', type: 'Conférence', date: '25 Octobre 2025', time: '14:00 - 18:00', room: 'Amphithéâtre A', roomStatus: 'Salle réservée', attendees: 180, maxAttendees: 200, fillRate: '90% de remplissage', organizer: 'Département Informatique', organizerLabel: 'Organisateur' },
    { id: 2, title: 'Séminaire de recherche - Physique Quantique', description: 'Présentation des dernières recherches en mécanique quantique.', status: 'Confirmé', type: 'Séminaire', date: '24 Octobre 2025', time: '10:00 - 12:00', room: 'Salle B203', roomStatus: 'Salle réservée', attendees: 35, maxAttendees: 40, fillRate: '88% de remplissage', organizer: 'Prof. Sophie Bernard', organizerLabel: 'Organisateur' },
    { id: 3, title: 'Examen de Mathématiques L1', description: 'Examen final du semestre pour les étudiants de première année.', status: 'Confirmé', type: 'Examen', date: '28 Octobre 2025', time: '08:00 - 10:00', room: 'Amphi B', roomStatus: 'Salle réservée', attendees: 150, maxAttendees: 180, fillRate: '83% de remplissage', organizer: 'Dr. Martin Dupont', organizerLabel: 'Organisateur' },
    { id: 4, title: 'Journée Portes Ouvertes', description: "Présentation de l'université aux futurs étudiants et leurs familles.", status: 'En attente', type: 'Événement', date: '02 Novembre 2025', time: '09:00 - 17:00', room: 'Tout le campus', roomStatus: 'En cours de planification', attendees: 0, maxAttendees: 500, fillRate: '0% de remplissage', organizer: 'Direction', organizerLabel: 'Organisateur' },
  ];

  newEvent      = { title: '', description: '', type: 'Conférence' as AppEvent['type'], status: 'Confirmé' as AppEvent['status'], date: '', startTime: '', endTime: '', room: '', organizer: '', maxAttendees: 50 };
  editEventData = { title: '', description: '', type: 'Conférence' as AppEvent['type'], status: 'Confirmé' as AppEvent['status'], date: '', startTime: '', endTime: '', room: '', organizer: '', maxAttendees: 50 };

  ngOnInit(): void { 
    this.updateDateTime(); 
    setInterval(() => this.updateDateTime(), 1000);
    this.loadEvents();
  }

  private loadEvents(): void {
    this.eventsService.getEvents().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.events = data.map(e => ({
            id: e.id,
            title: e.title,
            description: e.description,
            status: 'Confirmé' as AppEvent['status'],
            type: 'Événement' as AppEvent['type'],
            date: e.date ? new Date(e.date).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }) : 'À définir',
            time: e.startTime && e.endTime ? `${e.startTime} - ${e.endTime}` : 'À définir',
            room: e.location || 'À définir',
            roomStatus: 'Salle réservée',
            attendees: e.participants || 0,
            maxAttendees: e.participants || 0,
            fillRate: '0% de remplissage',
            organizer: e.organizer || 'À définir',
            organizerLabel: 'Organisateur'
          }));
        }
        // garde les données démo si backend vide
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

  get filteredEvents(): AppEvent[] {
    if (!this.searchQuery) return this.events;
    const q = this.searchQuery.toLowerCase();
    return this.events.filter(e => e.title.toLowerCase().includes(q) || e.type.toLowerCase().includes(q));
  }

  /* ── CRÉER ── */
  openCreateModal(): void {
    this.newEvent = { title: '', description: '', type: 'Conférence', status: 'Confirmé', date: '', startTime: '', endTime: '', room: '', organizer: '', maxAttendees: 50 };
    this.isCreateModalOpen = true;
  }
  closeCreateModal(): void { this.isCreateModalOpen = false; }
  handleAdd(): void {
    if (!this.newEvent.title.trim()) return;
    const time = this.newEvent.startTime && this.newEvent.endTime ? `${this.newEvent.startTime} - ${this.newEvent.endTime}` : 'À définir';
    const dateLabel = this.newEvent.date ? new Date(this.newEvent.date).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }) : 'Date à définir';
    this.events = [...this.events, { id: Date.now(), title: this.newEvent.title, description: this.newEvent.description, status: this.newEvent.status, type: this.newEvent.type, date: dateLabel, time, room: this.newEvent.room || 'Salle à définir', roomStatus: 'En attente', attendees: 0, maxAttendees: this.newEvent.maxAttendees, fillRate: '0% de remplissage', organizer: this.newEvent.organizer || 'À définir', organizerLabel: 'Organisateur' }];
    this.closeCreateModal();
  }

  /* ── VOIR ── */
  openViewModal(event: AppEvent): void { this.viewingEvent = event; this.isViewModalOpen = true; }
  closeViewModal(): void               { this.isViewModalOpen = false; this.viewingEvent = null; }

  /* ── MODIFIER ── */
  openEditModal(event: AppEvent): void {
    this.editingEvent = event;
    const parts = event.time.split(' - ');
    this.editEventData = { title: event.title, description: event.description, type: event.type, status: event.status, date: '', startTime: parts[0] || '', endTime: parts[1] || '', room: event.room, organizer: event.organizer, maxAttendees: event.maxAttendees };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingEvent = null; }
  saveEdit(): void {
    if (!this.editingEvent) return;
    const time = this.editEventData.startTime && this.editEventData.endTime ? `${this.editEventData.startTime} - ${this.editEventData.endTime}` : this.editingEvent.time;
    const dateLabel = this.editEventData.date ? new Date(this.editEventData.date).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }) : this.editingEvent.date;
    this.events = this.events.map(e => e.id === this.editingEvent!.id ? { ...e, title: this.editEventData.title, description: this.editEventData.description, type: this.editEventData.type, status: this.editEventData.status, date: dateLabel, time, room: this.editEventData.room, organizer: this.editEventData.organizer, maxAttendees: this.editEventData.maxAttendees } : e);
    this.closeEditModal();
  }

  /* ── SUPPRIMER ── */
  openDeleteModal(event: AppEvent): void { this.eventToDelete = event; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void               { this.isDeleteModalOpen = false; this.eventToDelete = null; }
  confirmDelete(): void {
    if (!this.eventToDelete) return;
    this.events = this.events.filter(e => e.id !== this.eventToDelete!.id);
    this.closeDeleteModal();
  }

  /* ── IMPORT ── */
  openImportModal(): void {
    this.importStep = 1; this.importFile = null; this.isDragging = false;
    this.isImportLoading = false; this.importParseError = null; this.importPreviewRows = [];
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
    if (!['.csv', '.xlsx', '.xls'].includes(ext)) { this.importParseError = 'Format non supporté. Utilisez CSV ou XLSX.'; return; }
    this.importFile = file; this.importParseError = null;
  }
  async parseImportFile(): Promise<void> {
    if (!this.importFile) return;
    this.isImportLoading = true;
    try {
      const text = await this.importFile.text();
      const lines = text.replace(/^\uFEFF/, '').split('\n').filter(l => l.trim());
      const sep = lines[0].includes(';') ? ';' : ',';
      this.importPreviewRows = lines.slice(1).map(line => { const c = line.split(sep).map(x => x.replace(/^"|"$/g, '').trim()); return { title: c[0] || '', type: c[1] || '', date: c[2] || '', room: c[3] || '', valid: !!(c[0] && c[1]) }; }).filter(r => r.title);
      if (!this.importPreviewRows.length) { this.importParseError = 'Aucune donnée valide trouvée.'; return; }
      this.importStep = 2;
    } catch { this.importParseError = 'Erreur de lecture du fichier.'; }
    finally { this.isImportLoading = false; }
  }
  get validImportRows()   { return this.importPreviewRows.filter(r => r.valid); }
  get invalidImportRows() { return this.importPreviewRows.filter(r => !r.valid); }
  confirmImport(): void {
    const valid = this.validImportRows;
    valid.forEach(r => { this.events.push({ id: Date.now() + Math.random(), title: r.title, description: '', status: 'En attente', type: (r.type as AppEvent['type']) || 'Événement', date: r.date || 'À définir', time: 'À définir', room: r.room || 'À définir', roomStatus: 'En attente', attendees: 0, maxAttendees: 0, fillRate: '0% de remplissage', organizer: 'À définir', organizerLabel: 'Organisateur' }); });
    this.importedCount = valid.length;
    this.closeImportModal();
    this.showImportToast = true;
    setTimeout(() => this.showImportToast = false, 4000);
  }
  downloadTemplate(): void {
    const csv = '\uFEFF' + '"Titre";"Type";"Date";"Salle";"Organisateur";"Capacité max"\n"Conférence IA";"Conférence";"2025-11-01";"Amphi A";"Dr. Dupont";"200"\n"Examen Maths";"Examen";"2025-11-15";"Amphi B";"Dr. Martin";"150"';
    const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' })); a.download = 'modele_evenements.csv'; a.click();
  }
  formatSize(b: number): string { if (b < 1024) return `${b} o`; if (b < 1048576) return `${(b/1024).toFixed(1)} Ko`; return `${(b/1048576).toFixed(1)} Mo`; }

  getStatusClass(s: string): string { return s === 'Confirmé' ? 'status-confirmed' : s === 'En attente' ? 'status-pending' : 'status-cancelled'; }
  getTypeClass(t:  string): string  { return t === 'Conférence' ? 'type-conference' : t === 'Séminaire' ? 'type-seminaire' : t === 'Examen' ? 'type-examen' : 'type-event'; }
  getStatusIcon(s: string): string  { return s === 'Confirmé' ? 'check_circle' : s === 'En attente' ? 'hourglass_empty' : 'cancel'; }
  getFillPercent(att: number, max: number): number { return max > 0 ? Math.round((att / max) * 100) : 0; }
}