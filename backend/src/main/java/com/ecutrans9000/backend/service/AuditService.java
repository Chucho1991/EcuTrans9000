package com.ecutrans9000.backend.service;

import com.ecutrans9000.backend.adapters.out.persistence.entity.ActionAuditLogJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ApiAuditLogJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ActionAuditLogJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ApiAuditLogJpaRepository;
import com.ecutrans9000.backend.domain.audit.ActionType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Componente publico de backend para AuditService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

  private final ApiAuditLogJpaRepository apiAuditLogJpaRepository;
  private final ActionAuditLogJpaRepository actionAuditLogJpaRepository;

  public void saveApiAudit(String endpoint, String requestJson, String responseJson, String usuario, String rolUsuario) {
    try {
      apiAuditLogJpaRepository.save(ApiAuditLogJpaEntity.builder()
          .fechaHora(LocalDateTime.now())
          .endpoint(endpoint)
          .requestJson(sanitizeForTextColumn(requestJson))
          .responseJson(sanitizeForTextColumn(responseJson))
          .usuario(usuario)
          .rolUsuario(rolUsuario)
          .build());
    } catch (Exception ex) {
      log.warn("No se pudo guardar api_audit_log: {}", ex.getMessage());
    }
  }

  public void saveActionAudit(String usuario, String rolUsuario, String modulo, ActionType actionType, String idRegistro, String nombreTabla) {
    try {
      actionAuditLogJpaRepository.save(ActionAuditLogJpaEntity.builder()
          .fechaHora(LocalDateTime.now())
          .usuario(usuario)
          .rolUsuario(rolUsuario)
          .moduloAfectado(modulo)
          .tipoModificacion(actionType)
          .idRegistro(idRegistro)
          .nombreTabla(nombreTabla)
          .build());
    } catch (Exception ex) {
      log.warn("No se pudo guardar action_audit_log: {}", ex.getMessage());
    }
  }

  private String sanitizeForTextColumn(String value) {
    if (value == null) {
      return null;
    }
    // PostgreSQL TEXT no admite byte NUL.
    return value.replace("\u0000", "");
  }
}
