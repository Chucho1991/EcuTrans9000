import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { PopupService } from '../../../core/services/popup.service';
import { DatePickerComponent } from '../../../shared/components/date-picker/date-picker.component';
import { CatalogSearchOption, CatalogSearchSelectComponent } from '../../../shared/components/catalog-search-select/catalog-search-select.component';
import { AuthService } from '../../auth/services/auth.service';
import { VehiculoResponse, VehiculosService } from '../../vehiculos/services/vehiculos.service';
import {
  DescuentoViajeImportResult,
  DescuentoViajeResponse,
  DescuentosViajesService
} from '../services/descuentos-viajes.service';

@Component({
  selector: 'app-descuentos-viajes-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, CatalogSearchSelectComponent, DatePickerComponent],
  templateUrl: './descuentos-viajes-list.component.html'
})
export class DescuentosViajesListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly descuentosViajesService = inject(DescuentosViajesService);
  private readonly vehiculosService = inject(VehiculosService);
  private readonly popupService = inject(PopupService);
  private readonly authService = inject(AuthService);

  protected descuentos: DescuentoViajeResponse[] = [];
  protected vehiculos: VehiculoResponse[] = [];
  protected vehiculoOptions: CatalogSearchOption[] = [];
  protected page = 0;
  protected readonly size = 10;
  protected totalPages = 0;
  protected totalElements = 0;
  protected showFormModal = false;
  protected showDetailModal = false;
  protected showImportModal = false;
  protected editingId: number | null = null;
  protected selectedDescuento: DescuentoViajeResponse | null = null;
  protected importFile: File | null = null;
  protected importMode: 'INSERT_ONLY' | 'UPSERT' = 'INSERT_ONLY';
  protected partialOk = true;
  protected importPreview: DescuentoViajeImportResult | null = null;

  protected readonly filtersForm = this.fb.group({
    q: [''],
    vehiculoId: [''],
    activo: [''],
    includeDeleted: [false]
  });

  protected readonly form = this.fb.group({
    vehiculoId: ['', [Validators.required]],
    descripcionMotivo: ['', [Validators.required]],
    montoMotivo: [0, [Validators.required, Validators.min(0)]],
    fechaAplicacion: [''],
    activo: [true]
  });

  constructor() {
    this.loadVehiculos();
    this.loadDescuentos();
  }

  protected canSoftDelete(): boolean {
    return this.authService.getRole() === 'SUPERADMINISTRADOR';
  }

  protected goToPage(page: number): void {
    this.page = page;
    this.loadDescuentos();
  }

  protected resetFilters(): void {
    this.filtersForm.reset({
      q: '',
      vehiculoId: '',
      activo: '',
      includeDeleted: false
    });
    this.page = 0;
    this.loadDescuentos();
  }

  protected startCreate(): void {
    this.editingId = null;
    this.form.reset({
      vehiculoId: '',
      descripcionMotivo: '',
      montoMotivo: 0,
      fechaAplicacion: '',
      activo: true
    });
    this.showFormModal = true;
  }

  protected startEdit(descuento: DescuentoViajeResponse): void {
    this.editingId = descuento.id;
    this.form.reset({
      vehiculoId: descuento.vehiculoId,
      descripcionMotivo: descuento.descripcionMotivo,
      montoMotivo: descuento.montoMotivo,
      fechaAplicacion: descuento.fechaAplicacion ?? '',
      activo: descuento.activo
    });
    this.showFormModal = true;
  }

  protected openDetail(descuento: DescuentoViajeResponse): void {
    this.selectedDescuento = descuento;
    this.showDetailModal = true;
  }

  protected closeDetailModal(): void {
    this.selectedDescuento = null;
    this.showDetailModal = false;
  }

  protected closeFormModal(): void {
    this.showFormModal = false;
    this.editingId = null;
  }

  protected openImportModal(): void {
    this.showImportModal = true;
    this.importPreview = null;
    this.importFile = null;
  }

  protected closeImportModal(): void {
    this.showImportModal = false;
    this.importPreview = null;
    this.importFile = null;
  }

  protected onImportFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.importFile = input.files?.[0] ?? null;
    this.importPreview = null;
  }

  protected async submit(): Promise<void> {
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }
    const confirmed = await this.popupService.confirm({
      title: this.editingId === null ? 'Registrar descuento' : 'Editar descuento',
      message: this.editingId === null
        ? 'Vas a registrar un nuevo descuento de viaje. ¿Deseas continuar?'
        : 'Vas a actualizar el descuento seleccionado. ¿Deseas continuar?'
    });
    if (!confirmed) {
      return;
    }

    const value = this.form.getRawValue();
    const request = {
      vehiculoId: value.vehiculoId ?? '',
      descripcionMotivo: value.descripcionMotivo ?? '',
      montoMotivo: Number(value.montoMotivo ?? 0),
      fechaAplicacion: value.fechaAplicacion ? String(value.fechaAplicacion) : null,
      activo: Boolean(value.activo)
    };

    const operation = this.editingId === null
      ? this.descuentosViajesService.create(request)
      : this.descuentosViajesService.update(this.editingId, request);

    operation.subscribe({
      next: async () => {
        await this.popupService.info({
          title: this.editingId === null ? 'Descuento registrado' : 'Descuento actualizado',
          message: this.editingId === null
            ? 'El descuento fue registrado correctamente.'
            : 'El descuento fue actualizado correctamente.'
        });
        this.closeFormModal();
        this.loadDescuentos();
      }
    });
  }

  protected async activate(descuento: DescuentoViajeResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Activar descuento',
      message: `Activar el descuento #${descuento.id}?`
    });
    if (!confirmed) {
      return;
    }
    this.descuentosViajesService.activate(descuento.id).subscribe({ next: () => this.loadDescuentos() });
  }

  protected async deactivate(descuento: DescuentoViajeResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Inactivar descuento',
      message: `Inactivar el descuento #${descuento.id}?`
    });
    if (!confirmed) {
      return;
    }
    this.descuentosViajesService.deactivate(descuento.id).subscribe({ next: () => this.loadDescuentos() });
  }

  protected async softDelete(descuento: DescuentoViajeResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Eliminar descuento',
      message: `Eliminar logicamente el descuento #${descuento.id}?`
    });
    if (!confirmed) {
      return;
    }
    this.descuentosViajesService.softDelete(descuento.id).subscribe({ next: () => this.loadDescuentos() });
  }

  protected async restore(descuento: DescuentoViajeResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Restaurar descuento',
      message: `Restaurar el descuento #${descuento.id}?`
    });
    if (!confirmed) {
      return;
    }
    this.descuentosViajesService.restore(descuento.id).subscribe({ next: () => this.loadDescuentos() });
  }

  protected downloadTemplate(): void {
    this.descuentosViajesService.downloadTemplate().subscribe({
      next: (blob) => this.saveBlob(blob, 'descuentos_viajes_template.xlsx')
    });
  }

  protected downloadTemplateExample(): void {
    this.descuentosViajesService.downloadExampleTemplate().subscribe({
      next: (blob) => this.saveBlob(blob, 'descuentos_viajes_template_ejemplo.xlsx')
    });
  }

  protected previewImport(): void {
    if (!this.importFile) {
      return;
    }
    this.descuentosViajesService.previewImport(this.importFile, this.importMode, this.partialOk).subscribe({
      next: (response) => {
        this.importPreview = response;
      }
    });
  }

  protected executeImport(): void {
    if (!this.importFile) {
      return;
    }
    this.descuentosViajesService.importExcel(this.importFile, this.importMode, this.partialOk).subscribe({
      next: async (response) => {
        this.importPreview = response;
        await this.popupService.info({
          title: 'Importacion finalizada',
          message: 'La importacion de descuentos de viajes finalizo correctamente.'
        });
        this.loadDescuentos();
      }
    });
  }

  protected showError(controlName: string, errorKey: string): boolean {
    const control = this.form.get(controlName);
    return Boolean(control && control.touched && control.hasError(errorKey));
  }

  protected loadDescuentos(): void {
    const filters = this.filtersForm.getRawValue();
    const activo = filters.activo === '' ? '' : filters.activo === 'true';
    this.descuentosViajesService.list({
      page: this.page,
      size: this.size,
      q: filters.q ?? '',
      vehiculoId: filters.vehiculoId ?? '',
      activo,
      includeDeleted: Boolean(filters.includeDeleted)
    }).subscribe({
      next: (response) => {
        this.descuentos = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
      }
    });
  }

  private loadVehiculos(): void {
    this.vehiculosService.list({
      page: 0,
      size: 5000,
      includeDeleted: false
    }).subscribe({
      next: (response) => {
        this.vehiculos = response.content;
        this.vehiculoOptions = response.content.map((vehiculo) => ({
          value: vehiculo.id,
          label: `${vehiculo.choferDefault} | ${vehiculo.placa}`,
          secondaryLabel: vehiculo.documentoPersonal,
          searchText: `${vehiculo.choferDefault} ${vehiculo.placa} ${vehiculo.documentoPersonal}`
        }));
      }
    });
  }

  private saveBlob(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    window.URL.revokeObjectURL(url);
  }
}
