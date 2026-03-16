import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

export interface Student {
  id: number;
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  filiere: string;
  niveau: string;
  classe: string;
  dateNaissance: string;
  enabled: boolean;
}

export interface ImportedStudent {
  matricule: string; nom: string; prenom: string; email: string;
  telephone: string; filiere: string; niveau: string; classe: string;
  dateNaissance: string; enabled: boolean;
  valid: boolean; errors: string[];
}

export interface ImportResult {
  total: number; valid: number; invalid: number; rows: ImportedStudent[];
}

@Component({
  selector: 'app-students',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './students.html',
  styleUrl: './students.css'
})
export class StudentsComponent implements OnInit {

  searchQuery = '';
  isAddModalOpen    = false;
  isEditModalOpen   = false;
  isImportModalOpen = false;
  isViewModalOpen   = false;
  isDeleteModalOpen = false;

  editingStudent:  Student | null = null;
  viewingStudent:  Student | null = null;
  studentToDelete: Student | null = null;

  currentDate = ''; currentTime = '';
  importSuccessCount = 0; showImportSuccess = false;

  importStep: 1 | 2 = 1;
  importFile: File | null = null;
  isDragging = false;
  isImportLoading = false;
  importParseError: string | null = null;
  importResult: ImportResult | null = null;
  importPreviewFilter: 'all' | 'valid' | 'invalid' = 'all';

  expectedColumns = ['Matricule', 'Nom', 'Prénom', 'Email', 'Téléphone', 'Filière', 'Niveau', 'Classe', 'Date naissance', 'Actif (oui/non)'];

  filieres = ['Informatique', 'Mathématiques', 'Physique', 'Chimie', 'Biologie', 'Économie', 'Droit', 'Lettres'];
  niveaux  = ['L1', 'L2', 'L3', 'M1', 'M2', 'D1', 'D2', 'D3'];
  classes  = ['Groupe A', 'Groupe B', 'Groupe C', 'Groupe D'];

  students: Student[] = [
    { id: 1, matricule: 'ETU-2024-001', nom: 'Dupont', prenom: 'Alice', email: 'alice.dupont@univ.fr', telephone: '06 12 34 56 78', filiere: 'Informatique', niveau: 'L3', classe: 'Groupe A', dateNaissance: '2002-05-14', enabled: true },
    { id: 2, matricule: 'ETU-2024-002', nom: 'Martin', prenom: 'Baptiste', email: 'b.martin@univ.fr', telephone: '06 98 76 54 32', filiere: 'Mathématiques', niveau: 'M1', classe: 'Groupe B', dateNaissance: '2001-11-22', enabled: true },
    { id: 3, matricule: 'ETU-2024-003', nom: 'Leroy', prenom: 'Camille', email: 'c.leroy@univ.fr', telephone: '07 11 22 33 44', filiere: 'Physique', niveau: 'L2', classe: 'Groupe A', dateNaissance: '2003-03-08', enabled: false },
  ];

  emptyStudent = () => ({ matricule: '', nom: '', prenom: '', email: '', telephone: '', filiere: '', niveau: '', classe: '', dateNaissance: '', enabled: true });
  newStudent = this.emptyStudent();
  editStudentData = this.emptyStudent();

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

  get filteredStudents(): Student[] {
    if (!this.searchQuery) return this.students;
    const q = this.searchQuery.toLowerCase();
    return this.students.filter(s =>
      s.nom.toLowerCase().includes(q) ||
      s.prenom.toLowerCase().includes(q) ||
      s.email.toLowerCase().includes(q) ||
      s.matricule.toLowerCase().includes(q) ||
      s.filiere.toLowerCase().includes(q) ||
      s.classe.toLowerCase().includes(q)
    );
  }

  getFullName(s: Student): string { return `${s.prenom} ${s.nom}`; }
  getInitials(s: Student): string { return (s.prenom[0] + s.nom[0]).toUpperCase(); }

  getNiveauClass(niveau: string): string {
    if (['L1','L2','L3'].includes(niveau)) return 'badge-licence';
    if (['M1','M2'].includes(niveau))      return 'badge-master';
    return 'badge-doctorat';
  }

  /* ── Voir ── */
  openViewModal(s: Student): void { this.viewingStudent = s; this.isViewModalOpen = true; }
  closeViewModal(): void          { this.isViewModalOpen = false; this.viewingStudent = null; }

