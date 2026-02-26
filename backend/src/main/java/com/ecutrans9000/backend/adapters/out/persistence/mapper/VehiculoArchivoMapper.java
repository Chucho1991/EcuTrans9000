package com.ecutrans9000.backend.adapters.out.persistence.mapper;

import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoArchivoJpaEntity;
import com.ecutrans9000.backend.domain.vehiculo.VehiculoArchivo;

public final class VehiculoArchivoMapper {

  private VehiculoArchivoMapper() {
  }

  public static VehiculoArchivo toDomain(VehiculoArchivoJpaEntity entity) {
    return VehiculoArchivo.builder()
        .id(entity.getId())
        .vehiculoId(entity.getVehiculoId())
        .tipo(entity.getTipo())
        .fileName(entity.getFileName())
        .contentType(entity.getContentType())
        .contenido(entity.getContenido())
        .sizeBytes(entity.getSizeBytes())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}
