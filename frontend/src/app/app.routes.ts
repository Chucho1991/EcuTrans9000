import { Routes } from '@angular/router';

export const APP_ROUTES: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'auth/login', loadComponent: () => import('./modules/auth/pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'dashboard', loadComponent: () => import('./modules/dashboard/pages/home/dashboard-home.component').then(m => m.DashboardHomeComponent) },
  { path: 'users', loadComponent: () => import('./modules/users/pages/list/users-list.component').then(m => m.UsersListComponent) },
  { path: 'vehiculos', loadComponent: () => import('./modules/vehiculos/pages/vehiculos-placeholder.component').then(m => m.VehiculosPlaceholderComponent) },
  { path: 'clientes', loadComponent: () => import('./modules/clientes/pages/clientes-placeholder.component').then(m => m.ClientesPlaceholderComponent) },
  { path: 'bitacora', loadComponent: () => import('./modules/bitacora/pages/bitacora-placeholder.component').then(m => m.BitacoraPlaceholderComponent) },
  { path: 'placas', loadComponent: () => import('./modules/placas/pages/placas-placeholder.component').then(m => m.PlacasPlaceholderComponent) }
];
