package com.ecutrans9000.backend.adapters.out.persistence.postgres;

import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoArchivoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.mapper.VehiculoArchivoMapper;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoArchivoJpaRepository;
import com.ecutrans9000.backend.domain.vehiculo.TipoArchivoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.VehiculoArchivo;
import com.ecutrans9000.backend.ports.out.vehiculo.VehiculoArchivoRepositoryPort;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehiculoArchivoRepositoryAdapter implements VehiculoArchivoRepositoryPort {

  private final VehiculoArchivoJpaRepository repository;

  @Override
  public VehiculoArchivo save(VehiculoArchivo archivo) {
    LocalDateTime now = LocalDateTime.now();
    VehiculoArchivoJpaEntity entity = VehiculoArchivoJpaEntity.builder()
        .id(archivo.getId() == null ? UUID.randomUUID() : archivo.getId())
        .vehiculoId(archivo.getVehiculoId())
        .tipo(archivo.getTipo())
        .fileName(archivo.getFileName())
        .contentType(archivo.getContentType())
        .contenido(archivo.getContenido())
        .sizeBytes(archivo.getSizeBytes())
        .createdAt(archivo.getCreatedAt() == null ? now : archivo.getCreatedAt())
        .updatedAt(now)
        .build();

    return VehiculoArchivoMapper.toDomain(repository.save(entity));
  }

  @Override
  public Optional<VehiculoArchivo> findByVehiculoIdAndTipo(UUID vehiculoId, TipoArchivoVehiculo tipo) {
    return repository.findByVehiculoIdAndTipo(vehiculoId, tipo).map(VehiculoArchivoMapper::toDomain);
  }

  @Override
  public List<VehiculoArchivo> findByVehiculoIdsAndTipo(List<UUID> vehiculoIds, TipoArchivoVehiculo tipo) {
    if (vehiculoIds == null || vehiculoIds.isEmpty()) {
      return List.of();
    }
    return repository.findByVehiculoIdInAndTipo(vehiculoIds, tipo).stream()
        .map(VehiculoArchivoMapper::toDomain)
        .toList();
  }
}
