import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private router = inject(Router);
  private authService = inject(AuthService);

  showPassword = false;
  showConfirmPassword = false;
  loading = false;
  errorMessage = '';
  
  formData = {
    name: '',
    email: '',
    phone: '',
    role: '',
    password: '',
    confirmPassword: ''
  };

  onSubmit() {
    if (!this.formData.name || !this.formData.email || !this.formData.password || !this.formData.role) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    if (this.formData.password !== this.formData.confirmPassword) {
      this.errorMessage = 'Les mots de passe ne correspondent pas';
      return;
    }

    if (this.formData.password.length < 8) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 8 caractères';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const userData = {
      name: this.formData.name,
      email: this.formData.email,
      phone: this.formData.phone,
      role: this.formData.role,
      password: this.formData.password
    };

    this.authService.register(userData).subscribe({
      next: (response) => {
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Erreur lors de l\'inscription. Veuillez réessayer.';
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

  toggleConfirmPassword() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }
}
