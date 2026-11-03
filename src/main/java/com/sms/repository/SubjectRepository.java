package com.sms.repository;

import com.sms.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, String> {
    Optional<Subject> findByCode(String code);
    List<Subject> findByDepartmentId(String departmentId);
    boolean existsByCode(String code);
}

