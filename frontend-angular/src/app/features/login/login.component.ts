import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username = '';
  password = '';
  showPassword = false;
  rememberMe = false;
  loading = false;

  constructor(private router: Router) {}

  ngOnInit() {
    const savedEmail = localStorage.getItem('savedEmail');
    if (savedEmail) {
      this.username = savedEmail;
      this.rememberMe = true;
    }
  }

  onSubmit() {
    console.log('Form submitted!', this.username, this.password); // Debug
    
    if (!this.username.trim() || !this.password.trim()) {
      alert('Veuillez remplir tous les champs');
      return;
    }

    this.loading = true;
    console.log('Setting authenticated...'); // Debug

    // Simuler une connexion
    setTimeout(() => {
      if (this.rememberMe) {
        localStorage.setItem('savedEmail', this.username);
      }
      localStorage.setItem('isAuthenticated', 'true');
      console.log('Navigating to dashboard...'); // Debug
      this.router.navigate(['/dashboard']).then(success => {
        console.log('Navigation success:', success); // Debug
        this.loading = false;
      }).catch(err => {
        console.error('Navigation error:', err); // Debug
        this.loading = false;
      });
    }, 500); // Réduit à 500ms pour être plus rapide
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }
}
