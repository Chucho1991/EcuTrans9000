import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { buildApiUrl } from '../../../core/config/api.config';

export interface UserResponse {
  id: string;
  nombres: string;
  correo: string;
  username: string;
  rol: string;
  activo: boolean;
  deleted: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserListResponse {
  content: UserResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreateUserRequest {
  nombres: string;
  correo: string;
  username: string;
  password: string;
  confirmPassword: string;
  rol: string;
  activo: boolean;
}

export interface UpdateUserRequest {
  nombres: string;
  correo: string;
  username: string;
  rol: string;
  activo: boolean;
}

export interface UpdateMyProfileRequest {
  nombres: string;
  correo: string;
  username: string;
  password?: string;
  confirmPassword?: string;
}

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = buildApiUrl('/users');

  list(params: {
    page: number;
    size: number;
    rol?: string;
    activo?: boolean | null;
    deleted?: boolean | null;
  }): Observable<UserListResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page)
      .set('size', params.size);
    if (params.rol) {
      httpParams = httpParams.set('rol', params.rol);
    }
    if (params.activo !== null && params.activo !== undefined) {
      httpParams = httpParams.set('activo', params.activo);
    }
    if (params.deleted !== null && params.deleted !== undefined) {
      httpParams = httpParams.set('deleted', params.deleted);
    }
    return this.http.get<UserListResponse>(this.baseUrl, { params: httpParams });
  }

  getById(id: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/${id}`);
  }

  create(payload: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.baseUrl, payload);
  }

  update(id: string, payload: UpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.baseUrl}/${id}`, payload);
  }

  softDelete(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/soft-delete`, {});
  }

  restore(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/restore`, {});
  }

  activate(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/activate`, {});
  }

  deactivate(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/deactivate`, {});
  }

  hardDelete(id: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/${id}`);
  }

  me(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/me`);
  }

  updateMe(payload: UpdateMyProfileRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.baseUrl}/me`, payload);
  }
}
