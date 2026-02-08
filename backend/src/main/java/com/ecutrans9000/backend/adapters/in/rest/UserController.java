package com.ecutrans9000.backend.adapters.in.rest;

import com.ecutrans9000.backend.adapters.in.rest.dto.user.CreateUserRequest;
import com.ecutrans9000.backend.adapters.in.rest.dto.user.UpdateMyProfileRequest;
import com.ecutrans9000.backend.adapters.in.rest.dto.user.UpdateUserRequest;
import com.ecutrans9000.backend.adapters.in.rest.dto.user.UserListResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.user.UserResponse;
import com.ecutrans9000.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Crear usuario")
  public ResponseEntity<UserResponse> create(
      @Valid @RequestBody CreateUserRequest request,
      Authentication auth) {
    return ResponseEntity.ok(userService.createUser(request, auth.getName(), role(auth)));
  }

  @GetMapping
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Listar usuarios")
  public ResponseEntity<UserListResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String rol,
      @RequestParam(required = false) Boolean activo,
      @RequestParam(required = false) Boolean deleted) {
    return ResponseEntity.ok(userService.listUsers(page, size, rol, activo, deleted));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Detalle de usuario")
  public ResponseEntity<UserResponse> detail(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Editar usuario")
  public ResponseEntity<UserResponse> update(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest request,
      Authentication auth) {
    return ResponseEntity.ok(userService.updateUser(id, request, auth.getName(), role(auth)));
  }

  @PostMapping("/{id}/soft-delete")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Eliminado logico")
  public ResponseEntity<Map<String, String>> softDelete(@PathVariable UUID id, Authentication auth) {
    userService.softDelete(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Usuario eliminado logicamente"));
  }

  @PostMapping("/{id}/restore")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Restaurar usuario")
  public ResponseEntity<Map<String, String>> restore(@PathVariable UUID id, Authentication auth) {
    userService.restore(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Usuario restaurado"));
  }

  @PostMapping("/{id}/activate")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Activar usuario")
  public ResponseEntity<Map<String, String>> activate(@PathVariable UUID id, Authentication auth) {
    userService.activate(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Usuario activado"));
  }

  @PostMapping("/{id}/deactivate")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Inhabilitar usuario")
  public ResponseEntity<Map<String, String>> deactivate(@PathVariable UUID id, Authentication auth) {
    userService.deactivate(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Usuario inhabilitado"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Eliminacion fisica (uso excepcional)")
  public ResponseEntity<Map<String, String>> hardDelete(@PathVariable UUID id, Authentication auth) {
    userService.hardDelete(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Usuario eliminado fisicamente"));
  }

  @GetMapping("/me")
  @Operation(summary = "Perfil propio")
  public ResponseEntity<UserResponse> me(Authentication auth) {
    return ResponseEntity.ok(userService.getMe(auth.getName()));
  }

  @PutMapping("/me")
  @Operation(summary = "Actualizar perfil propio")
  public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UpdateMyProfileRequest request, Authentication auth) {
    return ResponseEntity.ok(userService.updateMe(auth.getName(), request));
  }

  private String role(Authentication auth) {
    return auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("UNKNOWN");
  }
}
