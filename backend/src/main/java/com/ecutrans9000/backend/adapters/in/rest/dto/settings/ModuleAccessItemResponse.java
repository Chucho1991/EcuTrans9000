package com.ecutrans9000.backend.adapters.in.rest.dto.settings;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de salida que describe si un modulo especifico esta habilitado para un rol.
 */
@Getter
@Builder
public class ModuleAccessItemResponse {
  private String moduleKey;
  private String moduleName;
  private boolean enabled;
}
