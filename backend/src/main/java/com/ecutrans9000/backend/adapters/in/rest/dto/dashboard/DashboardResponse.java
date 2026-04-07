package com.ecutrans9000.backend.adapters.in.rest.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO principal de salida para el dashboard administrativo.
 */
public class DashboardResponse {
  private LocalDateTime generatedAt;
  private Overview overview;
  private FinancialSummary financial;
  private LicenseAlerts licenseAlerts;
  private List<DistributionItem> vehiculosPorEstado;
  private List<MonthlyTripsItem> viajesPorMes;
  private List<RankingItem> topDestinos;
  private List<RankingItem> topClientes;
  private List<RankingItem> topVehiculos;
  private List<RecentTripItem> viajesRecientes;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Overview {
    private long usuariosActivos;
    private long usuariosInactivos;
    private long totalVehiculos;
    private long vehiculosActivos;
    private long vehiculosInactivos;
    private long clientesActivos;
    private long clientesInactivos;
    private long viajesMesActual;
    private long viajesPendientesFacturar;
    private long alertasActivas;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FinancialSummary {
    private BigDecimal facturacionTotal;
    private BigDecimal cobrosTotal;
    private BigDecimal anticiposTotal;
    private BigDecimal facturacionMesActual;
    private BigDecimal cobrosMesActual;
    private BigDecimal anticiposMesActual;
    private BigDecimal pendienteFacturar;
    private BigDecimal pendienteCobrar;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LicenseAlerts {
    private long vencidas;
    private long porVencer7Dias;
    private long porVencer30Dias;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DistributionItem {
    private String label;
    private long total;
    private String tone;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MonthlyTripsItem {
    private String label;
    private LocalDate monthStart;
    private long viajes;
    private BigDecimal valor;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RankingItem {
    private String label;
    private String secondaryLabel;
    private long total;
    private BigDecimal amount;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RecentTripItem {
    private Integer numeroViaje;
    private LocalDate fechaViaje;
    private String placa;
    private String cliente;
    private String destino;
    private BigDecimal valor;
    private Boolean facturadoCliente;
    private Boolean pagadoTransportista;
  }
}
