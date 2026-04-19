import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { EventsManagementService, Event as ApiEvent, EventType, EventStatus } from '../../core/services/events-management.service';
import { RoomsManagementService } from '../../core/services/rooms-management.service';
import { UsersManagementService } from '../../core/services/users-management.service';

export interface AppEvent {
  id: number; title: string; description: string;
  status: string;
  type: string;
  date: string; time: string; room: string; roomStatus: string;
  attendees: number; maxAttendees: number; fillRate: string;
  organizer: string; organizerLabel: string;
  // champs backend bruts
  startDateTime?: string; endDateTime?: string;
  resourceId?: number; organizerId?: number;
  rawType?: EventType;
  rawStatus?: EventStatus;
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
  private roomsSvc      = inject(RoomsManagementService);
  private usersSvc      = inject(UsersManagementService);

  currentDate = ''; currentTime = '';
  searchQuery = '';

  availableRooms: string[] = [];
  availableOrganizers: string[] = [];

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

  events: AppEvent[] = [];

  newEvent      = { title: '', description: '', type: 'CONFERENCE' as EventType, status: 'CONFIRMED' as EventStatus, date: '', startTime: '', endTime: '', room: '', organizer: '', maxAttendees: 50, resourceId: 0, organizerId: 0 };
  editEventData = { title: '', description: '', type: 'CONFERENCE' as EventType, status: 'CONFIRMED' as EventStatus, date: '', startTime: '', endTime: '', room: '', organizer: '', maxAttendees: 50, resourceId: 0, organizerId: 0 };

  ngOnInit(): void { 
    this.updateDateTime(); 
    setInterval(() => this.updateDateTime(), 1000);
    this.loadEvents();
    this.loadRooms();
    this.loadOrganizers();
  }

  private loadRooms(): void {
    this.roomsSvc.getRooms().subscribe({
      next: (rooms: import('../../core/services/rooms-management.service').Room[]) => {
        this.availableRooms = rooms.map(r => r.name);
      },
      error: () => {}
    });
  }

  private loadOrganizers(): void {
    this.usersSvc.getUsers().subscribe({
      next: (users: import('../../core/services/users-management.service').UserManagement[]) => {
        this.availableOrganizers = users
          .map(u => u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || '')
          .filter((n): n is string => !!n).sort();
      },
      error: () => {}
    });
  }

