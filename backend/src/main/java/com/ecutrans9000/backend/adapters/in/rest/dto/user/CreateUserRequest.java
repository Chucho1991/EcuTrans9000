package com.ecutrans9000.backend.adapters.in.rest.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CreateUserRequest {
  @NotBlank
  @Size(max = 200)
  private String nombres;
  @NotBlank
  @Email
  @Size(max = 200)
  private String correo;
  @NotBlank
  @Size(max = 100)
  private String username;
  @NotBlank
  @Size(min = 8, max = 72)
  private String password;
  @NotBlank
  private String confirmPassword;
  @NotBlank
  private String rol;
  private Boolean activo;
}
