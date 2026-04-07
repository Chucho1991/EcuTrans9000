package com.ecutrans9000.backend.application.bitacora;

import com.ecutrans9000.backend.adapters.out.persistence.repository.ViajeBitacoraJpaRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio que concentra validaciones de viajes pendientes para otros módulos.
 */
@Service
@RequiredArgsConstructor
public class ViajeBitacoraPendingValidationService {

  private final ViajeBitacoraJpaRepository viajeBitacoraJpaRepository;

  public boolean clienteTieneViajesPendientes(UUID clienteId) {
    return viajeBitacoraJpaRepository.existsClienteWithPendingStatuses(clienteId);
  }

  public boolean vehiculoTieneViajesPendientes(UUID vehiculoId) {
    return viajeBitacoraJpaRepository.existsVehiculoWithPendingStatuses(vehiculoId);
  }
}
