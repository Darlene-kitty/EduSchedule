import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reset-password.html',
  styleUrls: ['./reset-password.css']
})
export class ResetPasswordComponent implements OnInit {

  formData = {
    newPassword: '',
    confirmPassword: ''
  };

  showPassword = false;
  showConfirmPassword = false;
  isLoading = false;
  message = '';
  isSuccess = false;
  tokenValid: boolean | null = null;
  token: string | null = null;

  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token');
    if (this.token) {
      this.validateToken(this.token);
    } else {
      this.message = "Token manquant dans l'URL";
      this.tokenValid = false;
    }
  }

  validateToken(token: string): void {
    this.http.get(`${this.apiUrl}/reset-password/validate?token=${token}`)
      .subscribe({
        next: () => {
          this.tokenValid = true;
          this.message = 'Token valide. Vous pouvez définir votre nouveau mot de passe.';
        },
        error: () => {
          this.tokenValid = false;
          this.message = 'Token invalide ou expiré';
        }
      });
  }

  handleSubmit(): void {
    if (this.formData.newPassword !== this.formData.confirmPassword) {
      this.message = 'Les mots de passe ne correspondent pas';
      return;
    }

    if (this.formData.newPassword.length < 6) {
      this.message = 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }

    this.isLoading = true;
    this.message = '';

    this.http.post(`${this.apiUrl}/reset-password`, {
      token: this.token,
      newPassword: this.formData.newPassword,
      confirmPassword: this.formData.confirmPassword
    }).subscribe({
      next: () => {
        this.isSuccess = true;
        this.message = 'Votre mot de passe a été modifié avec succès !';
        this.isLoading = false;
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: (err) => {
        this.message = err.error?.message || 'Erreur lors de la réinitialisation';
        this.isLoading = false;
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}