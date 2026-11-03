package com.sms.config;

import com.sms.entity.*;
import com.sms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking baseline administrative and user accounts...");

        // 1. Seed Teacher Account (user login only — profile created by teacher via
        // setup)
        if (userRepository.findByUsername("teacher").isEmpty()) {
            User teacherUser = User.builder()
                    .id("user-teacher-001")
                    .username("teacher")
                    .email("teacher@college.edu")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.ROLE_TEACHER)
                    .active(true)
                    .build();
            userRepository.save(teacherUser);
            log.info("Initialized default Teacher account: teacher / password123");
        }

        // 2. Seed Student Account (user login only — profile linked by teacher)
        if (userRepository.findByUsername("student").isEmpty()) {
            User studentUser = User.builder()
                    .id("user-student-001")
                    .username("student")
                    .email("student@college.edu")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.ROLE_STUDENT)
                    .active(true)
                    .build();
            userRepository.save(studentUser);
            log.info("Initialized default Student account: student / password123");
        }

        // 3. Seed Admin Account
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = User.builder()
                    .id("user-admin-001")
                    .username("admin")
                    .email("admin@college.edu")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.ROLE_ADMIN)
                    .active(true)
                    .build();
            userRepository.save(adminUser);
            log.info("Initialized default Admin account: admin / password123");
        }
    }
}
