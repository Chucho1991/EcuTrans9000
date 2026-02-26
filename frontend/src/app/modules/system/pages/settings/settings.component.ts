import { Component } from '@angular/core';

@Component({
  selector: 'app-settings',
  standalone: true,
  template: `
    <section class="panel-card p-6">
      <h1 class="page-title">Configuracion</h1>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
        Pantalla base de configuracion del sistema. Proximamente se agregaran parametros globales.
      </p>
    </section>
  `
})
export class SettingsComponent {}
