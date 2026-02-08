import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  template: `
    <section class="space-y-6">
      <header>
        <h1 class="text-2xl font-semibold text-gray-900">Dashboard</h1>
        <p class="text-sm text-gray-500">Vista inicial para administradores.</p>
      </header>
      <div class="grid gap-4 md:grid-cols-3">
        <article class="rounded-2xl border border-gray-200 bg-white p-5 shadow-theme-xs">
          <p class="text-sm text-gray-500">Vehiculos activos</p>
          <p class="mt-2 text-3xl font-semibold text-gray-900">128</p>
        </article>
        <article class="rounded-2xl border border-gray-200 bg-white p-5 shadow-theme-xs">
          <p class="text-sm text-gray-500">Alertas hoy</p>
          <p class="mt-2 text-3xl font-semibold text-error-600">7</p>
        </article>
        <article class="rounded-2xl border border-gray-200 bg-white p-5 shadow-theme-xs">
          <p class="text-sm text-gray-500">Placas consultadas</p>
          <p class="mt-2 text-3xl font-semibold text-brand-600">54</p>
        </article>
      </div>
    </section>
  `
})
export class DashboardHomeComponent {}
