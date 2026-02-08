package com.ecutrans9000.backend.adapters.in.rest.dto.auth;

import java.util.UUID;
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
public class LoginResponse {
  private String token;
  private UUID userId;
  private String nombres;
  private String username;
  private String rol;
}