  /* ── Ajout ── */
  openAddModal(): void  { this.newStudent = this.emptyStudent(); this.isAddModalOpen = true; }
  closeAddModal(): void { this.isAddModalOpen = false; }
  handleAddStudent(): void {
    const id = this.students.length ? Math.max(...this.students.map(s => s.id)) + 1 : 1;
    this.students = [...this.students, { id, ...this.newStudent }];
    this.closeAddModal();
  }

  /* ── Édition ── */
  openEditModal(s: Student): void { this.editingStudent = s; this.editStudentData = { ...s }; this.isEditModalOpen = true; }
  closeEditModal(): void          { this.isEditModalOpen = false; this.editingStudent = null; }
  handleEditStudent(): void {
    if (!this.editingStudent) return;
    this.students = this.students.map(s => s.id === this.editingStudent!.id ? { ...s, ...this.editStudentData } : s);
    this.closeEditModal();
  }

  /* ── Suppression ── */
  openDeleteModal(s: Student): void { this.studentToDelete = s; this.isDeleteModalOpen = true; }
  closeDeleteModal(): void          { this.isDeleteModalOpen = false; this.studentToDelete = null; }
  confirmDelete(): void {
    if (!this.studentToDelete) return;
    this.students = this.students.filter(s => s.id !== this.studentToDelete!.id);
    this.closeDeleteModal();
  }

  /* ── Import Excel ── */
  openImportModal(): void {
    this.importStep = 1; this.importFile = null; this.isDragging = false;
    this.isImportLoading = false; this.importParseError = null;
    this.importResult = null; this.importPreviewFilter = 'all';
    this.isImportModalOpen = true;
  }
  closeImportModal(): void { this.isImportModalOpen = false; }

  get importFilteredRows(): ImportedStudent[] {
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
    const rows: ImportedStudent[] = [];
    for (let i = 1; i < lines.length; i++) {
      const cells = lines[i].split(sep).map(c => c.replace(/^"|"$/g, '').trim());
      const [matricule, nom, prenom, email, telephone, filiere, niveau, classe, dateNaissance, enabledStr] = cells;
      const errors: string[] = [];
      if (!matricule) errors.push('Matricule manquant');
      if (!nom)       errors.push('Nom manquant');
      if (!prenom)    errors.push('Prénom manquant');
      if (!email)     errors.push('Email manquant');
      else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.push('Email invalide');
      rows.push({
        matricule: matricule || '', nom: nom || '', prenom: prenom || '',
        email: email || '', telephone: telephone || '',
        filiere: filiere || '', niveau: niveau || '', classe: classe || '',
        dateNaissance: dateNaissance || '',
        enabled: ['oui','yes','true','1'].includes((enabledStr || 'oui').toLowerCase()),
        valid: errors.length === 0, errors
      });
    }
    return { total: rows.length, valid: rows.filter(r => r.valid).length, invalid: rows.filter(r => !r.valid).length, rows };
  }

  confirmImport(): void {
    if (!this.importResult) return;
    const validRows = this.importResult.rows.filter(r => r.valid);
    let nextId = this.students.length ? Math.max(...this.students.map(s => s.id)) + 1 : 1;
    const newStudents: Student[] = validRows.map(r => ({
      id: nextId++, matricule: r.matricule, nom: r.nom, prenom: r.prenom,
      email: r.email, telephone: r.telephone, filiere: r.filiere,
      niveau: r.niveau, classe: r.classe, dateNaissance: r.dateNaissance, enabled: r.enabled
    }));
    this.students = [...this.students, ...newStudents];
    this.importSuccessCount = validRows.length;
    this.showImportSuccess = true;
    setTimeout(() => this.showImportSuccess = false, 4000);
    this.closeImportModal();
  }

  downloadTemplate(): void {
    const BOM = '\uFEFF';
    const csv = BOM + [
      '"Matricule";"Nom";"Prénom";"Email";"Téléphone";"Filière";"Niveau";"Classe";"Date naissance";"Actif (oui/non)"',
      '"ETU-2024-001";"Dupont";"Alice";"alice.dupont@univ.fr";"06 12 34 56 78";"Informatique";"L3";"Groupe A";"2002-05-14";"oui"',
      '"ETU-2024-002";"Martin";"Baptiste";"b.martin@univ.fr";"06 98 76 54 32";"Mathématiques";"M1";"Groupe B";"2001-11-22";"oui"',
    ].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const a = document.createElement('a'); a.href = URL.createObjectURL(blob);
    a.download = 'modele_etudiants.csv'; a.click();
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} o`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
    return `${(bytes / 1024 / 1024).toFixed(1)} Mo`;
  }
}
