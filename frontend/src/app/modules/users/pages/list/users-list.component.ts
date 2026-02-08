import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

import { UsersService, UserResponse } from '../../services/users.service';
import { PopupService } from '../../../../core/services/popup.service';

const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password')?.value ?? '';
  const confirmPassword = control.get('confirmPassword')?.value ?? '';
  if (!password && !confirmPassword) {
    return null;
  }
  return password === confirmPassword ? null : { passwordMismatch: true };
};

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="space-y-6">
      <header class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="text-2xl font-semibold text-gray-900 dark:text-white">Usuarios</h1>
          <p class="text-sm text-gray-500 dark:text-gray-400">Gestion completa del modulo de usuarios.</p>
        </div>
        <button
          class="basis-full rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-600 sm:basis-auto"
          type="button"
          (click)="startCreate()"
        >
          Nuevo usuario
        </button>
      </header>

      <article class="rounded-2xl border border-gray-200 bg-white p-4 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900">
        <form class="grid gap-3 md:grid-cols-2 lg:grid-cols-4" [formGroup]="filtersForm" (ngSubmit)="loadUsers(0)">
          <select class="h-10 rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="rol">
            <option value="">Rol: todos</option>
            <option value="SUPERADMINISTRADOR">SUPERADMINISTRADOR</option>
            <option value="REGISTRADOR">REGISTRADOR</option>
          </select>
          <select class="h-10 rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="activo">
            <option value="">Estado: todos</option>
            <option value="true">Activos</option>
            <option value="false">Inactivos</option>
          </select>
          <select class="h-10 rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="deleted">
            <option value="">Eliminacion: todos</option>
            <option value="false">No eliminados</option>
            <option value="true">Eliminados</option>
          </select>
          <button class="h-10 rounded-lg border border-gray-300 text-sm font-medium hover:bg-gray-100 dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800" type="submit">Filtrar</button>
        </form>
      </article>

      <article class="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-theme-sm dark:border-gray-800 dark:bg-gray-900">
        <div class="overflow-x-auto lg:overflow-x-visible">
        <table class="table-auto min-w-[780px] text-left text-xs sm:text-sm lg:min-w-full">
          <thead class="border-b border-gray-200 bg-gray-50 text-gray-600 dark:border-gray-800 dark:bg-gray-950 dark:text-gray-300">
            <tr>
              <th class="px-3 py-3 sm:px-4">Username</th>
              <th class="px-3 py-3 sm:px-4">Nombres</th>
              <th class="px-3 py-3 sm:px-4">Correo</th>
              <th class="px-3 py-3 sm:px-4">Rol</th>
              <th class="px-3 py-3 sm:px-4">Estado</th>
              <th class="px-3 py-3 sm:px-4">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr class="border-b border-gray-100 dark:border-gray-800" *ngFor="let user of users">
              <td class="px-3 py-3 sm:px-4">{{ user.username }}</td>
              <td class="px-3 py-3 sm:px-4">{{ user.nombres }}</td>
              <td class="break-all px-3 py-3 sm:px-4">{{ user.correo }}</td>
              <td class="px-3 py-3 sm:px-4">{{ user.rol }}</td>
              <td class="px-3 py-3 sm:px-4">
                <span
                  *ngIf="user.deleted"
                  class="inline-flex rounded-full border border-orange-200 bg-orange-50 px-2.5 py-1 text-xs font-semibold text-orange-700 dark:border-orange-900/40 dark:bg-orange-900/20 dark:text-orange-300"
                >
                  ELIMINADO
                </span>
                <span
                  *ngIf="!user.deleted && user.activo"
                  class="inline-flex rounded-full border border-green-200 bg-green-50 px-2.5 py-1 text-xs font-semibold text-green-700 dark:border-green-900/40 dark:bg-green-900/20 dark:text-green-300"
                >
                  ACTIVO
                </span>
                <span
                  *ngIf="!user.deleted && !user.activo"
                  class="inline-flex rounded-full border border-red-200 bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-700 dark:border-red-900/40 dark:bg-red-900/20 dark:text-red-300"
                >
                  INACTIVO
                </span>
              </td>
              <td class="px-3 py-3 sm:px-4">
                <div class="flex flex-wrap gap-2">
                  <button class="icon-action-btn" type="button" aria-label="Ver usuario" (click)="selectDetail(user)">
                    <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.8"/></svg>
                    <span class="icon-action-tooltip">Ver</span>
                  </button>
                  <button class="icon-action-btn" type="button" aria-label="Editar usuario" (click)="startEdit(user)">
                    <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="m3 21 3.8-1 10-10a2.1 2.1 0 0 0-3-3l-10 10L3 21Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/><path d="m13.5 6.5 3 3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                    <span class="icon-action-tooltip">Editar</span>
                  </button>
                  <button
                    *ngIf="!user.deleted && !user.activo"
                    class="icon-action-btn text-green-600 hover:text-green-700"
                    type="button"
                    aria-label="Activar usuario"
                    (click)="activate(user)"
                  >
                    <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M5 12h14M12 5v14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                    <span class="icon-action-tooltip">Activar</span>
                  </button>
                  <button
                    *ngIf="!user.deleted && user.activo"
                    class="icon-action-btn text-red-600 hover:text-red-700"
                    type="button"
                    aria-label="Inhabilitar usuario"
                    (click)="deactivate(user)"
                  >
                    <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M5 12h14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                    <span class="icon-action-tooltip">Inhabilitar</span>
                  </button>
                  <button
                    *ngIf="!user.deleted"
                    class="icon-action-btn text-orange-600 hover:text-orange-700"
                    type="button"
                    aria-label="Soft delete"
                    (click)="softDelete(user)"
                  >
                    <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M3 6h18M8 6V4h8v2m-9 0 1 14h8l1-14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                    <span class="icon-action-tooltip">Soft delete</span>
                  </button>
                  <button
                    *ngIf="user.deleted"
                    class="icon-action-btn text-green-600 hover:text-green-700"
                    type="button"
                    aria-label="Restaurar usuario"
                    (click)="restore(user)"
                  >
                    <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M3 12a9 9 0 1 0 2.6-6.4M3 4v5h5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                    <span class="icon-action-tooltip">Restore</span>
                  </button>
                </div>
              </td>
            </tr>
            <tr *ngIf="users.length === 0">
              <td class="px-4 py-4 text-center text-gray-500 dark:text-gray-400" colspan="6">No hay usuarios para mostrar.</td>
            </tr>
          </tbody>
        </table>
        </div>
        <div class="flex flex-wrap items-center justify-between gap-2 px-4 py-3 text-sm text-gray-600 dark:text-gray-300">
          <span class="basis-full sm:basis-auto">Pagina {{ page + 1 }} de {{ totalPages || 1 }}</span>
          <div class="flex grow gap-2 sm:grow-0">
            <button class="rounded border border-gray-300 px-3 py-1 disabled:opacity-50 dark:border-gray-700 dark:hover:bg-gray-800" type="button" (click)="loadUsers(page - 1)" [disabled]="page === 0">
              Anterior
            </button>
            <button class="rounded border border-gray-300 px-3 py-1 disabled:opacity-50 dark:border-gray-700 dark:hover:bg-gray-800" type="button" (click)="loadUsers(page + 1)" [disabled]="page + 1 >= totalPages">
              Siguiente
            </button>
          </div>
        </div>
      </article>

      <article class="rounded-2xl border border-gray-200 bg-white p-4 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900 sm:p-6" *ngIf="selectedUser">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Detalle de usuario</h2>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-300"><strong>ID:</strong> {{ selectedUser.id }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Username:</strong> {{ selectedUser.username }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Nombres:</strong> {{ selectedUser.nombres }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Correo:</strong> {{ selectedUser.correo }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Rol:</strong> {{ selectedUser.rol }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300">
          <strong>Estado:</strong>
          <span
            *ngIf="selectedUser.deleted"
            class="ml-1 inline-flex rounded-full border border-orange-200 bg-orange-50 px-2 py-0.5 text-xs font-semibold text-orange-700 dark:border-orange-900/40 dark:bg-orange-900/20 dark:text-orange-300"
          >
            ELIMINADO
          </span>
          <span
            *ngIf="!selectedUser.deleted && selectedUser.activo"
            class="ml-1 inline-flex rounded-full border border-green-200 bg-green-50 px-2 py-0.5 text-xs font-semibold text-green-700 dark:border-green-900/40 dark:bg-green-900/20 dark:text-green-300"
          >
            ACTIVO
          </span>
          <span
            *ngIf="!selectedUser.deleted && !selectedUser.activo"
            class="ml-1 inline-flex rounded-full border border-red-200 bg-red-50 px-2 py-0.5 text-xs font-semibold text-red-700 dark:border-red-900/40 dark:bg-red-900/20 dark:text-red-300"
          >
            INACTIVO
          </span>
        </p>
      </article>

      <article class="rounded-2xl border border-gray-200 bg-white p-4 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900 sm:p-6" *ngIf="mode !== 'none'">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ mode === 'create' ? 'Crear usuario' : 'Editar usuario' }}</h2>
        <form class="mt-4 mx-auto grid max-w-5xl gap-4 md:grid-cols-2 xl:grid-cols-3" [formGroup]="userForm" (ngSubmit)="submitUser()">
          <div class="md:col-span-2 xl:col-span-3">
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Nombres</label>
            <input class="h-10 rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="nombres" />
            <p class="mt-1 text-xs text-error-600" *ngIf="showError('nombres', 'required')">
              Nombres es obligatorio. Escribe el nombre completo del usuario.
            </p>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Correo</label>
            <input class="h-10 rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="correo" />
            <p class="mt-1 text-xs text-error-600" *ngIf="showError('correo', 'required')">
              Correo es obligatorio. Ingresa un email válido del usuario.
            </p>
            <p class="mt-1 text-xs text-error-600" *ngIf="showError('correo', 'email')">
              Correo inválido. Usa un formato como usuario&#64;dominio.com.
            </p>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Username</label>
            <input class="h-10 rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="username" />
            <p class="mt-1 text-xs text-error-600" *ngIf="showError('username', 'required')">
              Username es obligatorio. Define el identificador de inicio de sesión.
            </p>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Rol</label>
            <select class="h-10 rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="rol">
              <option value="SUPERADMINISTRADOR">SUPERADMINISTRADOR</option>
              <option value="REGISTRADOR">REGISTRADOR</option>
            </select>
            <p class="mt-1 text-xs text-error-600" *ngIf="showError('rol', 'required')">
              Rol es obligatorio. Selecciona el nivel de permisos del usuario.
            </p>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Activo</label>
            <select class="h-10 rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="activo">
              <option [ngValue]="true">SI</option>
              <option [ngValue]="false">NO</option>
            </select>
          </div>
          <ng-container *ngIf="mode === 'create'">
            <div>
              <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Password</label>
              <input type="password" class="h-10 rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="password" />
              <p class="mt-1 text-xs text-error-600" *ngIf="showError('password', 'required') && mode === 'create'">
                Password es obligatorio. Ingresa una contraseña para el nuevo usuario.
              </p>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Confirmar password</label>
              <input type="password" class="h-10 rounded-lg border border-gray-300 px-3 text-sm dark:border-gray-700 dark:bg-gray-950 dark:text-gray-100" formControlName="confirmPassword" />
              <p class="mt-1 text-xs text-error-600" *ngIf="showError('confirmPassword', 'required') && mode === 'create'">
                Confirmar password es obligatorio. Repite la contraseña.
              </p>
              <p class="mt-1 text-xs text-error-600" *ngIf="showPasswordMismatch()">
                Las contraseñas no coinciden. Verifica ambos campos.
              </p>
            </div>
          </ng-container>
          <div class="md:col-span-2 xl:col-span-3 flex flex-wrap items-center gap-3">
            <button class="basis-full rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-600 sm:basis-auto" type="submit">
              {{ mode === 'create' ? 'Crear' : 'Guardar cambios' }}
            </button>
            <button class="basis-full rounded-lg border border-gray-300 px-4 py-2 text-sm dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800 sm:basis-auto" type="button" (click)="cancelForm()">Cancelar</button>
          </div>
        </form>
      </article>
    </section>
  `
})
export class UsersListComponent {
  private readonly usersService = inject(UsersService);
  private readonly fb = inject(FormBuilder);
  private readonly popupService = inject(PopupService);

  protected users: UserResponse[] = [];
  protected selectedUser: UserResponse | null = null;
  protected mode: 'none' | 'create' | 'edit' = 'none';
  protected editingId: string | null = null;
  protected page = 0;
  protected readonly size = 10;
  protected totalPages = 0;

  protected readonly filtersForm = this.fb.nonNullable.group({
    rol: [''],
    activo: [''],
    deleted: ['']
  });

  protected readonly userForm = this.fb.group({
    nombres: ['', [Validators.required]],
    correo: ['', [Validators.required, Validators.email]],
    username: ['', [Validators.required]],
    rol: ['REGISTRADOR', [Validators.required]],
    activo: [true, [Validators.required]],
    password: [''],
    confirmPassword: ['']
  }, {
    validators: [passwordMatchValidator]
  });

  constructor() {
    this.loadUsers(0);
  }

  protected loadUsers(page: number): void {
    const safePage = page < 0 ? 0 : page;
    const filters = this.filtersForm.getRawValue();
    this.usersService
      .list({
        page: safePage,
        size: this.size,
        rol: filters.rol || undefined,
        activo: filters.activo === '' ? null : filters.activo === 'true',
        deleted: filters.deleted === '' ? null : filters.deleted === 'true'
      })
      .subscribe({
        next: (response) => {
          this.users = response.content;
          this.page = response.page;
          this.totalPages = response.totalPages;
        }
      });
  }

  protected selectDetail(user: UserResponse): void {
    this.usersService.getById(user.id).subscribe((detail) => (this.selectedUser = detail));
  }

  protected startCreate(): void {
    this.mode = 'create';
    this.editingId = null;
    this.userForm.controls.password.setValidators([Validators.required]);
    this.userForm.controls.confirmPassword.setValidators([Validators.required]);
    this.userForm.controls.password.updateValueAndValidity();
    this.userForm.controls.confirmPassword.updateValueAndValidity();
    this.userForm.reset({
      nombres: '',
      correo: '',
      username: '',
      rol: 'REGISTRADOR',
      activo: true,
      password: '',
      confirmPassword: ''
    });
  }

  protected startEdit(user: UserResponse): void {
    this.mode = 'edit';
    this.editingId = user.id;
    this.userForm.controls.password.clearValidators();
    this.userForm.controls.confirmPassword.clearValidators();
    this.userForm.controls.password.updateValueAndValidity();
    this.userForm.controls.confirmPassword.updateValueAndValidity();
    this.userForm.reset({
      nombres: user.nombres,
      correo: user.correo,
      username: user.username,
      rol: user.rol,
      activo: user.activo,
      password: '',
      confirmPassword: ''
    });
  }

  protected cancelForm(): void {
    this.mode = 'none';
    this.editingId = null;
  }

  protected async submitUser(): Promise<void> {
    this.userForm.markAllAsTouched();
    if (this.userForm.invalid) {
      if (this.mode === 'create' && this.userForm.hasError('passwordMismatch')) {
        void this.popupService.info({
          title: 'Validación de contraseña',
          message: 'Las contraseñas no coinciden. Verifica los campos de contraseña.'
        });
      }
      return;
    }
    const value = this.userForm.getRawValue();
    if (this.mode === 'create') {
      const confirmedCreate = await this.popupService.confirm({
        title: 'Crear usuario',
        message: 'Vas a crear un nuevo usuario. ¿Deseas continuar?'
      });
      if (!confirmedCreate) {
        return;
      }
      this.usersService
        .create({
          nombres: value.nombres ?? '',
          correo: value.correo ?? '',
          username: value.username ?? '',
          rol: value.rol ?? 'REGISTRADOR',
          activo: value.activo ?? true,
          password: value.password ?? '',
          confirmPassword: value.confirmPassword ?? ''
        })
        .subscribe(() => {
          void this.popupService.info({
            title: 'Usuario creado',
            message: 'Usuario creado correctamente.'
          });
          this.cancelForm();
          this.loadUsers(this.page);
        });
      return;
    }
    if (this.mode === 'edit' && this.editingId) {
      const confirmedEdit = await this.popupService.confirm({
        title: 'Editar usuario',
        message: 'Vas a editar este usuario. ¿Deseas continuar?'
      });
      if (!confirmedEdit) {
        return;
      }
      this.usersService
        .update(this.editingId, {
          nombres: value.nombres ?? '',
          correo: value.correo ?? '',
          username: value.username ?? '',
          rol: value.rol ?? 'REGISTRADOR',
          activo: value.activo ?? true
        })
        .subscribe(() => {
          void this.popupService.info({
            title: 'Usuario editado',
            message: 'Usuario editado correctamente.'
          });
          this.cancelForm();
          this.loadUsers(this.page);
        });
    }
  }

  protected async softDelete(user: UserResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Eliminación lógica',
      message: `Vas a eliminar lógicamente al usuario "${user.username}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.usersService.softDelete(user.id).subscribe(() => {
      void this.popupService.info({
        title: 'Usuario eliminado',
        message: 'Usuario eliminado lógicamente.'
      });
      this.loadUsers(this.page);
    });
  }

  protected async restore(user: UserResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Restaurar usuario',
      message: `Vas a restaurar al usuario "${user.username}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.usersService.restore(user.id).subscribe(() => {
      void this.popupService.info({
        title: 'Usuario restaurado',
        message: 'Usuario restaurado correctamente.'
      });
      this.loadUsers(this.page);
    });
  }

  protected async activate(user: UserResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Activar usuario',
      message: `Vas a activar al usuario "${user.username}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.usersService.activate(user.id).subscribe(() => {
      void this.popupService.info({
        title: 'Usuario activado',
        message: 'Usuario activado correctamente.'
      });
      this.loadUsers(this.page);
    });
  }

  protected async deactivate(user: UserResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Inhabilitar usuario',
      message: `Vas a inhabilitar al usuario "${user.username}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.usersService.deactivate(user.id).subscribe(() => {
      void this.popupService.info({
        title: 'Usuario inhabilitado',
        message: 'Usuario inhabilitado correctamente.'
      });
      this.loadUsers(this.page);
    });
  }

  protected showError(controlName: string, errorName: string): boolean {
    const control = this.userForm.get(controlName);
    if (!control) {
      return false;
    }
    return control.hasError(errorName) && (control.touched || control.dirty);
  }

  protected showPasswordMismatch(): boolean {
    const passwordControl = this.userForm.controls.password;
    const confirmPasswordControl = this.userForm.controls.confirmPassword;
    const hasInteraction =
      passwordControl.touched ||
      passwordControl.dirty ||
      confirmPasswordControl.touched ||
      confirmPasswordControl.dirty;
    return this.mode === 'create' && this.userForm.hasError('passwordMismatch') && hasInteraction;
  }
}
