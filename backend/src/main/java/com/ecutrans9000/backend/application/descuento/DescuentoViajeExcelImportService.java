package com.ecutrans9000.backend.application.descuento;

import com.ecutrans9000.backend.adapters.in.rest.dto.descuento.DescuentoViajeImportError;
import com.ecutrans9000.backend.adapters.in.rest.dto.descuento.DescuentoViajeImportResult;
import com.ecutrans9000.backend.adapters.in.rest.dto.descuento.DescuentoViajeUpsertRequest;
import com.ecutrans9000.backend.adapters.out.persistence.entity.DescuentoViajeJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.DescuentoViajeJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoJpaRepository;
import com.ecutrans9000.backend.application.usecase.vehiculo.ImportMode;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import com.ecutrans9000.backend.service.AuditService;
import com.ecutrans9000.backend.service.BusinessException;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio de aplicación para importación Excel del módulo descuentos de viajes.
 */
@Service
public class DescuentoViajeExcelImportService {

  private static final List<String> TEMPLATE_HEADERS = List.of(
      "placa",
      "descripcion motivo",
      "monto motivo",
      "activo"
  );

  private static final DataFormatter DATA_FORMATTER = new DataFormatter();

  private final DescuentoViajeService descuentoViajeService;
  private final DescuentoViajeJpaRepository descuentoRepository;
  private final VehiculoJpaRepository vehiculoRepository;
  private final AuditService auditService;
  private final TransactionTemplate transactionTemplate;

  public DescuentoViajeExcelImportService(
      DescuentoViajeService descuentoViajeService,
      DescuentoViajeJpaRepository descuentoRepository,
      VehiculoJpaRepository vehiculoRepository,
      AuditService auditService,
      TransactionTemplate transactionTemplate) {
    this.descuentoViajeService = descuentoViajeService;
    this.descuentoRepository = descuentoRepository;
    this.vehiculoRepository = vehiculoRepository;
    this.auditService = auditService;
    this.transactionTemplate = transactionTemplate;
  }

  @Transactional(readOnly = true)
  public byte[] downloadTemplate() {
    return buildTemplateWorkbook(false);
  }

  @Transactional(readOnly = true)
  public byte[] downloadExampleTemplate() {
    return buildTemplateWorkbook(true);
  }

  public DescuentoViajeImportResult previewExcel(MultipartFile file, ImportMode mode, boolean partialOk) {
    return processExcel(file, mode, partialOk, true, "SYSTEM", "SYSTEM");
  }

  public DescuentoViajeImportResult importExcel(MultipartFile file, ImportMode mode, boolean partialOk, String actorUsername, String actorRole) {
    return processExcel(file, mode, partialOk, false, actorUsername, actorRole);
  }

