import { Component } from '@angular/core';

@Component({
  selector: 'app-version',
  standalone: true,
  template: `
    <section class="panel-card p-6">
      <h1 class="page-title">Version de la aplicacion</h1>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">EcuTrans9000 Frontend v0.0.1</p>
      <p class="mt-1 text-xs text-gray-500 dark:text-gray-500">Build base TailAdmin + Angular</p>
    </section>
  `
})
export class VersionComponent {}
