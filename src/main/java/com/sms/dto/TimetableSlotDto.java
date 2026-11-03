package com.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSlotDto {

    private String id;

    @NotBlank(message = "Class is required")
    private String classId;

    @NotBlank(message = "Subject is required")
    private String subjectId;

    @NotBlank(message = "Teacher is required")
    private String teacherId;

    @NotBlank(message = "Weekday is required")
    private String weekday; // MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Classroom is required")
    private String classroom;

    @NotBlank(message = "Academic year is required")
    private String academicYearId;

    @NotBlank(message = "Semester is required")
    private String semesterId;
}

