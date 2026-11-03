package com.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "marks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"assessment_id", "student_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mark {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "assessment_id", nullable = false, length = 36)
    private String assessmentId;

    @Column(name = "student_id", nullable = false, length = 36)
    private String studentId;

    @Column(name = "marks_obtained", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal marksObtained = BigDecimal.ZERO;

    @Column(name = "is_absent", nullable = false)
    @Builder.Default
    private boolean absent = false;

    @Column(length = 255)
    private String remarks;

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

