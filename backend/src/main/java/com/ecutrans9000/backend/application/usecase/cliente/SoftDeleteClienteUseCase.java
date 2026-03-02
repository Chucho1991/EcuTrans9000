package com.ecutrans9000.backend.application.usecase.cliente;

import com.ecutrans9000.backend.application.cliente.ClienteApplicationService;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SoftDeleteClienteUseCase {

  private final ClienteApplicationService service;

  public void execute(UUID id, String actorUsername, String actorRole) {
    service.softDelete(id, actorUsername, actorRole);
  }
}
