package com.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamCreateDto {

    @NotBlank(message = "Academic year is required")
    private String academicYearId;

    @NotBlank(message = "Semester is required")
    private String semesterId;

    @NotBlank(message = "Exam name is required")
    private String name;

    @NotBlank(message = "Exam type is required")
    private String examType; // INTERNAL_ASSESSMENT, MID_EXAM, PRACTICAL, FINAL_EXAM

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}

