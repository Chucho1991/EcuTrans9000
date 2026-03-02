package com.ecutrans9000.backend.application.usecase.cliente;

import com.ecutrans9000.backend.application.cliente.ClienteApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DownloadClientesCsvTemplateUseCase {

  private final ClienteApplicationService service;

  public String execute() {
    return service.downloadTemplate();
  }
}
