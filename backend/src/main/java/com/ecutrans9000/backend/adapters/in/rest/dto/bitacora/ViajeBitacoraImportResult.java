package com.ecutrans9000.backend.adapters.in.rest.dto.bitacora;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViajeBitacoraImportResult {
  private int totalRows;
  private int processed;
  private int inserted;
  private int updated;
  private int skipped;
  private int errorsCount;
  private List<ViajeBitacoraImportError> errors;
}
