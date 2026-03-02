package com.ecutrans9000.backend.adapters.out.persistence.postgres;

import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.mapper.ClienteMapper;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ClienteJpaRepository;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import com.ecutrans9000.backend.ports.out.cliente.ClienteRepositoryPort;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

  private final ClienteJpaRepository clienteJpaRepository;

  @Override
  public Cliente save(Cliente cliente) {
    LocalDateTime now = LocalDateTime.now();
    ClienteJpaEntity entity = ClienteJpaEntity.builder()
        .id(cliente.getId())
        .tipoDocumento(cliente.getTipoDocumento())
        .documento(cliente.getDocumento())
        .documentoNorm(cliente.getDocumentoNorm())
        .nombre(cliente.getNombre())
        .nombreComercial(cliente.getNombreComercial())
        .descripcion(cliente.getDescripcion())
        .logoFileName(cliente.getLogoFileName())
        .logoContentType(cliente.getLogoContentType())
        .logoContenido(cliente.getLogoContenido())
        .activo(Boolean.TRUE.equals(cliente.getActivo()))
        .deleted(Boolean.TRUE.equals(cliente.getDeleted()))
        .deletedAt(cliente.getDeletedAt())
        .deletedBy(cliente.getDeletedBy())
        .createdAt(cliente.getCreatedAt() == null ? now : cliente.getCreatedAt())
        .updatedAt(now)
        .build();
    return ClienteMapper.toDomain(clienteJpaRepository.save(entity));
  }

  @Override
  public Optional<Cliente> findById(UUID id) {
    return clienteJpaRepository.findById(id).map(ClienteMapper::toDomain);
  }

  @Override
  public Optional<Cliente> findByDocumentoNorm(String documentoNorm) {
    return clienteJpaRepository.findByDocumentoNorm(documentoNorm).map(ClienteMapper::toDomain);
  }

  @Override
  public boolean existsByDocumentoNorm(String documentoNorm) {
    return clienteJpaRepository.existsByDocumentoNorm(documentoNorm);
  }

  @Override
  public boolean existsByDocumentoNormAndIdNot(String documentoNorm, UUID id) {
    return clienteJpaRepository.existsByDocumentoNormAndIdNot(documentoNorm, id);
  }

  @Override
  public Page<Cliente> search(int page, int size, String q, Boolean includeDeleted) {
    Page<ClienteJpaEntity> results = clienteJpaRepository.findAll((root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (q != null && !q.isBlank()) {
        String like = "%" + q.trim().toLowerCase() + "%";
        predicates.add(cb.or(
            cb.like(cb.lower(root.get("documentoNorm")), like),
            cb.like(cb.lower(root.get("nombre")), like),
            cb.like(cb.lower(root.get("nombreComercial")), like)
        ));
      }
      if (includeDeleted == null || !includeDeleted) {
        predicates.add(cb.isFalse(root.get("deleted")));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    }, PageRequest.of(Math.max(page, 0), size));

    List<Cliente> content = results.getContent().stream().map(ClienteMapper::toDomain).toList();
    return new PageImpl<>(content, results.getPageable(), results.getTotalElements());
  }

  @Override
  public void deleteById(UUID id) {
    clienteJpaRepository.deleteById(id);
  }
}
