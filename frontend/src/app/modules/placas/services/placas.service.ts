import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';

export interface ConsultaPlacaDetalleResponse {
  ordenCompra: string;
  valor: number;
  fecha: string | null;
  factura: string;
  anticipo: number;
  estiba: number;
  despacho: string;
  cliente: string;
  origenDestino: string;
}

export interface ConsultaPlacaResponse {
  placa: string | null;
  chofer: string | null;
  fechaDesde: string | null;
  fechaHasta: string | null;
  registros: ConsultaPlacaDetalleResponse[];
  valorFacturaTotal: number;
  retencionUnoPorciento: number;
  comisionAdministrativaSeisPorciento: number;
  anticiposTotal: number;
  pagoTotal: number;
}

@Injectable({ providedIn: 'root' })
export class PlacasService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/placas/consulta`;

  consultar(params: { placa?: string; fechaDesde?: string; fechaHasta?: string }): Observable<ConsultaPlacaResponse> {
    let httpParams = new HttpParams();

    if (params.placa) {
      httpParams = httpParams.set('placa', params.placa);
    }
    if (params.fechaDesde) {
      httpParams = httpParams.set('fechaDesde', params.fechaDesde);
    }
    if (params.fechaHasta) {
      httpParams = httpParams.set('fechaHasta', params.fechaHasta);
    }

    return this.http.get<ConsultaPlacaResponse>(this.baseUrl, { params: httpParams });
  }

  exportar(params: { placa?: string; fechaDesde?: string; fechaHasta?: string }): Observable<Blob> {
    let httpParams = new HttpParams();

    if (params.placa) {
      httpParams = httpParams.set('placa', params.placa);
    }
    if (params.fechaDesde) {
      httpParams = httpParams.set('fechaDesde', params.fechaDesde);
    }
    if (params.fechaHasta) {
      httpParams = httpParams.set('fechaHasta', params.fechaHasta);
    }

    return this.http.get(`${this.baseUrl}/export`, { params: httpParams, responseType: 'blob' });
  }
}
