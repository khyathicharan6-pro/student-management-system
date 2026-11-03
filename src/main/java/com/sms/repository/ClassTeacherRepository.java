package com.sms.repository;

import com.sms.entity.ClassTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassTeacherRepository extends JpaRepository<ClassTeacher, String> {
    List<ClassTeacher> findByTeacherId(String teacherId);
    List<ClassTeacher> findByClassId(String classId);
    Optional<ClassTeacher> findByClassIdAndTeacherId(String classId, String teacherId);
    boolean existsByClassIdAndTeacherId(String classId, String teacherId);
}

