package com.ecutrans9000.backend.application.vehiculo;

import com.ecutrans9000.backend.application.usecase.vehiculo.ImportMode;
import com.ecutrans9000.backend.application.usecase.vehiculo.VehiculoImportError;
import com.ecutrans9000.backend.application.usecase.vehiculo.VehiculoImportResult;
import com.ecutrans9000.backend.application.usecase.vehiculo.VehiculoUpsertCommand;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.domain.vehiculo.EstadoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.TipoArchivoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.TipoDocumento;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import com.ecutrans9000.backend.domain.vehiculo.VehiculoArchivo;
import com.ecutrans9000.backend.ports.out.vehiculo.VehiculoArchivoRepositoryPort;
import com.ecutrans9000.backend.ports.out.vehiculo.VehiculoRepositoryPort;
import com.ecutrans9000.backend.service.AuditService;
import com.ecutrans9000.backend.service.BusinessException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
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

/**
 * Servicio de aplicación para orquestar casos de uso del módulo vehículos.
 */
@Service
@RequiredArgsConstructor
public class VehiculoApplicationService {

  private static final List<String> HEADER_COLUMNS = List.of(
      "placa",
      "chofer_default",
      "licencia",
      "fecha_caducidad_licencia",
      "tipo_documento",
      "documento_personal",
      "tonelaje_categoria",
      "m3",
      "estado"
  );
  private static final DataFormatter DATA_FORMATTER = new DataFormatter();

  private static final List<String> ALLOWED_IMAGE_CONTENT_TYPES = List.of(
      "image/jpeg",
      "image/png",
      "image/webp"
  );
  private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of(
      "jpg",
      "jpeg",
      "png",
      "webp"
  );
  private static final List<String> ALLOWED_DOC_CONTENT_TYPES = List.of(
      "image/jpeg",
      "image/png",
      "image/webp",
      "application/pdf"
  );
  private static final List<String> ALLOWED_DOC_EXTENSIONS = List.of(
      "jpg",
      "jpeg",
      "png",
      "webp",
      "pdf"
  );

  private final VehiculoRepositoryPort vehiculoRepositoryPort;
  private final VehiculoArchivoRepositoryPort vehiculoArchivoRepositoryPort;
  private final AuditService auditService;

  @Value("${app.vehiculos.max-image-bytes:5242880}")
  private long maxImageBytes;

  @Value("${app.vehiculos.import-batch-size:500}")
  private int importBatchSize;

  public Vehiculo create(VehiculoUpsertCommand command, String actorUsername, String actorRole) {
    String placaNorm = Vehiculo.normalizePlaca(command.placa());
    if (vehiculoRepositoryPort.existsByPlacaNorm(placaNorm)) {
      throw new BusinessException(HttpStatus.CONFLICT, "La placa ya existe");
    }

    Vehiculo vehiculo = Vehiculo.builder()
        .id(UUID.randomUUID())
        .placa(command.placa().trim())
        .placaNorm(placaNorm)
        .choferDefault(command.choferDefault().trim())
        .licencia(trimNullable(command.licencia()))
        .fechaCaducidadLicencia(command.fechaCaducidadLicencia())
        .tipoDocumento(command.tipoDocumento())
        .documentoPersonal(command.documentoPersonal().trim())
        .tonelajeCategoria(command.tonelajeCategoria().trim())
        .m3(command.m3())
        .estado(command.estado())
        .deleted(false)
        .deletedAt(null)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    validateVehiculo(vehiculo);
    Vehiculo saved = vehiculoRepositoryPort.save(vehiculo);
    auditService.saveActionAudit(actorUsername, actorRole, "VEHICULOS", ActionType.CREACION, saved.getId().toString(), "vehiculos");
    return saved;
  }

