import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ProfileUpdateRequest {
  name: string;
  email: string;
  phone: string;
  address: string;
  bio: string;
  department: string;
  specialization: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  private apiUrl = `${environment.apiUrl}/profile`;

  constructor(private http: HttpClient) {}

  updateProfile(data: ProfileUpdateRequest): Observable<any> {
    return this.http.put(`${this.apiUrl}/update`, data);
  }

  changePassword(data: PasswordChangeRequest): Observable<any> {
    return this.http.put(`${this.apiUrl}/change-password`, data);
  }
}