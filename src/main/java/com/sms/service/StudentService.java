package com.sms.service;

import com.sms.dto.StudentCreateDto;
import com.sms.dto.StudentUpdateDto;
import com.sms.entity.*;
import com.sms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassRepository classRepository;
    private final AcademicYearRepository academicYearRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public List<StudentProfile> getAllStudents() {
        return studentProfileRepository.findAll();
    }

    public List<StudentProfile> searchStudents(String keyword, String classId) {
        List<StudentProfile> list;
        if (classId != null && !classId.isBlank()) {
            List<String> studentIdsInClass = enrollmentRepository.findByClassId(classId).stream()
                    .map(Enrollment::getStudentId)
                    .toList();
            list = studentProfileRepository.findAllById(studentIdsInClass);
        } else {
            list = studentProfileRepository.findAll();
        }

        if (keyword != null && !keyword.isBlank()) {
            String lower = keyword.toLowerCase();
            list = list.stream()
                    .filter(s -> (s.getFullName() != null && s.getFullName().toLowerCase().contains(lower)) ||
                                 (s.getRollNumber() != null && s.getRollNumber().toLowerCase().contains(lower)) ||
                                 (s.getRegistrationNumber() != null && s.getRegistrationNumber().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public Optional<StudentProfile> getStudentById(String id) {
        return studentProfileRepository.findById(id);
    }

    public Optional<StudentProfile> getStudentByUserId(String userId) {
        return studentProfileRepository.findByUserId(userId);
    }

    @Transactional
    public StudentProfile createStudent(StudentCreateDto dto, String actorId) {
        if (studentProfileRepository.existsByRollNumber(dto.getRollNumber())) {
            throw new IllegalArgumentException("A student with Roll Number " + dto.getRollNumber() + " already exists.");
        }
        if (studentProfileRepository.existsByRegistrationNumber(dto.getRegistrationNumber())) {
            throw new IllegalArgumentException("A student with Registration Number " + dto.getRegistrationNumber() + " already exists.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("A user with Email " + dto.getEmail() + " already exists.");
        }

        ClassEntity classEntity = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + dto.getClassId()));

        String username = dto.getRollNumber().toLowerCase().replaceAll("[^a-z0-9]", "");
        if (userRepository.existsByUsername(username)) {
            username = dto.getEmail().split("@")[0].toLowerCase();
        }

        String rawPassword = (dto.getDefaultPassword() != null && !dto.getDefaultPassword().isBlank()) 
                ? dto.getDefaultPassword() 
                : "password123";

        // 1. Create User
        User user = User.builder()
                .username(username)
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(Role.ROLE_STUDENT)
                .active(true)
                .build();
        userRepository.save(user);

        // 2. Create Student Profile
        StudentProfile profile = StudentProfile.builder()
                .user(user)
                .rollNumber(dto.getRollNumber().trim())
                .registrationNumber(dto.getRegistrationNumber().trim())
                .fullName(dto.getFullName().trim())
                .phoneNumber(dto.getPhoneNumber())
                .departmentId(dto.getDepartmentId())
                .course(dto.getCourse().trim())
                .currentAcademicYearId(classEntity.getAcademicYearId())
                .currentSemesterId(classEntity.getSemesterId())
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        studentProfileRepository.save(profile);

        // 3. Create Class Enrollment
        Enrollment enrollment = Enrollment.builder()
                .studentId(profile.getId())
                .classId(classEntity.getId())
                .academicYearId(classEntity.getAcademicYearId())
                .status("ENROLLED")
                .build();
        enrollmentRepository.save(enrollment);

        auditService.log(actorId, null, "CREATE_STUDENT", "student_profiles", profile.getId(), null, "Created roll=" + profile.getRollNumber());
        log.info("Successfully created student: {} (Username: {}, Roll: {})", profile.getFullName(), username, profile.getRollNumber());
        return profile;
    }

    @Transactional
    public StudentProfile updateStudent(String id, StudentUpdateDto dto, String actorId) {
        StudentProfile student = studentProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + id));

        String before = "name=" + student.getFullName() + ", status=" + student.getAccountStatus();

        student.setFullName(dto.getFullName().trim());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setCourse(dto.getCourse().trim());

        if (dto.getAccountStatus() != null) {
            try {
                student.setAccountStatus(AccountStatus.valueOf(dto.getAccountStatus()));
            } catch (Exception ignored) {}
        }

        // Update User email
        User user = student.getUser();
        if (user != null && !user.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email already taken by another account.");
            }
            user.setEmail(dto.getEmail());
            userRepository.save(user);
        }

        studentProfileRepository.save(student);
        String after = "name=" + student.getFullName() + ", status=" + student.getAccountStatus();
        auditService.log(actorId, null, "UPDATE_STUDENT", "student_profiles", student.getId(), before, after);
        return student;
    }

    @Transactional
    public void toggleAccountStatus(String id, AccountStatus status, String actorId) {
        StudentProfile student = studentProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));
        student.setAccountStatus(status);
        if (student.getUser() != null) {
            student.getUser().setActive(status == AccountStatus.ACTIVE);
            userRepository.save(student.getUser());
        }
        studentProfileRepository.save(student);
        auditService.log(actorId, null, "STATUS_CHANGE", "student_profiles", id, null, "New status=" + status);
    }

    @Transactional
    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return false;
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditService.log(userId, null, "CHANGE_PASSWORD", "users", userId, null, "Password updated successfully");
        return true;
    }
}

