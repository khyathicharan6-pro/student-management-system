package com.sms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementDto {

    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String targetClassId; // null = college-wide

    @Builder.Default
    private String targetRole = "ALL"; // ALL, TEACHER, STUDENT

    private LocalDateTime expiryDate;
}

