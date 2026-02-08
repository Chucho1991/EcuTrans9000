package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID>, JpaSpecificationExecutor<UserJpaEntity> {
  boolean existsByUsernameIgnoreCase(String username);
  boolean existsByCorreoIgnoreCase(String correo);
  boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);
  boolean existsByCorreoIgnoreCaseAndIdNot(String correo, UUID id);

  Optional<UserJpaEntity> findByUsernameIgnoreCase(String username);
  Optional<UserJpaEntity> findByUsernameIgnoreCaseOrCorreoIgnoreCase(String username, String correo);
}
