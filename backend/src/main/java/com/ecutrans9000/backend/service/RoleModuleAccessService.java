package com.ecutrans9000.backend.service;

import com.ecutrans9000.backend.adapters.in.rest.dto.settings.CurrentModuleAccessResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.settings.ModuleAccessItemResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.settings.ModuleAccessToggleRequest;
import com.ecutrans9000.backend.adapters.in.rest.dto.settings.RoleModuleAccessResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.settings.UpdateRoleModuleAccessRequest;
import com.ecutrans9000.backend.adapters.out.persistence.entity.RoleModuleAccessJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.RoleModuleAccessJpaRepository;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.domain.user.SystemModule;
import com.ecutrans9000.backend.domain.user.UserRole;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicacion que consulta, persiste y resuelve el acceso a modulos por rol.
 */
@Service
@RequiredArgsConstructor
public class RoleModuleAccessService {

  private final RoleModuleAccessJpaRepository repository;
  private final AuditService auditService;

  public List<RoleModuleAccessResponse> getEditableConfiguration() {
    List<RoleModuleAccessResponse> response = new ArrayList<>();
    for (UserRole role : editableRoles()) {
      response.add(RoleModuleAccessResponse.builder()
          .role(role.name())
          .modules(buildModuleItems(role))
          .build());
    }
    return response;
  }

  public RoleModuleAccessResponse updateRoleAccess(UserRole role, UpdateRoleModuleAccessRequest request, String actorUsername, String actorRole) {
    if (role == UserRole.SUPERADMINISTRADOR) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede modificar el acceso del rol SUPERADMINISTRADOR");
    }

    Map<SystemModule, Boolean> requestedState = new EnumMap<>(SystemModule.class);
    for (ModuleAccessToggleRequest moduleRequest : request.getModules()) {
      SystemModule module = parseModule(moduleRequest.getModuleKey());
      requestedState.put(module, moduleRequest.isEnabled());
    }

    LocalDateTime now = LocalDateTime.now();
    List<RoleModuleAccessJpaEntity> entitiesToSave = new ArrayList<>();
    for (SystemModule module : SystemModule.editableModules()) {
      RoleModuleAccessJpaEntity entity = repository.findById(new RoleModuleAccessJpaEntity.RoleModuleAccessId(role, module))
          .orElseGet(() -> RoleModuleAccessJpaEntity.builder()
              .roleName(role)
              .moduleKey(module)
              .createdAt(now)
              .build());
      entity.setEnabled(requestedState.getOrDefault(module, false));
      entity.setUpdatedAt(now);
      entitiesToSave.add(entity);
    }

    repository.saveAll(entitiesToSave);
    auditService.saveActionAudit(actorUsername, actorRole, "SETTINGS", ActionType.EDICION, role.name(), "role_module_access");

    return RoleModuleAccessResponse.builder()
        .role(role.name())
        .modules(buildModuleItems(role))
        .build();
  }

  public CurrentModuleAccessResponse getCurrentAccess(String authority) {
    UserRole role = parseAuthority(authority);
    List<String> allowedModules = getAllowedModules(role).stream()
        .map(Enum::name)
        .toList();
    return CurrentModuleAccessResponse.builder()
        .role(role.name())
        .allowedModules(allowedModules)
        .build();
  }

  public boolean hasAccess(UserRole role, SystemModule module) {
    if (role == UserRole.SUPERADMINISTRADOR) {
      return true;
    }
    return repository.findById(new RoleModuleAccessJpaEntity.RoleModuleAccessId(role, module))
        .map(RoleModuleAccessJpaEntity::getEnabled)
        .orElse(true);
  }

  public UserRole parseAuthority(String authority) {
    if (authority == null || authority.isBlank()) {
      throw new BusinessException(HttpStatus.FORBIDDEN, "Rol no autenticado");
    }
    String normalized = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
    try {
      return UserRole.valueOf(normalized.trim().toUpperCase());
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.FORBIDDEN, "Rol invalido");
    }
  }

  public UserRole parseRole(String role) {
    try {
      return UserRole.valueOf(role.trim().toUpperCase());
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Rol invalido");
    }
  }

  public SystemModule parseModule(String moduleKey) {
    try {
      return SystemModule.fromKey(moduleKey);
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Modulo invalido");
    }
  }

  private List<SystemModule> getAllowedModules(UserRole role) {
    return SystemModule.editableModules().stream()
        .filter(module -> hasAccess(role, module))
        .toList();
  }

  private List<ModuleAccessItemResponse> buildModuleItems(UserRole role) {
    return SystemModule.editableModules().stream()
        .sorted(Comparator.comparing(SystemModule::name))
        .map(module -> ModuleAccessItemResponse.builder()
            .moduleKey(module.name())
            .moduleName(module.getLabel())
            .enabled(hasAccess(role, module))
            .build())
        .toList();
  }

  private List<UserRole> editableRoles() {
    return List.of(UserRole.values()).stream()
        .filter(role -> role != UserRole.SUPERADMINISTRADOR)
        .sorted(Comparator.comparing(Enum::name))
        .toList();
  }
}
