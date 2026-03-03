package com.ecutrans9000.backend.application.dashboard;

import com.ecutrans9000.backend.adapters.in.rest.dto.dashboard.DashboardResponse;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.UserJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.VehiculoJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.ViajeBitacoraJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ClienteJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.UserJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.VehiculoJpaRepository;
import com.ecutrans9000.backend.adapters.out.persistence.repository.ViajeBitacoraJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardQueryService {

  private static final Locale DASHBOARD_LOCALE = Locale.forLanguageTag("es-EC");

  private final UserJpaRepository userRepository;
  private final VehiculoJpaRepository vehiculoRepository;
  private final ClienteJpaRepository clienteRepository;
  private final ViajeBitacoraJpaRepository viajeRepository;

  public DashboardQueryService(
      UserJpaRepository userRepository,
      VehiculoJpaRepository vehiculoRepository,
      ClienteJpaRepository clienteRepository,
      ViajeBitacoraJpaRepository viajeRepository) {
    this.userRepository = userRepository;
    this.vehiculoRepository = vehiculoRepository;
    this.clienteRepository = clienteRepository;
    this.viajeRepository = viajeRepository;
  }

  @Transactional(readOnly = true)
  public DashboardResponse getDashboard() {
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
    YearMonth currentMonth = YearMonth.from(today);

    List<UserJpaEntity> users = userRepository.findAll();
    List<VehiculoJpaEntity> vehiculos = vehiculoRepository.findAll();
    List<ClienteJpaEntity> clientes = clienteRepository.findAll();
    List<ViajeBitacoraJpaEntity> viajes = viajeRepository.findAll();

    List<UserJpaEntity> activeUsers = users.stream()
        .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
        .filter(user -> Boolean.TRUE.equals(user.getActivo()))
        .toList();
    List<UserJpaEntity> inactiveUsers = users.stream()
        .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
        .filter(user -> !Boolean.TRUE.equals(user.getActivo()))
        .toList();

    List<VehiculoJpaEntity> activeVehiculos = vehiculos.stream()
        .filter(vehiculo -> !Boolean.TRUE.equals(vehiculo.getDeleted()))
        .toList();
    long vehiculosEliminados = vehiculos.stream().filter(vehiculo -> Boolean.TRUE.equals(vehiculo.getDeleted())).count();
    long vehiculosActivos = activeVehiculos.stream()
        .filter(vehiculo -> vehiculo.getEstado() != null && "ACTIVO".equals(vehiculo.getEstado().name()))
        .count();
    long vehiculosInactivos = activeVehiculos.size() - vehiculosActivos;

    List<ClienteJpaEntity> activeClientes = clientes.stream()
        .filter(cliente -> !Boolean.TRUE.equals(cliente.getDeleted()))
        .toList();
    long clientesActivos = activeClientes.stream().filter(cliente -> Boolean.TRUE.equals(cliente.getActivo())).count();
    long clientesInactivos = activeClientes.size() - clientesActivos;

    List<ViajeBitacoraJpaEntity> activeViajes = viajes.stream()
        .filter(viaje -> !Boolean.TRUE.equals(viaje.getDeleted()))
        .toList();
    List<ViajeBitacoraJpaEntity> viajesMesActual = activeViajes.stream()
        .filter(viaje -> viaje.getFechaViaje() != null && YearMonth.from(viaje.getFechaViaje()).equals(currentMonth))
        .toList();

    DashboardResponse.LicenseAlerts licenseAlerts = buildLicenseAlerts(activeVehiculos, today);
    BigDecimal facturacionTotal = sum(activeViajes, ViajeBitacoraJpaEntity::getValor);
    BigDecimal anticiposTotal = sum(activeViajes, ViajeBitacoraJpaEntity::getAnticipo);
    BigDecimal cobrosTotal = activeViajes.stream()
        .filter(viaje -> viaje.getFechaPagoCliente() != null)
        .map(ViajeBitacoraJpaEntity::getValor)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal facturacionMesActual = sum(viajesMesActual, ViajeBitacoraJpaEntity::getValor);
    BigDecimal anticiposMesActual = sum(viajesMesActual, ViajeBitacoraJpaEntity::getAnticipo);
    BigDecimal cobrosMesActual = activeViajes.stream()
        .filter(viaje -> viaje.getFechaPagoCliente() != null && YearMonth.from(viaje.getFechaPagoCliente()).equals(currentMonth))
        .map(ViajeBitacoraJpaEntity::getValor)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal pendienteFacturar = activeViajes.stream()
        .filter(viaje -> !Boolean.TRUE.equals(viaje.getFacturadoCliente()))
        .map(ViajeBitacoraJpaEntity::getValor)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal pendienteCobrar = activeViajes.stream()
        .filter(viaje -> Boolean.TRUE.equals(viaje.getFacturadoCliente()))
        .filter(viaje -> viaje.getFechaPagoCliente() == null)
        .map(ViajeBitacoraJpaEntity::getValor)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    long pendientesFacturarCount = activeViajes.stream()
        .filter(viaje -> !Boolean.TRUE.equals(viaje.getFacturadoCliente()))
        .count();

    long alertasActivas = licenseAlerts.getVencidas()
        + licenseAlerts.getPorVencer7Dias()
        + pendientesFacturarCount;

    return DashboardResponse.builder()
        .generatedAt(now)
        .overview(DashboardResponse.Overview.builder()
            .usuariosActivos(activeUsers.size())
            .usuariosInactivos(inactiveUsers.size())
            .totalVehiculos(activeVehiculos.size())
            .vehiculosActivos(vehiculosActivos)
            .vehiculosInactivos(vehiculosInactivos)
            .clientesActivos(clientesActivos)
            .clientesInactivos(clientesInactivos)
            .viajesMesActual(viajesMesActual.size())
            .viajesPendientesFacturar(pendientesFacturarCount)
            .alertasActivas(alertasActivas)
            .build())
        .financial(DashboardResponse.FinancialSummary.builder()
            .facturacionTotal(facturacionTotal)
            .cobrosTotal(cobrosTotal)
            .anticiposTotal(anticiposTotal)
            .facturacionMesActual(facturacionMesActual)
            .cobrosMesActual(cobrosMesActual)
            .anticiposMesActual(anticiposMesActual)
            .pendienteFacturar(pendienteFacturar)
            .pendienteCobrar(pendienteCobrar)
            .build())
        .licenseAlerts(licenseAlerts)
        .vehiculosPorEstado(buildVehicleDistribution(activeVehiculos, vehiculosActivos, vehiculosInactivos, vehiculosEliminados))
        .viajesPorMes(buildMonthlyTrips(activeViajes, currentMonth))
        .topDestinos(buildTopDestinations(activeViajes))
        .topClientes(buildTopClientes(activeViajes, activeClientes))
        .topVehiculos(buildTopVehiculos(activeViajes, activeVehiculos))
        .viajesRecientes(buildRecentTrips(activeViajes, activeVehiculos, activeClientes))
        .build();
  }

  private DashboardResponse.LicenseAlerts buildLicenseAlerts(List<VehiculoJpaEntity> vehiculos, LocalDate today) {
    long vencidas = vehiculos.stream()
        .filter(vehiculo -> vehiculo.getFechaCaducidadLicencia() != null)
        .filter(vehiculo -> vehiculo.getFechaCaducidadLicencia().isBefore(today))
        .count();
    long porVencer7Dias = vehiculos.stream()
        .filter(vehiculo -> vehiculo.getFechaCaducidadLicencia() != null)
        .filter(vehiculo -> !vehiculo.getFechaCaducidadLicencia().isBefore(today))
        .filter(vehiculo -> !vehiculo.getFechaCaducidadLicencia().isAfter(today.plusDays(7)))
        .count();
    long porVencer30Dias = vehiculos.stream()
        .filter(vehiculo -> vehiculo.getFechaCaducidadLicencia() != null)
        .filter(vehiculo -> vehiculo.getFechaCaducidadLicencia().isAfter(today.plusDays(7)))
        .filter(vehiculo -> !vehiculo.getFechaCaducidadLicencia().isAfter(today.plusDays(30)))
        .count();

    return DashboardResponse.LicenseAlerts.builder()
        .vencidas(vencidas)
        .porVencer7Dias(porVencer7Dias)
        .porVencer30Dias(porVencer30Dias)
        .build();
  }

  private List<DashboardResponse.DistributionItem> buildVehicleDistribution(
      List<VehiculoJpaEntity> activeVehiculos,
      long vehiculosActivos,
      long vehiculosInactivos,
      long vehiculosEliminados) {
    return List.of(
        DashboardResponse.DistributionItem.builder().label("Activos").total(vehiculosActivos).tone("success").build(),
        DashboardResponse.DistributionItem.builder().label("Inactivos").total(vehiculosInactivos).tone("warning").build(),
        DashboardResponse.DistributionItem.builder().label("Eliminados").total(vehiculosEliminados).tone("danger").build(),
        DashboardResponse.DistributionItem.builder().label("Con licencia").total(activeVehiculos.stream().filter(v -> v.getFechaCaducidadLicencia() != null).count()).tone("brand").build());
  }

  private List<DashboardResponse.MonthlyTripsItem> buildMonthlyTrips(List<ViajeBitacoraJpaEntity> activeViajes, YearMonth currentMonth) {
    List<DashboardResponse.MonthlyTripsItem> items = new ArrayList<>();
    for (int offset = 5; offset >= 0; offset--) {
      YearMonth month = currentMonth.minusMonths(offset);
      List<ViajeBitacoraJpaEntity> monthTrips = activeViajes.stream()
          .filter(viaje -> viaje.getFechaViaje() != null && YearMonth.from(viaje.getFechaViaje()).equals(month))
          .toList();
      items.add(DashboardResponse.MonthlyTripsItem.builder()
          .label(capitalize(month.getMonth().getDisplayName(TextStyle.SHORT, DASHBOARD_LOCALE)))
          .monthStart(month.atDay(1))
          .viajes(monthTrips.size())
          .valor(sum(monthTrips, ViajeBitacoraJpaEntity::getValor))
          .build());
    }
    return items;
  }

  private List<DashboardResponse.RankingItem> buildTopDestinations(List<ViajeBitacoraJpaEntity> activeViajes) {
    return activeViajes.stream()
        .filter(viaje -> viaje.getDestino() != null && !viaje.getDestino().isBlank())
        .collect(java.util.stream.Collectors.groupingBy(
            ViajeBitacoraJpaEntity::getDestino,
            LinkedHashMap::new,
            java.util.stream.Collectors.toList()))
        .entrySet()
        .stream()
        .map(entry -> DashboardResponse.RankingItem.builder()
            .label(entry.getKey())
            .secondaryLabel("Viajes")
            .total(entry.getValue().size())
            .amount(sum(entry.getValue(), ViajeBitacoraJpaEntity::getValor))
            .build())
        .sorted(Comparator.comparing(DashboardResponse.RankingItem::getTotal).reversed()
            .thenComparing(DashboardResponse.RankingItem::getAmount, Comparator.reverseOrder()))
        .limit(5)
        .toList();
  }

  private List<DashboardResponse.RankingItem> buildTopClientes(
      List<ViajeBitacoraJpaEntity> activeViajes,
      List<ClienteJpaEntity> activeClientes) {
    Map<UUID, ClienteJpaEntity> clientsById = activeClientes.stream().collect(
        java.util.stream.Collectors.toMap(ClienteJpaEntity::getId, cliente -> cliente));
    return activeViajes.stream()
        .collect(java.util.stream.Collectors.groupingBy(ViajeBitacoraJpaEntity::getClienteId))
        .entrySet()
        .stream()
        .map(entry -> {
          ClienteJpaEntity cliente = clientsById.get(entry.getKey());
          String clientName = cliente == null ? "Cliente no disponible" : preferredClientName(cliente);
          return DashboardResponse.RankingItem.builder()
              .label(clientName)
              .secondaryLabel(cliente == null ? null : cliente.getDocumento())
              .total(entry.getValue().size())
              .amount(sum(entry.getValue(), ViajeBitacoraJpaEntity::getValor))
              .build();
        })
        .sorted(Comparator.comparing(DashboardResponse.RankingItem::getAmount, Comparator.reverseOrder())
            .thenComparing(DashboardResponse.RankingItem::getTotal, Comparator.reverseOrder()))
        .limit(5)
        .toList();
  }

  private List<DashboardResponse.RankingItem> buildTopVehiculos(
      List<ViajeBitacoraJpaEntity> activeViajes,
      List<VehiculoJpaEntity> activeVehiculos) {
    Map<UUID, VehiculoJpaEntity> vehiculosById = activeVehiculos.stream().collect(
        java.util.stream.Collectors.toMap(VehiculoJpaEntity::getId, vehiculo -> vehiculo));
    return activeViajes.stream()
        .collect(java.util.stream.Collectors.groupingBy(ViajeBitacoraJpaEntity::getVehiculoId))
        .entrySet()
        .stream()
        .map(entry -> {
          VehiculoJpaEntity vehiculo = vehiculosById.get(entry.getKey());
          return DashboardResponse.RankingItem.builder()
              .label(vehiculo == null ? "Vehiculo no disponible" : vehiculo.getPlaca())
              .secondaryLabel(vehiculo == null ? null : vehiculo.getChoferDefault())
              .total(entry.getValue().size())
              .amount(sum(entry.getValue(), ViajeBitacoraJpaEntity::getValor))
              .build();
        })
        .sorted(Comparator.comparing(DashboardResponse.RankingItem::getTotal).reversed()
            .thenComparing(DashboardResponse.RankingItem::getAmount, Comparator.reverseOrder()))
        .limit(5)
        .toList();
  }

  private List<DashboardResponse.RecentTripItem> buildRecentTrips(
      List<ViajeBitacoraJpaEntity> activeViajes,
      List<VehiculoJpaEntity> activeVehiculos,
      List<ClienteJpaEntity> activeClientes) {
    Map<UUID, VehiculoJpaEntity> vehiculosById = activeVehiculos.stream().collect(
        java.util.stream.Collectors.toMap(VehiculoJpaEntity::getId, vehiculo -> vehiculo));
    Map<UUID, ClienteJpaEntity> clientesById = activeClientes.stream().collect(
        java.util.stream.Collectors.toMap(ClienteJpaEntity::getId, cliente -> cliente));

    return activeViajes.stream()
        .sorted(Comparator.comparing(ViajeBitacoraJpaEntity::getFechaViaje, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(ViajeBitacoraJpaEntity::getNumeroViaje, Comparator.nullsLast(Comparator.reverseOrder())))
        .limit(6)
        .map(viaje -> {
          VehiculoJpaEntity vehiculo = vehiculosById.get(viaje.getVehiculoId());
          ClienteJpaEntity cliente = clientesById.get(viaje.getClienteId());
          return DashboardResponse.RecentTripItem.builder()
              .numeroViaje(viaje.getNumeroViaje())
              .fechaViaje(viaje.getFechaViaje())
              .placa(vehiculo == null ? "N/D" : vehiculo.getPlaca())
              .cliente(cliente == null ? "N/D" : preferredClientName(cliente))
              .destino(viaje.getDestino())
              .valor(viaje.getValor())
              .facturadoCliente(viaje.getFacturadoCliente())
              .pagadoTransportista(viaje.getPagadoTransportista())
              .build();
        })
        .toList();
  }

  private String preferredClientName(ClienteJpaEntity cliente) {
    if (cliente.getNombreComercial() != null && !cliente.getNombreComercial().isBlank()) {
      return cliente.getNombreComercial();
    }
    return cliente.getNombre();
  }

  private BigDecimal sum(List<ViajeBitacoraJpaEntity> viajes, java.util.function.Function<ViajeBitacoraJpaEntity, BigDecimal> extractor) {
    return viajes.stream()
        .map(extractor)
        .filter(value -> value != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private String capitalize(String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    return value.substring(0, 1).toUpperCase(DASHBOARD_LOCALE) + value.substring(1);
  }
}
