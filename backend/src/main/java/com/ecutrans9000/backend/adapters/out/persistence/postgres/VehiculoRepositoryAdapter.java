package com.ecutrans9000.backend.adapters.out.persistence.postgres;

import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.mapper.VehiculoMapper;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoJpaRepository;
import com.ecutrans9000.backend.domain.vehiculo.EstadoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import com.ecutrans9000.backend.ports.out.vehiculo.VehiculoRepositoryPort;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
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

/**
 * Componente publico de backend para VehiculoRepositoryAdapter.
 */
@Component
@RequiredArgsConstructor
public class VehiculoRepositoryAdapter implements VehiculoRepositoryPort {

  private final VehiculoJpaRepository vehiculoJpaRepository;

  @Override
  public Vehiculo save(Vehiculo vehiculo) {
    LocalDateTime now = LocalDateTime.now();
    VehiculoJpaEntity entity = VehiculoJpaEntity.builder()
        .id(vehiculo.getId())
        .placa(vehiculo.getPlaca())
        .placaNorm(vehiculo.getPlacaNorm())
        .choferDefault(vehiculo.getChoferDefault())
        .licencia(vehiculo.getLicencia())
        .fechaCaducidadLicencia(vehiculo.getFechaCaducidadLicencia())
        .tipoDocumento(vehiculo.getTipoDocumento())
        .documentoPersonal(vehiculo.getDocumentoPersonal())
        .cuentaBancaria(vehiculo.getCuentaBancaria())
        .tonelajeCategoria(vehiculo.getTonelajeCategoria())
        .m3(vehiculo.getM3())
        .estado(vehiculo.getEstado())
        .fotoPath(vehiculo.getFotoPath())
        .docPath(vehiculo.getDocPath())
        .licPath(vehiculo.getLicPath())
        .deleted(Boolean.TRUE.equals(vehiculo.getDeleted()))
        .deletedAt(vehiculo.getDeletedAt())
        .createdAt(vehiculo.getCreatedAt() == null ? now : vehiculo.getCreatedAt())
        .updatedAt(now)
        .build();
    return VehiculoMapper.toDomain(vehiculoJpaRepository.save(entity));
  }

  @Override
  public Optional<Vehiculo> findById(UUID id) {
    return vehiculoJpaRepository.findById(id).map(VehiculoMapper::toDomain);
  }

  @Override
  public Optional<Vehiculo> findByPlacaNorm(String placaNorm) {
    return vehiculoJpaRepository.findByPlacaNorm(placaNorm).map(VehiculoMapper::toDomain);
  }

  @Override
  public boolean existsByPlacaNorm(String placaNorm) {
    return vehiculoJpaRepository.existsByPlacaNorm(placaNorm);
  }

  @Override
  public boolean existsByPlacaNormAndIdNot(String placaNorm, UUID id) {
    return vehiculoJpaRepository.existsByPlacaNormAndIdNot(placaNorm, id);
  }

  @Override
  public Page<Vehiculo> search(int page, int size, String q, String estado, Boolean includeDeleted) {
    Page<VehiculoJpaEntity> results = vehiculoJpaRepository.findAll((root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (q != null && !q.isBlank()) {
        String like = "%" + q.trim().toLowerCase() + "%";
        predicates.add(cb.or(
            cb.like(cb.lower(root.get("placaNorm")), like),
            cb.like(cb.lower(root.get("choferDefault")), like)
        ));
      }
      if (estado != null && !estado.isBlank()) {
        predicates.add(cb.equal(root.get("estado"), EstadoVehiculo.valueOf(estado.trim().toUpperCase())));
      }
      if (includeDeleted == null || !includeDeleted) {
        predicates.add(cb.isFalse(root.get("deleted")));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    }, PageRequest.of(Math.max(page, 0), size));

    List<Vehiculo> content = results.getContent().stream().map(VehiculoMapper::toDomain).toList();
    return new PageImpl<>(content, results.getPageable(), results.getTotalElements());
  }

  @Override
  public int deactivateExpiredLicenses(LocalDate today) {
    return vehiculoJpaRepository.deactivateExpiredLicenses(EstadoVehiculo.INACTIVO, EstadoVehiculo.ACTIVO, today, LocalDateTime.now());
  }
}
