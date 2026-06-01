package tn.iteam.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
