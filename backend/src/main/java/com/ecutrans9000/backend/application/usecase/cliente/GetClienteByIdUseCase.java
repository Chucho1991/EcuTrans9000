package com.ecutrans9000.backend.application.usecase.cliente;

import com.ecutrans9000.backend.application.cliente.ClienteApplicationService;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/**
 * Caso de uso para consultar el detalle de un cliente por identificador.
 */
public class GetClienteByIdUseCase {

  private final ClienteApplicationService service;

  public Cliente execute(UUID id) {
    return service.getById(id);
  }
}
