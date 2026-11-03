package com.sms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectCreateDto {

    @NotBlank(message = "Department is required")
    private String departmentId;

    @NotBlank(message = "Subject code is required")
    private String code;

    @NotBlank(message = "Subject name is required")
    private String name;

    @DecimalMin(value = "0.5", message = "Credits must be at least 0.5")
    @Builder.Default
    private BigDecimal credits = new BigDecimal("3.0");

    @Builder.Default
    private boolean elective = false;
}

