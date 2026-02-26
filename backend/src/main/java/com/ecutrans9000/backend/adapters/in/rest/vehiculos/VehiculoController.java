package com.ecutrans9000.backend.adapters.in.rest.vehiculos;

import com.ecutrans9000.backend.adapters.in.rest.dto.vehiculo.VehiculoListResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.vehiculo.VehiculoResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.vehiculo.VehiculoUpsertRequest;
import com.ecutrans9000.backend.application.usecase.vehiculo.ActivateVehiculoUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.CreateVehiculoUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.DeactivateVehiculoUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.DownloadVehiculosCsvTemplateUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.ImportMode;
import com.ecutrans9000.backend.application.usecase.vehiculo.ImportVehiculosCsvUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.PreviewVehiculosCsvUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.RestoreVehiculoUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.SearchVehiculosUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.SoftDeleteVehiculoUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.UpdateVehiculoUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.UploadVehiculoImageUseCase;
import com.ecutrans9000.backend.application.usecase.vehiculo.VehiculoImportResult;
import com.ecutrans9000.backend.application.usecase.vehiculo.VehiculoUpsertCommand;
import com.ecutrans9000.backend.application.vehiculo.VehiculoApplicationService;
import com.ecutrans9000.backend.domain.vehiculo.EstadoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.TipoDocumento;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import com.ecutrans9000.backend.service.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Adaptador REST para el catálogo de vehículos.
 */
@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
@Tag(name = "Vehiculos")
public class VehiculoController {

  private final CreateVehiculoUseCase createVehiculoUseCase;
  private final UpdateVehiculoUseCase updateVehiculoUseCase;
  private final SearchVehiculosUseCase searchVehiculosUseCase;
  private final ActivateVehiculoUseCase activateVehiculoUseCase;
  private final DeactivateVehiculoUseCase deactivateVehiculoUseCase;
  private final SoftDeleteVehiculoUseCase softDeleteVehiculoUseCase;
  private final RestoreVehiculoUseCase restoreVehiculoUseCase;
  private final UploadVehiculoImageUseCase uploadVehiculoImageUseCase;
  private final PreviewVehiculosCsvUseCase previewVehiculosCsvUseCase;
  private final ImportVehiculosCsvUseCase importVehiculosCsvUseCase;
  private final DownloadVehiculosCsvTemplateUseCase downloadVehiculosCsvTemplateUseCase;
  private final VehiculoApplicationService vehiculoApplicationService;

