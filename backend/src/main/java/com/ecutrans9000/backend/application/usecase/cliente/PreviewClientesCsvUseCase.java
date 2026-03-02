package com.ecutrans9000.backend.application.usecase.cliente;

import com.ecutrans9000.backend.application.cliente.ClienteApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PreviewClientesCsvUseCase {

  private final ClienteApplicationService service;

  public ClienteImportResult execute(MultipartFile file, ImportMode mode, boolean partialOk) {
    return service.previewCsv(file, mode, partialOk);
  }
}
