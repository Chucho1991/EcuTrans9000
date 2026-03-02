package com.ecutrans9000.backend.application.placas;

import com.ecutrans9000.backend.adapters.in.rest.dto.placas.ConsultaPlacaDetalleResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.placas.ConsultaPlacaResponse;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ViajeBitacoraJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ClienteJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ViajeBitacoraJpaRepository;
import com.ecutrans9000.backend.domain.vehiculo.Vehiculo;
import com.ecutrans9000.backend.service.BusinessException;
import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para consolidar información de bitácora por placa.
 */
@Service
public class ConsultaPlacasService {

  private static final BigDecimal ONE_PERCENT = new BigDecimal("0.01");
  private static final BigDecimal SIX_PERCENT = new BigDecimal("0.06");
  private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
  private static final byte[] BRAND_BLUE = new byte[]{31, 61, 114};

  private final ViajeBitacoraJpaRepository viajeRepository;
  private final VehiculoJpaRepository vehiculoRepository;
  private final ClienteJpaRepository clienteRepository;

  public ConsultaPlacasService(
      ViajeBitacoraJpaRepository viajeRepository,
      VehiculoJpaRepository vehiculoRepository,
      ClienteJpaRepository clienteRepository) {
    this.viajeRepository = viajeRepository;
    this.vehiculoRepository = vehiculoRepository;
    this.clienteRepository = clienteRepository;
  }

  @Transactional(readOnly = true)
  public ConsultaPlacaResponse consultar(String placa, LocalDate fechaDesde, LocalDate fechaHasta) {
    validateDateRange(fechaDesde, fechaHasta);

    Optional<VehiculoJpaEntity> vehiculo = resolveVehiculo(placa);
    List<ViajeBitacoraJpaEntity> viajes = loadViajes(vehiculo.orElse(null), fechaDesde, fechaHasta);
    return buildResponse(placa, vehiculo.orElse(null), fechaDesde, fechaHasta, viajes);
  }

