import { Injectable, signal } from '@angular/core';

export interface PopupOptions {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
}

export interface PopupState {
  mode: 'confirm' | 'info';
  title: string;
  message: string;
  confirmText: string;
  cancelText?: string;
  resolve: (result: boolean) => void;
}

@Injectable({ providedIn: 'root' })
export class PopupService {
  readonly state = signal<PopupState | null>(null);

  confirm(options: PopupOptions): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.state.set({
        mode: 'confirm',
        title: options.title,
        message: options.message,
        confirmText: options.confirmText ?? 'Confirmar',
        cancelText: options.cancelText ?? 'Cancelar',
        resolve
      });
    });
  }

  info(options: PopupOptions): Promise<void> {
    return new Promise<void>((resolve) => {
      this.state.set({
        mode: 'info',
        title: options.title,
        message: options.message,
        confirmText: options.confirmText ?? 'Entendido',
        resolve: () => resolve()
      });
    });
  }

  onConfirm(): void {
    const current = this.state();
    if (!current) {
      return;
    }
    current.resolve(true);
    this.state.set(null);
  }

  onCancel(): void {
    const current = this.state();
    if (!current) {
      return;
    }
    current.resolve(false);
    this.state.set(null);
  }
}
