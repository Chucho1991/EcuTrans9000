package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DownloadVehiculosCsvTemplateUseCase {

  private final VehiculoApplicationService service;

  public String execute() {
    return service.downloadTemplate();
  }
}
