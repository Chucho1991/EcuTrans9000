package com.ecutrans9000.backend.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
 * Entidad JPA que representa un descuento configurado para viajes por chofer o vehículo.
 */
@Entity
@Table(name = "descuentos_viajes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoViajeJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "vehiculo_id", nullable = false)
  private UUID vehiculoId;

  @Column(name = "descripcion_motivo", nullable = false, length = 250)
  private String descripcionMotivo;

  @Column(name = "descripcion_motivo_norm", nullable = false, length = 250)
  private String descripcionMotivoNorm;

  @Column(name = "monto_motivo", nullable = false, precision = 12, scale = 2)
  private BigDecimal montoMotivo;

  @Column(name = "fecha_aplicacion")
  private LocalDate fechaAplicacion;

  @Column(name = "activo", nullable = false)
  private Boolean activo;

  @Column(name = "deleted", nullable = false)
  private Boolean deleted;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 100)
  private String deletedBy;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
