package com.ecutrans9000.backend.adapters.in.rest.dto.descuento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de salida para descuentos de viajes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoViajeResponse {
  private Long id;
  private UUID vehiculoId;
  private String vehiculoPlaca;
  private String vehiculoChofer;
  private String descripcionMotivo;
  private BigDecimal montoMotivo;
  private Boolean activo;
  private Boolean deleted;
  private LocalDateTime deletedAt;
  private String deletedBy;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
