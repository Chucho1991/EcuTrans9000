package com.ecutrans9000.backend.domain.vehiculo;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Archivo binario asociado a vehiculo.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoArchivo {
  private UUID id;
  private UUID vehiculoId;
  private TipoArchivoVehiculo tipo;
  private String fileName;
  private String contentType;
  private byte[] contenido;
  private Long sizeBytes;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
