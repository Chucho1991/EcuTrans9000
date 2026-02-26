package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestoreVehiculoUseCase {

  private final VehiculoApplicationService service;

  public void execute(UUID id, String actorUsername, String actorRole) {
    service.restore(id, actorUsername, actorRole);
  }
}
