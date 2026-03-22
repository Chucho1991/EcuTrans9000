package com.ecutrans9000.backend.adapters.in.rest.clientes;

import com.ecutrans9000.backend.adapters.in.rest.dto.cliente.ClienteEquivalenciaRequest;
import com.ecutrans9000.backend.adapters.in.rest.dto.cliente.ClienteEquivalenciaResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.cliente.ClienteEquivalenciasUpdateRequest;
import com.ecutrans9000.backend.adapters.in.rest.dto.cliente.ClienteListResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.cliente.ClienteResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.cliente.ClienteUpsertRequest;
import com.ecutrans9000.backend.application.cliente.ClienteApplicationService;
import com.ecutrans9000.backend.application.usecase.cliente.ClienteEquivalenciaUpsertCommand;
import com.ecutrans9000.backend.application.usecase.cliente.ClienteImportResult;
import com.ecutrans9000.backend.application.usecase.cliente.ClienteUpsertCommand;
import com.ecutrans9000.backend.application.usecase.cliente.CreateClienteUseCase;
import com.ecutrans9000.backend.application.usecase.cliente.DownloadClientesCsvTemplateUseCase;
import com.ecutrans9000.backend.application.usecase.cliente.ForceDeleteClienteUseCase;
import com.ecutrans9000.backend.application.usecase.cliente.GetClienteByIdUseCase;
import com.ecutrans9000.backend.application.usecase.cliente.ImportClientesCsvUseCase;
import com.ecutrans9000.backend.application.usecase.cliente.ImportMode;
import com.ecutrans9000.backend.application.usecase.cliente.ListClientesUseCase;
import com.ecutrans9000.backend.application.usecase.cliente.PreviewClientesCsvUseCase;
import com.ecutrans9000.backend.application.usecase.cliente.RestoreClienteUseCase;
import com.ecutrans9000.backend.application.usecase.cliente.SoftDeleteClienteUseCase;
import com.ecutrans9000.backend.application.usecase.cliente.ToggleActivoClienteUseCase;
import com.ecutrans9000.backend.application.usecase.cliente.UpdateClienteUseCase;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import com.ecutrans9000.backend.domain.cliente.ClienteEquivalencia;
import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;
import com.ecutrans9000.backend.service.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * Controlador REST del módulo clientes.
 */
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes")
@PreAuthorize("@moduleAccessAuthorizationService.canAccess(authentication, 'CLIENTES')")
public class ClienteController {

  private final CreateClienteUseCase createClienteUseCase;
  private final UpdateClienteUseCase updateClienteUseCase;
  private final ListClientesUseCase listClientesUseCase;
  private final GetClienteByIdUseCase getClienteByIdUseCase;
  private final ToggleActivoClienteUseCase toggleActivoClienteUseCase;
  private final SoftDeleteClienteUseCase softDeleteClienteUseCase;
  private final RestoreClienteUseCase restoreClienteUseCase;
  private final ForceDeleteClienteUseCase forceDeleteClienteUseCase;
  private final PreviewClientesCsvUseCase previewClientesCsvUseCase;
  private final ImportClientesCsvUseCase importClientesCsvUseCase;
  private final DownloadClientesCsvTemplateUseCase downloadClientesCsvTemplateUseCase;
  private final ClienteApplicationService clienteApplicationService;

  @PostMapping
  @Operation(summary = "Crear cliente")
  public ResponseEntity<ClienteResponse> create(@Valid @RequestBody ClienteUpsertRequest request, Authentication auth) {
    Cliente saved = createClienteUseCase.execute(toCommand(request), auth.getName(), role(auth));
    return ResponseEntity.ok(toResponse(saved));
  }

