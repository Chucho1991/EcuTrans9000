package com.ecutrans9000.backend.adapters.in.rest.dto.bitacora;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de salida para exponer viajes de bitácora.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViajeBitacoraResponse {
  private UUID id;
  private Integer numeroViaje;
  private LocalDate fechaViaje;
  private UUID vehiculoId;
  private String vehiculoPlaca;
  private String vehiculoChofer;
  private String vehiculoTonelajeCategoria;
  private BigDecimal vehiculoM3;
  private UUID clienteId;
  private String clienteNombre;
  private String clienteNombreComercial;
  private String destino;
  private String detalleViaje;
  private BigDecimal valor;
  private BigDecimal costoChofer;
  private BigDecimal estiba;
  private BigDecimal anticipo;
  private Boolean aplicaRetencion;
  private Boolean facturadoCliente;
  private String numeroFactura;
  private LocalDate fechaFactura;
  private LocalDate fechaPagoCliente;
  private Boolean pagadoTransportista;
  private String observaciones;
  private Boolean deleted;
  private LocalDateTime deletedAt;
  private String deletedBy;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
