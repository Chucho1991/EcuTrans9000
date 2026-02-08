import { Component } from '@angular/core';

@Component({
  selector: 'app-settings',
  standalone: true,
  template: `
    <section class="rounded-2xl border border-gray-200 bg-white p-6 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900">
      <h1 class="text-2xl font-semibold text-gray-900 dark:text-white">Configuracion</h1>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
        Pantalla base de configuracion del sistema. Proximamente se agregaran parametros globales.
      </p>
    </section>
  `
})
export class SettingsComponent {}
