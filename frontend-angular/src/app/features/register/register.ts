import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  showPassword = false;
  showConfirmPassword = false;
  loading = false;
  
  formData = {
    name: '',
    email: '',
    phone: '',
    role: '',
    password: '',
    confirmPassword: ''
  };

  constructor(private router: Router) {}

  onSubmit() {
    if (!this.formData.name || !this.formData.email || !this.formData.password || !this.formData.role) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (this.formData.password !== this.formData.confirmPassword) {
      alert('Les mots de passe ne correspondent pas');
      return;
    }

    if (this.formData.password.length < 8) {
      alert('Le mot de passe doit contenir au moins 8 caractères');
      return;
    }

    this.loading = true;

    // Simuler une inscription
    setTimeout(() => {
      localStorage.setItem('isAuthenticated', 'true');
      this.router.navigate(['/dashboard']);
    }, 1000);
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }
}

