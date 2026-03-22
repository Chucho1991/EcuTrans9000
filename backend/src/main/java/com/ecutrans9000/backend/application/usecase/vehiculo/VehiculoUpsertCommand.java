package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.domain.vehiculo.EstadoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.TipoDocumento;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Record publico de backend para VehiculoUpsertCommand.
 */
public record VehiculoUpsertCommand(
    String placa,
    String choferDefault,
    String licencia,
    LocalDate fechaCaducidadLicencia,
    TipoDocumento tipoDocumento,
    String documentoPersonal,
    String cuentaBancaria,
    String tonelajeCategoria,
    BigDecimal m3,
    EstadoVehiculo estado
) {
}
