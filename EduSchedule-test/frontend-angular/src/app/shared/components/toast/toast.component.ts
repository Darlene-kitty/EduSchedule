import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      @for (t of toast.toasts(); track t.id) {
        <div class="toast toast--{{ t.type }}">
          <span class="toast__icon">{{ icons[t.type] }}</span>
          <span class="toast__msg">{{ t.message }}</span>
          <button class="toast__close" (click)="toast.dismiss(t.id)">✕</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      display: flex;
      flex-direction: column;
      gap: .5rem;
      z-index: 9999;
      max-width: 380px;
    }
    .toast {
      display: flex;
      align-items: center;
      gap: .75rem;
      padding: .75rem 1rem;
      border-radius: 8px;
      color: #fff;
      font-size: .875rem;
      box-shadow: 0 4px 12px rgba(0,0,0,.2);
      animation: slideIn .2s ease;
    }
    .toast--error   { background: #ef4444; }
    .toast--warning { background: #f59e0b; }
    .toast--success { background: #22c55e; }
    .toast--info    { background: #3b82f6; }
    .toast__msg  { flex: 1; }
    .toast__close {
      background: none; border: none; color: #fff;
      cursor: pointer; font-size: 1rem; opacity: .8;
    }
    .toast__close:hover { opacity: 1; }
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to   { transform: translateX(0);    opacity: 1; }
    }
  `]
})
export class ToastComponent {
  toast = inject(ToastService);
  icons: Record<string, string> = {
    error: '✖', warning: '⚠', success: '✔', info: 'ℹ'
  };
}