  private byte[] buildTemplateWorkbook(boolean includeExampleRow) {
    try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      XSSFSheet sheet = workbook.createSheet("Descuentos Viajes Import");
      CellStyle textColumnStyle = createTextColumnStyle(workbook);
      sheet.setDefaultColumnStyle(0, textColumnStyle);
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(displayHeader(TEMPLATE_HEADERS.get(i)));
        sheet.setColumnWidth(i, 24 * 256);
      }
      if (includeExampleRow) {
        Row exampleRow = sheet.createRow(1);
        Cell placaCell = exampleRow.createCell(0);
        placaCell.setCellStyle(textColumnStyle);
        placaCell.setCellValue("ABC-1234");
        exampleRow.createCell(1).setCellValue("DESCUENTO POR PEAJE");
        exampleRow.createCell(2).setCellValue(15.50);
        exampleRow.createCell(3).setCellValue("SI");
      }
      workbook.write(outputStream);
      return outputStream.toByteArray();
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo obtener la plantilla Excel");
    }
  }

  private DescuentoViajeImportResult processExcel(
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
    List<DescuentoViajeImportError> errors = new ArrayList<>();
    List<ImportRowPayload> validRows = new ArrayList<>();

    try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
      XSSFSheet sheet = workbook.getSheetAt(0);
      int headerRowIndex = findHeaderRowIndex(sheet);
      validateHeaderRow(sheet.getRow(headerRowIndex));
      Map<String, VehiculoJpaEntity> vehiculosLookup = buildVehiculosLookup();

      for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
        Row row = sheet.getRow(rowIndex);
        if (row == null || !rowHasContent(row)) {
          continue;
        }
        totalRows++;
        try {
          DescuentoViajeUpsertRequest request = parseImportRow(row, vehiculosLookup);
          DescuentoViajeJpaEntity existing = findExisting(request);
          if (mode == ImportMode.INSERT_ONLY && existing != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ya existe un descuento para la placa y motivo indicados");
          }
          if (mode == ImportMode.INSERT_ONLY) {
            descuentoViajeService.validateForCreate(request);
          }
          if (existing == null) {
            inserted++;
          } else {
            updated++;
          }
          processed++;
          if (!previewOnly) {
            validRows.add(new ImportRowPayload(request, existing != null && mode == ImportMode.UPSERT));
          }
        } catch (BusinessException ex) {
          skipped++;
          errors.add(new DescuentoViajeImportError(rowIndex + 1, "row", ex.getMessage()));
          if (!partialOk) {
            break;
          }
        } catch (Exception ex) {
          skipped++;
          errors.add(new DescuentoViajeImportError(rowIndex + 1, "row", "Error inesperado: " + ex.getMessage()));
          if (!partialOk) {
            break;
          }
        }
      }
    } catch (BusinessException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo Excel");
    }

    if (!previewOnly) {
      persistValidRows(validRows, errors, partialOk, actorUsername, actorRole);
      if ((inserted > 0 || updated > 0) && (partialOk || errors.isEmpty())) {
        auditService.saveActionAudit(actorUsername, actorRole, "DESCUENTOS_VIAJES", ActionType.IMPORT_CSV, "N/A", "descuentos_viajes");
      }
    }

    return DescuentoViajeImportResult.builder()
        .totalRows(totalRows)
        .processed(processed)
        .inserted(inserted)
        .updated(updated)
        .skipped(skipped)
        .errorsCount(errors.size())
        .errors(errors)
        .build();
  }

  private void persistValidRows(
      List<ImportRowPayload> validRows,
      List<DescuentoViajeImportError> errors,
      boolean partialOk,
      String actorUsername,
      String actorRole) {
    if (validRows.isEmpty()) {
      return;
    }
    if (!partialOk && !errors.isEmpty()) {
      return;
    }
    Runnable execution = () -> validRows.forEach(payload -> {
      if (payload.updateExisting()) {
        DescuentoViajeJpaEntity existing = findExisting(payload.request());
        if (existing == null) {
          descuentoViajeService.create(payload.request(), actorUsername, actorRole);
        } else {
          if (Boolean.TRUE.equals(existing.getDeleted())) {
            descuentoViajeService.restore(existing.getId(), actorUsername, actorRole);
          }
          descuentoViajeService.update(existing.getId(), payload.request(), actorUsername, actorRole);
        }
      } else {
        descuentoViajeService.create(payload.request(), actorUsername, actorRole);
      }
    });
    if (partialOk) {
      execution.run();
    } else {
      transactionTemplate.executeWithoutResult(status -> execution.run());
    }
  }

  private DescuentoViajeJpaEntity findExisting(DescuentoViajeUpsertRequest request) {
    return descuentoRepository.findByVehiculoIdAndDescripcionMotivoNorm(
        request.getVehiculoId(),
        normalizeText(request.getDescripcionMotivo())).orElse(null);
  }

  private Map<String, VehiculoJpaEntity> buildVehiculosLookup() {
    Map<String, VehiculoJpaEntity> result = new HashMap<>();
    for (VehiculoJpaEntity vehiculo : vehiculoRepository.findAll()) {
      result.put(Vehiculo.normalizePlaca(vehiculo.getPlaca()), vehiculo);
    }
    return result;
  }

  private DescuentoViajeUpsertRequest parseImportRow(Row row, Map<String, VehiculoJpaEntity> vehiculosLookup) {
    String placa = requireText(row.getCell(0), "Placa");
    String descripcionMotivo = requireText(row.getCell(1), "Descripcion motivo");
    BigDecimal montoMotivo = parseRequiredDecimalCell(row.getCell(2), "Monto motivo");
    boolean activo = parseBooleanCell(row.getCell(3));
    VehiculoJpaEntity vehiculo = vehiculosLookup.get(Vehiculo.normalizePlaca(placa));
    if (vehiculo == null) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Vehiculo no encontrado para placa: " + placa);
    }
    return DescuentoViajeUpsertRequest.builder()
        .vehiculoId(vehiculo.getId())
        .descripcionMotivo(descripcionMotivo)
        .montoMotivo(montoMotivo)
        .activo(activo)
        .build();
  }

  private void validateHeaderRow(Row row) {
    if (row == null) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se encontro la fila de encabezados");
    }
    List<String> header = new ArrayList<>();
    for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
      header.add(normalizeText(readCellAsString(row.getCell(i))));
    }
    if (!header.equals(TEMPLATE_HEADERS)) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "La plantilla Excel no tiene el formato esperado");
    }
  }

  private int findHeaderRowIndex(XSSFSheet sheet) {
    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (row == null) {
        continue;
      }
      Cell cell = row.getCell(0);
      if (cell != null && cell.getCellType() == CellType.STRING) {
        String value = cell.getStringCellValue();
        if (value != null && normalizeText(value).equals("placa")) {
          return i;
        }
      }
    }
    throw new BusinessException(HttpStatus.BAD_REQUEST, "No se encontro la fila de encabezados");
  }

  private boolean rowHasContent(Row row) {
    for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
      Cell cell = row.getCell(i);
      if (cell == null || cell.getCellType() == CellType.BLANK) {
        continue;
      }
      String value = cell.toString();
      if (value != null && !value.isBlank()) {
        return true;
      }
    }
    return false;
  }

  private String requireText(Cell cell, String field) {
    String value = readCellAsString(cell);
    if (value == null || value.isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, field + " es obligatorio");
    }
    return value.trim();
  }

  private BigDecimal parseRequiredDecimalCell(Cell cell, String field) {
    String value = readCellAsString(cell);
    if (value == null || value.isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, field + " es obligatorio");
    }
    try {
      return new BigDecimal(value.replace(",", "."));
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, field + " invalido");
    }
  }

  private boolean parseBooleanCell(Cell cell) {
    String value = readCellAsString(cell);
    if (value == null || value.isBlank()) {
      return true;
    }
    return switch (normalizeText(value)) {
      case "a", "si", "s", "true", "1", "x", "ok", "activo" -> true;
      case "r", "no", "n", "false", "0", "inactivo" -> false;
      default -> throw new BusinessException(HttpStatus.BAD_REQUEST, "Activo invalido");
    };
  }

  private String readCellAsString(Cell cell) {
    if (cell == null) {
      return null;
    }
    String value = DATA_FORMATTER.formatCellValue(cell);
    return value == null ? null : value.trim();
  }

  private CellStyle createTextColumnStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.cloneStyleFrom(workbook.createCellStyle());
    style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("@"));
    style.setFillPattern(FillPatternType.NO_FILL);
    return style;
  }

  private String displayHeader(String normalizedHeader) {
    return switch (normalizedHeader) {
      case "placa" -> "Placa";
      case "descripcion motivo" -> "Descripcion motivo";
      case "monto motivo" -> "Monto motivo";
      case "activo" -> "Activo";
      default -> normalizedHeader;
    };
  }

  private String normalizeText(String value) {
    if (value == null) {
      return "";
    }
    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .replace('\n', ' ')
        .replace('\r', ' ')
        .replaceAll("\\s+", " ")
        .trim()
        .toLowerCase(Locale.ROOT);
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

  private record ImportRowPayload(DescuentoViajeUpsertRequest request, boolean updateExisting) {
  }
}
