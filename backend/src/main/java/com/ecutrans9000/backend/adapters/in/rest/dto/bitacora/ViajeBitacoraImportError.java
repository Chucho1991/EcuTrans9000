package com.ecutrans9000.backend.adapters.in.rest.dto.bitacora;

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
/**
 * DTO de error por fila para la importación Excel de bitácora.
 */
public class ViajeBitacoraImportError {
  private int row;
  private String column;
  private String message;
}
