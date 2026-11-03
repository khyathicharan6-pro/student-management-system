package com.sms.repository;

import com.sms.entity.AttendanceRecord;
import com.sms.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, String> {
    List<AttendanceRecord> findBySessionId(String sessionId);
    List<AttendanceRecord> findByStudentId(String studentId);
    Optional<AttendanceRecord> findBySessionIdAndStudentId(String sessionId, String studentId);
    long countByStudentIdAndStatus(String studentId, AttendanceStatus status);
    long countByStudentId(String studentId);
}

