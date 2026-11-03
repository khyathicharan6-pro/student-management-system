package com.sms.service;

import com.sms.dto.SubjectCreateDto;
import com.sms.entity.ClassSubject;
import com.sms.entity.Subject;
import com.sms.repository.ClassSubjectRepository;
import com.sms.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final AuditService auditService;

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public List<Subject> getSubjectsByDepartment(String deptId) {
        return subjectRepository.findByDepartmentId(deptId);
    }

    public Optional<Subject> getSubjectById(String id) {
        return subjectRepository.findById(id);
    }

    @Transactional
    public Subject createSubject(SubjectCreateDto dto, String actorId) {
        if (subjectRepository.existsByCode(dto.getCode().trim().toUpperCase())) {
            throw new IllegalArgumentException("Subject with code " + dto.getCode() + " already exists.");
        }

        Subject subject = Subject.builder()
                .departmentId(dto.getDepartmentId())
                .code(dto.getCode().trim().toUpperCase())
                .name(dto.getName().trim())
                .credits(dto.getCredits())
                .elective(dto.isElective())
                .build();

        subjectRepository.save(subject);
        auditService.log(actorId, null, "CREATE_SUBJECT", "subjects", subject.getId(), null, "Created code=" + subject.getCode());
        return subject;
    }

    @Transactional
    public void assignSubjectToClass(String classId, String subjectId, String teacherId, String actorId) {
        Optional<ClassSubject> existing = classSubjectRepository.findByClassIdAndSubjectId(classId, subjectId);
        if (existing.isPresent()) {
            ClassSubject cs = existing.get();
            cs.setTeacherId(teacherId);
            classSubjectRepository.save(cs);
        } else {
            ClassSubject cs = ClassSubject.builder()
                    .classId(classId)
                    .subjectId(subjectId)
                    .teacherId(teacherId)
                    .build();
            classSubjectRepository.save(cs);
        }
        auditService.log(actorId, null, "ASSIGN_CLASS_SUBJECT", "class_subjects", subjectId, null, "Class " + classId + " mapped to subject " + subjectId);
    }

    public List<Subject> getSubjectsForClass(String classId) {
        List<String> subjectIds = classSubjectRepository.findByClassId(classId).stream()
                .map(ClassSubject::getSubjectId)
                .toList();
        return subjectRepository.findAllById(subjectIds);
    }

    public List<Subject> getSubjectsForTeacher(String teacherId) {
        List<String> subjectIds = classSubjectRepository.findByTeacherId(teacherId).stream()
                .map(ClassSubject::getSubjectId)
                .toList();
        if (subjectIds.isEmpty()) {
            return subjectRepository.findAll();
        }
        return subjectRepository.findAllById(subjectIds);
    }
}

