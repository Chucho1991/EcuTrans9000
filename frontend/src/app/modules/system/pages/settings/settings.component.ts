import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';

import {
  ModuleAccessSettingsService,
  RoleModuleAccessResponse
} from '../../services/module-access-settings.service';
import { PopupService } from '../../../../core/services/popup.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="min-w-0 w-full space-y-6">
      <header class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 class="page-title">Configuracion de modulos</h1>
          <p class="page-subtitle">Define a que modulos funcionales pueden ingresar los roles distintos de SUPERADMINISTRADOR.</p>
        </div>
        <div class="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:border-amber-900/40 dark:bg-amber-950/30 dark:text-amber-200">
          Dashboard, Usuarios y Configuracion siguen reservados para SUPERADMINISTRADOR.
        </div>
      </header>

      <article class="panel-card p-5" *ngIf="loading">
        <p class="text-sm text-gray-600 dark:text-gray-300">Cargando configuracion de accesos...</p>
      </article>

      <article class="panel-card p-5" *ngIf="!loading && roleConfigs.length === 0">
        <p class="text-sm text-gray-600 dark:text-gray-300">No hay roles adicionales configurables.</p>
      </article>

      <article class="panel-card p-5 sm:p-6" *ngFor="let roleConfig of roleConfigs">
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ roleConfig.role }}</h2>
            <p class="mt-1 text-sm text-gray-600 dark:text-gray-300">Activa o desactiva los modulos visibles y navegables para este rol.</p>
          </div>
          <button
            class="btn-primary-brand basis-full sm:basis-auto disabled:cursor-not-allowed disabled:opacity-60"
            type="button"
            [disabled]="isSaving(roleConfig.role)"
            (click)="saveRole(roleConfig)"
          >
            {{ isSaving(roleConfig.role) ? 'Guardando...' : 'Guardar cambios' }}
          </button>
        </div>

        <div class="mt-5 grid grid-cols-1 gap-3 lg:grid-cols-2">
          <label
            *ngFor="let module of roleConfig.modules"
            class="flex items-start justify-between gap-3 rounded-2xl border border-gray-200 bg-gray-50 px-4 py-4 text-sm dark:border-gray-800 dark:bg-gray-950/60"
          >
            <div class="min-w-0">
              <p class="font-semibold text-gray-900 dark:text-white">{{ module.moduleName }}</p>
              <p class="mt-1 text-xs uppercase tracking-wide text-gray-500 dark:text-gray-400">{{ module.moduleKey }}</p>
            </div>
            <input
              class="mt-1 h-4 w-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500"
              type="checkbox"
              [checked]="module.enabled"
              [disabled]="isSaving(roleConfig.role)"
              (change)="toggleModule(roleConfig.role, module.moduleKey, $event)"
            />
          </label>
        </div>
      </article>
    </section>
  `
})
export class SettingsComponent {
  private readonly moduleAccessSettingsService = inject(ModuleAccessSettingsService);
  private readonly popupService = inject(PopupService);

  protected roleConfigs: RoleModuleAccessResponse[] = [];
  protected loading = true;
  private readonly savingRoles = new Set<string>();

  constructor() {
    this.loadConfiguration();
  }

  protected toggleModule(role: string, moduleKey: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.roleConfigs = this.roleConfigs.map((roleConfig) => {
      if (roleConfig.role !== role) {
        return roleConfig;
      }
      return {
        ...roleConfig,
        modules: roleConfig.modules.map((module) =>
          module.moduleKey === moduleKey ? { ...module, enabled: checked } : module)
      };
    });
  }

  protected isSaving(role: string): boolean {
    return this.savingRoles.has(role);
  }

  protected async saveRole(roleConfig: RoleModuleAccessResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Guardar configuracion',
      message: `Vas a actualizar los accesos del rol "${roleConfig.role}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }

    this.savingRoles.add(roleConfig.role);
    this.moduleAccessSettingsService.update(roleConfig.role, {
      modules: roleConfig.modules.map((module) => ({
        moduleKey: module.moduleKey,
        enabled: module.enabled
      }))
    }).subscribe({
      next: async (updated) => {
        this.roleConfigs = this.roleConfigs.map((current) => current.role === updated.role ? updated : current);
        this.savingRoles.delete(roleConfig.role);
        await this.popupService.info({
          title: 'Configuracion actualizada',
          message: `Los accesos del rol "${updated.role}" se guardaron correctamente.`
        });
      },
      error: () => {
        this.savingRoles.delete(roleConfig.role);
      }
    });
  }

  private loadConfiguration(): void {
    this.moduleAccessSettingsService.list().subscribe({
      next: (response) => {
        this.roleConfigs = response;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
