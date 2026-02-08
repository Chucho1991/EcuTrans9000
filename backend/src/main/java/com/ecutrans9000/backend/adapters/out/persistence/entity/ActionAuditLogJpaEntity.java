package com.ecutrans9000.backend.adapters.out.persistence.entity;

import com.ecutrans9000.backend.domain.audit.ActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "action_audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionAuditLogJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "fecha_hora", nullable = false)
  private LocalDateTime fechaHora;

  @Column(name = "usuario", length = 100)
  private String usuario;

  @Column(name = "rol_usuario", length = 50)
  private String rolUsuario;

  @Column(name = "modulo_afectado", length = 100)
  private String moduloAfectado;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_modificacion", length = 50)
  private ActionType tipoModificacion;

  @Column(name = "id_registro", length = 100)
  private String idRegistro;

  @Column(name = "nombre_tabla", length = 100)
  private String nombreTabla;
}
