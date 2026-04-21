import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { buildApiUrl } from '../../../core/config/api.config';

export interface ModuleAccessItemResponse {
  moduleKey: string;
  moduleName: string;
  enabled: boolean;
}

export interface RoleModuleAccessResponse {
  role: string;
  modules: ModuleAccessItemResponse[];
}

export interface UpdateRoleModuleAccessRequest {
  modules: Array<{
    moduleKey: string;
    enabled: boolean;
  }>;
}

@Injectable({ providedIn: 'root' })
export class ModuleAccessSettingsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = buildApiUrl('/settings/module-access');

  list(): Observable<RoleModuleAccessResponse[]> {
    return this.http.get<RoleModuleAccessResponse[]>(this.baseUrl);
  }

  update(role: string, payload: UpdateRoleModuleAccessRequest): Observable<RoleModuleAccessResponse> {
    return this.http.put<RoleModuleAccessResponse>(`${this.baseUrl}/${role}`, payload);
  }
}
