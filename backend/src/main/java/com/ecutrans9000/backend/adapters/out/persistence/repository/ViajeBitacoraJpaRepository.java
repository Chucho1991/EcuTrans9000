package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.ViajeBitacoraJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ViajeBitacoraJpaRepository extends JpaRepository<ViajeBitacoraJpaEntity, UUID>, JpaSpecificationExecutor<ViajeBitacoraJpaEntity> {

  boolean existsByNumeroViaje(Integer numeroViaje);

  boolean existsByNumeroViajeAndIdNot(Integer numeroViaje, UUID id);

  Optional<ViajeBitacoraJpaEntity> findByNumeroViaje(Integer numeroViaje);

  Optional<ViajeBitacoraJpaEntity> findTopByOrderByNumeroViajeDesc();
}
