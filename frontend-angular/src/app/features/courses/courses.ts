import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppLayoutComponent } from '../../shared/components/app-layout/app-layout.component';

interface Course {
  id: number;
  name: string;
  code: string;
  type: string;
  hoursPerWeek: number;
  level?: string;
  department?: string;
  teacherId?: number;
  createdAt: string;
}

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [CommonModule, FormsModule, AppLayoutComponent],
  templateUrl: './courses.html',
  styleUrls: ['./courses.css'],
})
export class Courses implements OnInit {
  searchQuery = '';
  selectedDepartment = 'all';
  courses: Course[] = [];
  loading = false;

  // Données de démonstration
  ngOnInit() {
    this.loadCourses();
  }

  loadCourses() {
    // Simuler des données
    this.courses = [
      {
        id: 1,
        name: 'Mathématiques Avancées',
        code: 'MATH301',
        type: 'COURS',
        hoursPerWeek: 4,
        level: 'L3',
        department: 'Mathématiques',
        teacherId: 1,
        createdAt: new Date().toISOString()
      },
      {
        id: 2,
        name: 'Programmation Web',
        code: 'INFO201',
        type: 'TP',
        hoursPerWeek: 3,
        level: 'L2',
        department: 'Informatique',
        teacherId: 2,
        createdAt: new Date().toISOString()
      },
      {
        id: 3,
        name: 'Physique Quantique',
        code: 'PHYS401',
        type: 'COURS',
        hoursPerWeek: 5,
        level: 'M1',
        department: 'Physique',
        createdAt: new Date().toISOString()
      },
      {
        id: 4,
        name: 'Algorithmique',
        code: 'INFO101',
        type: 'TD',
        hoursPerWeek: 2,
        level: 'L1',
        department: 'Informatique',
        teacherId: 3,
        createdAt: new Date().toISOString()
      }
    ];
  }

  get filteredCourses() {
    return this.courses.filter(course => {
      const matchesSearch = 
        course.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        course.code.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        course.type.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchesDepartment = this.selectedDepartment === 'all' || course.department === this.selectedDepartment;
      return matchesSearch && matchesDepartment;
    });
  }

  get uniqueDepartments() {
    return Array.from(new Set(this.courses.map(c => c.department).filter(Boolean)));
  }

  get stats() {
    return {
      total: this.courses.length,
      totalHours: this.courses.reduce((sum, c) => sum + c.hoursPerWeek, 0),
      departments: this.uniqueDepartments.length,
      withTeacher: this.courses.filter(c => c.teacherId).length
    };
  }

  getTypeColor(type: string): string {
    switch (type.toUpperCase()) {
      case 'COURS':
      case 'LECTURE':
        return 'bg-blue-100 text-blue-700';
      case 'TD':
      case 'TUTORIAL':
        return 'bg-green-100 text-green-700';
      case 'TP':
      case 'PRACTICAL':
        return 'bg-purple-100 text-purple-700';
      case 'PROJET':
      case 'PROJECT':
        return 'bg-orange-100 text-orange-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }

  getLevelColor(level?: string): string {
    if (!level) return 'bg-gray-100 text-gray-700';
    
    switch (level.toUpperCase()) {
      case 'L1':
      case 'LICENCE 1':
        return 'bg-green-100 text-green-700';
      case 'L2':
      case 'LICENCE 2':
        return 'bg-blue-100 text-blue-700';
      case 'L3':
      case 'LICENCE 3':
        return 'bg-purple-100 text-purple-700';
      case 'M1':
      case 'MASTER 1':
        return 'bg-orange-100 text-orange-700';
      case 'M2':
      case 'MASTER 2':
        return 'bg-red-100 text-red-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }

  viewCourse(course: Course) {
    alert(`Voir le cours: ${course.name}`);
  }

  editCourse(course: Course) {
    alert(`Éditer le cours: ${course.name}`);
  }

  deleteCourse(course: Course) {
    if (confirm(`Êtes-vous sûr de vouloir supprimer le cours "${course.name}" ?`)) {
      this.courses = this.courses.filter(c => c.id !== course.id);
    }
  }

  addCourse() {
    alert('Ajouter un nouveau cours');
  }
}