  public Vehiculo update(UUID id, VehiculoUpsertCommand command, String actorUsername, String actorRole) {
    Vehiculo vehiculo = getExisting(id);
    String placaNorm = Vehiculo.normalizePlaca(command.placa());

    if (vehiculoRepositoryPort.existsByPlacaNormAndIdNot(placaNorm, id)) {
      throw new BusinessException(HttpStatus.CONFLICT, "La placa ya existe");
    }

    vehiculo.setPlaca(command.placa().trim());
    vehiculo.setPlacaNorm(placaNorm);
    vehiculo.setChoferDefault(command.choferDefault().trim());
    vehiculo.setLicencia(trimNullable(command.licencia()));
    vehiculo.setFechaCaducidadLicencia(command.fechaCaducidadLicencia());
    vehiculo.setTipoDocumento(command.tipoDocumento());
    vehiculo.setDocumentoPersonal(command.documentoPersonal().trim());
    vehiculo.setTonelajeCategoria(command.tonelajeCategoria().trim());
    vehiculo.setM3(command.m3());
    vehiculo.setEstado(command.estado());

    validateVehiculo(vehiculo);
    Vehiculo saved = vehiculoRepositoryPort.save(vehiculo);
    auditService.saveActionAudit(actorUsername, actorRole, "VEHICULOS", ActionType.EDICION, saved.getId().toString(), "vehiculos");
    return saved;
  }

  public Vehiculo getById(UUID id) {
    return getExisting(id);
  }

  public Page<Vehiculo> search(int page, int size, String q, String estado, Boolean includeDeleted) {
    return vehiculoRepositoryPort.search(page, size, q, estado, includeDeleted);
  }

