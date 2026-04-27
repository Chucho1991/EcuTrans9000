import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { PopupService } from '../../../core/services/popup.service';
import { CatalogSearchOption, CatalogSearchSelectComponent } from '../../../shared/components/catalog-search-select/catalog-search-select.component';
import { AuthService } from '../../auth/services/auth.service';
import { DatePickerComponent } from '../../../shared/components/date-picker/date-picker.component';
import { ClienteResponse, ClientesService } from '../../clientes/services/clientes.service';
import { VehiculoResponse, VehiculosService } from '../../vehiculos/services/vehiculos.service';
import {
  BitacoraService,
  ViajeBitacoraImportResult,
  ViajeBitacoraResponse,
  ViajeBitacoraUpsertRequest
} from '../services/bitacora.service';

@Component({
  selector: 'app-bitacora-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, DatePickerComponent, CatalogSearchSelectComponent],
  template: `
    <section class="min-w-0 w-full space-y-6">
      <header class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="page-title">Bitacora</h1>
          <p class="page-subtitle">Registro operativo de viajes usando los catalogos de vehiculos y clientes.</p>
        </div>
        <div class="flex w-full flex-col gap-2 sm:w-auto sm:flex-row">
          <button class="btn-outline-neutral px-4 py-2" type="button" (click)="openImportModal()">Importar Excel</button>
          <button class="btn-outline-neutral px-4 py-2" type="button" (click)="downloadExcel()">Exportar Excel</button>
          <button class="btn-primary-brand" type="button" (click)="startCreate()">Nuevo viaje</button>
        </div>
      </header>

      <article class="panel-card min-w-0 w-full max-w-full p-4">
        <form class="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-5" [formGroup]="filtersForm" (ngSubmit)="loadViajes(0)">
          <input class="filter-control" formControlName="q" placeholder="Buscar por viaje, destino o factura" />
          <app-date-picker inputClass="filter-control" placeholder="Fecha desde" formControlName="fechaDesde" />
          <app-date-picker inputClass="filter-control" placeholder="Fecha hasta" formControlName="fechaHasta" />
          <app-catalog-search-select
            formControlName="vehiculoId"
            placeholder="Todos los vehiculos"
            searchPlaceholder="Buscar vehiculo por placa, documento o chofer"
            noResultsText="No hay vehiculos que coincidan."
            [options]="vehiculoFilterOptions" />
          <app-catalog-search-select
            formControlName="clienteId"
            placeholder="Todos los clientes"
            searchPlaceholder="Buscar cliente por documento o nombre"
            noResultsText="No hay clientes que coincidan."
            [options]="clienteFilterOptions" />
          <label *ngIf="canAdmin()" class="inline-flex items-center gap-2 text-sm text-gray-600 dark:text-gray-300">
            <input class="h-4 w-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500" type="checkbox" formControlName="includeDeleted" />
            Incluir eliminados
          </label>
          <div *ngIf="!canAdmin()"></div>
          <button class="btn-outline-neutral h-10 w-full rounded-lg font-medium hover:bg-gray-100" type="submit">Filtrar</button>
        </form>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full overflow-hidden">
        <div class="min-w-0 w-full max-w-full overflow-x-auto">
          <table class="w-full table-auto min-w-[1120px] text-left text-xs sm:text-sm lg:min-w-full">
            <thead class="border-b border-gray-200 bg-gray-50 text-gray-600 dark:border-gray-800 dark:bg-gray-950 dark:text-gray-300">
              <tr>
                <th class="px-3 py-3 sm:px-4">Viaje</th>
                <th class="px-3 py-3 sm:px-4">Fecha</th>
                <th class="px-3 py-3 sm:px-4">Vehiculo</th>
                <th class="px-3 py-3 sm:px-4">Cliente</th>
                <th class="px-3 py-3 sm:px-4">Destino</th>
                <th class="px-3 py-3 sm:px-4">Valor</th>
                <th class="px-3 py-3 sm:px-4">Costo chofer</th>
                <th class="px-3 py-3 sm:px-4">Retencion 1%</th>
                <th class="px-3 py-3 sm:px-4">Factura</th>
                <th class="px-3 py-3 sm:px-4">Fecha factura</th>
                <th class="px-3 py-3 sm:px-4">Fecha pago cliente</th>
                <th class="px-3 py-3 sm:px-4">Pago Cliente</th>
                <th class="px-3 py-3 sm:px-4">Pago Transportista</th>
                <th class="px-3 py-3 sm:px-4">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr class="border-b border-gray-100 dark:border-gray-800" *ngFor="let viaje of viajes">
                <td class="px-3 py-3 sm:px-4 font-semibold text-gray-900 dark:text-white">#{{ viaje.numeroViaje }}</td>
                <td class="px-3 py-3 sm:px-4">{{ viaje.fechaViaje }}</td>
                <td class="px-3 py-3 sm:px-4">
                  <div>{{ viaje.vehiculoPlaca }}</div>
                  <div class="text-xs text-gray-500 dark:text-gray-400">{{ viaje.vehiculoChofer }}</div>
                </td>
                <td class="px-3 py-3 sm:px-4">
                  <div>{{ viaje.clienteNombre }}</div>
                  <div class="text-xs text-gray-500 dark:text-gray-400">{{ viaje.clienteNombreComercial || 'Sin nombre comercial' }}</div>
                </td>
                <td class="px-3 py-3 sm:px-4">{{ viaje.destino }}</td>
                <td class="px-3 py-3 sm:px-4">&#36;{{ viaje.valor | number: '1.2-2' }}</td>
                <td class="px-3 py-3 sm:px-4">&#36;{{ viaje.costoChofer | number: '1.2-2' }}</td>
                <td class="px-3 py-3 sm:px-4">
                  <span
                    class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold"
                    [class]="viaje.aplicaRetencion
                      ? 'border-green-200 bg-green-50 text-green-700 dark:border-green-900/40 dark:bg-green-900/20 dark:text-green-300'
                      : 'border-red-200 bg-red-50 text-red-700 dark:border-red-900/40 dark:bg-red-900/20 dark:text-red-300'">
                    {{ viaje.aplicaRetencion ? 'SI' : 'NO' }}
                  </span>
                </td>
                <td class="px-3 py-3 sm:px-4">
                  <span *ngIf="viaje.deleted" class="mb-2 inline-flex rounded-full border border-orange-200 bg-orange-50 px-2.5 py-1 text-xs font-semibold text-orange-700 dark:border-orange-900/40 dark:bg-orange-900/20 dark:text-orange-300">
                    ELIMINADO
                  </span>
                  <span *ngIf="viaje.facturadoCliente; else noFacturado" class="inline-flex rounded-full border border-green-200 bg-green-50 px-2.5 py-1 text-xs font-semibold text-green-700 dark:border-green-900/40 dark:bg-green-900/20 dark:text-green-300">
                    {{ viaje.numeroFactura || 'Facturado' }}
                  </span>
                  <ng-template #noFacturado>
                    <span class="inline-flex rounded-full border border-gray-200 bg-gray-50 px-2.5 py-1 text-xs font-semibold text-gray-600 dark:border-gray-800 dark:bg-gray-900 dark:text-gray-300">Pendiente</span>
                  </ng-template>
                </td>
                <td class="px-3 py-3 sm:px-4">
                  <span *ngIf="viaje.fechaFactura; else fechaFacturaPendiente" class="font-bold text-green-600 dark:text-green-300">
                    {{ viaje.fechaFactura }}
                  </span>
                  <ng-template #fechaFacturaPendiente>
                    <span class="inline-flex rounded-full border border-yellow-200 bg-yellow-50 px-2.5 py-1 text-xs font-semibold text-yellow-700 dark:border-yellow-900/40 dark:bg-yellow-900/20 dark:text-yellow-300">
                      Pendiente
                    </span>
                  </ng-template>
                </td>
                <td class="px-3 py-3 sm:px-4">
                  <span *ngIf="viaje.fechaPagoCliente; else fechaPagoPendiente" class="font-bold text-green-600 dark:text-green-300">
                    {{ viaje.fechaPagoCliente }}
                  </span>
                  <ng-template #fechaPagoPendiente>
                    <span class="inline-flex rounded-full border border-yellow-200 bg-yellow-50 px-2.5 py-1 text-xs font-semibold text-yellow-700 dark:border-yellow-900/40 dark:bg-yellow-900/20 dark:text-yellow-300">
                      Pendiente
                    </span>
                  </ng-template>
                </td>
                <td class="px-3 py-3 sm:px-4">
                  <span *ngIf="viaje.facturadoCliente" class="inline-flex rounded-full border border-blue-light-200 bg-blue-light-50 px-2.5 py-1 text-xs font-semibold text-blue-light-700">Facturado</span>
                  <span *ngIf="!viaje.facturadoCliente" class="inline-flex rounded-full border border-orange-200 bg-orange-50 px-2.5 py-1 text-xs font-semibold text-orange-700 dark:border-orange-900/40 dark:bg-orange-900/20 dark:text-orange-300">Pendiente</span>
                </td>
                <td class="px-3 py-3 sm:px-4">
                  <span *ngIf="viaje.pagadoTransportista" class="inline-flex rounded-full border border-blue-light-200 bg-blue-light-50 px-2.5 py-1 text-xs font-semibold text-blue-light-700">Pagado</span>
                  <span *ngIf="!viaje.pagadoTransportista" class="inline-flex rounded-full border border-orange-200 bg-orange-50 px-2.5 py-1 text-xs font-semibold text-orange-700 dark:border-orange-900/40 dark:bg-orange-900/20 dark:text-orange-300">Pendiente</span>
                </td>
                <td class="px-3 py-3 sm:px-4">
                  <div class="flex flex-wrap gap-1.5 sm:gap-2">
                    <button class="icon-action-btn" type="button" aria-label="Ver" (click)="selectDetail(viaje)">
                      <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.8"/></svg>
                      <span class="icon-action-tooltip">Ver</span>
                    </button>
                    <button *ngIf="!viaje.deleted" class="icon-action-btn" type="button" aria-label="Editar" (click)="startEdit(viaje)">
                      <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="m3 21 3.8-1 10-10a2.1 2.1 0 0 0-3-3l-10 10L3 21Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/><path d="m13.5 6.5 3 3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                      <span class="icon-action-tooltip">Editar</span>
                    </button>
                    <button *ngIf="canAdmin() && !viaje.deleted" class="icon-action-btn text-orange-600 hover:text-orange-700" type="button" aria-label="Eliminar" (click)="softDelete(viaje)">
                      <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M3 6h18M8 6V4h8v2m-9 0 1 14h8l1-14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                      <span class="icon-action-tooltip">Soft delete</span>
                    </button>
                    <button *ngIf="canAdmin() && viaje.deleted" class="icon-action-btn text-green-600 hover:text-green-700" type="button" aria-label="Restaurar" (click)="restore(viaje)">
                      <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M3 12a9 9 0 1 0 2.6-6.4M3 4v5h5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                      <span class="icon-action-tooltip">Restaurar</span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="viajes.length === 0">
                <td class="px-4 py-4 text-center text-gray-500 dark:text-gray-400" colspan="14">No hay viajes registrados.</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="flex flex-wrap items-center justify-between gap-2 overflow-x-auto px-4 py-3 text-sm text-gray-600 dark:text-gray-300">
          <span class="basis-full sm:basis-auto">Pagina {{ page + 1 }} de {{ totalPages || 1 }}</span>
          <div class="flex min-w-[220px] w-full gap-2 sm:w-auto sm:grow-0">
            <button class="btn-outline-neutral flex-1 px-3 py-1 disabled:opacity-50 sm:flex-none" type="button" (click)="loadViajes(page - 1)" [disabled]="page === 0">Anterior</button>
            <button class="btn-outline-neutral flex-1 px-3 py-1 disabled:opacity-50 sm:flex-none" type="button" (click)="loadViajes(page + 1)" [disabled]="page + 1 >= totalPages">Siguiente</button>
          </div>
        </div>
      </article>

      <article class="panel-card min-w-0 w-full max-w-full p-4 sm:p-6" *ngIf="selectedViaje">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Detalle de viaje</h2>
        <div class="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div>
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Viaje</p>
            <p class="mt-1 text-sm text-gray-700 dark:text-gray-200">#{{ selectedViaje.numeroViaje }}</p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Fecha</p>
            <p class="mt-1 text-sm text-gray-700 dark:text-gray-200">{{ selectedViaje.fechaViaje }}</p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Vehiculo</p>
            <p class="mt-1 text-sm text-gray-700 dark:text-gray-200">{{ selectedViaje.vehiculoPlaca }} | {{ selectedViaje.vehiculoTonelajeCategoria }}</p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Cliente</p>
            <p class="mt-1 text-sm text-gray-700 dark:text-gray-200">{{ selectedViaje.clienteNombre }}</p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Destino</p>
            <p class="mt-1 text-sm text-gray-700 dark:text-gray-200">{{ selectedViaje.destino }}</p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Detalle</p>
            <p class="mt-1 text-sm text-gray-700 dark:text-gray-200">{{ selectedViaje.detalleViaje || '-' }}</p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Pagado cliente</p>
            <p class="mt-1 text-sm text-gray-700 dark:text-gray-200">{{ selectedViaje.facturadoCliente ? (selectedViaje.numeroFactura || 'SI') : 'NO' }}</p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Pago transportista</p>
            <p class="mt-1 text-sm text-gray-700 dark:text-gray-200">{{ selectedViaje.pagadoTransportista ? 'SI' : 'NO' }}</p>
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Aplica retencion 1%</p>
            <p class="mt-1 text-sm text-gray-700 dark:text-gray-200">{{ selectedViaje.aplicaRetencion ? 'SI' : 'NO' }}</p>
          </div>
        </div>
        <div class="mt-4 grid grid-cols-1 gap-4 md:grid-cols-3">
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Valor</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">&#36;{{ selectedViaje.valor | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Costo chofer</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">&#36;{{ selectedViaje.costoChofer | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Estiba</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">&#36;{{ selectedViaje.estiba | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Anticipo</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">&#36;{{ selectedViaje.anticipo | number: '1.2-2' }}</p>
          </div>
        </div>
        <div class="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-5">
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Valor factura</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">&#36;{{ selectedViaje.valor | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Retencion 1%</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">&#36;{{ calculateRetencion(selectedViaje.valor, selectedViaje.aplicaRetencion) | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Comision 6%</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">&#36;{{ calculateComision(selectedViaje.valor) | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-400">Anticipos</p>
            <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">&#36;{{ selectedViaje.anticipo | number: '1.2-2' }}</p>
          </div>
          <div class="rounded-xl border border-brand-500/20 bg-brand-500/5 p-4 dark:border-gray-800">
            <p class="text-xs uppercase tracking-[0.2em] text-gray-500">Pago total</p>
            <p class="mt-1 text-lg font-semibold text-brand-700 dark:text-brand-300">&#36;{{ calculatePagoTotal(selectedViaje) | number: '1.2-2' }}</p>
          </div>
        </div>
        <p class="mt-4 text-sm text-gray-600 dark:text-gray-300"><strong>Observaciones:</strong> {{ selectedViaje.observaciones || '-' }}</p>
      </article>

      <div class="fixed inset-0 z-50 flex items-center justify-center bg-gray-950/70 p-4" *ngIf="mode !== 'none'">
        <article class="panel-card w-full max-w-6xl p-5 sm:p-6 lg:p-7">
        <div class="flex flex-wrap items-start justify-between gap-2">
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ mode === 'create' ? 'Registrar viaje' : 'Editar viaje' }}</h2>
            <span class="text-xs text-gray-500 dark:text-gray-400">Campos alineados con la bitacora operativa</span>
          </div>
          <button class="icon-action-btn" type="button" aria-label="Cerrar" (click)="cancelForm()">
            <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M6 6l12 12M18 6 6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" /></svg>
            <span class="icon-action-tooltip">Cerrar</span>
          </button>
        </div>

        <form class="mt-5 min-w-0 w-full grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-12" [formGroup]="viajeForm" (ngSubmit)="submitViaje()">
          <div class="min-w-0 xl:col-span-2">
            <label class="form-label form-label-required">Nro. Viaje</label>
            <input class="form-control" type="number" min="1" formControlName="numeroViaje" />
            <p class="form-error" *ngIf="showError('numeroViaje', 'required')">
              Nro. Viaje es obligatorio. Ingresa el consecutivo del viaje.
            </p>
            <p class="form-error" *ngIf="showError('numeroViaje', 'min')">
              Nro. Viaje tiene formato invalido. Ingresa un valor mayor o igual a 1.
            </p>
          </div>
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label form-label-required">Fecha viaje</label>
            <app-date-picker inputClass="form-control" placeholder="Selecciona la fecha del viaje" formControlName="fechaViaje" />
            <p class="form-error" *ngIf="showError('fechaViaje', 'required')">
              Fecha viaje es obligatoria. Selecciona la fecha del viaje.
            </p>
          </div>
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label form-label-required">Vehiculo</label>
            <app-catalog-search-select
              formControlName="vehiculoId"
              placeholder="Selecciona un vehiculo"
              searchPlaceholder="Buscar vehiculo por placa, documento o chofer"
              noResultsText="No hay vehiculos disponibles."
              [options]="vehiculoFormOptions" />
            <p class="form-error" *ngIf="showError('vehiculoId', 'required')">
              Vehiculo es obligatorio. Selecciona un vehiculo del catalogo.
            </p>
          </div>
          <div class="min-w-0 xl:col-span-4">
            <label class="form-label form-label-required">Cliente</label>
            <app-catalog-search-select
              formControlName="clienteId"
              placeholder="Selecciona un cliente"
              searchPlaceholder="Buscar cliente por documento o nombre"
              noResultsText="No hay clientes disponibles."
              [options]="clienteFormOptions" />
            <p class="form-error" *ngIf="showError('clienteId', 'required')">
              Cliente es obligatorio. Selecciona un cliente del catalogo.
            </p>
          </div>

          <div class="min-w-0 xl:col-span-4">
            <label class="form-label form-label-required">Destino</label>
            <ng-container *ngIf="usesEquivalenciasForSelectedCliente(); else manualDestinoField">
              <app-catalog-search-select
                formControlName="destino"
                placeholder="Selecciona un destino"
                searchPlaceholder="Buscar destino"
                noResultsText="No hay destinos que coincidan."
                [options]="destinoEquivalenciaFormOptions" />
            </ng-container>
            <ng-template #manualDestinoField>
              <input class="form-control" formControlName="destino" />
            </ng-template>
            <p class="form-error" *ngIf="showError('destino', 'required')">
              Destino es obligatorio. {{ usesEquivalenciasForSelectedCliente() ? 'Selecciona un destino disponible para el cliente.' : 'Ingresa el destino del viaje.' }}
            </p>
          </div>
          <div class="min-w-0 xl:col-span-8">
            <label class="form-label">Detalle viaje</label>
            <input class="form-control" formControlName="detalleViaje" />
          </div>

          <div class="min-w-0 xl:col-span-2">
            <label class="form-label form-label-required">Valor</label>
            <input class="form-control" type="number" min="0" step="0.01" formControlName="valor" [readonly]="usesEquivalenciasForSelectedCliente()" />
            <p class="form-error" *ngIf="showError('valor', 'required')">
              Valor es obligatorio. Ingresa el valor del viaje.
            </p>
            <p class="form-error" *ngIf="showError('valor', 'min')">
              Valor tiene formato invalido. Ingresa un valor igual o mayor a 0.
            </p>
          </div>
          <div class="min-w-0 xl:col-span-2">
            <label class="form-label form-label-required">Costo chofer</label>
            <input class="form-control" type="number" min="0" step="0.01" formControlName="costoChofer" [readonly]="usesEquivalenciasForSelectedCliente()" />
            <p class="form-error" *ngIf="showError('costoChofer', 'required')">
              Costo chofer es obligatorio. Ingresa el valor a pagar al chofer.
            </p>
            <p class="form-error" *ngIf="showError('costoChofer', 'min')">
              Costo chofer tiene formato invalido. Ingresa un valor igual o mayor a 0.
            </p>
          </div>
          <div class="min-w-0 xl:col-span-2">
            <label class="form-label form-label-required">Estiba</label>
            <input class="form-control" type="number" min="0" step="0.01" formControlName="estiba" />
            <p class="form-error" *ngIf="showError('estiba', 'required')">
              Estiba es obligatoria. Ingresa el valor de estiba.
            </p>
            <p class="form-error" *ngIf="showError('estiba', 'min')">
              Estiba tiene formato invalido. Ingresa un valor igual o mayor a 0.
            </p>
          </div>
          <div class="min-w-0 xl:col-span-2">
            <label class="form-label form-label-required">Anticipo</label>
            <input class="form-control" type="number" min="0" step="0.01" formControlName="anticipo" />
            <p class="form-error" *ngIf="showError('anticipo', 'required')">
              Anticipo es obligatorio. Ingresa el valor del anticipo.
            </p>
            <p class="form-error" *ngIf="showError('anticipo', 'min')">
              Anticipo tiene formato invalido. Ingresa un valor igual o mayor a 0.
            </p>
          </div>
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label form-label-required">Aplica retencion 1%</label>
            <label class="flex h-11 items-center gap-3 rounded-xl border border-gray-200 px-4 text-sm font-medium text-gray-700 dark:border-gray-800 dark:text-gray-200">
              <input class="h-4 w-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500" type="checkbox" formControlName="aplicaRetencion" />
              <span>{{ viajeForm.get('aplicaRetencion')?.value ? 'SI' : 'NO' }}</span>
            </label>
          </div>
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label form-label-required">Pagado cliente</label>
            <label class="flex h-11 items-center gap-3 rounded-xl border border-gray-200 px-4 text-sm font-medium text-gray-700 dark:border-gray-800 dark:text-gray-200">
              <input class="h-4 w-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500" type="checkbox" formControlName="facturadoCliente" />
              <span>{{ viajeForm.get('facturadoCliente')?.value ? 'SI' : 'NO' }}</span>
            </label>
          </div>
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label form-label-required">Pagado transportista</label>
            <label class="flex h-11 items-center gap-3 rounded-xl border border-gray-200 px-4 text-sm font-medium text-gray-700 dark:border-gray-800 dark:text-gray-200">
              <input class="h-4 w-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500" type="checkbox" formControlName="pagadoTransportista" />
              <span>{{ viajeForm.get('pagadoTransportista')?.value ? 'SI' : 'NO' }}</span>
            </label>
          </div>

          <div class="min-w-0 xl:col-span-3">
            <label class="form-label">Nro. Factura</label>
            <input class="form-control" formControlName="numeroFactura" />
          </div>
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label">Fecha factura</label>
            <app-date-picker inputClass="form-control" placeholder="Selecciona la fecha de factura" formControlName="fechaFactura" />
          </div>
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label">Fecha pago cliente</label>
            <app-date-picker inputClass="form-control" placeholder="Selecciona la fecha de pago" formControlName="fechaPagoCliente" />
          </div>
          <div class="min-w-0 xl:col-span-3">
            <label class="form-label">Resumen catalogo</label>
            <div class="flex h-11 items-center rounded-xl border border-gray-200 px-3 text-sm text-gray-600 dark:border-gray-800 dark:text-gray-300">
              {{ selectedVehiculoResumen() || 'Selecciona vehiculo' }}
            </div>
          </div>

          <div class="min-w-0 xl:col-span-12">
            <label class="form-label">Observaciones</label>
            <textarea class="form-control !h-24 !py-2" formControlName="observaciones"></textarea>
          </div>

          <div class="min-w-0 flex w-full flex-col-reverse gap-2 pt-2 sm:flex-row sm:justify-end md:col-span-2 xl:col-span-12">
            <button class="w-full rounded-xl border border-gray-300 px-4 py-2.5 text-sm dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800 sm:w-auto" type="button" (click)="cancelForm()">Cancelar</button>
            <button class="w-full rounded-xl bg-brand-500 px-5 py-2.5 text-sm font-semibold text-white hover:bg-brand-600 sm:w-auto" type="submit">{{ mode === 'create' ? 'Registrar' : 'Guardar cambios' }}</button>
          </div>
        </form>
        </article>
      </div>

      <div class="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/70 p-4" *ngIf="showImportModal">
        <article class="panel-card w-full max-w-3xl p-5 sm:p-6">
          <div class="flex items-start justify-between gap-3">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Importar viajes Excel</h3>
            <button class="btn-outline-neutral px-3 py-1" type="button" (click)="closeImportModal()">Cerrar</button>
          </div>

          <div class="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2">
            <button class="btn-outline-neutral inline-flex items-center justify-center gap-2 px-4 py-2" type="button" (click)="downloadTemplate()">
              <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M12 3v12m0 0 4-4m-4 4-4-4M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
              Descargar plantilla
            </button>
            <button class="btn-outline-neutral inline-flex items-center justify-center gap-2 px-4 py-2" type="button" (click)="downloadExampleTemplate()">
              <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M12 3v12m0 0 4-4m-4 4-4-4M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
              Descargar plantilla con ejemplo
            </button>
            <label class="space-y-2">
              <span class="form-label">Modo</span>
              <select class="form-control" [(ngModel)]="importMode" [ngModelOptions]="{ standalone: true }">
                <option value="INSERT_ONLY">Solo insertar</option>
                <option value="UPSERT">Insertar o actualizar</option>
              </select>
            </label>
            <label class="mt-8 inline-flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
              <input type="checkbox" [(ngModel)]="partialOk" [ngModelOptions]="{ standalone: true }" />
              Continuar con filas validas
            </label>
            <input class="form-control md:col-span-2" type="file" accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" (change)="onImportFileChange($event)" />
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
export class BitacoraListComponent {
  private static readonly ECUADOR_TIMEZONE = 'America/Guayaquil';

  private readonly bitacoraService = inject(BitacoraService);
  private readonly vehiculosService = inject(VehiculosService);
  private readonly clientesService = inject(ClientesService);
  private readonly popupService = inject(PopupService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  protected viajes: ViajeBitacoraResponse[] = [];
  protected vehiculosCatalogo: VehiculoResponse[] = [];
  protected clientesCatalogo: ClienteResponse[] = [];
  protected vehiculoFilterOptions: CatalogSearchOption[] = [];
  protected clienteFilterOptions: CatalogSearchOption[] = [];
  protected vehiculoFormOptions: CatalogSearchOption[] = [];
  protected clienteFormOptions: CatalogSearchOption[] = [];
  protected destinoEquivalenciaFormOptions: CatalogSearchOption[] = [];
  protected selectedViaje: ViajeBitacoraResponse | null = null;
  protected mode: 'none' | 'create' | 'edit' = 'none';
  protected editingId: string | null = null;
  protected page = 0;
  protected readonly size = 10;
  protected totalPages = 0;
  protected showImportModal = false;
  protected importFile: File | null = null;
  protected importMode: 'INSERT_ONLY' | 'UPSERT' = 'INSERT_ONLY';
  protected partialOk = true;
  protected importPreview: ViajeBitacoraImportResult | null = null;

  protected readonly filtersForm = this.fb.nonNullable.group({
    q: [''],
    fechaDesde: [''],
    fechaHasta: [''],
    vehiculoId: [''],
    clienteId: [''],
    includeDeleted: [false]
  });

  protected readonly viajeForm = this.fb.group({
    numeroViaje: [1, [Validators.required, Validators.min(1)]],
    fechaViaje: [this.today(), [Validators.required]],
    vehiculoId: ['', [Validators.required]],
    clienteId: ['', [Validators.required]],
    destino: ['', [Validators.required]],
    detalleViaje: [''],
    valor: [0, [Validators.required, Validators.min(0)]],
    costoChofer: [0, [Validators.required, Validators.min(0)]],
    estiba: [0, [Validators.required, Validators.min(0)]],
    anticipo: [0, [Validators.required, Validators.min(0)]],
    aplicaRetencion: [false, [Validators.required]],
    facturadoCliente: [false, [Validators.required]],
    numeroFactura: [''],
    fechaFactura: [''],
    fechaPagoCliente: [''],
    pagadoTransportista: [false, [Validators.required]],
    observaciones: ['']
  });

  constructor() {
    this.viajeForm.get('clienteId')?.valueChanges.subscribe((clienteId) => {
      this.syncClienteEquivalencias(clienteId ?? '');
    });
    this.viajeForm.get('destino')?.valueChanges.subscribe((destino) => {
      this.syncValoresDesdeDestino(destino ?? '');
    });
    this.loadCatalogos();
    this.loadViajes(0);
  }

  protected canAdmin(): boolean {
    return this.authService.getRole() === 'SUPERADMINISTRADOR';
  }

  protected loadCatalogos(): void {
    forkJoin({
      vehiculos: this.vehiculosService.list({ page: 0, size: 200, estado: 'ACTIVO', includeDeleted: false }),
      clientes: this.clientesService.list({ page: 0, size: 200, includeDeleted: false })
    }).subscribe({
      next: ({ vehiculos, clientes }) => {
        this.vehiculosCatalogo = vehiculos.content.filter((item) => !item.deleted && item.estado === 'ACTIVO');
        this.clientesCatalogo = clientes.content.filter((item) => !item.deleted && item.activo);
        this.vehiculoFilterOptions = [
          { value: '', label: 'Todos los vehiculos', searchText: 'todos vehiculos' },
          ...this.vehiculosCatalogo.map((vehiculo) => this.mapVehiculoOption(vehiculo, false))
        ];
        this.clienteFilterOptions = [
          { value: '', label: 'Todos los clientes', searchText: 'todos clientes' },
          ...this.clientesCatalogo.map((cliente) => this.mapClienteOption(cliente, false))
        ];
        this.vehiculoFormOptions = this.vehiculosCatalogo.map((vehiculo) => this.mapVehiculoOption(vehiculo, true));
        this.clienteFormOptions = this.clientesCatalogo.map((cliente) => this.mapClienteOption(cliente, true));
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected loadViajes(page: number): void {
    const safePage = page < 0 ? 0 : page;
    const filters = this.filtersForm.getRawValue();

    this.bitacoraService.list({
      page: safePage,
      size: this.size,
      q: filters.q || undefined,
      fechaDesde: filters.fechaDesde || undefined,
      fechaHasta: filters.fechaHasta || undefined,
      vehiculoId: filters.vehiculoId || undefined,
      clienteId: filters.clienteId || undefined,
      includeDeleted: this.canAdmin() && Boolean(filters.includeDeleted)
    }).subscribe({
      next: (response) => {
        this.viajes = response.content;
        this.page = response.page;
        this.totalPages = response.totalPages;
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected selectDetail(viaje: ViajeBitacoraResponse): void {
    this.bitacoraService.getById(viaje.id).subscribe({
      next: (detail) => {
        this.selectedViaje = detail;
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected startCreate(): void {
    this.mode = 'create';
    this.editingId = null;
    this.viajeForm.reset({
      numeroViaje: this.nextNumeroViaje(),
      fechaViaje: this.today(),
      vehiculoId: '',
      clienteId: '',
      destino: '',
      detalleViaje: '',
      valor: 0,
      costoChofer: 0,
      estiba: 0,
      anticipo: 0,
      aplicaRetencion: false,
      facturadoCliente: false,
      numeroFactura: '',
      fechaFactura: '',
      fechaPagoCliente: '',
      pagadoTransportista: false,
      observaciones: ''
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
    this.bitacoraService.downloadTemplate().subscribe({
      next: (blob) => {
        this.saveBlob(blob, 'bitacora_template.xlsx');
      },
      error: async (error) => {
        void this.popupService.info({ title: 'Error', message: await this.getDownloadErrorMessage(error) });
      }
    });
  }

  protected downloadExampleTemplate(): void {
    this.bitacoraService.downloadExampleTemplate().subscribe({
      next: (blob) => {
        this.saveBlob(blob, 'bitacora_template_ejemplo.xlsx');
      },
      error: async (error) => {
        void this.popupService.info({ title: 'Error', message: await this.getDownloadErrorMessage(error) });
      }
    });
  }

  protected previewImport(): void {
    if (!this.importFile) {
      return;
    }
    this.bitacoraService.previewImport(this.importFile, this.importMode, this.partialOk).subscribe({
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
    this.bitacoraService.importExcel(this.importFile, this.importMode, this.partialOk).subscribe({
      next: (result) => {
        this.importPreview = result;
        void this.popupService.info({
          title: 'Importacion finalizada',
          message: `Procesadas ${result.processed} filas. Insertadas: ${result.inserted}. Actualizadas: ${result.updated}.`
        });
        this.closeImportModal();
        this.loadViajes(0);
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected startEdit(viaje: ViajeBitacoraResponse): void {
    if (viaje.deleted) {
      return;
    }
    this.mode = 'edit';
    this.editingId = viaje.id;
    this.viajeForm.reset({
      numeroViaje: viaje.numeroViaje,
      fechaViaje: viaje.fechaViaje,
      vehiculoId: viaje.vehiculoId,
      clienteId: viaje.clienteId,
      destino: viaje.destino,
      detalleViaje: viaje.detalleViaje || '',
      valor: viaje.valor,
      costoChofer: viaje.costoChofer,
      estiba: viaje.estiba,
      anticipo: viaje.anticipo,
      aplicaRetencion: viaje.aplicaRetencion,
      facturadoCliente: viaje.facturadoCliente,
      numeroFactura: viaje.numeroFactura || '',
      fechaFactura: viaje.fechaFactura || '',
      fechaPagoCliente: viaje.fechaPagoCliente || '',
      pagadoTransportista: viaje.pagadoTransportista,
      observaciones: viaje.observaciones || ''
    });
  }

  protected cancelForm(): void {
    this.mode = 'none';
    this.editingId = null;
  }

  protected async softDelete(viaje: ViajeBitacoraResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Eliminar logico',
      message: `Eliminar logicamente el viaje #${viaje.numeroViaje}?`
    });
    if (!confirmed) {
      return;
    }
    this.bitacoraService.softDelete(viaje.id).subscribe({
      next: () => {
        if (this.selectedViaje?.id === viaje.id) {
          this.selectedViaje = null;
        }
        this.loadViajes(this.page);
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected async restore(viaje: ViajeBitacoraResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Restaurar viaje',
      message: `Restaurar el viaje #${viaje.numeroViaje}?`
    });
    if (!confirmed) {
      return;
    }
    this.bitacoraService.restore(viaje.id).subscribe({
      next: () => {
        this.loadViajes(this.page);
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected selectedVehiculoResumen(): string | null {
    const vehiculoId = this.viajeForm.getRawValue().vehiculoId;
    const vehiculo = this.vehiculosCatalogo.find((item) => item.id === vehiculoId);
    if (!vehiculo) {
      return null;
    }
    return `${vehiculo.placa} | ${vehiculo.tonelajeCategoria} | ${vehiculo.m3} m3`;
  }

  protected async submitViaje(): Promise<void> {
    this.viajeForm.markAllAsTouched();
    if (this.viajeForm.invalid) {
      return;
    }

    const value = this.viajeForm.getRawValue();
    const payload: ViajeBitacoraUpsertRequest = {
      numeroViaje: Number(value.numeroViaje ?? 0),
      fechaViaje: value.fechaViaje ?? '',
      vehiculoId: value.vehiculoId ?? '',
      clienteId: value.clienteId ?? '',
      destino: value.destino ?? '',
      detalleViaje: value.detalleViaje ?? '',
      valor: Number(value.valor ?? 0),
      costoChofer: Number(value.costoChofer ?? 0),
      estiba: Number(value.estiba ?? 0),
      anticipo: Number(value.anticipo ?? 0),
      aplicaRetencion: !!value.aplicaRetencion,
      facturadoCliente: !!value.facturadoCliente,
      numeroFactura: value.numeroFactura ?? '',
      fechaFactura: value.fechaFactura || null,
      fechaPagoCliente: value.fechaPagoCliente || null,
      pagadoTransportista: !!value.pagadoTransportista,
      observaciones: value.observaciones ?? ''
    };

    if (this.mode === 'create') {
      const confirmed = await this.popupService.confirm({
        title: 'Registrar viaje',
        message: `Vas a registrar el viaje #${payload.numeroViaje}. ¿Deseas continuar?`
      });
      if (!confirmed) {
        return;
      }
      this.bitacoraService.create(payload).subscribe({
        next: () => {
          void this.popupService.info({ title: 'Viaje registrado', message: 'El viaje fue registrado correctamente.' });
          this.cancelForm();
          this.loadViajes(0);
        },
        error: (error) => {
          void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
        }
      });
      return;
    }

    if (this.mode === 'edit' && this.editingId) {
      const confirmed = await this.popupService.confirm({
        title: 'Editar viaje',
        message: `Vas a actualizar el viaje #${payload.numeroViaje}. ¿Deseas continuar?`
      });
      if (!confirmed) {
        return;
      }
      this.bitacoraService.update(this.editingId, payload).subscribe({
        next: () => {
          void this.popupService.info({ title: 'Viaje actualizado', message: 'El viaje fue actualizado correctamente.' });
          this.cancelForm();
          this.loadViajes(this.page);
        },
        error: (error) => {
          void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
        }
      });
    }
  }

  protected downloadExcel(): void {
    const filters = this.filtersForm.getRawValue();
    this.bitacoraService.downloadExcel({
      q: filters.q || undefined,
      fechaDesde: filters.fechaDesde || undefined,
      fechaHasta: filters.fechaHasta || undefined,
      vehiculoId: filters.vehiculoId || undefined,
      clienteId: filters.clienteId || undefined
    }).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'bitacora_viajes.xlsx';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        setTimeout(() => URL.revokeObjectURL(url), 1000);
      },
      error: async (error) => {
        void this.popupService.info({ title: 'Error', message: await this.getDownloadErrorMessage(error) });
      }
    });
  }

  protected showError(controlName: string, errorKey: string): boolean {
    const control = this.viajeForm.get(controlName);
    return !!control && control.touched && control.hasError(errorKey);
  }

  protected usesEquivalenciasForSelectedCliente(): boolean {
    return this.selectedClienteEquivalencias().length > 0;
  }

  protected selectedClienteEquivalencias(): ClienteResponse['equivalencias'] {
    const clienteId = this.viajeForm.getRawValue().clienteId;
    const cliente = this.clientesCatalogo.find((item) => item.id === clienteId);
    return cliente?.aplicaTablaEquivalencia ? cliente.equivalencias : [];
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

  private nextNumeroViaje(): number {
    if (this.viajes.length === 0) {
      return 1;
    }
    return Math.max(...this.viajes.map((item) => item.numeroViaje)) + 1;
  }

  private today(): string {
    const formatter = new Intl.DateTimeFormat('en-CA', {
      timeZone: BitacoraListComponent.ECUADOR_TIMEZONE,
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
    return formatter.format(new Date());
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

  private mapVehiculoOption(vehiculo: VehiculoResponse, includeResumen: boolean): CatalogSearchOption {
    return {
      value: vehiculo.id,
      label: `${vehiculo.placa} | ${vehiculo.choferDefault}`,
      secondaryLabel: includeResumen ? `${vehiculo.documentoPersonal} | ${vehiculo.tonelajeCategoria} | ${vehiculo.m3} m3` : vehiculo.documentoPersonal,
      searchText: `${vehiculo.placa} ${vehiculo.choferDefault} ${vehiculo.documentoPersonal} ${vehiculo.tonelajeCategoria}`
    };
  }

  private mapClienteOption(cliente: ClienteResponse, includeDocumento: boolean): CatalogSearchOption {
    return {
      value: cliente.id,
      label: cliente.nombre,
      secondaryLabel: includeDocumento ? `${cliente.tipoDocumento} | ${cliente.documento}` : cliente.documento,
      searchText: `${cliente.nombre} ${cliente.documento} ${cliente.tipoDocumento} ${cliente.direccion ?? ''}`
    };
  }

  protected calculateRetencion(valor: number, aplicaRetencion = true): number {
    return aplicaRetencion ? this.roundCurrency((valor || 0) * 0.01) : 0;
  }

  protected calculateComision(valor: number): number {
    return this.roundCurrency((valor || 0) * 0.06);
  }

  protected calculatePagoTotal(viaje: ViajeBitacoraResponse): number {
    const valor = viaje.valor || 0;
    const anticipo = viaje.anticipo || 0;
    return this.roundCurrency(valor - this.calculateRetencion(valor, viaje.aplicaRetencion) - this.calculateComision(valor) - anticipo);
  }

  private roundCurrency(value: number): number {
    return Math.round((value + Number.EPSILON) * 100) / 100;
  }

  private syncClienteEquivalencias(clienteId: string): void {
    const cliente = this.clientesCatalogo.find((item) => item.id === clienteId);
    const equivalencias = cliente?.aplicaTablaEquivalencia ? cliente.equivalencias : [];
    this.destinoEquivalenciaFormOptions = equivalencias.map((equivalencia) => ({
      value: equivalencia.destino,
      label: equivalencia.destino,
      secondaryLabel: `Valor: $${equivalencia.valorDestino.toFixed(2)} | Costo chofer: $${equivalencia.costoChofer.toFixed(2)}`,
      searchText: equivalencia.destino
    }));

    if (equivalencias.length === 0) {
      this.destinoEquivalenciaFormOptions = [];
      return;
    }

    const destinoActual = this.viajeForm.get('destino')?.value ?? '';
    const equivalenciaActual = equivalencias.find((item) => item.destino === destinoActual);
    if (equivalenciaActual) {
      this.applyEquivalenciaValues(equivalenciaActual.valorDestino, equivalenciaActual.costoChofer);
      return;
    }

    this.viajeForm.patchValue({
      destino: '',
      valor: 0,
      costoChofer: 0
    });
  }

  private syncValoresDesdeDestino(destino: string): void {
    if (!this.usesEquivalenciasForSelectedCliente()) {
      return;
    }
    const equivalencia = this.selectedClienteEquivalencias().find((item) => item.destino === destino);
    if (!equivalencia) {
      this.applyEquivalenciaValues(0, 0);
      return;
    }
    this.applyEquivalenciaValues(equivalencia.valorDestino, equivalencia.costoChofer);
  }

  private applyEquivalenciaValues(valorDestino: number, costoChofer: number): void {
    this.viajeForm.patchValue({
      valor: valorDestino,
      costoChofer
    }, { emitEvent: false });
  }
}
