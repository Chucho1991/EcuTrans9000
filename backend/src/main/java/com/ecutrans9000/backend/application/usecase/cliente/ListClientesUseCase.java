package com.ecutrans9000.backend.application.usecase.cliente;

import com.ecutrans9000.backend.application.cliente.ClienteApplicationService;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListClientesUseCase {

  private final ClienteApplicationService service;

  public org.springframework.data.domain.Page<Cliente> execute(int page, int size, String q, Boolean includeDeleted) {
    return service.list(page, size, q, includeDeleted);
  }
}
