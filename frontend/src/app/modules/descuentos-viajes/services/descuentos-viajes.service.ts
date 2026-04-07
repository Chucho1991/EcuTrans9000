import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';

export interface DescuentoViajeResponse {
  id: number;
  vehiculoId: string;
  vehiculoPlaca: string;
  vehiculoChofer: string;
  descripcionMotivo: string;
  montoMotivo: number;
  fechaAplicacion: string | null;
  activo: boolean;
  deleted: boolean;
  deletedAt: string | null;
  deletedBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DescuentoViajeListResponse {
  content: DescuentoViajeResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface DescuentoViajeUpsertRequest {
  vehiculoId: string;
  descripcionMotivo: string;
  montoMotivo: number;
  fechaAplicacion: string | null;
  activo: boolean;
}

export interface DescuentoViajeImportError {
  row: number;
  column: string;
  message: string;
}

export interface DescuentoViajeImportResult {
  totalRows: number;
  processed: number;
  inserted: number;
  updated: number;
  skipped: number;
  errorsCount: number;
  errors: DescuentoViajeImportError[];
}

@Injectable({ providedIn: 'root' })
export class DescuentosViajesService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/api/descuentos-viajes`;

  list(params: {
    page: number;
    size: number;
    q?: string;
    vehiculoId?: string;
    activo?: boolean | '';
    includeDeleted?: boolean;
  }): Observable<DescuentoViajeListResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page)
      .set('size', params.size);

    if (params.q) {
      httpParams = httpParams.set('q', params.q);
    }
    if (params.vehiculoId) {
      httpParams = httpParams.set('vehiculoId', params.vehiculoId);
    }
    if (params.activo !== '' && params.activo !== undefined) {
      httpParams = httpParams.set('activo', params.activo);
    }
    if (params.includeDeleted !== undefined) {
      httpParams = httpParams.set('includeDeleted', params.includeDeleted);
    }

    return this.http.get<DescuentoViajeListResponse>(this.baseUrl, { params: httpParams });
  }

  getById(id: number): Observable<DescuentoViajeResponse> {
    return this.http.get<DescuentoViajeResponse>(`${this.baseUrl}/${id}`);
  }

  create(payload: DescuentoViajeUpsertRequest): Observable<DescuentoViajeResponse> {
    return this.http.post<DescuentoViajeResponse>(this.baseUrl, payload);
  }

  update(id: number, payload: DescuentoViajeUpsertRequest): Observable<DescuentoViajeResponse> {
    return this.http.put<DescuentoViajeResponse>(`${this.baseUrl}/${id}`, payload);
  }

  activate(id: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/activate`, {});
  }

  deactivate(id: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/deactivate`, {});
  }

  softDelete(id: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/soft-delete`, {});
  }

  restore(id: number): Observable<DescuentoViajeResponse> {
    return this.http.patch<DescuentoViajeResponse>(`${this.baseUrl}/${id}/restore`, {});
  }

  downloadTemplate(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/import/template`, { responseType: 'blob' });
  }

  downloadExampleTemplate(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/import/template/example`, { responseType: 'blob' });
  }

  previewImport(file: File, mode: 'INSERT_ONLY' | 'UPSERT', partialOk: boolean): Observable<DescuentoViajeImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<DescuentoViajeImportResult>(`${this.baseUrl}/import/preview?mode=${mode}&partialOk=${partialOk}`, formData);
  }

  importExcel(file: File, mode: 'INSERT_ONLY' | 'UPSERT', partialOk: boolean): Observable<DescuentoViajeImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<DescuentoViajeImportResult>(`${this.baseUrl}/import?mode=${mode}&partialOk=${partialOk}`, formData);
  }
}
