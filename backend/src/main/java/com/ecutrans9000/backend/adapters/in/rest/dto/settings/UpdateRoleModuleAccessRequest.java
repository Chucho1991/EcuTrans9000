package com.ecutrans9000.backend.adapters.in.rest.dto.settings;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada que contiene la lista completa de modulos a persistir para un rol.
 */
@Getter
@Setter
public class UpdateRoleModuleAccessRequest {

  @Valid
  @NotEmpty
  private List<ModuleAccessToggleRequest> modules;
}
