import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CourseService, Course } from '../../core/services/course.service';

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.css']
})
export class CoursesComponent implements OnInit {
  courses: Course[] = [];
  filteredCourses: Course[] = [];
  searchTerm = '';
  isLoading = true;
  showAddModal = false;
  editingCourse: Course | null = null;

  newCourse: Partial<Course> = {
    name: '',
    code: '',
    description: '',
    credits: 0,
    semester: ''
  };

  constructor(private courseService: CourseService) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.isLoading = true;
    this.courseService.getCourses().subscribe({
      next: (courses) => {
        this.courses = courses;
        this.filteredCourses = courses;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading courses', error);
        this.isLoading = false;
      }
    });
  }

  filterCourses(): void {
    const term = this.searchTerm.toLowerCase();
    this.filteredCourses = this.courses.filter(course =>
      course.name.toLowerCase().includes(term) ||
      course.code.toLowerCase().includes(term) ||
      (course.description?.toLowerCase().includes(term) || false)
    );
  }

  openAddModal(): void {
    this.showAddModal = true;
    this.editingCourse = null;
    this.newCourse = {
      name: '',
      code: '',
      description: '',
      credits: 0,
      semester: ''
    };
  }

  openEditModal(course: Course): void {
    this.showAddModal = true;
    this.editingCourse = course;
    this.newCourse = { ...course };
  }

  closeModal(): void {
    this.showAddModal = false;
    this.editingCourse = null;
  }

  saveCourse(): void {
    if (this.editingCourse) {
      this.courseService.updateCourse(this.editingCourse.id, this.newCourse).subscribe({
        next: () => {
          this.loadCourses();
          this.closeModal();
        },
        error: (error) => console.error('Error updating course', error)
      });
    } else {
      this.courseService.createCourse(this.newCourse).subscribe({
        next: () => {
          this.loadCourses();
          this.closeModal();
        },
        error: (error) => console.error('Error creating course', error)
      });
    }
  }

  deleteCourse(id: number): void {
    if (confirm('Are you sure you want to delete this course?')) {
      this.courseService.deleteCourse(id).subscribe({
        next: () => this.loadCourses(),
        error: (error) => console.error('Error deleting course', error)
      });
    }
  }
}
