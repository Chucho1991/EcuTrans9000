package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.RoleModuleAccessJpaEntity;
import com.ecutrans9000.backend.adapters.out.persistence.entity.RoleModuleAccessJpaEntity.RoleModuleAccessId;
import com.ecutrans9000.backend.domain.user.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para consultar y actualizar la matriz de acceso rol-modulo.
 */
public interface RoleModuleAccessJpaRepository extends JpaRepository<RoleModuleAccessJpaEntity, RoleModuleAccessId> {
  List<RoleModuleAccessJpaEntity> findAllByRoleNameOrderByModuleKey(UserRole roleName);
}
