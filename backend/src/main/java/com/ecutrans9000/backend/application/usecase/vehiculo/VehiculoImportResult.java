package com.ecutrans9000.backend.application.usecase.vehiculo;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VehiculoImportResult {
  private int totalRows;
  private int processed;
  private int inserted;
  private int updated;
  private int skipped;
  private int errorsCount;
  @Builder.Default
  private List<VehiculoImportError> errors = new ArrayList<>();
}
