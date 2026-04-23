import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { CoursesManagementService } from '../../core/services/courses-management.service';
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
  templateUrl: './courses.html',
  styleUrl: './courses.css'
})
export class CoursesComponent implements OnInit {
  private coursesManagementService = inject(CoursesManagementService);
  private usersSvc                 = inject(UsersManagementService);

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
  editingCourse: Course | null = null;
  editingGroup:  StudentGroup | null = null;
  itemToDelete:  { id: number; name: string; type: 'course' | 'group' } | null = null;
  viewingCourse: Course | null = null;

  courses: Course[] = [];

  groups: StudentGroup[] = [];

  newCourse = { name: '', code: '', level: 'L1', type: 'Cours magistral' as Course['type'], professor: '', hours: 30, students: 0, credits: 3, duration: 90, department: 'Informatique', semester: 'S1' };
  newGroup  = { name: '', level: 'L1', promotion: 'Licence 1', capacity: 30, responsible: '' };
  professors: string[] = [];
  semesters = ['S1', 'S2', 'S3', 'S4', 'S5', 'S6', 'S7', 'S8', 'S9', 'S10'];

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
        // Fallback: extraire depuis les cours existants
        if (!this.teachers.length) {
          this.coursesManagementService.getCourses().subscribe({
            next: courses => {
              const names = courses.map(c => c.teacher ?? '').filter(n => n.length > 0);
              this.professors = [...new Set(names)].sort();
            },
            error: () => {}
          });
        }
      },
      error: () => {}
    });
  }

  private loadCourses(): void {
    this.coursesManagementService.getCourses().subscribe({
      next: (managedCourses) => {
        this.courses = managedCourses.map(c => ({
          id: c.id,
          name: c.name,
          code: c.code,
          level: c.level,
          type: 'Cours magistral' as Course['type'],
          professor: c.teacher ?? '',
          hours: c.hours ?? 0,
          students: 0,
          groups: [c.group ?? '']
        }));
      },
      error: (err) => {
        console.error('Erreur chargement cours:', err?.error?.message || err);
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
      this.newCourse = { name: course.name, code: course.code, level: course.level, type: course.type, professor: course.professor, hours: course.hours, students: course.students, credits: 3, duration: 90, department: 'Informatique', semester: 'S1' };
    } else {
      this.newCourse = { name: '', code: '', level: 'L1', type: 'Cours magistral', professor: '', hours: 30, students: 0, credits: 3, duration: 90, department: 'Informatique', semester: 'S1' };
    }
    this.isCourseModalOpen = true;
  }

  closeCourseModal(): void { this.isCourseModalOpen = false; this.editingCourse = null; }

  saveCourse(): void {
    if (this.editingCourse) {
      this.coursesManagementService.updateCourse(this.editingCourse.id, {
        name: this.newCourse.name,
        code: this.newCourse.code,
        level: this.newCourse.level,
        hoursPerWeek: this.newCourse.hours,
        semester: 'S1'
      }).subscribe({
        next: () => { this.closeCourseModal(); this.loadCourses(); },
        error: (err) => alert(err?.error?.message || 'Erreur lors de la mise à jour')
      });
    } else {
      this.coursesManagementService.addCourse({
        name: this.newCourse.name,
        code: this.newCourse.code,
        level: this.newCourse.level,
        hoursPerWeek: this.newCourse.hours,
        semester: this.newCourse.semester || 'S1',
        credits: this.newCourse.credits || 3,
        duration: this.newCourse.duration || 90,
        department: this.newCourse.department || 'Informatique'
      }).subscribe({
        next: () => { this.closeCourseModal(); this.loadCourses(); },
        error: (err) => alert(err?.error?.message || 'Erreur lors de la création')
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
    const { id, type } = this.itemToDelete;
    if (type === 'course') {
      this.coursesManagementService.deleteCourse(id).subscribe({
        next: () => { this.closeDeleteModal(); this.loadCourses(); },
        error: (err) => alert(err?.error?.message || 'Erreur lors de la suppression')
      });
    } else {
      this.groups = this.groups.filter(g => g.id !== id);
      this.closeDeleteModal();
    }
  }

  /* ── Helpers ── */
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