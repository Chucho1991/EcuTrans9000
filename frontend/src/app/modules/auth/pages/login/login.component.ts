import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { PopupService } from '../../../../core/services/popup.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="relative min-h-screen overflow-hidden bg-gray-50 dark:bg-gray-950">
      <div class="absolute inset-0 bg-gradient-to-b from-brand-25 to-transparent dark:from-gray-900 dark:to-gray-950"></div>
      <div class="relative mx-auto flex min-h-screen w-full max-w-6xl items-center px-4 py-10 sm:px-6">
        <div class="grid w-full items-center gap-10 lg:grid-cols-2">
          <div class="hidden rounded-3xl bg-white/80 p-10 shadow-theme-lg backdrop-blur lg:block dark:bg-gray-900/80">
            <h1 class="text-title-sm font-semibold text-gray-900 dark:text-white">EcuTrans9000</h1>
            <p class="mt-4 text-theme-xl text-gray-600 dark:text-gray-300">
              Plataforma para gestion de bitacoras, usuarios y operacion de flota.
            </p>
          </div>

          <div class="w-full rounded-3xl border border-gray-200 bg-white p-6 shadow-theme-lg sm:p-8 dark:border-gray-800 dark:bg-gray-900">
            <h2 class="text-2xl font-semibold text-gray-900 dark:text-white">Iniciar sesion</h2>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Ingresa con tu username o correo.</p>

            <form class="mt-6 space-y-4" [formGroup]="form" (ngSubmit)="onSubmit()" novalidate>
              <div>
                <label for="usernameOrEmail" class="mb-2 block text-sm font-medium text-gray-700 dark:text-gray-300">Usuario o correo</label>
                <input
                  id="usernameOrEmail"
                  type="text"
                  autocomplete="username"
                  formControlName="usernameOrEmail"
                  class="h-11 w-full rounded-lg border border-gray-300 px-3 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100 dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100"
                  [attr.aria-invalid]="hasError('usernameOrEmail')"
                  aria-describedby="usernameOrEmailError"
                />
                <p id="usernameOrEmailError" class="mt-1 text-xs text-error-600" *ngIf="hasError('usernameOrEmail')">
                  Usuario o correo es obligatorio. Ingresa tu username o email registrado.
                </p>
              </div>
              <div>
                <label for="password" class="mb-2 block text-sm font-medium text-gray-700 dark:text-gray-300">Contrasena</label>
                <input
                  id="password"
                  type="password"
                  autocomplete="current-password"
                  formControlName="password"
                  class="h-11 w-full rounded-lg border border-gray-300 px-3 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100 dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100"
                  [attr.aria-invalid]="hasError('password')"
                  aria-describedby="passwordError"
                />
                <p id="passwordError" class="mt-1 text-xs text-error-600" *ngIf="hasError('password')">
                  Contrasena es obligatoria. Ingresa tu contrasena de acceso.
                </p>
              </div>
              <p class="rounded-lg bg-error-50 px-3 py-2 text-sm text-error-700" role="alert" aria-live="polite" *ngIf="errorMessage">
                {{ errorMessage }}
              </p>
              <button
                type="submit"
                class="h-11 w-full rounded-lg bg-brand-500 text-sm font-semibold text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-70"
                [disabled]="loading"
              >
                {{ loading ? 'Ingresando...' : 'Entrar' }}
              </button>
            </form>
          </div>
        </div>
      </div>
    </section>
  `
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly popupService = inject(PopupService);

  protected loading = false;
  protected errorMessage = '';
  protected form = this.fb.nonNullable.group({
    usernameOrEmail: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  protected hasError(controlName: 'usernameOrEmail' | 'password'): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (this.form.invalid || this.loading) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage = '';
    this.loading = true;
    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading = false;
        void this.popupService.info({
          title: 'Inicio de sesión',
          message: 'Inicio de sesión exitoso.'
        });
        const role = this.authService.getRole();
        this.router.navigateByUrl(role === 'SUPERADMINISTRADOR' ? '/dashboard' : '/profile');
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No fue posible iniciar sesion. Verifica tus credenciales.';
      }
    });
  }
}
