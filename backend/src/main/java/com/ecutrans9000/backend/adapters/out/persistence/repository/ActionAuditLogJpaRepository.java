package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.ActionAuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionAuditLogJpaRepository extends JpaRepository<ActionAuditLogJpaEntity, Long> {
}