  @Transactional(readOnly = true)
  public byte[] exportExcel(String placa, LocalDate fechaDesde, LocalDate fechaHasta) {
    ConsultaPlacaResponse response = consultar(placa, fechaDesde, fechaHasta);

    try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      XSSFSheet sheet = workbook.createSheet("Consulta Placas");
      sheet.setDisplayGridlines(false);
      configureColumns(sheet);

      Styles styles = createStyles(workbook);
      addLogo(workbook, sheet);
      writeHeaderBlock(sheet, styles, response);
      writeTable(sheet, styles, response.getRegistros());
      writeSummary(sheet, styles, response, 8 + response.getRegistros().size() + 2);

      workbook.write(outputStream);
      return outputStream.toByteArray();
    } catch (IOException ex) {
      throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el reporte Excel");
    }
  }

  private Optional<VehiculoJpaEntity> resolveVehiculo(String placa) {
    if (placa == null || placa.isBlank()) {
      return Optional.empty();
    }
    return vehiculoRepository.findByPlacaNorm(Vehiculo.normalizePlaca(placa));
  }

  private List<ViajeBitacoraJpaEntity> loadViajes(VehiculoJpaEntity vehiculo, LocalDate fechaDesde, LocalDate fechaHasta) {
    if (vehiculo == null && fechaDesde == null && fechaHasta == null) {
      return List.of();
    }
    Specification<ViajeBitacoraJpaEntity> specification = buildSpecification(vehiculo, fechaDesde, fechaHasta);
    return viajeRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "fechaViaje", "numeroViaje"));
  }

  private Specification<ViajeBitacoraJpaEntity> buildSpecification(
      VehiculoJpaEntity vehiculo,
      LocalDate fechaDesde,
      LocalDate fechaHasta) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.isFalse(root.get("deleted")));
      if (vehiculo != null) {
        predicates.add(cb.equal(root.get("vehiculoId"), vehiculo.getId()));
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

  private ConsultaPlacaResponse buildResponse(
      String placaFilter,
      VehiculoJpaEntity vehiculo,
      LocalDate fechaDesde,
      LocalDate fechaHasta,
      List<ViajeBitacoraJpaEntity> viajes) {
    List<ConsultaPlacaDetalleResponse> registros = viajes.stream()
        .map(this::toDetalleResponse)
        .toList();

    BigDecimal valorFacturaTotal = scale(sum(viajes, ViajeBitacoraJpaEntity::getValor));
    BigDecimal retencion = scale(valorFacturaTotal.multiply(ONE_PERCENT));
    BigDecimal comision = scale(valorFacturaTotal.multiply(SIX_PERCENT));
    BigDecimal anticipos = scale(sum(viajes, ViajeBitacoraJpaEntity::getAnticipo));
    BigDecimal pagoTotal = scale(valorFacturaTotal.subtract(retencion).subtract(comision).subtract(anticipos));

    return ConsultaPlacaResponse.builder()
        .placa(vehiculo != null ? vehiculo.getPlaca() : cleanText(placaFilter))
        .chofer(vehiculo != null ? vehiculo.getChoferDefault() : null)
        .fechaDesde(fechaDesde)
        .fechaHasta(fechaHasta)
        .registros(registros)
        .valorFacturaTotal(valorFacturaTotal)
        .retencionUnoPorciento(retencion)
        .comisionAdministrativaSeisPorciento(comision)
        .anticiposTotal(anticipos)
        .pagoTotal(pagoTotal)
        .build();
  }

  private ConsultaPlacaDetalleResponse toDetalleResponse(ViajeBitacoraJpaEntity viaje) {
    ClienteJpaEntity cliente = clienteRepository.findById(viaje.getClienteId())
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cliente relacionado no encontrado"));

    return ConsultaPlacaDetalleResponse.builder()
        .ordenCompra(String.valueOf(viaje.getNumeroViaje()))
        .valor(scale(viaje.getValor()))
        .fecha(viaje.getFechaViaje())
        .factura(cleanTextOrDash(viaje.getNumeroFactura()))
        .anticipo(scale(viaje.getAnticipo()))
        .estiba(scale(viaje.getEstiba()))
        .despacho(cleanTextOrDash(viaje.getDetalleViaje()))
        .cliente(preferredClientName(cliente))
        .origenDestino(cleanTextOrDash(viaje.getDestino()))
        .build();
  }

  private String preferredClientName(ClienteJpaEntity cliente) {
    if (cliente.getNombreComercial() != null && !cliente.getNombreComercial().isBlank()) {
      return cliente.getNombreComercial().trim();
    }
    return cliente.getNombre();
  }

  private void writeHeaderBlock(XSSFSheet sheet, Styles styles, ConsultaPlacaResponse response) {
    sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 8));
    Row driverRow = getOrCreateRow(sheet, 1);
    driverRow.setHeightInPoints(21);
    Cell driverCell = driverRow.createCell(1);
    driverCell.setCellValue(cleanTextOrDash(response.getChofer()));
    driverCell.setCellStyle(styles.title);

    sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 8));
    Row plateRow = getOrCreateRow(sheet, 2);
    plateRow.setHeightInPoints(21);
    Cell plateCell = plateRow.createCell(1);
    plateCell.setCellValue(cleanTextOrDash(response.getPlaca()));
    plateCell.setCellStyle(styles.subtitle);

    String period = buildPeriodLabel(response.getFechaDesde(), response.getFechaHasta());
    if (period != null) {
      sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 8));
      Row periodRow = getOrCreateRow(sheet, 3);
      periodRow.setHeightInPoints(18);
      Cell periodCell = periodRow.createCell(1);
      periodCell.setCellValue(period);
      periodCell.setCellStyle(styles.meta);
    }
  }

  private void writeTable(XSSFSheet sheet, Styles styles, List<ConsultaPlacaDetalleResponse> registros) {
    int headerRowIndex = 6;
    Row header = getOrCreateRow(sheet, headerRowIndex);
    String[] columns = {
        "Orden de compra",
        "Valor",
        "Fecha",
        "Factura",
        "Anticipos",
        "Estiba",
        "Despacho",
        "Cliente",
        "Origen - Destino"
    };
    for (int i = 0; i < columns.length; i++) {
      Cell cell = header.createCell(i);
      cell.setCellValue(columns[i]);
      cell.setCellStyle(styles.tableHeader);
    }

    int rowIndex = headerRowIndex + 1;
    for (ConsultaPlacaDetalleResponse registro : registros) {
      Row row = getOrCreateRow(sheet, rowIndex++);
      writeTextCell(row, 0, registro.getOrdenCompra(), styles.textCell);
      writeMoneyCell(row, 1, registro.getValor(), styles.moneyCell);
      writeTextCell(row, 2, formatDate(registro.getFecha()), styles.centerCell);
      writeTextCell(row, 3, registro.getFactura(), styles.centerCell);
      writeMoneyCell(row, 4, registro.getAnticipo(), styles.moneyCell);
      writeMoneyCell(row, 5, registro.getEstiba(), styles.moneyCell);
      writeTextCell(row, 6, registro.getDespacho(), styles.textCell);
      writeTextCell(row, 7, registro.getCliente(), styles.textCell);
      writeTextCell(row, 8, registro.getOrigenDestino(), styles.textCell);
    }

    if (registros.isEmpty()) {
      Row emptyRow = getOrCreateRow(sheet, rowIndex);
      for (int i = 0; i < columns.length; i++) {
        Cell cell = emptyRow.createCell(i);
        cell.setCellStyle(styles.emptyCell);
      }
    }

    sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, headerRowIndex, 0, columns.length - 1));
  }

  private void writeSummary(XSSFSheet sheet, Styles styles, ConsultaPlacaResponse response, int startRowIndex) {
    writeSummaryRow(sheet, startRowIndex, "Valor Factura", response.getValorFacturaTotal(), styles, false);
    writeSummaryRow(sheet, startRowIndex + 1, "Retencion 1%", response.getRetencionUnoPorciento(), styles, false);
    writeSummaryRow(sheet, startRowIndex + 2, "Ecutran comision", response.getComisionAdministrativaSeisPorciento(), styles, false);
    writeSummaryRow(sheet, startRowIndex + 3, "Anticipos", response.getAnticiposTotal(), styles, false);
    writeSummaryRow(sheet, startRowIndex + 4, "Pago Total", response.getPagoTotal(), styles, true);
  }

  private void writeSummaryRow(
      XSSFSheet sheet,
      int rowIndex,
      String label,
      BigDecimal amount,
      Styles styles,
      boolean emphasize) {
    Row row = getOrCreateRow(sheet, rowIndex);
    writeTextCell(row, 0, label, emphasize ? styles.summaryLabelStrong : styles.summaryLabel);
    writeMoneyCell(row, 1, amount, emphasize ? styles.summaryValueStrong : styles.summaryValue);
  }

  private void addLogo(XSSFWorkbook workbook, XSSFSheet sheet) throws IOException {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("reports/ecutrans-logo.png")) {
      if (inputStream == null) {
        return;
      }
      byte[] imageBytes = inputStream.readAllBytes();
      int pictureIndex = workbook.addPicture(imageBytes, XSSFWorkbook.PICTURE_TYPE_PNG);
      CreationHelper helper = workbook.getCreationHelper();
      var drawing = sheet.createDrawingPatriarch();
      ClientAnchor anchor = helper.createClientAnchor();
      anchor.setCol1(0);
      anchor.setRow1(1);
      anchor.setCol2(1);
      anchor.setRow2(4);
      XSSFPicture picture = (XSSFPicture) drawing.createPicture(anchor, pictureIndex);
      picture.resize(0.95, 0.95);
    }
  }

  private Styles createStyles(XSSFWorkbook workbook) {
    XSSFFont titleFont = workbook.createFont();
    titleFont.setFontName("Aptos");
    titleFont.setFontHeightInPoints((short) 14);
    titleFont.setBold(true);

    XSSFFont subtitleFont = workbook.createFont();
    subtitleFont.setFontName("Aptos");
    subtitleFont.setFontHeightInPoints((short) 12);
    subtitleFont.setBold(true);

    XSSFFont metaFont = workbook.createFont();
    metaFont.setFontName("Aptos");
    metaFont.setFontHeightInPoints((short) 10);

    XSSFFont headerFont = workbook.createFont();
    headerFont.setFontName("Aptos");
    headerFont.setFontHeightInPoints((short) 11);
    headerFont.setBold(true);
    headerFont.setColor(IndexedColors.WHITE.getIndex());

    XSSFFont strongFont = workbook.createFont();
    strongFont.setFontName("Aptos");
    strongFont.setFontHeightInPoints((short) 11);
    strongFont.setBold(true);

    XSSFCellStyle title = workbook.createCellStyle();
    title.setFont(titleFont);
    title.setAlignment(HorizontalAlignment.LEFT);

    XSSFCellStyle subtitle = workbook.createCellStyle();
    subtitle.setFont(subtitleFont);
    subtitle.setAlignment(HorizontalAlignment.LEFT);

    XSSFCellStyle meta = workbook.createCellStyle();
    meta.setFont(metaFont);
    meta.setAlignment(HorizontalAlignment.LEFT);

    XSSFCellStyle tableHeader = borderedStyle(workbook, headerFont, BRAND_BLUE, HorizontalAlignment.CENTER);
    tableHeader.setVerticalAlignment(VerticalAlignment.CENTER);
    tableHeader.setWrapText(true);

    XSSFCellStyle textCell = borderedStyle(workbook, null, null, HorizontalAlignment.LEFT);
    XSSFCellStyle centerCell = borderedStyle(workbook, null, null, HorizontalAlignment.CENTER);
    XSSFCellStyle moneyCell = borderedStyle(workbook, null, null, HorizontalAlignment.RIGHT);
    moneyCell.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

    XSSFCellStyle emptyCell = borderedStyle(workbook, null, null, HorizontalAlignment.LEFT);
    XSSFCellStyle summaryLabel = workbook.createCellStyle();
    summaryLabel.setAlignment(HorizontalAlignment.LEFT);
    summaryLabel.setFont(metaFont);

    XSSFCellStyle summaryValue = workbook.createCellStyle();
    summaryValue.setAlignment(HorizontalAlignment.RIGHT);
    summaryValue.setFont(metaFont);
    summaryValue.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

    XSSFCellStyle summaryLabelStrong = workbook.createCellStyle();
    summaryLabelStrong.cloneStyleFrom(summaryLabel);
    summaryLabelStrong.setFont(strongFont);

    XSSFCellStyle summaryValueStrong = workbook.createCellStyle();
    summaryValueStrong.cloneStyleFrom(summaryValue);
    summaryValueStrong.setFont(strongFont);
    summaryValueStrong.setBorderTop(BorderStyle.THIN);

    return new Styles(
        title,
        subtitle,
        meta,
        tableHeader,
        textCell,
        centerCell,
        moneyCell,
        emptyCell,
        summaryLabel,
        summaryValue,
        summaryLabelStrong,
        summaryValueStrong
    );
  }

  private XSSFCellStyle borderedStyle(
      XSSFWorkbook workbook,
      XSSFFont font,
      byte[] fillColor,
      HorizontalAlignment alignment) {
    XSSFCellStyle style = workbook.createCellStyle();
    style.setAlignment(alignment);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    if (fillColor != null) {
      style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      style.setFillForegroundColor(new XSSFColor(fillColor, null));
    }
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    if (font != null) {
      style.setFont(font);
    }
    return style;
  }

  private void configureColumns(XSSFSheet sheet) {
    int[] widths = {20, 15, 16, 14, 14, 14, 16, 24, 30, 10, 10};
    for (int i = 0; i < widths.length; i++) {
      sheet.setColumnWidth(i, widths[i] * 256);
    }
  }

  private void writeTextCell(Row row, int columnIndex, String value, CellStyle style) {
    Cell cell = row.createCell(columnIndex);
    cell.setCellValue(value == null || value.isBlank() ? "-" : value);
    cell.setCellStyle(style);
  }

  private void writeMoneyCell(Row row, int columnIndex, BigDecimal value, CellStyle style) {
    Cell cell = row.createCell(columnIndex);
    cell.setCellValue(scale(value).doubleValue());
    cell.setCellStyle(style);
  }

  private Row getOrCreateRow(XSSFSheet sheet, int rowIndex) {
    Row row = sheet.getRow(rowIndex);
    return row != null ? row : sheet.createRow(rowIndex);
  }

  private String buildPeriodLabel(LocalDate fechaDesde, LocalDate fechaHasta) {
    if (fechaDesde == null && fechaHasta == null) {
      return null;
    }
    String desde = fechaDesde == null ? "-" : formatDate(fechaDesde);
    String hasta = fechaHasta == null ? "-" : formatDate(fechaHasta);
    return "Periodo: " + desde + " a " + hasta;
  }

  private String formatDate(LocalDate value) {
    return value == null ? "-" : DISPLAY_DATE_FORMATTER.format(value);
  }

  private void validateDateRange(LocalDate fechaDesde, LocalDate fechaHasta) {
    if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "La fecha desde no puede ser mayor a la fecha hasta");
    }
  }

  private BigDecimal sum(List<ViajeBitacoraJpaEntity> viajes, java.util.function.Function<ViajeBitacoraJpaEntity, BigDecimal> mapper) {
    return viajes.stream()
        .map(mapper)
        .filter(value -> value != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal scale(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  private String cleanTextOrDash(String value) {
    String clean = cleanText(value);
    return clean == null ? "-" : clean;
  }

  private String cleanText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  private record Styles(
      XSSFCellStyle title,
      XSSFCellStyle subtitle,
      XSSFCellStyle meta,
      XSSFCellStyle tableHeader,
      XSSFCellStyle textCell,
      XSSFCellStyle centerCell,
      XSSFCellStyle moneyCell,
      XSSFCellStyle emptyCell,
      XSSFCellStyle summaryLabel,
      XSSFCellStyle summaryValue,
      XSSFCellStyle summaryLabelStrong,
      XSSFCellStyle summaryValueStrong) {
  }
}
