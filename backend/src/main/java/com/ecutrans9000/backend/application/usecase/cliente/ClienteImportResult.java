package com.ecutrans9000.backend.application.usecase.cliente;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
/**
 * Resultado consolidado de una importación masiva de clientes.
 */
public class ClienteImportResult {
  private int totalRows;
  private int processed;
  private int inserted;
  private int updated;
  private int skipped;
  private int errorsCount;
  @Builder.Default
  private List<ClienteImportError> errors = new ArrayList<>();
}
