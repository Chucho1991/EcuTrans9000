package com.ecutrans9000.backend.adapters.in.rest.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para crear o editar clientes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteUpsertRequest {

  @NotBlank
  private String tipoDocumento;

  @NotBlank
  private String documento;

  @NotBlank
  private String nombre;

  private String nombreComercial;

  private String direccion;

  private String descripcion;

  @NotNull
  private Boolean aplicaTablaEquivalencia;

  @NotNull
  private Boolean activo;
}
