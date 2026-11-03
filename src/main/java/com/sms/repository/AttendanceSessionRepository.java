package com.sms.repository;

import com.sms.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, String> {
    List<AttendanceSession> findByClassIdAndSessionDate(String classId, LocalDate sessionDate);
    List<AttendanceSession> findByClassIdAndSubjectId(String classId, String subjectId);
    Optional<AttendanceSession> findByClassIdAndSubjectIdAndSessionDateAndSlotNumber(
            String classId, String subjectId, LocalDate sessionDate, Integer slotNumber);
    List<AttendanceSession> findByClassId(String classId);
}

