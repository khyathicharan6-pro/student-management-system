package com.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"exam_id", "class_id", "subject_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "exam_id", nullable = false, length = 36)
    private String examId;

    @Column(name = "class_id", nullable = false, length = 36)
    private String classId;

    @Column(name = "subject_id", nullable = false, length = 36)
    private String subjectId;

    @Column(name = "created_by_teacher_id", nullable = false, length = 36)
    private String createdByTeacherId;

    @Column(name = "maximum_marks", nullable = false, precision = 6, scale = 2)
    private BigDecimal maximumMarks;

    @Column(name = "passing_marks", nullable = false, precision = 6, scale = 2)
    private BigDecimal passingMarks;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AssessmentStatus status = AssessmentStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "published_by", length = 36)
    private String publishedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
}

