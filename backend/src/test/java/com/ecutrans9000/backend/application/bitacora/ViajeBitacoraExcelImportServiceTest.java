package com.ecutrans9000.backend.application.bitacora;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraImportResult;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ClienteJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ViajeBitacoraJpaRepository;
import com.ecutrans9000.backend.application.usecase.vehiculo.ImportMode;
import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;
import com.ecutrans9000.backend.domain.vehiculo.EstadoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.TipoDocumento;
import com.ecutrans9000.backend.service.AuditService;
import com.ecutrans9000.backend.service.BusinessException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Pruebas unitarias del preview de importación Excel del módulo bitácora.
 */
@ExtendWith(MockitoExtension.class)
class ViajeBitacoraExcelImportServiceTest {

  @Mock
  private ViajeBitacoraService viajeBitacoraService;

  @Mock
  private ViajeBitacoraJpaRepository viajeBitacoraJpaRepository;

  @Mock
  private VehiculoJpaRepository vehiculoJpaRepository;

  @Mock
  private ClienteJpaRepository clienteJpaRepository;

  @Mock
  private AuditService auditService;

  @Mock
  private TransactionTemplate transactionTemplate;

  @InjectMocks
  private ViajeBitacoraExcelImportService viajaBitacoraExcelImportService;

  @Test
  void previewExcelShouldValidateRowsWithoutPersistingTrips() throws IOException {
    UUID vehiculoId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();

    when(viajeBitacoraJpaRepository.findTopByOrderByNumeroViajeDesc()).thenReturn(Optional.empty());
    when(vehiculoJpaRepository.findByPlacaNorm("ABC-1234")).thenReturn(Optional.of(buildVehiculo(vehiculoId)));
    when(clienteJpaRepository.findAllByDeletedFalse()).thenReturn(List.of(buildCliente(clienteId)));

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "bitacora.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        buildWorkbook());

    ViajeBitacoraImportResult result =
        viajaBitacoraExcelImportService.previewExcel(file, ImportMode.INSERT_ONLY, true);

    assertEquals(1, result.getTotalRows());
    assertEquals(1, result.getProcessed());
    assertEquals(1, result.getInserted());
    assertEquals(0, result.getErrorsCount());
    verify(viajeBitacoraService).validateForCreate(any());
    verify(viajeBitacoraService, never()).create(any());
  }

  @Test
  void importExcelShouldReturnErrorsWithoutPersistingWhenPartialOkIsFalse() throws IOException {
    when(viajeBitacoraJpaRepository.findTopByOrderByNumeroViajeDesc()).thenReturn(Optional.empty());
    when(clienteJpaRepository.findAllByDeletedFalse()).thenReturn(List.of());

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "bitacora.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        buildWorkbook());

    ViajeBitacoraImportResult result =
        viajaBitacoraExcelImportService.importExcel(file, ImportMode.INSERT_ONLY, false, "admin", "ROLE_SUPERADMINISTRADOR");

    assertEquals(1, result.getTotalRows());
    assertEquals(0, result.getProcessed());
    assertEquals(1, result.getSkipped());
    assertEquals(1, result.getErrorsCount());
    verify(viajeBitacoraService, never()).create(any());
    verify(auditService, never()).saveActionAudit(any(), any(), any(), any(), any(), any());
  }

  @Test
  void previewExcelShouldReturnValidationErrorsWithoutRollbackException() throws IOException {
    UUID vehiculoId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();

    when(viajeBitacoraJpaRepository.findTopByOrderByNumeroViajeDesc()).thenReturn(Optional.empty());
    when(vehiculoJpaRepository.findByPlacaNorm("ABC-1234")).thenReturn(Optional.of(buildVehiculo(vehiculoId)));
    when(clienteJpaRepository.findAllByDeletedFalse()).thenReturn(List.of(buildCliente(clienteId)));
    doThrow(new BusinessException(HttpStatus.BAD_REQUEST, "Cliente no encontrado"))
        .when(viajeBitacoraService)
        .validateForCreate(any());

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "bitacora.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        buildWorkbook());

    ViajeBitacoraImportResult result =
        viajaBitacoraExcelImportService.previewExcel(file, ImportMode.INSERT_ONLY, true);

    assertEquals(1, result.getTotalRows());
    assertEquals(0, result.getProcessed());
    assertEquals(1, result.getSkipped());
    assertEquals(1, result.getErrorsCount());
    verify(viajeBitacoraService, never()).create(any());
  }

  @Test
  void downloadExampleTemplateShouldFormatDocumentoClienteColumnAsText() throws IOException {
    byte[] template = viajaBitacoraExcelImportService.downloadExampleTemplate();

    try (XSSFWorkbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(template))) {
      var sheet = workbook.getSheetAt(0);
      assertEquals("@", sheet.getColumnStyle(4).getDataFormatString());
      assertEquals("@", sheet.getRow(1).getCell(4).getCellStyle().getDataFormatString());
      assertEquals("0999999999001", sheet.getRow(1).getCell(4).getStringCellValue());
    }
  }

  private byte[] buildWorkbook() throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      var sheet = workbook.createSheet("Bitacora Import");
      var header = sheet.createRow(0);
      header.createCell(0).setCellValue("Fecha viaje");
      header.createCell(1).setCellValue("Placa");
      header.createCell(2).setCellValue("Destino");
      header.createCell(3).setCellValue("Detalle viaje");
      header.createCell(4).setCellValue("Documento cliente");
      header.createCell(5).setCellValue("Valor");
      header.createCell(6).setCellValue("Estiba");
      header.createCell(7).setCellValue("Anticipo");
      header.createCell(8).setCellValue("Pagado cliente");
      header.createCell(9).setCellValue("N° Factura");
      header.createCell(10).setCellValue("Fecha factura");
      header.createCell(11).setCellValue("Fecha pago cliente a Ecutrans");
      header.createCell(12).setCellValue("Pagado transportista");

      var row = sheet.createRow(1);
      row.createCell(0).setCellValue("02/03/2026");
      row.createCell(1).setCellValue("ABC-1234");
      row.createCell(2).setCellValue("UIO - GYE");
      row.createCell(3).setCellValue("ENTREGA PROGRAMADA");
      row.createCell(4).setCellValue("0999999999001");
      row.createCell(5).setCellValue(150d);
      row.createCell(6).setCellValue(15d);
      row.createCell(7).setCellValue(25d);
      row.createCell(8).setCellValue("SI");
      row.createCell(9).setCellValue("FAC-001");
      row.createCell(10).setCellValue("03/03/2026");
      row.createCell(11).setCellValue("05/03/2026");
      row.createCell(12).setCellValue("NO");

      workbook.write(outputStream);
      return outputStream.toByteArray();
    }
  }

  private VehiculoJpaEntity buildVehiculo(UUID id) {
    return VehiculoJpaEntity.builder()
        .id(id)
        .placa("ABC-1234")
        .placaNorm("ABC-1234")
        .choferDefault("Chofer Demo")
        .licencia("LIC-001")
        .tipoDocumento(TipoDocumento.CEDULA)
        .documentoPersonal("0102030405")
        .tonelajeCategoria("PESADO")
        .m3(BigDecimal.TEN)
        .estado(EstadoVehiculo.ACTIVO)
        .deleted(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  private ClienteJpaEntity buildCliente(UUID id) {
    return ClienteJpaEntity.builder()
        .id(id)
        .tipoDocumento(TipoDocumentoCliente.RUC)
        .documento("0999999999001")
        .documentoNorm("0999999999001")
        .nombre("Cliente Demo")
        .activo(true)
        .deleted(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
