package com.ecutrans9000.backend.application.bitacora;

import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraImportError;
import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraImportResult;
import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraUpsertRequest;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ClienteJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ViajeBitacoraJpaRepository;
import com.ecutrans9000.backend.application.usecase.vehiculo.ImportMode;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import com.ecutrans9000.backend.service.AuditService;
import com.ecutrans9000.backend.service.BusinessException;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ViajeBitacoraExcelImportService {

  private static final List<String> TEMPLATE_HEADERS = List.of(
      "fecha viaje",
      "placa",
      "destino",
      "detalle viaje",
      "documento cliente",
      "valor",
      "estiba",
      "anticipo",
      "pagado cliente",
      "n° factura",
      "fecha factura",
      "fecha pago cliente a ecutrans",
      "pagado transportista"
  );

  private static final DataFormatter DATA_FORMATTER = new DataFormatter();

  private final ViajeBitacoraService viajeBitacoraService;
  private final ViajeBitacoraJpaRepository viajeRepository;
  private final VehiculoJpaRepository vehiculoRepository;
  private final ClienteJpaRepository clienteRepository;
  private final AuditService auditService;

  public ViajeBitacoraExcelImportService(
      ViajeBitacoraService viajeBitacoraService,
      ViajeBitacoraJpaRepository viajeRepository,
      VehiculoJpaRepository vehiculoRepository,
      ClienteJpaRepository clienteRepository,
      AuditService auditService) {
    this.viajeBitacoraService = viajeBitacoraService;
    this.viajeRepository = viajeRepository;
    this.vehiculoRepository = vehiculoRepository;
    this.clienteRepository = clienteRepository;
    this.auditService = auditService;
  }

  @Transactional(readOnly = true)
  public byte[] downloadTemplate() {
    return buildTemplateWorkbook(false);
  }

  @Transactional(readOnly = true)
  public byte[] downloadExampleTemplate() {
    return buildTemplateWorkbook(true);
  }

  private byte[] buildTemplateWorkbook(boolean includeExampleRow) {
    try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      XSSFSheet sheet = workbook.createSheet("Bitacora Import");
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(displayHeader(TEMPLATE_HEADERS.get(i)));
        sheet.setColumnWidth(i, 18 * 256);
      }
      if (includeExampleRow) {
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("02/03/2026");
        exampleRow.createCell(1).setCellValue("ABC-1234");
        exampleRow.createCell(2).setCellValue("UIO - GYE");
        exampleRow.createCell(3).setCellValue("ENTREGA PROGRAMADA");
        exampleRow.createCell(4).setCellValue("0999999999001");
        exampleRow.createCell(5).setCellValue(150.00);
        exampleRow.createCell(6).setCellValue(15.00);
        exampleRow.createCell(7).setCellValue(25.00);
        exampleRow.createCell(8).setCellValue("SI");
        exampleRow.createCell(9).setCellValue("FAC-001");
        exampleRow.createCell(10).setCellValue("03/03/2026");
        exampleRow.createCell(11).setCellValue("05/03/2026");
        exampleRow.createCell(12).setCellValue("NO");
      }
      workbook.write(outputStream);
      return outputStream.toByteArray();
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo obtener la plantilla Excel");
    }
  }

  @Transactional
  public ViajeBitacoraImportResult previewExcel(MultipartFile file, ImportMode mode, boolean partialOk) {
    return processExcel(file, mode, partialOk, true, "SYSTEM", "SYSTEM");
  }

  @Transactional
  public ViajeBitacoraImportResult importExcel(MultipartFile file, ImportMode mode, boolean partialOk, String actorUsername, String actorRole) {
    ViajeBitacoraImportResult result = processExcel(file, mode, partialOk, false, actorUsername, actorRole);
    if (!partialOk && result.getErrorsCount() > 0) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
    if (result.getProcessed() > 0) {
      auditService.saveActionAudit(actorUsername, actorRole, "BITACORA", ActionType.IMPORT_CSV, "N/A", "viajes_bitacora");
    }
    return result;
  }

  private ViajeBitacoraImportResult processExcel(
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
    List<ViajeBitacoraImportError> errors = new ArrayList<>();

    try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
      XSSFSheet sheet = workbook.getSheetAt(0);
      int headerRowIndex = findHeaderRowIndex(sheet);
      int notesRowIndex = findNotesRowIndex(sheet);
      int nextNumeroViaje = nextNumeroViaje();

      validateHeaderRow(sheet.getRow(headerRowIndex));
      Map<String, ClienteJpaEntity> clientesLookup = buildClientesLookup();

      for (int rowIndex = headerRowIndex + 1; rowIndex < notesRowIndex; rowIndex++) {
        Row row = sheet.getRow(rowIndex);
        if (row == null || !rowHasContent(row)) {
          continue;
        }

        totalRows++;
        try {
          ViajeBitacoraUpsertRequest request = parseImportRow(row, clientesLookup, nextNumeroViaje);

          if (previewOnly) {
            viajeBitacoraService.create(request);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
          } else {
            viajeBitacoraService.create(request);
          }
          inserted++;
          processed++;
          nextNumeroViaje++;
        } catch (BusinessException ex) {
          skipped++;
          errors.add(new ViajeBitacoraImportError(rowIndex + 1, "row", ex.getMessage()));
          if (!partialOk) {
            break;
          }
        } catch (Exception ex) {
          skipped++;
          errors.add(new ViajeBitacoraImportError(rowIndex + 1, "row", "Error inesperado: " + ex.getMessage()));
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

    return ViajeBitacoraImportResult.builder()
        .totalRows(totalRows)
        .processed(processed)
        .inserted(inserted)
        .updated(updated)
        .skipped(skipped)
        .errorsCount(errors.size())
        .errors(errors)
        .build();
  }

  private void validateHeaderRow(Row row) {
    if (row == null) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se encontro la fila de encabezados");
    }
    List<String> header = new ArrayList<>();
    for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
      header.add(normalizeHeader(readCellAsString(row.getCell(i))));
    }
    if (!headerMatches(header)) {
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
        if (value != null && value.trim().equalsIgnoreCase("Fecha viaje")) {
          return i;
        }
      }
    }
    throw new BusinessException(HttpStatus.BAD_REQUEST, "No se encontro la fila de encabezados");
  }

  private int findNotesRowIndex(XSSFSheet sheet) {
    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (row == null) {
        continue;
      }
      for (int columnIndex = 0; columnIndex < Math.max(row.getLastCellNum(), 3); columnIndex++) {
        Cell cell = row.getCell(columnIndex);
        if (cell != null && cell.getCellType() == CellType.STRING) {
          String value = cell.getStringCellValue();
          if (value != null && value.trim().toLowerCase(Locale.ROOT).startsWith("observaci")) {
            return i;
          }
        }
      }
    }
    return sheet.getLastRowNum() + 1;
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

  private ViajeBitacoraUpsertRequest parseImportRow(Row row, Map<String, ClienteJpaEntity> clientesLookup, int numeroViaje) {
    LocalDate fechaViaje = parseDateCell(row.getCell(0), "Fecha viaje");
    String placa = requireText(row.getCell(1), "Placa");
    String destino = requireText(row.getCell(2), "Destino");
    String detalleViaje = detailOrNull(readCellAsString(row.getCell(3)));
    String clienteDocumento = requireText(row.getCell(4), "Documento cliente");
    BigDecimal valor = parseDecimalCell(row.getCell(5), "Valor");
    BigDecimal estiba = parseDecimalCell(row.getCell(6), "Estiba");
    BigDecimal anticipo = parseDecimalCell(row.getCell(7), "Anticipo");
    boolean facturadoCliente = parseBooleanCell(row.getCell(8));
    String numeroFactura = detailOrNull(readCellAsString(row.getCell(9)));
    LocalDate fechaFactura = parseOptionalDateCell(row.getCell(10));
    LocalDate fechaPagoCliente = parseOptionalDateCell(row.getCell(11));
    boolean pagadoTransportista = parseBooleanCell(row.getCell(12));

    VehiculoJpaEntity vehiculo = vehiculoRepository.findByPlacaNorm(normalizePlate(placa))
        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Vehiculo no encontrado para placa: " + placa));
    ClienteJpaEntity cliente = clientesLookup.get(normalizeText(clienteDocumento));
    if (cliente == null) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Cliente no encontrado para documento: " + clienteDocumento);
    }

    return ViajeBitacoraUpsertRequest.builder()
        .numeroViaje(numeroViaje)
        .fechaViaje(fechaViaje)
        .vehiculoId(vehiculo.getId())
        .clienteId(cliente.getId())
        .destino(destino)
        .detalleViaje(detalleViaje)
        .valor(valor)
        .estiba(estiba)
        .anticipo(anticipo)
        .facturadoCliente(facturadoCliente)
        .numeroFactura(numeroFactura)
        .fechaFactura(fechaFactura)
        .fechaPagoCliente(fechaPagoCliente)
        .pagadoTransportista(pagadoTransportista)
        .observaciones(null)
        .build();
  }

  private int nextNumeroViaje() {
    return viajeRepository.findTopByOrderByNumeroViajeDesc()
        .map(viaje -> viaje.getNumeroViaje() + 1)
        .orElse(1);
  }

  private Map<String, ClienteJpaEntity> buildClientesLookup() {
    Map<String, ClienteJpaEntity> result = new HashMap<>();
    for (ClienteJpaEntity cliente : clienteRepository.findAllByDeletedFalse()) {
      result.put(normalizeText(cliente.getDocumento()), cliente);
    }
    return result;
  }

  private BigDecimal parseDecimalCell(Cell cell, String field) {
    String value = readCellAsString(cell);
    if (value == null || value.isBlank()) {
      return BigDecimal.ZERO;
    }
    try {
      return new BigDecimal(value.replace(",", "."));
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, field + " invalido");
    }
  }

  private LocalDate parseDateCell(Cell cell, String field) {
    LocalDate value = parseOptionalDateCell(cell);
    if (value == null) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, field + " es obligatoria");
    }
    return value;
  }

  private LocalDate parseOptionalDateCell(Cell cell) {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return null;
    }
    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
      return cell.getLocalDateTimeCellValue().toLocalDate();
    }
    String value = readCellAsString(cell);
    if (value == null || value.isBlank()) {
      return null;
    }
    for (DateTimeFormatter formatter : List.of(
        DateTimeFormatter.ofPattern("d/M/uuuu"),
        DateTimeFormatter.ofPattern("d/M/uu"),
        DateTimeFormatter.ofPattern("dd/MM/uuuu"),
        DateTimeFormatter.ISO_LOCAL_DATE)) {
      try {
        return LocalDate.parse(value, formatter);
      } catch (Exception ignored) {
      }
    }
    throw new BusinessException(HttpStatus.BAD_REQUEST, "Fecha invalida: " + value);
  }

  private boolean parseBooleanCell(Cell cell) {
    String value = readCellAsString(cell);
    if (value == null || value.isBlank()) {
      return false;
    }
    return switch (normalizeText(value)) {
      case "a", "si", "s", "true", "1", "x", "ok" -> true;
      case "r", "no", "n", "false", "0" -> false;
      default -> false;
    };
  }

  private String requireText(Cell cell, String field) {
    String value = readCellAsString(cell);
    if (value == null || value.isBlank()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, field + " es obligatorio");
    }
    return value.trim();
  }

  private String readCellAsString(Cell cell) {
    if (cell == null) {
      return null;
    }
    String value = DATA_FORMATTER.formatCellValue(cell);
    return value == null ? null : value.trim();
  }

  private String normalizeHeader(String value) {
    return normalizeText(value)
        .replace("n factura", "n° factura")
        .replace("facturado cliente", "pagado cliente");
  }

  private boolean headerMatches(List<String> header) {
    return header.equals(TEMPLATE_HEADERS);
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

  private String normalizePlate(String placa) {
    return Vehiculo.normalizePlaca(placa);
  }

  private String detailOrNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private String displayHeader(String normalizedHeader) {
    return switch (normalizedHeader) {
      case "fecha viaje" -> "Fecha viaje";
      case "placa" -> "Placa";
      case "destino" -> "Destino";
      case "detalle viaje" -> "Detalle viaje";
      case "documento cliente" -> "Documento cliente";
      case "valor" -> "Valor";
      case "estiba" -> "Estiba";
      case "anticipo" -> "Anticipo";
      case "pagado cliente" -> "Pagado cliente";
      case "n° factura" -> "N° Factura";
      case "fecha factura" -> "Fecha factura";
      case "fecha pago cliente a ecutrans" -> "Fecha pago cliente a Ecutrans";
      case "pagado transportista" -> "Pagado transportista";
      default -> normalizedHeader;
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
}
