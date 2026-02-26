package com.ecutrans9000.backend.application.usecase.vehiculo;

import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PreviewVehiculosCsvUseCase {

  private final VehiculoApplicationService service;

  public VehiculoImportResult execute(MultipartFile file, ImportMode mode, boolean partialOk) {
    return service.previewCsv(file, mode, partialOk);
  }
}
