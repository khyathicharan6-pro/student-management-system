package com.sms.repository;

import com.sms.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, String> {
    List<ClassEntity> findByDepartmentId(String departmentId);
    List<ClassEntity> findByAcademicYearIdAndSemesterId(String academicYearId, String semesterId);
}

