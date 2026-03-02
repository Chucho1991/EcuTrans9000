import { CommonModule } from '@angular/common';
import { Component, OnDestroy, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { PopupService } from '../../../../core/services/popup.service';
import { AuthService } from '../../../auth/services/auth.service';
import {
  ClienteImportResult,
  ClienteResponse,
  ClientesService,
  TipoDocumentoCliente
} from '../../services/clientes.service';

@Component({
  selector: 'app-clientes-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <section class="min-w-0 w-full space-y-6">
      <header class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="page-title">Clientes</h1>
          <p class="page-subtitle">Gestion del catalogo de clientes para Bitacora de Viajes.</p>
        </div>
        <div class="flex w-full flex-col gap-2 sm:w-auto sm:flex-row" *ngIf="canManage()">
          <button class="btn-outline-neutral px-4 py-2" type="button" (click)="openImportModal()">Importar Excel</button>
          <button class="btn-primary-brand basis-full sm:basis-auto" type="button" (click)="startCreate()">Nuevo cliente</button>
        </div>
      </header>

      <article class="panel-card min-w-0 w-full max-w-full p-4">
        <form class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4" [formGroup]="filtersForm" (ngSubmit)="loadClientes(0)">
          <input class="filter-control" formControlName="q" placeholder="Buscar documento o nombre" />
          <select *ngIf="canManage()" class="filter-control" formControlName="includeDeleted">
            <option value="false">No eliminados</option>
            <option value="true">Incluir eliminados</option>
          </select>
          <div *ngIf="!canManage()"></div>
          <button class="btn-outline-neutral h-10 w-full rounded-lg font-medium hover:bg-gray-100" type="submit">Filtrar</button>
        </form>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full overflow-hidden">
        <div class="min-w-0 w-full max-w-full overflow-x-auto">
          <table class="w-full table-auto min-w-[860px] text-left text-xs sm:text-sm lg:min-w-full">
            <thead class="border-b border-gray-200 bg-gray-50 text-gray-600 dark:border-gray-800 dark:bg-gray-950 dark:text-gray-300">
              <tr>
                <th class="px-3 py-3 sm:px-4">Documento</th>
                <th class="px-3 py-3 sm:px-4">Logo</th>
                <th class="px-3 py-3 sm:px-4">Nombre</th>
                <th class="px-3 py-3 sm:px-4">Direccion</th>
                <th class="px-3 py-3 sm:px-4">Estado</th>
                <th class="px-3 py-3 sm:px-4">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr class="border-b border-gray-100 dark:border-gray-800" *ngFor="let cliente of clientes">
                <td class="px-3 py-3 sm:px-4">{{ cliente.tipoDocumento }} {{ cliente.documento }}</td>
                <td class="px-3 py-3 sm:px-4">
                  <img
                    *ngIf="logoPreviewUrls[cliente.id]; else sinLogo"
                    [src]="logoPreviewUrls[cliente.id]"
                    alt="Logo cliente"
                    class="h-10 w-10 rounded-md border border-gray-200 object-cover dark:border-gray-700"
                  />
                  <ng-template #sinLogo>
                    <span class="text-xs text-gray-400">Sin logo</span>
                  </ng-template>
                </td>
                <td class="px-3 py-3 sm:px-4">{{ cliente.nombre }}</td>
                <td class="px-3 py-3 sm:px-4">{{ cliente.direccion || '-' }}</td>
                <td class="px-3 py-3 sm:px-4">
                  <span *ngIf="cliente.deleted" class="inline-flex rounded-full border border-orange-200 bg-orange-50 px-2.5 py-1 text-xs font-semibold text-orange-700 dark:border-orange-900/40 dark:bg-orange-900/20 dark:text-orange-300">ELIMINADO</span>
                  <span *ngIf="!cliente.deleted && cliente.activo" class="inline-flex rounded-full border border-green-200 bg-green-50 px-2.5 py-1 text-xs font-semibold text-green-700 dark:border-green-900/40 dark:bg-green-900/20 dark:text-green-300">ACTIVO</span>
                  <span *ngIf="!cliente.deleted && !cliente.activo" class="inline-flex rounded-full border border-red-200 bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-700 dark:border-red-900/40 dark:bg-red-900/20 dark:text-red-300">INACTIVO</span>
                </td>
                <td class="px-3 py-3 sm:px-4">
                  <div class="flex flex-wrap gap-1.5 sm:gap-2">
                    <button class="icon-action-btn" type="button" aria-label="Ver" (click)="selectDetail(cliente)">
                      <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.8"/></svg>
                      <span class="icon-action-tooltip">Ver</span>
                    </button>
                    <button *ngIf="canManage() && !cliente.deleted" class="icon-action-btn" type="button" aria-label="Editar" (click)="startEdit(cliente)">
                      <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="m3 21 3.8-1 10-10a2.1 2.1 0 0 0-3-3l-10 10L3 21Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/><path d="m13.5 6.5 3 3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                      <span class="icon-action-tooltip">Editar</span>
                    </button>
                    <button *ngIf="canManage() && !cliente.deleted" class="icon-action-btn" [class.text-green-600]="!cliente.activo" [class.text-red-600]="cliente.activo" type="button" aria-label="Cambiar estado" (click)="toggleActivo(cliente)">
                      <svg *ngIf="!cliente.activo" viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M5 12h14M12 5v14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                      <svg *ngIf="cliente.activo" viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M5 12h14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                      <span class="icon-action-tooltip">{{ cliente.activo ? 'Inactivar' : 'Activar' }}</span>
                    </button>
                    <button *ngIf="canManage() && !cliente.deleted" class="icon-action-btn text-orange-600 hover:text-orange-700" type="button" aria-label="Eliminar" (click)="softDelete(cliente)">
                      <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M3 6h18M8 6V4h8v2m-9 0 1 14h8l1-14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                      <span class="icon-action-tooltip">Eliminar</span>
                    </button>
                    <button *ngIf="canManage() && cliente.deleted" class="icon-action-btn text-green-600 hover:text-green-700" type="button" aria-label="Restaurar" (click)="restore(cliente)">
                      <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M3 12a9 9 0 1 0 2.6-6.4M3 4v5h5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                      <span class="icon-action-tooltip">Restaurar</span>
                    </button>
                    <button *ngIf="canManage() && cliente.deleted" class="icon-action-btn text-error-600 hover:text-error-700" type="button" aria-label="Eliminar fisico" (click)="forceDelete(cliente)">
                      <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M6 6l12 12M18 6 6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                      <span class="icon-action-tooltip">Force delete</span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="clientes.length === 0">
                <td class="px-4 py-4 text-center text-gray-500 dark:text-gray-400" colspan="6">No hay clientes para mostrar.</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="flex flex-wrap items-center justify-between gap-2 overflow-x-auto px-4 py-3 text-sm text-gray-600 dark:text-gray-300">
          <span class="basis-full sm:basis-auto">Pagina {{ page + 1 }} de {{ totalPages || 1 }}</span>
          <div class="flex min-w-[220px] w-full gap-2 sm:w-auto sm:grow-0">
            <button class="btn-outline-neutral flex-1 px-3 py-1 disabled:opacity-50 sm:flex-none" type="button" (click)="loadClientes(page - 1)" [disabled]="page === 0">Anterior</button>
            <button class="btn-outline-neutral flex-1 px-3 py-1 disabled:opacity-50 sm:flex-none" type="button" (click)="loadClientes(page + 1)" [disabled]="page + 1 >= totalPages">Siguiente</button>
          </div>
        </div>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full p-4 sm:p-6" *ngIf="selectedCliente">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Detalle de cliente</h2>
        <div class="mt-3">
          <p class="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">Logo</p>
          <img
            *ngIf="selectedLogoUrl; else sinLogoDetalle"
            [src]="selectedLogoUrl"
            alt="Logo cliente"
            class="h-24 w-24 rounded-xl border border-gray-200 object-cover dark:border-gray-700"
          />
          <ng-template #sinLogoDetalle>
            <div class="flex h-24 w-24 items-center justify-center rounded-xl border border-dashed border-gray-300 text-xs text-gray-400 dark:border-gray-700">
              Sin logo
            </div>
          </ng-template>
        </div>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-300"><strong>ID:</strong> {{ selectedCliente.id }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Tipo documento:</strong> {{ selectedCliente.tipoDocumento }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Documento:</strong> {{ selectedCliente.documento }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Nombre:</strong> {{ selectedCliente.nombre }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Direccion:</strong> {{ selectedCliente.direccion || '-' }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Descripcion:</strong> {{ selectedCliente.descripcion || '-' }}</p>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full p-4 sm:p-6 lg:p-7" *ngIf="canManage() && mode !== 'none'">
        <div class="flex flex-wrap items-start justify-between gap-2">
          <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ mode === 'create' ? 'Crear cliente' : 'Editar cliente' }}</h2>
          <span class="text-xs text-gray-500 dark:text-gray-400">Completa los campos requeridos</span>
        </div>
        <form class="mt-5 min-w-0 w-full grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-12" [formGroup]="clienteForm" (ngSubmit)="submitCliente()">
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label">Tipo Documento</label>
            <select class="form-control" formControlName="tipoDocumento">
              <option value="CEDULA">CEDULA</option>
              <option value="RUC">RUC</option>
              <option value="PASAPORTE">PASAPORTE</option>
            </select>
            <p class="form-error" *ngIf="showError('tipoDocumento', 'required')">Tipo de documento es obligatorio.</p>
          </div>
          <div class="min-w-0 xl:col-span-4">
            <label class="form-label">Documento</label>
            <input class="form-control" formControlName="documento" />
            <p class="form-error" *ngIf="showError('documento', 'required')">Documento es obligatorio.</p>
          </div>
          <div class="min-w-0 xl:col-span-5">
            <label class="form-label">Nombre</label>
            <input class="form-control" formControlName="nombre" />
            <p class="form-error" *ngIf="showError('nombre', 'required')">Nombre es obligatorio.</p>
          </div>
          <div class="min-w-0 xl:col-span-6">
            <label class="form-label">Activo</label>
            <select class="form-control" formControlName="activo">
              <option [ngValue]="true">SI</option>
              <option [ngValue]="false">NO</option>
            </select>
          </div>
          <div class="min-w-0 xl:col-span-6">
            <label class="form-label">Direccion</label>
            <input class="form-control" formControlName="direccion" />
          </div>
          <div class="min-w-0 xl:col-span-6">
            <label class="form-label">Logo Empresa</label>
            <input class="form-control" type="file" accept="image/png,image/jpeg,image/webp" (change)="onLogoFileChange($event)" />
          </div>
          <div class="min-w-0 xl:col-span-12">
            <label class="form-label">Descripcion</label>
            <textarea class="form-control !h-24 !py-2" formControlName="descripcion"></textarea>
          </div>
          <div class="min-w-0 flex w-full flex-col-reverse gap-2 pt-2 sm:flex-row sm:justify-end md:col-span-2 xl:col-span-12">
            <button class="w-full rounded-xl border border-gray-300 px-4 py-2.5 text-sm dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800 sm:w-auto" type="button" (click)="cancelForm()">Cancelar</button>
            <button class="w-full rounded-xl bg-brand-500 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-600 sm:w-auto" type="submit">{{ mode === 'create' ? 'Crear' : 'Guardar cambios' }}</button>
          </div>
        </form>
      </article>

      <div class="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/70 p-4" *ngIf="showImportModal">
        <article class="panel-card w-full max-w-3xl p-5 sm:p-6">
          <div class="flex items-start justify-between gap-3">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Importar clientes Excel</h3>
            <button class="btn-outline-neutral px-3 py-1" type="button" (click)="closeImportModal()">Cerrar</button>
          </div>

          <div class="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2">
            <button class="btn-outline-neutral inline-flex items-center justify-center gap-2 px-4 py-2" type="button" (click)="downloadTemplate()">
              <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M12 3v12m0 0 4-4m-4 4-4-4M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
              Descargar plantilla
            </button>
            <button class="btn-outline-neutral inline-flex items-center justify-center gap-2 px-4 py-2" type="button" (click)="downloadExampleTemplate()">
              <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M8 3h8l5 5v11a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/><path d="M16 3v5h5M9 13h6M9 17h6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
              Descargar ejemplo Excel
            </button>
            <input class="form-control" type="file" accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" (change)="onImportFileChange($event)" />
            <select class="form-control" [(ngModel)]="importMode" [ngModelOptions]="{standalone: true}">
              <option value="INSERT_ONLY">INSERT_ONLY</option>
              <option value="UPSERT">UPSERT</option>
            </select>
            <label class="flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
              <input type="checkbox" [(ngModel)]="partialOk" [ngModelOptions]="{standalone: true}" />
              partialOk
            </label>
          </div>

          <div class="mt-4 flex flex-wrap gap-2">
            <button class="btn-outline-neutral inline-flex items-center gap-2 px-4 py-2" type="button" (click)="previewImport()" [disabled]="!importFile">
              <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.8"/></svg>
              Previsualizar
            </button>
            <button class="btn-primary-brand inline-flex items-center gap-2 px-4 py-2" type="button" (click)="executeImport()" [disabled]="!importFile">
              <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M12 21V9m0 0 4 4m-4-4-4 4M4 7V5a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
              Importar
            </button>
          </div>

          <div class="mt-4 rounded-lg border border-gray-200 p-4 text-sm dark:border-gray-800" *ngIf="importPreview">
            <p><strong>Total filas:</strong> {{ importPreview.totalRows }}</p>
            <p><strong>Procesadas:</strong> {{ importPreview.processed }}</p>
            <p><strong>Insertadas:</strong> {{ importPreview.inserted }}</p>
            <p><strong>Actualizadas:</strong> {{ importPreview.updated }}</p>
            <p><strong>Saltadas:</strong> {{ importPreview.skipped }}</p>
            <p><strong>Errores:</strong> {{ importPreview.errorsCount }}</p>
            <div class="mt-3 max-h-36 overflow-auto" *ngIf="importPreview.errors.length > 0">
              <p class="font-semibold">Errores:</p>
              <p class="text-xs" *ngFor="let e of importPreview.errors">Fila {{ e.row }} - {{ e.message }}</p>
            </div>
          </div>
        </article>
      </div>
    </section>
  `
})
export class ClientesListComponent implements OnDestroy {
  private readonly clientesService = inject(ClientesService);
  private readonly popupService = inject(PopupService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  protected clientes: ClienteResponse[] = [];
  protected logoPreviewUrls: Record<string, string> = {};
  protected selectedCliente: ClienteResponse | null = null;
  protected selectedLogoUrl: string | null = null;
  protected mode: 'none' | 'create' | 'edit' = 'none';
  protected editingId: string | null = null;
  protected page = 0;
  protected readonly size = 10;
  protected totalPages = 0;

  protected showImportModal = false;
  protected importFile: File | null = null;
  protected importMode: 'INSERT_ONLY' | 'UPSERT' = 'INSERT_ONLY';
  protected partialOk = true;
  protected importPreview: ClienteImportResult | null = null;

  private logoFile: File | null = null;

  protected readonly filtersForm = this.fb.nonNullable.group({
    q: [''],
    includeDeleted: ['false']
  });

  protected readonly clienteForm = this.fb.group({
    tipoDocumento: ['CEDULA', [Validators.required]],
    documento: ['', [Validators.required]],
    nombre: ['', [Validators.required]],
    direccion: [''],
    descripcion: [''],
    activo: [true, [Validators.required]]
  });

  constructor() {
    this.loadClientes(0);
  }

  ngOnDestroy(): void {
    this.revokeLogoPreviews();
    this.revokeSelectedLogoPreview();
  }

  protected canManage(): boolean {
    return this.authService.getRole() === 'SUPERADMINISTRADOR';
  }

  protected loadClientes(page: number): void {
    const safePage = page < 0 ? 0 : page;
    const filters = this.filtersForm.getRawValue();
    this.clientesService
      .list({
        page: safePage,
        size: this.size,
        q: filters.q || undefined,
        includeDeleted: this.canManage() && filters.includeDeleted === 'true'
      })
      .subscribe({
        next: (response) => {
          this.revokeLogoPreviews();
          this.clientes = response.content;
          this.page = response.page;
          this.totalPages = response.totalPages;
          this.loadLogoPreviews(response.content);
        },
        error: (error) => {
          void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
        }
      });
  }

  protected selectDetail(cliente: ClienteResponse): void {
    this.clientesService.getById(cliente.id).subscribe({
      next: (detail) => {
        this.selectedCliente = detail;
        this.loadSelectedLogo(detail);
      }
    });
  }

  protected startCreate(): void {
    this.mode = 'create';
    this.editingId = null;
    this.logoFile = null;
    this.clienteForm.reset({
      tipoDocumento: 'CEDULA',
      documento: '',
      nombre: '',
      direccion: '',
      descripcion: '',
      activo: true
    });
  }

  protected startEdit(cliente: ClienteResponse): void {
    this.mode = 'edit';
    this.editingId = cliente.id;
    this.logoFile = null;
    this.clienteForm.reset({
      tipoDocumento: cliente.tipoDocumento,
      documento: cliente.documento,
      nombre: cliente.nombre,
      direccion: cliente.direccion ?? '',
      descripcion: cliente.descripcion ?? '',
      activo: cliente.activo
    });
  }

  protected cancelForm(): void {
    this.mode = 'none';
    this.editingId = null;
    this.logoFile = null;
  }

  protected onLogoFileChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.logoFile = target.files?.[0] ?? null;
  }

  protected async submitCliente(): Promise<void> {
    this.clienteForm.markAllAsTouched();
    if (this.clienteForm.invalid) {
      return;
    }

    const value = this.clienteForm.getRawValue();
    const payload = {
      tipoDocumento: (value.tipoDocumento ?? 'CEDULA') as TipoDocumentoCliente,
      documento: value.documento ?? '',
      nombre: value.nombre ?? '',
      direccion: value.direccion ?? '',
      descripcion: value.descripcion ?? '',
      activo: value.activo ?? true
    };

    if (this.mode === 'create') {
      const confirmed = await this.popupService.confirm({
        title: 'Crear cliente',
        message: 'Vas a crear un cliente nuevo. ¿Deseas continuar?'
      });
      if (!confirmed) {
        return;
      }
      this.clientesService.create(payload).subscribe({
        next: (saved) => {
          this.uploadPendingLogo(saved.id, () => {
            void this.popupService.info({ title: 'Cliente creado', message: 'Cliente creado correctamente.' });
            this.cancelForm();
            this.loadClientes(this.page);
          });
        },
        error: (error) => {
          void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
        }
      });
      return;
    }

    if (this.mode === 'edit' && this.editingId) {
      const confirmed = await this.popupService.confirm({
        title: 'Editar cliente',
        message: 'Vas a editar este cliente. ¿Deseas continuar?'
      });
      if (!confirmed) {
        return;
      }
      this.clientesService.update(this.editingId, payload).subscribe({
        next: (saved) => {
          this.uploadPendingLogo(saved.id, () => {
            void this.popupService.info({ title: 'Cliente editado', message: 'Cliente editado correctamente.' });
            this.cancelForm();
            this.loadClientes(this.page);
          });
        },
        error: (error) => {
          void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
        }
      });
    }
  }

  protected async toggleActivo(cliente: ClienteResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: cliente.activo ? 'Inactivar cliente' : 'Activar cliente',
      message: `Vas a ${cliente.activo ? 'inactivar' : 'activar'} a "${cliente.nombre}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.clientesService.toggleActivo(cliente.id).subscribe({
      next: () => this.loadClientes(this.page),
      error: (error) => void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) })
    });
  }

  protected async softDelete(cliente: ClienteResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Eliminación lógica',
      message: `Vas a eliminar lógicamente al cliente "${cliente.nombre}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.clientesService.softDelete(cliente.id).subscribe({
      next: () => {
        void this.popupService.info({ title: 'Cliente eliminado', message: 'Cliente eliminado lógicamente.' });
        this.loadClientes(this.page);
      },
      error: (error) => void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) })
    });
  }

  protected async restore(cliente: ClienteResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Restaurar cliente',
      message: `Vas a restaurar al cliente "${cliente.nombre}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.clientesService.restore(cliente.id).subscribe({
      next: () => {
        void this.popupService.info({ title: 'Cliente restaurado', message: 'Cliente restaurado correctamente.' });
        this.loadClientes(this.page);
      },
      error: (error) => void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) })
    });
  }

  protected async forceDelete(cliente: ClienteResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Eliminación física',
      message: `Vas a eliminar físicamente al cliente "${cliente.nombre}". Esta acción no se puede deshacer. ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.clientesService.forceDelete(cliente.id).subscribe({
      next: () => {
        void this.popupService.info({ title: 'Cliente eliminado', message: 'Cliente eliminado físicamente.' });
        this.loadClientes(this.page);
      },
      error: (error) => void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) })
    });
  }

  protected openImportModal(): void {
    this.showImportModal = true;
    this.importPreview = null;
  }

  protected closeImportModal(): void {
    this.showImportModal = false;
  }

  protected onImportFileChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.importFile = target.files?.[0] ?? null;
    this.importPreview = null;
  }

  protected downloadTemplate(): void {
    this.clientesService.downloadTemplate().subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'clientes_template.xlsx';
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  protected downloadExampleTemplate(): void {
    this.clientesService.downloadExampleTemplate().subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'clientes_template_ejemplo.xlsx';
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  protected previewImport(): void {
    if (!this.importFile) {
      return;
    }
    this.clientesService.previewImport(this.importFile, this.importMode, this.partialOk).subscribe({
      next: (result) => {
        this.importPreview = result;
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected executeImport(): void {
    if (!this.importFile) {
      return;
    }
    this.clientesService.importExcel(this.importFile, this.importMode, this.partialOk).subscribe({
      next: (result) => {
        this.importPreview = result;
        void this.popupService.info({
          title: 'Importación finalizada',
          message: `Procesadas ${result.processed} filas. Insertadas: ${result.inserted}. Actualizadas: ${result.updated}.`
        });
        this.loadClientes(this.page);
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected showError(controlName: string, errorName: string): boolean {
    const control = this.clienteForm.get(controlName);
    if (!control) {
      return false;
    }
    return control.hasError(errorName) && (control.touched || control.dirty);
  }

  private uploadPendingLogo(clienteId: string, onDone: () => void): void {
    if (!this.logoFile) {
      onDone();
      return;
    }
    this.clientesService.uploadLogo(clienteId, this.logoFile).subscribe({
      next: () => onDone(),
      error: () => onDone()
    });
  }

  private loadLogoPreviews(items: ClienteResponse[]): void {
    items.filter((c) => !!c.logoPath).forEach((cliente) => {
      this.clientesService.getLogoBlob(cliente.id).subscribe({
        next: (blob) => {
          this.logoPreviewUrls[cliente.id] = URL.createObjectURL(blob);
        }
      });
    });
  }

  private revokeLogoPreviews(): void {
    Object.values(this.logoPreviewUrls).forEach((url) => URL.revokeObjectURL(url));
    this.logoPreviewUrls = {};
  }

  private loadSelectedLogo(cliente: ClienteResponse): void {
    this.revokeSelectedLogoPreview();
    if (!cliente.logoPath) {
      this.selectedLogoUrl = null;
      return;
    }
    this.clientesService.getLogoBlob(cliente.id).subscribe({
      next: (blob) => {
        this.selectedLogoUrl = URL.createObjectURL(blob);
      },
      error: () => {
        this.selectedLogoUrl = null;
      }
    });
  }

  private revokeSelectedLogoPreview(): void {
    if (this.selectedLogoUrl) {
      URL.revokeObjectURL(this.selectedLogoUrl);
      this.selectedLogoUrl = null;
    }
  }

  private getErrorMessage(error: unknown): string {
    const maybe = error as { error?: { message?: string } };
    return maybe?.error?.message ?? 'Ocurrio un error inesperado.';
  }
}
