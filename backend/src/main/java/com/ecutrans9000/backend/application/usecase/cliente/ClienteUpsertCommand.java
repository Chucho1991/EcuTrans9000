package com.ecutrans9000.backend.application.usecase.cliente;

import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;

public record ClienteUpsertCommand(
    TipoDocumentoCliente tipoDocumento,
    String documento,
    String nombre,
    String nombreComercial,
    String descripcion,
    Boolean activo) {
}
