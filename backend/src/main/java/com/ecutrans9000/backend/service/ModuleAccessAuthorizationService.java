package com.ecutrans9000.backend.service;

import com.ecutrans9000.backend.domain.user.SystemModule;
import com.ecutrans9000.backend.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Servicio auxiliar expuesto a SpEL para validar en @PreAuthorize el acceso dinamico por modulo.
 */
@Service("moduleAccessAuthorizationService")
@RequiredArgsConstructor
public class ModuleAccessAuthorizationService {

  private final RoleModuleAccessService roleModuleAccessService;

  public boolean canAccess(Authentication authentication, String moduleKey) {
    if (authentication == null || authentication.getAuthorities().isEmpty()) {
      return false;
    }
    UserRole role = roleModuleAccessService.parseAuthority(
        authentication.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse(null));
    SystemModule module = roleModuleAccessService.parseModule(moduleKey);
    return roleModuleAccessService.hasAccess(role, module);
  }
}
