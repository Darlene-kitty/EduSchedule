import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-competences',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-6">
      <h2 class="text-2xl font-bold text-gray-800 mb-4">Compétences</h2>
      <p class="text-gray-500">Gestion des compétences.</p>
    </div>
  `
})
export class CompetencesComponent {}
