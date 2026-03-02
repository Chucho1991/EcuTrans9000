import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { ModuleAccessService } from '../core/services/module-access.service';
import { AuthService } from '../modules/auth/services/auth.service';
import { ThemeService } from '../core/services/theme.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="min-h-screen overflow-x-hidden bg-gray-50 text-gray-800 dark:bg-gray-950 dark:text-gray-100">
      <div class="flex min-h-screen w-full overflow-x-hidden">
        <aside
          class="hidden w-64 border-r border-gray-200 bg-white lg:flex lg:flex-col xl:w-72 dark:border-gray-800 dark:bg-gray-900"
          [class.lg:hidden]="desktopSidebarHidden"
          [class.lg:flex]="!desktopSidebarHidden"
        >
          <div class="border-b border-gray-200 px-6 py-5 dark:border-gray-800">
            <div class="flex items-center gap-3">
              <img
                src="brand/ecutrans-logo.png"
                alt="Logo de EcuTrans"
                class="h-12 w-12 rounded-xl bg-white object-contain p-1 shadow-sm"
              />
              <div class="min-w-0">
                <h1 class="text-xl font-semibold text-gray-900 dark:text-white">EcuTrans9000</h1>
                <p class="text-sm text-gray-500 dark:text-gray-400">Sistema de trafico</p>
              </div>
            </div>
          </div>
          <nav class="space-y-1 overflow-y-auto p-4">
            <a *ngIf="isSuperadmin()" routerLink="/dashboard" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M4 13h6V4H4v9Zm10 7h6V4h-6v16ZM4 20h6v-5H4v5Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>
              <span>Dashboard</span>
            </a>
            <a *ngIf="isSuperadmin()" routerLink="/users" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm8 2a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm5 8v-2a4 4 0 0 0-3-3.87" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
              <span>Usuarios</span>
            </a>
            <a routerLink="/profile" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M20 21a8 8 0 1 0-16 0M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
              <span>Mi perfil</span>
            </a>
            <a *ngIf="hasModuleAccess('VEHICULOS')" routerLink="/vehiculos" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M3 13h18l-1-4H4l-1 4Zm2 0v5m14-5v5M7 18h.01M17 18h.01" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <span>Vehiculos</span>
            </a>
            <a *ngIf="hasModuleAccess('CLIENTES')" routerLink="/clientes" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M3 20h18M5 20V8l7-4 7 4v12M9 20v-6h6v6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <span>Clientes</span>
            </a>
            <a *ngIf="hasModuleAccess('BITACORA')" routerLink="/bitacora" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M6 3h12a2 2 0 0 1 2 2v14l-4-2-4 2-4-2-4 2V5a2 2 0 0 1 2-2Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>
              <span>Bitacora</span>
            </a>
            <a *ngIf="hasModuleAccess('PLACAS')" routerLink="/placas" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M4 7h16v10H4V7Zm4 3h8m-8 4h5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <span>Consulta por placas</span>
            </a>
          </nav>
        </aside>

        <div *ngIf="mobileSidebarOpen" class="fixed inset-0 z-40 bg-gray-950/50 lg:hidden" (click)="closeMobileSidebar()"></div>
        <aside
          class="fixed inset-y-0 left-0 z-50 w-[88vw] max-w-72 -translate-x-full border-r border-gray-200 bg-white transition-transform duration-200 lg:hidden dark:border-gray-800 dark:bg-gray-900"
          [class.translate-x-0]="mobileSidebarOpen"
          [class.-translate-x-full]="!mobileSidebarOpen"
          aria-label="Menu lateral movil"
        >
          <div class="flex items-start justify-between gap-3 border-b border-gray-200 px-4 py-4 dark:border-gray-800 sm:px-6 sm:py-5">
            <div class="flex min-w-0 flex-1 items-center gap-3">
              <img
                src="brand/ecutrans-logo.png"
                alt="Logo de EcuTrans"
                class="h-11 w-11 rounded-xl bg-white object-contain p-1 shadow-sm"
              />
              <div class="min-w-0">
                <h1 class="break-words text-lg font-semibold leading-tight text-gray-900 dark:text-white sm:text-xl">EcuTrans9000</h1>
                <p class="text-sm text-gray-500 dark:text-gray-400">Sistema de trafico</p>
              </div>
            </div>
            <button type="button" class="icon-action-btn" aria-label="Cerrar menu lateral" (click)="closeMobileSidebar()">
              <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M6 6l12 12M18 6 6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
              <span class="icon-action-tooltip">Cerrar menu</span>
            </button>
          </div>
          <nav class="space-y-1 p-4">
            <a *ngIf="isSuperadmin()" routerLink="/dashboard" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive" (click)="closeMobileSidebar()">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M4 13h6V4H4v9Zm10 7h6V4h-6v16ZM4 20h6v-5H4v5Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>
              <span>Dashboard</span>
            </a>
            <a *ngIf="isSuperadmin()" routerLink="/users" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive" (click)="closeMobileSidebar()">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm8 2a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm5 8v-2a4 4 0 0 0-3-3.87" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
              <span>Usuarios</span>
            </a>
            <a routerLink="/profile" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive" (click)="closeMobileSidebar()">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M20 21a8 8 0 1 0-16 0M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
              <span>Mi perfil</span>
            </a>
            <a *ngIf="hasModuleAccess('VEHICULOS')" routerLink="/vehiculos" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive" (click)="closeMobileSidebar()">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M3 13h18l-1-4H4l-1 4Zm2 0v5m14-5v5M7 18h.01M17 18h.01" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <span>Vehiculos</span>
            </a>
            <a *ngIf="hasModuleAccess('CLIENTES')" routerLink="/clientes" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive" (click)="closeMobileSidebar()">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M3 20h18M5 20V8l7-4 7 4v12M9 20v-6h6v6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <span>Clientes</span>
            </a>
            <a *ngIf="hasModuleAccess('BITACORA')" routerLink="/bitacora" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive" (click)="closeMobileSidebar()">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M6 3h12a2 2 0 0 1 2 2v14l-4-2-4 2-4-2-4 2V5a2 2 0 0 1 2-2Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/></svg>
              <span>Bitacora</span>
            </a>
            <a *ngIf="hasModuleAccess('PLACAS')" routerLink="/placas" routerLinkActive="menu-item-active" class="menu-item menu-item-inactive" (click)="closeMobileSidebar()">
              <svg viewBox="0 0 24 24" fill="none" class="h-5 w-5"><path d="M4 7h16v10H4V7Zm4 3h8m-8 4h5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <span>Consulta por placas</span>
            </a>
          </nav>
        </aside>

        <div class="flex min-h-screen min-w-0 flex-1 flex-col overflow-x-hidden">
          <header class="flex w-full min-w-0 items-center gap-2 border-b border-gray-200 bg-white px-3 py-3 sm:px-4 sm:py-4 lg:px-6 dark:border-gray-800 dark:bg-gray-900">
            <div class="flex min-w-0 items-center gap-2">
              <button type="button" class="icon-action-btn" aria-label="Ocultar menu lateral" (click)="toggleSidebar()">
                <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M4 6h16M4 12h16M4 18h10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                <span class="icon-action-tooltip">Menu lateral</span>
              </button>
              <p class="hidden truncate text-sm text-gray-500 dark:text-gray-400 sm:block">Panel administrativo</p>
            </div>
            <div class="ml-auto flex min-w-0 items-center gap-1 overflow-hidden sm:gap-2">
              <span class="hidden rounded-full border border-brand-300/40 bg-brand-500/15 px-3 py-1 text-xs font-semibold tracking-wide text-brand-600 dark:border-brand-400/30 dark:bg-brand-400/20 dark:text-brand-300 sm:inline-flex">
                {{ getUsernameLabel() }}
              </span>
              <button type="button" class="icon-action-btn" [attr.aria-label]="themeService.isDark() ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'" (click)="themeService.toggle()">
                <svg *ngIf="themeService.isDark()" viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M12 3v2m0 14v2m9-9h-2M5 12H3m14.36 6.36-1.41-1.41M7.05 7.05 5.64 5.64m12.72 0-1.41 1.41M7.05 16.95l-1.41 1.41M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                <svg *ngIf="!themeService.isDark()" viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M21 12.79A9 9 0 1 1 11.21 3a7 7 0 1 0 9.79 9.79Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
                <span class="icon-action-tooltip">Tema</span>
              </button>

              <a routerLink="/profile" class="icon-action-btn" aria-label="Ver perfil">
                <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M20 21a8 8 0 1 0-16 0M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                <span class="icon-action-tooltip">Perfil</span>
              </a>

              <a *ngIf="isSuperadmin()" routerLink="/settings" class="icon-action-btn hidden sm:inline-flex" aria-label="Ver configuracion">
                <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM19.4 15a1.7 1.7 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06A1.7 1.7 0 0 0 15 19.4a1.7 1.7 0 0 0-1 .6 1.7 1.7 0 0 0-.4 1.07V21a2 2 0 0 1-4 0v-.09a1.7 1.7 0 0 0-.4-1.07 1.7 1.7 0 0 0-1-.6 1.7 1.7 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.7 1.7 0 0 0 4.6 15a1.7 1.7 0 0 0-.6-1 1.7 1.7 0 0 0-1.07-.4H3a2 2 0 0 1 0-4h.09A1.7 1.7 0 0 0 4.16 9a1.7 1.7 0 0 0 .44-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.7 1.7 0 0 0 9 4.6a1.7 1.7 0 0 0 1-.6 1.7 1.7 0 0 0 .4-1.07V3a2 2 0 0 1 4 0v.09a1.7 1.7 0 0 0 .4 1.07 1.7 1.7 0 0 0 1 .6 1.7 1.7 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.7 1.7 0 0 0 19.4 9c.28.3.45.68.6 1 .16.34.4.68 1.07.6H21a2 2 0 0 1 0 4h-.09a1.7 1.7 0 0 0-1.51.4Z" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
                <span class="icon-action-tooltip">Configuracion</span>
              </a>

              <a routerLink="/version" class="icon-action-btn hidden sm:inline-flex" aria-label="Ver version">
                <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M12 7v6m0 4h.01M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                <span class="icon-action-tooltip">Version</span>
              </a>

              <button type="button" class="icon-action-btn text-error-600 hover:text-error-700" aria-label="Cerrar sesion" (click)="logout()">
                <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
                <span class="icon-action-tooltip">Cerrar sesion</span>
              </button>
            </div>
          </header>
          <main class="flex-1 min-w-0 overflow-x-hidden bg-gray-50 px-3 py-4 dark:bg-gray-950 sm:px-4 sm:py-6 lg:px-6">
            <router-outlet />
          </main>
        </div>
      </div>
    </div>
  `
})
export class AppLayoutComponent {
  private readonly authService = inject(AuthService);
  private readonly moduleAccessService = inject(ModuleAccessService);
  private readonly router = inject(Router);
  protected readonly themeService = inject(ThemeService);
  protected desktopSidebarHidden = false;
  protected mobileSidebarOpen = false;

  constructor() {
    this.moduleAccessService.fetchMyAccess().subscribe();
  }

  protected isSuperadmin(): boolean {
    return this.authService.getRole() === 'SUPERADMINISTRADOR';
  }

  protected getUsernameLabel(): string {
    return this.authService.getNombres() ?? this.authService.getUsername() ?? 'USUARIO';
  }

  protected hasModuleAccess(moduleKey: string): boolean {
    return this.moduleAccessService.hasAccess(moduleKey);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/auth/login');
  }

  protected toggleSidebar(): void {
    if (window.innerWidth < 1024) {
      this.mobileSidebarOpen = !this.mobileSidebarOpen;
      return;
    }
    this.desktopSidebarHidden = !this.desktopSidebarHidden;
  }

  protected closeMobileSidebar(): void {
    this.mobileSidebarOpen = false;
  }
}
