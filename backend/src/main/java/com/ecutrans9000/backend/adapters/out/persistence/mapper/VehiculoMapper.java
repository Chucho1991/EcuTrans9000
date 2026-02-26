package com.ecutrans9000.backend.adapters.out.persistence.mapper;

import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;

public final class VehiculoMapper {

  private VehiculoMapper() {
  }

  public static Vehiculo toDomain(VehiculoJpaEntity entity) {
    return Vehiculo.builder()
        .id(entity.getId())
        .placa(entity.getPlaca())
        .placaNorm(entity.getPlacaNorm())
        .choferDefault(entity.getChoferDefault())
        .licencia(entity.getLicencia())
        .tipoDocumento(entity.getTipoDocumento())
        .documentoPersonal(entity.getDocumentoPersonal())
        .tonelajeCategoria(entity.getTonelajeCategoria())
        .m3(entity.getM3())
        .estado(entity.getEstado())
        .fotoPath(entity.getFotoPath())
        .docPath(entity.getDocPath())
        .licPath(entity.getLicPath())
        .deleted(entity.getDeleted())
        .deletedAt(entity.getDeletedAt())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}
