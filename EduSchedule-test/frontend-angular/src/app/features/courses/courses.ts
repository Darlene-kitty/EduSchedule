import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { CoursesManagementService, Course as ManagedCourse } from '../../core/services/courses-management.service';

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

  professors: string[] = [];

  newCourse = { name: '', code: '', level: 'L1', type: 'Cours magistral' as Course['type'], professor: '', hours: 30, students: 0 };
  newGroup  = { name: '', level: 'L1', promotion: 'Licence 1', capacity: 30, responsible: '' };

  ngOnInit(): void { 
    this.updateDateTime(); 
    setInterval(() => this.updateDateTime(), 1000);
    this.loadCourses();
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
          professor: c.teacher ?? c.teacherName ?? '',
          hours: c.hours ?? c.hoursPerWeek ?? 0,
          students: 0,
          groups: [c.group ?? ''].filter(Boolean)
        }));
        // Extraire la liste unique des enseignants
        this.professors = [...new Set(
          managedCourses.map(c => c.teacher ?? c.teacherName ?? '').filter((t): t is string => !!t)
        )].sort();
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
      this.newCourse = { name: course.name, code: course.code, level: course.level, type: course.type, professor: course.professor, hours: course.hours, students: course.students };
    } else {
      this.newCourse = { name: '', code: '', level: 'L1', type: 'Cours magistral', professor: '', hours: 30, students: 0 };
    }
    this.isCourseModalOpen = true;
  }

  closeCourseModal(): void { this.isCourseModalOpen = false; this.editingCourse = null; }

  private buildPayload(): import('../../core/services/courses-management.service').CoursePayload {
    const matchedCourse = this.courses.find(c => c.professor === this.newCourse.professor);
    const teacherId = (matchedCourse as any)?.teacherId ?? undefined;
    const deptMap: Record<string, string> = {
      L1: 'Licence 1', L2: 'Licence 2', L3: 'Licence 3', M1: 'Master 1', M2: 'Master 2'
    };
    return {
      name: this.newCourse.name,
      code: this.newCourse.code,
      level: this.newCourse.level,
      semester: 'S1',
      credits: 3,
      duration: this.newCourse.hours * 60,
      department: deptMap[this.newCourse.level] || 'Général',
      hoursPerWeek: this.newCourse.hours,
      teacherId
    };
  }

  saveCourse(): void {
    if (this.editingCourse) {
      this.coursesManagementService.updateCourse(this.editingCourse.id, this.buildPayload()).subscribe({
        next: () => { this.closeCourseModal(); this.loadCourses(); },
        error: (err) => alert(err?.error?.message || 'Erreur lors de la mise à jour')
      });
    } else {
      this.coursesManagementService.addCourse(this.buildPayload()).subscribe({
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