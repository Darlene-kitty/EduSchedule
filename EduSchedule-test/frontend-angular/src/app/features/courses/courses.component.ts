import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { CoursesManagementService, CoursePayload } from '../../core/services/courses-management.service';
import { UsersManagementService } from '../../core/services/users-management.service';

export interface Course {
  id: number;
  name: string;
  code: string;
  level: string;
  type: 'Cours magistral' | 'TD' | 'TP' | 'Séminaire';
  professor: string;
  hours: number;
  students: number;
  groups: string[];
  schoolId?: number;
  teacherId?: number;
  // champs backend
  credits?: number;
  duration?: number;
  department?: string;
  semester?: string;
  description?: string;
}

export interface StudentGroup {
  id: number;
  name: string;
  level: string;
  promotion: string;
  capacity: number;
  enrolled: number;
  courses: string[];
  responsible: string;
}

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './courses.component.html',
  styleUrl: './courses.component.css'
})
export class CoursesComponent implements OnInit {
  private coursesService = inject(CoursesManagementService);
  private usersSvc       = inject(UsersManagementService);

  // Listes pour les selects
  teachers: { id: number; name: string }[] = [];
  readonly departments = ['Informatique','Mathématiques','Physique','Chimie','Biologie','Économie','Droit','Lettres','Sciences Humaines','Génie Civil','Génie Électrique'];
  readonly durations   = [30, 45, 60, 90, 120, 150, 180, 240];
  readonly creditsList = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

  activeTab: 'courses' | 'groups' = 'courses';
  searchQuery = '';
  currentDate = '';
  currentTime = '';

  /* ── Modals ── */
  isCourseModalOpen = false;
  isGroupModalOpen  = false;
  isDeleteModalOpen = false;
  isViewModalOpen   = false;
  isLoading = false;
  editingCourse: Course | null = null;
  editingGroup:  StudentGroup | null = null;
  itemToDelete:  { id: number; name: string; type: 'course' | 'group' } | null = null;
  viewingCourse: Course | null = null;

  courses: Course[] = [];
  groups: StudentGroup[] = [];

  newCourse = { name: '', code: '', level: 'L1', type: 'Cours magistral' as Course['type'], professor: '', hours: 30, students: 0, credits: 3, duration: 90, department: 'Informatique', semester: 'S1', description: '' };
  newGroup  = { name: '', level: 'L1', promotion: 'Licence 1', capacity: 30, responsible: '' };


  /* ── Import ── */
  isImportModalOpen = false;
  importStep: 1 | 2 = 1;
  importFile: File | null = null;
  isDragging = false;
  isImportLoading = false;
  importParseError: string | null = null;
  importPreviewRows: any[] = [];
  showImportToast = false;
  importedCount = 0;
  validImportCount: number = 0;
  importPreviewFilter: 'all' | 'valid' | 'invalid' = 'all';
  importColumns = ['Nom du cours', 'Code', 'Niveau', 'Type', 'Enseignant'];

  ngOnInit(): void {
    this.updateDateTime();
    setInterval(() => this.updateDateTime(), 1000);
    this.loadCourses();
    this.loadTeachers();
  }

  private loadTeachers(): void {
    this.usersSvc.getUsers().subscribe({
      next: users => {
        this.teachers = users
          .filter(u => (u.role || '').toUpperCase().includes('TEACHER'))
          .map(u => ({ id: u.id, name: u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || '' }))
          .filter(t => t.name);
      },
      error: () => {}
    });
  }

  loadCourses(): void {
    this.isLoading = true;
    this.coursesService.getCourses().subscribe({
      next: (data) => {
        this.courses = (data || []).map(c => ({
          id: c.id,
          name: c.name,
          code: c.code,
          level: c.level || '',
          type: (c as any).type || 'Cours magistral',
          professor: c.teacher || c.teacherName || '',
          hours: c.hoursPerWeek || c.hours || 0,
          students: (c as any).maxStudents || 0,
          groups: c.group ? [c.group] : [],
          schoolId: c.schoolId,
          teacherId: c.teacherId,
          credits: c.credits,
          duration: c.duration,
          department: c.department,
          semester: c.semester,
          description: c.description,
        }));
        this.isLoading = false;
      },
      error: () => {
        this.courses = [];
        this.isLoading = false;
      }
    });
  }

