package com.ecutrans9000.backend.application.usecase.cliente;

/**
 * Error por fila detectado durante la importación de clientes.
 */
public record ClienteImportError(
    int row,
    String column,
    String message
) {
}
