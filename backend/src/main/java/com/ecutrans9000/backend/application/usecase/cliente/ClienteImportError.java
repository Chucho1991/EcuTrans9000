package com.ecutrans9000.backend.application.usecase.cliente;

public record ClienteImportError(
    int row,
    String column,
    String message
) {
}
