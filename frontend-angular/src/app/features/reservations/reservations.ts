import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { Subscription } from 'rxjs';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { RoomsManagementService } from '../../core/services/rooms-management.service';
import { ReservationsManagementService, Reservation } from '../../core/services/reservations-management.service';
import { RoomSuggestionService, RoomSuggestion, SuggestionCriteria } from '../../core/services/room-suggestion.service';
import { AuthService } from '../../core/services/auth.service';
import { WebSocketService } from '../../core/services/websocket.service';

export interface Room {
  id: number; name: string; building: string; capacity: number;
  type: string; equipments: string[];
  available: boolean; currentCourse?: string;
}
export interface ImportedRoom {
  name: string; building: string; capacity: number; type: string;
  valid: boolean; errors: string[];
}

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './reservations.html',
  styleUrl: './reservations.css'
})
export class ReservationsComponent implements OnInit, OnDestroy {
  private roomsSvc        = inject(RoomsManagementService);
  private reservationsSvc = inject(ReservationsManagementService);
  private suggestionSvc   = inject(RoomSuggestionService);
  private authService     = inject(AuthService);
  private wsSvc           = inject(WebSocketService);

  private wsSub?: Subscription;

  isAdmin = false;
  searchQuery = ''; currentDate = ''; currentTime = '';
  activeTab: 'rooms' | 'pending' | 'all' = 'rooms';

  // ── Réservation modal ──────────────────────────────────────────────────────
  isModalOpen = false;
  newReservation = { roomId: '', date: '', startTime: '', endTime: '', purpose: '', type: 'COURSE', attendees: 20 };

  // ── Suggestion automatique ─────────────────────────────────────────────────
  isSuggesting = false;
  suggestions: RoomSuggestion[] = [];
  showSuggestions = false;

  // ── Approbation ────────────────────────────────────────────────────────────
  isApproveModalOpen = false;
  isRejectModalOpen  = false;
  selectedReservation: Reservation | null = null;
  rejectReason = '';
  isProcessing = false;

  // ── Toast ──────────────────────────────────────────────────────────────────
  showToast = false; toastMessage = ''; toastType: 'success' | 'error' = 'success';

  // ── Import ─────────────────────────────────────────────────────────────────
  isImportModalOpen = false; importStep: 1 | 2 = 1;
  importFile: File | null = null; isDragging = false; isImportLoading = false;
  importParseError: string | null = null; importPreviewRows: ImportedRoom[] = [];
  importPreviewFilter: 'all' | 'valid' | 'invalid' = 'all';
  importColumns = ['Nom salle', 'Bâtiment', 'Capacité', 'Type'];

  rooms: Room[] = [];
  reservations: Reservation[] = [];

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    const user = this.authService.getUser();
    this.isAdmin = (user?.role || '').toUpperCase().includes('ADMIN');
    this.loadRooms();
    this.loadReservations();

