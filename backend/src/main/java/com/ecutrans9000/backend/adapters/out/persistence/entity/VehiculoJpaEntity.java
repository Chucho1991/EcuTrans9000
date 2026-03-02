package com.ecutrans9000.backend.adapters.out.persistence.entity;

import com.ecutrans9000.backend.domain.vehiculo.EstadoVehiculo;
import com.ecutrans9000.backend.domain.vehiculo.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Componente publico de backend para VehiculoJpaEntity.
 */
@Entity
@Table(name = "vehiculos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoJpaEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "placa", nullable = false, length = 50)
  private String placa;

  @Column(name = "placa_norm", nullable = false, unique = true, length = 50)
  private String placaNorm;

  @Column(name = "chofer_default", nullable = false, length = 200)
  private String choferDefault;

  @Column(name = "licencia", length = 100)
  private String licencia;

  @Column(name = "fecha_caducidad_licencia")
  private LocalDate fechaCaducidadLicencia;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_documento", nullable = false, length = 20)
  private TipoDocumento tipoDocumento;

  @Column(name = "documento_personal", nullable = false, length = 100)
  private String documentoPersonal;

  @Column(name = "tonelaje_categoria", nullable = false, length = 100)
  private String tonelajeCategoria;

  @Column(name = "m3", nullable = false, precision = 10, scale = 2)
  private BigDecimal m3;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false, length = 20)
  private EstadoVehiculo estado;

  @Column(name = "foto_path", length = 500)
  private String fotoPath;

  @Column(name = "doc_path", length = 500)
  private String docPath;

  @Column(name = "lic_path", length = 500)
  private String licPath;

  @Column(name = "deleted", nullable = false)
  private Boolean deleted;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
