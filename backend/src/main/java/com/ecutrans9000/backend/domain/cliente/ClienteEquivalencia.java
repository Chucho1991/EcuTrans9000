package com.ecutrans9000.backend.domain.cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un valor de equivalencia por destino configurado para un cliente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEquivalencia {
  private UUID id;
  private String destino;
  private BigDecimal valorDestino;
  private BigDecimal costoChofer;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
