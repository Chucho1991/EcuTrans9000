package com.ecutrans9000.backend.application.descuento;

import com.ecutrans9000.backend.adapters.in.rest.dto.descuento.DescuentoViajeResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.descuento.DescuentoViajeUpsertRequest;
import com.ecutrans9000.backend.adapters.out.persistence.entity.DescuentoViajeJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.DescuentoViajeJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoJpaRepository;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.service.AuditService;
import com.ecutrans9000.backend.service.BusinessException;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para descuentos de viajes.
 */
@Service
public class DescuentoViajeService {

  private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

  private final DescuentoViajeJpaRepository descuentoRepository;
  private final VehiculoJpaRepository vehiculoRepository;
  private final AuditService auditService;

  public DescuentoViajeService(
      DescuentoViajeJpaRepository descuentoRepository,
      VehiculoJpaRepository vehiculoRepository,
      AuditService auditService) {
    this.descuentoRepository = descuentoRepository;
    this.vehiculoRepository = vehiculoRepository;
    this.auditService = auditService;
  }

  @Transactional(readOnly = true)
  public Page<DescuentoViajeResponse> list(int page, int size, String q, UUID vehiculoId, Boolean activo, Boolean includeDeleted) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), DEFAULT_SORT);
    return descuentoRepository.findAll(buildSpecification(q, vehiculoId, activo, includeDeleted), pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public DescuentoViajeResponse getById(Long id) {
    return toResponse(findEntity(id));
  }

  @Transactional
  public DescuentoViajeResponse create(DescuentoViajeUpsertRequest request, String actorUsername, String actorRole) {
    validate(request, null);
    LocalDateTime now = LocalDateTime.now();
    DescuentoViajeJpaEntity entity = DescuentoViajeJpaEntity.builder()
        .vehiculoId(request.getVehiculoId())
        .descripcionMotivo(clean(request.getDescripcionMotivo()))
        .descripcionMotivoNorm(normalizeText(request.getDescripcionMotivo()))
        .montoMotivo(request.getMontoMotivo())
        .activo(Boolean.TRUE.equals(request.getActivo()))
        .deleted(false)
        .deletedAt(null)
        .deletedBy(null)
        .createdAt(now)
        .updatedAt(now)
        .build();
    DescuentoViajeJpaEntity saved = descuentoRepository.save(entity);
    auditService.saveActionAudit(actorUsername, actorRole, "DESCUENTOS_VIAJES", ActionType.CREACION, String.valueOf(saved.getId()), "descuentos_viajes");
    return toResponse(saved);
  }

  @Transactional
  public DescuentoViajeResponse update(Long id, DescuentoViajeUpsertRequest request, String actorUsername, String actorRole) {
    DescuentoViajeJpaEntity entity = findEntity(id);
    if (Boolean.TRUE.equals(entity.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede editar un descuento eliminado logicamente");
    }
    validate(request, id);
    entity.setVehiculoId(request.getVehiculoId());
    entity.setDescripcionMotivo(clean(request.getDescripcionMotivo()));
    entity.setDescripcionMotivoNorm(normalizeText(request.getDescripcionMotivo()));
    entity.setMontoMotivo(request.getMontoMotivo());
    entity.setActivo(Boolean.TRUE.equals(request.getActivo()));
    entity.setUpdatedAt(LocalDateTime.now());
    DescuentoViajeJpaEntity saved = descuentoRepository.save(entity);
    auditService.saveActionAudit(actorUsername, actorRole, "DESCUENTOS_VIAJES", ActionType.EDICION, String.valueOf(saved.getId()), "descuentos_viajes");
    return toResponse(saved);
  }

  @Transactional
  public void activate(Long id, String actorUsername, String actorRole) {
    updateActiveState(id, true, actorUsername, actorRole);
  }

  @Transactional
  public void deactivate(Long id, String actorUsername, String actorRole) {
    updateActiveState(id, false, actorUsername, actorRole);
  }

  @Transactional
  public void softDelete(Long id, String actorUsername, String actorRole) {
    DescuentoViajeJpaEntity entity = findEntity(id);
    if (Boolean.TRUE.equals(entity.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El descuento ya esta eliminado logicamente");
    }
    entity.setDeleted(true);
    entity.setDeletedAt(LocalDateTime.now());
    entity.setDeletedBy(actorUsername);
    entity.setUpdatedAt(LocalDateTime.now());
    descuentoRepository.save(entity);
    auditService.saveActionAudit(actorUsername, actorRole, "DESCUENTOS_VIAJES", ActionType.ELIMINADO_LOGICO, String.valueOf(id), "descuentos_viajes");
  }

  @Transactional
  public DescuentoViajeResponse restore(Long id, String actorUsername, String actorRole) {
    DescuentoViajeJpaEntity entity = findEntity(id);
    if (!Boolean.TRUE.equals(entity.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El descuento no esta eliminado logicamente");
    }
    validateUnique(entity.getVehiculoId(), entity.getDescripcionMotivoNorm(), id);
    entity.setDeleted(false);
    entity.setDeletedAt(null);
    entity.setDeletedBy(null);
    entity.setUpdatedAt(LocalDateTime.now());
    DescuentoViajeJpaEntity saved = descuentoRepository.save(entity);
    auditService.saveActionAudit(actorUsername, actorRole, "DESCUENTOS_VIAJES", ActionType.RESTAURACION, String.valueOf(id), "descuentos_viajes");
    return toResponse(saved);
  }

  public void validateForCreate(DescuentoViajeUpsertRequest request) {
    validate(request, null);
  }

  private void updateActiveState(Long id, boolean active, String actorUsername, String actorRole) {
    DescuentoViajeJpaEntity entity = findEntity(id);
    if (Boolean.TRUE.equals(entity.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede cambiar el estado de un descuento eliminado logicamente");
    }
    if (Boolean.valueOf(active).equals(entity.getActivo())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, active ? "El descuento ya esta activo" : "El descuento ya esta inactivo");
    }
    entity.setActivo(active);
    entity.setUpdatedAt(LocalDateTime.now());
    descuentoRepository.save(entity);
    auditService.saveActionAudit(actorUsername, actorRole, "DESCUENTOS_VIAJES", ActionType.CAMBIO_ESTADO, String.valueOf(id), "descuentos_viajes");
  }

  private Specification<DescuentoViajeJpaEntity> buildSpecification(String q, UUID vehiculoId, Boolean activo, Boolean includeDeleted) {
    return (root, query, cb) -> {
      ArrayList<Predicate> predicates = new ArrayList<>();
      if (!Boolean.TRUE.equals(includeDeleted)) {
        predicates.add(cb.isFalse(root.get("deleted")));
      }
      if (vehiculoId != null) {
        predicates.add(cb.equal(root.get("vehiculoId"), vehiculoId));
      }
      if (activo != null) {
        predicates.add(cb.equal(root.get("activo"), activo));
      }
      if (q != null && !q.isBlank()) {
        String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
        predicates.add(cb.like(cb.lower(root.get("descripcionMotivo")), like));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  private void validate(DescuentoViajeUpsertRequest request, Long existingId) {
    UUID vehiculoId = request.getVehiculoId();
    if (vehiculoId == null) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Chofer es obligatorio");
    }
    VehiculoJpaEntity vehiculo = vehiculoRepository.findById(vehiculoId)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado"));
    if (Boolean.TRUE.equals(vehiculo.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede asociar un vehiculo eliminado");
    }
    String normalizedMotivo = normalizeText(request.getDescripcionMotivo());
    if (normalizedMotivo.isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Descripcion motivo es obligatoria");
    }
    validateUnique(vehiculoId, normalizedMotivo, existingId);
    BigDecimal monto = request.getMontoMotivo();
    if (monto == null || monto.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Monto motivo debe ser mayor o igual a 0");
    }
  }

  private void validateUnique(UUID vehiculoId, String descripcionMotivoNorm, Long existingId) {
    boolean exists = existingId == null
        ? descuentoRepository.existsByVehiculoIdAndDescripcionMotivoNorm(vehiculoId, descripcionMotivoNorm)
        : descuentoRepository.existsByVehiculoIdAndDescripcionMotivoNormAndIdNot(vehiculoId, descripcionMotivoNorm, existingId);
    if (exists) {
      throw new BusinessException(HttpStatus.CONFLICT, "Ya existe un descuento registrado para ese chofer y motivo");
    }
  }

  private DescuentoViajeJpaEntity findEntity(Long id) {
    return descuentoRepository.findById(id)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Descuento no encontrado"));
  }

  private DescuentoViajeResponse toResponse(DescuentoViajeJpaEntity entity) {
    VehiculoJpaEntity vehiculo = vehiculoRepository.findById(entity.getVehiculoId())
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehiculo relacionado no encontrado"));
    return DescuentoViajeResponse.builder()
        .id(entity.getId())
        .vehiculoId(entity.getVehiculoId())
        .vehiculoPlaca(vehiculo.getPlaca())
        .vehiculoChofer(vehiculo.getChoferDefault())
        .descripcionMotivo(entity.getDescripcionMotivo())
        .montoMotivo(entity.getMontoMotivo())
        .activo(entity.getActivo())
        .deleted(entity.getDeleted())
        .deletedAt(entity.getDeletedAt())
        .deletedBy(entity.getDeletedBy())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  private String clean(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  private String normalizeText(String value) {
    if (value == null) {
      return "";
    }
    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .replace('\n', ' ')
        .replace('\r', ' ')
        .replaceAll("\\s+", " ")
        .trim()
        .toLowerCase(Locale.ROOT);
  }
}
