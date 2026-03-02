import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';

export interface ViajeBitacoraResponse {
  id: string;
  numeroViaje: number;
  fechaViaje: string;
  vehiculoId: string;
  vehiculoPlaca: string;
  vehiculoChofer: string;
  vehiculoTonelajeCategoria: string;
  vehiculoM3: number;
  clienteId: string;
  clienteNombre: string;
  clienteNombreComercial: string | null;
  destino: string;
  detalleViaje: string | null;
  valor: number;
  estiba: number;
  anticipo: number;
  facturadoCliente: boolean;
  numeroFactura: string | null;
  fechaFactura: string | null;
  fechaPagoCliente: string | null;
  pagadoTransportista: boolean;
  observaciones: string | null;
  deleted: boolean;
  deletedAt: string | null;
  deletedBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ViajeBitacoraListResponse {
  content: ViajeBitacoraResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ViajeBitacoraUpsertRequest {
  numeroViaje: number;
  fechaViaje: string;
  vehiculoId: string;
  clienteId: string;
  destino: string;
  detalleViaje: string;
  valor: number;
  estiba: number;
  anticipo: number;
  facturadoCliente: boolean;
  numeroFactura: string;
  fechaFactura: string | null;
  fechaPagoCliente: string | null;
  pagadoTransportista: boolean;
  observaciones: string;
}

export interface ViajeBitacoraImportError {
  row: number;
  column: string;
  message: string;
}

export interface ViajeBitacoraImportResult {
  totalRows: number;
  processed: number;
  inserted: number;
  updated: number;
  skipped: number;
  errorsCount: number;
  errors: ViajeBitacoraImportError[];
}

@Injectable({ providedIn: 'root' })
export class BitacoraService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/api/bitacora/viajes`;

  list(params: {
    page: number;
    size: number;
    q?: string;
    vehiculoId?: string;
    clienteId?: string;
    fechaDesde?: string;
    fechaHasta?: string;
    includeDeleted?: boolean;
  }): Observable<ViajeBitacoraListResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page)
      .set('size', params.size);

    if (params.q) {
      httpParams = httpParams.set('q', params.q);
    }
    if (params.vehiculoId) {
      httpParams = httpParams.set('vehiculoId', params.vehiculoId);
    }
    if (params.clienteId) {
      httpParams = httpParams.set('clienteId', params.clienteId);
    }
    if (params.fechaDesde) {
      httpParams = httpParams.set('fechaDesde', params.fechaDesde);
    }
    if (params.fechaHasta) {
      httpParams = httpParams.set('fechaHasta', params.fechaHasta);
    }
    if (params.includeDeleted !== undefined) {
      httpParams = httpParams.set('includeDeleted', params.includeDeleted);
    }

    return this.http.get<ViajeBitacoraListResponse>(this.baseUrl, { params: httpParams });
  }

  getById(id: string): Observable<ViajeBitacoraResponse> {
    return this.http.get<ViajeBitacoraResponse>(`${this.baseUrl}/${id}`);
  }

  create(payload: ViajeBitacoraUpsertRequest): Observable<ViajeBitacoraResponse> {
    return this.http.post<ViajeBitacoraResponse>(this.baseUrl, payload);
  }

  update(id: string, payload: ViajeBitacoraUpsertRequest): Observable<ViajeBitacoraResponse> {
    return this.http.put<ViajeBitacoraResponse>(`${this.baseUrl}/${id}`, payload);
  }

  softDelete(id: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/${id}`);
  }

  restore(id: string): Observable<ViajeBitacoraResponse> {
    return this.http.patch<ViajeBitacoraResponse>(`${this.baseUrl}/${id}/restore`, {});
  }

  downloadExcel(params: {
    q?: string;
    vehiculoId?: string;
    clienteId?: string;
    fechaDesde?: string;
    fechaHasta?: string;
  }): Observable<Blob> {
    let httpParams = new HttpParams();

    if (params.q) {
      httpParams = httpParams.set('q', params.q);
    }
    if (params.vehiculoId) {
      httpParams = httpParams.set('vehiculoId', params.vehiculoId);
    }
    if (params.clienteId) {
      httpParams = httpParams.set('clienteId', params.clienteId);
    }
    if (params.fechaDesde) {
      httpParams = httpParams.set('fechaDesde', params.fechaDesde);
    }
    if (params.fechaHasta) {
      httpParams = httpParams.set('fechaHasta', params.fechaHasta);
    }

    return this.http.get(`${this.baseUrl}/export`, { params: httpParams, responseType: 'blob' });
  }

  downloadTemplate(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/import/template`, { responseType: 'blob' });
  }

  downloadExampleTemplate(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/import/template/example`, { responseType: 'blob' });
  }

  previewImport(file: File, mode: 'INSERT_ONLY' | 'UPSERT', partialOk: boolean): Observable<ViajeBitacoraImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ViajeBitacoraImportResult>(`${this.baseUrl}/import/preview?mode=${mode}&partialOk=${partialOk}`, formData);
  }

  importExcel(file: File, mode: 'INSERT_ONLY' | 'UPSERT', partialOk: boolean): Observable<ViajeBitacoraImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ViajeBitacoraImportResult>(`${this.baseUrl}/import?mode=${mode}&partialOk=${partialOk}`, formData);
  }
}