    // WebSocket — rafraîchit la liste des pending en temps réel
    this.wsSvc.connect();
    this.wsSub = this.wsSvc.reservationEvents$.subscribe(event => {
      // Toute mutation de réservation → recharger la liste
      this.loadReservations();
      if (event.event === 'created' && this.isAdmin) {
        this.toast('Nouvelle réservation en attente d\'approbation.', 'success');
        this.activeTab = 'pending';
      }
    });
  }

  ngOnDestroy(): void { this.wsSub?.unsubscribe(); }

  // ── Data ───────────────────────────────────────────────────────────────────

  private loadRooms(): void {
    this.roomsSvc.getRooms().subscribe(list => {
      this.rooms = list.map(r => ({
        id: r.id, name: r.name, building: r.building ?? '',
        capacity: r.capacity, type: r.type, equipments: r.equipment || [],
        available: r.status === 'available',
        currentCourse: r.status === 'occupied' ? 'Cours en cours' : undefined,
      }));
    });
  }

  private loadReservations(): void {
    this.reservationsSvc.getReservations().subscribe(list => { this.reservations = list; });
  }

  // ── Suggestion ─────────────────────────────────────────────────────────────

  suggestRooms(): void {
    if (!this.newReservation.date || !this.newReservation.startTime || !this.newReservation.endTime) {
      this.toast('Renseignez la date et les horaires d\'abord.', 'error'); return;
    }
    this.isSuggesting = true; this.showSuggestions = false;
    const criteria: SuggestionCriteria = {
      startTime: `${this.newReservation.date}T${this.newReservation.startTime}:00`,
      endTime:   `${this.newReservation.date}T${this.newReservation.endTime}:00`,
      minCapacity: this.newReservation.attendees || 1,
      courseType: this.newReservation.type, maxSuggestions: 5
    };
    this.suggestionSvc.suggest(criteria).subscribe({
      next: list => { this.suggestions = list; this.showSuggestions = true; this.isSuggesting = false; },
      error: ()  => { this.isSuggesting = false; this.toast('Impossible de récupérer les suggestions.', 'error'); }
    });
  }

  selectSuggestion(s: RoomSuggestion): void {
    this.newReservation.roomId = s.resourceId.toString(); this.showSuggestions = false;
  }

  // ── Réservation ────────────────────────────────────────────────────────────

  openModal(): void {
    this.newReservation = { roomId: '', date: '', startTime: '', endTime: '', purpose: '', type: 'COURSE', attendees: 20 };
    this.suggestions = []; this.showSuggestions = false; this.isModalOpen = true;
  }
  closeModal(): void { this.isModalOpen = false; }
  reserveRoom(room: Room): void { this.newReservation.roomId = room.id.toString(); this.isModalOpen = true; }

  handleReserve(): void {
    if (!this.newReservation.roomId || !this.newReservation.date || !this.newReservation.startTime || !this.newReservation.endTime) {
      this.toast('Veuillez remplir tous les champs obligatoires.', 'error'); return;
    }
    const room = this.rooms.find(r => r.id.toString() === this.newReservation.roomId);
    if (!room) return;
    const userId = this.authService.getUser()?.id || 1;
    this.reservationsSvc.addReservation({
      resourceId: room.id,
      userId,
      title: this.newReservation.purpose || this.newReservation.type || 'Réservation',
      notes: this.newReservation.purpose,
      startTime: `${this.newReservation.date}T${this.newReservation.startTime}:00`,
      endTime:   `${this.newReservation.date}T${this.newReservation.endTime}:00`,
      type: this.newReservation.type as any,
      status: 'PENDING',
      expectedAttendees: this.newReservation.attendees,
    }).subscribe({
      next: () => { this.closeModal(); this.loadReservations(); this.toast('Réservation soumise, en attente d\'approbation.'); },
      error: () => this.toast('Erreur lors de la réservation.', 'error')
    });
  }

  // ── Approbation ────────────────────────────────────────────────────────────

  get pendingReservations(): Reservation[] {
    return this.reservations.filter(r => (r.status || '').toUpperCase() === 'PENDING');
  }

  openApproveModal(r: Reservation): void { this.selectedReservation = r; this.isApproveModalOpen = true; }
  closeApproveModal(): void { this.isApproveModalOpen = false; this.selectedReservation = null; }
  openRejectModal(r: Reservation): void { this.selectedReservation = r; this.rejectReason = ''; this.isRejectModalOpen = true; }
  closeRejectModal(): void { this.isRejectModalOpen = false; this.selectedReservation = null; }

  confirmApprove(): void {
    if (!this.selectedReservation) return;
    this.isProcessing = true;
    const userId = this.authService.getUser()?.id || 1;
    this.reservationsSvc.approveReservation(this.selectedReservation.id, userId).subscribe({
      next: () => { this.isProcessing = false; this.closeApproveModal(); this.loadReservations(); this.toast('Réservation approuvée. Notification envoyée.'); },
      error: () => { this.isProcessing = false; this.toast('Erreur lors de l\'approbation.', 'error'); }
    });
  }

  confirmReject(): void {
    if (!this.selectedReservation) return;
    this.isProcessing = true;
    const userId = this.authService.getUser()?.id || 1;
    this.reservationsSvc.rejectReservation(this.selectedReservation.id, userId, this.rejectReason).subscribe({
      next: () => { this.isProcessing = false; this.closeRejectModal(); this.loadReservations(); this.toast('Réservation refusée. Notification envoyée.'); },
      error: () => { this.isProcessing = false; this.toast('Erreur lors du refus.', 'error'); }
    });
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  get filteredRooms(): Room[] {
    if (!this.searchQuery) return this.rooms;
    const q = this.searchQuery.toLowerCase();
    return this.rooms.filter(r => r.name.toLowerCase().includes(q) || r.building.toLowerCase().includes(q));
  }

  getStatusClass(s: string): string {
    switch ((s || '').toUpperCase()) {
      case 'CONFIRMED': case 'APPROVED': return 'status-confirmed';
      case 'PENDING':   return 'status-pending';
      default:          return 'status-rejected';
    }
  }

  getStatusLabel(s: string): string {
    switch ((s || '').toUpperCase()) {
      case 'CONFIRMED': case 'APPROVED': return 'Confirmée';
      case 'PENDING':   return 'En attente';
      case 'REJECTED':  return 'Refusée';
      case 'CANCELLED': return 'Annulée';
      default: return s;
    }
  }

  getScoreColor(score: number): string {
    return score >= 80 ? '#15803D' : score >= 60 ? '#EA580C' : '#DC2626';
  }

  toast(msg: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage = msg; this.toastType = type; this.showToast = true;
    setTimeout(() => this.showToast = false, 4000);
  }

  getEquipmentIcon(e: string): string {
    switch (e.toLowerCase()) { case 'projecteur': return 'monitor'; case 'wifi': return 'wifi'; default: return 'devices'; }
  }

  getTypeClass(t: string): string {
    switch (t) { case 'COURSE': return 'type-cm'; case 'TD': return 'type-td'; case 'TP': return 'type-tp'; default: return 'type-other'; }
  }

  // ── Import ─────────────────────────────────────────────────────────────────

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
        if (!c[0]) errors.push('Nom manquant');
        if (!c[1]) errors.push('Bâtiment manquant');
        const cap = parseInt(c[2]);
        if (isNaN(cap) || cap <= 0) errors.push('Capacité invalide');
        return { name: c[0] || '', building: c[1] || '', capacity: cap || 0, type: c[3] || 'COURSE', valid: errors.length === 0, errors };
      }).filter(r => r.name);
      if (!this.importPreviewRows.length) { this.importParseError = 'Aucune donnée valide.'; return; }
      this.importStep = 2;
    } catch { this.importParseError = 'Erreur de lecture.'; }
    finally { this.isImportLoading = false; }
  }

  get validImportRows()    { return this.importPreviewRows.filter(r => r.valid); }
  get invalidImportRows()  { return this.importPreviewRows.filter(r => !r.valid); }
  get filteredImportRows() {
    if (this.importPreviewFilter === 'valid')   return this.validImportRows;
    if (this.importPreviewFilter === 'invalid') return this.invalidImportRows;
    return this.importPreviewRows;
  }
  get validImportCount(): number { return this.validImportRows.length; }

  confirmImport(): void {
    this.validImportRows.forEach(r =>
      this.roomsSvc.addRoom({ name: r.name, building: r.building, capacity: r.capacity, type: r.type, equipment: [], status: 'available' }, r.name).subscribe()
    );
    this.closeImportModal();
    this.toast(`${this.validImportRows.length} salle(s) importée(s).`);
    this.loadRooms();
  }

  downloadTemplate(): void {
    const csv = '\uFEFF"Nom salle";"Bâtiment";"Capacité";"Type"\n"Salle E101";"Bâtiment E";"35";"COURSE"';
    const a = document.createElement('a');
    a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
    a.download = 'modele_salles.csv'; a.click();
  }

  formatSize(b: number): string {
    if (b < 1024) return `${b} o`;
    if (b < 1048576) return `${(b / 1024).toFixed(1)} Ko`;
    return `${(b / 1048576).toFixed(1)} Mo`;
  }
}
