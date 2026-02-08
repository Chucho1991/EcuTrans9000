import { Component } from '@angular/core';

@Component({
  selector: 'app-users-list',
  standalone: true,
  template: `
    <section class="rounded-2xl border border-gray-200 bg-white p-6 shadow-theme-sm">
      <h1 class="text-2xl font-semibold text-gray-900">Usuarios</h1>
      <p class="mt-1 text-sm text-gray-500">Listado de usuarios (estructura inicial).</p>
    </section>
  `
})
export class UsersListComponent {}
