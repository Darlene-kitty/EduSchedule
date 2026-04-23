import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { StorageService } from './storage.service';

export interface ProfileUpdateRequest {
  username: string;
  email: string;
  role?: string;
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
  private api = inject(ApiService);
  private storageService = inject(StorageService);

  updateProfile(data: ProfileUpdateRequest): Observable<any> {
    return this.api.put<any>('/users/profile', data).pipe(
      tap((updated: any) => {
        const current = this.storageService.getUser();
        this.storageService.setUser({ ...current, ...updated });
      })
    );
  }

  changePassword(data: PasswordChangeRequest): Observable<any> {
    return this.api.post<any>('/users/change-password', data);
  }
}
