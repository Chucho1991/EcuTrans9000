import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';

import { DashboardService } from '../../services/dashboard.service';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="space-y-6">
      <header>
        <h1 class="text-2xl font-semibold text-gray-900 dark:text-white">Dashboard</h1>
        <p class="text-sm text-gray-500 dark:text-gray-400">Vista inicial para administradores.</p>
      </header>
      <div class="grid gap-4 md:grid-cols-3">
        <article class="rounded-2xl border border-gray-200 bg-white p-5 shadow-theme-xs dark:border-gray-800 dark:bg-gray-900">
          <p class="text-sm text-gray-500 dark:text-gray-400">Usuarios activos</p>
          <p class="mt-2 text-3xl font-semibold text-gray-900 dark:text-white">{{ metrics?.usuariosActivos ?? '-' }}</p>
        </article>
        <article class="rounded-2xl border border-gray-200 bg-white p-5 shadow-theme-xs dark:border-gray-800 dark:bg-gray-900">
          <p class="text-sm text-gray-500 dark:text-gray-400">Alertas hoy</p>
          <p class="mt-2 text-3xl font-semibold text-error-600">{{ metrics?.alertasHoy ?? '-' }}</p>
        </article>
        <article class="rounded-2xl border border-gray-200 bg-white p-5 shadow-theme-xs dark:border-gray-800 dark:bg-gray-900">
          <p class="text-sm text-gray-500 dark:text-gray-400">Viajes registrados</p>
          <p class="mt-2 text-3xl font-semibold text-brand-600">{{ metrics?.viajesRegistrados ?? '-' }}</p>
        </article>
      </div>
    </section>
  `
})
export class DashboardHomeComponent {
  private readonly dashboardService = inject(DashboardService);
  protected metrics: { usuariosActivos: number; alertasHoy: number; viajesRegistrados: number } | null = null;

  constructor() {
    this.dashboardService.getMetrics().subscribe({
      next: (response) => (this.metrics = response),
      error: () => (this.metrics = null)
    });
  }
}
