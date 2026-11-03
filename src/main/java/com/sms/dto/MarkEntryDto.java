package com.sms.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkEntryDto {

    private String assessmentId;
    private List<StudentScoreItem> scores;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentScoreItem {
        private String studentId;
        private BigDecimal marksObtained;
        private boolean absent;
        private String remarks;
    }
}

