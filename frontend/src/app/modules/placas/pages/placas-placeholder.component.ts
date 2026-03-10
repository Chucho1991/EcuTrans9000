import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';

import { PopupService } from '../../../core/services/popup.service';
import { CatalogSearchOption, CatalogSearchSelectComponent } from '../../../shared/components/catalog-search-select/catalog-search-select.component';
import { DatePickerComponent } from '../../../shared/components/date-picker/date-picker.component';
import { VehiculoResponse, VehiculosService } from '../../vehiculos/services/vehiculos.service';
import { ConsultaPlacaResponse, PlacasService } from '../services/placas.service';

type EstadoPagoChoferOption = 'TODOS' | 'PAGADOS' | 'NO_PAGADOS';

@Component({
  selector: 'app-placas-placeholder',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePickerComponent, CatalogSearchSelectComponent],
  template: `
    <section class="min-w-0 w-full space-y-6">
      <header class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="page-title">Consulta por placas</h1>
          <p class="page-subtitle">Consulta financiera por placa usando los registros de bitacora.</p>
        </div>
        <button class="btn-outline-neutral px-4 py-2" type="button" (click)="exportarExcel()" [disabled]="!canExport()">
          Exportar Excel
        </button>
      </header>

      <article class="panel-card min-w-0 w-full max-w-full p-4">
        <form class="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-6" [formGroup]="filtersForm" (ngSubmit)="filtrar()">
          <app-catalog-search-select
            formControlName="placa"
            placeholder="Selecciona una placa"
            searchPlaceholder="Buscar vehiculo por placa, documento o chofer"
            noResultsText="No hay vehiculos que coincidan."
            [options]="vehiculoOptions" />
          <label class="relative block">
            <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-gray-400 dark:text-gray-500">
              <svg aria-hidden="true" class="h-4 w-4" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8">
                <path stroke-linecap="round" stroke-linejoin="round" d="m17.5 17.5-3.6-3.6m1.6-4.4a6 6 0 1 1-12 0 6 6 0 0 1 12 0Z" />
              </svg>
            </span>
            <input class="filter-control pl-10" type="search" formControlName="codigoViaje" placeholder="Buscar codigo de viaje" />
          </label>
          <select class="filter-control" formControlName="estadoPagoChofer">
            <option *ngFor="let option of estadoPagoChoferOptions" [value]="option.value">
              {{ option.label }}
            </option>
          </select>
          <app-date-picker inputClass="filter-control" placeholder="Fecha desde" formControlName="fechaDesde" />
          <app-date-picker inputClass="filter-control" placeholder="Fecha hasta" formControlName="fechaHasta" />
          <button class="btn-outline-neutral h-10 rounded-lg font-medium hover:bg-gray-100" type="submit">Filtrar</button>
          <button class="btn-outline-neutral h-10 rounded-lg font-medium hover:bg-gray-100" type="button" (click)="limpiar()">Limpiar</button>
        </form>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full p-4 sm:p-6" *ngIf="consulta">
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ consulta.chofer || 'Chofer no disponible' }}</h2>
            <p class="text-sm text-gray-600 dark:text-gray-300">{{ consulta.placa || selectedPlacaLabel() || '-' }}</p>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">{{ buildPeriodoLabel() }}</p>
        </div>

        <div class="mt-5 grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-5">
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Valor factura</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{{ consulta.valorFacturaTotal | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Retencion 1%</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{{ consulta.retencionUnoPorciento | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Comision 6%</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{{ consulta.comisionAdministrativaSeisPorciento | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Anticipos</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{{ consulta.anticiposTotal | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 bg-brand-500/5 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-500">Pago total</p>
            <p class="mt-1 text-lg font-semibold text-brand-700 dark:text-brand-300">{{ consulta.pagoTotal | number: '1.2-2' }}</p>
          </div>
        </div>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full overflow-hidden">
        <div class="min-w-0 w-full max-w-full overflow-x-auto">
          <table class="w-full min-w-[1040px] table-auto text-left text-xs sm:text-sm">
            <thead class="bg-brand-900 text-white">
              <tr>
                <th class="px-3 py-3 font-semibold">Orden de compra</th>
                <th class="px-3 py-3 font-semibold">Pago chofer</th>
                <th class="px-3 py-3 font-semibold">Valor</th>
                <th class="px-3 py-3 font-semibold">Fecha</th>
                <th class="px-3 py-3 font-semibold">Factura</th>
                <th class="px-3 py-3 font-semibold">Anticipos</th>
                <th class="px-3 py-3 font-semibold">Estiba</th>
                <th class="px-3 py-3 font-semibold">Despacho</th>
                <th class="px-3 py-3 font-semibold">Cliente</th>
                <th class="px-3 py-3 font-semibold">Origen - Destino</th>
              </tr>
            </thead>
            <tbody>
              <tr class="border-b border-gray-100 dark:border-gray-800" *ngFor="let registro of registros">
                <td class="px-3 py-3">{{ displayText(registro.ordenCompra) }}</td>
                <td class="px-3 py-3">
                  <span
                    class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold"
                    [class]="registro.pagadoTransportista
                      ? 'border-blue-light-200 bg-blue-light-50 text-blue-light-700'
                      : 'border-orange-200 bg-orange-50 text-orange-700 dark:border-orange-900/40 dark:bg-orange-900/20 dark:text-orange-300'">
                    {{ registro.pagadoTransportista ? 'Pagado' : 'Pendiente' }}
                  </span>
                </td>
                <td class="px-3 py-3">{{ registro.valor | number: '1.2-2' }}</td>
                <td class="px-3 py-3">{{ formatFecha(registro.fecha) }}</td>
                <td class="px-3 py-3">{{ displayText(registro.factura) }}</td>
                <td class="px-3 py-3">{{ displayAmountOrDash(registro.anticipo) }}</td>
                <td class="px-3 py-3">{{ displayAmountOrDash(registro.estiba) }}</td>
                <td class="px-3 py-3">{{ displayText(registro.despacho) }}</td>
                <td class="px-3 py-3">{{ displayText(registro.cliente) }}</td>
                <td class="px-3 py-3">{{ displayText(registro.origenDestino) }}</td>
              </tr>
              <tr *ngIf="registros.length === 0">
                <td class="px-4 py-8 text-center text-gray-500 dark:text-gray-400" colspan="10">
                  {{ searched ? 'No hay registros para los filtros seleccionados.' : 'La lista esta vacia. Selecciona una placa y filtra para consultar la bitacora.' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </section>
  `
})
export class PlacasPlaceholderComponent {
  private static readonly ECUADOR_TIMEZONE = 'America/Guayaquil';

