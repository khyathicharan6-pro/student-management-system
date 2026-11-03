package com.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grade_scales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeScale {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "min_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal minPercentage;

    @Column(name = "max_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxPercentage;

    @Column(name = "grade_letter", nullable = false, length = 5)
    private String gradeLetter;

    @Column(name = "grade_point", nullable = false, precision = 4, scale = 2)
    private BigDecimal gradePoint;

    @Column(length = 100)
    private String description;

    @Column(name = "is_passing", nullable = false)
    @Builder.Default
    private boolean passing = true;

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

