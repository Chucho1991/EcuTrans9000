package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoArchivoJpaEntity;
import com.ecutrans9000.backend.domain.vehiculo.TipoArchivoVehiculo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoArchivoJpaRepository extends JpaRepository<VehiculoArchivoJpaEntity, UUID> {
  Optional<VehiculoArchivoJpaEntity> findByVehiculoIdAndTipo(UUID vehiculoId, TipoArchivoVehiculo tipo);
}
