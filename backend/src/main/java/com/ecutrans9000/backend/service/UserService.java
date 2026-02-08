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

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserJpaRepository userJpaRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuditService auditService;

  public UserResponse createUser(CreateUserRequest request, String actorUsername, String actorRole) {
    validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());
    validateUniqueOnCreate(request.getUsername(), request.getCorreo());

    UserJpaEntity entity = UserJpaEntity.builder()
        .id(UUID.randomUUID())
        .nombres(request.getNombres().trim())
        .correo(request.getCorreo().trim().toLowerCase())
        .username(request.getUsername().trim())
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
    validateUniqueOnUpdate(id, request.getUsername(), request.getCorreo());
    user.setNombres(request.getNombres().trim());
    user.setCorreo(request.getCorreo().trim().toLowerCase());
    user.setUsername(request.getUsername().trim());
    user.setRol(parseRole(request.getRol()));
    user.setActivo(request.getActivo() == null || request.getActivo());
    user.setUpdatedAt(LocalDateTime.now());
    UserJpaEntity saved = userJpaRepository.save(user);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.EDICION, saved.getId().toString(), "users");
    return toResponse(saved);
  }

  public UserResponse getMe(String username) {
    UserJpaEntity user = userJpaRepository.findByUsernameIgnoreCase(username)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    return toResponse(user);
  }

  public UserResponse updateMe(String username, UpdateMyProfileRequest request) {
    UserJpaEntity user = userJpaRepository.findByUsernameIgnoreCase(username)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    validateUniqueOnUpdate(user.getId(), request.getUsername(), request.getCorreo());
    user.setNombres(request.getNombres().trim());
    user.setCorreo(request.getCorreo().trim().toLowerCase());
    user.setUsername(request.getUsername().trim());
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());
      user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    }
    user.setUpdatedAt(LocalDateTime.now());
    UserJpaEntity saved = userJpaRepository.save(user);
    auditService.saveActionAudit(saved.getUsername(), saved.getRol().name(), "USERS", ActionType.EDICION, saved.getId().toString(), "users");
    return toResponse(saved);
  }

  public void softDelete(UUID id, String actorUsername, String actorRole) {
    UserJpaEntity user = getExisting(id);
    user.setDeleted(true);
    user.setActivo(false);
    user.setDeletedAt(LocalDateTime.now());
    user.setDeletedBy(actorUsername);
    user.setUpdatedAt(LocalDateTime.now());
    userJpaRepository.save(user);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.ELIMINADO_LOGICO, user.getId().toString(), "users");
  }

  public void restore(UUID id, String actorUsername, String actorRole) {
    UserJpaEntity user = getExisting(id);
    user.setDeleted(false);
    user.setActivo(true);
    user.setDeletedAt(null);
    user.setDeletedBy(null);
    user.setUpdatedAt(LocalDateTime.now());
    userJpaRepository.save(user);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.EDICION, user.getId().toString(), "users");
  }

  public void activate(UUID id, String actorUsername, String actorRole) {
    UserJpaEntity user = getExisting(id);
    if (Boolean.TRUE.equals(user.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede activar un usuario eliminado. Debe restaurarse primero.");
    }
    user.setActivo(true);
    user.setUpdatedAt(LocalDateTime.now());
    userJpaRepository.save(user);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.EDICION, user.getId().toString(), "users");
  }

  public void deactivate(UUID id, String actorUsername, String actorRole) {
    UserJpaEntity user = getExisting(id);
    if (Boolean.TRUE.equals(user.getDeleted())) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "El usuario ya esta eliminado logicamente.");
    }
    user.setActivo(false);
    user.setUpdatedAt(LocalDateTime.now());
    userJpaRepository.save(user);
    auditService.saveActionAudit(actorUsername, actorRole, "USERS", ActionType.EDICION, user.getId().toString(), "users");
  }

  public void hardDelete(UUID id, String actorUsername, String actorRole) {
    throw new BusinessException(
        HttpStatus.METHOD_NOT_ALLOWED,
        "Eliminacion fisica deshabilitada. Use /users/{id}/soft-delete para eliminacion logica.");
  }

  private void validateUniqueOnCreate(String username, String correo) {
    if (userJpaRepository.existsByUsernameIgnoreCase(username.trim())) {
      throw new BusinessException(HttpStatus.CONFLICT, "Username ya existe");
    }
    if (userJpaRepository.existsByCorreoIgnoreCase(correo.trim())) {
      throw new BusinessException(HttpStatus.CONFLICT, "Correo ya existe");
    }
  }

  private void validateUniqueOnUpdate(UUID id, String username, String correo) {
    if (userJpaRepository.existsByUsernameIgnoreCaseAndIdNot(username.trim(), id)) {
      throw new BusinessException(HttpStatus.CONFLICT, "Username ya existe");
    }
    if (userJpaRepository.existsByCorreoIgnoreCaseAndIdNot(correo.trim(), id)) {
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
