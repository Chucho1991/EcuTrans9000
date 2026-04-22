package com.ecutrans9000.backend.application.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecutrans9000.backend.application.bitacora.ViajeBitacoraPendingValidationService;
import com.ecutrans9000.backend.application.usecase.cliente.ClienteEquivalenciaUpsertCommand;
import com.ecutrans9000.backend.application.usecase.cliente.ClienteUpsertCommand;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import com.ecutrans9000.backend.domain.cliente.ClienteEquivalencia;
import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;
import com.ecutrans9000.backend.ports.out.cliente.ClienteRepositoryPort;
import com.ecutrans9000.backend.service.AuditService;
import com.ecutrans9000.backend.service.BusinessException;
import java.math.BigDecimal;
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

  @Mock
  private ViajeBitacoraPendingValidationService viajeBitacoraPendingValidationService;

  @InjectMocks
  private ClienteApplicationService clienteApplicationService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(clienteApplicationService, "maxLogoBytes", 5_242_880L);
    lenient().when(clienteRepositoryPort.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
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

  @Test
  void importEquivalenciasExcelShouldUpdateDuplicatedDestinationsAndPreserveOthers() {
    UUID clienteId = UUID.randomUUID();
    UUID guayaquilId = UUID.randomUUID();
    UUID mantaId = UUID.randomUUID();
    when(clienteRepositoryPort.findById(clienteId)).thenReturn(Optional.of(buildClienteWithEquivalencias(
        clienteId,
        List.of(
            buildEquivalencia(guayaquilId, "GUAYAQUIL", "100", "60"),
            buildEquivalencia(mantaId, "MANTA", "80", "45")))));

    MultipartFile file = new MockMultipartFile(
        "file",
        "equivalencias.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        buildEquivalenciasWorkbook(
            List.of(
                new Object[]{"guayaquil", 125, 77},
                new Object[]{"GUAYAQUIL", 130, 82},
                new Object[]{"QUITO", 150, 90})));

    Cliente result = clienteApplicationService.importEquivalenciasExcel(
        clienteId,
        file,
        "admin",
        "ROLE_SUPERADMINISTRADOR");

    assertTrue(Boolean.TRUE.equals(result.getAplicaTablaEquivalencia()));
    assertEquals(3, result.getEquivalencias().size());

    ClienteEquivalencia updatedGuayaquil = result.getEquivalencias().stream()
        .filter(item -> "GUAYAQUIL".equalsIgnoreCase(item.getDestino()))
        .findFirst()
        .orElseThrow();
    assertEquals(guayaquilId, updatedGuayaquil.getId());
    assertEquals(new BigDecimal("130"), updatedGuayaquil.getValorDestino());
    assertEquals(new BigDecimal("82"), updatedGuayaquil.getCostoChofer());

    ClienteEquivalencia preservedManta = result.getEquivalencias().stream()
        .filter(item -> "MANTA".equalsIgnoreCase(item.getDestino()))
        .findFirst()
        .orElseThrow();
    assertEquals(mantaId, preservedManta.getId());
    assertEquals(new BigDecimal("80"), preservedManta.getValorDestino());
    assertEquals(new BigDecimal("45"), preservedManta.getCostoChofer());

    ClienteEquivalencia insertedQuito = result.getEquivalencias().stream()
        .filter(item -> "QUITO".equalsIgnoreCase(item.getDestino()))
        .findFirst()
        .orElseThrow();
    assertEquals(new BigDecimal("150"), insertedQuito.getValorDestino());
    assertEquals(new BigDecimal("90"), insertedQuito.getCostoChofer());
  }

  @Test
  void toggleActivoShouldRejectWhenClienteHasPendingTrips() {
    UUID clienteId = UUID.randomUUID();
    when(clienteRepositoryPort.findById(clienteId)).thenReturn(Optional.of(buildCliente(clienteId)));
    when(viajeBitacoraPendingValidationService.clienteTieneViajesPendientes(clienteId)).thenReturn(true);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> clienteApplicationService.toggleActivo(clienteId, "admin", "ROLE_SUPERADMINISTRADOR"));

    assertEquals("No se puede desactivar el cliente porque tiene viajes con estados pendientes asociados", exception.getMessage());
    verify(clienteRepositoryPort, never()).save(any(Cliente.class));
  }

  @Test
  void updateShouldRejectWhenTryingToDeactivateClienteWithPendingTrips() {
    UUID clienteId = UUID.randomUUID();
    when(clienteRepositoryPort.findById(clienteId)).thenReturn(Optional.of(buildCliente(clienteId)));
    when(viajeBitacoraPendingValidationService.clienteTieneViajesPendientes(clienteId)).thenReturn(true);

    ClienteUpsertCommand command = new ClienteUpsertCommand(
        TipoDocumentoCliente.RUC,
        "1799999999001",
        "Cliente Demo",
        null,
        "Direccion",
        "Descripcion",
        false,
        false);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> clienteApplicationService.update(clienteId, command, "admin", "ROLE_SUPERADMINISTRADOR"));

    assertEquals("No se puede desactivar el cliente porque tiene viajes con estados pendientes asociados", exception.getMessage());
    verify(clienteRepositoryPort, never()).save(any(Cliente.class));
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

  private Cliente buildClienteWithEquivalencias(UUID id, List<ClienteEquivalencia> equivalencias) {
    Cliente cliente = buildCliente(id);
    cliente.setAplicaTablaEquivalencia(true);
    cliente.setEquivalencias(equivalencias);
    return cliente;
  }

  private ClienteEquivalencia buildEquivalencia(UUID id, String destino, String valorDestino, String costoChofer) {
    LocalDateTime now = LocalDateTime.now();
    return ClienteEquivalencia.builder()
        .id(id)
        .destino(destino)
        .valorDestino(new BigDecimal(valorDestino))
        .costoChofer(new BigDecimal(costoChofer))
        .createdAt(now.minusDays(1))
        .updatedAt(now.minusHours(1))
        .build();
  }

  private byte[] buildEquivalenciasWorkbook() {
    return buildEquivalenciasWorkbook(List.<Object[]>of(new Object[]{"QUITO", 150, 90}));
  }

  private byte[] buildEquivalenciasWorkbook(List<Object[]> rows) {
    try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
         var output = new java.io.ByteArrayOutputStream()) {
      var sheet = workbook.createSheet("Tabla");
      var header = sheet.createRow(0);
      header.createCell(0).setCellValue("DESTINO");
      header.createCell(1).setCellValue("VALOR DESTINO");
      header.createCell(2).setCellValue("COSTO CHOFER");
      for (int index = 0; index < rows.size(); index++) {
        Object[] values = rows.get(index);
        var row = sheet.createRow(index + 1);
        row.createCell(0).setCellValue(String.valueOf(values[0]));
        row.createCell(1).setCellValue(((Number) values[1]).doubleValue());
        row.createCell(2).setCellValue(((Number) values[2]).doubleValue());
      }
      workbook.write(output);
      return output.toByteArray();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