  private readonly placasService = inject(PlacasService);
  private readonly vehiculosService = inject(VehiculosService);
  private readonly popupService = inject(PopupService);
  private readonly fb = inject(FormBuilder);

  protected vehiculosCatalogo: VehiculoResponse[] = [];
  protected vehiculoOptions: CatalogSearchOption[] = [];
  protected consulta: ConsultaPlacaResponse | null = null;
  protected registros: ConsultaPlacaResponse['registros'] = [];
  protected searched = false;
  protected readonly estadoPagoChoferOptions: Array<{ value: EstadoPagoChoferOption; label: string }> = [
    { value: 'TODOS', label: 'Todos los viajes' },
    { value: 'PAGADOS', label: 'Pagados al chofer' },
    { value: 'NO_PAGADOS', label: 'No pagados al chofer' }
  ];

  protected readonly filtersForm = this.fb.nonNullable.group({
    placa: [''],
    codigoViaje: [''],
    estadoPagoChofer: ['TODOS' as EstadoPagoChoferOption],
    fechaDesde: [''],
    fechaHasta: ['']
  });

  constructor() {
    this.loadVehiculos();
  }

  protected filtrar(): void {
    const filters = this.filtersForm.getRawValue();
    if (!this.hasActiveFilters()) {
      void this.popupService.info({
        title: 'Filtro requerido',
        message: 'Selecciona una placa, un codigo de viaje o un rango de fechas para consultar la bitacora.'
      });
      return;
    }

    this.placasService.consultar({
      placa: filters.placa || undefined,
      codigoViaje: filters.codigoViaje || undefined,
      estadoPagoChofer: filters.estadoPagoChofer || undefined,
      fechaDesde: filters.fechaDesde || undefined,
      fechaHasta: filters.fechaHasta || undefined
    }).subscribe({
      next: (response) => {
        this.consulta = response;
        this.registros = response.registros;
        this.searched = true;
      },
      error: (error) => {
        this.consulta = null;
        this.registros = [];
        this.searched = true;
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected limpiar(): void {
    this.filtersForm.reset({
      placa: '',
      codigoViaje: '',
      estadoPagoChofer: 'TODOS',
      fechaDesde: '',
      fechaHasta: ''
    });
    this.consulta = null;
    this.registros = [];
    this.searched = false;
  }

  protected exportarExcel(): void {
    const filters = this.filtersForm.getRawValue();
    if (!this.hasActiveFilters()) {
      void this.popupService.info({
        title: 'Filtro requerido',
        message: 'Aplica al menos un filtro antes de exportar.'
      });
      return;
    }

    this.placasService.exportar({
      placa: filters.placa || undefined,
      codigoViaje: filters.codigoViaje || undefined,
      estadoPagoChofer: filters.estadoPagoChofer || undefined,
      fechaDesde: filters.fechaDesde || undefined,
      fechaHasta: filters.fechaHasta || undefined
    }).subscribe({
      next: (blob) => {
        this.saveBlob(blob, `placas_${filters.placa}.xlsx`);
      },
      error: async (error) => {
        void this.popupService.info({ title: 'Error', message: await this.getDownloadErrorMessage(error) });
      }
    });
  }

  protected canExport(): boolean {
    return this.searched && this.consulta !== null;
  }

  protected formatFecha(value: string | null): string {
    if (!value) {
      return '-';
    }
    return new Intl.DateTimeFormat('es-EC', {
      timeZone: PlacasPlaceholderComponent.ECUADOR_TIMEZONE,
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    }).format(new Date(`${value}T00:00:00-05:00`));
  }

  protected displayText(value: string | null): string {
    return value && value.trim().length > 0 ? value : '-';
  }

  protected displayAmountOrDash(value: number): string {
    return value > 0 ? new Intl.NumberFormat('es-EC', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value) : '-';
  }

  protected selectedPlacaLabel(): string | null {
    return this.filtersForm.getRawValue().placa || null;
  }

  protected buildPeriodoLabel(): string {
    const { fechaDesde, fechaHasta } = this.filtersForm.getRawValue();
    if (!fechaDesde && !fechaHasta) {
      return 'Sin rango de fechas';
    }
    return `Periodo: ${fechaDesde || '-'} a ${fechaHasta || '-'}`;
  }

  private hasActiveFilters(): boolean {
    const { placa, codigoViaje, estadoPagoChofer, fechaDesde, fechaHasta } = this.filtersForm.getRawValue();
    return !!placa || !!codigoViaje || !!fechaDesde || !!fechaHasta || estadoPagoChofer !== 'TODOS';
  }

  private loadVehiculos(): void {
    this.vehiculosService.list({ page: 0, size: 500, includeDeleted: false }).subscribe({
      next: (response) => {
        this.vehiculosCatalogo = response.content.filter((item) => !item.deleted);
        this.vehiculoOptions = [
          { value: '', label: 'Selecciona una placa', searchText: 'seleccionar placa' },
          ...this.vehiculosCatalogo.map((vehiculo) => ({
            value: vehiculo.placa,
            label: `${vehiculo.placa} | ${vehiculo.choferDefault}`,
            secondaryLabel: `${vehiculo.tipoDocumento} | ${vehiculo.documentoPersonal}`,
            searchText: `${vehiculo.placa} ${vehiculo.choferDefault} ${vehiculo.documentoPersonal} ${vehiculo.tipoDocumento}`
          }))
        ];
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  private getErrorMessage(error: unknown): string {
    const maybe = error as { error?: { message?: string } };
    return maybe?.error?.message ?? 'Ocurrio un error inesperado.';
  }

  private async getDownloadErrorMessage(error: unknown): Promise<string> {
    const maybe = error as { error?: Blob | { message?: string } };
    if (maybe?.error instanceof Blob) {
      try {
        const text = await maybe.error.text();
        const parsed = JSON.parse(text) as { message?: string };
        return parsed.message ?? 'Ocurrio un error inesperado.';
      } catch {
        return 'Ocurrio un error inesperado.';
      }
    }
    return this.getErrorMessage(error);
  }

  private saveBlob(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
}
