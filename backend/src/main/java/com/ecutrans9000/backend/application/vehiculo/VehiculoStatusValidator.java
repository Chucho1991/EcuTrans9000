package com.ecutrans9000.backend.application.vehiculo;

import com.ecutrans9000.backend.domain.vehiculo.EstadoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import com.ecutrans9000.backend.ports.out.vehiculo.VehiculoRepositoryPort;
import com.ecutrans9000.backend.service.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Validador reutilizable de estado/soft-delete por placa.
 */
@Component
@RequiredArgsConstructor
public class VehiculoStatusValidator {

  private final VehiculoRepositoryPort vehiculoRepositoryPort;

  /**
   * Valida que la placa exista, no esté eliminada y esté activa.
   *
   * @throws BusinessException si la placa no cumple condiciones operativas
   */
  public void validatePlacaActiva(String placa) {
    String placaNorm = Vehiculo.normalizePlaca(placa);
    Vehiculo vehiculo = vehiculoRepositoryPort.findByPlacaNorm(placaNorm)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado para placa: " + placaNorm));

    if (Boolean.TRUE.equals(vehiculo.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Vehiculo eliminado logicamente: " + placaNorm);
    }
    if (vehiculo.getEstado() != EstadoVehiculo.ACTIVO) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Vehiculo inactivo: " + placaNorm);
    }
  }
}
