package com.ecutrans9000.backend.adapters.in.rest.settings;

import com.ecutrans9000.backend.adapters.in.rest.dto.settings.CurrentModuleAccessResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.settings.RoleModuleAccessResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.settings.UpdateRoleModuleAccessRequest;
import com.ecutrans9000.backend.service.RoleModuleAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del modulo de configuracion de acceso a modulos por rol.
 */
@RestController
@RequestMapping("/settings/module-access")
@RequiredArgsConstructor
@Tag(name = "Configuracion de modulos")
public class ModuleAccessSettingsController {

  private final RoleModuleAccessService roleModuleAccessService;

  @GetMapping
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Listar accesos configurables por rol")
  public ResponseEntity<List<RoleModuleAccessResponse>> list() {
    return ResponseEntity.ok(roleModuleAccessService.getEditableConfiguration());
  }

  @PutMapping("/{role}")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Actualizar accesos de un rol")
  public ResponseEntity<RoleModuleAccessResponse> update(
      @PathVariable String role,
      @Valid @RequestBody UpdateRoleModuleAccessRequest request,
      Authentication auth) {
    return ResponseEntity.ok(roleModuleAccessService.updateRoleAccess(
        roleModuleAccessService.parseRole(role),
        request,
        auth.getName(),
        currentAuthority(auth)));
  }

  @GetMapping("/me")
  @Operation(summary = "Listar modulos permitidos del usuario autenticado")
  public ResponseEntity<CurrentModuleAccessResponse> current(Authentication auth) {
    return ResponseEntity.ok(roleModuleAccessService.getCurrentAccess(currentAuthority(auth)));
  }

  private String currentAuthority(Authentication auth) {
    return auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("UNKNOWN");
  }
}
