package com.ecutrans9000.backend.adapters.in.rest.dto.descuento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para crear o actualizar descuentos de viajes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoViajeUpsertRequest {

  @NotNull
  private UUID vehiculoId;

  @NotBlank
  private String descripcionMotivo;

  @NotNull
  @DecimalMin(value = "0", inclusive = true)
  private BigDecimal montoMotivo;

  private LocalDate fechaAplicacion;

  @NotNull
  private Boolean activo;
}
