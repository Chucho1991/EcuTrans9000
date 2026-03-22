package com.ecutrans9000.backend.adapters.in.rest.dto.cliente;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para reemplazar la tabla de equivalencias de un cliente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEquivalenciasUpdateRequest {

  @NotNull
  @Valid
  private List<ClienteEquivalenciaRequest> equivalencias;
}
