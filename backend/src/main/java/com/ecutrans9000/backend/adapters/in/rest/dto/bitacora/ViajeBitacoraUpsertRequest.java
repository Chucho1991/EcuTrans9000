package com.ecutrans9000.backend.adapters.in.rest.dto.bitacora;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para crear o actualizar un viaje de bitácora.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViajeBitacoraUpsertRequest {

  @NotNull
  private Integer numeroViaje;

  @NotNull
  private LocalDate fechaViaje;

  @NotNull
  private UUID vehiculoId;

  @NotNull
  private UUID clienteId;

  @NotBlank
  private String destino;

  private String detalleViaje;

  @NotNull
  @DecimalMin(value = "0", inclusive = true)
  private BigDecimal valor;

  @NotNull
  @DecimalMin(value = "0", inclusive = true)
  private BigDecimal costoChofer;

  @NotNull
  @DecimalMin(value = "0", inclusive = true)
  private BigDecimal estiba;

  @NotNull
  @DecimalMin(value = "0", inclusive = true)
  private BigDecimal anticipo;

  @NotNull
  private Boolean facturadoCliente;

  private String numeroFactura;

  private LocalDate fechaFactura;

  private LocalDate fechaPagoCliente;

  @NotNull
  private Boolean pagadoTransportista;

  private String observaciones;
}
