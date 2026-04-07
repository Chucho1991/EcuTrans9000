package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.DescuentoViajeJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositorio JPA para descuentos de viajes.
 */
public interface DescuentoViajeJpaRepository extends JpaRepository<DescuentoViajeJpaEntity, Long>, JpaSpecificationExecutor<DescuentoViajeJpaEntity> {

  boolean existsByVehiculoIdAndDescripcionMotivoNorm(UUID vehiculoId, String descripcionMotivoNorm);

  boolean existsByVehiculoIdAndDescripcionMotivoNormAndIdNot(UUID vehiculoId, String descripcionMotivoNorm, Long id);

  Optional<DescuentoViajeJpaEntity> findByVehiculoIdAndDescripcionMotivoNorm(UUID vehiculoId, String descripcionMotivoNorm);
}
