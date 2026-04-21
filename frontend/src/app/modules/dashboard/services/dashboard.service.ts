import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../../core/config/api.config';

export interface DashboardOverview {
  usuariosActivos: number;
  usuariosInactivos: number;
  totalVehiculos: number;
  vehiculosActivos: number;
  vehiculosInactivos: number;
  clientesActivos: number;
  clientesInactivos: number;
  viajesMesActual: number;
  viajesPendientesFacturar: number;
  alertasActivas: number;
}

export interface DashboardFinancialSummary {
  facturacionTotal: number;
  cobrosTotal: number;
  anticiposTotal: number;
  facturacionMesActual: number;
  cobrosMesActual: number;
  anticiposMesActual: number;
  pendienteFacturar: number;
  pendienteCobrar: number;
}

export interface DashboardLicenseAlerts {
  vencidas: number;
  porVencer7Dias: number;
  porVencer30Dias: number;
}

export interface DashboardDistributionItem {
  label: string;
  total: number;
  tone: 'success' | 'warning' | 'danger' | 'brand';
}

export interface DashboardMonthlyTripsItem {
  label: string;
  monthStart: string;
  viajes: number;
  valor: number;
}

export interface DashboardRankingItem {
  label: string;
  secondaryLabel: string | null;
  total: number;
  amount: number;
}

export interface DashboardRecentTripItem {
  numeroViaje: number;
  fechaViaje: string;
  placa: string;
  cliente: string;
  destino: string;
  valor: number;
  facturadoCliente: boolean;
  pagadoTransportista: boolean;
}

export interface DashboardResponse {
  generatedAt: string;
  overview: DashboardOverview;
  financial: DashboardFinancialSummary;
  licenseAlerts: DashboardLicenseAlerts;
  vehiculosPorEstado: DashboardDistributionItem[];
  viajesPorMes: DashboardMonthlyTripsItem[];
  topDestinos: DashboardRankingItem[];
  topClientes: DashboardRankingItem[];
  topVehiculos: DashboardRankingItem[];
  viajesRecientes: DashboardRecentTripItem[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  getMetrics(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${API_BASE_URL}/dashboard`);
  }
}
