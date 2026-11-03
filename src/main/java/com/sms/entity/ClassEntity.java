package com.sms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "classes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"department_id", "academic_year_id", "semester_id", "section"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "department_id", nullable = false, length = 36)
    private String departmentId;

    @Column(name = "academic_year_id", nullable = false, length = 36)
    private String academicYearId;

    @Column(name = "semester_id", nullable = false, length = 36)
    private String semesterId;

    @Column(nullable = false, length = 10)
    private String section;

    @Column(nullable = false)
    @Builder.Default
    private int capacity = 60;

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

