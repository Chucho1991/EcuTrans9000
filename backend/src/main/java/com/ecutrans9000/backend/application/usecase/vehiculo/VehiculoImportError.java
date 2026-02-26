package com.ecutrans9000.backend.application.usecase.vehiculo;

/**
 * Record publico de backend para VehiculoImportError.
 */
public record VehiculoImportError(
    int row,
    String column,
    String message
) {
}
