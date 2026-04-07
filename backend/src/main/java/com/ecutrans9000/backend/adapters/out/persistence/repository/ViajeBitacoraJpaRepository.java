package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.ViajeBitacoraJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio JPA para viajes registrados en bitácora.
 */
public interface ViajeBitacoraJpaRepository extends JpaRepository<ViajeBitacoraJpaEntity, UUID>, JpaSpecificationExecutor<ViajeBitacoraJpaEntity> {

  boolean existsByNumeroViaje(Integer numeroViaje);

  boolean existsByNumeroViajeAndIdNot(Integer numeroViaje, UUID id);

  Optional<ViajeBitacoraJpaEntity> findByNumeroViaje(Integer numeroViaje);

  Optional<ViajeBitacoraJpaEntity> findTopByOrderByNumeroViajeDesc();

  @Query("""
      select count(v) > 0
      from ViajeBitacoraJpaEntity v
      where v.deleted = false
        and v.clienteId = :clienteId
        and (
          v.facturadoCliente = false
          or v.fechaFactura is null
          or v.fechaPagoCliente is null
        )
      """)
  boolean existsClienteWithPendingStatuses(@Param("clienteId") UUID clienteId);

  @Query("""
      select count(v) > 0
      from ViajeBitacoraJpaEntity v
      where v.deleted = false
        and v.vehiculoId = :vehiculoId
        and v.pagadoTransportista = false
      """)
  boolean existsVehiculoWithPendingStatuses(@Param("vehiculoId") UUID vehiculoId);
}
