import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';

export type EstadoVehiculo = 'ACTIVO' | 'INACTIVO';
export type TipoDocumento = 'CEDULA' | 'RUC' | 'PASAPORTE';

export interface VehiculoResponse {
  id: string;
  placa: string;
  placaNorm: string;
  choferDefault: string;
  licencia: string | null;
  fechaCaducidadLicencia: string | null;
  tipoDocumento: TipoDocumento;
  documentoPersonal: string;
  tonelajeCategoria: string;
  m3: number;
  estado: EstadoVehiculo;
  fotoPath: string | null;
  docPath: string | null;
  licPath: string | null;
  deleted: boolean;
  deletedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface VehiculoListResponse {
  content: VehiculoResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface VehiculoUpsertRequest {
  placa: string;
  choferDefault: string;
  licencia: string;
  fechaCaducidadLicencia: string | null;
  tipoDocumento: TipoDocumento;
  documentoPersonal: string;
  tonelajeCategoria: string;
  m3: number;
  estado: EstadoVehiculo;
}

export interface VehiculoImportError {
  row: number;
  column: string;
  message: string;
}

export interface VehiculoImportResult {
  totalRows: number;
  processed: number;
  inserted: number;
  updated: number;
  skipped: number;
  errorsCount: number;
  errors: VehiculoImportError[];
}

export interface VehiculoFileDownload {
  blob: Blob;
  fileName: string | null;
}

@Injectable({ providedIn: 'root' })
export class VehiculosService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/vehiculos`;

  list(params: {
    page: number;
    size: number;
    q?: string;
    estado?: string;
    includeDeleted?: boolean;
  }): Observable<VehiculoListResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page)
      .set('size', params.size);

    if (params.q) {
      httpParams = httpParams.set('q', params.q);
    }
    if (params.estado) {
      httpParams = httpParams.set('estado', params.estado);
    }
    if (params.includeDeleted !== undefined) {
      httpParams = httpParams.set('includeDeleted', params.includeDeleted);
    }

    return this.http.get<VehiculoListResponse>(this.baseUrl, { params: httpParams });
  }

  getById(id: string): Observable<VehiculoResponse> {
    return this.http.get<VehiculoResponse>(`${this.baseUrl}/${id}`);
  }

  create(payload: VehiculoUpsertRequest): Observable<VehiculoResponse> {
    return this.http.post<VehiculoResponse>(this.baseUrl, payload);
  }

  update(id: string, payload: VehiculoUpsertRequest): Observable<VehiculoResponse> {
    return this.http.put<VehiculoResponse>(`${this.baseUrl}/${id}`, payload);
  }

  activate(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/activate`, {});
  }

  deactivate(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/deactivate`, {});
  }

  softDelete(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/soft-delete`, {});
  }

  restore(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${id}/restore`, {});
  }

  uploadFoto(id: string, file: File): Observable<VehiculoResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<VehiculoResponse>(`${this.baseUrl}/${id}/foto`, formData);
  }

  uploadDocumento(id: string, file: File): Observable<VehiculoResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<VehiculoResponse>(`${this.baseUrl}/${id}/documento`, formData);
  }

  uploadLicenciaImg(id: string, file: File): Observable<VehiculoResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<VehiculoResponse>(`${this.baseUrl}/${id}/licencia-img`, formData);
  }

  getFotoBlob(id: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/foto`, { responseType: 'blob' });
  }

  getDocumentoBlob(id: string): Observable<VehiculoFileDownload> {
    return this.http.get(`${this.baseUrl}/${id}/documento`, {
      observe: 'response',
      responseType: 'blob'
    }).pipe(map((response) => this.toFileDownload(response)));
  }

  getLicenciaBlob(id: string): Observable<VehiculoFileDownload> {
    return this.http.get(`${this.baseUrl}/${id}/licencia-img`, {
      observe: 'response',
      responseType: 'blob'
    }).pipe(map((response) => this.toFileDownload(response)));
  }

  downloadTemplate(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/import/template`, { responseType: 'blob' });
  }

  downloadTemplateExample(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/import/template-example`, { responseType: 'blob' });
  }

  previewImport(file: File, mode: 'INSERT_ONLY' | 'UPSERT', partialOk: boolean): Observable<VehiculoImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<VehiculoImportResult>(`${this.baseUrl}/import/preview?mode=${mode}&partialOk=${partialOk}`, formData);
  }

  importExcel(file: File, mode: 'INSERT_ONLY' | 'UPSERT', partialOk: boolean): Observable<VehiculoImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<VehiculoImportResult>(`${this.baseUrl}/import?mode=${mode}&partialOk=${partialOk}`, formData);
  }

  private toFileDownload(response: HttpResponse<Blob>): VehiculoFileDownload {
    return {
      blob: response.body ?? new Blob(),
      fileName: this.extractFileName(response.headers.get('content-disposition'))
    };
  }

  private extractFileName(contentDisposition: string | null): string | null {
    if (!contentDisposition) {
      return null;
    }

    const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8Match?.[1]) {
      return decodeURIComponent(utf8Match[1]).replace(/["']/g, '').trim();
    }

    const basicMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
    return basicMatch?.[1]?.trim() ?? null;
  }
}
