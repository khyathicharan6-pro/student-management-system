package com.sms.dto;

import com.sms.entity.AttendanceStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEntryDto {

    private String sessionId;
    private List<RecordItem> records;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecordItem {
        private String studentId;
        private AttendanceStatus status;
        private String remarks;
    }
}

