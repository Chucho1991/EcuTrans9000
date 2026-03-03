package com.ecutrans9000.backend.adapters.in.rest;

import com.ecutrans9000.backend.adapters.in.rest.dto.dashboard.DashboardResponse;
import com.ecutrans9000.backend.application.dashboard.DashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

  private final DashboardQueryService dashboardQueryService;

  public DashboardController(DashboardQueryService dashboardQueryService) {
    this.dashboardQueryService = dashboardQueryService;
  }

  @GetMapping
  @Operation(summary = "Metricas generales")
  public ResponseEntity<DashboardResponse> metrics() {
    return ResponseEntity.ok(dashboardQueryService.getDashboard());
  }
}
