import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

export interface ConflictItem { name: string; professor: string; students: number; }
export interface Solution { room: string; available: boolean; }
export interface Conflict {
  id: number; title: string; description: string;
  priority: 'urgent' | 'normal'; date: string; time: string;
  items: ConflictItem[]; solutions: Solution[]; resolved: boolean;
}

@Component({
  selector: 'app-conflicts',
  standalone: true,
  imports: [CommonModule, MatIconModule, SidebarComponent],
  templateUrl: './conflicts.html',
  styleUrl: './conflicts.css'
})
export class ConflictsComponent implements OnInit {
  currentDate = ''; currentTime = '';

  stats = [
    { label: 'Conflits actifs',      value: '4',   color: 'red'    },
    { label: "Résolus aujourd'hui",  value: '2',   color: 'green'  },
    { label: 'Haute priorité',       value: '2',   color: 'orange' },
    { label: 'Taux de résolution',   value: '95%', color: 'blue'   },
  ];

  conflicts: Conflict[] = [
    {
      id: 1, title: 'Double réservation de la salle B203',
      description: 'Deux cours planifiés au même horaire',
      priority: 'urgent', date: 'Lundi 21 Oct 2025', time: '14:00 - 16:00',
      items: [
        { name: 'Physique Quantique', professor: 'Prof. Bernard', students: 30 },
        { name: 'TD Mathématiques',   professor: 'Dr. Dupont',    students: 25 },
      ],
      solutions: [
        { room: 'Salle A102', available: true },
        { room: 'Salle B104', available: true },
      ],
      resolved: false
    },
    {
      id: 2, title: 'Conflit de ressource - Projecteur HD',
      description: 'Même équipement demandé par deux salles',
      priority: 'normal', date: 'Mercredi 23 Oct 2025', time: '10:00 - 12:00',
      items: [
        { name: 'Chimie Organique', professor: 'Prof. Laurent', students: 35 },
        { name: 'Biologie Cellulaire', professor: 'Dr. Martin', students: 28 },
      ],
      solutions: [
        { room: 'Projecteur B disponible', available: true },
      ],
      resolved: false
    },
  ];

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

  applySolution(conflict: Conflict, solution: Solution): void {
    conflict.resolved = true;
    this.conflicts = this.conflicts.filter(c => c.id !== conflict.id);
  }
}