  @PostMapping
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Crear vehiculo")
  public ResponseEntity<VehiculoResponse> create(@Valid @org.springframework.web.bind.annotation.RequestBody VehiculoUpsertRequest request, Authentication auth) {
    Vehiculo saved = createVehiculoUseCase.execute(toCommand(request), auth.getName(), role(auth));
    return ResponseEntity.ok(toResponse(saved));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Editar vehiculo")
  public ResponseEntity<VehiculoResponse> update(@PathVariable UUID id, @Valid @org.springframework.web.bind.annotation.RequestBody VehiculoUpsertRequest request, Authentication auth) {
    Vehiculo saved = updateVehiculoUseCase.execute(id, toCommand(request), auth.getName(), role(auth));
    return ResponseEntity.ok(toResponse(saved));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','REGISTRADOR')")
  @Operation(summary = "Detalle de vehiculo")
  public ResponseEntity<VehiculoResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(toResponse(vehiculoApplicationService.getById(id)));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','REGISTRADOR')")
  @Operation(summary = "Buscar vehiculos")
  public ResponseEntity<VehiculoListResponse> search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String estado,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted) {
    Page<Vehiculo> result = searchVehiculosUseCase.execute(page, size, q, estado, includeDeleted);
    return ResponseEntity.ok(VehiculoListResponse.builder()
        .content(result.getContent().stream().map(this::toResponse).toList())
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .build());
  }

  @PostMapping("/{id}/activate")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Activar vehiculo")
  public ResponseEntity<Map<String, String>> activate(@PathVariable UUID id, Authentication auth) {
    activateVehiculoUseCase.execute(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Vehiculo activado"));
  }

  @PostMapping("/{id}/deactivate")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Inactivar vehiculo")
  public ResponseEntity<Map<String, String>> deactivate(@PathVariable UUID id, Authentication auth) {
    deactivateVehiculoUseCase.execute(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Vehiculo inactivado"));
  }

  @PostMapping("/{id}/soft-delete")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Eliminado logico")
  public ResponseEntity<Map<String, String>> softDelete(@PathVariable UUID id, Authentication auth) {
    softDeleteVehiculoUseCase.execute(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Vehiculo eliminado logicamente"));
  }

  @PostMapping("/{id}/restore")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Restaurar vehiculo")
  public ResponseEntity<Map<String, String>> restore(@PathVariable UUID id, Authentication auth) {
    restoreVehiculoUseCase.execute(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Vehiculo restaurado"));
  }

  @PostMapping(path = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Subir foto de vehiculo")
  public ResponseEntity<VehiculoResponse> uploadFoto(@PathVariable UUID id, @RequestPart("file") MultipartFile file, Authentication auth) {
    return ResponseEntity.ok(toResponse(uploadVehiculoImageUseCase.uploadFoto(id, file, auth.getName(), role(auth))));
  }

  @PostMapping(path = "/{id}/documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Subir imagen de documento")
  public ResponseEntity<VehiculoResponse> uploadDocumento(@PathVariable UUID id, @RequestPart("file") MultipartFile file, Authentication auth) {
    return ResponseEntity.ok(toResponse(uploadVehiculoImageUseCase.uploadDocumento(id, file, auth.getName(), role(auth))));
  }

  @PostMapping(path = "/{id}/licencia-img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Subir imagen de licencia")
  public ResponseEntity<VehiculoResponse> uploadLicencia(@PathVariable UUID id, @RequestPart("file") MultipartFile file, Authentication auth) {
    return ResponseEntity.ok(toResponse(uploadVehiculoImageUseCase.uploadLicencia(id, file, auth.getName(), role(auth))));
  }

  @GetMapping("/{id}/foto")
  @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','REGISTRADOR')")
  @Operation(summary = "Descargar foto")
  public ResponseEntity<Resource> getFoto(@PathVariable UUID id) {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(vehiculoApplicationService.getFoto(id));
  }

  @GetMapping("/{id}/documento")
  @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','REGISTRADOR')")
  @Operation(summary = "Descargar imagen de documento")
  public ResponseEntity<Resource> getDocumento(@PathVariable UUID id) {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(vehiculoApplicationService.getDocumento(id));
  }

  @GetMapping("/{id}/licencia-img")
  @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','REGISTRADOR')")
  @Operation(summary = "Descargar imagen de licencia")
  public ResponseEntity<Resource> getLicencia(@PathVariable UUID id) {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(vehiculoApplicationService.getLicencia(id));
  }

  @GetMapping("/import/template")
  @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','REGISTRADOR')")
  @Operation(summary = "Descargar plantilla CSV")
  public ResponseEntity<String> template() {
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .body(downloadVehiculosCsvTemplateUseCase.execute());
  }

  @PostMapping(path = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Previsualizar importacion CSV")
  public ResponseEntity<VehiculoImportResult> preview(
      @RequestPart("file") MultipartFile file,
      @RequestParam(defaultValue = "INSERT_ONLY") String mode,
      @RequestParam(defaultValue = "true") boolean partialOk) {
    return ResponseEntity.ok(previewVehiculosCsvUseCase.execute(file, parseMode(mode), partialOk));
  }

  @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Importar CSV")
  public ResponseEntity<VehiculoImportResult> importCsv(
      @RequestPart("file") MultipartFile file,
      @RequestParam(defaultValue = "INSERT_ONLY") String mode,
      @RequestParam(defaultValue = "true") boolean partialOk,
      Authentication auth) {
    return ResponseEntity.ok(importVehiculosCsvUseCase.execute(file, parseMode(mode), partialOk, auth.getName(), role(auth)));
  }

  private VehiculoUpsertCommand toCommand(VehiculoUpsertRequest request) {
    return new VehiculoUpsertCommand(
        request.getPlaca(),
        request.getChoferDefault(),
        request.getLicencia(),
        parseTipoDocumento(request.getTipoDocumento()),
        request.getDocumentoPersonal(),
        request.getTonelajeCategoria(),
        request.getM3(),
        parseEstado(request.getEstado())
    );
  }

  private VehiculoResponse toResponse(Vehiculo vehiculo) {
    return VehiculoResponse.builder()
        .id(vehiculo.getId())
        .placa(vehiculo.getPlaca())
        .placaNorm(vehiculo.getPlacaNorm())
        .choferDefault(vehiculo.getChoferDefault())
        .licencia(vehiculo.getLicencia())
        .tipoDocumento(vehiculo.getTipoDocumento().name())
        .documentoPersonal(vehiculo.getDocumentoPersonal())
        .tonelajeCategoria(vehiculo.getTonelajeCategoria())
        .m3(vehiculo.getM3())
        .estado(vehiculo.getEstado().name())
        .fotoPath(vehiculo.getFotoPath())
        .docPath(vehiculo.getDocPath())
        .licPath(vehiculo.getLicPath())
        .deleted(vehiculo.getDeleted())
        .deletedAt(vehiculo.getDeletedAt())
        .createdAt(vehiculo.getCreatedAt())
        .updatedAt(vehiculo.getUpdatedAt())
        .build();
  }

  private ImportMode parseMode(String mode) {
    try {
      return ImportMode.valueOf(mode.trim().toUpperCase());
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Modo de importacion invalido");
    }
  }

  private TipoDocumento parseTipoDocumento(String tipoDocumento) {
    try {
      return TipoDocumento.valueOf(tipoDocumento.trim().toUpperCase());
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "tipoDocumento invalido");
    }
  }

  private EstadoVehiculo parseEstado(String estado) {
    try {
      return EstadoVehiculo.valueOf(estado.trim().toUpperCase());
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "estado invalido");
    }
  }

  private String role(Authentication auth) {
    return auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("UNKNOWN");
  }
}
