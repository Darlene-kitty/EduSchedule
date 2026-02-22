import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

export interface CourseCard {
  id: number; name: string; room: string; group: string; professor: string;
  startTime: string; endTime: string; day: string;
  color: string; gradientEnd: string;
  type: 'CM' | 'TD' | 'TP' | 'Séminaire';
}

@Component({
  selector: 'app-schedules',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, SidebarComponent],
  templateUrl: './schedule.html',
  styleUrl: './schedule.css'
})
export class SchedulesComponent implements OnInit {
  currentDate = ''; currentTime = '';
  isModalOpen = false;
  draggedCourse: CourseCard | null = null;

  days = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi'];

  courses: CourseCard[] = [
    { id: 1,  name: 'Mathématiques', room: 'A101', group: 'L1-G1', professor: 'Dr. Martin',   startTime: '08:00', endTime: '10:00', day: 'Lundi',    color: '#3B5BDB', gradientEnd: '#4C6EF5', type: 'CM' },
    { id: 2,  name: 'Physique',      room: 'B203', group: 'L1-G1', professor: 'Dr. Laurent',  startTime: '10:00', endTime: '12:00', day: 'Mardi',    color: '#2F9E44', gradientEnd: '#40C057', type: 'CM' },
    { id: 3,  name: 'Analyse',       room: 'A103', group: 'L2-G1', professor: 'Prof. Bernard',startTime: '08:00', endTime: '10:00', day: 'Mercredi', color: '#1098AD', gradientEnd: '#22B8CF', type: 'CM' },
    { id: 4,  name: 'Géométrie',     room: 'A101', group: 'L1-G1', professor: 'Dr. Martin',   startTime: '09:00', endTime: '11:00', day: 'Jeudi',    color: '#1971C2', gradientEnd: '#339AF0', type: 'TD' },
    { id: 5,  name: 'Statistiques',  room: 'A104', group: 'L3-G1', professor: 'Dr. Sophie',   startTime: '08:00', endTime: '10:00', day: 'Vendredi', color: '#C92A2A', gradientEnd: '#FA5252', type: 'CM' },
    { id: 6,  name: 'Algèbre',       room: 'A102', group: 'L2-G2', professor: 'Prof. Dubois', startTime: '14:00', endTime: '16:00', day: 'Lundi',    color: '#862E9C', gradientEnd: '#CC5DE8', type: 'TD' },
    { id: 7,  name: 'TP Physique',   room: 'Lab B1',group: 'L1-G2',professor: 'Dr. Laurent',  startTime: '14:00', endTime: '16:00', day: 'Mardi',    color: '#2F9E44', gradientEnd: '#40C057', type: 'TP' },
    { id: 8,  name: 'Séminaire',     room: 'Amphi A',group: 'M1',  professor: 'Prof. Richard',startTime: '14:00', endTime: '17:00', day: 'Jeudi',    color: '#E67700', gradientEnd: '#FCC419', type: 'Séminaire' },
    { id: 9,  name: 'Probabilités',  room: 'A104', group: 'L3-G2', professor: 'Dr. Sophie',   startTime: '10:00', endTime: '12:00', day: 'Vendredi', color: '#C92A2A', gradientEnd: '#FA5252', type: 'TD' },
  ];

  newCourse = { name: '', room: '', group: '', professor: '', startTime: '08:00', endTime: '10:00', day: 'Lundi', type: 'CM' };

  colors = [
    { color: '#3B5BDB', gradientEnd: '#4C6EF5' },
    { color: '#2F9E44', gradientEnd: '#40C057' },
    { color: '#1098AD', gradientEnd: '#22B8CF' },
    { color: '#C92A2A', gradientEnd: '#FA5252' },
    { color: '#862E9C', gradientEnd: '#CC5DE8' },
    { color: '#E67700', gradientEnd: '#FCC419' },
  ];
  selectedColorIdx = 0;

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

  getCoursesForDay(day: string): CourseCard[] {
    return this.courses.filter(c => c.day === day);
  }

  deleteCourse(id: number): void { this.courses = this.courses.filter(c => c.id !== id); }

  openModal(): void {
    this.newCourse = { name: '', room: '', group: '', professor: '', startTime: '08:00', endTime: '10:00', day: 'Lundi', type: 'CM' };
    this.isModalOpen = true;
  }

  closeModal(): void { this.isModalOpen = false; }

  handleAdd(): void {
    const c = this.colors[this.selectedColorIdx];
    this.courses.push({
      id: Date.now(), ...this.newCourse, color: c.color, gradientEnd: c.gradientEnd,
      type: this.newCourse.type as any
    });
    this.closeModal();
  }

  onDragStart(course: CourseCard): void { this.draggedCourse = course; }
  onDrop(day: string): void {
    if (this.draggedCourse) {
      this.draggedCourse.day = day;
      this.draggedCourse = null;
    }
  }
  onDragOver(event: DragEvent): void { event.preventDefault(); }
}