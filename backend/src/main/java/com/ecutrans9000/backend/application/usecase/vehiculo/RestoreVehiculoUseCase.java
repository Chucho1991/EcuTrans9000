package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Componente publico de backend para RestoreVehiculoUseCase.
 */
@Service
@RequiredArgsConstructor
public class RestoreVehiculoUseCase {

  private final VehiculoApplicationService service;

  public void execute(UUID id, String actorUsername, String actorRole) {
    service.restore(id, actorUsername, actorRole);
  }
}
