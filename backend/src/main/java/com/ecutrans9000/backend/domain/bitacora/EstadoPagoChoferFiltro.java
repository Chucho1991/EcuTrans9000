package com.ecutrans9000.backend.domain.bitacora;

/**
 * Opciones de filtrado para el estado de pago al chofer en la consulta por placas.
 */
public enum EstadoPagoChoferFiltro {
  TODOS,
  PAGADOS,
  NO_PAGADOS;

  /**
   * Convierte el valor recibido por query string en una opcion de filtro valida.
   *
   * @param value valor textual del filtro
   * @return opcion normalizada; si no se envia valor, retorna {@link #TODOS}
   */
  public static EstadoPagoChoferFiltro fromValue(String value) {
    if (value == null || value.isBlank()) {
      return TODOS;
    }
    return EstadoPagoChoferFiltro.valueOf(value.trim().toUpperCase());
  }
}
