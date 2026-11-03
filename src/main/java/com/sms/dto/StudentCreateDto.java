package com.sms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCreateDto {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "College roll number is required")
    private String rollNumber;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "Department is required")
    private String departmentId;

    @NotBlank(message = "Course is required")
    private String course;

    @NotBlank(message = "Class is required")
    private String classId;

    private String defaultPassword;
}

