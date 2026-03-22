package com.ecutrans9000.backend.adapters.in.rest.dto.cliente;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para crear o editar una equivalencia de cliente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEquivalenciaRequest {

  private UUID id;

  @NotBlank
  private String destino;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  private BigDecimal valorDestino;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  private BigDecimal costoChofer;
}
