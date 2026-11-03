package com.sms.repository;

import com.sms.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, String> {

    Optional<TeacherProfile> findByUserId(String userId);

    Optional<TeacherProfile> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);
}

