package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.domain.vehiculo.EstadoVehiculo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Contrato publico de backend para VehiculoJpaRepository.
 */
public interface VehiculoJpaRepository extends JpaRepository<VehiculoJpaEntity, UUID>, JpaSpecificationExecutor<VehiculoJpaEntity> {

  boolean existsByPlacaNorm(String placaNorm);

  boolean existsByPlacaNormAndIdNot(String placaNorm, UUID id);

  Optional<VehiculoJpaEntity> findByPlacaNorm(String placaNorm);

  @Modifying
  @Query("""
      update VehiculoJpaEntity v
         set v.estado = :inactiveState,
             v.updatedAt = :updatedAt
       where v.deleted = false
         and v.estado = :activeState
         and v.fechaCaducidadLicencia is not null
         and v.fechaCaducidadLicencia <= :today
      """)
  int deactivateExpiredLicenses(
      @Param("inactiveState") EstadoVehiculo inactiveState,
      @Param("activeState") EstadoVehiculo activeState,
      @Param("today") LocalDate today,
      @Param("updatedAt") LocalDateTime updatedAt);
}
