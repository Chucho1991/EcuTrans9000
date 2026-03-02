package com.ecutrans9000.backend.adapters.in.rest.dto.placas;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de detalle para el reporte de consulta por placa.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaPlacaDetalleResponse {
  private String ordenCompra;
  private BigDecimal valor;
  private LocalDate fecha;
  private String factura;
  private BigDecimal anticipo;
  private BigDecimal estiba;
  private String despacho;
  private String cliente;
  private String origenDestino;
}
