package com.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeScaleDto {

    private String id;

    @NotBlank(message = "Scale name is required")
    private String name;

    @NotNull(message = "Minimum percentage is required")
    private BigDecimal minPercentage;

    @NotNull(message = "Maximum percentage is required")
    private BigDecimal maxPercentage;

    @NotBlank(message = "Grade letter is required")
    private String gradeLetter;

    @NotNull(message = "Grade point is required")
    private BigDecimal gradePoint;

    private String description;

    @Builder.Default
    private boolean passing = true;
}

