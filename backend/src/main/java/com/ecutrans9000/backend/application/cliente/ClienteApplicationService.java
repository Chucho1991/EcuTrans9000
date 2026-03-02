package com.ecutrans9000.backend.application.cliente;

import com.ecutrans9000.backend.application.usecase.cliente.ClienteImportError;
import com.ecutrans9000.backend.application.usecase.cliente.ClienteImportResult;
import com.ecutrans9000.backend.application.usecase.cliente.ClienteUpsertCommand;
import com.ecutrans9000.backend.application.usecase.cliente.ImportMode;
import com.ecutrans9000.backend.application.vehiculo.CsvLineParser;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;
import com.ecutrans9000.backend.ports.out.cliente.ClienteRepositoryPort;
import com.ecutrans9000.backend.service.AuditService;
import com.ecutrans9000.backend.service.BusinessException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ClienteApplicationService {

  private static final List<String> HEADER_COLUMNS = List.of(
      "tipo_documento",
      "documento",
      "nombre",
      "nombre_comercial",
      "descripcion",
      "activo"
  );
  private static final List<String> ALLOWED_LOGO_CONTENT_TYPES = List.of(
      "image/jpeg",
      "image/png",
      "image/webp"
  );

  private final ClienteRepositoryPort clienteRepositoryPort;
  private final AuditService auditService;

  @Value("${app.clientes.import-batch-size:500}")
  private int importBatchSize;

  @Value("${app.clientes.max-logo-bytes:5242880}")
  private long maxLogoBytes;

  public Cliente create(ClienteUpsertCommand command, String actorUsername, String actorRole) {
    String documentoNorm = Cliente.normalizeDocumento(command.documento());
    if (clienteRepositoryPort.existsByDocumentoNorm(documentoNorm)) {
      throw new BusinessException(HttpStatus.CONFLICT, "El documento ya existe");
    }

    Cliente cliente = Cliente.builder()
        .id(UUID.randomUUID())
        .tipoDocumento(command.tipoDocumento())
        .documento(command.documento().trim())
        .documentoNorm(documentoNorm)
        .nombre(command.nombre().trim())
        .nombreComercial(trimNullable(command.nombreComercial()))
        .descripcion(trimNullable(command.descripcion()))
        .activo(command.activo() == null || command.activo())
        .deleted(false)
        .deletedAt(null)
        .deletedBy(null)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    validate(cliente);
    Cliente saved = clienteRepositoryPort.save(cliente);
    auditService.saveActionAudit(actorUsername, actorRole, "CLIENTES", ActionType.CREACION, saved.getId().toString(), "clientes");
    return saved;
  }

  public Cliente update(UUID id, ClienteUpsertCommand command, String actorUsername, String actorRole) {
    Cliente cliente = getExisting(id);
    String documentoNorm = Cliente.normalizeDocumento(command.documento());
    if (clienteRepositoryPort.existsByDocumentoNormAndIdNot(documentoNorm, id)) {
      throw new BusinessException(HttpStatus.CONFLICT, "El documento ya existe");
    }

    cliente.setTipoDocumento(command.tipoDocumento());
    cliente.setDocumento(command.documento().trim());
    cliente.setDocumentoNorm(documentoNorm);
    cliente.setNombre(command.nombre().trim());
    cliente.setNombreComercial(trimNullable(command.nombreComercial()));
    cliente.setDescripcion(trimNullable(command.descripcion()));
    cliente.setActivo(command.activo() == null || command.activo());

    validate(cliente);
    Cliente saved = clienteRepositoryPort.save(cliente);
    auditService.saveActionAudit(actorUsername, actorRole, "CLIENTES", ActionType.EDICION, saved.getId().toString(), "clientes");
    return saved;
  }

  public Cliente getById(UUID id) {
    return getExisting(id);
  }

  public Page<Cliente> list(int page, int size, String q, Boolean includeDeleted) {
    return clienteRepositoryPort.search(page, size, q, includeDeleted);
  }

  public Cliente toggleActivo(UUID id, String actorUsername, String actorRole) {
    Cliente cliente = getExisting(id);
    if (Boolean.TRUE.equals(cliente.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede cambiar estado de un cliente eliminado");
    }
    cliente.setActivo(!Boolean.TRUE.equals(cliente.getActivo()));
    Cliente saved = clienteRepositoryPort.save(cliente);
    auditService.saveActionAudit(actorUsername, actorRole, "CLIENTES", ActionType.CAMBIO_ESTADO, saved.getId().toString(), "clientes");
    return saved;
  }

  public void softDelete(UUID id, String actorUsername, String actorRole) {
    Cliente cliente = getExisting(id);
    cliente.setDeleted(true);
    cliente.setActivo(false);
    cliente.setDeletedAt(LocalDateTime.now());
    cliente.setDeletedBy(actorUsername);
    clienteRepositoryPort.save(cliente);
    auditService.saveActionAudit(actorUsername, actorRole, "CLIENTES", ActionType.ELIMINADO_LOGICO, id.toString(), "clientes");
  }

  public Cliente restore(UUID id, String actorUsername, String actorRole) {
    Cliente cliente = getExisting(id);
    cliente.setDeleted(false);
    cliente.setDeletedAt(null);
    cliente.setDeletedBy(null);
    Cliente saved = clienteRepositoryPort.save(cliente);
    auditService.saveActionAudit(actorUsername, actorRole, "CLIENTES", ActionType.RESTAURACION, id.toString(), "clientes");
    return saved;
  }

  public void forceDelete(UUID id, String actorUsername, String actorRole) {
    getExisting(id);
    clienteRepositoryPort.deleteById(id);
    auditService.saveActionAudit(actorUsername, actorRole, "CLIENTES", ActionType.ELIMINADO_FISICO, id.toString(), "clientes");
  }

  public Cliente validateClienteDisponibleParaViaje(UUID id) {
    Cliente cliente = getExisting(id);
    if (Boolean.TRUE.equals(cliente.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El cliente esta eliminado logicamente");
    }
    if (!Boolean.TRUE.equals(cliente.getActivo())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El cliente esta inactivo y no puede asociarse a nuevos viajes");
    }
    return cliente;
  }

  public Cliente uploadLogo(UUID id, MultipartFile file, String actorUsername, String actorRole) {
    Cliente cliente = getExisting(id);
    validateLogoFile(file);
    try {
      cliente.setLogoFileName(file.getOriginalFilename());
      cliente.setLogoContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
      cliente.setLogoContenido(file.getBytes());
    } catch (IOException ex) {
      throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar logo");
    }
    Cliente saved = clienteRepositoryPort.save(cliente);
    auditService.saveActionAudit(actorUsername, actorRole, "CLIENTES", ActionType.EDICION, id.toString(), "clientes");
    return saved;
  }

  public Resource getLogo(UUID id) {
    Cliente cliente = getExisting(id);
    if (cliente.getLogoContenido() == null || cliente.getLogoContenido().length == 0) {
      throw new BusinessException(HttpStatus.NOT_FOUND, "Logo no encontrado");
    }
    return new ByteArrayResource(cliente.getLogoContenido());
  }

  public String downloadTemplate() {
    return String.join(",", HEADER_COLUMNS) + "\n";
  }

  public ClienteImportResult previewCsv(MultipartFile file, ImportMode mode, boolean partialOk) {
    return processCsv(file, mode, partialOk, true, "SYSTEM", "SYSTEM");
  }

  @Transactional
  public ClienteImportResult importCsv(MultipartFile file, ImportMode mode, boolean partialOk, String actorUsername, String actorRole) {
    ClienteImportResult result = processCsv(file, mode, partialOk, false, actorUsername, actorRole);
    if (!partialOk && result.getErrorsCount() > 0) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
    if (result.getProcessed() > 0) {
      auditService.saveActionAudit(actorUsername, actorRole, "CLIENTES", ActionType.IMPORT_CSV, "N/A", "clientes");
    }
    return result;
  }

  private ClienteImportResult processCsv(
      MultipartFile file,
      ImportMode mode,
      boolean partialOk,
      boolean previewOnly,
      String actorUsername,
      String actorRole) {

    validateCsvFile(file);

    int totalRows = 0;
    int processed = 0;
    int inserted = 0;
    int updated = 0;
    int skipped = 0;
    List<ClienteImportError> errors = new ArrayList<>();
    List<Cliente> batch = new ArrayList<>();

    try (InputStream inputStream = new BufferedInputStream(file.getInputStream());
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

      String headerLine = reader.readLine();
      if (headerLine == null || headerLine.isBlank()) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, "CSV vacio");
      }

      char delimiter = detectDelimiter(headerLine);
      List<String> header = CsvLineParser.parse(headerLine, delimiter).stream()
          .map(h -> h.toLowerCase(Locale.ROOT).trim())
          .toList();
      validateHeader(header);

      String line;
      int rowNumber = 1;
      while ((line = reader.readLine()) != null) {
        rowNumber++;
        if (line.isBlank()) {
          continue;
        }

        totalRows++;
        List<String> values = CsvLineParser.parse(line, delimiter);
        if (values.stream().allMatch(String::isBlank)) {
          continue;
        }

        try {
          ClienteUpsertCommand command = parseCommand(values, rowNumber);
          String documentoNorm = Cliente.normalizeDocumento(command.documento());

          Optional<Cliente> existing = clienteRepositoryPort.findByDocumentoNorm(documentoNorm);
          if (mode == ImportMode.INSERT_ONLY && existing.isPresent()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "documento ya existe");
          }

          if (existing.isPresent()) {
            Cliente cliente = existing.get();
            cliente.setTipoDocumento(command.tipoDocumento());
            cliente.setDocumento(command.documento().trim());
            cliente.setDocumentoNorm(documentoNorm);
            cliente.setNombre(command.nombre().trim());
            cliente.setNombreComercial(trimNullable(command.nombreComercial()));
            cliente.setDescripcion(trimNullable(command.descripcion()));
            cliente.setActivo(command.activo() == null || command.activo());
            validate(cliente);

            if (!previewOnly) {
              batch.add(cliente);
              flushBatchIfNeeded(batch);
            }
            updated++;
            processed++;
          } else {
            Cliente cliente = Cliente.builder()
                .id(UUID.randomUUID())
                .tipoDocumento(command.tipoDocumento())
                .documento(command.documento().trim())
                .documentoNorm(documentoNorm)
                .nombre(command.nombre().trim())
                .nombreComercial(trimNullable(command.nombreComercial()))
                .descripcion(trimNullable(command.descripcion()))
                .activo(command.activo() == null || command.activo())
                .deleted(false)
                .deletedAt(null)
                .deletedBy(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            validate(cliente);

            if (!previewOnly) {
              batch.add(cliente);
              flushBatchIfNeeded(batch);
            }
            inserted++;
            processed++;
          }
        } catch (BusinessException ex) {
          skipped++;
          errors.add(new ClienteImportError(rowNumber, "row", ex.getMessage()));
          if (!partialOk) {
            break;
          }
        } catch (Exception ex) {
          skipped++;
          errors.add(new ClienteImportError(rowNumber, "row", "Error inesperado: " + ex.getMessage()));
          if (!partialOk) {
            break;
          }
        }
      }

      if (!previewOnly) {
        flushAll(batch);
      }
    } catch (IOException ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se pudo leer el CSV");
    }

    return ClienteImportResult.builder()
        .totalRows(totalRows)
        .processed(processed)
        .inserted(inserted)
        .updated(updated)
        .skipped(skipped)
        .errorsCount(errors.size())
        .errors(errors)
        .build();
  }

  private ClienteUpsertCommand parseCommand(List<String> values, int rowNumber) {
    if (values.size() < HEADER_COLUMNS.size()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Fila incompleta en row " + rowNumber);
    }

    String tipoDocumento = values.get(0);
    String documento = values.get(1);
    String nombre = values.get(2);
    String nombreComercial = values.get(3);
    String descripcion = values.get(4);
    String activoValue = values.get(5);

    if (documento == null || documento.isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "documento es obligatorio");
    }
    if (nombre == null || nombre.isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "nombre es obligatorio");
    }

    Boolean activo;
    try {
      activo = parseBoolean(activoValue);
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "activo invalido. Use true|false|si|no|1|0");
    }

    return new ClienteUpsertCommand(
        parseTipoDocumento(tipoDocumento),
        documento,
        nombre,
        nombreComercial,
        descripcion,
        activo
    );
  }

  private TipoDocumentoCliente parseTipoDocumento(String value) {
    try {
      return TipoDocumentoCliente.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "tipo_documento invalido. Use CEDULA|RUC|PASAPORTE");
    }
  }

  private boolean parseBoolean(String value) {
    String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "true", "1", "si", "sí", "s", "yes" -> true;
      case "false", "0", "no", "n" -> false;
      default -> throw new IllegalArgumentException("boolean invalido");
    };
  }

  private void validateCsvFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe adjuntar un archivo CSV");
    }
    String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
    if (!name.endsWith(".csv")) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El archivo debe ser .csv");
    }
  }

  private void validateHeader(List<String> header) {
    if (!header.equals(HEADER_COLUMNS)) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
          "Encabezados invalidos. Debe usar: " + String.join(",", HEADER_COLUMNS));
    }
  }

  private char detectDelimiter(String headerLine) {
    long commaCount = headerLine.chars().filter(ch -> ch == ',').count();
    long semicolonCount = headerLine.chars().filter(ch -> ch == ';').count();
    return semicolonCount > commaCount ? ';' : ',';
  }

  private void flushBatchIfNeeded(List<Cliente> batch) {
    if (batch.size() >= Math.max(importBatchSize, 1)) {
      flushAll(batch);
    }
  }

  private void flushAll(List<Cliente> batch) {
    if (batch.isEmpty()) {
      return;
    }
    batch.forEach(clienteRepositoryPort::save);
    batch.clear();
  }

  private Cliente getExisting(UUID id) {
    return clienteRepositoryPort.findById(id)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
  }

  private void validate(Cliente cliente) {
    if (cliente.getTipoDocumento() == null) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Tipo de documento es obligatorio");
    }
    if (cliente.getDocumentoNorm().isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Documento es obligatorio");
    }
    if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Nombre es obligatorio");
    }
  }

  private void validateLogoFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe seleccionar un archivo");
    }
    if (file.getSize() > maxLogoBytes) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Archivo excede tamano maximo");
    }
    String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
    if (!ALLOWED_LOGO_CONTENT_TYPES.contains(contentType)) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Tipo de archivo no permitido. Solo JPG/PNG/WEBP");
    }
  }

  private String trimNullable(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
