package com.sms.repository;

import com.sms.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkRepository extends JpaRepository<Mark, String> {
    List<Mark> findByAssessmentId(String assessmentId);
    List<Mark> findByStudentId(String studentId);
    Optional<Mark> findByAssessmentIdAndStudentId(String assessmentId, String studentId);
}

