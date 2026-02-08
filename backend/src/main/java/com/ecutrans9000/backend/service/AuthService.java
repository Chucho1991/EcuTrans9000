package com.ecutrans9000.backend.service;

import com.ecutrans9000.backend.adapters.in.rest.dto.auth.LoginRequest;
import com.ecutrans9000.backend.adapters.in.rest.dto.auth.LoginResponse;
import com.ecutrans9000.backend.adapters.out.persistence.entity.UserJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.UserJpaRepository;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.security.JwtService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserJpaRepository userJpaRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuditService auditService;

  public LoginResponse login(LoginRequest request) {
    UserJpaEntity user = userJpaRepository.findByUsernameIgnoreCaseOrCorreoIgnoreCase(
            request.getUsernameOrEmail(),
            request.getUsernameOrEmail())
        .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BusinessException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
    }
    if (Boolean.TRUE.equals(user.getDeleted()) || !Boolean.TRUE.equals(user.getActivo())) {
      throw new BusinessException(HttpStatus.FORBIDDEN, "Usuario inactivo");
    }

    String token = jwtService.generateToken(user.getUsername(), Map.of("role", user.getRol().name(), "uid", user.getId().toString()));

    auditService.saveActionAudit(
        user.getUsername(),
        user.getRol().name(),
        "AUTH",
        ActionType.LOGIN,
        user.getId().toString(),
        "users");

    return LoginResponse.builder()
        .token(token)
        .userId(user.getId())
        .nombres(user.getNombres())
        .username(user.getUsername())
        .rol(user.getRol().name())
        .build();
  }
}
