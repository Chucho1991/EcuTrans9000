package com.ecutrans9000.backend.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Componente publico de backend para DashboardController.
 */
@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard")
@PreAuthorize("@moduleAccessAuthorizationService.canAccess(authentication, 'DASHBOARD')")
public class DashboardController {

  @GetMapping
  @Operation(summary = "Metricas generales")
  public ResponseEntity<Map<String, Object>> metrics() {
    return ResponseEntity.ok(Map.of(
        "usuariosActivos", 1,
        "alertasHoy", 0,
        "viajesRegistrados", 0
    ));
  }
}
