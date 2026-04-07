package com.ecutrans9000.backend.adapters.in.rest.descuentos;

import com.ecutrans9000.backend.adapters.in.rest.dto.descuento.DescuentoViajeImportResult;
import com.ecutrans9000.backend.adapters.in.rest.dto.descuento.DescuentoViajeListResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.descuento.DescuentoViajeResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.descuento.DescuentoViajeUpsertRequest;
import com.ecutrans9000.backend.application.descuento.DescuentoViajeExcelImportService;
import com.ecutrans9000.backend.application.descuento.DescuentoViajeService;
import com.ecutrans9000.backend.application.usecase.vehiculo.ImportMode;
import com.ecutrans9000.backend.service.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST del módulo descuentos de viajes.
 */
@RestController
@RequestMapping("/api/descuentos-viajes")
@Tag(name = "Descuentos viajes")
@PreAuthorize("@moduleAccessAuthorizationService.canAccess(authentication, 'DESCUENTOS_VIAJES')")
public class DescuentoViajeController {

  private final DescuentoViajeService descuentoViajeService;
  private final DescuentoViajeExcelImportService descuentoViajeExcelImportService;

  public DescuentoViajeController(
      DescuentoViajeService descuentoViajeService,
      DescuentoViajeExcelImportService descuentoViajeExcelImportService) {
    this.descuentoViajeService = descuentoViajeService;
    this.descuentoViajeExcelImportService = descuentoViajeExcelImportService;
  }

  @GetMapping
  @Operation(summary = "Listar descuentos de viajes")
  public ResponseEntity<DescuentoViajeListResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) UUID vehiculoId,
      @RequestParam(required = false) Boolean activo,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted) {
    Page<DescuentoViajeResponse> result = descuentoViajeService.list(page, size, q, vehiculoId, activo, includeDeleted);
    return ResponseEntity.ok(DescuentoViajeListResponse.builder()
        .content(result.getContent())
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalle de descuento de viaje")
  public ResponseEntity<DescuentoViajeResponse> getById(@PathVariable Long id) {
    return ResponseEntity.ok(descuentoViajeService.getById(id));
  }

  @PostMapping
  @Operation(summary = "Crear descuento de viaje")
  public ResponseEntity<DescuentoViajeResponse> create(@Valid @RequestBody DescuentoViajeUpsertRequest request, Authentication auth) {
    return ResponseEntity.ok(descuentoViajeService.create(request, auth.getName(), role(auth)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Editar descuento de viaje")
  public ResponseEntity<DescuentoViajeResponse> update(@PathVariable Long id, @Valid @RequestBody DescuentoViajeUpsertRequest request, Authentication auth) {
    return ResponseEntity.ok(descuentoViajeService.update(id, request, auth.getName(), role(auth)));
  }

  @PostMapping("/{id}/activate")
  @Operation(summary = "Activar descuento de viaje")
  public ResponseEntity<Map<String, String>> activate(@PathVariable Long id, Authentication auth) {
    descuentoViajeService.activate(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Descuento activado"));
  }

  @PostMapping("/{id}/deactivate")
  @Operation(summary = "Inactivar descuento de viaje")
  public ResponseEntity<Map<String, String>> deactivate(@PathVariable Long id, Authentication auth) {
    descuentoViajeService.deactivate(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Descuento inactivado"));
  }

  @PostMapping("/{id}/soft-delete")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Eliminar descuento logicamente")
  public ResponseEntity<Map<String, String>> softDelete(@PathVariable Long id, Authentication auth) {
    descuentoViajeService.softDelete(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Descuento eliminado logicamente"));
  }

  @PatchMapping("/{id}/restore")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Restaurar descuento")
  public ResponseEntity<DescuentoViajeResponse> restore(@PathVariable Long id, Authentication auth) {
    return ResponseEntity.ok(descuentoViajeService.restore(id, auth.getName(), role(auth)));
  }

  @GetMapping("/import/template")
  @Operation(summary = "Descargar plantilla Excel")
  public ResponseEntity<Resource> template() {
    return excelResponse(descuentoViajeExcelImportService.downloadTemplate(), "descuentos_viajes_template.xlsx");
  }

  @GetMapping("/import/template/example")
  @Operation(summary = "Descargar plantilla Excel con ejemplo")
  public ResponseEntity<Resource> templateExample() {
    return excelResponse(descuentoViajeExcelImportService.downloadExampleTemplate(), "descuentos_viajes_template_ejemplo.xlsx");
  }

  @PostMapping(path = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Previsualizar importacion Excel")
  public ResponseEntity<DescuentoViajeImportResult> preview(
      @RequestPart("file") MultipartFile file,
      @RequestParam(defaultValue = "INSERT_ONLY") String mode,
      @RequestParam(defaultValue = "true") boolean partialOk) {
    return ResponseEntity.ok(descuentoViajeExcelImportService.previewExcel(file, parseMode(mode), partialOk));
  }

  @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Importar descuentos desde Excel")
  public ResponseEntity<DescuentoViajeImportResult> importExcel(
      @RequestPart("file") MultipartFile file,
      @RequestParam(defaultValue = "INSERT_ONLY") String mode,
      @RequestParam(defaultValue = "true") boolean partialOk,
      Authentication auth) {
    return ResponseEntity.ok(descuentoViajeExcelImportService.importExcel(file, parseMode(mode), partialOk, auth.getName(), role(auth)));
  }

  private ImportMode parseMode(String mode) {
    try {
      return ImportMode.valueOf(mode.trim().toUpperCase());
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Modo de importacion invalido");
    }
  }

  private String role(Authentication auth) {
    return auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("UNKNOWN");
  }

  private ResponseEntity<Resource> excelResponse(byte[] content, String fileName) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
        .body(new ByteArrayResource(content));
  }
}
