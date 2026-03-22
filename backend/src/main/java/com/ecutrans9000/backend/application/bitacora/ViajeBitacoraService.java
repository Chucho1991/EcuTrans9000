package com.ecutrans9000.backend.application.bitacora;

import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraUpsertRequest;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ViajeBitacoraJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ClienteJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ViajeBitacoraJpaRepository;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.service.AuditService;
import com.ecutrans9000.backend.service.BusinessException;
import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViajeBitacoraService {
  private static final int CHOFER_COLUMN_INDEX = 5;
  private static final int DESTINO_COLUMN_INDEX = 6;
  private static final int DETALLE_VIAJE_COLUMN_INDEX = 7;
  private static final int CLIENTE_COLUMN_INDEX = 8;
  private static final int MIN_TEXT_COLUMN_WIDTH = 12;
  private static final int MAX_TEXT_COLUMN_WIDTH = 80;

  private final ViajeBitacoraJpaRepository viajeRepository;
  private final VehiculoJpaRepository vehiculoRepository;
  private final ClienteJpaRepository clienteRepository;
  private final AuditService auditService;

  public ViajeBitacoraService(
      ViajeBitacoraJpaRepository viajeRepository,
      VehiculoJpaRepository vehiculoRepository,
      ClienteJpaRepository clienteRepository,
      AuditService auditService) {
    this.viajeRepository = viajeRepository;
    this.vehiculoRepository = vehiculoRepository;
    this.clienteRepository = clienteRepository;
    this.auditService = auditService;
  }

  @Transactional(readOnly = true)
  public Page<ViajeBitacoraResponse> list(
      int page,
      int size,
      String q,
      UUID vehiculoId,
      UUID clienteId,
      LocalDate fechaDesde,
      LocalDate fechaHasta,
      Boolean includeDeleted) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "fechaViaje", "numeroViaje"));
    validateDateRange(fechaDesde, fechaHasta);
    Specification<ViajeBitacoraJpaEntity> specification = buildSpecification(q, vehiculoId, clienteId, fechaDesde, fechaHasta, includeDeleted);
    return viajeRepository.findAll(specification, pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public ViajeBitacoraResponse getById(UUID id) {
    ViajeBitacoraJpaEntity entity = viajeRepository.findById(id)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));
    return toResponse(entity);
  }

  @Transactional
  public ViajeBitacoraResponse create(ViajeBitacoraUpsertRequest request) {
    validateForCreate(request);
    LocalDateTime now = LocalDateTime.now();
    ViajeBitacoraJpaEntity entity = ViajeBitacoraJpaEntity.builder()
        .id(UUID.randomUUID())
        .numeroViaje(request.getNumeroViaje())
        .fechaViaje(request.getFechaViaje())
        .vehiculoId(request.getVehiculoId())
        .clienteId(request.getClienteId())
        .destino(clean(request.getDestino()))
        .detalleViaje(clean(request.getDetalleViaje()))
        .valor(zeroIfNull(request.getValor()))
        .estiba(zeroIfNull(request.getEstiba()))
        .anticipo(zeroIfNull(request.getAnticipo()))
        .facturadoCliente(Boolean.TRUE.equals(request.getFacturadoCliente()))
        .numeroFactura(clean(request.getNumeroFactura()))
        .fechaFactura(request.getFechaFactura())
        .fechaPagoCliente(request.getFechaPagoCliente())
        .pagadoTransportista(Boolean.TRUE.equals(request.getPagadoTransportista()))
        .observaciones(clean(request.getObservaciones()))
        .deleted(false)
        .deletedAt(null)
        .deletedBy(null)
        .createdAt(now)
        .updatedAt(now)
        .build();
    return toResponse(viajeRepository.save(entity));
  }

  public void validateForCreate(ViajeBitacoraUpsertRequest request) {
    validate(request, null);
  }

  @Transactional
  public ViajeBitacoraResponse update(UUID id, ViajeBitacoraUpsertRequest request) {
    ViajeBitacoraJpaEntity entity = viajeRepository.findById(id)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));
    if (Boolean.TRUE.equals(entity.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede editar un viaje eliminado logicamente");
    }
    validate(request, id);
    entity.setNumeroViaje(request.getNumeroViaje());
    entity.setFechaViaje(request.getFechaViaje());
    entity.setVehiculoId(request.getVehiculoId());
    entity.setClienteId(request.getClienteId());
    entity.setDestino(clean(request.getDestino()));
    entity.setDetalleViaje(clean(request.getDetalleViaje()));
    entity.setValor(zeroIfNull(request.getValor()));
    entity.setEstiba(zeroIfNull(request.getEstiba()));
    entity.setAnticipo(zeroIfNull(request.getAnticipo()));
    entity.setFacturadoCliente(Boolean.TRUE.equals(request.getFacturadoCliente()));
    entity.setNumeroFactura(clean(request.getNumeroFactura()));
    entity.setFechaFactura(request.getFechaFactura());
    entity.setFechaPagoCliente(request.getFechaPagoCliente());
    entity.setPagadoTransportista(Boolean.TRUE.equals(request.getPagadoTransportista()));
    entity.setObservaciones(clean(request.getObservaciones()));
    entity.setUpdatedAt(LocalDateTime.now());
    return toResponse(viajeRepository.save(entity));
  }

  @Transactional
  public void softDelete(UUID id, String actorUsername, String actorRole) {
    ViajeBitacoraJpaEntity entity = viajeRepository.findById(id)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));
    if (Boolean.TRUE.equals(entity.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El viaje ya esta eliminado logicamente");
    }
    entity.setDeleted(true);
    entity.setDeletedAt(LocalDateTime.now());
    entity.setDeletedBy(actorUsername);
    entity.setUpdatedAt(LocalDateTime.now());
    viajeRepository.save(entity);
    auditService.saveActionAudit(actorUsername, actorRole, "BITACORA", ActionType.ELIMINADO_LOGICO, id.toString(), "viajes_bitacora");
  }

  @Transactional
  public ViajeBitacoraResponse restore(UUID id, String actorUsername, String actorRole) {
    ViajeBitacoraJpaEntity entity = viajeRepository.findById(id)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));
    if (!Boolean.TRUE.equals(entity.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El viaje no esta eliminado logicamente");
    }
    entity.setDeleted(false);
    entity.setDeletedAt(null);
    entity.setDeletedBy(null);
    entity.setUpdatedAt(LocalDateTime.now());
    ViajeBitacoraJpaEntity saved = viajeRepository.save(entity);
    auditService.saveActionAudit(actorUsername, actorRole, "BITACORA", ActionType.RESTAURACION, id.toString(), "viajes_bitacora");
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public byte[] exportExcel(String q, UUID vehiculoId, UUID clienteId, LocalDate fechaDesde, LocalDate fechaHasta) {
    validateDateRange(fechaDesde, fechaHasta);
    Specification<ViajeBitacoraJpaEntity> specification = buildSpecification(q, vehiculoId, clienteId, fechaDesde, fechaHasta, false);
    Sort sort = Sort.by(Sort.Direction.ASC, "numeroViaje");
    List<ViajeBitacoraResponse> viajes = viajeRepository.findAll(specification, sort).stream().map(this::toResponse).toList();
    int reportYear = resolveReportYear(fechaDesde, fechaHasta, viajes);

    try (
        var templateStream = new ClassPathResource("reports/bitacora-template.xlsx").getInputStream();
        XSSFWorkbook workbook = new XSSFWorkbook(templateStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      XSSFSheet sheet = workbook.getSheetAt(0);
      fillTemplateSheet(sheet, viajes, reportYear);
      workbook.write(outputStream);
      return outputStream.toByteArray();
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el reporte Excel");
    }
  }

  private Specification<ViajeBitacoraJpaEntity> buildSpecification(
      String q,
      UUID vehiculoId,
      UUID clienteId,
      LocalDate fechaDesde,
      LocalDate fechaHasta,
      Boolean includeDeleted) {
    return (root, query, cb) -> {
      ArrayList<Predicate> predicates = new ArrayList<>();
      if (!Boolean.TRUE.equals(includeDeleted)) {
        predicates.add(cb.isFalse(root.get("deleted")));
      }
      if (q != null && !q.isBlank()) {
        String like = "%" + q.trim().toLowerCase() + "%";
        Predicate numeroViaje = cb.like(cb.concat("", root.get("numeroViaje").as(String.class)), like);
        Predicate destino = cb.like(cb.lower(root.get("destino")), like);
        Predicate detalle = cb.like(cb.lower(cb.coalesce(root.get("detalleViaje"), "")), like);
        Predicate numeroFactura = cb.like(cb.lower(cb.coalesce(root.get("numeroFactura"), "")), like);
        predicates.add(cb.or(numeroViaje, destino, detalle, numeroFactura));
      }
      if (vehiculoId != null) {
        predicates.add(cb.equal(root.get("vehiculoId"), vehiculoId));
      }
      if (clienteId != null) {
        predicates.add(cb.equal(root.get("clienteId"), clienteId));
      }
      if (fechaDesde != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("fechaViaje"), fechaDesde));
      }
      if (fechaHasta != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("fechaViaje"), fechaHasta));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  private void fillTemplateSheet(XSSFSheet sheet, List<ViajeBitacoraResponse> viajes, int reportYear) {
    int titleRowIndex = findTitleRowIndex(sheet);
    int headerRowIndex = findHeaderRowIndex(sheet);
    int firstDataRowIndex = headerRowIndex + 1;
    int notesRowIndex = findNotesRowIndex(sheet);
    boolean hasNotesSection = notesRowIndex <= sheet.getLastRowNum();
    int lastSampleDataRowIndex = findLastSampleDataRowIndex(sheet, firstDataRowIndex, notesRowIndex);
    int templateDataRows = lastSampleDataRowIndex >= firstDataRowIndex ? (lastSampleDataRowIndex - firstDataRowIndex + 1) : 0;
    int delta = viajes.size() - templateDataRows;
    int finalNotesRowIndex = notesRowIndex;

    if (delta > 0 && hasNotesSection) {
      sheet.shiftRows(notesRowIndex, sheet.getLastRowNum(), delta, true, false);
      finalNotesRowIndex = notesRowIndex + delta;
    } else if (delta < 0) {
      removeRows(sheet, firstDataRowIndex + viajes.size(), lastSampleDataRowIndex);
      if (hasNotesSection) {
        sheet.shiftRows(notesRowIndex, sheet.getLastRowNum(), delta, true, false);
        finalNotesRowIndex = notesRowIndex + delta;
      }
    }

    updateTitle(sheet.getRow(titleRowIndex), reportYear);

    Row styleRow = sheet.getRow(firstDataRowIndex);
    int rowsToWrite = Math.max(viajes.size(), 1);
    for (int i = 0; i < rowsToWrite; i++) {
      Row row = sheet.getRow(firstDataRowIndex + i);
      if (row == null) {
        row = sheet.createRow(firstDataRowIndex + i);
      }
      copyRowFormatting(styleRow, row);
      if (i < viajes.size()) {
        writeViajeRow(row, viajes.get(i));
      } else {
        clearRowValues(row);
      }
    }

    applyLeftAlignmentToDataColumns(sheet, firstDataRowIndex, viajes.size());
    adjustDynamicTextColumnWidths(sheet, viajes);

    if (hasNotesSection) {
      removeRows(sheet, finalNotesRowIndex, sheet.getLastRowNum());
    }
  }

  private void applyLeftAlignmentToDataColumns(XSSFSheet sheet, int firstDataRowIndex, int totalRows) {
    if (totalRows <= 0) {
      return;
    }
    Map<CellStyle, CellStyle> leftAlignedStyles = new HashMap<>();
    for (int rowIndex = firstDataRowIndex; rowIndex < firstDataRowIndex + totalRows; rowIndex++) {
      Row row = sheet.getRow(rowIndex);
      if (row == null) {
        continue;
      }
      applyLeftAlignment(row, CHOFER_COLUMN_INDEX, leftAlignedStyles);
      applyLeftAlignment(row, DESTINO_COLUMN_INDEX, leftAlignedStyles);
      applyLeftAlignment(row, DETALLE_VIAJE_COLUMN_INDEX, leftAlignedStyles);
      applyLeftAlignment(row, CLIENTE_COLUMN_INDEX, leftAlignedStyles);
    }
  }

  private void applyLeftAlignment(Row row, int columnIndex, Map<CellStyle, CellStyle> leftAlignedStyles) {
    Cell cell = row.getCell(columnIndex);
    if (cell == null) {
      return;
    }
    CellStyle originalStyle = cell.getCellStyle();
    if (originalStyle == null) {
      return;
    }
    CellStyle leftAlignedStyle = leftAlignedStyles.computeIfAbsent(originalStyle, style -> {
      CellStyle clonedStyle = row.getSheet().getWorkbook().createCellStyle();
      clonedStyle.cloneStyleFrom(style);
      clonedStyle.setAlignment(HorizontalAlignment.LEFT);
      return clonedStyle;
    });
    cell.setCellStyle(leftAlignedStyle);
  }

  private void adjustDynamicTextColumnWidths(XSSFSheet sheet, List<ViajeBitacoraResponse> viajes) {
    setDynamicColumnWidth(sheet, CHOFER_COLUMN_INDEX, "Chofer", viajes.stream()
        .map(ViajeBitacoraResponse::getVehiculoChofer)
        .toList());
    setDynamicColumnWidth(sheet, DESTINO_COLUMN_INDEX, "Destino", viajes.stream()
        .map(ViajeBitacoraResponse::getDestino)
        .toList());
    setDynamicColumnWidth(sheet, DETALLE_VIAJE_COLUMN_INDEX, "Detalle viaje", viajes.stream()
        .map(ViajeBitacoraResponse::getDetalleViaje)
        .toList());
    setDynamicColumnWidth(sheet, CLIENTE_COLUMN_INDEX, "Cliente", viajes.stream()
        .map(this::preferredClientName)
        .toList());
  }

  private void setDynamicColumnWidth(XSSFSheet sheet, int columnIndex, String headerValue, List<String> values) {
    int maxLength = visibleLength(headerValue);
    for (String value : values) {
      maxLength = Math.max(maxLength, visibleLength(value));
    }
    int normalizedWidth = Math.max(MIN_TEXT_COLUMN_WIDTH, Math.min(maxLength + 2, MAX_TEXT_COLUMN_WIDTH));
    sheet.setColumnWidth(columnIndex, normalizedWidth * 256);
  }

  private int visibleLength(String value) {
    if (value == null || value.isBlank()) {
      return 0;
    }
    return value.trim().length();
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
          if (value != null && value.trim().toLowerCase().startsWith("observaci")) {
            return i;
          }
        }
      }
    }
    return sheet.getLastRowNum() + 1;
  }

  private int findTitleRowIndex(XSSFSheet sheet) {
    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (row == null) {
        continue;
      }
      for (int columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++) {
        Cell cell = row.getCell(columnIndex);
        if (cell != null && cell.getCellType() == CellType.STRING) {
          String value = cell.getStringCellValue();
          if (value != null && value.trim().toUpperCase().startsWith("BITACORA VIAJES")) {
            return i;
          }
        }
      }
    }
    return 0;
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
        if (value != null && value.trim().equalsIgnoreCase("Viaje")) {
          return i;
        }
      }
    }
    return 0;
  }

  private int findLastSampleDataRowIndex(XSSFSheet sheet, int firstDataRowIndex, int notesRowIndex) {
    int lastSampleDataRowIndex = firstDataRowIndex - 1;
    for (int i = firstDataRowIndex; i < notesRowIndex; i++) {
      Row row = sheet.getRow(i);
      if (row == null) {
        continue;
      }
      if (rowHasContent(row)) {
        lastSampleDataRowIndex = i;
      }
    }
    return lastSampleDataRowIndex;
  }

  private boolean rowHasContent(Row row) {
    for (int i = 0; i < 17; i++) {
      Cell cell = row.getCell(i);
      if (cell == null) {
        continue;
      }
      if (cell.getCellType() == CellType.BLANK) {
        continue;
      }
      String value = cell.toString();
      if (value != null && !value.isBlank()) {
        return true;
      }
    }
    return false;
  }

  private void updateTitle(Row row, int reportYear) {
    if (row == null) {
      return;
    }
    Cell cell = row.getCell(0);
    if (cell != null) {
      cell.setCellValue("BITACORA VIAJES " + reportYear);
    }
  }

  private int resolveReportYear(LocalDate fechaDesde, LocalDate fechaHasta, List<ViajeBitacoraResponse> viajes) {
    if (fechaDesde != null) {
      return fechaDesde.getYear();
    }
    if (fechaHasta != null) {
      return fechaHasta.getYear();
    }
    if (!viajes.isEmpty() && viajes.get(0).getFechaViaje() != null) {
      return viajes.get(0).getFechaViaje().getYear();
    }
    return LocalDate.now().getYear();
  }

  private void validateDateRange(LocalDate fechaDesde, LocalDate fechaHasta) {
    if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "La fecha desde no puede ser mayor a la fecha hasta");
    }
  }

  private void copyRowFormatting(Row source, Row target) {
    if (source == null || target == null) {
      return;
    }
    target.setHeight(source.getHeight());
    for (int i = 0; i < source.getLastCellNum(); i++) {
      Cell sourceCell = source.getCell(i);
      Cell targetCell = target.getCell(i);
      if (targetCell == null) {
        targetCell = target.createCell(i);
      }
      if (sourceCell != null) {
        targetCell.setCellStyle(sourceCell.getCellStyle());
      }
    }
  }

  private void writeViajeRow(Row row, ViajeBitacoraResponse viaje) {
    setCellValue(row, 0, viaje.getNumeroViaje());
    setCellValue(row, 1, viaje.getFechaViaje());
    setCellValue(row, 2, viaje.getVehiculoPlaca());
    setCellValue(row, 3, viaje.getVehiculoTonelajeCategoria());
    setCellValue(row, 4, viaje.getVehiculoM3());
    setCellValue(row, 5, viaje.getVehiculoChofer());
    setCellValue(row, 6, viaje.getDestino());
    setCellValue(row, 7, viaje.getDetalleViaje());
    setCellValue(row, 8, preferredClientName(viaje));
    setCellValue(row, 9, viaje.getValor());
    setCellValue(row, 10, viaje.getEstiba());
    setCellValue(row, 11, viaje.getAnticipo());
    setCellValue(row, 12, Boolean.TRUE.equals(viaje.getFacturadoCliente()) ? "a" : "r");
    setCellValue(row, 13, viaje.getNumeroFactura());
    setCellValue(row, 14, viaje.getFechaFactura());
    setCellValue(row, 15, viaje.getFechaPagoCliente());
    setCellValue(row, 16, Boolean.TRUE.equals(viaje.getPagadoTransportista()) ? "a" : "r");
  }

  private String preferredClientName(ViajeBitacoraResponse viaje) {
    if (viaje.getClienteNombreComercial() != null && !viaje.getClienteNombreComercial().isBlank()) {
      return viaje.getClienteNombreComercial();
    }
    return viaje.getClienteNombre();
  }

  private void setCellValue(Row row, int columnIndex, Object value) {
    Cell cell = row.getCell(columnIndex);
    if (cell == null) {
      cell = row.createCell(columnIndex);
    }
    if (value == null) {
      cell.setBlank();
      return;
    }
    if (value instanceof Number number) {
      cell.setCellValue(number.doubleValue());
      return;
    }
    if (value instanceof LocalDate localDate) {
      cell.setCellValue(localDate);
      return;
    }
    cell.setCellValue(String.valueOf(value));
  }

  private void clearRowValues(Row row) {
    if (row == null) {
      return;
    }
    for (int i = 0; i < row.getLastCellNum(); i++) {
      Cell cell = row.getCell(i);
      if (cell != null) {
        cell.setBlank();
      }
    }
  }

  private void removeRows(XSSFSheet sheet, int fromRow, int toRow) {
    for (int i = fromRow; i <= toRow; i++) {
      Row row = sheet.getRow(i);
      if (row != null) {
        sheet.removeRow(row);
      }
    }
  }

  private void validate(ViajeBitacoraUpsertRequest request, UUID existingId) {
    if (request.getNumeroViaje() == null || request.getNumeroViaje() <= 0) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Numero de viaje invalido");
    }
    boolean numeroExiste = existingId == null
        ? viajeRepository.existsByNumeroViaje(request.getNumeroViaje())
        : viajeRepository.existsByNumeroViajeAndIdNot(request.getNumeroViaje(), existingId);
    if (numeroExiste) {
      throw new BusinessException(HttpStatus.CONFLICT, "El numero de viaje ya existe");
    }
    if (request.getFechaViaje() == null) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Fecha de viaje es obligatoria");
    }
    if (request.getFechaFactura() != null && request.getFechaFactura().isBefore(request.getFechaViaje())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "La fecha de factura no puede ser anterior a la fecha del viaje");
    }
    if (request.getFechaPagoCliente() != null && request.getFechaPagoCliente().isBefore(request.getFechaViaje())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "La fecha de pago no puede ser anterior a la fecha del viaje");
    }
    validateAmount(request.getValor(), "Valor");
    validateAmount(request.getEstiba(), "Estiba");
    validateAmount(request.getAnticipo(), "Anticipo");

    VehiculoJpaEntity vehiculo = vehiculoRepository.findById(request.getVehiculoId())
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado"));
    if (Boolean.TRUE.equals(vehiculo.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede registrar viaje con vehiculo eliminado");
    }
    if (vehiculo.getEstado() == null || !"ACTIVO".equals(vehiculo.getEstado().name())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede registrar viaje con vehiculo inactivo");
    }

    ClienteJpaEntity cliente = clienteRepository.findById(request.getClienteId())
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    if (Boolean.TRUE.equals(cliente.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede registrar viaje con cliente eliminado");
    }
    if (!Boolean.TRUE.equals(cliente.getActivo())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede registrar viaje con cliente inactivo");
    }

    if (Boolean.TRUE.equals(request.getFacturadoCliente())) {
      if (clean(request.getNumeroFactura()) == null) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, "Numero de factura es obligatorio cuando el viaje esta facturado");
      }
      if (request.getFechaFactura() == null) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, "Fecha de factura es obligatoria cuando el viaje esta facturado");
      }
    }
  }

  private void validateAmount(BigDecimal amount, String label) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, label + " debe ser mayor o igual a 0");
    }
  }

  private ViajeBitacoraResponse toResponse(ViajeBitacoraJpaEntity entity) {
    VehiculoJpaEntity vehiculo = vehiculoRepository.findById(entity.getVehiculoId())
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehiculo relacionado no encontrado"));
    ClienteJpaEntity cliente = clienteRepository.findById(entity.getClienteId())
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cliente relacionado no encontrado"));

    return ViajeBitacoraResponse.builder()
        .id(entity.getId())
        .numeroViaje(entity.getNumeroViaje())
        .fechaViaje(entity.getFechaViaje())
        .vehiculoId(entity.getVehiculoId())
        .vehiculoPlaca(vehiculo.getPlaca())
        .vehiculoChofer(vehiculo.getChoferDefault())
        .vehiculoTonelajeCategoria(vehiculo.getTonelajeCategoria())
        .vehiculoM3(vehiculo.getM3())
        .clienteId(entity.getClienteId())
        .clienteNombre(cliente.getNombre())
        .clienteNombreComercial(cliente.getNombreComercial())
        .destino(entity.getDestino())
        .detalleViaje(entity.getDetalleViaje())
        .valor(entity.getValor())
        .estiba(entity.getEstiba())
        .anticipo(entity.getAnticipo())
        .facturadoCliente(entity.getFacturadoCliente())
        .numeroFactura(entity.getNumeroFactura())
        .fechaFactura(entity.getFechaFactura())
        .fechaPagoCliente(entity.getFechaPagoCliente())
        .pagadoTransportista(entity.getPagadoTransportista())
        .observaciones(entity.getObservaciones())
        .deleted(entity.getDeleted())
        .deletedAt(entity.getDeletedAt())
        .deletedBy(entity.getDeletedBy())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  private String clean(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  private BigDecimal zeroIfNull(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }
}