  public void activate(UUID id, String actorUsername, String actorRole) {
    Vehiculo vehiculo = getExisting(id);
    if (Boolean.TRUE.equals(vehiculo.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede activar un vehiculo eliminado");
    }
    validateLicenseNotExpiredForActivation(vehiculo);
    vehiculo.setEstado(EstadoVehiculo.ACTIVO);
    vehiculoRepositoryPort.save(vehiculo);
    auditService.saveActionAudit(actorUsername, actorRole, "VEHICULOS", ActionType.EDICION, id.toString(), "vehiculos");
  }

  public void deactivate(UUID id, String actorUsername, String actorRole) {
    Vehiculo vehiculo = getExisting(id);
    if (Boolean.TRUE.equals(vehiculo.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El vehiculo ya esta eliminado logicamente");
    }
    vehiculo.setEstado(EstadoVehiculo.INACTIVO);
    vehiculoRepositoryPort.save(vehiculo);
    auditService.saveActionAudit(actorUsername, actorRole, "VEHICULOS", ActionType.EDICION, id.toString(), "vehiculos");
  }

  public void softDelete(UUID id, String actorUsername, String actorRole) {
    Vehiculo vehiculo = getExisting(id);
    vehiculo.setDeleted(true);
    vehiculo.setDeletedAt(LocalDateTime.now());
    vehiculo.setEstado(EstadoVehiculo.INACTIVO);
    vehiculoRepositoryPort.save(vehiculo);
    auditService.saveActionAudit(actorUsername, actorRole, "VEHICULOS", ActionType.ELIMINADO_LOGICO, id.toString(), "vehiculos");
  }

  public void restore(UUID id, String actorUsername, String actorRole) {
    Vehiculo vehiculo = getExisting(id);
    vehiculo.setDeleted(false);
    vehiculo.setDeletedAt(null);
    vehiculo.setEstado(EstadoVehiculo.ACTIVO);
    applyLicenseExpiryStatus(vehiculo);
    vehiculoRepositoryPort.save(vehiculo);
    auditService.saveActionAudit(actorUsername, actorRole, "VEHICULOS", ActionType.EDICION, id.toString(), "vehiculos");
  }

  public Vehiculo uploadFoto(UUID id, MultipartFile file, String actorUsername, String actorRole) {
    Vehiculo vehiculo = getExisting(id);
    upsertArchivo(file, vehiculo, TipoArchivoVehiculo.FOTO, ALLOWED_IMAGE_CONTENT_TYPES, ALLOWED_IMAGE_EXTENSIONS, "Solo JPG/PNG/WEBP");
    vehiculo.setFotoPath(file.getOriginalFilename());
    Vehiculo saved = vehiculoRepositoryPort.save(vehiculo);
    auditService.saveActionAudit(actorUsername, actorRole, "VEHICULOS", ActionType.EDICION, id.toString(), "vehiculos");
    return saved;
  }

  public Vehiculo uploadDocumento(UUID id, MultipartFile file, String actorUsername, String actorRole) {
    Vehiculo vehiculo = getExisting(id);
    upsertArchivo(file, vehiculo, TipoArchivoVehiculo.DOCUMENTO, ALLOWED_DOC_CONTENT_TYPES, ALLOWED_DOC_EXTENSIONS, "Solo JPG/PNG/WEBP/PDF");
    vehiculo.setDocPath(file.getOriginalFilename());
    Vehiculo saved = vehiculoRepositoryPort.save(vehiculo);
    auditService.saveActionAudit(actorUsername, actorRole, "VEHICULOS", ActionType.EDICION, id.toString(), "vehiculos");
    return saved;
  }

  public Vehiculo uploadLicencia(UUID id, MultipartFile file, String actorUsername, String actorRole) {
    Vehiculo vehiculo = getExisting(id);
    upsertArchivo(file, vehiculo, TipoArchivoVehiculo.LICENCIA, ALLOWED_DOC_CONTENT_TYPES, ALLOWED_DOC_EXTENSIONS, "Solo JPG/PNG/WEBP/PDF");
    vehiculo.setLicPath(file.getOriginalFilename());
    Vehiculo saved = vehiculoRepositoryPort.save(vehiculo);
    auditService.saveActionAudit(actorUsername, actorRole, "VEHICULOS", ActionType.EDICION, id.toString(), "vehiculos");
    return saved;
  }

  public Resource getFoto(UUID id) {
    return readArchivo(id, TipoArchivoVehiculo.FOTO);
  }

  public Resource getDocumento(UUID id) {
    return readArchivo(id, TipoArchivoVehiculo.DOCUMENTO);
  }

  public Resource getLicencia(UUID id) {
    return readArchivo(id, TipoArchivoVehiculo.LICENCIA);
  }

  public String getFotoPreviewDataUrl(UUID id) {
    return vehiculoArchivoRepositoryPort.findByVehiculoIdAndTipo(id, TipoArchivoVehiculo.FOTO)
        .map(this::toDataUrl)
        .orElse(null);
  }

  public Map<UUID, String> getFotoPreviewDataUrls(List<UUID> vehiculoIds) {
    if (vehiculoIds == null || vehiculoIds.isEmpty()) {
      return Map.of();
    }
    Map<UUID, String> previews = new LinkedHashMap<>();
    vehiculoArchivoRepositoryPort.findByVehiculoIdsAndTipo(vehiculoIds, TipoArchivoVehiculo.FOTO)
        .forEach(archivo -> previews.put(archivo.getVehiculoId(), toDataUrl(archivo)));
    return previews;
  }

  public byte[] downloadTemplate() {
    return buildTemplateWorkbook(false);
  }

  public byte[] downloadExampleTemplate() {
    return buildTemplateWorkbook(true);
  }

  public VehiculoImportResult previewExcel(MultipartFile file, ImportMode mode, boolean partialOk) {
    return processExcel(file, mode, partialOk, true, "SYSTEM", "SYSTEM");
  }

  @Transactional
  public VehiculoImportResult importExcel(MultipartFile file, ImportMode mode, boolean partialOk, String actorUsername, String actorRole) {
    VehiculoImportResult result = processExcel(file, mode, partialOk, false, actorUsername, actorRole);
    if (!partialOk && result.getErrorsCount() > 0) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
    if (result.getProcessed() > 0) {
      auditService.saveActionAudit(actorUsername, actorRole, "VEHICULOS", ActionType.IMPORT_CSV, "N/A", "vehiculos");
    }
    return result;
  }

  @Transactional
  public int deactivateExpiredLicenses() {
    int affected = vehiculoRepositoryPort.deactivateExpiredLicenses(LocalDate.now());
    if (affected > 0) {
      auditService.saveActionAudit("SYSTEM", "SYSTEM", "VEHICULOS", ActionType.EDICION, "N/A", "vehiculos");
    }
    return affected;
  }

  private byte[] buildTemplateWorkbook(boolean includeExampleRow) {
    try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      XSSFSheet sheet = workbook.createSheet("Vehiculos");
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < HEADER_COLUMNS.size(); i++) {
        headerRow.createCell(i).setCellValue(HEADER_COLUMNS.get(i));
        sheet.setColumnWidth(i, 22 * 256);
      }
      if (includeExampleRow) {
        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("ABC-1234");
        row.createCell(1).setCellValue("Juan Perez");
        row.createCell(2).setCellValue("LIC-0001");
        row.createCell(3).setCellValue("2026-12-31");
        row.createCell(4).setCellValue("CEDULA");
        row.createCell(5).setCellValue("0102030405");
        row.createCell(6).setCellValue("PESADO");
        row.createCell(7).setCellValue(12.5d);
        row.createCell(8).setCellValue("ACTIVO");
      }
      workbook.write(outputStream);
      return outputStream.toByteArray();
    } catch (IOException ex) {
      throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar la plantilla Excel");
    }
  }

