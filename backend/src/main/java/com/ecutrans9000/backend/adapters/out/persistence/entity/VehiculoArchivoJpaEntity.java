package com.ecutrans9000.backend.adapters.out.persistence.entity;

import com.ecutrans9000.backend.domain.vehiculo.TipoArchivoVehiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehiculo_archivos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoArchivoJpaEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "vehiculo_id", nullable = false)
  private UUID vehiculoId;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo", nullable = false, length = 20)
  private TipoArchivoVehiculo tipo;

  @Column(name = "file_name", nullable = false, length = 255)
  private String fileName;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "contenido", nullable = false)
  private byte[] contenido;

  @Column(name = "size_bytes", nullable = false)
  private Long sizeBytes;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
