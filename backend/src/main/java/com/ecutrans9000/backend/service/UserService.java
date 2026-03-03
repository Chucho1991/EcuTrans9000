package com.ecutrans9000.backend.service;

import com.ecutrans9000.backend.adapters.in.rest.dto.user.CreateUserRequest;
import com.ecutrans9000.backend.adapters.in.rest.dto.user.UpdateMyProfileRequest;
import com.ecutrans9000.backend.adapters.in.rest.dto.user.UpdateUserRequest;
import com.ecutrans9000.backend.adapters.in.rest.dto.user.UserListResponse;
import com.ecutrans9000.backend.adapters.in.rest.dto.user.UserResponse;
import com.ecutrans9000.backend.adapters.out.persistence.entity.UserJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.repository.UserJpaRepository;
import com.ecutrans9000.backend.domain.audit.ActionType;
import com.ecutrans9000.backend.domain.user.UserRole;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Componente publico de backend para UserService.
 */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserJpaRepository userJpaRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuditService auditService;

  public UserResponse createUser(CreateUserRequest request, String actorUsername, String actorRole) {
    validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());
    String username = normalize(request.getUsername());
    String correo = normalizeEmail(request.getCorreo());
    validateUniqueOnCreate(username, correo);

    UserJpaEntity entity = UserJpaEntity.builder()
        .id(UUID.randomUUID())
        .nombres(normalize(request.getNombres()))
        .correo(correo)
        .username(username)
        .passwordHash(passwordEncoder.encode(request.getPassword()))
        .rol(parseRole(request.getRol()))
        .activo(request.getActivo() == null || request.getActivo())
        .deleted(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    UserJpaEntity saved = userJpaRepository.save(entity);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.CREACION, saved.getId().toString(), "users");
    return toResponse(saved);
  }

  public UserListResponse listUsers(int page, int size, String rol, Boolean activo, Boolean deleted) {
    Page<UserJpaEntity> users = userJpaRepository.findAll((root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (rol != null && !rol.isBlank()) {
        predicates.add(cb.equal(root.get("rol"), parseRole(rol)));
      }
      if (activo != null) {
        predicates.add(cb.equal(root.get("activo"), activo));
      }
      if (deleted != null) {
        predicates.add(cb.equal(root.get("deleted"), deleted));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    }, PageRequest.of(page, size));

    return UserListResponse.builder()
        .content(users.getContent().stream().map(this::toResponse).toList())
        .page(users.getNumber())
        .size(users.getSize())
        .totalElements(users.getTotalElements())
        .totalPages(users.getTotalPages())
        .build();
  }

  public UserResponse getUserById(UUID id) {
    return toResponse(getExisting(id));
  }

  public UserResponse updateUser(UUID id, UpdateUserRequest request, String actorUsername, String actorRole) {
    UserJpaEntity user = getExisting(id);
    String username = normalize(request.getUsername());
    String correo = normalizeEmail(request.getCorreo());
    validateUniqueOnUpdate(id, username, correo);
    user.setNombres(normalize(request.getNombres()));
    user.setCorreo(correo);
    user.setUsername(username);
    user.setRol(parseRole(request.getRol()));
    user.setActivo(request.getActivo() == null || request.getActivo());
    user.setUpdatedAt(LocalDateTime.now());
    UserJpaEntity saved = userJpaRepository.save(user);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.EDICION, saved.getId().toString(), "users");
    return toResponse(saved);
  }

  public UserResponse getMe(String username) {
    return toResponse(getByUsername(username));
  }

  public UserResponse updateMe(String username, UpdateMyProfileRequest request) {
    UserJpaEntity user = getByUsername(username);
    String updatedUsername = normalize(request.getUsername());
    String updatedCorreo = normalizeEmail(request.getCorreo());
    validateUniqueOnUpdate(user.getId(), updatedUsername, updatedCorreo);
    user.setNombres(normalize(request.getNombres()));
    user.setCorreo(updatedCorreo);
    user.setUsername(updatedUsername);
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());
      user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    }
    UserJpaEntity saved = saveUpdatedUser(user);
    auditService.saveActionAudit(saved.getUsername(), saved.getRol().name(), "USERS", ActionType.EDICION, saved.getId().toString(), "users");
    return toResponse(saved);
  }

  public void softDelete(UUID id, String actorUsername, String actorRole) {
    UserJpaEntity user = getExisting(id);
    LocalDateTime now = LocalDateTime.now();
    user.setDeleted(true);
    user.setActivo(false);
    user.setDeletedAt(now);
    user.setDeletedBy(actorUsername);
    user.setUpdatedAt(now);
    userJpaRepository.save(user);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.ELIMINADO_LOGICO, user.getId().toString(), "users");
  }

  public void restore(UUID id, String actorUsername, String actorRole) {
    UserJpaEntity user = getExisting(id);
    LocalDateTime now = LocalDateTime.now();
    user.setDeleted(false);
    user.setActivo(true);
    user.setDeletedAt(null);
    user.setDeletedBy(null);
    user.setUpdatedAt(now);
    userJpaRepository.save(user);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.EDICION, user.getId().toString(), "users");
  }

  public void activate(UUID id, String actorUsername, String actorRole) {
    UserJpaEntity user = getExisting(id);
    if (Boolean.TRUE.equals(user.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede activar un usuario eliminado. Debe restaurarse primero.");
    }
    user.setActivo(true);
    saveUpdatedUser(user);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.EDICION, user.getId().toString(), "users");
  }

  public void deactivate(UUID id, String actorUsername, String actorRole) {
    UserJpaEntity user = getExisting(id);
    if (Boolean.TRUE.equals(user.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El usuario ya esta eliminado logicamente.");
    }
    user.setActivo(false);
    saveUpdatedUser(user);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.EDICION, user.getId().toString(), "users");
  }

  public void hardDelete(UUID id, String actorUsername, String actorRole) {
    throw new BusinessException(
        HttpStatus.METHOD_NOT_ALLOWED,
        "Eliminacion fisica deshabilitada. Use /users/{id}/soft-delete para eliminacion logica.");
  }

  private void validateUniqueOnCreate(String username, String correo) {
    if (userJpaRepository.existsByUsernameIgnoreCase(username)) {
      throw new BusinessException(HttpStatus.CONFLICT, "Username ya existe");
    }
    if (userJpaRepository.existsByCorreoIgnoreCase(correo)) {
      throw new BusinessException(HttpStatus.CONFLICT, "Correo ya existe");
    }
  }

  private void validateUniqueOnUpdate(UUID id, String username, String correo) {
    if (userJpaRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
      throw new BusinessException(HttpStatus.CONFLICT, "Username ya existe");
    }
    if (userJpaRepository.existsByCorreoIgnoreCaseAndIdNot(correo, id)) {
      throw new BusinessException(HttpStatus.CONFLICT, "Correo ya existe");
    }
  }

  private void validatePasswordConfirmation(String password, String confirmPassword) {
    if (password == null || !password.equals(confirmPassword)) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Password y confirmPassword no coinciden");
    }
  }

  private UserRole parseRole(String role) {
    try {
      return UserRole.valueOf(role.trim().toUpperCase());
    } catch (Exception ex) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Rol invalido");
    }
  }

  private UserJpaEntity getExisting(UUID id) {
    return userJpaRepository.findById(id)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
  }

  private UserJpaEntity getByUsername(String username) {
    return userJpaRepository.findByUsernameIgnoreCase(username)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
  }

  private UserJpaEntity saveUpdatedUser(UserJpaEntity user) {
    user.setUpdatedAt(LocalDateTime.now());
    return userJpaRepository.save(user);
  }

  private String normalize(String value) {
    return value.trim();
  }

  private String normalizeEmail(String value) {
    return normalize(value).toLowerCase();
  }

  private UserResponse toResponse(UserJpaEntity entity) {
    return UserResponse.builder()
        .id(entity.getId())
        .nombres(entity.getNombres())
        .correo(entity.getCorreo())
        .username(entity.getUsername())
        .rol(entity.getRol().name())
        .activo(entity.getActivo())
        .deleted(entity.getDeleted())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}
