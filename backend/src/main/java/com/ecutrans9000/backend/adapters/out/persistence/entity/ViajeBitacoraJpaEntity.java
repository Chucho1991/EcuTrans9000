package com.ecutrans9000.backend.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entidad JPA que representa un viaje registrado en la bitácora operativa.
 */
@Entity
@Table(name = "viajes_bitacora")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViajeBitacoraJpaEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "numero_viaje", nullable = false, unique = true)
  private Integer numeroViaje;

  @Column(name = "fecha_viaje", nullable = false)
  private LocalDate fechaViaje;

  @Column(name = "vehiculo_id", nullable = false)
  private UUID vehiculoId;

  @Column(name = "cliente_id", nullable = false)
  private UUID clienteId;

  @Column(name = "destino", nullable = false, length = 250)
  private String destino;

  @Column(name = "detalle_viaje", length = 500)
  private String detalleViaje;

  @Column(name = "valor", nullable = false, precision = 12, scale = 2)
  private BigDecimal valor;

  @Column(name = "costo_chofer", nullable = false, precision = 12, scale = 2)
  private BigDecimal costoChofer;

  @Column(name = "estiba", nullable = false, precision = 12, scale = 2)
  private BigDecimal estiba;

  @Column(name = "anticipo", nullable = false, precision = 12, scale = 2)
  private BigDecimal anticipo;

  @Column(name = "facturado_cliente", nullable = false)
  private Boolean facturadoCliente;

  @Column(name = "numero_factura", length = 100)
  private String numeroFactura;

  @Column(name = "fecha_factura")
  private LocalDate fechaFactura;

  @Column(name = "fecha_pago_cliente")
  private LocalDate fechaPagoCliente;

  @Column(name = "pagado_transportista", nullable = false)
  private Boolean pagadoTransportista;

  @Column(name = "observaciones", length = 1000)
  private String observaciones;

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
