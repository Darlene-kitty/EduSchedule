import { Component, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './reports.html',
  styleUrl: './reports.css'
})
export class ReportsComponent implements AfterViewInit {

  @ViewChild('weeklyChart')  weeklyChartRef!: ElementRef;
  @ViewChild('roomChart')    roomChartRef!: ElementRef;
  @ViewChild('pieChart')     pieChartRef!: ElementRef;
  @ViewChild('workloadChart') workloadChartRef!: ElementRef;

  metrics = [
    { label: 'Total cours',      value: '156', trend: '+8%',  color: 'bg-blue',   icon: 'menu_book' },
    { label: 'Étudiants actifs', value: '248', trend: '+12%', color: 'bg-green',  icon: 'group' },
    { label: 'Heures totales',   value: '220h', trend: '+5%', color: 'bg-purple', icon: 'schedule' },
    { label: 'Taux occupation',  value: '73%',  trend: '+3%', color: 'bg-orange', icon: 'bar_chart' },
  ];

  ngAfterViewInit(): void {
    this.initWeeklyChart();
    this.initRoomChart();
    this.initPieChart();
    this.initWorkloadChart();
  }

  initWeeklyChart(): void {
    new Chart(this.weeklyChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels: ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven'],
        datasets: [
          { label: 'Cours',  data: [24, 22, 20, 26, 18], backgroundColor: '#15803D' },
          { label: 'Heures', data: [48, 44, 40, 52, 36], backgroundColor: '#3B82F6' }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'top' } },
        scales: { y: { beginAtZero: true } }
      }
    });
  }

  initRoomChart(): void {
    new Chart(this.roomChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels: ['A101', 'A102', 'A103', 'B203', 'Lab B1', 'Amphi A'],
        datasets: [{
          label: 'Utilisation (%)',
          data: [85, 72, 68, 90, 78, 45],
          backgroundColor: '#10B981'
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { x: { beginAtZero: true, max: 100 } }
      }
    });
  }

  initPieChart(): void {
    new Chart(this.pieChartRef.nativeElement, {
      type: 'pie',
      data: {
        labels: ['Mathématiques', 'Physique', 'Informatique', 'Langues', 'Autres'],
        datasets: [{
          data: [28, 22, 18, 15, 17],
          backgroundColor: ['#3B82F6', '#10B981', '#8B5CF6', '#F59E0B', '#6B7280']
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } }
      }
    });
  }

  initWorkloadChart(): void {
    new Chart(this.workloadChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels: ['Dr. Martin', 'Dr. Laurent', 'Dr. Sophie', 'Prof. Bernard', 'Prof. Dubois'],
        datasets: [{
          label: 'Heures/semaine',
          data: [24, 22, 26, 20, 18],
          backgroundColor: '#8B5CF6'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true } }
      }
    });
  }
}