package com.ecutrans9000.backend.adapters.in.rest.dto.placas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO principal de respuesta para la consulta financiera por placa.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaPlacaResponse {
  private Boolean aplicaRetencion;
  private String placa;
  private String chofer;
  private LocalDate fechaDesde;
  private LocalDate fechaHasta;
  private List<ConsultaPlacaDetalleResponse> registros;
  private BigDecimal valorFacturaTotal;
  private BigDecimal totalDescuentos;
  private BigDecimal retencionUnoPorciento;
  private BigDecimal comisionAdministrativaSeisPorciento;
  private BigDecimal anticiposTotal;
  private BigDecimal pagoTotal;
}
