import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { moduleAccessGuard } from './core/guards/module-access.guard';
import { AppLayoutComponent } from './layouts/app-layout.component';
import { roleGuard } from './core/guards/role.guard';

export const APP_ROUTES: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'auth/login', loadComponent: () => import('./modules/auth/pages/login/login.component').then(m => m.LoginComponent) },
  {
    path: '',
    component: AppLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', canActivate: [moduleAccessGuard], data: { moduleKey: 'DASHBOARD' }, loadComponent: () => import('./modules/dashboard/pages/home/dashboard-home.component').then(m => m.DashboardHomeComponent) },
      { path: 'users', canActivate: [roleGuard], data: { roles: ['SUPERADMINISTRADOR'] }, loadComponent: () => import('./modules/users/pages/list/users-list.component').then(m => m.UsersListComponent) },
      { path: 'profile', loadComponent: () => import('./modules/users/pages/profile/users-profile.component').then(m => m.UsersProfileComponent) },
      { path: 'settings', canActivate: [roleGuard], data: { roles: ['SUPERADMINISTRADOR'] }, loadComponent: () => import('./modules/system/pages/settings/settings.component').then(m => m.SettingsComponent) },
      { path: 'version', loadComponent: () => import('./modules/system/pages/version/version.component').then(m => m.VersionComponent) },
      { path: 'vehiculos', canActivate: [moduleAccessGuard], data: { moduleKey: 'VEHICULOS' }, loadComponent: () => import('./modules/vehiculos/pages/list/vehiculos-list.component').then(m => m.VehiculosListComponent) },
      { path: 'clientes', canActivate: [moduleAccessGuard], data: { moduleKey: 'CLIENTES' }, loadComponent: () => import('./modules/clientes/pages/list/clientes-list.component').then(m => m.ClientesListComponent) },
      { path: 'bitacora', canActivate: [moduleAccessGuard], data: { moduleKey: 'BITACORA' }, loadComponent: () => import('./modules/bitacora/pages/bitacora-list.component').then(m => m.BitacoraListComponent) },
      { path: 'descuentos-viajes', canActivate: [moduleAccessGuard], data: { moduleKey: 'DESCUENTOS_VIAJES' }, loadComponent: () => import('./modules/descuentos-viajes/pages/descuentos-viajes-list.component').then(m => m.DescuentosViajesListComponent) },
      { path: 'placas', canActivate: [moduleAccessGuard], data: { moduleKey: 'PLACAS' }, loadComponent: () => import('./modules/placas/pages/placas-placeholder.component').then(m => m.PlacasPlaceholderComponent) }
    ]
  },
  { path: '**', redirectTo: 'auth/login' }
];
