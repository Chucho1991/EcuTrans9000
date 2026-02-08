import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { AppLayoutComponent } from './layouts/app-layout.component';

export const APP_ROUTES: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'auth/login', loadComponent: () => import('./modules/auth/pages/login/login.component').then(m => m.LoginComponent) },
  {
    path: '',
    component: AppLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', canActivate: [roleGuard], data: { roles: ['SUPERADMINISTRADOR'] }, loadComponent: () => import('./modules/dashboard/pages/home/dashboard-home.component').then(m => m.DashboardHomeComponent) },
      { path: 'users', canActivate: [roleGuard], data: { roles: ['SUPERADMINISTRADOR'] }, loadComponent: () => import('./modules/users/pages/list/users-list.component').then(m => m.UsersListComponent) },
      { path: 'profile', loadComponent: () => import('./modules/users/pages/profile/users-profile.component').then(m => m.UsersProfileComponent) },
      { path: 'settings', loadComponent: () => import('./modules/system/pages/settings/settings.component').then(m => m.SettingsComponent) },
      { path: 'version', loadComponent: () => import('./modules/system/pages/version/version.component').then(m => m.VersionComponent) },
      { path: 'vehiculos', loadComponent: () => import('./modules/vehiculos/pages/vehiculos-placeholder.component').then(m => m.VehiculosPlaceholderComponent) },
      { path: 'clientes', loadComponent: () => import('./modules/clientes/pages/clientes-placeholder.component').then(m => m.ClientesPlaceholderComponent) },
      { path: 'bitacora', loadComponent: () => import('./modules/bitacora/pages/bitacora-placeholder.component').then(m => m.BitacoraPlaceholderComponent) },
      { path: 'placas', loadComponent: () => import('./modules/placas/pages/placas-placeholder.component').then(m => m.PlacasPlaceholderComponent) }
    ]
  },
  { path: '**', redirectTo: 'auth/login' }
];
