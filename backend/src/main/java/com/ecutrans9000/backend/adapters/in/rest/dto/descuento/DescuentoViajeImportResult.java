package com.ecutrans9000.backend.adapters.in.rest.dto.descuento;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO resumen de importación Excel de descuentos de viajes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoViajeImportResult {
  private int totalRows;
  private int processed;
  private int inserted;
  private int updated;
  private int skipped;
  private int errorsCount;

  @Builder.Default
  private List<DescuentoViajeImportError> errors = new ArrayList<>();
}
