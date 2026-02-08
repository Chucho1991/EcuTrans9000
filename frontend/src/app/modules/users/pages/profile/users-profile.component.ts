import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

import { PopupService } from '../../../../core/services/popup.service';
import { UsersService } from '../../services/users.service';

const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password')?.value ?? '';
  const confirmPassword = control.get('confirmPassword')?.value ?? '';
  if (!password && !confirmPassword) {
    return null;
  }
  return password === confirmPassword ? null : { passwordMismatch: true };
};

@Component({
  selector: 'app-users-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="mx-auto max-w-3xl space-y-6">
      <header>
        <h1 class="text-2xl font-semibold text-gray-900 dark:text-white">Mi perfil</h1>
        <p class="text-sm text-gray-500 dark:text-gray-400">Actualiza tus datos personales y password.</p>
      </header>

      <article class="rounded-2xl border border-gray-200 bg-white p-6 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900">
        <form class="grid gap-4 sm:grid-cols-2" [formGroup]="form" (ngSubmit)="save()">
          <div class="sm:col-span-2">
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Nombres</label>
            <input class="h-10 w-full rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="nombres" />
            <p class="mt-1 text-xs text-error-600" *ngIf="showError('nombres', 'required')">
              Nombres es obligatorio. Escribe tu nombre completo.
            </p>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Correo</label>
            <input class="h-10 w-full rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="correo" />
            <p class="mt-1 text-xs text-error-600" *ngIf="showError('correo', 'required')">
              Correo es obligatorio. Ingresa tu correo de contacto.
            </p>
            <p class="mt-1 text-xs text-error-600" *ngIf="showError('correo', 'email')">
              Correo inválido. Usa un formato como usuario&#64;dominio.com.
            </p>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Username</label>
            <input class="h-10 w-full rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="username" />
            <p class="mt-1 text-xs text-error-600" *ngIf="showError('username', 'required')">
              Username es obligatorio. Define tu identificador de acceso.
            </p>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Nueva password (opcional)</label>
            <input type="password" class="h-10 w-full rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="password" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Confirmar password</label>
            <input type="password" class="h-10 w-full rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="confirmPassword" />
            <p class="mt-1 text-xs text-error-600" *ngIf="showPasswordMismatch()">
              Las contraseñas no coinciden. Verifica ambos campos.
            </p>
          </div>
          <div class="sm:col-span-2 flex items-center gap-3">
            <button class="rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-600" type="submit">Guardar perfil</button>
            <span class="text-sm text-gray-600 dark:text-gray-300" *ngIf="message">{{ message }}</span>
          </div>
        </form>
      </article>
    </section>
  `
})
export class UsersProfileComponent {
  private readonly usersService = inject(UsersService);
  private readonly popupService = inject(PopupService);
  private readonly fb = inject(FormBuilder);
  protected message = '';

  protected readonly form = this.fb.group({
    nombres: ['', [Validators.required]],
    correo: ['', [Validators.required, Validators.email]],
    username: ['', [Validators.required]],
    password: [''],
    confirmPassword: ['']
  }, {
    validators: [passwordMatchValidator]
  });

  constructor() {
    this.usersService.me().subscribe((me) => {
      this.form.patchValue({
        nombres: me.nombres,
        correo: me.correo,
        username: me.username
      });
    });
  }

  protected async save(): Promise<void> {
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      if (this.form.hasError('passwordMismatch')) {
        void this.popupService.info({
          title: 'Validación de contraseña',
          message: 'Las contraseñas no coinciden. Verifica los campos de contraseña.'
        });
      }
      this.form.markAllAsTouched();
      return;
    }
    const confirmed = await this.popupService.confirm({
      title: 'Confirmar actualización',
      message: 'Vas a actualizar tu perfil. ¿Deseas continuar?'
    });
    if (!confirmed) {
      return;
    }
    const value = this.form.getRawValue();
    this.usersService
      .updateMe({
        nombres: value.nombres ?? '',
        correo: value.correo ?? '',
        username: value.username ?? '',
        password: value.password || undefined,
        confirmPassword: value.confirmPassword || undefined
      })
      .subscribe(() => {
        this.message = 'Perfil actualizado correctamente.';
        void this.popupService.info({
          title: 'Perfil actualizado',
          message: 'Perfil actualizado correctamente.'
        });
      });
  }

  protected showError(controlName: string, errorName: string): boolean {
    const control = this.form.get(controlName);
    if (!control) {
      return false;
    }
    return control.hasError(errorName) && (control.touched || control.dirty);
  }

  protected showPasswordMismatch(): boolean {
    const passwordControl = this.form.controls.password;
    const confirmPasswordControl = this.form.controls.confirmPassword;
    const hasInteraction =
      passwordControl.touched ||
      passwordControl.dirty ||
      confirmPasswordControl.touched ||
      confirmPasswordControl.dirty;
    return this.form.hasError('passwordMismatch') && hasInteraction;
  }
}
