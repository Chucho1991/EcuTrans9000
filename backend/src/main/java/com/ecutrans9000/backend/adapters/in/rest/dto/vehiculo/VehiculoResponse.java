package com.ecutrans9000.backend.adapters.in.rest.dto.vehiculo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Componente publico de backend para VehiculoResponse.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoResponse {
  private UUID id;
  private String placa;
  private String placaNorm;
  private String choferDefault;
  private String licencia;
  private LocalDate fechaCaducidadLicencia;
  private String tipoDocumento;
  private String documentoPersonal;
  private String tonelajeCategoria;
  private BigDecimal m3;
  private String estado;
  private String fotoPath;
  private String docPath;
  private String licPath;
  private Boolean deleted;
  private LocalDateTime deletedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
