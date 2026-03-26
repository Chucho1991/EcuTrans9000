package com.ecutrans9000.backend.application.bitacora;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ecutrans9000.backend.adapters.in.rest.dto.bitacora.ViajeBitacoraResponse;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ViajeBitacoraJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ClienteJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ViajeBitacoraJpaRepository;
import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;
import com.ecutrans9000.backend.domain.vehiculo.EstadoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.TipoDocumento;
import com.ecutrans9000.backend.service.AuditService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * Pruebas unitarias de exportación Excel del módulo bitácora.
 */
@ExtendWith(MockitoExtension.class)
class ViajeBitacoraServiceTest {

  @Mock
  private ViajeBitacoraJpaRepository viajeRepository;

  @Mock
  private VehiculoJpaRepository vehiculoRepository;

  @Mock
  private ClienteJpaRepository clienteRepository;

  @Mock
  private AuditService auditService;

  @InjectMocks
  private ViajeBitacoraService viajeBitacoraService;

  @Test
  void listShouldRequestDescendingSortByNumeroViaje() {
    when(viajeRepository.findAll(anySpecification(), argThat((Pageable pageable) -> isSortedByNumeroViajeDesc(pageable))))
        .thenReturn(new PageImpl<ViajeBitacoraJpaEntity>(List.of()));

    var response = viajeBitacoraService.list(0, 10, null, null, null, null, null, false);

    assertEquals(List.<ViajeBitacoraResponse>of(), response.getContent());
  }

  @Test
  void exportExcelShouldLeftAlignTextColumnsAndResizeThemUsingLongestValue() throws IOException {
    UUID vehiculoId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();

    ViajeBitacoraJpaEntity viaje = ViajeBitacoraJpaEntity.builder()
        .id(UUID.randomUUID())
        .numeroViaje(125)
        .fechaViaje(LocalDate.of(2026, 3, 20))
        .vehiculoId(vehiculoId)
        .clienteId(clienteId)
        .destino("Destino interprovincial muy largo")
        .detalleViaje("Detalle de viaje con descripcion operativa extensa")
        .valor(new BigDecimal("150.00"))
        .costoChofer(new BigDecimal("110.00"))
        .estiba(new BigDecimal("10.00"))
        .anticipo(new BigDecimal("25.00"))
        .facturadoCliente(true)
        .numeroFactura("FAC-001")
        .fechaFactura(LocalDate.of(2026, 3, 21))
        .fechaPagoCliente(LocalDate.of(2026, 3, 22))
        .pagadoTransportista(false)
        .deleted(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    VehiculoJpaEntity vehiculo = VehiculoJpaEntity.builder()
        .id(vehiculoId)
        .placa("ABC-1234")
        .placaNorm("ABC-1234")
        .choferDefault("Chofer con nombre bastante largo")
        .licencia("LIC-001")
        .tipoDocumento(TipoDocumento.CEDULA)
        .documentoPersonal("0102030405")
        .tonelajeCategoria("PESADO")
        .m3(new BigDecimal("25"))
        .estado(EstadoVehiculo.ACTIVO)
        .deleted(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    ClienteJpaEntity cliente = ClienteJpaEntity.builder()
        .id(clienteId)
        .tipoDocumento(TipoDocumentoCliente.RUC)
        .documento("0999999999001")
        .documentoNorm("0999999999001")
        .nombre("Cliente base")
        .nombreComercial("Cliente comercial de nombre extenso")
        .activo(true)
        .deleted(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    when(viajeRepository.findAll(anySpecification(), argThat((Sort sort) -> isSortedByNumeroViajeDesc(sort))))
        .thenReturn(List.of(viaje));
    when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
    when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

    byte[] report = viajeBitacoraService.exportExcel(null, null, null, null, null);

    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(report))) {
      var sheet = workbook.getSheetAt(0);
      int dataRowIndex = findHeaderRowIndex(sheet) + 1;
      var row = sheet.getRow(dataRowIndex);

      assertEquals(HorizontalAlignment.LEFT, row.getCell(5).getCellStyle().getAlignment());
      assertEquals(HorizontalAlignment.LEFT, row.getCell(6).getCellStyle().getAlignment());
      assertEquals(HorizontalAlignment.LEFT, row.getCell(7).getCellStyle().getAlignment());
      assertEquals(HorizontalAlignment.LEFT, row.getCell(8).getCellStyle().getAlignment());
      assertEquals(110d, row.getCell(17).getNumericCellValue());

      assertEquals(expectedColumnWidth("Chofer con nombre bastante largo", "Chofer"), sheet.getColumnWidth(5));
      assertEquals(expectedColumnWidth("Destino interprovincial muy largo", "Destino"), sheet.getColumnWidth(6));
      assertEquals(expectedColumnWidth("Detalle de viaje con descripcion operativa extensa", "Detalle viaje"), sheet.getColumnWidth(7));
      assertEquals(expectedColumnWidth("Cliente comercial de nombre extenso", "Cliente"), sheet.getColumnWidth(8));
      assertEquals(expectedAmountColumnWidth("110", "Costo Chofer"), sheet.getColumnWidth(17));
    }
  }

  private int expectedColumnWidth(String value, String header) {
    int maxLength = Math.max(value.trim().length(), header.length());
    int normalizedWidth = Math.max(12, Math.min(maxLength + 2, 80));
    return normalizedWidth * 256;
  }

  private int expectedAmountColumnWidth(String value, String header) {
    int maxLength = Math.max(value.trim().length(), header.length());
    int normalizedWidth = Math.max(14, Math.min(maxLength + 2, 20));
    return normalizedWidth * 256;
  }

  private int findHeaderRowIndex(org.apache.poi.xssf.usermodel.XSSFSheet sheet) {
    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
      var row = sheet.getRow(i);
      if (row == null) {
        continue;
      }
      var cell = row.getCell(0);
      if (cell != null && cell.getCellType() == CellType.STRING && "Viaje".equalsIgnoreCase(cell.getStringCellValue().trim())) {
        return i;
      }
    }
    return 0;
  }

  private boolean isSortedByNumeroViajeDesc(Pageable pageable) {
    return isSortedByNumeroViajeDesc(pageable.getSort());
  }

  private boolean isSortedByNumeroViajeDesc(Sort sort) {
    Sort.Order order = sort.getOrderFor("numeroViaje");
    return order != null && order.isDescending();
  }

  @SuppressWarnings("unchecked")
  private Specification<ViajeBitacoraJpaEntity> anySpecification() {
    return any(Specification.class);
  }
}
