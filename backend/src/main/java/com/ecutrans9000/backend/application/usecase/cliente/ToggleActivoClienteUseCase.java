package com.ecutrans9000.backend.application.usecase.cliente;

import com.ecutrans9000.backend.application.cliente.ClienteApplicationService;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToggleActivoClienteUseCase {

  private final ClienteApplicationService service;

  public Cliente execute(UUID id, String actorUsername, String actorRole) {
    return service.toggleActivo(id, actorUsername, actorRole);
  }
}
