import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';

export type TipoDocumentoCliente = 'CEDULA' | 'RUC' | 'PASAPORTE';

export interface ClienteResponse {
  id: string;
  tipoDocumento: TipoDocumentoCliente;
  documento: string;
  nombre: string;
  direccion: string | null;
  descripcion: string | null;
  logoPath: string | null;
  activo: boolean;
  deleted: boolean;
  deletedAt: string | null;
  deletedBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ClienteListResponse {
  content: ClienteResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ClienteUpsertRequest {
  tipoDocumento: TipoDocumentoCliente;
  documento: string;
  nombre: string;
  direccion: string;
  descripcion: string;
  activo: boolean;
}

export interface ClienteImportError {
  row: number;
  column: string;
  message: string;
}

export interface ClienteImportResult {
  totalRows: number;
  processed: number;
  inserted: number;
  updated: number;
  skipped: number;
  errorsCount: number;
  errors: ClienteImportError[];
}

@Injectable({ providedIn: 'root' })
export class ClientesService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/clientes`;

  list(params: {
    page: number;
    size: number;
    q?: string;
    includeDeleted?: boolean;
  }): Observable<ClienteListResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page)
      .set('size', params.size);

    if (params.q) {
      httpParams = httpParams.set('q', params.q);
    }
    if (params.includeDeleted !== undefined) {
      httpParams = httpParams.set('includeDeleted', params.includeDeleted);
    }

    return this.http.get<ClienteListResponse>(this.baseUrl, { params: httpParams });
  }

  getById(id: string): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(`${this.baseUrl}/${id}`);
  }

  create(payload: ClienteUpsertRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(this.baseUrl, payload);
  }

  update(id: string, payload: ClienteUpsertRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${this.baseUrl}/${id}`, payload);
  }

  toggleActivo(id: string): Observable<ClienteResponse> {
    return this.http.patch<ClienteResponse>(`${this.baseUrl}/${id}/toggle-activo`, {});
  }

  softDelete(id: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/${id}`);
  }

  restore(id: string): Observable<ClienteResponse> {
    return this.http.patch<ClienteResponse>(`${this.baseUrl}/${id}/restore`, {});
  }

  forceDelete(id: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/${id}/force`);
  }

  uploadLogo(id: string, file: File): Observable<ClienteResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ClienteResponse>(`${this.baseUrl}/${id}/logo`, formData);
  }

  getLogoBlob(id: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/logo`, { responseType: 'blob' });
  }

  downloadTemplate(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/import/template`, { responseType: 'blob' });
  }

  downloadExampleTemplate(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/import/template/example`, { responseType: 'blob' });
  }

  previewImport(file: File, mode: 'INSERT_ONLY' | 'UPSERT', partialOk: boolean): Observable<ClienteImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ClienteImportResult>(`${this.baseUrl}/import/preview?mode=${mode}&partialOk=${partialOk}`, formData);
  }

  importExcel(file: File, mode: 'INSERT_ONLY' | 'UPSERT', partialOk: boolean): Observable<ClienteImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ClienteImportResult>(`${this.baseUrl}/import?mode=${mode}&partialOk=${partialOk}`, formData);
  }
}
