import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HeaderComponent } from '../header/header.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent],
  template: `
    <div class="flex h-screen bg-gray-50">
      <app-sidebar [activePage]="activePage"></app-sidebar>
      <div class="flex-1 flex flex-col overflow-hidden">
        <app-header [title]="title" [subtitle]="subtitle">
          <ng-content select="[header-actions]"></ng-content>
        </app-header>
        <main class="flex-1 overflow-y-auto p-6">
          <ng-content></ng-content>
        </main>
      </div>
    </div>
  `
})
export class AppLayoutComponent {
  @Input() activePage: string = '';
  @Input() title: string = '';
  @Input() subtitle?: string;
}
