package com.ecutrans9000.backend.application.vehiculo;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
/**
 * Scheduler que desactiva licencias vencidas de vehículos.
 */
public class VehiculoLicenseExpirationScheduler {

  private final VehiculoApplicationService vehiculoApplicationService;

  @Scheduled(cron = "${app.vehiculos.license-expiration-cron:0 0 0 * * *}", zone = "${app.vehiculos.license-expiration-zone:America/Guayaquil}")
  public void deactivateExpiredLicenses() {
    vehiculoApplicationService.deactivateExpiredLicenses();
  }
}
