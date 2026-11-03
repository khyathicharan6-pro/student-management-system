package com.sms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentCreateDto {

    @NotBlank(message = "Exam is required")
    private String examId;

    @NotBlank(message = "Class is required")
    private String classId;

    @NotBlank(message = "Subject is required")
    private String subjectId;

    @NotNull(message = "Maximum marks is required")
    @DecimalMin(value = "1.0", message = "Max marks must be at least 1.0")
    private BigDecimal maximumMarks;

    @NotNull(message = "Passing marks is required")
    @DecimalMin(value = "0.0", message = "Passing marks must be at least 0.0")
    private BigDecimal passingMarks;

    @NotNull(message = "Assessment date is required")
    private LocalDate assessmentDate;
}

