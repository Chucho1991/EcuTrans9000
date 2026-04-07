package com.ecutrans9000.backend.application.usecase.cliente;

import com.ecutrans9000.backend.application.cliente.ClienteApplicationService;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/**
 * Caso de uso para eliminar físicamente clientes cuando la operación está autorizada.
 */
public class ForceDeleteClienteUseCase {

  private final ClienteApplicationService service;

  public void execute(UUID id, String actorUsername, String actorRole) {
    service.forceDelete(id, actorUsername, actorRole);
  }
}
