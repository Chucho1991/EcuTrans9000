package com.ecutrans9000.backend.adapters.out.persistence.entity;

import com.ecutrans9000.backend.domain.user.SystemModule;
import com.ecutrans9000.backend.domain.user.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que representa la configuracion persistida de acceso a un modulo por rol.
 */
@Entity
@Table(name = "role_module_access")
@IdClass(RoleModuleAccessJpaEntity.RoleModuleAccessId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleModuleAccessJpaEntity {

  @Id
  @Enumerated(EnumType.STRING)
  @Column(name = "role_name", nullable = false, length = 50)
  private UserRole roleName;

  @Id
  @Enumerated(EnumType.STRING)
  @Column(name = "module_key", nullable = false, length = 50)
  private SystemModule moduleKey;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class RoleModuleAccessId implements Serializable {
    private UserRole roleName;
    private SystemModule moduleKey;
  }
}
