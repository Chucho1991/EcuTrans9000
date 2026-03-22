package com.ecutrans9000.backend.application.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecutrans9000.backend.application.usecase.cliente.ClienteEquivalenciaUpsertCommand;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;
import java.math.BigDecimal;
import com.ecutrans9000.backend.ports.out.cliente.ClienteRepositoryPort;
import com.ecutrans9000.backend.service.AuditService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Pruebas unitarias de la carga de logos del módulo clientes.
 */
@ExtendWith(MockitoExtension.class)
class ClienteApplicationServiceTest {

  @Mock
  private ClienteRepositoryPort clienteRepositoryPort;

  @Mock
  private AuditService auditService;

  @InjectMocks
  private ClienteApplicationService clienteApplicationService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(clienteApplicationService, "maxLogoBytes", 5_242_880L);
    when(clienteRepositoryPort.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void uploadLogoShouldAcceptPngWithGenericContentType() {
    UUID clienteId = UUID.randomUUID();
    when(clienteRepositoryPort.findById(clienteId)).thenReturn(Optional.of(buildCliente(clienteId)));

    MultipartFile file = new MockMultipartFile(
        "file",
        "logo.PNG",
        "application/octet-stream",
        "png-data".getBytes());

    Cliente result = clienteApplicationService.uploadLogo(clienteId, file, "admin", "ROLE_SUPERADMINISTRADOR");

    verify(auditService).saveActionAudit(
        eq("admin"),
        eq("ROLE_SUPERADMINISTRADOR"),
        eq("CLIENTES"),
        eq(ActionType.EDICION),
        eq(clienteId.toString()),
        eq("clientes"));

    assertNotNull(result);
    assertEquals("logo.PNG", result.getLogoFileName());
    assertEquals("image/png", result.getLogoContentType());
  }

  @Test
  void replaceEquivalenciasShouldStoreRowsAndEnableFlag() {
    UUID clienteId = UUID.randomUUID();
    when(clienteRepositoryPort.findById(clienteId)).thenReturn(Optional.of(buildCliente(clienteId)));

    Cliente result = clienteApplicationService.replaceEquivalencias(
        clienteId,
        List.of(new ClienteEquivalenciaUpsertCommand(null, "GUAYAQUIL", new BigDecimal("120.50"), new BigDecimal("75.00"))),
        "admin",
        "ROLE_SUPERADMINISTRADOR");

    assertTrue(Boolean.TRUE.equals(result.getAplicaTablaEquivalencia()));
    assertEquals(1, result.getEquivalencias().size());
    assertEquals("GUAYAQUIL", result.getEquivalencias().get(0).getDestino());
    assertEquals(new BigDecimal("120.50"), result.getEquivalencias().get(0).getValorDestino());

    verify(auditService).saveActionAudit(
        eq("admin"),
        eq("ROLE_SUPERADMINISTRADOR"),
        eq("CLIENTES"),
        eq(ActionType.EDICION),
        eq(clienteId.toString()),
        eq("clientes"));
  }

  @Test
  void importEquivalenciasExcelShouldParseWorkbook() {
    UUID clienteId = UUID.randomUUID();
    when(clienteRepositoryPort.findById(clienteId)).thenReturn(Optional.of(buildCliente(clienteId)));

    MultipartFile file = new MockMultipartFile(
        "file",
        "equivalencias.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        buildEquivalenciasWorkbook());

    Cliente result = clienteApplicationService.importEquivalenciasExcel(
        clienteId,
        file,
        "admin",
        "ROLE_SUPERADMINISTRADOR");

    assertTrue(Boolean.TRUE.equals(result.getAplicaTablaEquivalencia()));
    assertEquals(1, result.getEquivalencias().size());
    assertEquals("QUITO", result.getEquivalencias().get(0).getDestino());
    assertEquals(new BigDecimal("150"), result.getEquivalencias().get(0).getValorDestino());
    assertEquals(new BigDecimal("90"), result.getEquivalencias().get(0).getCostoChofer());
  }

  private Cliente buildCliente(UUID id) {
    return Cliente.builder()
        .id(id)
        .tipoDocumento(TipoDocumentoCliente.RUC)
        .documento("1799999999001")
        .documentoNorm("1799999999001")
        .nombre("Cliente Demo")
        .activo(true)
        .deleted(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  private byte[] buildEquivalenciasWorkbook() {
    try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
         var output = new java.io.ByteArrayOutputStream()) {
      var sheet = workbook.createSheet("Tabla");
      var header = sheet.createRow(0);
      header.createCell(0).setCellValue("DESTINO");
      header.createCell(1).setCellValue("VALOR DESTINO");
      header.createCell(2).setCellValue("COSTO CHOFER");
      var row = sheet.createRow(1);
      row.createCell(0).setCellValue("QUITO");
      row.createCell(1).setCellValue(150);
      row.createCell(2).setCellValue(90);
      workbook.write(output);
      return output.toByteArray();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
