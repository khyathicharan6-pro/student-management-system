package com.sms.repository;

import com.sms.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, String> {

    Optional<StudentProfile> findByUserId(String userId);

    Optional<StudentProfile> findByRollNumber(String rollNumber);

    Optional<StudentProfile> findByRegistrationNumber(String registrationNumber);

    boolean existsByRollNumber(String rollNumber);

    boolean existsByRegistrationNumber(String registrationNumber);
}

