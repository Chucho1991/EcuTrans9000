import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { PopupService } from '../../../core/services/popup.service';
import { CatalogSearchOption, CatalogSearchSelectComponent } from '../../../shared/components/catalog-search-select/catalog-search-select.component';
import { DatePickerComponent } from '../../../shared/components/date-picker/date-picker.component';
import {
  DescuentoViajeResponse,
  DescuentosViajesService
} from '../../descuentos-viajes/services/descuentos-viajes.service';
import { VehiculoResponse, VehiculosService } from '../../vehiculos/services/vehiculos.service';
import { ConsultaPlacaDetalleResponse, ConsultaPlacaResponse, PlacasService } from '../services/placas.service';

type EstadoPagoChoferOption = 'TODOS' | 'PAGADOS' | 'NO_PAGADOS';

type ConsultaResumen = {
  valorFacturaTotal: number;
  retencionUnoPorciento: number;
  comisionAdministrativaSeisPorciento: number;
  anticiposTotal: number;
  totalDescuentos: number;
  pagoTotal: number;
};

@Component({
  selector: 'app-placas-placeholder',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, DatePickerComponent, CatalogSearchSelectComponent],
  template: `
    <section class="min-w-0 w-full space-y-6">
      <header class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="page-title">Consulta por placas</h1>
          <p class="page-subtitle">Consulta financiera por placa con chofer obligatorio, descuentos seleccionables y exportacion por viajes marcados.</p>
        </div>
        <button class="btn-outline-neutral px-4 py-2" type="button" (click)="exportarExcel()" [disabled]="!canExport()">
          Exportar Excel
        </button>
      </header>

      <article class="panel-card min-w-0 w-full max-w-full p-4">
        <form class="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-7" [formGroup]="filtersForm" (ngSubmit)="filtrar()">
          <label class="space-y-2">
            <span class="form-label form-label-required">Chofer por placa</span>
            <app-catalog-search-select
              formControlName="placa"
              placeholder="Selecciona una placa"
              searchPlaceholder="Buscar vehiculo por placa, documento o chofer"
              noResultsText="No hay vehiculos que coincidan."
              [options]="vehiculoOptions" />
            <p class="form-error" *ngIf="showFilterError('placa', 'required')">Chofer por placa es obligatorio. Selecciona la placa asociada al chofer.</p>
          </label>

          <label class="relative block space-y-2">
            <span class="form-label">Codigo de viaje</span>
            <span class="pointer-events-none absolute inset-y-0 left-3 top-7 flex items-center text-gray-400 dark:text-gray-500">
              <svg aria-hidden="true" class="h-4 w-4" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8">
                <path stroke-linecap="round" stroke-linejoin="round" d="m17.5 17.5-3.6-3.6m1.6-4.4a6 6 0 1 1-12 0 6 6 0 0 1 12 0Z" />
              </svg>
            </span>
            <input class="filter-control pl-10" type="search" formControlName="codigoViaje" placeholder="Buscar codigo de viaje" />
          </label>

          <label class="space-y-2">
            <span class="form-label">Estado pago chofer</span>
            <select class="filter-control" formControlName="estadoPagoChofer">
              <option *ngFor="let option of estadoPagoChoferOptions" [value]="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>

          <label class="space-y-2">
            <span class="form-label">Retencion 1%</span>
            <select class="filter-control" formControlName="aplicarRetencion">
              <option [ngValue]="true">Aplicar retencion</option>
              <option [ngValue]="false">No aplicar retencion</option>
            </select>
          </label>

          <label class="space-y-2">
            <span class="form-label form-label-required">Fecha inicio</span>
            <app-date-picker inputClass="filter-control" placeholder="Fecha desde" formControlName="fechaDesde" />
            <p class="form-error" *ngIf="showFilterError('fechaDesde', 'required')">Fecha inicio es obligatoria. Selecciona la fecha inicial del reporte.</p>
          </label>

          <label class="space-y-2">
            <span class="form-label form-label-required">Fecha fin</span>
            <app-date-picker inputClass="filter-control" placeholder="Fecha hasta" formControlName="fechaHasta" />
            <p class="form-error" *ngIf="showFilterError('fechaHasta', 'required')">Fecha fin es obligatoria. Selecciona la fecha final del reporte.</p>
          </label>

          <div class="flex flex-wrap items-end gap-2">
            <button class="btn-outline-neutral h-10 rounded-lg px-4 font-medium hover:bg-gray-100" type="submit">Filtrar</button>
            <button class="btn-outline-neutral h-10 rounded-lg px-4 font-medium hover:bg-gray-100" type="button" (click)="limpiar()">Limpiar</button>
          </div>
        </form>

        <div class="mt-4 grid grid-cols-1 gap-3 xl:grid-cols-[minmax(0,1fr)_auto]">
          <div class="space-y-2">
            <span class="form-label">Descuentos por motivo</span>
            <div class="catalog-select" [class.catalog-select-disabled]="!selectedVehiculo">
              <button
                class="catalog-select-trigger"
                type="button"
                [disabled]="!selectedVehiculo"
                [class.catalog-select-trigger-open]="descuentosOpen"
                (click)="toggleDescuentosPanel()">
                <span class="catalog-select-trigger-text">
                  {{ selectedDescuentosLabel() }}
                </span>
                <svg aria-hidden="true" class="h-4 w-4 shrink-0" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="m6 8 4 4 4-4" />
                </svg>
              </button>

              <div *ngIf="descuentosOpen" class="catalog-select-panel panel-card" (click)="$event.stopPropagation()">
                <label class="catalog-select-search">
                  <span class="catalog-select-search-icon">
                    <svg aria-hidden="true" class="h-4 w-4" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path stroke-linecap="round" stroke-linejoin="round" d="m17.5 17.5-3.6-3.6m1.6-4.4a6 6 0 1 1-12 0 6 6 0 0 1 12 0Z" />
                    </svg>
                  </span>
                  <input
                    class="catalog-select-search-input"
                    type="search"
                    placeholder="Buscar descuento por motivo"
                    [(ngModel)]="descuentoSearchTerm"
                    [ngModelOptions]="{ standalone: true }" />
                </label>

                <div class="catalog-select-options custom-scrollbar space-y-1">
                  <label
                    *ngFor="let descuento of filteredDescuentos"
                    class="flex cursor-pointer items-start gap-3 rounded-xl px-3 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-800/80">
                    <input
                      class="mt-1 h-4 w-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500"
                      type="checkbox"
                      [checked]="isDescuentoSelected(descuento.id)"
                      (change)="toggleDescuento(descuento.id, $any($event.target).checked)" />
                    <span class="min-w-0 flex-1">
                      <span class="block font-medium text-gray-900 dark:text-white">{{ descuento.descripcionMotivo }}</span>
                      <span class="block text-xs text-gray-500 dark:text-gray-400">
                        {{ descuento.fechaAplicacion || 'Sin fecha' }} | {{ descuento.montoMotivo | number: '1.2-2' }}
                      </span>
                    </span>
                  </label>

                  <p *ngIf="filteredDescuentos.length === 0" class="catalog-select-empty">
                    {{ descuentosDisponibles.length === 0 ? 'No hay descuentos activos para el chofer seleccionado.' : 'No hay descuentos que coincidan con la busqueda.' }}
                  </p>
                </div>
              </div>
            </div>
            <p class="text-xs text-gray-500 dark:text-gray-400">La busqueda por motivo usa los descuentos activos del chofer seleccionado.</p>
          </div>

          <div class="flex items-end justify-start xl:justify-end">
            <button class="btn-outline-neutral px-4 py-2" type="button" (click)="clearDescuentosSelection()" [disabled]="selectedDescuentoIds.length === 0">
              Limpiar descuentos
            </button>
          </div>
        </div>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full p-4 sm:p-6" *ngIf="consulta">
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ consulta.chofer || 'Chofer no disponible' }}</h2>
            <p class="text-sm text-gray-600 dark:text-gray-300">{{ consulta.placa || selectedPlacaLabel() || '-' }}</p>
          </div>
          <div class="text-right text-sm text-gray-500 dark:text-gray-400">
            <p>{{ buildPeriodoLabel() }}</p>
            <p>{{ selectedCountLabel() }}</p>
          </div>
        </div>

        <div class="mt-5 grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-6">
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Valor factura</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{{ resumenActual.valorFacturaTotal | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Retencion 1%</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{{ resumenActual.retencionUnoPorciento | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Comision 6%</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{{ resumenActual.comisionAdministrativaSeisPorciento | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Anticipos</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{{ resumenActual.anticiposTotal | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Total descuentos</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{{ resumenActual.totalDescuentos | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 bg-brand-500/5 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-500">Pago total</p>
            <p class="mt-1 text-lg font-semibold text-brand-700 dark:text-brand-300">{{ resumenActual.pagoTotal | number: '1.2-2' }}</p>
          </div>
        </div>

        <div class="mt-5 rounded-2xl border border-gray-200 bg-gray-50 p-4 dark:border-gray-800 dark:bg-gray-950/60">
          <div class="flex flex-wrap items-center justify-between gap-2">
            <p class="text-sm font-semibold text-gray-900 dark:text-white">Descuentos aplicados</p>
            <p class="text-xs text-gray-500 dark:text-gray-400">{{ descuentosSeleccionados.length }} seleccionados</p>
          </div>
          <div class="mt-3 space-y-2" *ngIf="descuentosSeleccionados.length > 0; else noDiscountsSelected">
            <div class="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-gray-200 px-3 py-2 text-sm dark:border-gray-800" *ngFor="let descuento of descuentosSeleccionados">
              <span class="text-gray-700 dark:text-gray-200">{{ descuento.fechaAplicacion || 'Sin fecha' }} - {{ descuento.descripcionMotivo }}</span>
              <span class="font-semibold text-gray-900 dark:text-white">{{ descuento.montoMotivo | number: '1.2-2' }}</span>
            </div>
          </div>
          <ng-template #noDiscountsSelected>
            <p class="mt-3 text-sm text-gray-500 dark:text-gray-400">No hay descuentos aplicados para este reporte.</p>
          </ng-template>
        </div>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full overflow-hidden">
        <div class="min-w-0 w-full max-w-full overflow-x-auto">
          <table class="w-full min-w-[1120px] table-auto text-left text-xs sm:text-sm">
            <thead class="bg-brand-900 text-white">
              <tr>
                <th class="px-3 py-3 font-semibold">
                  <label class="inline-flex items-center gap-2">
                    <input
                      class="h-4 w-4 rounded border-white/40 text-brand-300 focus:ring-brand-300"
                      type="checkbox"
                      [checked]="isAllSelected()"
                      [disabled]="registros.length === 0"
                      (change)="toggleAllRegistros($any($event.target).checked)" />
                    <span>Incluir</span>
                  </label>
                </th>
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
                <td class="px-3 py-3">
                  <input
                    class="h-4 w-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500"
                    type="checkbox"
                    [checked]="isRegistroSelected(registro.id)"
                    (change)="toggleRegistroSelection(registro.id, $any($event.target).checked)" />
                </td>
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
                <td class="px-4 py-8 text-center text-gray-500 dark:text-gray-400" colspan="11">
                  {{ searched ? 'No hay registros para los filtros seleccionados.' : 'La lista esta vacia. Selecciona placa, fechas y filtra para consultar la bitacora.' }}
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
  private readonly descuentosViajesService = inject(DescuentosViajesService);
  private readonly popupService = inject(PopupService);
  private readonly fb = inject(FormBuilder);
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  protected vehiculosCatalogo: VehiculoResponse[] = [];
  protected vehiculoOptions: CatalogSearchOption[] = [];
  protected consulta: ConsultaPlacaResponse | null = null;
  protected registros: ConsultaPlacaDetalleResponse[] = [];
  protected descuentosDisponibles: DescuentoViajeResponse[] = [];
  protected descuentosOpen = false;
  protected descuentoSearchTerm = '';
  protected selectedDescuentoIds: number[] = [];
  protected selectedViajeIds = new Set<string>();
  protected searched = false;
  protected readonly estadoPagoChoferOptions: Array<{ value: EstadoPagoChoferOption; label: string }> = [
    { value: 'TODOS', label: 'Todos los viajes' },
    { value: 'PAGADOS', label: 'Pagados al chofer' },
    { value: 'NO_PAGADOS', label: 'No pagados al chofer' }
  ];

  protected readonly filtersForm = this.fb.nonNullable.group({
    placa: ['', Validators.required],
    codigoViaje: [''],
    estadoPagoChofer: ['TODOS' as EstadoPagoChoferOption],
    aplicarRetencion: [true],
    fechaDesde: ['', Validators.required],
    fechaHasta: ['', Validators.required]
  });

  constructor() {
    this.loadVehiculos();
    this.filtersForm.controls.placa.valueChanges.subscribe((placa) => {
      this.syncDescuentosForPlaca(placa);
    });
  }

  protected get selectedVehiculo(): VehiculoResponse | undefined {
    const placa = this.filtersForm.controls.placa.getRawValue();
    return this.vehiculosCatalogo.find((vehiculo) => vehiculo.placa === placa);
  }

  protected get filteredDescuentos(): DescuentoViajeResponse[] {
    const normalized = this.normalize(this.descuentoSearchTerm);
    if (!normalized) {
      return this.descuentosDisponibles;
    }
    return this.descuentosDisponibles.filter((descuento) => {
      return this.normalize(`${descuento.descripcionMotivo} ${descuento.fechaAplicacion ?? ''}`).includes(normalized);
    });
  }

  protected get descuentosSeleccionados(): DescuentoViajeResponse[] {
    return this.descuentosDisponibles.filter((descuento) => this.selectedDescuentoIds.includes(descuento.id));
  }

  protected get resumenActual(): ConsultaResumen {
    const viajesSeleccionados = this.registros.filter((registro) => this.selectedViajeIds.has(registro.id));
    const valorFacturaTotal = this.round(viajesSeleccionados.reduce((sum, registro) => sum + Number(registro.valor ?? 0), 0));
    const anticiposTotal = this.round(viajesSeleccionados.reduce((sum, registro) => sum + Number(registro.anticipo ?? 0), 0));
    const totalDescuentos = this.round(this.descuentosSeleccionados.reduce((sum, descuento) => sum + Number(descuento.montoMotivo ?? 0), 0));
    const retencionUnoPorciento = this.filtersForm.controls.aplicarRetencion.getRawValue()
      ? this.round(valorFacturaTotal * 0.01)
      : 0;
    const comisionAdministrativaSeisPorciento = this.round(valorFacturaTotal * 0.06);
    const pagoTotal = this.round(
      valorFacturaTotal
      - retencionUnoPorciento
      - comisionAdministrativaSeisPorciento
      - anticiposTotal
      - totalDescuentos
    );

    return {
      valorFacturaTotal,
      retencionUnoPorciento,
      comisionAdministrativaSeisPorciento,
      anticiposTotal,
      totalDescuentos,
      pagoTotal
    };
  }

  protected filtrar(): void {
    this.filtersForm.markAllAsTouched();
    if (this.filtersForm.invalid) {
      return;
    }

    const filters = this.filtersForm.getRawValue();
    if (filters.fechaDesde && filters.fechaHasta && filters.fechaDesde > filters.fechaHasta) {
      void this.popupService.info({
        title: 'Rango invalido',
        message: 'La fecha inicio no puede ser mayor a la fecha fin.'
      });
      return;
    }

    this.placasService.consultar({
      placa: filters.placa,
      codigoViaje: filters.codigoViaje || undefined,
      estadoPagoChofer: filters.estadoPagoChofer || undefined,
      fechaDesde: filters.fechaDesde,
      fechaHasta: filters.fechaHasta,
      aplicarRetencion: Boolean(filters.aplicarRetencion)
    }).subscribe({
      next: (response) => {
        this.consulta = response;
        this.registros = response.registros;
        this.selectedViajeIds = new Set(response.registros.map((registro) => registro.id));
        this.searched = true;
      },
      error: (error) => {
        this.consulta = null;
        this.registros = [];
        this.selectedViajeIds.clear();
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
      aplicarRetencion: true,
      fechaDesde: '',
      fechaHasta: ''
    });
    this.consulta = null;
    this.registros = [];
    this.descuentosDisponibles = [];
    this.selectedDescuentoIds = [];
    this.selectedViajeIds.clear();
    this.descuentosOpen = false;
    this.descuentoSearchTerm = '';
    this.searched = false;
  }

  protected exportarExcel(): void {
    const filters = this.filtersForm.getRawValue();
    if (!this.canExport()) {
      void this.popupService.info({
        title: 'Seleccion requerida',
        message: 'Selecciona al menos un viaje para exportar el reporte.'
      });
      return;
    }

    this.placasService.exportar({
      placa: filters.placa,
      codigoViaje: filters.codigoViaje || undefined,
      estadoPagoChofer: filters.estadoPagoChofer || undefined,
      fechaDesde: filters.fechaDesde,
      fechaHasta: filters.fechaHasta,
      aplicarRetencion: Boolean(filters.aplicarRetencion),
      descuentoIds: this.selectedDescuentoIds,
      viajeIds: Array.from(this.selectedViajeIds)
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
    return this.searched && this.consulta !== null && this.selectedViajeIds.size > 0;
  }

  protected showFilterError(controlName: 'placa' | 'fechaDesde' | 'fechaHasta', errorKey: string): boolean {
    const control = this.filtersForm.get(controlName);
    return Boolean(control && control.touched && control.hasError(errorKey));
  }

  protected toggleDescuentosPanel(): void {
    if (!this.selectedVehiculo) {
      return;
    }
    this.descuentosOpen = !this.descuentosOpen;
  }

  protected selectedDescuentosLabel(): string {
    if (!this.selectedVehiculo) {
      return 'Selecciona primero una placa';
    }
    if (this.selectedDescuentoIds.length === 0) {
      return 'Selecciona uno o varios descuentos';
    }
    if (this.selectedDescuentoIds.length === 1) {
      return this.descuentosSeleccionados[0]?.descripcionMotivo ?? '1 descuento seleccionado';
    }
    return `${this.selectedDescuentoIds.length} descuentos seleccionados`;
  }

  protected isDescuentoSelected(id: number): boolean {
    return this.selectedDescuentoIds.includes(id);
  }

  protected toggleDescuento(id: number, checked: boolean): void {
    if (checked) {
      if (!this.selectedDescuentoIds.includes(id)) {
        this.selectedDescuentoIds = [...this.selectedDescuentoIds, id];
      }
      return;
    }
    this.selectedDescuentoIds = this.selectedDescuentoIds.filter((item) => item !== id);
  }

  protected clearDescuentosSelection(): void {
    this.selectedDescuentoIds = [];
  }

  protected isRegistroSelected(id: string): boolean {
    return this.selectedViajeIds.has(id);
  }

  protected toggleRegistroSelection(id: string, checked: boolean): void {
    const next = new Set(this.selectedViajeIds);
    if (checked) {
      next.add(id);
    } else {
      next.delete(id);
    }
    this.selectedViajeIds = next;
  }

  protected isAllSelected(): boolean {
    return this.registros.length > 0 && this.selectedViajeIds.size === this.registros.length;
  }

  protected toggleAllRegistros(checked: boolean): void {
    this.selectedViajeIds = checked
      ? new Set(this.registros.map((registro) => registro.id))
      : new Set<string>();
  }

  protected selectedCountLabel(): string {
    return `${this.selectedViajeIds.size} de ${this.registros.length} viajes seleccionados`;
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
    return this.filtersForm.controls.placa.getRawValue() || null;
  }

  protected buildPeriodoLabel(): string {
    const { fechaDesde, fechaHasta } = this.filtersForm.getRawValue();
    return `Periodo: ${fechaDesde || '-'} a ${fechaHasta || '-'}`;
  }

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: MouseEvent): void {
    if (!this.descuentosOpen) {
      return;
    }
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.descuentosOpen = false;
    }
  }

  private loadVehiculos(): void {
    this.vehiculosService.list({ page: 0, size: 500, includeDeleted: false }).subscribe({
      next: (response) => {
        this.vehiculosCatalogo = response.content.filter((item) => !item.deleted);
        this.vehiculoOptions = this.vehiculosCatalogo.map((vehiculo) => ({
          value: vehiculo.placa,
          label: `${vehiculo.placa} | ${vehiculo.choferDefault}`,
          secondaryLabel: `${vehiculo.tipoDocumento} | ${vehiculo.documentoPersonal}`,
          searchText: `${vehiculo.placa} ${vehiculo.choferDefault} ${vehiculo.documentoPersonal} ${vehiculo.tipoDocumento}`
        }));
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  private syncDescuentosForPlaca(placa: string): void {
    this.descuentosOpen = false;
    this.descuentoSearchTerm = '';
    this.selectedDescuentoIds = [];

    const vehiculo = this.vehiculosCatalogo.find((item) => item.placa === placa);
    if (!vehiculo) {
      this.descuentosDisponibles = [];
      return;
    }

    this.descuentosViajesService.list({
      page: 0,
      size: 500,
      vehiculoId: vehiculo.id,
      activo: true,
      includeDeleted: false
    }).subscribe({
      next: (response) => {
        this.descuentosDisponibles = response.content.filter((descuento) => descuento.activo && !descuento.deleted);
      },
      error: () => {
        this.descuentosDisponibles = [];
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

  private round(value: number): number {
    return Math.round((value + Number.EPSILON) * 100) / 100;
  }

  private normalize(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }
}
