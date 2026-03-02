package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.ClienteJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, UUID>, JpaSpecificationExecutor<ClienteJpaEntity> {

  boolean existsByDocumentoNorm(String documentoNorm);

  boolean existsByDocumentoNormAndIdNot(String documentoNorm, UUID id);

  Optional<ClienteJpaEntity> findByDocumentoNorm(String documentoNorm);

  List<ClienteJpaEntity> findAllByDeletedFalse();
}
