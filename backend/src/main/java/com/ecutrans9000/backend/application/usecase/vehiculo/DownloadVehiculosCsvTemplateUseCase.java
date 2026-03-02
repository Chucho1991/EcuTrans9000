package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Componente publico de backend para DownloadVehiculosCsvTemplateUseCase.
 */
@Service
@RequiredArgsConstructor
public class DownloadVehiculosCsvTemplateUseCase {

  private final VehiculoApplicationService service;

  public byte[] execute() {
    return service.downloadTemplate();
  }

  public byte[] executeExample() {
    return service.downloadExampleTemplate();
  }
}
