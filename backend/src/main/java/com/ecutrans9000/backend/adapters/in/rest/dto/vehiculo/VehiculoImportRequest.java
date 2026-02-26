package com.ecutrans9000.backend.adapters.in.rest.dto.vehiculo;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Parametriza una importación CSV de vehículos.
 */
public class VehiculoImportRequest {

  @NotBlank
  private String mode;

  private Boolean partialOk;
}
