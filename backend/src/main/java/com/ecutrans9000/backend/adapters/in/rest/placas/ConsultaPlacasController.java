package com.ecutrans9000.backend.adapters.in.rest.placas;

import com.ecutrans9000.backend.adapters.in.rest.dto.placas.ConsultaPlacaResponse;
import com.ecutrans9000.backend.application.placas.ConsultaPlacasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultar y exportar reportes por placa.
 */
@RestController
@RequestMapping("/api/placas")
@Tag(name = "Consulta por placas")
public class ConsultaPlacasController {

  private final ConsultaPlacasService consultaPlacasService;

  public ConsultaPlacasController(ConsultaPlacasService consultaPlacasService) {
    this.consultaPlacasService = consultaPlacasService;
  }

  @GetMapping("/consulta")
  @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','REGISTRADOR')")
  @Operation(summary = "Consultar bitacora por placa")
  public ResponseEntity<ConsultaPlacaResponse> consultar(
      @RequestParam(required = false) String placa,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
    return ResponseEntity.ok(consultaPlacasService.consultar(placa, fechaDesde, fechaHasta));
  }

  @GetMapping("/consulta/export")
  @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','REGISTRADOR')")
  @Operation(summary = "Exportar consulta por placa a Excel")
  public ResponseEntity<byte[]> exportar(
      @RequestParam(required = false) String placa,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
    byte[] report = consultaPlacasService.exportExcel(placa, fechaDesde, fechaHasta);
    String normalized = placa == null || placa.isBlank() ? "consulta_placas" : placa.trim().toUpperCase();
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"placas_" + normalized + ".xlsx\"")
        .body(report);
  }
}
