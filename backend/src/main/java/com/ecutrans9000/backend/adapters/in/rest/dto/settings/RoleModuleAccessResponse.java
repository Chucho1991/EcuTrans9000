package com.ecutrans9000.backend.adapters.in.rest.dto.settings;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO de salida con la configuracion completa de modulos para un rol.
 */
@Getter
@Builder
public class RoleModuleAccessResponse {
  private String role;
  private List<ModuleAccessItemResponse> modules;
}