  private VehiculoImportResult processExcel(
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
    List<VehiculoImportError> errors = new ArrayList<>();
    List<Vehiculo> batch = new ArrayList<>();

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

        totalRows++;
        try {
          VehiculoUpsertCommand command = parseCommand(row, rowIndex + 1);
          String placaNorm = Vehiculo.normalizePlaca(command.placa());
          Optional<Vehiculo> existing = vehiculoRepositoryPort.findByPlacaNorm(placaNorm);

          if (mode == ImportMode.INSERT_ONLY && existing.isPresent()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "placa ya existe");
          }

          if (existing.isPresent()) {
            Vehiculo vehiculo = existing.get();
            vehiculo.setPlaca(command.placa().trim());
            vehiculo.setPlacaNorm(placaNorm);
            vehiculo.setChoferDefault(command.choferDefault().trim());
            vehiculo.setLicencia(trimNullable(command.licencia()));
            vehiculo.setFechaCaducidadLicencia(command.fechaCaducidadLicencia());
            vehiculo.setTipoDocumento(command.tipoDocumento());
            vehiculo.setDocumentoPersonal(command.documentoPersonal().trim());
            vehiculo.setTonelajeCategoria(command.tonelajeCategoria().trim());
            vehiculo.setM3(command.m3());
            vehiculo.setEstado(command.estado());
            validateVehiculo(vehiculo);

            if (!previewOnly) {
              batch.add(vehiculo);
              flushBatchIfNeeded(batch);
            }
            updated++;
            processed++;
          } else {
            Vehiculo vehiculo = Vehiculo.builder()
                .id(UUID.randomUUID())
                .placa(command.placa().trim())
                .placaNorm(placaNorm)
                .choferDefault(command.choferDefault().trim())
                .licencia(trimNullable(command.licencia()))
                .fechaCaducidadLicencia(command.fechaCaducidadLicencia())
                .tipoDocumento(command.tipoDocumento())
                .documentoPersonal(command.documentoPersonal().trim())
                .tonelajeCategoria(command.tonelajeCategoria().trim())
                .m3(command.m3())
                .estado(command.estado())
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            validateVehiculo(vehiculo);

            if (!previewOnly) {
              batch.add(vehiculo);
              flushBatchIfNeeded(batch);
            }
            inserted++;
            processed++;
          }
        } catch (BusinessException ex) {
          skipped++;
          errors.add(new VehiculoImportError(rowIndex + 1, "row", ex.getMessage()));
          if (!partialOk) {
            break;
          }
        } catch (Exception ex) {
          skipped++;
          errors.add(new VehiculoImportError(rowIndex + 1, "row", "Error inesperado: " + ex.getMessage()));
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

    return VehiculoImportResult.builder()
        .totalRows(totalRows)
        .processed(processed)
        .inserted(inserted)
        .updated(updated)
        .skipped(skipped)
        .errorsCount(errors.size())
        .errors(errors)
        .build();
  }

  private void flushBatchIfNeeded(List<Vehiculo> batch) {
    if (batch.size() >= Math.max(importBatchSize, 1)) {
      flushAll(batch);
    }
  }

