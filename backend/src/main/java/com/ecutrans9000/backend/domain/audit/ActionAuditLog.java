package com.ecutrans9000.backend.domain.audit;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionAuditLog {
  private Long id;
  private LocalDateTime fechaHora;
  private String usuario;
  private String rolUsuario;
  private String moduloAfectado;
  private ActionType tipoModificacion;
  private String idRegistro;
  private String nombreTabla;
}
