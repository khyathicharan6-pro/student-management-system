package com.sms.service;

import com.sms.dto.TimetableSlotDto;
import com.sms.entity.ClassEntity;
import com.sms.entity.Enrollment;
import com.sms.entity.Subject;
import com.sms.entity.TeacherProfile;
import com.sms.entity.TimetableSlot;
import com.sms.repository.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableService {

    private final TimetableSlotRepository timetableSlotRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditService auditService;

    public List<TimetableSlotView> getTimetableForClass(String classId) {
        List<TimetableSlot> slots = timetableSlotRepository.findByClassId(classId);
        return mapToViews(slots);
    }

    public List<TimetableSlotView> getTimetableForTeacher(String teacherId) {
        List<TimetableSlot> slots = timetableSlotRepository.findByTeacherId(teacherId);
        return mapToViews(slots);
    }

    public List<TimetableSlotView> getTimetableForStudent(String studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        if (enrollments.isEmpty()) {
            return List.of();
        }
        String classId = enrollments.get(0).getClassId();
        return getTimetableForClass(classId);
    }

    @Transactional
    public TimetableSlot createOrUpdateSlot(TimetableSlotDto dto, String actorId) {
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new IllegalArgumentException("Class end time must be strictly after start time.");
        }

        // 1. Conflict Check: Class/Section Overlap
        List<TimetableSlot> classConflicts = timetableSlotRepository.findClassConflicts(
                dto.getClassId(), dto.getWeekday(), dto.getStartTime(), dto.getEndTime(), dto.getId());
        if (!classConflicts.isEmpty()) {
            throw new IllegalArgumentException("Schedule Conflict: Class already has another session scheduled on " +
                    dto.getWeekday() + " between " + dto.getStartTime() + " and " + dto.getEndTime());
        }

        // 2. Conflict Check: Teacher Overlap
        List<TimetableSlot> teacherConflicts = timetableSlotRepository.findTeacherConflicts(
                dto.getTeacherId(), dto.getWeekday(), dto.getStartTime(), dto.getEndTime(), dto.getId());
        if (!teacherConflicts.isEmpty()) {
            throw new IllegalArgumentException("Schedule Conflict: Teacher is already assigned to another class on " +
                    dto.getWeekday() + " between " + dto.getStartTime() + " and " + dto.getEndTime());
        }

        // 3. Conflict Check: Room Reservation Clash
        List<TimetableSlot> roomConflicts = timetableSlotRepository.findRoomConflicts(
                dto.getClassroom(), dto.getWeekday(), dto.getStartTime(), dto.getEndTime(), dto.getId());
        if (!roomConflicts.isEmpty()) {
            throw new IllegalArgumentException("Schedule Conflict: Classroom '" + dto.getClassroom() +
                    "' is already booked on " + dto.getWeekday() + " between " + dto.getStartTime() + " and " + dto.getEndTime());
        }

        TimetableSlot slot;
        if (dto.getId() != null && !dto.getId().isBlank()) {
            slot = timetableSlotRepository.findById(dto.getId()).orElseGet(TimetableSlot::new);
        } else {
            slot = new TimetableSlot();
        }

        slot.setClassId(dto.getClassId());
        slot.setSubjectId(dto.getSubjectId());
        slot.setTeacherId(dto.getTeacherId());
        slot.setWeekday(dto.getWeekday().toUpperCase());
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setClassroom(dto.getClassroom().trim());
        slot.setAcademicYearId(dto.getAcademicYearId());
        slot.setSemesterId(dto.getSemesterId());

        timetableSlotRepository.save(slot);
        auditService.log(actorId, null, "SAVE_TIMETABLE_SLOT", "timetable_slots", slot.getId(), null, 
                "Slot: " + slot.getWeekday() + " " + slot.getStartTime() + "-" + slot.getEndTime() + " in " + slot.getClassroom());
        return slot;
    }

    @Transactional
    public void deleteSlot(String id, String actorId) {
        timetableSlotRepository.deleteById(id);
        auditService.log(actorId, null, "DELETE_TIMETABLE_SLOT", "timetable_slots", id, null, "Deleted slot");
    }

    private List<TimetableSlotView> mapToViews(List<TimetableSlot> slots) {
        List<TimetableSlotView> views = new ArrayList<>();
        for (TimetableSlot s : slots) {
            String className = classRepository.findById(s.getClassId()).map(ClassEntity::getName).orElse("Class");
            String subjectName = subjectRepository.findById(s.getSubjectId()).map(Subject::getName).orElse("Subject");
            String teacherName = teacherProfileRepository.findById(s.getTeacherId()).map(TeacherProfile::getFullName).orElse("Faculty");

            views.add(TimetableSlotView.builder()
                    .id(s.getId())
                    .classId(s.getClassId())
                    .className(className)
                    .subjectId(s.getSubjectId())
                    .subjectName(subjectName)
                    .teacherId(s.getTeacherId())
                    .teacherName(teacherName)
                    .weekday(s.getWeekday())
                    .startTime(s.getStartTime().toString())
                    .endTime(s.getEndTime().toString())
                    .classroom(s.getClassroom())
                    .build());
        }
        return views;
    }

    @Getter
    @Builder
    public static class TimetableSlotView {
        private String id;
        private String classId;
        private String className;
        private String subjectId;
        private String subjectName;
        private String teacherId;
        private String teacherName;
        private String weekday;
        private String startTime;
        private String endTime;
        private String classroom;
    }
}

