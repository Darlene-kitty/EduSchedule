import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  @Input() title: string = '';
  @Input() subtitle?: string;

  currentUser: any = null;

  now = new Date();
  dateStr = this.now.toLocaleDateString('fr-FR', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
  timeStr = this.now.toLocaleTimeString('fr-FR', {
    hour: '2-digit',
    minute: '2-digit'
  });

  ngOnInit() {
    // Charger l'utilisateur depuis le service
    this.authService.user$.subscribe(user => {
      this.currentUser = user;
    });

    // Update time every minute
    setInterval(() => {
      this.now = new Date();
      this.timeStr = this.now.toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
      });
    }, 60000);
  }

  getUserName(): string {
    return this.currentUser?.name || this.currentUser?.username || 'Utilisateur';
  }

  getUserInitials(): string {
    const name = this.getUserName();
    return name.substring(0, 2).toUpperCase();
  }

  logout(): void {
    this.authService.logout();
  }
}
