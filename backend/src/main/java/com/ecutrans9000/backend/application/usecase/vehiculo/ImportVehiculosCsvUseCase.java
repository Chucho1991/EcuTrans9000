package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Componente publico de backend para ImportVehiculosCsvUseCase.
 */
@Service
@RequiredArgsConstructor
public class ImportVehiculosCsvUseCase {

  private final VehiculoApplicationService service;

  public VehiculoImportResult execute(MultipartFile file, ImportMode mode, boolean partialOk, String actorUsername, String actorRole) {
    return service.importCsv(file, mode, partialOk, actorUsername, actorRole);
  }
}
