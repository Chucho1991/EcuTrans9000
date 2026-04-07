package com.ecutrans9000.backend.application.vehiculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecutrans9000.backend.application.bitacora.ViajeBitacoraPendingValidationService;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Pruebas unitarias de la carga de archivos del módulo vehículos.
 */
@ExtendWith(MockitoExtension.class)
class VehiculoApplicationServiceTest {

  @Mock
  private VehiculoRepositoryPort vehiculoRepositoryPort;

  @Mock
  private VehiculoArchivoRepositoryPort vehiculoArchivoRepositoryPort;

  @Mock
  private AuditService auditService;

  @Mock
  private ViajeBitacoraPendingValidationService viajeBitacoraPendingValidationService;

  @InjectMocks
  private VehiculoApplicationService vehiculoApplicationService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(vehiculoApplicationService, "maxImageBytes", 5_242_880L);
    lenient().when(vehiculoRepositoryPort.save(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));
    lenient().when(vehiculoArchivoRepositoryPort.save(any(VehiculoArchivo.class))).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void uploadDocumentoShouldAcceptPdfWithGenericContentType() {
    UUID vehiculoId = UUID.randomUUID();
    when(vehiculoRepositoryPort.findById(vehiculoId)).thenReturn(Optional.of(buildVehiculo(vehiculoId)));
    when(vehiculoArchivoRepositoryPort.findByVehiculoIdAndTipo(vehiculoId, TipoArchivoVehiculo.DOCUMENTO))
        .thenReturn(Optional.empty());

    MultipartFile file = new MockMultipartFile(
        "file",
        "matricula.PDF",
        "application/octet-stream",
        "pdf-data".getBytes());

    Vehiculo result = vehiculoApplicationService.uploadDocumento(vehiculoId, file, "admin", "ROLE_SUPERADMINISTRADOR");

    ArgumentCaptor<VehiculoArchivo> archivoCaptor = ArgumentCaptor.forClass(VehiculoArchivo.class);
    verify(vehiculoArchivoRepositoryPort).save(archivoCaptor.capture());
    verify(auditService).saveActionAudit(
        eq("admin"),
        eq("ROLE_SUPERADMINISTRADOR"),
        eq("VEHICULOS"),
        eq(ActionType.EDICION),
        eq(vehiculoId.toString()),
        eq("vehiculos"));

    VehiculoArchivo archivoGuardado = archivoCaptor.getValue();
    assertNotNull(result);
    assertEquals("matricula.PDF", result.getDocPath());
    assertEquals("matricula.PDF", archivoGuardado.getFileName());
    assertEquals("application/pdf", archivoGuardado.getContentType());
  }

  @Test
  void deactivateShouldRejectWhenChoferHasPendingTrips() {
    UUID vehiculoId = UUID.randomUUID();
    when(vehiculoRepositoryPort.findById(vehiculoId)).thenReturn(Optional.of(buildVehiculo(vehiculoId)));
    when(viajeBitacoraPendingValidationService.vehiculoTieneViajesPendientes(vehiculoId)).thenReturn(true);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> vehiculoApplicationService.deactivate(vehiculoId, "admin", "ROLE_SUPERADMINISTRADOR"));

    assertEquals("No se puede inactivar el chofer porque tiene viajes con estados pendientes asociados", exception.getMessage());
    verify(vehiculoRepositoryPort, never()).save(any(Vehiculo.class));
  }

  @Test
  void updateShouldRejectWhenTryingToDeactivateChoferWithPendingTrips() {
    UUID vehiculoId = UUID.randomUUID();
    when(vehiculoRepositoryPort.findById(vehiculoId)).thenReturn(Optional.of(buildVehiculo(vehiculoId)));
    when(viajeBitacoraPendingValidationService.vehiculoTieneViajesPendientes(vehiculoId)).thenReturn(true);

    VehiculoUpsertCommand command = new VehiculoUpsertCommand(
        "ABC-123",
        "Chofer",
        "LIC-1",
        null,
        TipoDocumento.CEDULA,
        "0102030405",
        "Banco Pichincha - 1234567890",
        "PESADO",
        BigDecimal.ONE,
        EstadoVehiculo.INACTIVO);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> vehiculoApplicationService.update(vehiculoId, command, "admin", "ROLE_SUPERADMINISTRADOR"));

    assertEquals("No se puede inactivar el chofer porque tiene viajes con estados pendientes asociados", exception.getMessage());
    verify(vehiculoRepositoryPort, never()).save(any(Vehiculo.class));
  }

  private Vehiculo buildVehiculo(UUID id) {
    return Vehiculo.builder()
        .id(id)
        .placa("ABC-123")
        .placaNorm("ABC-123")
        .choferDefault("Chofer")
        .licencia("LIC-1")
        .tipoDocumento(TipoDocumento.CEDULA)
        .documentoPersonal("0102030405")
        .cuentaBancaria("Banco Pichincha - 1234567890")
        .tonelajeCategoria("PESADO")
        .m3(BigDecimal.ONE)
        .estado(EstadoVehiculo.ACTIVO)
        .deleted(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
