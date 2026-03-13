package com.ecutrans9000.backend.application.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.domain.cliente.Cliente;
import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;
import com.ecutrans9000.backend.ports.out.cliente.ClienteRepositoryPort;
import com.ecutrans9000.backend.service.AuditService;
import java.time.LocalDateTime;
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
}
