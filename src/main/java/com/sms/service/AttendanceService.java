package com.sms.service;

import com.sms.entity.*;
import com.sms.repository.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AuditService auditService;

    @Transactional
    public AttendanceSession getOrCreateSession(String classId, String subjectId, String teacherId, LocalDate date, Integer slotNumber) {
        return sessionRepository.findByClassIdAndSubjectIdAndSessionDateAndSlotNumber(classId, subjectId, date, slotNumber)
                .orElseGet(() -> {
                    AttendanceSession session = AttendanceSession.builder()
                            .classId(classId)
                            .subjectId(subjectId)
                            .teacherId(teacherId)
                            .sessionDate(date)
                            .slotNumber(slotNumber)
                            .build();
                    return sessionRepository.save(session);
                });
    }

    public List<AttendanceRecord> getRecordsForSession(String sessionId) {
        return recordRepository.findBySessionId(sessionId);
    }

    @Transactional
    public void saveSessionAttendance(String sessionId, Map<String, AttendanceStatus> studentStatusMap, String actorId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance session not found: " + sessionId));

        for (Map.Entry<String, AttendanceStatus> entry : studentStatusMap.entrySet()) {
            String studentId = entry.getKey();
            AttendanceStatus status = entry.getValue();

            Optional<AttendanceRecord> existing = recordRepository.findBySessionIdAndStudentId(sessionId, studentId);
            if (existing.isPresent()) {
                AttendanceRecord rec = existing.get();
                rec.setStatus(status);
                recordRepository.save(rec);
            } else {
                AttendanceRecord rec = AttendanceRecord.builder()
                        .sessionId(sessionId)
                        .studentId(studentId)
                        .status(status)
                        .build();
                recordRepository.save(rec);
            }
        }

        auditService.log(actorId, null, "SUBMIT_ATTENDANCE", "attendance_sessions", sessionId, null, "Recorded attendance for " + studentStatusMap.size() + " students");
    }

    public StudentAttendanceSummary getStudentAttendanceSummary(String studentId) {
        long totalSessions = recordRepository.countByStudentId(studentId);
        long presentCount = recordRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.PRESENT);
        long lateCount = recordRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.LATE);
        long absentCount = recordRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.ABSENT);
        long excusedCount = recordRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.EXCUSED);

        double percentage = 0.0;
        if (totalSessions > 0) {
            // Formula: (Present + Late + Excused) / Total
            double attended = presentCount + (lateCount * 0.75) + excusedCount;
            percentage = Math.min(100.0, (attended / totalSessions) * 100.0);
        }

        return StudentAttendanceSummary.builder()
                .studentId(studentId)
                .totalSessions(totalSessions)
                .presentCount(presentCount)
                .lateCount(lateCount)
                .absentCount(absentCount)
                .excusedCount(excusedCount)
                .attendancePercentage(Math.round(percentage * 10.0) / 10.0)
                .build();
    }

    @Getter
    @Builder
    public static class StudentAttendanceSummary {
        private String studentId;
        private long totalSessions;
        private long presentCount;
        private long lateCount;
        private long absentCount;
        private long excusedCount;
        private double attendancePercentage;
    }
}

