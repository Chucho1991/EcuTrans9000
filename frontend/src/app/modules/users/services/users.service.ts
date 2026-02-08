import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';

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
    return this.http.get<UserListResponse>(`${API_BASE_URL}/users`, { params: httpParams });
  }

  getById(id: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${API_BASE_URL}/users/${id}`);
  }

  create(payload: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${API_BASE_URL}/users`, payload);
  }

  update(id: string, payload: UpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${API_BASE_URL}/users/${id}`, payload);
  }

  softDelete(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${API_BASE_URL}/users/${id}/soft-delete`, {});
  }

  restore(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${API_BASE_URL}/users/${id}/restore`, {});
  }

  activate(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${API_BASE_URL}/users/${id}/activate`, {});
  }

  deactivate(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${API_BASE_URL}/users/${id}/deactivate`, {});
  }

  hardDelete(id: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${API_BASE_URL}/users/${id}`);
  }

  me(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${API_BASE_URL}/users/me`);
  }

  updateMe(payload: UpdateMyProfileRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${API_BASE_URL}/users/me`, payload);
  }
}
