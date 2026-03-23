import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { StorageService } from '../../core/services/storage.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  private router = inject(Router);
  private authService = inject(AuthService);
  private storageService = inject(StorageService);

  username = '';
  password = '';
  showPassword = false;
  rememberMe = false;
  loading = false;
  errorMessage = '';

  ngOnInit() {
    const savedEmail = this.storageService.getItem<string>('savedEmail');
    if (savedEmail) {
      this.username = savedEmail;
      this.rememberMe = true;
    }
  }

  onSubmit() {
    if (!this.username.trim() || !this.password.trim()) {
      this.errorMessage = 'Veuillez remplir tous les champs';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: (response) => {
        if (this.rememberMe) {
          this.storageService.setItem('savedEmail', this.username);
        } else {
          this.storageService.removeItem('savedEmail');
        }
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Erreur de connexion. Veuillez réessayer.';
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }
}
