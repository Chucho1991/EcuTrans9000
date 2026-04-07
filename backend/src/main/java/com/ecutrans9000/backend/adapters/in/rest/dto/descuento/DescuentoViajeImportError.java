package com.ecutrans9000.backend.adapters.in.rest.dto.descuento;

/**
 * DTO de error por fila durante la importación Excel de descuentos de viajes.
 */
public record DescuentoViajeImportError(
    int row,
    String column,
    String message
) {
}
