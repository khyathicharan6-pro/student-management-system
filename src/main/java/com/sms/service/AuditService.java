package com.sms.service;

import com.sms.entity.AuditLog;
import com.sms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String actorId, String ipAddress, String action, String entityType, String entityId, String beforeState, String afterState) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .actorId(actorId)
                    .ipAddress(ipAddress)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .beforeState(beforeState)
                    .afterState(afterState)
                    .build();
            auditLogRepository.save(auditLog);
            log.info("Audit logged: action={}, entityType={}, entityId={}, actorId={}", action, entityType, entityId, actorId);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }
}

