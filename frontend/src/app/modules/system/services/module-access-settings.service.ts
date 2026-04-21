import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';

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

  list(): Observable<RoleModuleAccessResponse[]> {
    return this.http.get<RoleModuleAccessResponse[]>(`${API_BASE_URL}/settings/module-access`);
  }

  update(role: string, payload: UpdateRoleModuleAccessRequest): Observable<RoleModuleAccessResponse> {
    return this.http.put<RoleModuleAccessResponse>(`${API_BASE_URL}/settings/module-access/${role}`, payload);
  }
}
