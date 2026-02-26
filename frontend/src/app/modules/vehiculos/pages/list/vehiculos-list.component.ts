import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { PopupService } from '../../../../core/services/popup.service';
import {
  EstadoVehiculo,
  TipoDocumento,
  VehiculoImportResult,
  VehiculoResponse,
  VehiculosService
} from '../../services/vehiculos.service';

@Component({
  selector: 'app-vehiculos-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <section class="min-w-0 w-full space-y-6">
      <header class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="page-title">Vehiculos</h1>
          <p class="page-subtitle">Catalogo completo con imagenes e importacion CSV.</p>
        </div>
        <div class="flex w-full flex-col gap-2 sm:w-auto sm:flex-row">
          <button class="btn-outline-neutral px-4 py-2" type="button" (click)="openImportModal()">Importar CSV</button>
          <button class="btn-primary-brand" type="button" (click)="startCreate()">Nuevo vehiculo</button>
        </div>
      </header>

      <article class="panel-card min-w-0 w-full max-w-full p-4">
        <form class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4" [formGroup]="filtersForm" (ngSubmit)="loadVehiculos(0)">
          <input class="filter-control" formControlName="q" placeholder="Buscar placa o chofer" />
          <select class="filter-control" formControlName="estado">
            <option value="">Estado: todos</option>
            <option value="ACTIVO">ACTIVO</option>
            <option value="INACTIVO">INACTIVO</option>
          </select>
          <select class="filter-control" formControlName="includeDeleted">
            <option value="false">No eliminados</option>
            <option value="true">Incluir eliminados</option>
          </select>
          <button class="btn-outline-neutral h-10 w-full rounded-lg font-medium hover:bg-gray-100" type="submit">Filtrar</button>
        </form>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full overflow-hidden">
        <div class="min-w-0 w-full max-w-full overflow-x-auto">
          <table class="w-full table-auto min-w-[760px] text-left text-xs sm:text-sm lg:min-w-full">
            <thead class="border-b border-gray-200 bg-gray-50 text-gray-600 dark:border-gray-800 dark:bg-gray-950 dark:text-gray-300">
              <tr>
                <th class="px-3 py-3 sm:px-4">Placa</th>
                <th class="px-3 py-3 sm:px-4">Chofer</th>
                <th class="px-3 py-3 sm:px-4">Tonelaje/Categoria</th>
                <th class="px-3 py-3 sm:px-4">M3</th>
                <th class="px-3 py-3 sm:px-4">Estado</th>
                <th class="px-3 py-3 sm:px-4">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr class="border-b border-gray-100 dark:border-gray-800" *ngFor="let vehiculo of vehiculos">
                <td class="px-3 py-3 sm:px-4">{{ vehiculo.placa }}</td>
                <td class="px-3 py-3 sm:px-4">{{ vehiculo.choferDefault }}</td>
                <td class="px-3 py-3 sm:px-4">{{ vehiculo.tonelajeCategoria }}</td>
                <td class="px-3 py-3 sm:px-4">{{ vehiculo.m3 }}</td>
                <td class="px-3 py-3 sm:px-4">
                  <span *ngIf="vehiculo.deleted" class="inline-flex rounded-full border border-orange-200 bg-orange-50 px-2.5 py-1 text-xs font-semibold text-orange-700 dark:border-orange-900/40 dark:bg-orange-900/20 dark:text-orange-300">ELIMINADO</span>
                  <span *ngIf="!vehiculo.deleted && vehiculo.estado === 'ACTIVO'" class="inline-flex rounded-full border border-green-200 bg-green-50 px-2.5 py-1 text-xs font-semibold text-green-700 dark:border-green-900/40 dark:bg-green-900/20 dark:text-green-300">ACTIVO</span>
                  <span *ngIf="!vehiculo.deleted && vehiculo.estado === 'INACTIVO'" class="inline-flex rounded-full border border-red-200 bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-700 dark:border-red-900/40 dark:bg-red-900/20 dark:text-red-300">INACTIVO</span>
                </td>
                <td class="px-3 py-3 sm:px-4">
                  <div class="flex flex-wrap gap-1.5 sm:gap-2">
                    <button class="icon-action-btn" type="button" aria-label="Ver" (click)="selectDetail(vehiculo)"><span class="icon-action-tooltip">Ver</span>VER</button>
                    <button class="icon-action-btn" type="button" aria-label="Editar" (click)="startEdit(vehiculo)"><span class="icon-action-tooltip">Editar</span>EDIT</button>
                    <button *ngIf="!vehiculo.deleted && vehiculo.estado === 'INACTIVO'" class="icon-action-btn text-green-600" type="button" aria-label="Activar" (click)="activate(vehiculo)"><span class="icon-action-tooltip">Activar</span>ON</button>
                    <button *ngIf="!vehiculo.deleted && vehiculo.estado === 'ACTIVO'" class="icon-action-btn text-red-600" type="button" aria-label="Inactivar" (click)="deactivate(vehiculo)"><span class="icon-action-tooltip">Inactivar</span>OFF</button>
                    <button *ngIf="!vehiculo.deleted" class="icon-action-btn text-orange-600" type="button" aria-label="Eliminar" (click)="softDelete(vehiculo)"><span class="icon-action-tooltip">Soft delete</span>DEL</button>
                    <button *ngIf="vehiculo.deleted" class="icon-action-btn text-green-600" type="button" aria-label="Restaurar" (click)="restore(vehiculo)"><span class="icon-action-tooltip">Restaurar</span>RES</button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="vehiculos.length === 0">
                <td class="px-4 py-4 text-center text-gray-500 dark:text-gray-400" colspan="6">No hay vehiculos para mostrar.</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="flex flex-wrap items-center justify-between gap-2 overflow-x-auto px-4 py-3 text-sm text-gray-600 dark:text-gray-300">
          <span class="basis-full sm:basis-auto">Pagina {{ page + 1 }} de {{ totalPages || 1 }}</span>
          <div class="flex min-w-[220px] w-full gap-2 sm:w-auto sm:grow-0">
            <button class="btn-outline-neutral flex-1 px-3 py-1 disabled:opacity-50 sm:flex-none" type="button" (click)="loadVehiculos(page - 1)" [disabled]="page === 0">Anterior</button>
            <button class="btn-outline-neutral flex-1 px-3 py-1 disabled:opacity-50 sm:flex-none" type="button" (click)="loadVehiculos(page + 1)" [disabled]="page + 1 >= totalPages">Siguiente</button>
          </div>
        </div>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full p-4 sm:p-6" *ngIf="selectedVehiculo">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Detalle de vehiculo</h2>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-300"><strong>Placa:</strong> {{ selectedVehiculo.placa }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Chofer:</strong> {{ selectedVehiculo.choferDefault }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Documento:</strong> {{ selectedVehiculo.tipoDocumento }} {{ selectedVehiculo.documentoPersonal }}</p>
        <p class="mt-1 text-sm text-gray-600 dark:text-gray-300"><strong>Estado:</strong> {{ selectedVehiculo.estado }}</p>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full p-4 sm:p-6 lg:p-7" *ngIf="mode !== 'none'">
        <div class="flex flex-wrap items-start justify-between gap-2">
          <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ mode === 'create' ? 'Crear vehiculo' : 'Editar vehiculo' }}</h2>
          <span class="text-xs text-gray-500 dark:text-gray-400">Campos obligatorios y subida de imagenes</span>
        </div>

        <form class="mt-5 min-w-0 w-full grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-12" [formGroup]="vehiculoForm" (ngSubmit)="submitVehiculo()">
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label">Placa</label>
            <input class="form-control" formControlName="placa" />
          </div>
          <div class="min-w-0 xl:col-span-4">
            <label class="form-label">Chofer</label>
            <input class="form-control" formControlName="choferDefault" />
          </div>
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label">Licencia</label>
            <input class="form-control" formControlName="licencia" />
          </div>
          <div class="min-w-0 xl:col-span-2">
            <label class="form-label">Estado</label>
            <select class="form-control" formControlName="estado">
              <option value="ACTIVO">ACTIVO</option>
              <option value="INACTIVO">INACTIVO</option>
            </select>
          </div>

          <div class="min-w-0 xl:col-span-3">
            <label class="form-label">Tipo documento</label>
            <select class="form-control" formControlName="tipoDocumento">
              <option value="CEDULA">CEDULA</option>
              <option value="RUC">RUC</option>
              <option value="PASAPORTE">PASAPORTE</option>
            </select>
          </div>
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label">Documento personal</label>
            <input class="form-control" formControlName="documentoPersonal" />
          </div>
          <div class="min-w-0 xl:col-span-4">
            <label class="form-label">Tonelaje/Categoria</label>
            <input class="form-control" formControlName="tonelajeCategoria" />
          </div>
          <div class="min-w-0 xl:col-span-2">
            <label class="form-label">M3</label>
            <input class="form-control" type="number" min="0" step="0.01" formControlName="m3" />
          </div>

          <div class="min-w-0 xl:col-span-4">
            <label class="form-label">Foto</label>
            <input class="form-control" type="file" accept="image/png,image/jpeg,image/webp" (change)="onFileChange($event, 'foto')" />
          </div>
          <div class="min-w-0 xl:col-span-4">
            <label class="form-label">Documento (imagen)</label>
            <input class="form-control" type="file" accept="image/png,image/jpeg,image/webp" (change)="onFileChange($event, 'documento')" />
          </div>
          <div class="min-w-0 xl:col-span-4">
            <label class="form-label">Licencia (imagen)</label>
            <input class="form-control" type="file" accept="image/png,image/jpeg,image/webp" (change)="onFileChange($event, 'licencia')" />
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
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Importar vehiculos CSV</h3>
            <button class="btn-outline-neutral px-3 py-1" type="button" (click)="closeImportModal()">Cerrar</button>
          </div>

          <div class="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2">
            <button class="btn-outline-neutral px-4 py-2" type="button" (click)="downloadTemplate()">Descargar plantilla</button>
            <input class="form-control" type="file" accept=".csv,text/csv" (change)="onImportFileChange($event)" />
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
            <button class="btn-outline-neutral px-4 py-2" type="button" (click)="previewImport()" [disabled]="!importFile">Previsualizar</button>
            <button class="btn-primary-brand px-4 py-2" type="button" (click)="executeImport()" [disabled]="!importFile">Importar</button>
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
export class VehiculosListComponent {
  private readonly vehiculosService = inject(VehiculosService);
  private readonly fb = inject(FormBuilder);
  private readonly popupService = inject(PopupService);

  protected vehiculos: VehiculoResponse[] = [];
  protected selectedVehiculo: VehiculoResponse | null = null;
  protected mode: 'none' | 'create' | 'edit' = 'none';
  protected editingId: string | null = null;
  protected page = 0;
  protected readonly size = 10;
  protected totalPages = 0;

  protected showImportModal = false;
  protected importFile: File | null = null;
  protected importMode: 'INSERT_ONLY' | 'UPSERT' = 'INSERT_ONLY';
  protected partialOk = true;
  protected importPreview: VehiculoImportResult | null = null;

  private fotoFile: File | null = null;
  private documentoFile: File | null = null;
  private licenciaFile: File | null = null;

  protected readonly filtersForm = this.fb.nonNullable.group({
    q: [''],
    estado: [''],
    includeDeleted: ['false']
  });

  protected readonly vehiculoForm = this.fb.group({
    placa: ['', [Validators.required]],
    choferDefault: ['', [Validators.required]],
    licencia: [''],
    tipoDocumento: ['CEDULA', [Validators.required]],
    documentoPersonal: ['', [Validators.required]],
    tonelajeCategoria: ['', [Validators.required]],
    m3: [0, [Validators.required, Validators.min(0)]],
    estado: ['ACTIVO', [Validators.required]]
  });

  constructor() {
    this.loadVehiculos(0);
  }

  protected loadVehiculos(page: number): void {
    const safePage = page < 0 ? 0 : page;
    const filters = this.filtersForm.getRawValue();

    this.vehiculosService
      .list({
        page: safePage,
        size: this.size,
        q: filters.q || undefined,
        estado: filters.estado || undefined,
        includeDeleted: filters.includeDeleted === 'true'
      })
      .subscribe({
        next: (response) => {
          this.vehiculos = response.content;
          this.page = response.page;
          this.totalPages = response.totalPages;
        }
      });
  }

  protected selectDetail(vehiculo: VehiculoResponse): void {
    this.vehiculosService.getById(vehiculo.id).subscribe((detail) => (this.selectedVehiculo = detail));
  }

  protected startCreate(): void {
    this.mode = 'create';
    this.editingId = null;
    this.resetFormFiles();
    this.vehiculoForm.reset({
      placa: '',
      choferDefault: '',
      licencia: '',
      tipoDocumento: 'CEDULA',
      documentoPersonal: '',
      tonelajeCategoria: '',
      m3: 0,
      estado: 'ACTIVO'
    });
  }

  protected startEdit(vehiculo: VehiculoResponse): void {
    this.mode = 'edit';
    this.editingId = vehiculo.id;
    this.resetFormFiles();
    this.vehiculoForm.reset({
      placa: vehiculo.placa,
      choferDefault: vehiculo.choferDefault,
      licencia: vehiculo.licencia ?? '',
      tipoDocumento: vehiculo.tipoDocumento,
      documentoPersonal: vehiculo.documentoPersonal,
      tonelajeCategoria: vehiculo.tonelajeCategoria,
      m3: vehiculo.m3,
      estado: vehiculo.estado
    });
  }

  protected cancelForm(): void {
    this.mode = 'none';
    this.editingId = null;
    this.resetFormFiles();
  }

  protected onFileChange(event: Event, kind: 'foto' | 'documento' | 'licencia'): void {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0] ?? null;
    if (kind === 'foto') {
      this.fotoFile = file;
    }
    if (kind === 'documento') {
      this.documentoFile = file;
    }
    if (kind === 'licencia') {
      this.licenciaFile = file;
    }
  }

  protected async submitVehiculo(): Promise<void> {
    this.vehiculoForm.markAllAsTouched();
    if (this.vehiculoForm.invalid) {
      return;
    }

    const value = this.vehiculoForm.getRawValue();
    const payload = {
      placa: value.placa ?? '',
      choferDefault: value.choferDefault ?? '',
      licencia: value.licencia ?? '',
      tipoDocumento: (value.tipoDocumento ?? 'CEDULA') as TipoDocumento,
      documentoPersonal: value.documentoPersonal ?? '',
      tonelajeCategoria: value.tonelajeCategoria ?? '',
      m3: Number(value.m3 ?? 0),
      estado: (value.estado ?? 'ACTIVO') as EstadoVehiculo
    };

    if (this.mode === 'create') {
      const confirmed = await this.popupService.confirm({
        title: 'Crear vehiculo',
        message: 'Vas a crear un vehiculo nuevo. ¿Deseas continuar?'
      });
      if (!confirmed) {
        return;
      }

      this.vehiculosService.create(payload).subscribe({
        next: (saved) => {
          this.uploadPendingImages(saved.id);
          void this.popupService.info({ title: 'Vehiculo creado', message: 'Vehiculo creado correctamente.' });
          this.cancelForm();
          this.loadVehiculos(this.page);
        },
        error: (error) => {
          void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
        }
      });
      return;
    }

    if (this.mode === 'edit' && this.editingId) {
      const confirmed = await this.popupService.confirm({
        title: 'Editar vehiculo',
        message: 'Vas a editar el vehiculo. ¿Deseas continuar?'
      });
      if (!confirmed) {
        return;
      }

      this.vehiculosService.update(this.editingId, payload).subscribe({
        next: (saved) => {
          this.uploadPendingImages(saved.id);
          void this.popupService.info({ title: 'Vehiculo editado', message: 'Vehiculo editado correctamente.' });
          this.cancelForm();
          this.loadVehiculos(this.page);
        },
        error: (error) => {
          void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
        }
      });
    }
  }

  protected async activate(vehiculo: VehiculoResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({ title: 'Activar vehiculo', message: `Activar ${vehiculo.placa}?` });
    if (!confirmed) {
      return;
    }
    this.vehiculosService.activate(vehiculo.id).subscribe(() => this.loadVehiculos(this.page));
  }

  protected async deactivate(vehiculo: VehiculoResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({ title: 'Inactivar vehiculo', message: `Inactivar ${vehiculo.placa}?` });
    if (!confirmed) {
      return;
    }
    this.vehiculosService.deactivate(vehiculo.id).subscribe(() => this.loadVehiculos(this.page));
  }

  protected async softDelete(vehiculo: VehiculoResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({ title: 'Eliminar logico', message: `Eliminar logicamente ${vehiculo.placa}?` });
    if (!confirmed) {
      return;
    }
    this.vehiculosService.softDelete(vehiculo.id).subscribe(() => this.loadVehiculos(this.page));
  }

  protected async restore(vehiculo: VehiculoResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({ title: 'Restaurar vehiculo', message: `Restaurar ${vehiculo.placa}?` });
    if (!confirmed) {
      return;
    }
    this.vehiculosService.restore(vehiculo.id).subscribe(() => this.loadVehiculos(this.page));
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
    this.vehiculosService.downloadTemplate().subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'vehiculos_template.csv';
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  protected previewImport(): void {
    if (!this.importFile) {
      return;
    }
    this.vehiculosService.previewImport(this.importFile, this.importMode, this.partialOk).subscribe({
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
    this.vehiculosService.importCsv(this.importFile, this.importMode, this.partialOk).subscribe({
      next: (result) => {
        this.importPreview = result;
        void this.popupService.info({
          title: 'Importacion finalizada',
          message: `Procesadas ${result.processed} filas. Insertadas: ${result.inserted}. Actualizadas: ${result.updated}.`
        });
        this.loadVehiculos(this.page);
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  private uploadPendingImages(vehiculoId: string): void {
    if (this.fotoFile) {
      this.vehiculosService.uploadFoto(vehiculoId, this.fotoFile).subscribe();
    }
    if (this.documentoFile) {
      this.vehiculosService.uploadDocumento(vehiculoId, this.documentoFile).subscribe();
    }
    if (this.licenciaFile) {
      this.vehiculosService.uploadLicenciaImg(vehiculoId, this.licenciaFile).subscribe();
    }
  }

  private resetFormFiles(): void {
    this.fotoFile = null;
    this.documentoFile = null;
    this.licenciaFile = null;
  }

  private getErrorMessage(error: unknown): string {
    const maybe = error as { error?: { message?: string } };
    return maybe?.error?.message ?? 'Ocurrio un error inesperado.';
  }
}