  @GetMapping
  @Operation(summary = "Listar clientes")
  public ResponseEntity<ClienteListResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String q,
      @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted) {
    Page<Cliente> result = listClientesUseCase.execute(page, size, q, includeDeleted);
    return ResponseEntity.ok(ClienteListResponse.builder()
        .content(result.getContent().stream().map(this::toResponse).toList())
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Detalle de cliente")
  public ResponseEntity<ClienteResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(toResponse(getClienteByIdUseCase.execute(id)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Editar cliente")
  public ResponseEntity<ClienteResponse> update(
      @PathVariable UUID id,
      @Valid @RequestBody ClienteUpsertRequest request,
      Authentication auth) {
    Cliente saved = updateClienteUseCase.execute(id, toCommand(request), auth.getName(), role(auth));
    return ResponseEntity.ok(toResponse(saved));
  }

  @PutMapping("/{id}/equivalencias")
  @Operation(summary = "Reemplazar tabla de equivalencia del cliente")
  public ResponseEntity<ClienteResponse> replaceEquivalencias(
      @PathVariable UUID id,
      @Valid @RequestBody ClienteEquivalenciasUpdateRequest request,
      Authentication auth) {
    Cliente saved = clienteApplicationService.replaceEquivalencias(
        id,
        request.getEquivalencias().stream().map(this::toEquivalenciaCommand).toList(),
        auth.getName(),
        role(auth));
    return ResponseEntity.ok(toResponse(saved));
  }

  @PostMapping(path = "/{id}/equivalencias/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Importar tabla de equivalencia desde Excel")
  public ResponseEntity<ClienteResponse> importEquivalencias(
      @PathVariable UUID id,
      @RequestPart("file") MultipartFile file,
      Authentication auth) {
    Cliente saved = clienteApplicationService.importEquivalenciasExcel(id, file, auth.getName(), role(auth));
    return ResponseEntity.ok(toResponse(saved));
  }

  @PatchMapping("/{id}/toggle-activo")
  @Operation(summary = "Alternar estado activo")
  public ResponseEntity<ClienteResponse> toggleActivo(@PathVariable UUID id, Authentication auth) {
    return ResponseEntity.ok(toResponse(toggleActivoClienteUseCase.execute(id, auth.getName(), role(auth))));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Eliminado logico")
  public ResponseEntity<Map<String, String>> softDelete(@PathVariable UUID id, Authentication auth) {
    softDeleteClienteUseCase.execute(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Cliente eliminado logicamente"));
  }

  @PatchMapping("/{id}/restore")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Restaurar cliente")
  public ResponseEntity<ClienteResponse> restore(@PathVariable UUID id, Authentication auth) {
    return ResponseEntity.ok(toResponse(restoreClienteUseCase.execute(id, auth.getName(), role(auth))));
  }

  @DeleteMapping("/{id}/force")
  @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
  @Operation(summary = "Eliminacion fisica")
  public ResponseEntity<Map<String, String>> forceDelete(@PathVariable UUID id, Authentication auth) {
    forceDeleteClienteUseCase.execute(id, auth.getName(), role(auth));
    return ResponseEntity.ok(Map.of("message", "Cliente eliminado fisicamente"));
  }

  @PostMapping(path = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Subir logo de cliente")
  public ResponseEntity<ClienteResponse> uploadLogo(@PathVariable UUID id, @RequestPart("file") MultipartFile file, Authentication auth) {
    Cliente saved = clienteApplicationService.uploadLogo(id, file, auth.getName(), role(auth));
    return ResponseEntity.ok(toResponse(saved));
  }

  @GetMapping("/{id}/logo")
  @Operation(summary = "Descargar logo de cliente")
  public ResponseEntity<Resource> getLogo(@PathVariable UUID id) {
    Cliente cliente = getClienteByIdUseCase.execute(id);
    Resource resource = clienteApplicationService.getLogo(id);
    return buildFileResponse(resource, cliente.getLogoContentType(), cliente.getLogoFileName(), "logo");
  }

  @GetMapping("/import/template")
  @Operation(summary = "Descargar plantilla Excel")
  public ResponseEntity<Resource> template() {
    return excelResponse(downloadClientesCsvTemplateUseCase.execute(), "clientes_template.xlsx");
  }

  @GetMapping("/import/template/example")
  @Operation(summary = "Descargar plantilla Excel con ejemplo")
  public ResponseEntity<Resource> templateExample() {
    return excelResponse(downloadClientesCsvTemplateUseCase.executeExample(), "clientes_template_ejemplo.xlsx");
  }

  @GetMapping("/equivalencias/template")
  @Operation(summary = "Descargar plantilla Excel de tabla de equivalencia")
  public ResponseEntity<Resource> equivalenciasTemplate() {
    return excelResponse(clienteApplicationService.downloadEquivalenciasTemplate(), "clientes_equivalencias_template.xlsx");
  }

  @GetMapping("/equivalencias/template/example")
  @Operation(summary = "Descargar ejemplo Excel de tabla de equivalencia")
  public ResponseEntity<Resource> equivalenciasTemplateExample() {
    return excelResponse(clienteApplicationService.downloadEquivalenciasTemplateExample(), "clientes_equivalencias_template_ejemplo.xlsx");
  }

  @PostMapping(path = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Previsualizar importacion Excel")
  public ResponseEntity<ClienteImportResult> preview(
      @RequestPart("file") MultipartFile file,
      @RequestParam(defaultValue = "INSERT_ONLY") String mode,
      @RequestParam(defaultValue = "true") boolean partialOk) {
    return ResponseEntity.ok(previewClientesCsvUseCase.execute(file, parseMode(mode), partialOk));
  }

  @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Importar Excel")
  public ResponseEntity<ClienteImportResult> importCsv(
      @RequestPart("file") MultipartFile file,
      @RequestParam(defaultValue = "INSERT_ONLY") String mode,
      @RequestParam(defaultValue = "true") boolean partialOk,
      Authentication auth) {
    return ResponseEntity.ok(importClientesCsvUseCase.execute(file, parseMode(mode), partialOk, auth.getName(), role(auth)));
  }

  private ClienteUpsertCommand toCommand(ClienteUpsertRequest request) {
    return new ClienteUpsertCommand(
        parseTipoDocumento(request.getTipoDocumento()),
        request.getDocumento(),
        request.getNombre(),
        request.getNombreComercial(),
        request.getDireccion(),
        request.getDescripcion(),
        request.getAplicaTablaEquivalencia(),
        request.getActivo());
  }

  private ClienteEquivalenciaUpsertCommand toEquivalenciaCommand(ClienteEquivalenciaRequest request) {
    return new ClienteEquivalenciaUpsertCommand(
        request.getId(),
        request.getDestino(),
        request.getValorDestino(),
        request.getCostoChofer());
  }

  private TipoDocumentoCliente parseTipoDocumento(String tipoDocumento) {
    try {
      return TipoDocumentoCliente.valueOf(tipoDocumento.trim().toUpperCase());
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "tipoDocumento invalido");
    }
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

  private ClienteResponse toResponse(Cliente cliente) {
    List<ClienteEquivalenciaResponse> equivalencias = cliente.getEquivalencias() == null
        ? List.of()
        : cliente.getEquivalencias().stream().map(this::toEquivalenciaResponse).toList();
    return ClienteResponse.builder()
        .id(cliente.getId())
        .tipoDocumento(cliente.getTipoDocumento().name())
        .documento(cliente.getDocumento())
        .nombre(cliente.getNombre())
        .nombreComercial(cliente.getNombreComercial())
        .direccion(cliente.getDireccion())
        .descripcion(cliente.getDescripcion())
        .aplicaTablaEquivalencia(cliente.getAplicaTablaEquivalencia())
        .equivalencias(equivalencias)
        .logoPath(cliente.getLogoFileName())
        .activo(cliente.getActivo())
        .deleted(cliente.getDeleted())
        .deletedAt(cliente.getDeletedAt())
        .deletedBy(cliente.getDeletedBy())
        .createdAt(cliente.getCreatedAt())
        .updatedAt(cliente.getUpdatedAt())
        .build();
  }

  private ClienteEquivalenciaResponse toEquivalenciaResponse(ClienteEquivalencia equivalencia) {
    return ClienteEquivalenciaResponse.builder()
        .id(equivalencia.getId())
        .destino(equivalencia.getDestino())
        .valorDestino(equivalencia.getValorDestino())
        .costoChofer(equivalencia.getCostoChofer())
        .createdAt(equivalencia.getCreatedAt())
        .updatedAt(equivalencia.getUpdatedAt())
        .build();
  }

  private ResponseEntity<Resource> buildFileResponse(Resource resource, String contentType, String fileName, String fallbackName) {
    MediaType mediaType = resolveMediaType(contentType, fileName);
    String resolvedFileName = resolveFileName(fileName, fallbackName);
    return ResponseEntity.ok()
        .contentType(mediaType)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resolvedFileName + "\"")
        .body(resource);
  }

  private ResponseEntity<Resource> excelResponse(byte[] content, String fileName) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
        .body(new ByteArrayResource(content));
  }

  private MediaType resolveMediaType(String contentType, String fileName) {
    if (contentType != null && !contentType.isBlank()) {
      try {
        return MediaType.parseMediaType(contentType);
      } catch (Exception ignored) {
        // fallback to file extension resolution below
      }
    }
    String lower = fileName == null ? "" : fileName.toLowerCase();
    if (lower.endsWith(".png")) {
      return MediaType.IMAGE_PNG;
    }
    if (lower.endsWith(".webp")) {
      return MediaType.valueOf("image/webp");
    }
    return MediaType.IMAGE_JPEG;
  }

  private String resolveFileName(String fileName, String fallbackName) {
    if (fileName == null || fileName.isBlank()) {
      return fallbackName;
    }
    return fileName;
  }
}
