package com.sms.repository;

import com.sms.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {
    List<Enrollment> findByClassId(String classId);
    List<Enrollment> findByStudentId(String studentId);
    Optional<Enrollment> findByStudentIdAndAcademicYearId(String studentId, String academicYearId);
    boolean existsByStudentIdAndAcademicYearId(String studentId, String academicYearId);
}

