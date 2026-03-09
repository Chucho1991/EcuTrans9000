import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { merge, Subject, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import {
  DashboardDistributionItem,
  DashboardRankingItem,
  DashboardResponse,
  DashboardService
} from '../../services/dashboard.service';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="space-y-6">
      <header class="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p class="text-xs font-semibold uppercase tracking-[0.24em] text-brand-500">Centro de control</p>
          <h1 class="page-title mt-1">Dashboard operativo EcuTran9000</h1>
          <p class="page-subtitle mt-2 max-w-3xl">
            Seguimiento de flota, facturacion, clientes y actividad reciente desde la bitacora central.
          </p>
        </div>
        <div class="flex flex-wrap items-center gap-3">
          <button
            class="btn-secondary-brand inline-flex items-center gap-2"
            type="button"
            (click)="refreshNow()"
            [disabled]="isRefreshing"
          >
            <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4">
              <path d="M3 12a9 9 0 1 0 2.6-6.4M3 4v5h5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
            {{ isRefreshing ? 'Actualizando...' : 'Actualizar ahora' }}
          </button>
          <div class="rounded-2xl border border-brand-100 bg-brand-50 px-4 py-3 text-sm text-brand-900 dark:border-brand-500/20 dark:bg-brand-500/10 dark:text-brand-100">
            <p class="font-medium">Ultima actualizacion</p>
            <p class="mt-1">{{ dashboard?.generatedAt ? (dashboard!.generatedAt | date:'dd/MM/yyyy HH:mm:ss') : 'Sin datos' }}</p>
            <p class="mt-1 text-xs opacity-80">Auto-refresh cada 30 segundos</p>
          </div>
        </div>
      </header>

      <div *ngIf="loadError" class="rounded-2xl border border-warning-200 bg-warning-50 px-4 py-3 text-sm text-warning-800 dark:border-warning-500/20 dark:bg-warning-500/10 dark:text-warning-200">
        No se pudo refrescar el dashboard. Se mantiene la ultima informacion disponible.
      </div>

      <div *ngIf="dashboard as data; else loadingState" class="space-y-6">
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
          <article class="metric-card overflow-hidden">
            <div class="flex items-center justify-between gap-2">
              <p class="text-sm text-gray-500 dark:text-gray-400">Usuarios activos</p>
              <span class="rounded-full bg-blue-light-50 px-2.5 py-1 text-xs font-semibold text-blue-light-700 dark:bg-blue-light-500/10 dark:text-blue-light-300">
                {{ data.overview.usuariosInactivos }} inactivos
              </span>
            </div>
            <p class="mt-3 text-3xl font-semibold text-gray-900 dark:text-white">{{ data.overview.usuariosActivos }}</p>
            <p class="mt-2 text-xs text-gray-500 dark:text-gray-400">Accesos habilitados para operar el sistema.</p>
          </article>

          <article class="metric-card overflow-hidden">
            <div class="flex items-center justify-between gap-2">
              <p class="text-sm text-gray-500 dark:text-gray-400">Flota activa</p>
              <span class="rounded-full bg-success-50 px-2.5 py-1 text-xs font-semibold text-success-700 dark:bg-success-500/10 dark:text-success-300">
                {{ data.overview.totalVehiculos }} total
              </span>
            </div>
            <p class="mt-3 text-3xl font-semibold text-gray-900 dark:text-white">{{ data.overview.vehiculosActivos }}</p>
            <p class="mt-2 text-xs text-gray-500 dark:text-gray-400">Unidades listas para registrar viajes.</p>
          </article>

          <article class="metric-card overflow-hidden">
            <div class="flex items-center justify-between gap-2">
              <p class="text-sm text-gray-500 dark:text-gray-400">Viajes del mes</p>
              <span class="rounded-full bg-orange-50 px-2.5 py-1 text-xs font-semibold text-orange-700 dark:bg-orange-500/10 dark:text-orange-300">
                {{ data.overview.viajesPendientesFacturar }} pendientes
              </span>
            </div>
            <p class="mt-3 text-3xl font-semibold text-gray-900 dark:text-white">{{ data.overview.viajesMesActual }}</p>
            <p class="mt-2 text-xs text-gray-500 dark:text-gray-400">Actividad acumulada del periodo vigente.</p>
          </article>

          <article class="metric-card overflow-hidden">
            <div class="flex items-center justify-between gap-2">
              <p class="text-sm text-gray-500 dark:text-gray-400">Facturacion mensual</p>
              <span class="rounded-full bg-brand-50 px-2.5 py-1 text-xs font-semibold text-brand-700 dark:bg-brand-500/10 dark:text-brand-300">
                Cobros {{ data.financial.cobrosMesActual | currency:'USD':'symbol':'1.0-0' }}
              </span>
            </div>
            <p class="mt-3 text-3xl font-semibold text-gray-900 dark:text-white">
              {{ data.financial.facturacionMesActual | currency:'USD':'symbol':'1.0-0' }}
            </p>
            <p class="mt-2 text-xs text-gray-500 dark:text-gray-400">Valor generado por viajes del mes actual.</p>
          </article>

          <article class="metric-card overflow-hidden">
            <div class="flex items-center justify-between gap-2">
              <p class="text-sm text-gray-500 dark:text-gray-400">Alertas activas</p>
              <span class="rounded-full bg-error-50 px-2.5 py-1 text-xs font-semibold text-error-700 dark:bg-error-500/10 dark:text-error-300">
                Licencias + pendientes
              </span>
            </div>
            <p class="mt-3 text-3xl font-semibold text-error-600 dark:text-error-400">{{ data.overview.alertasActivas }}</p>
            <p class="mt-2 text-xs text-gray-500 dark:text-gray-400">Eventos que requieren atencion operativa.</p>
          </article>
        </div>

        <div class="grid gap-6 xl:grid-cols-[1.4fr_1fr]">
          <article class="panel-card p-5 sm:p-6">
            <div class="flex flex-wrap items-start justify-between gap-4">
              <div>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Pulso operacional</h2>
                <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Estado de flota, clientes y atencion inmediata.</p>
              </div>
              <div class="rounded-2xl bg-gray-100 px-3 py-2 text-right dark:bg-gray-800">
                <p class="text-xs uppercase tracking-[0.2em] text-gray-500 dark:text-gray-400">Clientes activos</p>
                <p class="mt-1 text-xl font-semibold text-gray-900 dark:text-white">{{ data.overview.clientesActivos }}</p>
              </div>
            </div>

            <div class="mt-6 grid gap-5 lg:grid-cols-[1.15fr_0.85fr]">
              <div class="space-y-4">
                <div *ngFor="let item of data.vehiculosPorEstado" class="rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
                  <div class="flex items-center justify-between gap-2 text-sm">
                    <span class="font-medium text-gray-700 dark:text-gray-200">{{ item.label }}</span>
                    <span [class]="distributionBadgeClass(item.tone)">{{ item.total }}</span>
                  </div>
                  <div class="mt-3 h-2 rounded-full bg-gray-100 dark:bg-gray-800">
                    <div
                      class="h-2 rounded-full transition-all duration-500"
                      [class]="distributionBarClass(item.tone)"
                      [style.width.%]="toPercent(item.total, maxDistributionTotal)"
                    ></div>
                  </div>
                </div>
              </div>

              <div class="grid gap-4">
                <article class="rounded-2xl border border-error-200 bg-error-50 p-4 dark:border-error-500/20 dark:bg-error-500/10">
                  <p class="text-xs uppercase tracking-[0.2em] text-error-700 dark:text-error-300">Licencias vencidas</p>
                  <p class="mt-2 text-3xl font-semibold text-error-700 dark:text-error-200">{{ data.licenseAlerts.vencidas }}</p>
                  <p class="mt-2 text-sm text-error-700/80 dark:text-error-200/80">Vehiculos que ya no deberian salir a ruta.</p>
                </article>
                <article class="rounded-2xl border border-warning-200 bg-warning-50 p-4 dark:border-warning-500/20 dark:bg-warning-500/10">
                  <p class="text-xs uppercase tracking-[0.2em] text-warning-700 dark:text-warning-300">Vencen en 7 dias</p>
                  <p class="mt-2 text-3xl font-semibold text-warning-700 dark:text-warning-200">{{ data.licenseAlerts.porVencer7Dias }}</p>
                  <p class="mt-2 text-sm text-warning-700/80 dark:text-warning-200/80">Prioridad alta para renovacion documental.</p>
                </article>
                <article class="rounded-2xl border border-brand-200 bg-brand-50 p-4 dark:border-brand-500/20 dark:bg-brand-500/10">
                  <p class="text-xs uppercase tracking-[0.2em] text-brand-700 dark:text-brand-300">Vencen en 30 dias</p>
                  <p class="mt-2 text-3xl font-semibold text-brand-700 dark:text-brand-200">{{ data.licenseAlerts.porVencer30Dias }}</p>
                  <p class="mt-2 text-sm text-brand-700/80 dark:text-brand-200/80">Ventana de planificacion preventiva.</p>
                </article>
              </div>
            </div>
          </article>

          <article class="panel-card p-5 sm:p-6">
            <div class="flex items-start justify-between gap-4">
              <div>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Radar financiero</h2>
                <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Acumulado financiero vivo con referencia adicional del mes actual.</p>
              </div>
              <div class="rounded-2xl bg-gray-100 px-3 py-2 dark:bg-gray-800">
                <p class="text-xs uppercase tracking-[0.2em] text-gray-500 dark:text-gray-400">Facturacion del mes</p>
                <p class="mt-1 text-lg font-semibold text-gray-900 dark:text-white">
                  {{ data.financial.facturacionMesActual | currency:'USD':'symbol':'1.0-0' }}
                </p>
              </div>
            </div>

            <div class="mt-6 space-y-4">
              <div class="rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
                <div class="flex items-center justify-between gap-3">
                  <div>
                    <p class="text-sm font-medium text-gray-700 dark:text-gray-200">Facturacion acumulada</p>
                    <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Suma total de viajes operativos registrados.</p>
                  </div>
                  <p class="text-xl font-semibold text-gray-900 dark:text-white">
                    {{ data.financial.facturacionTotal | currency:'USD':'symbol':'1.0-0' }}
                  </p>
                </div>
              </div>

              <div class="rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
                <div class="flex items-center justify-between gap-3">
                  <div>
                    <p class="text-sm font-medium text-gray-700 dark:text-gray-200">Cobros registrados</p>
                    <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Viajes con fecha de pago cliente registrada.</p>
                  </div>
                  <p class="text-xl font-semibold text-gray-900 dark:text-white">
                    {{ data.financial.cobrosTotal | currency:'USD':'symbol':'1.0-0' }}
                  </p>
                </div>
              </div>

              <div class="grid gap-4 sm:grid-cols-2">
                <div class="rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
                  <div class="flex items-center justify-between gap-3">
                    <div>
                      <p class="text-sm font-medium text-gray-700 dark:text-gray-200">Pendiente por facturar</p>
                      <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Viajes ejecutados sin factura emitida.</p>
                    </div>
                    <p class="text-lg font-semibold text-gray-900 dark:text-white">
                      {{ data.financial.pendienteFacturar | currency:'USD':'symbol':'1.0-0' }}
                    </p>
                  </div>
                </div>
                <div class="rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
                  <div class="flex items-center justify-between gap-3">
                    <div>
                      <p class="text-sm font-medium text-gray-700 dark:text-gray-200">Pendiente por cobrar</p>
                      <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Facturas emitidas aun sin pago cliente.</p>
                    </div>
                    <p class="text-lg font-semibold text-gray-900 dark:text-white">
                      {{ data.financial.pendienteCobrar | currency:'USD':'symbol':'1.0-0' }}
                    </p>
                  </div>
                </div>
              </div>

              <div class="rounded-2xl bg-gray-50 p-4 dark:bg-gray-950">
                <div class="flex items-center justify-between gap-3">
                  <span class="text-sm text-gray-600 dark:text-gray-300">Cobertura global de cobro</span>
                  <span class="text-sm font-semibold text-gray-900 dark:text-white">{{ overallCollectionRate(data) | number:'1.0-0' }}%</span>
                </div>
                <div class="mt-3 h-3 rounded-full bg-gray-200 dark:bg-gray-800">
                  <div
                    class="h-3 rounded-full bg-gradient-to-r from-brand-500 via-blue-light-500 to-success-500"
                    [style.width.%]="overallCollectionRate(data)"
                  ></div>
                </div>
                <div class="mt-4 grid gap-3 sm:grid-cols-2">
                  <div class="rounded-xl bg-white px-3 py-2 dark:bg-gray-900">
                    <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400">Anticipos acumulados</p>
                    <p class="mt-1 text-sm font-semibold text-gray-900 dark:text-white">
                      {{ data.financial.anticiposTotal | currency:'USD':'symbol':'1.0-0' }}
                    </p>
                  </div>
                  <div class="rounded-xl bg-white px-3 py-2 dark:bg-gray-900">
                    <p class="text-[11px] uppercase tracking-[0.18em] text-gray-400">Cobros del mes</p>
                    <p class="mt-1 text-sm font-semibold text-gray-900 dark:text-white">
                      {{ data.financial.cobrosMesActual | currency:'USD':'symbol':'1.0-0' }}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </article>
        </div>

        <div class="grid gap-6 xl:grid-cols-[1.5fr_1fr_1fr]">
          <article class="panel-card p-5 sm:p-6">
            <div class="flex flex-wrap items-center justify-between gap-4">
              <div>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Histograma de viajes</h2>
                <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Ultimos 6 meses con volumen y valor movilizado.</p>
              </div>
              <div class="rounded-2xl bg-gray-100 px-3 py-2 text-sm dark:bg-gray-800">
                <p class="text-xs uppercase tracking-[0.2em] text-gray-500 dark:text-gray-400">Pico mensual</p>
                <p class="mt-1 font-semibold text-gray-900 dark:text-white">{{ maxMonthlyTrips }} viajes</p>
              </div>
            </div>

            <div class="mt-6 grid h-[260px] grid-cols-6 items-end gap-3">
              <div *ngFor="let item of data.viajesPorMes" class="flex h-full flex-col justify-end">
                <div class="flex h-full flex-col justify-end gap-2">
                  <div class="rounded-t-2xl bg-gradient-to-t from-brand-600 via-brand-500 to-blue-light-400" [style.height.%]="toPercent(item.viajes, maxMonthlyTrips)"></div>
                </div>
                <div class="mt-3 text-center">
                  <p class="text-sm font-medium text-gray-700 dark:text-gray-200">{{ item.label }}</p>
                  <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">{{ item.viajes }} viajes</p>
                  <p class="text-[11px] text-gray-400 dark:text-gray-500">{{ item.valor | currency:'USD':'symbol':'1.0-0' }}</p>
                </div>
              </div>
            </div>
          </article>

          <article class="panel-card p-5 sm:p-6">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Top destinos</h2>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Rutas con mayor recurrencia.</p>
            <div class="mt-5 space-y-4">
              <div *ngFor="let item of data.topDestinos" class="space-y-2">
                <div class="flex items-center justify-between gap-3 text-sm">
                  <div class="min-w-0">
                    <p class="truncate font-medium text-gray-800 dark:text-white">{{ item.label }}</p>
                    <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.total }} viajes</p>
                  </div>
                  <span class="text-sm font-semibold text-gray-700 dark:text-gray-200">
                    {{ item.amount | currency:'USD':'symbol':'1.0-0' }}
                  </span>
                </div>
                <div class="h-2 rounded-full bg-gray-100 dark:bg-gray-800">
                  <div class="h-2 rounded-full bg-orange-500" [style.width.%]="toPercent(item.total, maxRankingTotal(data.topDestinos))"></div>
                </div>
              </div>
              <p *ngIf="data.topDestinos.length === 0" class="text-sm text-gray-500 dark:text-gray-400">Sin viajes registrados.</p>
            </div>
          </article>

          <article class="panel-card p-5 sm:p-6">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Top clientes</h2>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Mayor valor movilizado acumulado.</p>
            <div class="mt-5 space-y-4">
              <div *ngFor="let item of data.topClientes" class="rounded-2xl border border-gray-200 p-4 dark:border-gray-800">
                <div class="flex items-start justify-between gap-3">
                  <div class="min-w-0">
                    <p class="truncate font-medium text-gray-800 dark:text-white">{{ item.label }}</p>
                    <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">{{ item.secondaryLabel || 'Sin documento' }}</p>
                  </div>
                  <span class="rounded-full bg-success-50 px-2.5 py-1 text-xs font-semibold text-success-700 dark:bg-success-500/10 dark:text-success-300">
                    {{ item.total }} viajes
                  </span>
                </div>
                <p class="mt-3 text-lg font-semibold text-gray-900 dark:text-white">
                  {{ item.amount | currency:'USD':'symbol':'1.0-0' }}
                </p>
              </div>
              <p *ngIf="data.topClientes.length === 0" class="text-sm text-gray-500 dark:text-gray-400">Aun no hay clientes con actividad.</p>
            </div>
          </article>
        </div>

        <div class="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
          <article class="panel-card overflow-hidden">
            <div class="flex flex-wrap items-center justify-between gap-3 px-5 pt-5 sm:px-6 sm:pt-6">
              <div>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Viajes recientes</h2>
                <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Ultimos movimientos capturados en bitacora.</p>
              </div>
            </div>
            <div class="mt-5 overflow-x-auto">
              <table class="min-w-[720px] w-full text-left text-sm lg:min-w-full">
                <thead class="border-y border-gray-200 bg-gray-50 text-gray-600 dark:border-gray-800 dark:bg-gray-950 dark:text-gray-300">
                  <tr>
                    <th class="px-5 py-3 sm:px-6">Viaje</th>
                    <th class="px-5 py-3 sm:px-6">Fecha</th>
                    <th class="px-5 py-3 sm:px-6">Placa</th>
                    <th class="px-5 py-3 sm:px-6">Cliente</th>
                    <th class="px-5 py-3 sm:px-6">Destino</th>
                    <th class="px-5 py-3 sm:px-6">Valor</th>
                    <th class="px-5 py-3 sm:px-6">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let viaje of data.viajesRecientes" class="border-b border-gray-100 dark:border-gray-800">
                    <td class="px-5 py-4 font-medium text-gray-800 dark:text-white sm:px-6">#{{ viaje.numeroViaje }}</td>
                    <td class="px-5 py-4 text-gray-600 dark:text-gray-300 sm:px-6">{{ viaje.fechaViaje | date:'dd/MM/yyyy' }}</td>
                    <td class="px-5 py-4 text-gray-600 dark:text-gray-300 sm:px-6">{{ viaje.placa }}</td>
                    <td class="px-5 py-4 text-gray-600 dark:text-gray-300 sm:px-6">{{ viaje.cliente }}</td>
                    <td class="px-5 py-4 text-gray-600 dark:text-gray-300 sm:px-6">{{ viaje.destino }}</td>
                    <td class="px-5 py-4 text-gray-600 dark:text-gray-300 sm:px-6">{{ viaje.valor | currency:'USD':'symbol':'1.0-0' }}</td>
                    <td class="px-5 py-4 sm:px-6">
                      <div class="flex flex-wrap gap-2">
                        <span
                          class="inline-flex rounded-full px-2.5 py-1 text-xs font-semibold"
                          [class]="viaje.facturadoCliente
                            ? 'bg-success-50 text-success-700 dark:bg-success-500/10 dark:text-success-300'
                            : 'bg-warning-50 text-warning-700 dark:bg-warning-500/10 dark:text-warning-300'"
                        >
                          {{ viaje.facturadoCliente ? 'Facturado' : 'Sin factura' }}
                        </span>
                        <span
                          class="inline-flex rounded-full px-2.5 py-1 text-xs font-semibold"
                          [class]="viaje.pagadoTransportista
                            ? 'bg-brand-50 text-brand-700 dark:bg-brand-500/10 dark:text-brand-300'
                            : 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300'"
                        >
                          {{ viaje.pagadoTransportista ? 'Pagado' : 'Pendiente' }}
                        </span>
                      </div>
                    </td>
                  </tr>
                  <tr *ngIf="data.viajesRecientes.length === 0">
                    <td colspan="7" class="px-5 py-6 text-center text-gray-500 dark:text-gray-400 sm:px-6">Sin viajes recientes para mostrar.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article class="panel-card p-5 sm:p-6">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Vehiculos con mayor uso</h2>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Ranking por numero de viajes registrados.</p>
            <div class="mt-5 space-y-4">
              <div *ngFor="let item of data.topVehiculos" class="rounded-2xl bg-gray-50 p-4 dark:bg-gray-950">
                <div class="flex items-start justify-between gap-3">
                  <div class="min-w-0">
                    <p class="truncate font-medium text-gray-800 dark:text-white">{{ item.label }}</p>
                    <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">{{ item.secondaryLabel || 'Chofer no disponible' }}</p>
                  </div>
                  <span class="text-sm font-semibold text-gray-900 dark:text-white">{{ item.total }} viajes</span>
                </div>
                <div class="mt-3 h-2 rounded-full bg-gray-200 dark:bg-gray-800">
                  <div class="h-2 rounded-full bg-blue-light-500" [style.width.%]="toPercent(item.total, maxRankingTotal(data.topVehiculos))"></div>
                </div>
                <p class="mt-3 text-sm text-gray-600 dark:text-gray-300">{{ item.amount | currency:'USD':'symbol':'1.0-0' }}</p>
              </div>
              <p *ngIf="data.topVehiculos.length === 0" class="text-sm text-gray-500 dark:text-gray-400">No hay datos suficientes todavia.</p>
            </div>
          </article>
        </div>
      </div>

      <ng-template #loadingState>
        <article class="panel-card p-8 text-center">
          <p class="text-sm text-gray-500 dark:text-gray-400">
            {{ isLoading ? 'Cargando paneles del dashboard...' : 'No hay datos disponibles.' }}
          </p>
        </article>
      </ng-template>
    </section>
  `
})
export class DashboardHomeComponent {
  private readonly dashboardService = inject(DashboardService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly manualRefresh$ = new Subject<void>();
  private readonly refreshIntervalMs = 30000;

  protected dashboard: DashboardResponse | null = null;
  protected isLoading = true;
  protected isRefreshing = false;
  protected loadError = false;

  constructor() {
    merge(timer(0, this.refreshIntervalMs), this.manualRefresh$)
      .pipe(
        switchMap(() => {
          this.isLoading = this.dashboard === null;
          this.isRefreshing = this.dashboard !== null;
          this.loadError = false;
          return this.dashboardService.getMetrics();
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (response) => {
          this.dashboard = response;
          this.isLoading = false;
          this.isRefreshing = false;
          this.loadError = false;
        },
        error: () => {
          this.isLoading = false;
          this.isRefreshing = false;
          this.loadError = true;
        }
      });
  }

  protected refreshNow(): void {
    this.manualRefresh$.next();
  }

  protected get maxDistributionTotal(): number {
    return this.dashboard?.vehiculosPorEstado.reduce((max, item) => Math.max(max, item.total), 0) ?? 0;
  }

  protected get maxMonthlyTrips(): number {
    return this.dashboard?.viajesPorMes.reduce((max, item) => Math.max(max, item.viajes), 0) ?? 0;
  }

  protected toPercent(value: number, total: number): number {
    if (!total || total <= 0 || value <= 0) {
      return 0;
    }
    return Math.max(6, Math.round((value / total) * 100));
  }

  protected maxRankingTotal(items: DashboardRankingItem[]): number {
    return items.reduce((max, item) => Math.max(max, item.total), 0);
  }

  protected overallCollectionRate(data: DashboardResponse): number {
    const billed = data.financial.facturacionTotal ?? 0;
    if (billed <= 0) {
      return 0;
    }
    return Math.min(100, Math.round(((data.financial.cobrosTotal ?? 0) / billed) * 100));
  }

  protected distributionBadgeClass(tone: DashboardDistributionItem['tone']): string {
    return {
      success: 'rounded-full bg-success-50 px-2.5 py-1 text-xs font-semibold text-success-700 dark:bg-success-500/10 dark:text-success-300',
      warning: 'rounded-full bg-warning-50 px-2.5 py-1 text-xs font-semibold text-warning-700 dark:bg-warning-500/10 dark:text-warning-300',
      danger: 'rounded-full bg-error-50 px-2.5 py-1 text-xs font-semibold text-error-700 dark:bg-error-500/10 dark:text-error-300',
      brand: 'rounded-full bg-brand-50 px-2.5 py-1 text-xs font-semibold text-brand-700 dark:bg-brand-500/10 dark:text-brand-300'
    }[tone];
  }

  protected distributionBarClass(tone: DashboardDistributionItem['tone']): string {
    return {
      success: 'bg-success-500',
      warning: 'bg-warning-500',
      danger: 'bg-error-500',
      brand: 'bg-brand-500'
    }[tone];
  }
}
