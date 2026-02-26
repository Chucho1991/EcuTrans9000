package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Componente publico de backend para CreateVehiculoUseCase.
 */
@Service
@RequiredArgsConstructor
public class CreateVehiculoUseCase {

  private final VehiculoApplicationService service;

  public Vehiculo execute(VehiculoUpsertCommand command, String actorUsername, String actorRole) {
    return service.create(command, actorUsername, actorRole);
  }
}
