package com.ecutrans9000.backend.adapters.in.rest.dto.settings;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para activar o desactivar un modulo dentro de la configuracion de un rol.
 */
@Getter
@Setter
public class ModuleAccessToggleRequest {

  @NotBlank
  private String moduleKey;

  private boolean enabled;
}
