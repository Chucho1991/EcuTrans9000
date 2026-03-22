package com.ecutrans9000.backend.adapters.out.persistence.mapper;

import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import com.ecutrans9000.backend.domain.cliente.ClienteEquivalencia;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import java.util.List;

/**
 * Mapper entre persistencia y dominio para clientes.
 */
public final class ClienteMapper {

  private ClienteMapper() {
  }

  public static Cliente toDomain(ClienteJpaEntity entity) {
    List<ClienteEquivalencia> equivalencias = entity.getEquivalencias() == null
        ? List.of()
        : entity.getEquivalencias().stream()
            .map(item -> ClienteEquivalencia.builder()
                .id(item.getId())
                .destino(item.getDestino())
                .valorDestino(item.getValorDestino())
                .costoChofer(item.getCostoChofer())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build())
            .toList();
    return Cliente.builder()
        .id(entity.getId())
        .tipoDocumento(entity.getTipoDocumento())
        .documento(entity.getDocumento())
        .documentoNorm(entity.getDocumentoNorm())
        .nombre(entity.getNombre())
        .nombreComercial(entity.getNombreComercial())
        .direccion(entity.getDireccion())
        .descripcion(entity.getDescripcion())
        .aplicaTablaEquivalencia(entity.getAplicaTablaEquivalencia())
        .equivalencias(equivalencias)
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
