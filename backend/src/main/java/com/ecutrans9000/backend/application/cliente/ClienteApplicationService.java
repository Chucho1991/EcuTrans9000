package com.ecutrans9000.backend.application.cliente;

import com.ecutrans9000.backend.application.usecase.cliente.ClienteImportError;
import com.ecutrans9000.backend.application.usecase.cliente.ClienteImportResult;
import com.ecutrans9000.backend.application.usecase.cliente.ClienteUpsertCommand;
import com.ecutrans9000.backend.application.usecase.cliente.ImportMode;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;
import com.ecutrans9000.backend.ports.out.cliente.ClienteRepositoryPort;
import com.ecutrans9000.backend.service.AuditService;
import com.ecutrans9000.backend.service.BusinessException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
      "direccion",
      "descripcion",
      "activo"
  );
  private static final List<String> ALLOWED_LOGO_CONTENT_TYPES = List.of(
      "image/jpeg",
      "image/png",
      "image/webp"
  );
  private static final DataFormatter DATA_FORMATTER = new DataFormatter();

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
        .direccion(trimNullable(command.direccion()))
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
    cliente.setDireccion(trimNullable(command.direccion()));
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

  public byte[] downloadTemplate() {
    return buildTemplateWorkbook(false);
  }

  public byte[] downloadExampleTemplate() {
    return buildTemplateWorkbook(true);
  }

  public ClienteImportResult previewExcel(MultipartFile file, ImportMode mode, boolean partialOk) {
    return processExcel(file, mode, partialOk, true, "SYSTEM", "SYSTEM");
  }

  @Transactional
  public ClienteImportResult importExcel(MultipartFile file, ImportMode mode, boolean partialOk, String actorUsername, String actorRole) {
    ClienteImportResult result = processExcel(file, mode, partialOk, false, actorUsername, actorRole);
    if (!partialOk && result.getErrorsCount() > 0) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
    if (result.getProcessed() > 0) {
      auditService.saveActionAudit(actorUsername, actorRole, "CLIENTES", ActionType.IMPORT_CSV, "N/A", "clientes");
    }
    return result;
  }

  private byte[] buildTemplateWorkbook(boolean includeExampleRow) {
    try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      XSSFSheet sheet = workbook.createSheet("Clientes Import");
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < HEADER_COLUMNS.size(); i++) {
        headerRow.createCell(i).setCellValue(HEADER_COLUMNS.get(i));
        sheet.setColumnWidth(i, 22 * 256);
      }
      if (includeExampleRow) {
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("RUC");
        exampleRow.createCell(1).setCellValue("1799999999001");
        exampleRow.createCell(2).setCellValue("COMERCIAL LOPEZ S.A.");
        exampleRow.createCell(3).setCellValue("Av. Americas y Naciones Unidas");
        exampleRow.createCell(4).setCellValue("Cliente corporativo");
        exampleRow.createCell(5).setCellValue("true");
      }
      workbook.write(outputStream);
      return outputStream.toByteArray();
    } catch (IOException ex) {
      throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar la plantilla Excel");
    }
  }

  private ClienteImportResult processExcel(
      MultipartFile file,
      ImportMode mode,
      boolean partialOk,
      boolean previewOnly,
      String actorUsername,
      String actorRole) {

    validateExcelFile(file);

    int totalRows = 0;
    int processed = 0;
    int inserted = 0;
    int updated = 0;
    int skipped = 0;
    List<ClienteImportError> errors = new ArrayList<>();
    List<Cliente> batch = new ArrayList<>();

    try (InputStream inputStream = new BufferedInputStream(file.getInputStream());
         XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
      XSSFSheet sheet = workbook.getSheetAt(0);
      Row headerRow = sheet.getRow(0);
      if (headerRow == null) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, "La plantilla Excel no tiene encabezados");
      }
      validateHeader(readHeader(headerRow));

      for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
        Row row = sheet.getRow(rowIndex);
        if (row == null || !rowHasContent(row)) {
          continue;
        }

        int rowNumber = rowIndex + 1;
        totalRows++;

        try {
          ClienteUpsertCommand command = parseCommand(row, rowNumber);
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
            cliente.setDireccion(trimNullable(command.direccion()));
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
                .direccion(trimNullable(command.direccion()))
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
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo Excel");
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

  private ClienteUpsertCommand parseCommand(Row row, int rowNumber) {
    String tipoDocumento = readCellAsString(row.getCell(0));
    String documento = readCellAsString(row.getCell(1));
    String nombre = readCellAsString(row.getCell(2));
    String direccion = readCellAsString(row.getCell(3));
    String descripcion = readCellAsString(row.getCell(4));
    String activoValue = readCellAsString(row.getCell(5));

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
        null,
        direccion,
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

  private void validateExcelFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe adjuntar un archivo Excel");
    }
    String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
    if (!name.endsWith(".xlsx")) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El archivo debe ser .xlsx");
    }
  }

  private void validateHeader(List<String> header) {
    if (!header.equals(HEADER_COLUMNS)) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
            "Encabezados invalidos. Debe usar: " + String.join(",", HEADER_COLUMNS));
    }
  }

  private List<String> readHeader(Row row) {
    List<String> values = new ArrayList<>();
    for (int i = 0; i < HEADER_COLUMNS.size(); i++) {
      String value = readCellAsString(row.getCell(i));
      values.add(value == null ? "" : value.trim().toLowerCase(Locale.ROOT));
    }
    return values;
  }

  private boolean rowHasContent(Row row) {
    for (int i = 0; i < HEADER_COLUMNS.size(); i++) {
      Cell cell = row.getCell(i);
      if (cell == null || cell.getCellType() == CellType.BLANK) {
        continue;
      }
      String value = readCellAsString(cell);
      if (value != null && !value.isBlank()) {
        return true;
      }
    }
    return false;
  }

  private String readCellAsString(Cell cell) {
    if (cell == null) {
      return null;
    }
    String value = DATA_FORMATTER.formatCellValue(cell);
    return value == null ? null : value.trim();
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
