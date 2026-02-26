package com.ecutrans9000.backend.ports.out.vehiculo;

import com.ecutrans9000.backend.domain.vehiculo.TipoArchivoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.VehiculoArchivo;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para almacenamiento de archivos de vehiculos en persistencia.
 */
public interface VehiculoArchivoRepositoryPort {
  VehiculoArchivo save(VehiculoArchivo archivo);

  Optional<VehiculoArchivo> findByVehiculoIdAndTipo(UUID vehiculoId, TipoArchivoVehiculo tipo);
}
