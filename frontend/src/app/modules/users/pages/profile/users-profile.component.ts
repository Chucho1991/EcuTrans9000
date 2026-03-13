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
        <h1 class="page-title">Mi perfil</h1>
        <p class="page-subtitle">Actualiza tus datos personales y password.</p>
      </header>

      <article class="panel-card p-6">
        <form class="grid gap-4 sm:grid-cols-2" [formGroup]="form" (ngSubmit)="save()">
          <div class="sm:col-span-2">
            <label class="form-label form-label-required">Nombres</label>
            <input class="form-control-sm" formControlName="nombres" />
            <p class="form-error" *ngIf="showError('nombres', 'required')">
              Nombres es obligatorio. Escribe tu nombre completo.
            </p>
          </div>
          <div>
            <label class="form-label form-label-required">Correo</label>
            <input class="form-control-sm" formControlName="correo" />
            <p class="form-error" *ngIf="showError('correo', 'required')">
              Correo es obligatorio. Ingresa tu correo de contacto.
            </p>
            <p class="form-error" *ngIf="showError('correo', 'email')">
              Correo inválido. Usa un formato como usuario&#64;dominio.com.
            </p>
          </div>
          <div>
            <label class="form-label form-label-required">Username</label>
            <input class="form-control-sm" formControlName="username" />
            <p class="form-error" *ngIf="showError('username', 'required')">
              Username es obligatorio. Define tu identificador de acceso.
            </p>
          </div>
          <div>
            <label class="form-label">Nueva password (opcional)</label>
            <input type="password" class="form-control-sm" formControlName="password" />
          </div>
          <div>
            <label class="form-label">Confirmar password</label>
            <input type="password" class="form-control-sm" formControlName="confirmPassword" />
            <p class="form-error" *ngIf="showPasswordMismatch()">
              Las contraseñas no coinciden. Verifica ambos campos.
            </p>
          </div>
          <div class="sm:col-span-2 flex items-center gap-3">
            <button class="btn-primary-brand" type="submit">Guardar perfil</button>
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
