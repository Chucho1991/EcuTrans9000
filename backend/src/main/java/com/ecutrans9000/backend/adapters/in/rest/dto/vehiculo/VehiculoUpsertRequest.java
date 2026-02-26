package com.ecutrans9000.backend.adapters.in.rest.dto.vehiculo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
public class VehiculoUpsertRequest {

  @NotBlank
  private String placa;

  @NotBlank
  private String choferDefault;

  private String licencia;

  @NotBlank
  private String tipoDocumento;

  @NotBlank
  private String documentoPersonal;

  @NotBlank
  private String tonelajeCategoria;

  @NotNull
  @DecimalMin(value = "0", inclusive = true)
  private BigDecimal m3;

  @NotBlank
  private String estado;
}
