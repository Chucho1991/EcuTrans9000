package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Componente publico de backend para UpdateVehiculoUseCase.
 */
@Service
@RequiredArgsConstructor
public class UpdateVehiculoUseCase {

  private final VehiculoApplicationService service;

  public Vehiculo execute(UUID id, VehiculoUpsertCommand command, String actorUsername, String actorRole) {
    return service.update(id, command, actorUsername, actorRole);
  }
}
