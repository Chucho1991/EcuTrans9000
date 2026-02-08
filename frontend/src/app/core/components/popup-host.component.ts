import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';

import { PopupService } from '../services/popup.service';

@Component({
  selector: 'app-popup-host',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      *ngIf="popupService.state() as popup"
      class="fixed inset-0 z-[99999] flex items-center justify-center bg-gray-900/60 p-4 backdrop-blur-[2px]"
      role="dialog"
      aria-modal="true"
      [attr.aria-label]="popup.title"
    >
      <section class="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-6 shadow-theme-xl dark:border-gray-700 dark:bg-gray-900">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ popup.title }}</h2>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-300">{{ popup.message }}</p>
        <div class="mt-6 flex justify-end gap-2">
          <button
            *ngIf="popup.mode === 'confirm'"
            type="button"
            class="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800"
            (click)="popupService.onCancel()"
          >
            {{ popup.cancelText }}
          </button>
          <button
            type="button"
            class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-600"
            (click)="popupService.onConfirm()"
          >
            {{ popup.confirmText }}
          </button>
        </div>
      </section>
    </div>
  `
})
export class PopupHostComponent {
  protected readonly popupService = inject(PopupService);
}
