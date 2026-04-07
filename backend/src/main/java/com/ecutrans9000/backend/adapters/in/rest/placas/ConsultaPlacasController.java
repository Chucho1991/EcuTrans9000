package com.ecutrans9000.backend.adapters.in.rest.placas;

import com.ecutrans9000.backend.adapters.in.rest.dto.placas.ConsultaPlacaResponse;
import com.ecutrans9000.backend.application.placas.ConsultaPlacasService;
import com.ecutrans9000.backend.domain.bitacora.EstadoPagoChoferFiltro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Controlador REST para consultar y exportar reportes por placa.
 */
@RestController
@RequestMapping("/api/placas")
@Tag(name = "Consulta por placas")
@PreAuthorize("@moduleAccessAuthorizationService.canAccess(authentication, 'PLACAS')")
public class ConsultaPlacasController {

  private final ConsultaPlacasService consultaPlacasService;

  public ConsultaPlacasController(ConsultaPlacasService consultaPlacasService) {
    this.consultaPlacasService = consultaPlacasService;
  }

  @GetMapping("/consulta")
  @Operation(summary = "Consultar bitacora por placa")
  public ResponseEntity<ConsultaPlacaResponse> consultar(
      @RequestParam String placa,
      @RequestParam(required = false) String codigoViaje,
      @Parameter(description = "TODOS, PAGADOS o NO_PAGADOS")
      @RequestParam(required = false) String estadoPagoChofer,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
      @RequestParam(defaultValue = "true") boolean aplicarRetencion) {
    return ResponseEntity.ok(consultaPlacasService.consultar(
        placa,
        codigoViaje,
        parseEstadoPagoChofer(estadoPagoChofer),
        fechaDesde,
        fechaHasta,
        aplicarRetencion));
  }

  @GetMapping("/consulta/export")
  @Operation(summary = "Exportar consulta por placa a Excel")
  public ResponseEntity<byte[]> exportar(
      @RequestParam String placa,
      @RequestParam(required = false) String codigoViaje,
      @Parameter(description = "TODOS, PAGADOS o NO_PAGADOS")
      @RequestParam(required = false) String estadoPagoChofer,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
      @RequestParam(defaultValue = "true") boolean aplicarRetencion,
      @RequestParam(required = false) List<Long> descuentoIds,
      @RequestParam(required = false) List<UUID> viajeIds) {
    byte[] report = consultaPlacasService.exportExcel(
        placa,
        codigoViaje,
        parseEstadoPagoChofer(estadoPagoChofer),
        fechaDesde,
        fechaHasta,
        aplicarRetencion,
        descuentoIds,
        viajeIds);
    String normalized = placa == null || placa.isBlank() ? "consulta_placas" : placa.trim().toUpperCase();
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"placas_" + normalized + ".xlsx\"")
        .body(report);
  }

  private EstadoPagoChoferFiltro parseEstadoPagoChofer(String value) {
    try {
      return EstadoPagoChoferFiltro.fromValue(value);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "El filtro estadoPagoChofer debe ser TODOS, PAGADOS o NO_PAGADOS");
    }
  }
}