  private void flushAll(List<Vehiculo> batch) {
    if (batch.isEmpty()) {
      return;
    }
    batch.forEach(vehiculoRepositoryPort::save);
    batch.clear();
  }

  private VehiculoUpsertCommand parseCommand(Row row, int rowNumber) {
    String placa = readCellAsString(row.getCell(0));
    String choferDefault = readCellAsString(row.getCell(1));
    String licencia = readCellAsString(row.getCell(2));
    LocalDate fechaCaducidadLicencia = parseOptionalDate(row.getCell(3));
    String tipoDocumento = readCellAsString(row.getCell(4));
    String documentoPersonal = readCellAsString(row.getCell(5));
    String tonelajeCategoria = readCellAsString(row.getCell(6));
    String m3Value = readCellAsString(row.getCell(7));
    String estado = readCellAsString(row.getCell(8));

    if (placa == null || placa.isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "placa es obligatoria");
    }
    if (choferDefault == null || choferDefault.isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "chofer_default es obligatorio");
    }
    if (documentoPersonal == null || documentoPersonal.isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "documento_personal es obligatorio");
    }
    if (tonelajeCategoria == null || tonelajeCategoria.isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "tonelaje_categoria es obligatorio");
    }

    BigDecimal m3;
    try {
      m3 = new BigDecimal(m3Value.trim());
      if (m3.compareTo(BigDecimal.ZERO) < 0) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, "m3 debe ser >= 0");
      }
    } catch (NumberFormatException ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "m3 invalido");
    }

    return new VehiculoUpsertCommand(
        placa,
        choferDefault,
        licencia,
        fechaCaducidadLicencia,
        parseTipoDocumento(tipoDocumento),
        documentoPersonal,
        tonelajeCategoria,
        m3,
        parseEstado(estado)
    );
  }

  private void validateVehiculo(Vehiculo vehiculo) {
    if (vehiculo.getPlacaNorm().isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Placa es obligatoria");
    }
    if (vehiculo.getChoferDefault() == null || vehiculo.getChoferDefault().isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Chofer es obligatorio");
    }
    if (vehiculo.getDocumentoPersonal() == null || vehiculo.getDocumentoPersonal().isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Documento personal es obligatorio");
    }
    if (vehiculo.getTonelajeCategoria() == null || vehiculo.getTonelajeCategoria().isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Tonelaje/Categoria es obligatorio");
    }
    if (vehiculo.getM3() == null || vehiculo.getM3().compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "m3 debe ser >= 0");
    }
    if (vehiculo.getTipoDocumento() == null) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Tipo de documento es obligatorio");
    }
    if (vehiculo.getEstado() == null) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Estado es obligatorio");
    }
    applyLicenseExpiryStatus(vehiculo);
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

  private TipoDocumento parseTipoDocumento(String value) {
    try {
      return TipoDocumento.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "tipo_documento invalido. Use CEDULA|RUC|PASAPORTE");
    }
  }

  private EstadoVehiculo parseEstado(String value) {
    try {
      return EstadoVehiculo.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "estado invalido. Use ACTIVO|INACTIVO");
    }
  }

  private String trimNullable(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private List<String> readHeader(Row row) {
    List<String> header = new ArrayList<>();
    for (int i = 0; i < HEADER_COLUMNS.size(); i++) {
      header.add(readCellAsString(row.getCell(i)).toLowerCase(Locale.ROOT));
    }
    return header;
  }

  private boolean rowHasContent(Row row) {
    for (int i = 0; i < HEADER_COLUMNS.size(); i++) {
      String value = readCellAsString(row.getCell(i));
      if (value != null && !value.isBlank()) {
        return true;
      }
    }
    return false;
  }

  private String readCellAsString(Cell cell) {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return "";
    }
    return DATA_FORMATTER.formatCellValue(cell).trim();
  }

  private LocalDate parseOptionalDate(Cell cell) {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return null;
    }
    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
      return cell.getLocalDateTimeCellValue().toLocalDate();
    }
    String value = readCellAsString(cell);
    if (value.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(value);
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "fecha_caducidad_licencia invalida. Use aaaa-mm-dd");
    }
  }

  private void applyLicenseExpiryStatus(Vehiculo vehiculo) {
    LocalDate fechaCaducidadLicencia = vehiculo.getFechaCaducidadLicencia();
    if (fechaCaducidadLicencia != null && !fechaCaducidadLicencia.isAfter(LocalDate.now())) {
      vehiculo.setEstado(EstadoVehiculo.INACTIVO);
    }
  }

  private void validateLicenseNotExpiredForActivation(Vehiculo vehiculo) {
    LocalDate fechaCaducidadLicencia = vehiculo.getFechaCaducidadLicencia();
    if (fechaCaducidadLicencia != null && !fechaCaducidadLicencia.isAfter(LocalDate.now())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede activar un vehiculo con licencia caducada");
    }
  }

  private void upsertArchivo(
      MultipartFile file,
      Vehiculo vehiculo,
      TipoArchivoVehiculo tipo,
      List<String> allowedContentTypes,
      List<String> allowedExtensions,
      String allowedHint) {
    validateFile(file, allowedContentTypes, allowedExtensions, allowedHint);
    try {
      Optional<VehiculoArchivo> existing = vehiculoArchivoRepositoryPort.findByVehiculoIdAndTipo(vehiculo.getId(), tipo);
      VehiculoArchivo archivo = VehiculoArchivo.builder()
          .id(existing.map(VehiculoArchivo::getId).orElse(UUID.randomUUID()))
          .vehiculoId(vehiculo.getId())
          .tipo(tipo)
          .fileName(file.getOriginalFilename() == null ? tipo.name().toLowerCase(Locale.ROOT) : file.getOriginalFilename())
          .contentType(resolveStoredContentType(file))
          .contenido(file.getBytes())
          .sizeBytes(file.getSize())
          .createdAt(existing.map(VehiculoArchivo::getCreatedAt).orElse(LocalDateTime.now()))
          .updatedAt(LocalDateTime.now())
          .build();
      vehiculoArchivoRepositoryPort.save(archivo);
    } catch (IOException ex) {
      throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar archivo");
    }
  }

  private void validateFile(
      MultipartFile file,
      List<String> allowedContentTypes,
      List<String> allowedExtensions,
      String allowedHint) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe seleccionar un archivo");
    }
    if (file.getSize() > maxImageBytes) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Archivo excede tamano maximo");
    }
    String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
    String extension = extractFileExtension(file.getOriginalFilename());
    boolean allowedByContentType = allowedContentTypes.contains(contentType);
    boolean allowedByExtension = !extension.isBlank() && allowedExtensions.contains(extension);
    if (!allowedByContentType && !allowedByExtension) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Tipo de archivo no permitido. " + allowedHint);
    }
  }

  private String resolveStoredContentType(MultipartFile file) {
    String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
    if (!contentType.isBlank() && !"application/octet-stream".equals(contentType)) {
      return contentType;
    }

    return switch (extractFileExtension(file.getOriginalFilename())) {
      case "pdf" -> "application/pdf";
      case "png" -> "image/png";
      case "jpg", "jpeg" -> "image/jpeg";
      case "webp" -> "image/webp";
      default -> "application/octet-stream";
    };
  }

  private String extractFileExtension(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      return "";
    }
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
      return "";
    }
    return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
  }

  private Resource readArchivo(UUID vehiculoId, TipoArchivoVehiculo tipo) {
    VehiculoArchivo archivo = vehiculoArchivoRepositoryPort.findByVehiculoIdAndTipo(vehiculoId, tipo)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Archivo no encontrado"));
    return new ByteArrayResource(archivo.getContenido());
  }

  private String toDataUrl(VehiculoArchivo archivo) {
    byte[] contenido = archivo.getContenido();
    if (contenido == null || contenido.length == 0) {
      return null;
    }
    String contentType = archivo.getContentType();
    if (contentType == null || contentType.isBlank()) {
      contentType = "application/octet-stream";
    }
    return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(contenido);
  }

  private Vehiculo getExisting(UUID id) {
    return vehiculoRepositoryPort.findById(id)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado"));
  }
}
