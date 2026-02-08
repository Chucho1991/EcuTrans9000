package com.ecutrans9000.backend.adapters.out.persistence.repository;

import com.ecutrans9000.backend.adapters.out.persistence.entity.ApiAuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiAuditLogJpaRepository extends JpaRepository<ApiAuditLogJpaEntity, Long> {
}
