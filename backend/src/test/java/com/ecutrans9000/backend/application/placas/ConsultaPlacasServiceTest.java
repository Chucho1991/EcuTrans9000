package com.ecutrans9000.backend.application.placas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import com.ecutrans9000.backend.adapters.in.rest.dto.placas.ConsultaPlacaResponse;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ViajeBitacoraJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ClienteJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.DescuentoViajeJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ViajeBitacoraJpaRepository;
import com.ecutrans9000.backend.domain.bitacora.EstadoPagoChoferFiltro;
import com.ecutrans9000.backend.domain.cliente.TipoDocumentoCliente;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * Pruebas unitarias del cálculo financiero para consulta por placas.
 */
@ExtendWith(MockitoExtension.class)
class ConsultaPlacasServiceTest {

  @Mock
  private ViajeBitacoraJpaRepository viajeRepository;

  @Mock
  private VehiculoJpaRepository vehiculoRepository;

  @Mock
  private ClienteJpaRepository clienteRepository;

  @Mock
  private DescuentoViajeJpaRepository descuentoRepository;

  @InjectMocks
  private ConsultaPlacasService consultaPlacasService;

  @Test
  void consultarShouldUseCostoChoferAsValorAndMarkedValorBitacoraForRetencion() {
    UUID vehiculoId = UUID.randomUUID();
    UUID clienteId = UUID.randomUUID();

    VehiculoJpaEntity vehiculo = VehiculoJpaEntity.builder()
        .id(vehiculoId)
        .placa("PAE-4249")
        .placaNorm("PAE4249")
        .choferDefault("Chofer Prueba")
        .build();

    ClienteJpaEntity cliente = ClienteJpaEntity.builder()
        .id(clienteId)
        .tipoDocumento(TipoDocumentoCliente.RUC)
        .documento("0999999999001")
        .documentoNorm("0999999999001")
        .nombre("Cliente Demo")
        .activo(true)
        .deleted(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    ViajeBitacoraJpaEntity viaje = ViajeBitacoraJpaEntity.builder()
        .id(UUID.randomUUID())
        .numeroViaje(120)
        .fechaViaje(LocalDate.of(2026, 4, 10))
        .vehiculoId(vehiculoId)
        .clienteId(clienteId)
        .destino("Guayaquil")
        .detalleViaje("Ruta principal")
        .valor(new BigDecimal("100.00"))
        .costoChofer(new BigDecimal("80.00"))
        .estiba(new BigDecimal("5.00"))
        .anticipo(new BigDecimal("10.00"))
        .aplicaRetencion(true)
        .numeroFactura("FAC-120")
        .pagadoTransportista(false)
        .deleted(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    when(vehiculoRepository.findByPlacaNorm("PAE-4249")).thenReturn(Optional.of(vehiculo));
    when(viajeRepository.findAll(anySpecification(), argThat((Sort sort) -> isSortedByNumeroViajeDesc(sort)))).thenReturn(List.of(viaje));
    when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

    ConsultaPlacaResponse response = consultaPlacasService.consultar(
        "PAE-4249",
        null,
        EstadoPagoChoferFiltro.TODOS,
        LocalDate.of(2026, 4, 1),
        LocalDate.of(2026, 4, 30));

    assertEquals(new BigDecimal("80.00"), response.getValorFacturaTotal());
    assertEquals(new BigDecimal("4.80"), response.getComisionAdministrativaSeisPorciento());
    assertEquals(new BigDecimal("1.00"), response.getRetencionUnoPorciento());
    assertEquals(new BigDecimal("69.20"), response.getPagoTotal());
    assertEquals(new BigDecimal("80.00"), response.getRegistros().get(0).getValor());
    assertEquals(new BigDecimal("100.00"), response.getRegistros().get(0).getValorBitacora());
    assertEquals(true, response.getRegistros().get(0).getAplicaRetencion());
  }

  private boolean isSortedByNumeroViajeDesc(Sort sort) {
    Sort.Order order = sort.getOrderFor("numeroViaje");
    return order != null && order.isDescending();
  }

  @SuppressWarnings("unused")
  private boolean isSortedByNumeroViajeDesc(Pageable pageable) {
    return isSortedByNumeroViajeDesc(pageable.getSort());
  }

  @SuppressWarnings("unchecked")
  private Specification<ViajeBitacoraJpaEntity> anySpecification() {
    return any(Specification.class);
  }
}
