package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehiculoJpaRepository extends JpaRepository<VehiculoJpaEntity, UUID>, JpaSpecificationExecutor<VehiculoJpaEntity> {

  boolean existsByPlacaNorm(String placaNorm);

  boolean existsByPlacaNormAndIdNot(String placaNorm, UUID id);

  Optional<VehiculoJpaEntity> findByPlacaNorm(String placaNorm);
}
