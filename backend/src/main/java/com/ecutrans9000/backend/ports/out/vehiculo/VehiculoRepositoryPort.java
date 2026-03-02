package com.ecutrans9000.backend.ports.out.vehiculo;

import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;

/**
 * Puerto de salida para persistencia y búsqueda del agregado {@code Vehiculo}.
 */
public interface VehiculoRepositoryPort {

  /**
   * Guarda o actualiza un vehículo.
   */
  Vehiculo save(Vehiculo vehiculo);

  /**
   * Busca un vehículo por identificador.
   */
  Optional<Vehiculo> findById(UUID id);

  /**
   * Busca un vehículo por placa normalizada.
   */
  Optional<Vehiculo> findByPlacaNorm(String placaNorm);

  /**
   * Verifica existencia por placa normalizada.
   */
  boolean existsByPlacaNorm(String placaNorm);

  /**
   * Verifica existencia por placa normalizada excluyendo un id.
   */
  boolean existsByPlacaNormAndIdNot(String placaNorm, UUID id);

  /**
   * Búsqueda paginada por texto, estado y bandera de eliminados.
   */
  Page<Vehiculo> search(int page, int size, String q, String estado, Boolean includeDeleted);

  /**
   * Inactiva vehículos activos con licencia vencida.
   */
  int deactivateExpiredLicenses(LocalDate today);
}
