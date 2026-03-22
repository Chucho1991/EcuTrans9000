package com.ecutrans9000.backend.application.usecase.cliente;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Comando de escritura para una fila de tabla de equivalencia de cliente.
 */
public record ClienteEquivalenciaUpsertCommand(
    UUID id,
    String destino,
    BigDecimal valorDestino,
    BigDecimal costoChofer) {
}
