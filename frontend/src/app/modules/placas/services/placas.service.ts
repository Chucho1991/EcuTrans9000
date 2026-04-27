import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';

export interface ConsultaPlacaDetalleResponse {
  id: string;
  ordenCompra: string;
  valor: number;
  valorBitacora: number;
  fecha: string | null;
  factura: string;
  anticipo: number;
  estiba: number;
  aplicaRetencion: boolean;
  despacho: string;
  cliente: string;
  origenDestino: string;
  pagadoTransportista: boolean;
}

export interface ConsultaPlacaResponse {
  placa: string | null;
  chofer: string | null;
  fechaDesde: string | null;
  fechaHasta: string | null;
  registros: ConsultaPlacaDetalleResponse[];
  valorFacturaTotal: number;
  totalDescuentos: number;
  retencionUnoPorciento: number;
  comisionAdministrativaSeisPorciento: number;
  anticiposTotal: number;
  pagoTotal: number;
}

@Injectable({ providedIn: 'root' })
export class PlacasService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/placas/consulta`;

  consultar(params: {
    placa: string;
    codigoViaje?: string;
    estadoPagoChofer?: string;
    fechaDesde: string;
    fechaHasta: string;
  }): Observable<ConsultaPlacaResponse> {
    let httpParams = new HttpParams()
      .set('placa', params.placa)
      .set('fechaDesde', params.fechaDesde)
      .set('fechaHasta', params.fechaHasta);

    if (params.codigoViaje) {
      httpParams = httpParams.set('codigoViaje', params.codigoViaje);
    }
    if (params.estadoPagoChofer) {
      httpParams = httpParams.set('estadoPagoChofer', params.estadoPagoChofer);
    }

    return this.http.get<ConsultaPlacaResponse>(this.baseUrl, { params: httpParams });
  }

  exportar(params: {
    placa: string;
    codigoViaje?: string;
    estadoPagoChofer?: string;
    fechaDesde: string;
    fechaHasta: string;
    descuentoIds?: number[];
    viajeIds?: string[];
  }): Observable<Blob> {
    let httpParams = new HttpParams()
      .set('placa', params.placa)
      .set('fechaDesde', params.fechaDesde)
      .set('fechaHasta', params.fechaHasta);

    if (params.codigoViaje) {
      httpParams = httpParams.set('codigoViaje', params.codigoViaje);
    }
    if (params.estadoPagoChofer) {
      httpParams = httpParams.set('estadoPagoChofer', params.estadoPagoChofer);
    }
    for (const descuentoId of params.descuentoIds ?? []) {
      httpParams = httpParams.append('descuentoIds', descuentoId);
    }
    for (const viajeId of params.viajeIds ?? []) {
      httpParams = httpParams.append('viajeIds', viajeId);
    }

    return this.http.get(`${this.baseUrl}/export`, { params: httpParams, responseType: 'blob' });
  }
}
