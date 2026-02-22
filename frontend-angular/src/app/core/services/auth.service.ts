import { BehaviorSubject } from 'rxjs';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

   private userSubject = new BehaviorSubject<any>(null);
  user$ = this.userSubject.asObservable();

  setUser(user: any) {
    this.userSubject.next(user);
  }

  getUser() {
    return this.userSubject.value;
  }
  
  isAuthenticated(): boolean {
    // Vérifier si l'utilisateur est connecté
    return localStorage.getItem('isAuthenticated') === 'true';
  }

  login(username: string, password: string): boolean {
    // Simuler une connexion (accepter n'importe quel username/password)
    localStorage.setItem('isAuthenticated', 'true');
    localStorage.setItem('username', username);
    return true;
  }

  logout(): void {
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('username');
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }
}
