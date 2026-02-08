import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="min-h-screen bg-gray-50 text-gray-800">
      <div class="flex min-h-screen">
        <aside class="hidden w-72 border-r border-gray-200 bg-white lg:flex lg:flex-col">
          <div class="border-b border-gray-200 px-6 py-5">
            <h1 class="text-xl font-semibold text-gray-900">EcuTrans9000</h1>
            <p class="text-sm text-gray-500">Sistema de trafico</p>
          </div>
          <nav class="space-y-1 p-4">
            <a routerLink="/dashboard" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Dashboard</a>
            <a routerLink="/users" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Usuarios</a>
            <a routerLink="/vehiculos" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Vehiculos</a>
            <a routerLink="/clientes" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Clientes</a>
            <a routerLink="/bitacora" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Bitacora</a>
            <a routerLink="/placas" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Consulta por placas</a>
          </nav>
        </aside>

        <div class="flex min-h-screen flex-1 flex-col">
          <header class="border-b border-gray-200 bg-white px-4 py-4 sm:px-6">
            <p class="text-sm text-gray-500">Panel administrativo</p>
          </header>
          <main class="flex-1 p-4 sm:p-6">
            <router-outlet />
          </main>
        </div>
      </div>
    </div>
  `
})
export class AppComponent {}