  private loadEvents(): void {
    this.eventsService.getEvents().subscribe({
      next: (data) => {
        this.events = (data ?? []).map(e => ({
          id: e.id,
          title: e.title,
          description: e.description ?? '',
          status: EventsManagementService.statusLabel(e.status ?? 'PLANNED'),
          type: EventsManagementService.typeLabel(e.type),
          date: EventsManagementService.toDate(e.startDateTime),
          time: e.startDateTime && e.endDateTime
            ? `${EventsManagementService.toTime(e.startDateTime)} - ${EventsManagementService.toTime(e.endDateTime)}`
            : 'À définir',
          room: e.resourceName || 'À définir',
          roomStatus: 'Salle réservée',
          attendees: e.currentParticipants ?? 0,
          maxAttendees: e.maxParticipants ?? 0,
          fillRate: e.maxParticipants
            ? `${Math.round(((e.currentParticipants ?? 0) / e.maxParticipants) * 100)}% de remplissage`
            : '0% de remplissage',
          organizer: e.organizerName || 'À définir',
          organizerLabel: 'Organisateur',
          startDateTime: e.startDateTime,
          endDateTime: e.endDateTime,
          resourceId: e.resourceId,
          organizerId: e.organizerId,
          rawType: e.type,
          rawStatus: e.status,
        }));
      },
      error: () => {}
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
    this.newEvent = { title: '', description: '', type: 'CONFERENCE', status: 'CONFIRMED', date: '', startTime: '', endTime: '', room: '', organizer: '', maxAttendees: 50, resourceId: 0, organizerId: 0 };
    this.isCreateModalOpen = true;
  }
  closeCreateModal(): void { this.isCreateModalOpen = false; }
  handleAdd(): void {
    if (!this.newEvent.title.trim()) return;
    const typeMap: Record<string, EventType> = {
      'Conférence': 'CONFERENCE', 'Séminaire': 'SEMINAR', 'Examen': 'EXAM',
      'Compétition': 'COMPETITION', 'Événement': 'OTHER'
    };
    const statusMap: Record<string, EventStatus> = {
      'Confirmé': 'CONFIRMED', 'En attente': 'PLANNED', 'Annulé': 'CANCELLED'
    };
    this.eventsService.addEvent({
      title: this.newEvent.title,
      description: this.newEvent.description,
      type: typeMap[this.newEvent.type as string] ?? this.newEvent.type,
      startDateTime: EventsManagementService.toISO(this.newEvent.date, this.newEvent.startTime),
      endDateTime:   EventsManagementService.toISO(this.newEvent.date, this.newEvent.endTime),
      resourceId:  this.newEvent.resourceId || 1,
      organizerId: this.newEvent.organizerId || 1,
      maxParticipants: this.newEvent.maxAttendees,
    }).subscribe({
      next: () => { this.loadEvents(); this.closeCreateModal(); },
      error: (err: any) => { this.closeCreateModal(); console.error('Erreur création événement:', err); }
    });
  }

  /* ── VOIR ── */
  openViewModal(event: AppEvent): void { this.viewingEvent = event; this.isViewModalOpen = true; }
  closeViewModal(): void               { this.isViewModalOpen = false; this.viewingEvent = null; }

  /* ── MODIFIER ── */
  openEditModal(event: AppEvent): void {
    this.editingEvent = event;
    const startDate = event.startDateTime ? event.startDateTime.split('T')[0] : '';
    const startTime = EventsManagementService.toTime(event.startDateTime ?? '');
    const endTime   = EventsManagementService.toTime(event.endDateTime ?? '');
    this.editEventData = {
      title: event.title, description: event.description,
      type: event.rawType ?? 'CONFERENCE',
      status: event.rawStatus ?? 'CONFIRMED',
      date: startDate, startTime, endTime,
      room: event.room, organizer: event.organizer,
      maxAttendees: event.maxAttendees,
      resourceId: event.resourceId ?? 0,
      organizerId: event.organizerId ?? 0
    };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingEvent = null; }
  saveEdit(): void {
    if (!this.editingEvent) return;
    const id = this.editingEvent.id;
    const typeMap: Record<string, EventType> = {
      'Conférence': 'CONFERENCE', 'Séminaire': 'SEMINAR', 'Examen': 'EXAM',
      'Compétition': 'COMPETITION', 'Événement': 'OTHER'
    };
    const statusMap: Record<string, EventStatus> = {
      'Confirmé': 'CONFIRMED', 'En attente': 'PLANNED', 'Annulé': 'CANCELLED'
    };
    this.eventsService.updateEvent(id, {
      title: this.editEventData.title,
      description: this.editEventData.description,
      type: typeMap[this.editEventData.type as string] ?? this.editEventData.type,
      startDateTime: EventsManagementService.toISO(this.editEventData.date, this.editEventData.startTime),
      endDateTime:   EventsManagementService.toISO(this.editEventData.date, this.editEventData.endTime),
      resourceId:  this.editEventData.resourceId || this.editingEvent.resourceId || 1,
      organizerId: this.editEventData.organizerId || this.editingEvent.organizerId || 1,
      maxParticipants: this.editEventData.maxAttendees,
    }).subscribe({
      next: () => { this.loadEvents(); this.closeEditModal(); },
      error: (err: any) => { this.closeEditModal(); console.error('Erreur modification événement:', err); }
    });
  }

  /* ── SUPPRIMER ── */
  openDeleteModal(event: AppEvent): void { this.eventToDelete = event; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void               { this.isDeleteModalOpen = false; this.eventToDelete = null; }
  confirmDelete(): void {
    if (!this.eventToDelete) return;
    const id = this.eventToDelete.id;
    this.eventsService.deleteEvent(id).subscribe({
      next: () => { this.events = this.events.filter(e => e.id !== id); this.closeDeleteModal(); },
      error: (err: any) => { this.closeDeleteModal(); console.error('Erreur suppression événement:', err); }
    });
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
  onImportFileSelected(e: globalThis.Event): void  { const f = (e.target as HTMLInputElement).files?.[0]; if (f) this.setImportFile(f); }
  removeImportFile(e: globalThis.Event): void      { e.stopPropagation(); this.importFile = null; this.importParseError = null; }
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
    valid.forEach(r => {
      this.eventsService.addEvent({
        title: r.title,
        description: '',
        type: 'OTHER',
        startDateTime: EventsManagementService.toISO(r.date, '09:00'),
        endDateTime:   EventsManagementService.toISO(r.date, '11:00'),
        resourceId: 1,
        organizerId: 1,
      }).subscribe();
    });
    this.importedCount = valid.length;
    this.closeImportModal();
    this.showImportToast = true;
    setTimeout(() => { this.showImportToast = false; this.loadEvents(); }, 4000);
  }
  downloadTemplate(): void {
    const csv = '\uFEFF' + '"Titre";"Type";"Date";"Salle";"Organisateur";"Capacité max"\n"Conférence IA";"Conférence";"2025-11-01";"Amphi A";"Dr. Dupont";"200"\n"Examen Maths";"Examen";"2025-11-15";"Amphi B";"Dr. Martin";"150"';
    const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' })); a.download = 'modele_evenements.csv'; a.click();
  }
  formatSize(b: number): string { if (b < 1024) return `${b} o`; if (b < 1048576) return `${(b/1024).toFixed(1)} Ko`; return `${(b/1048576).toFixed(1)} Mo`; }

  getStatusClass(s: string): string { return s === 'Confirmé' ? 'status-confirmed' : s === 'En attente' ? 'status-pending' : 'status-cancelled'; }
  getTypeClass(t:  string): string  { return t === 'Conférence' ? 'type-conference' : t === 'Séminaire' ? 'type-seminaire' : t === 'Examen' ? 'type-examen' : t === 'Compétition' ? 'type-competition' : 'type-event'; }
  getStatusIcon(s: string): string  { return s === 'Confirmé' ? 'check_circle' : s === 'En attente' ? 'hourglass_empty' : 'cancel'; }
  getFillPercent(att: number, max: number): number { return max > 0 ? Math.round((att / max) * 100) : 0; }
}