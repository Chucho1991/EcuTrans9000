package com.ecutrans9000.backend.adapters.in.rest.dto.settings;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO de salida con los modulos permitidos para el usuario autenticado actual.
 */
@Getter
@Builder
public class CurrentModuleAccessResponse {
  private String role;
  private List<String> allowedModules;
}
