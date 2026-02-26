package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.domain.vehiculo.EstadoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.TipoDocumento;
import java.math.BigDecimal;

public record VehiculoUpsertCommand(
    String placa,
    String choferDefault,
    String licencia,
    TipoDocumento tipoDocumento,
    String documentoPersonal,
    String tonelajeCategoria,
    BigDecimal m3,
    EstadoVehiculo estado
) {
}
