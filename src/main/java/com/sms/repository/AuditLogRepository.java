package com.sms.repository;

import com.sms.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findTop50ByOrderByTimestampDesc();
    List<AuditLog> findByActorIdOrderByTimestampDesc(String actorId);
}

