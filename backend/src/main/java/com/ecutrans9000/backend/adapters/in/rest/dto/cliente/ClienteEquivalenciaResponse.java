package com.ecutrans9000.backend.adapters.in.rest.dto.cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de salida para una fila de equivalencia de cliente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEquivalenciaResponse {
  private UUID id;
  private String destino;
  private BigDecimal valorDestino;
  private BigDecimal costoChofer;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
