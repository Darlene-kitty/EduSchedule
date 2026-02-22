import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPasswordComponent {
  email = '';
  loading = false;
  message = '';
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient, private router: Router) {}

  onSubmit() {
    if (!this.email) return;
    this.loading = true;
    this.http.post(`${this.apiUrl}/forgot-password`, { email: this.email }).subscribe({
      next: () => {
        this.message = 'Un lien de réinitialisation a été envoyé à votre adresse e-mail.';
        this.loading = false;
      },
      error: (err) => {
        this.message = err.error?.message || 'Erreur lors de l’envoi du lien.';
        this.loading = false;
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
