import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { CoursesManagementService, CoursePayload } from '../../core/services/courses-management.service';
import { UsersManagementService } from '../../core/services/users-management.service';
import { GroupesManagementService, GroupeBackend } from '../../core/services/groupes-management.service';
import { FilieresManagementService, FiliereBackend } from '../../core/services/filieres-management.service';
import { NiveauxManagementService, NiveauBackend } from '../../core/services/niveaux-management.service';
import { SchoolManagementService, SchoolEntry } from '../../core/services/school-management.service';
import { AuthService } from '../../core/services/auth.service';
import { AppConfigService } from '../../core/services/app-config.service';
import { CourseDocumentsService, CourseDocument, DocumentCategory, UploadProgress } from '../../core/services/course-documents.service';
import { forkJoin } from 'rxjs';

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
  groupIds: number[];
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
  private groupesSvc     = inject(GroupesManagementService);
  private filieresSvc    = inject(FilieresManagementService);
  private niveauxSvc     = inject(NiveauxManagementService);
  private schoolSvc      = inject(SchoolManagementService);
  private authService    = inject(AuthService);
  private configSvc      = inject(AppConfigService);
  private docsSvc        = inject(CourseDocumentsService);

  // Helpers statiques exposés au template
  readonly docIconFor    = CourseDocumentsService.iconFor;
  readonly docColorFor   = CourseDocumentsService.colorFor;
  readonly docFormatSize = CourseDocumentsService.formatSize;
  readonly docCatLabel   = CourseDocumentsService.categoryLabel;

  // ── Supports de cours ──────────────────────────────────────────────────────
  selectedCourseForDocs: Course | null = null;
  courseDocuments: CourseDocument[] = [];
  isLoadingDocs = false;

  // Upload
  isUploadModalOpen = false;
  uploadFile: File | null = null;
  uploadCategory: DocumentCategory = 'COURS';
  uploadDescription = '';
  uploadProgress = 0;
  isUploading = false;
  uploadError = '';
  uploadSuccess = false;
  isDraggingDoc = false;

  readonly docCategories: { value: DocumentCategory; label: string }[] = [
    { value: 'COURS',  label: 'Cours'  },
    { value: 'TD',     label: 'TD'     },
    { value: 'TP',     label: 'TP'     },
    { value: 'EXAMEN', label: 'Examen' },
    { value: 'AUTRE',  label: 'Autre'  },
  ];

  // Référentiels pour les selects
  teachers:  { id: number; name: string }[] = [];
  filieres:  FiliereBackend[] = [];
  niveaux:   NiveauBackend[]  = [];
  groupes:   GroupeBackend[]  = [];
  schools:   SchoolEntry[]    = [];
  currentSchoolId = 1; // fallback, remplacé au chargement

  // Départements chargés depuis le backend (filières), avec fallback statique
  departments: string[] = ['Informatique','Mathématiques','Physique','Chimie','Biologie','Économie','Droit','Lettres','Sciences Humaines','Génie Civil','Génie Électrique'];
  durations:   number[] = [30, 45, 60, 90, 120, 150, 180, 240];
  creditsList: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
  readonly courseTypes: Course['type'][] = ['Cours magistral', 'TD', 'TP', 'Séminaire'];

  // Utilisateur connecté (pour le header)
  currentUserName = '';
  currentUserInitials = '';
  unreadCount = 0;

  // Groupes filtrés selon le niveau sélectionné dans le modal cours
  get groupesFiltres(): GroupeBackend[] {
    const selectedNiveau = this.niveaux.find(n => n.code === this.newCourse.level || n.name === this.newCourse.level);
    if (!selectedNiveau) return this.groupes;
    return this.groupes.filter(g => g.niveauId === selectedNiveau.id);
  }

  // IDs des groupes sélectionnés pour le cours en cours de création/édition
  selectedGroupIds: number[] = [];

  activeTab: 'courses' | 'groups' | 'documents' = 'courses';
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

  newCourse = { name: '', code: '', level: 'L1', type: 'Cours magistral' as Course['type'], professor: '', hours: 30, students: 0, credits: 3, duration: 90, department: 'Informatique', semester: 'S1', description: '', teacherId: 0 };
  newGroup  = { name: '', level: 'L1', niveauId: 0, promotion: 'Licence 1', capacity: 30, responsible: '' };


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
    const u = this.authService.getUser();
    if (u) {
      const n = u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || 'Utilisateur';
      this.currentUserName = n;
      const p = n.trim().split(' ').filter((x: string) => x);
      this.currentUserInitials = p.length >= 2 ? (p[0][0] + p[1][0]).toUpperCase() : n.substring(0, 2).toUpperCase();
    }
    setInterval(() => this.updateDateTime(), 1000);
    this.loadReferenceData();
    this.loadCourses();
  }

  private loadReferenceData(): void {
    // Récupérer le schoolId de l'utilisateur connecté
    const user = this.authService.getUser();
    this.currentSchoolId = user?.primarySchoolId || user?.schoolId || 1;

    forkJoin({
      teachers: this.usersSvc.getUsers(),
      filieres: this.filieresSvc.getAll(),
      niveaux:  this.niveauxSvc.getAll(),
      groupes:  this.groupesSvc.getAll(),
      schools:  this.schoolSvc.getAll(),
    }).subscribe({
      next: ({ teachers, filieres, niveaux, groupes, schools }) => {
        this.teachers = teachers
          .filter(u => (u.role || '').toUpperCase().includes('TEACHER'))
          .map(u => ({ id: u.id, name: u.name || [u.firstName, u.lastName].filter(Boolean).join(' ') || u.username || '' }))
          .filter(t => t.name);
        this.filieres = filieres;
        this.niveaux  = niveaux;
        this.groupes  = groupes;
        this.schools  = schools;
        // Peupler departments depuis les filières backend
        const deptNames = filieres.map(f => f.name || (f as any).nom || '').filter(Boolean);
        if (deptNames.length > 0) {
          this.departments = [...new Set(deptNames)].sort();
        }
        // Mettre à jour schoolId depuis la liste des écoles si besoin
        if (schools.length > 0 && !this.currentSchoolId) {
          this.currentSchoolId = schools[0].id;
        }
        // Charger les groupes comme StudentGroup pour l'onglet groupes
        this.groups = groupes.map(g => ({
          id: g.id,
          name: g.name,
          level: this.niveaux.find(n => n.id === g.niveauId)?.code || g.niveauName || '',
          promotion: g.niveauName || '',
          capacity: g.capacite,
          enrolled: 0,
          courses: [],
          responsible: '',
          // conserver niveauId pour l'édition
          niveauId: g.niveauId
        } as StudentGroup & { niveauId: number }));
      },
      error: () => {}
    });
  }

  private loadTeachers(): void {} // conservé pour compatibilité, remplacé par loadReferenceData

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
          groupIds: [],
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

  setTab(tab: 'courses' | 'groups' | 'documents'): void { this.activeTab = tab; this.searchQuery = ''; }

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
    this.selectedGroupIds = course?.groupIds ? [...course.groupIds] : [];
    if (course) {
      // Résoudre le teacherId depuis le nom si absent
      const resolvedTeacherId = course.teacherId
        ?? this.teachers.find(t => t.name === course.professor)?.id
        ?? 0;
      this.newCourse = {
        name: course.name, code: course.code, level: course.level,
        type: course.type, professor: course.professor, hours: course.hours,
        students: course.students,
        credits: course.credits ?? 3,
        duration: course.duration ?? 90,
        department: course.department ?? '',
        semester: course.semester ?? 'S1',
        description: course.description ?? '',
        teacherId: resolvedTeacherId
      };
    } else {
      this.newCourse = { name: '', code: '', level: 'L1', type: 'Cours magistral', professor: '', hours: 30, students: 0, credits: 3, duration: 90, department: '', semester: 'S1', description: '', teacherId: 0 };
    }
    this.isCourseModalOpen = true;
  }

  toggleGroupSelection(groupId: number): void {
    const idx = this.selectedGroupIds.indexOf(groupId);
    if (idx >= 0) this.selectedGroupIds.splice(idx, 1);
    else this.selectedGroupIds.push(groupId);
  }

  isGroupSelected(groupId: number): boolean {
    return this.selectedGroupIds.includes(groupId);
  }

  /** Quand le niveau change, réinitialiser les groupes sélectionnés */
  onLevelChange(): void {
    this.selectedGroupIds = [];
  }

  closeCourseModal(): void { this.isCourseModalOpen = false; this.editingCourse = null; }

  saveCourse(): void {
    // Résoudre le teacherId et le nom depuis le select (qui bind sur teacherId)
    const teacher = this.teachers.find(t => t.id === +this.newCourse.teacherId)
      || this.teachers.find(t => t.name === this.newCourse.professor);
    const teacherId = teacher?.id || undefined;

    if (this.editingCourse) {
      const editingId = this.editingCourse.id;
      const snapshot = { ...this.newCourse };
      const payload: Partial<CoursePayload> = {
        name:        snapshot.name,
        code:        snapshot.code.toUpperCase().trim(),
        level:       snapshot.level,
        semester:    snapshot.semester || 'S1',
        department:  snapshot.department || 'Informatique',
        credits:     snapshot.credits ?? 3,
        duration:    snapshot.duration ?? 90,
        hoursPerWeek: snapshot.hours,
        maxStudents: snapshot.students || undefined,
        description: snapshot.description || undefined,
        schoolId:    this.editingCourse.schoolId || this.currentSchoolId,
        teacherId
      };
      this.coursesService.updateCourse(editingId, payload).subscribe({
        next: () => {
          this.assignGroupsToCourse(editingId, this.selectedGroupIds);
          const groupNames = this.groupes.filter(g => this.selectedGroupIds.includes(g.id)).map(g => g.name);
          this.courses = this.courses.map(c => c.id === editingId
            ? { ...c, ...snapshot, code: payload.code!, professor: teacher?.name || snapshot.professor, teacherId, groups: groupNames, groupIds: [...this.selectedGroupIds] }
            : c);
          this.closeCourseModal();
        },
        error: (err) => alert(err?.error?.message || 'Erreur lors de la modification')
      });
    } else {
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
        schoolId:    this.currentSchoolId,
        teacherId
      };
      this.coursesService.addCourse(payload).subscribe({
        next: (created) => {
          this.assignGroupsToCourse(created.id, this.selectedGroupIds);
          const groupNames = this.groupes.filter(g => this.selectedGroupIds.includes(g.id)).map(g => g.name);
          this.courses = [...this.courses, {
            id: created.id, ...this.newCourse, code,
            professor: teacher?.name || this.newCourse.professor,
            teacherId, groups: groupNames, groupIds: [...this.selectedGroupIds]
          }];
          this.closeCourseModal();
        },
        error: (err) => alert(err?.error?.message || 'Erreur lors de la création')
      });
    }
  }

  /** Assigne les groupes (school-service) à un cours via course-groups */
  private assignGroupsToCourse(courseId: number, groupIds: number[]): void {
    if (!groupIds.length) return;
    groupIds.forEach(gId => {
      const groupe = this.groupes.find(g => g.id === gId);
      if (!groupe) return;
      this.coursesService.assignGroup(
        courseId,
        groupe.name,
        groupe.capacite ?? 30,
        undefined
      ).subscribe({ error: () => {} });
    });
  }


  /* ── Group modal ── */
  openGroupModal(group?: StudentGroup): void {
    this.editingGroup = group || null;
    if (group) {
      // Chercher le niveauId depuis la liste des niveaux chargés
      const niveau = this.niveaux.find(n =>
        n.id === (group as any).niveauId ||
        n.code === group.level ||
        n.name === group.level
      );
      const niveauId = niveau?.id ?? (group as any).niveauId ?? 0;
      this.newGroup = {
        name: group.name,
        level: niveau?.code || group.level,
        niveauId,
        promotion: group.promotion,
        capacity: group.capacity,
        responsible: group.responsible
      };
    } else {
      this.newGroup = { name: '', level: 'L1', niveauId: 0, promotion: 'Licence 1', capacity: 30, responsible: '' };
    }
    this.isGroupModalOpen = true;
  }

  closeGroupModal(): void { this.isGroupModalOpen = false; this.editingGroup = null; }

  saveGroup(): void {
    const niveauId = this.newGroup.niveauId
      || this.niveaux.find(n => n.code === this.newGroup.level || n.name === this.newGroup.level)?.id
      || 0;

    if (this.editingGroup) {
      const id = this.editingGroup.id;
      this.groupesSvc.update(id, { name: this.newGroup.name, capacite: this.newGroup.capacity, niveauId, active: true }).subscribe({
        next: (updated) => {
          const niveau = this.niveaux.find(n => n.id === niveauId);
          this.groups = this.groups.map(g => g.id === id
            ? { ...g, name: this.newGroup.name, level: niveau?.code || this.newGroup.level,
                promotion: niveau?.name || this.newGroup.promotion,
                capacity: this.newGroup.capacity, responsible: this.newGroup.responsible,
                niveauId } as any
            : g);
          // Mettre à jour aussi la liste groupes backend
          if (updated) {
            this.groupes = this.groupes.map(g => g.id === id ? { ...g, ...updated } : g);
          }
          this.closeGroupModal();
        },
        error: (err: any) => alert(err?.error?.message || 'Erreur lors de la modification du groupe')
      });
    } else {
      this.groupesSvc.create({ name: this.newGroup.name, capacite: this.newGroup.capacity, niveauId, active: true }).subscribe({
        next: (created) => {
          if (created) {
            const niveau = this.niveaux.find(n => n.id === niveauId);
            const newGroup: StudentGroup = {
              id: created.id, name: created.name,
              level: niveau?.code || this.newGroup.level,
              promotion: niveau?.name || this.newGroup.promotion,
              capacity: created.capacite, enrolled: 0, courses: [], responsible: this.newGroup.responsible
            };
            this.groups = [...this.groups, { ...newGroup, niveauId } as any];
            this.groupes = [...this.groupes, created];
          }
          this.closeGroupModal();
        },
        error: (err: any) => alert(err?.error?.message || 'Erreur lors de la création du groupe')
      });
    }
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
      this.groupesSvc.delete(this.itemToDelete.id).subscribe({
        next: () => {
          this.groups  = this.groups.filter(g => g.id !== this.itemToDelete!.id);
          this.groupes = this.groupes.filter(g => g.id !== this.itemToDelete!.id);
        },
        error: () => {
          this.groups  = this.groups.filter(g => g.id !== this.itemToDelete!.id);
          this.groupes = this.groupes.filter(g => g.id !== this.itemToDelete!.id);
        }
      });
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
    const codePattern = /^[A-Z]{2,4}[0-9]{2,3}$/;
    const validRows = this.validImportRows.filter((r: any) => codePattern.test((r.code || '').toUpperCase().trim()));
    let done = 0;
    this.importedCount = validRows.length;
    validRows.forEach((r: any) => {
      const code = r.code.toUpperCase().trim();
      const payload: CoursePayload = {
        name: r.name, code,
        level: r.level || 'L1',
        semester: 'S1',
        department: r.department || 'Informatique',
        credits: 3, duration: 90,
        hoursPerWeek: parseInt(r.hours) || 30,
        maxStudents: parseInt(r.students) || undefined,
        schoolId: this.currentSchoolId,
        teacherId: this.teachers.find(t => t.name === r.professor)?.id
      };
      this.coursesService.addCourse(payload).subscribe({
        next: (created) => {
          this.courses.push({ id: created.id, name: r.name, code, level: r.level || 'L1', type: r.type || 'Cours magistral', professor: r.professor || '', hours: payload.hoursPerWeek || 30, students: 0, groups: [], groupIds: [] });
          done++;
          if (done === validRows.length) { this.closeImportModal(); this.showImportToast = true; setTimeout(() => this.showImportToast = false, 4000); }
        },
        error: () => { done++; if (done === validRows.length) { this.closeImportModal(); this.showImportToast = true; setTimeout(() => this.showImportToast = false, 4000); } }
      });
    });
    if (!validRows.length) this.closeImportModal();
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

  // ── Supports de cours ─────────────────────────────────────────────────────

  openDocumentsTab(course: Course): void {
    this.selectedCourseForDocs = course;
    this.activeTab = 'documents';
    this.loadDocuments(course.id);
  }

  private loadDocuments(courseId: number): void {
    this.isLoadingDocs = true;
    this.docsSvc.getDocuments(courseId).subscribe({
      next: docs => { this.courseDocuments = docs; this.isLoadingDocs = false; },
      error: ()  => { this.courseDocuments = []; this.isLoadingDocs = false; }
    });
  }

  openUploadModal(): void {
    this.uploadFile = null;
    this.uploadCategory = 'COURS';
    this.uploadDescription = '';
    this.uploadProgress = 0;
    this.isUploading = false;
    this.uploadError = '';
    this.uploadSuccess = false;
    this.isDraggingDoc = false;
    this.isUploadModalOpen = true;
  }

  closeUploadModal(): void { this.isUploadModalOpen = false; }

  onDocDragOver(e: DragEvent): void  { e.preventDefault(); this.isDraggingDoc = true; }
  onDocDragLeave(): void             { this.isDraggingDoc = false; }
  onDocDrop(e: DragEvent): void {
    e.preventDefault(); this.isDraggingDoc = false;
    const f = e.dataTransfer?.files?.[0];
    if (f) this.setUploadFile(f);
  }
  onDocFileSelected(e: Event): void {
    const f = (e.target as HTMLInputElement).files?.[0];
    if (f) this.setUploadFile(f);
  }

  setUploadFile(file: File): void {
    const allowed = ['pdf','doc','docx','ppt','pptx','xls','xlsx','txt','jpg','jpeg','png'];
    const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
    if (!allowed.includes(ext)) {
      this.uploadError = `Format non supporté (.${ext}). Formats acceptés : PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, TXT, JPG, PNG`;
      return;
    }
    this.uploadFile = file;
    this.uploadError = '';
  }

  submitUpload(): void {
    if (!this.uploadFile || !this.selectedCourseForDocs) return;
    this.isUploading = true;
    this.uploadProgress = 0;
    this.uploadError = '';
    const userId = this.authService.getUser()?.id;

    this.docsSvc.uploadDocument(
      this.selectedCourseForDocs.id,
      this.uploadFile,
      this.uploadCategory,
      this.uploadDescription,
      userId
    ).subscribe({
      next: (progress: UploadProgress) => {
        this.uploadProgress = progress.progress;
        if (progress.done) {
          this.isUploading = false;
          if (progress.error) {
            this.uploadError = progress.error;
          } else {
            this.uploadSuccess = true;
            if (progress.document) this.courseDocuments = [progress.document, ...this.courseDocuments];
            setTimeout(() => this.closeUploadModal(), 1500);
          }
        }
      },
      error: () => { this.isUploading = false; this.uploadError = 'Erreur lors de l\'upload.'; }
    });
  }

  deleteDocument(doc: CourseDocument): void {
    if (!this.selectedCourseForDocs) return;
    if (!confirm(`Supprimer "${doc.originalFilename}" ?`)) return;
    this.docsSvc.deleteDocument(this.selectedCourseForDocs.id, doc.id).subscribe({
      next: () => { this.courseDocuments = this.courseDocuments.filter(d => d.id !== doc.id); },
      error: () => alert('Erreur lors de la suppression.')
    });
  }

  downloadDocument(doc: CourseDocument): void {
    window.open(doc.downloadUrl, '_blank');
  }

  get docsByCategory(): { category: string; label: string; docs: CourseDocument[] }[] {
    const cats: { category: string; label: string }[] = [
      { category: 'COURS',  label: 'Cours'  },
      { category: 'TD',     label: 'TD'     },
      { category: 'TP',     label: 'TP'     },
      { category: 'EXAMEN', label: 'Examens'},
      { category: 'AUTRE',  label: 'Autres' },
    ];
    return cats
      .map(c => ({ ...c, docs: this.courseDocuments.filter(d => d.category === c.category) }))
      .filter(c => c.docs.length > 0);
  }
}