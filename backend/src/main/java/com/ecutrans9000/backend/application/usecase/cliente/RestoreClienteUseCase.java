package com.ecutrans9000.backend.application.usecase.cliente;

import com.ecutrans9000.backend.application.cliente.ClienteApplicationService;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/**
 * Caso de uso para restaurar clientes eliminados lógicamente.
 */
public class RestoreClienteUseCase {

  private final ClienteApplicationService service;

  public Cliente execute(UUID id, String actorUsername, String actorRole) {
    return service.restore(id, actorUsername, actorRole);
  }
}
