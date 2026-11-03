package com.sms.repository;

import com.sms.entity.Assessment;
import com.sms.entity.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, String> {
    List<Assessment> findByClassId(String classId);
    List<Assessment> findByExamId(String examId);
    List<Assessment> findByClassIdAndStatus(String classId, AssessmentStatus status);
    Optional<Assessment> findByExamIdAndClassIdAndSubjectId(String examId, String classId, String subjectId);
}

