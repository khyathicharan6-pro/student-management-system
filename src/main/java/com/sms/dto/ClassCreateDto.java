package com.sms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassCreateDto {

    @NotBlank(message = "Class name is required")
    private String name;

    @NotBlank(message = "Department is required")
    private String departmentId;

    @NotBlank(message = "Academic year is required")
    private String academicYearId;

    @NotBlank(message = "Semester is required")
    private String semesterId;

    @NotBlank(message = "Section is required")
    private String section;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Builder.Default
    private int capacity = 60;
}

