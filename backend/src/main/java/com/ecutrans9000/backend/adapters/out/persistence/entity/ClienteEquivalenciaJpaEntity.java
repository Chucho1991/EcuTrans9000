package com.ecutrans9000.backend.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA para la tabla de equivalencias por destino de clientes.
 */
@Entity
@Table(name = "cliente_equivalencias")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEquivalenciaJpaEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  private ClienteJpaEntity cliente;

  @Column(name = "destino", nullable = false, length = 200)
  private String destino;

  @Column(name = "destino_norm", nullable = false, length = 200)
  private String destinoNorm;

  @Column(name = "valor_destino", nullable = false, precision = 14, scale = 2)
  private BigDecimal valorDestino;

  @Column(name = "costo_chofer", nullable = false, precision = 14, scale = 2)
  private BigDecimal costoChofer;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