  updateDateTime(): void {
    const now = new Date();
    this.currentDate = now.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    this.currentDate = this.currentDate.charAt(0).toUpperCase() + this.currentDate.slice(1);
    this.currentTime = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  setTab(tab: 'courses' | 'groups'): void { this.activeTab = tab; this.searchQuery = ''; }

  get filteredCourses(): Course[] {
    if (!this.searchQuery) return this.courses;
    const q = this.searchQuery.toLowerCase();
    return this.courses.filter(c =>
      c.name.toLowerCase().includes(q) ||
      c.code.toLowerCase().includes(q) ||
      c.professor.toLowerCase().includes(q)
    );
  }

  get filteredGroups(): StudentGroup[] {
    if (!this.searchQuery) return this.groups;
    const q = this.searchQuery.toLowerCase();
    return this.groups.filter(g =>
      g.name.toLowerCase().includes(q) ||
      g.promotion.toLowerCase().includes(q) ||
      g.responsible.toLowerCase().includes(q)
    );
  }

  /* ── Course modal ── */
  openCourseModal(course?: Course): void {
    this.editingCourse = course || null;
    if (course) {
      this.newCourse = {
        name: course.name, code: course.code, level: course.level,
        type: course.type, professor: course.professor, hours: course.hours,
        students: course.students,
        credits: course.credits ?? 3,
        duration: course.duration ?? 90,
        department: course.department ?? 'Informatique',
        semester: course.semester ?? 'S1',
        description: course.description ?? ''
      };
    } else {
      this.newCourse = { name: '', code: '', level: 'L1', type: 'Cours magistral', professor: '', hours: 30, students: 0, credits: 3, duration: 90, department: 'Informatique', semester: 'S1', description: '' };
    }
    this.isCourseModalOpen = true;
  }

  closeCourseModal(): void { this.isCourseModalOpen = false; this.editingCourse = null; }

  saveCourse(): void {
      if (this.editingCourse) {
        const editingId = this.editingCourse.id;
        const snapshot = { ...this.newCourse };
        const payload: Partial<CoursePayload> = {
          name:        snapshot.name,
          code:        snapshot.code,
          level:       snapshot.level,
          semester:    snapshot.semester || 'S1',
          department:  snapshot.department || 'Informatique',
          credits:     snapshot.credits ?? 3,
          duration:    snapshot.duration ?? 90,
          hoursPerWeek: snapshot.hours,
          maxStudents: snapshot.students || undefined,
          description: snapshot.description || undefined,
          schoolId:    this.editingCourse.schoolId,
          teacherId:   this.editingCourse.teacherId
        };
        this.coursesService.updateCourse(editingId, payload).subscribe({
          next: () => { this.courses = this.courses.map(c => c.id === editingId ? { ...c, ...snapshot } : c); this.closeCourseModal(); },
          error: () => { this.courses = this.courses.map(c => c.id === editingId ? { ...c, ...snapshot } : c); this.closeCourseModal(); }
        });
      } else {
        // Valider le format du code avant envoi (pattern backend: 2-4 lettres majuscules + 2-3 chiffres)
        const codePattern = /^[A-Z]{2,4}[0-9]{2,3}$/;
        const code = this.newCourse.code.toUpperCase().trim();
        if (!codePattern.test(code)) {
          alert('Format du code invalide. Exemple valide : INF101, MATH301');
          return;
        }
        const payload: CoursePayload = {
          name:        this.newCourse.name,
          code,
          level:       this.newCourse.level,
          semester:    this.newCourse.semester || 'S1',
          department:  this.newCourse.department || 'Informatique',
          credits:     this.newCourse.credits ?? 3,
          duration:    this.newCourse.duration ?? 90,
          hoursPerWeek: this.newCourse.hours,
          maxStudents: this.newCourse.students || undefined,
          description: this.newCourse.description || undefined,
        };
        this.coursesService.addCourse(payload).subscribe({
          next: (created) => { this.courses = [...this.courses, { id: created.id, ...this.newCourse, groups: [] }]; this.closeCourseModal(); },
          error: () => { this.courses = [...this.courses, { id: Date.now(), ...this.newCourse, groups: [] }]; this.closeCourseModal(); }
        });
      }
    }


  /* ── Group modal ── */
  openGroupModal(group?: StudentGroup): void {
    this.editingGroup = group || null;
    if (group) {
      this.newGroup = { name: group.name, level: group.level, promotion: group.promotion, capacity: group.capacity, responsible: group.responsible };
    } else {
      this.newGroup = { name: '', level: 'L1', promotion: 'Licence 1', capacity: 30, responsible: '' };
    }
    this.isGroupModalOpen = true;
  }

  closeGroupModal(): void { this.isGroupModalOpen = false; this.editingGroup = null; }

  saveGroup(): void {
    if (this.editingGroup) {
      this.groups = this.groups.map(g => g.id === this.editingGroup!.id ? { ...g, ...this.newGroup } : g);
    } else {
      this.groups = [...this.groups, { id: Date.now(), ...this.newGroup, enrolled: 0, courses: [] }];
    }
    this.closeGroupModal();
  }

  /* ── Delete ── */
  openDeleteModal(id: number, name: string, type: 'course' | 'group'): void {
    this.itemToDelete = { id, name, type };
    this.isDeleteModalOpen = true;
  }
  closeDeleteModal(): void { this.isDeleteModalOpen = false; this.itemToDelete = null; }
  confirmDelete(): void {
    if (!this.itemToDelete) return;
    if (this.itemToDelete.type === 'course') {
      this.coursesService.deleteCourse(this.itemToDelete.id).subscribe({
        next: () => this.courses = this.courses.filter(c => c.id !== this.itemToDelete!.id),
        error: () => this.courses = this.courses.filter(c => c.id !== this.itemToDelete!.id)
      });
    } else {
      this.groups = this.groups.filter(g => g.id !== this.itemToDelete!.id);
    }
    this.closeDeleteModal();
  }

  /* ── Helpers ── */

  /* ── Import ── */
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
      const lines = text.replace(/^\uFEFF/, '').split('\n').filter((l: string) => l.trim());
      if (lines.length < 2) { this.importParseError = 'Fichier vide.'; return; }
      const sep = lines[0].includes(';') ? ';' : ',';
      this.importPreviewRows = lines.slice(1).map((line: string) => {
        const c = line.split(sep).map((x: string) => x.replace(/^"|"$/g, '').trim());
        const errors: string[] = [];
        if (!c[0]) errors.push('Nom manquant'); if (!c[1]) errors.push('Code manquant');
        return { name: c[0] || '', code: c[1] || '', level: c[2] || 'L1', type: c[3] || 'Cours magistral', professor: c[4] || '', valid: errors.length === 0, errors };
      }).filter((r: any) => r.name);
      if (!this.importPreviewRows.length) { this.importParseError = 'Aucune donnée valide.'; return; }
      this.importStep = 2;
    } catch { this.importParseError = 'Erreur de lecture.'; } finally { this.isImportLoading = false; }
  }
  get validImportRows()    { return this.importPreviewRows.filter((r: any) => r.valid); }
  get invalidImportRows()  { return this.importPreviewRows.filter((r: any) => !r.valid); }
  get filteredImportRows() {
    if (this.importPreviewFilter === 'valid')   return this.validImportRows;
    if (this.importPreviewFilter === 'invalid') return this.invalidImportRows;
    return this.importPreviewRows;
  }
  confirmImport(): void {
    this.validImportRows.forEach((r: any) => { this.courses.push({ id: Date.now() + Math.random(), name: r.name, code: r.code, level: r.level, type: r.type, professor: r.professor, hours: 30, students: 0, groups: [] }); });
    this.importedCount = this.validImportRows.length;
    this.closeImportModal(); this.showImportToast = true;
    setTimeout(() => this.showImportToast = false, 4000);
  }
  downloadTemplate(): void {
    const csv = '\uFEFF' + '"Nom du cours";"Code";"Niveau";"Type";"Enseignant";"Volume horaire";"Étudiants"\n"Mathématiques Avancées";"MATH301";"L3";"Cours magistral";"Dr. Martin Dupont";"48";"85"\n"TP Chimie";"CHEM205";"L2";"TP";"Dr. Claire Dubois";"24";"30"';
    const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' })); a.download = 'modele_cours.csv'; a.click();
  }
  formatSize(b: number): string { if (b < 1024) return b + ' o'; if (b < 1048576) return (b/1024).toFixed(1) + ' Ko'; return (b/1048576).toFixed(1) + ' Mo'; }


  getLevelBg(level: string): string {
    const map: Record<string, string> = { L1: '#15803D', L2: '#0891B2', L3: '#1D4ED8', M1: '#6D28D9', M2: '#7C2D12' };
    return map[level] || '#374151';
  }

  getTypeBg(type: string): string {
    if (type === 'Cours magistral') return '#15803D';
    if (type === 'TD')  return '#1D4ED8';
    if (type === 'TP')  return '#7C3AED';
    return '#374151';
  }

  getFillRate(enrolled: number, capacity: number): number { return Math.round((enrolled / capacity) * 100); }
  getFillColor(rate: number): string {
    if (rate >= 90) return '#EF4444';
    if (rate >= 70) return '#F97316';
    return '#15803D';
  }
}