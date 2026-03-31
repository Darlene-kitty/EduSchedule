import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { UsersManagementService, UserManagement, SchoolAssignment } from '../../core/services/users-management.service';
import { SchoolManagementService, SchoolEntry } from '../../core/services/school-management.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface User {
  id: number;
  name: string;
  username: string;
  email: string;
  role: 'Enseignant' | 'Administrateur' | 'Étudiant';
  department: string;
  phone: string;
  enabled: boolean;
  primarySchoolId?: number;
  primarySchoolName?: string;
}

export interface ImportedUser {
  username: string; email: string; role: string;
  department: string; phone: string; enabled: boolean;
  valid: boolean; errors: string[];
}

export interface ImportResult {
  total: number; valid: number; invalid: number; rows: ImportedUser[];
}

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './users.html',
  styleUrl: './users.css'
})
export class UsersComponent implements OnInit {
  private usersManagementService = inject(UsersManagementService);
  private schoolService = inject(SchoolManagementService);

  searchQuery = '';
  isAddModalOpen      = false;
  isEditModalOpen     = false;
  isImportModalOpen   = false;
  isViewModalOpen     = false;
  isDeleteModalOpen   = false;
  isSwitchSchoolOpen  = false;

  editingUser:  User | null = null;
  viewingUser:  User | null = null;
  userToDelete: User | null = null;
  switchingUser: User | null = null;

  currentDate = ''; currentTime = '';
  importSuccessCount = 0; showImportSuccess = false;
  showSuccess = false; successMessage = '';

  // Vue d'ensemble utilisateur
  viewUserSchools: SchoolAssignment[] = [];
  viewUserSchoolsLoading = false;

  // Switch école
  selectedSwitchSchoolId: number | null = null;
  availableSchools: SchoolEntry[] = [];

  /* ── Import state ── */
  importStep: 1 | 2 = 1;
  importFile: File | null = null;
  isDragging = false;
  isImportLoading = false;
  importParseError: string | null = null;
  importResult: ImportResult | null = null;
  importPreviewFilter: 'all' | 'valid' | 'invalid' = 'all';

  expectedColumns = ['Nom complet', 'Email', 'Rôle', 'Département', 'Téléphone', 'Actif (oui/non)'];

  users: User[] = [];

