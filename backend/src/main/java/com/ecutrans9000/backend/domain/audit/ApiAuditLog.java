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
public class ApiAuditLog {
  private Long id;
  private LocalDateTime fechaHora;
  private String endpoint;
  private String requestJson;
  private String responseJson;
  private String usuario;
  private String rolUsuario;
}
