import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, finalize, map, shareReplay, tap } from 'rxjs/operators';

import { buildApiUrl } from '../config/api.config';

const USER_KEY = 'ecutrans9000_user';
const CONFIGURABLE_MODULE_KEYS = ['DASHBOARD', 'VEHICULOS', 'CLIENTES', 'BITACORA', 'PLACAS'] as const;

interface CurrentModuleAccessResponse {
  role: string;
  allowedModules: string[];
}

@Injectable({ providedIn: 'root' })
export class ModuleAccessService {
  private readonly http = inject(HttpClient);
  private readonly accessState = signal<Set<string>>(new Set());
  private readonly loadedState = signal(false);
  private pendingRequest$: Observable<Set<string>> | null = null;

  hasAccess(moduleKey: string): boolean {
    if (this.isSuperadmin()) {
      return true;
    }
    return this.accessState().has(moduleKey);
  }

  isLoaded(): boolean {
    return this.loadedState();
  }

  fetchMyAccess(force = false): Observable<Set<string>> {
    if (!this.getRole()) {
      this.clearCache();
      return of(new Set<string>());
    }

    if (this.isSuperadmin()) {
      const allModules = new Set<string>(CONFIGURABLE_MODULE_KEYS);
      this.accessState.set(allModules);
      this.loadedState.set(true);
      return of(allModules);
    }

    if (!force && this.loadedState()) {
      return of(new Set(this.accessState()));
    }

    if (!this.pendingRequest$ || force) {
      this.pendingRequest$ = this.http.get<CurrentModuleAccessResponse>(buildApiUrl('/settings/module-access/me')).pipe(
        map((response) => new Set(response.allowedModules ?? [])),
        tap((modules) => {
          this.accessState.set(modules);
          this.loadedState.set(true);
        }),
        catchError(() => {
          this.accessState.set(new Set<string>());
          this.loadedState.set(true);
          return of(new Set<string>());
        }),
        finalize(() => {
          this.pendingRequest$ = null;
        }),
        shareReplay(1)
      );
    }

    return this.pendingRequest$;
  }

  clearCache(): void {
    this.accessState.set(new Set());
    this.loadedState.set(false);
    this.pendingRequest$ = null;
  }

  private isSuperadmin(): boolean {
    return this.getRole() === 'SUPERADMINISTRADOR';
  }

  private getRole(): string | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return (JSON.parse(raw) as { rol?: string }).rol ?? null;
    } catch {
      return null;
    }
  }
}
