package com.ecutrans9000.backend.domain.user;

import java.util.Arrays;
import java.util.List;

/**
 * Catalogo de modulos funcionales cuyo acceso puede habilitarse o deshabilitarse por rol.
 */
public enum SystemModule {
  VEHICULOS("Vehiculos"),
  CLIENTES("Clientes"),
  BITACORA("Bitacora"),
  PLACAS("Consulta por placas");

  private final String label;

  SystemModule(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static SystemModule fromKey(String key) {
    return SystemModule.valueOf(key.trim().toUpperCase());
  }

  public static List<SystemModule> editableModules() {
    return Arrays.asList(values());
  }
}
