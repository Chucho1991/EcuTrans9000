package com.ecutrans9000.backend.domain.vehiculo;

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
 * Entidad de dominio para el catálogo de vehículos.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehiculo {
  private UUID id;
  private String placa;
  private String placaNorm;
  private String choferDefault;
  private String licencia;
  private LocalDate fechaCaducidadLicencia;
  private TipoDocumento tipoDocumento;
  private String documentoPersonal;
  private String cuentaBancaria;
  private String tonelajeCategoria;
  private BigDecimal m3;
  private EstadoVehiculo estado;
  private String fotoPath;
  private String docPath;
  private String licPath;
  private Boolean deleted;
  private LocalDateTime deletedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Normaliza placa a formato de comparación (trim + uppercase).
   */
  public static String normalizePlaca(String placa) {
    if (placa == null) {
      return "";
    }
    return placa.trim().toUpperCase();
  }
}
