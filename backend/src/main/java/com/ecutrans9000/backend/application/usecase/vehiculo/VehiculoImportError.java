package com.ecutrans9000.backend.application.usecase.vehiculo;

public record VehiculoImportError(
    int row,
    String column,
    String message
) {
}
