package com.ecutrans9000.backend.adapters.in.rest.bitacora;

import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraImportResult;
import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraListResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraUpsertRequest;
import com.ecutrans9000.backend.application.bitacora.ViajeBitacoraExcelImportService;
import com.ecutrans9000.backend.application.bitacora.ViajeBitacoraService;
import com.ecutrans9000.backend.application.usecase.vehiculo.ImportMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * Controlador REST del módulo bitácora.
 */
@RestController
@RequestMapping("/api/bitacora/viajes")
@Tag(name = "Bitacora")
@PreAuthorize("@moduleAccessAuthorizationService.canAccess(authentication, 'BITACORA')")
public class ViajeBitacoraController {

  private final ViajeBitacoraService viajeBitacoraService;
  private final ViajeBitacoraExcelImportService viajeBitacoraExcelImportService;

  public ViajeBitacoraController(
      ViajeBitacoraService viajeBitacoraService,
      ViajeBitacoraExcelImportService viajeBitacoraExcelImportService) {
    this.viajeBitacoraService = viajeBitacoraService;
    this.viajeBitacoraExcelImportService = viajeBitacoraExcelImportService;
  }

  @GetMapping
  @Operation(summary = "Listar viajes de bitacora")
  public ResponseEntity<ViajeBitacoraListResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) UUID vehiculoId,
      @RequestParam(required = false) UUID clienteId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted) {
    Page<ViajeBitacoraResponse> result = viajeBitacoraService.list(page, size, q, vehiculoId, clienteId, fechaDesde, fechaHasta, includeDeleted);
    return ResponseEntity.ok(ViajeBitacoraListResponse.builder()
        .content(result.getContent())
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalle de viaje")
  public ResponseEntity<ViajeBitacoraResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(viajeBitacoraService.getById(id));
  }

  @GetMapping("/export")
  @Operation(summary = "Exportar viajes a Excel")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) UUID vehiculoId,
      @RequestParam(required = false) UUID clienteId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
    byte[] report = viajeBitacoraService.exportExcel(q, vehiculoId, clienteId, fechaDesde, fechaHasta);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bitacora_viajes.xlsx\"")
        .body(report);
  }

  @GetMapping("/import/template")
  @Operation(summary = "Descargar plantilla Excel")
  public ResponseEntity<byte[]> template() {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bitacora_template.xlsx\"")
        .body(viajeBitacoraExcelImportService.downloadTemplate());
  }

  @GetMapping("/import/template/example")
  @Operation(summary = "Descargar plantilla Excel con ejemplo")
  public ResponseEntity<byte[]> exampleTemplate() {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bitacora_template_ejemplo.xlsx\"")
        .body(viajeBitacoraExcelImportService.downloadExampleTemplate());
  }

  @PostMapping(path = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Previsualizar importacion Excel")
  public ResponseEntity<ViajeBitacoraImportResult> preview(
      @RequestPart("file") MultipartFile file,
      @RequestParam(defaultValue = "INSERT_ONLY") String mode,
      @RequestParam(defaultValue = "true") boolean partialOk) {
    return ResponseEntity.ok(viajeBitacoraExcelImportService.previewExcel(file, parseMode(mode), partialOk));
  }

  @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Importar viajes desde Excel")
  public ResponseEntity<ViajeBitacoraImportResult> importExcel(
      @RequestPart("file") MultipartFile file,
      @RequestParam(defaultValue = "INSERT_ONLY") String mode,
      @RequestParam(defaultValue = "true") boolean partialOk,
      org.springframework.security.core.Authentication auth) {
    return ResponseEntity.ok(viajeBitacoraExcelImportService.importExcel(file, parseMode(mode), partialOk, auth.getName(), role(auth)));
  }

  @PostMapping
  @Operation(summary = "Crear viaje")
  public ResponseEntity<ViajeBitacoraResponse> create(@Valid @RequestBody ViajeBitacoraUpsertRequest request) {
    return ResponseEntity.ok(viajeBitacoraService.create(request));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Editar viaje")
  public ResponseEntity<ViajeBitacoraResponse> update(@PathVariable UUID id, @Valid @RequestBody ViajeBitacoraUpsertRequest request) {
    return ResponseEntity.ok(viajeBitacoraService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Eliminar viaje logicamente")
  public ResponseEntity<Map<String, String>> softDelete(@PathVariable UUID id, org.springframework.security.core.Authentication auth) {
    viajeBitacoraService.softDelete(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Viaje eliminado logicamente"));
  }

  @PatchMapping("/{id}/restore")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Restaurar viaje")
  public ResponseEntity<ViajeBitacoraResponse> restore(@PathVariable UUID id, org.springframework.security.core.Authentication auth) {
    return ResponseEntity.ok(viajeBitacoraService.restore(id, auth.getName(), role(auth)));
  }

  private ImportMode parseMode(String mode) {
    try {
      return ImportMode.valueOf(mode.trim().toUpperCase());
    } catch (Exception ex) {
      throw new com.ecutrans9000.backend.service.BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST, "Modo de importacion invalido");
    }
  }

  private String role(org.springframework.security.core.Authentication auth) {
    return auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("UNKNOWN");
  }
}
