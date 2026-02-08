package com.ecutrans9000.backend.adapters.in.rest.dto.user;

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
public class UpdateUserRequest {
  private String nombres;
  private String correo;
  private String username;
  private String rol;
  private Boolean activo;
}