  newUser = {
    firstName: '', lastName: '', username: '', email: '', password: '',
    role: 'Enseignant' as User['role'], department: '', phone: '', enabled: true,
    schoolId: null as number | null
  };
  editUserData = {
    name: '', username: '', email: '', role: 'Enseignant' as User['role'],
    department: '', phone: '', enabled: true
  };

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadUsers();
    this.loadSchools();
  }

  private loadUsers(): void {
    this.usersManagementService.getUsers().subscribe({
      next: users => {
        this.users = users.map(u => ({
          id: u.id,
          name: u.name ?? ([u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || ''),
          username: u.username || '',
          email: u.email,
          role: this.mapRole(u.role),
          department: u.department || '',
          phone: u.phone || '',
          enabled: u.enabled ?? u.status === 'active',
          primarySchoolId: u.primarySchoolId,
          primarySchoolName: u.primarySchoolName
        }));
      },
      error: () => {}
    });
  }

  private loadSchools(): void {
    this.schoolService.getAll().subscribe({
      next: schools => { this.availableSchools = schools; },
      error: () => {}
    });
  }

  private mapRole(role: string): 'Enseignant' | 'Administrateur' | 'Étudiant' {
    const r = (role || '').toUpperCase().replace('ROLE_', '');
    if (r === 'ADMIN') return 'Administrateur';
    if (r === 'STUDENT') return 'Étudiant';
    return 'Enseignant';
  }

  private reverseMapRole(role: 'Enseignant' | 'Administrateur' | 'Étudiant'): string {
    if (role === 'Administrateur') return 'ADMIN';
    if (role === 'Étudiant') return 'STUDENT';
    return 'TEACHER';
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  get filteredUsers(): User[] {
    if (!this.searchQuery) return this.users;
    const q = this.searchQuery.toLowerCase();
    return this.users.filter(u =>
      u.name.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      u.username.toLowerCase().includes(q) ||
      u.department.toLowerCase().includes(q)
    );
  }

  toast(msg: string): void {
    this.successMessage = msg;
    this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, 3500);
  }

  /* ── Voir (vue d'ensemble) ── */
  openViewModal(user: User): void {
    this.viewingUser = user;
    this.isViewModalOpen = true;
    this.viewUserSchools = [];
    this.viewUserSchoolsLoading = true;
    this.usersManagementService.getUserSchools(user.id).pipe(catchError(() => of([]))).subscribe(schools => {
      this.viewUserSchools = schools;
      this.viewUserSchoolsLoading = false;
    });
  }
  closeViewModal(): void { this.isViewModalOpen = false; this.viewingUser = null; }

  /* ── Ajout ── */
  openAddModal(): void {
    this.newUser = { firstName: '', lastName: '', username: '', email: '', password: '',
      role: 'Enseignant', department: '', phone: '', enabled: true, schoolId: null };
    this.isAddModalOpen = true;
  }
  closeAddModal(): void { this.isAddModalOpen = false; }

  handleAddUser(): void {
    const username = this.newUser.username || this.newUser.email.split('@')[0].replace(/[^a-zA-Z0-9_-]/g, '_');
    this.usersManagementService.addUser({
      username,
      firstName: this.newUser.firstName,
      lastName: this.newUser.lastName,
      name: `${this.newUser.firstName} ${this.newUser.lastName}`.trim(),
      email: this.newUser.email,
      password: this.newUser.password || 'ChangeMe123!',
      role: this.reverseMapRole(this.newUser.role),
      department: this.newUser.department,
      phone: this.newUser.phone,
      status: this.newUser.enabled ? 'active' : 'inactive'
    }).subscribe({
      next: (created) => {
        // Si une école est sélectionnée, assigner l'utilisateur
        if (this.newUser.schoolId && created.id) {
          const school = this.availableSchools.find(s => s.id === this.newUser.schoolId);
          this.usersManagementService.assignSchool(created.id, this.newUser.schoolId, school?.name || school?.nom || '', true)
            .pipe(catchError(() => of(null))).subscribe();
        }
        this.closeAddModal();
        this.loadUsers();
        this.toast('Utilisateur ajouté avec succès !');
      },
      error: () => this.toast('Erreur lors de l\'ajout de l\'utilisateur.')
    });
  }

  /* ── Édition ── */
  openEditModal(user: User): void {
    this.editingUser = user;
    this.editUserData = { name: user.name, username: user.username, email: user.email,
      role: user.role, department: user.department, phone: user.phone, enabled: user.enabled };
    this.isEditModalOpen = true;
  }
  closeEditModal(): void { this.isEditModalOpen = false; this.editingUser = null; }

  handleEditUser(): void {
    if (!this.editingUser) return;
    const nameParts = this.editUserData.name.trim().split(' ');
    const firstName = nameParts[0] || '';
    const lastName  = nameParts.slice(1).join(' ') || '';
    this.usersManagementService.updateUser(this.editingUser.id, {
      username: this.editUserData.username || undefined,
      firstName, lastName,
      email: this.editUserData.email,
      role: this.reverseMapRole(this.editUserData.role),
      enabled: this.editUserData.enabled
    }).subscribe({
      next: () => { this.closeEditModal(); this.loadUsers(); this.toast('Utilisateur modifié avec succès !'); },
      error: () => this.toast('Erreur lors de la modification.')
    });
  }

  /* ── Switch école ── */
  openSwitchSchoolModal(user: User): void {
    this.switchingUser = user;
    this.selectedSwitchSchoolId = user.primarySchoolId || null;
    this.isSwitchSchoolOpen = true;
  }
  closeSwitchSchoolModal(): void { this.isSwitchSchoolOpen = false; this.switchingUser = null; }

  confirmSwitchSchool(): void {
    if (!this.switchingUser || !this.selectedSwitchSchoolId) return;
    this.usersManagementService.switchSchool(this.switchingUser.id, this.selectedSwitchSchoolId).subscribe({
      next: () => { this.closeSwitchSchoolModal(); this.loadUsers(); this.toast('École permutée avec succès !'); },
      error: () => {
        // Si l'enseignant n'est pas encore assigné, on l'assigne d'abord
        const school = this.availableSchools.find(s => s.id === this.selectedSwitchSchoolId);
        this.usersManagementService.assignSchool(this.switchingUser!.id, this.selectedSwitchSchoolId!, school?.name || school?.nom || '', true)
          .subscribe({
            next: () => { this.closeSwitchSchoolModal(); this.loadUsers(); this.toast('École assignée avec succès !'); },
            error: () => this.toast('Erreur lors de la permutation.')
          });
      }
    });
  }

  /* ── Suppression ── */
  openDeleteModal(user: User): void { this.userToDelete = user; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void          { this.isDeleteModalOpen = false; this.userToDelete = null; }
  confirmDelete(): void {
    if (!this.userToDelete) return;
    this.usersManagementService.deleteUser(this.userToDelete.id).subscribe({
      next: () => { this.closeDeleteModal(); this.loadUsers(); this.toast('Utilisateur supprimé.'); },
      error: () => this.toast('Erreur lors de la suppression.')
    });
  }

  /* ── Import Excel ── */
  openImportModal(): void {
    this.importStep = 1; this.importFile = null; this.isDragging = false;
    this.isImportLoading = false; this.importParseError = null;
    this.importResult = null; this.importPreviewFilter = 'all';
    this.isImportModalOpen = true;
  }
  closeImportModal(): void { this.isImportModalOpen = false; }

  get importFilteredRows(): ImportedUser[] {
    if (!this.importResult) return [];
    if (this.importPreviewFilter === 'valid')   return this.importResult.rows.filter(r => r.valid);
    if (this.importPreviewFilter === 'invalid') return this.importResult.rows.filter(r => !r.valid);
    return this.importResult.rows;
  }

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
    this.isImportLoading = true; this.importParseError = null;
    try {
      const text = await this.importFile.text();
      this.importResult = this.parseCSV(text.replace(/^\uFEFF/, ''));
      this.importStep = 2;
    } catch (err: any) {
      this.importParseError = err.message || 'Erreur lors de l\'analyse du fichier';
    } finally { this.isImportLoading = false; }
  }

  private parseCSV(text: string): ImportResult {
    const lines = text.split('\n').filter(l => l.trim().length > 0);
    if (lines.length < 2) throw new Error('Le fichier est vide ou ne contient pas de données');
    const sep = lines[0].includes(';') ? ';' : ',';
    const rows: ImportedUser[] = [];
    for (let i = 1; i < lines.length; i++) {
      const cells = lines[i].split(sep).map(c => c.replace(/^"|"$/g, '').trim());
      const [name, email, role, department, phone, enabledStr] = cells;
      const errors: string[] = [];
      if (!name)  errors.push('Nom manquant');
      if (!email) errors.push('Email manquant');
      else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.push('Email invalide');
      const validRoles = ['Enseignant', 'Administrateur', 'Étudiant'];
      if (role && !validRoles.includes(role)) errors.push(`Rôle invalide: "${role}"`);
      rows.push({
        username: name || '', email: email || '',
        role: validRoles.includes(role) ? role : 'Enseignant',
        department: department || '', phone: phone || '',
        enabled: ['oui','yes','true','1'].includes((enabledStr || 'oui').toLowerCase()),
        valid: errors.length === 0, errors
      });
    }
    return { total: rows.length, valid: rows.filter(r => r.valid).length, invalid: rows.filter(r => !r.valid).length, rows };
  }

  confirmImport(): void {
    if (!this.importResult) return;
    const validRows = this.importResult.rows.filter(r => r.valid);
    validRows.forEach(u => {
      const nameParts = u.username.trim().split(' ');
      const firstName = nameParts[0] || '';
      const lastName  = nameParts.slice(1).join(' ') || '';
      const username  = u.email.split('@')[0].replace(/[^a-zA-Z0-9_-]/g, '_');
      this.usersManagementService.addUser({
        name: u.username, username, firstName, lastName, email: u.email,
        password: 'ChangeMe123!',
        role: this.reverseMapRole((u.role as User['role']) || 'Enseignant'),
        department: u.department, phone: u.phone,
        status: u.enabled ? 'active' : 'inactive'
      }).subscribe();
    });
    this.importSuccessCount = validRows.length;
    this.showImportSuccess = true;
    setTimeout(() => { this.showImportSuccess = false; this.loadUsers(); }, 4000);
    this.closeImportModal();
  }

  downloadTemplate(): void {
    const BOM = '\uFEFF';
    const csv = BOM + [
      '"Nom complet";"Email";"Rôle";"Département";"Téléphone";"Actif (oui/non)"',
      '"Dr. Martin Dupont";"martin.dupont@iusj.cm";"Enseignant";"Mathématiques";"237 6XX XX XX XX";"oui"',
      '"Admin Jean Moreau";"jean.moreau@iusj.cm";"Administrateur";"Administration";"237 6XX XX XX XX";"oui"',
    ].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const a = document.createElement('a'); a.href = URL.createObjectURL(blob);
    a.download = 'modele_utilisateurs.csv'; a.click();
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} o`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
    return `${(bytes / 1024 / 1024).toFixed(1)} Mo`;
  }

  getRoleClass(role: string): string {
    if (role === 'Administrateur') return 'badge-admin';
    if (role === 'Étudiant')       return 'badge-student';
    return 'badge-teacher';
  }

  getImportRoleClass(role: string): string {
    if (role === 'Administrateur') return 'role-admin';
    if (role === 'Étudiant')       return 'role-student';
    return 'role-teacher';
  }

  getAvatarInitials(name: string): string {
    const parts = name.trim().split(' ');
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return name.substring(0, 2).toUpperCase();
  }
}
