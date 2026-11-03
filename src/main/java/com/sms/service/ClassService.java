package com.sms.service;

import com.sms.dto.ClassCreateDto;
import com.sms.entity.ClassEntity;
import com.sms.entity.ClassTeacher;
import com.sms.entity.Enrollment;
import com.sms.entity.StudentProfile;
import com.sms.repository.ClassRepository;
import com.sms.repository.ClassTeacherRepository;
import com.sms.repository.EnrollmentRepository;
import com.sms.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassTeacherRepository classTeacherRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AuditService auditService;

    public List<ClassEntity> getAllClasses() {
        return classRepository.findAll();
    }

    public List<ClassEntity> getClassesForTeacher(String teacherId) {
        List<String> assignedClassIds = classTeacherRepository.findByTeacherId(teacherId).stream()
                .map(ClassTeacher::getClassId)
                .toList();
        if (assignedClassIds.isEmpty()) {
            return classRepository.findAll(); // Fallback if admin or general faculty
        }
        return classRepository.findAllById(assignedClassIds);
    }

    public Optional<ClassEntity> getClassById(String classId) {
        return classRepository.findById(classId);
    }

    public boolean verifyTeacherOwnsClass(String teacherId, String classId) {
        return classTeacherRepository.existsByClassIdAndTeacherId(classId, teacherId);
    }

    @Transactional
    public ClassEntity createClass(ClassCreateDto dto, String actorId) {
        ClassEntity classEntity = ClassEntity.builder()
                .name(dto.getName().trim())
                .departmentId(dto.getDepartmentId())
                .academicYearId(dto.getAcademicYearId())
                .semesterId(dto.getSemesterId())
                .section(dto.getSection().trim())
                .capacity(dto.getCapacity())
                .build();

        classRepository.save(classEntity);
        auditService.log(actorId, null, "CREATE_CLASS", "classes", classEntity.getId(), null, "Created " + classEntity.getName());
        return classEntity;
    }

    @Transactional
    public void assignTeacherToClass(String classId, String teacherId, boolean isPrimary, String actorId) {
        if (!classTeacherRepository.existsByClassIdAndTeacherId(classId, teacherId)) {
            ClassTeacher ct = ClassTeacher.builder()
                    .classId(classId)
                    .teacherId(teacherId)
                    .primary(isPrimary)
                    .build();
            classTeacherRepository.save(ct);
            auditService.log(actorId, null, "ASSIGN_TEACHER", "class_teachers", ct.getId(), null, "Assigned teacher=" + teacherId + " to class=" + classId);
        }
    }

    public List<StudentProfile> getEnrolledStudents(String classId) {
        List<String> studentIds = enrollmentRepository.findByClassId(classId).stream()
                .map(Enrollment::getStudentId)
                .toList();
        return studentProfileRepository.findAllById(studentIds);
    }
}

