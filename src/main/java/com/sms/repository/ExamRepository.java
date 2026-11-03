package com.sms.repository;

import com.sms.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {
    List<Exam> findByAcademicYearIdAndSemesterId(String academicYearId, String semesterId);
    List<Exam> findByActiveTrue();
}

