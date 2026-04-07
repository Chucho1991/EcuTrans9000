package com.ecutrans9000.backend.application.usecase.cliente;

import com.ecutrans9000.backend.application.cliente.ClienteApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
/**
 * Caso de uso para previsualizar la importación Excel de clientes.
 */
public class PreviewClientesCsvUseCase {

  private final ClienteApplicationService service;

  public ClienteImportResult execute(MultipartFile file, ImportMode mode, boolean partialOk) {
    return service.previewExcel(file, mode, partialOk);
  }
}
