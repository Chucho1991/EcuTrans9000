import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../modules/auth/services/auth.service';
import { ThemeService } from '../core/services/theme.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="min-h-screen bg-gray-50 text-gray-800 dark:bg-gray-950 dark:text-gray-100">
      <div class="flex min-h-screen">
        <aside class="hidden w-72 border-r border-gray-200 bg-white lg:flex lg:flex-col dark:border-gray-800 dark:bg-gray-900" [class.lg:hidden]="sidebarHidden" [class.lg:flex]="!sidebarHidden">
          <div class="border-b border-gray-200 px-6 py-5 dark:border-gray-800">
            <h1 class="text-xl font-semibold text-gray-900 dark:text-white">EcuTrans9000</h1>
            <p class="text-sm text-gray-500 dark:text-gray-400">Sistema de trafico</p>
          </div>
          <nav class="space-y-1 p-4">
            <a *ngIf="isSuperadmin()" routerLink="/dashboard" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Dashboard</a>
            <a *ngIf="isSuperadmin()" routerLink="/users" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Usuarios</a>
            <a routerLink="/profile" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Mi perfil</a>
            <a routerLink="/vehiculos" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Vehiculos</a>
            <a routerLink="/clientes" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Clientes</a>
            <a routerLink="/bitacora" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Bitacora</a>
            <a routerLink="/placas" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">Consulta por placas</a>
          </nav>
        </aside>

        <div class="flex min-h-screen flex-1 flex-col">
          <header class="flex flex-wrap items-center justify-between gap-2 border-b border-gray-200 bg-white px-4 py-4 sm:px-6 dark:border-gray-800 dark:bg-gray-900">
            <p class="text-sm text-gray-500 dark:text-gray-400">Panel administrativo</p>
            <div class="flex items-center gap-2">
              <button type="button" class="icon-action-btn" aria-label="Ocultar menu lateral" (click)="toggleSidebar()">
                <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M4 6h16M4 12h16M4 18h10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                <span class="icon-action-tooltip">Menu lateral</span>
              </button>

              <button type="button" class="icon-action-btn" [attr.aria-label]="themeService.isDark() ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'" (click)="themeService.toggle()">
                <svg *ngIf="themeService.isDark()" viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M12 3v2m0 14v2m9-9h-2M5 12H3m14.36 6.36-1.41-1.41M7.05 7.05 5.64 5.64m12.72 0-1.41 1.41M7.05 16.95l-1.41 1.41M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                <svg *ngIf="!themeService.isDark()" viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M21 12.79A9 9 0 1 1 11.21 3a7 7 0 1 0 9.79 9.79Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
                <span class="icon-action-tooltip">Tema</span>
              </button>

              <a routerLink="/profile" class="icon-action-btn" aria-label="Ver perfil">
                <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M20 21a8 8 0 1 0-16 0M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                <span class="icon-action-tooltip">Perfil</span>
              </a>

              <a routerLink="/settings" class="icon-action-btn" aria-label="Ver configuracion">
                <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM19.4 15a1.7 1.7 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06A1.7 1.7 0 0 0 15 19.4a1.7 1.7 0 0 0-1 .6 1.7 1.7 0 0 0-.4 1.07V21a2 2 0 0 1-4 0v-.09a1.7 1.7 0 0 0-.4-1.07 1.7 1.7 0 0 0-1-.6 1.7 1.7 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.7 1.7 0 0 0 4.6 15a1.7 1.7 0 0 0-.6-1 1.7 1.7 0 0 0-1.07-.4H3a2 2 0 0 1 0-4h.09A1.7 1.7 0 0 0 4.16 9a1.7 1.7 0 0 0 .44-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.7 1.7 0 0 0 9 4.6a1.7 1.7 0 0 0 1-.6 1.7 1.7 0 0 0 .4-1.07V3a2 2 0 0 1 4 0v.09a1.7 1.7 0 0 0 .4 1.07 1.7 1.7 0 0 0 1 .6 1.7 1.7 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.7 1.7 0 0 0 19.4 9c.28.3.45.68.6 1 .16.34.4.68 1.07.6H21a2 2 0 0 1 0 4h-.09a1.7 1.7 0 0 0-1.51.4Z" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
                <span class="icon-action-tooltip">Configuracion</span>
              </a>

              <a routerLink="/version" class="icon-action-btn" aria-label="Ver version">
                <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M12 7v6m0 4h.01M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                <span class="icon-action-tooltip">Version</span>
              </a>

              <button type="button" class="icon-action-btn text-error-600 hover:text-error-700" aria-label="Cerrar sesion" (click)="logout()">
                <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
                <span class="icon-action-tooltip">Cerrar sesion</span>
              </button>
            </div>
          </header>
          <main class="flex-1 p-4 sm:p-6">
            <router-outlet />
          </main>
        </div>
      </div>
    </div>
  `
})
export class AppLayoutComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly themeService = inject(ThemeService);
  protected sidebarHidden = false;

  protected isSuperadmin(): boolean {
    return this.authService.getRole() === 'SUPERADMINISTRADOR';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/auth/login');
  }

  protected toggleSidebar(): void {
    this.sidebarHidden = !this.sidebarHidden;
  }
}
