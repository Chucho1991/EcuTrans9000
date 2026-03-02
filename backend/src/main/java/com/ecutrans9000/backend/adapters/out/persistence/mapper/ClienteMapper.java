package com.ecutrans9000.backend.adapters.out.persistence.mapper;

import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import com.ecutrans9000.backend.domain.cliente.Cliente;

public final class ClienteMapper {

  private ClienteMapper() {
  }

  public static Cliente toDomain(ClienteJpaEntity entity) {
    return Cliente.builder()
        .id(entity.getId())
        .tipoDocumento(entity.getTipoDocumento())
        .documento(entity.getDocumento())
        .documentoNorm(entity.getDocumentoNorm())
        .nombre(entity.getNombre())
        .nombreComercial(entity.getNombreComercial())
        .descripcion(entity.getDescripcion())
        .logoFileName(entity.getLogoFileName())
        .logoContentType(entity.getLogoContentType())
        .logoContenido(entity.getLogoContenido())
        .activo(entity.getActivo())
        .deleted(entity.getDeleted())
        .deletedAt(entity.getDeletedAt())
        .deletedBy(entity.getDeletedBy())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}
