package com.ecutrans9000.backend.config;

import com.ecutrans9000.backend.adapters.out.persistence.entity.UserJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.UserJpaRepository;
import com.ecutrans9000.backend.domain.user.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SuperadminBootstrapConfig {

  private final UserJpaRepository userJpaRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  ApplicationRunner superadminBootstrapRunner(
      @Value("${app.bootstrap.superadmin.username:admin}") String username,
      @Value("${app.bootstrap.superadmin.password:Qwerty12345}") String rawPassword,
      @Value("${app.bootstrap.superadmin.nombres:SUPERADMINISTRADOR}") String nombres,
      @Value("${app.bootstrap.superadmin.correo:admin@ecutrans9000.local}") String correo) {
    return args -> {
      LocalDateTime now = LocalDateTime.now();
      UserJpaEntity superadmin = userJpaRepository.findByUsernameIgnoreCase(username)
          .orElseGet(() -> UserJpaEntity.builder()
              .id(UUID.randomUUID())
              .username(username)
              .createdAt(now)
              .build());

      superadmin.setNombres(nombres);
      superadmin.setCorreo(correo);
      superadmin.setUsername(username);
      superadmin.setPasswordHash(passwordEncoder.encode(rawPassword));
      superadmin.setRol(UserRole.SUPERADMINISTRADOR);
      superadmin.setActivo(true);
      superadmin.setDeleted(false);
      superadmin.setDeletedAt(null);
      superadmin.setDeletedBy(null);
      superadmin.setUpdatedAt(now);
      if (superadmin.getCreatedAt() == null) {
        superadmin.setCreatedAt(now);
      }
      userJpaRepository.save(superadmin);
      log.info("Usuario SUPERADMINISTRADOR bootstrap asegurado: {}", username);
    };
  }
}
