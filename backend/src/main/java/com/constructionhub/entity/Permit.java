package com.constructionhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "permits")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Permit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "permit_type", nullable = false)
    private String permitType;

    @Column(name = "permit_number")
    private String permitNumber;

    @Column(name = "issuing_authority")
    private String issuingAuthority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermitStatus status;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal fee;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "reminder_days_before", nullable = false)
    private Integer reminderDaysBefore;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = PermitStatus.PENDING;
        }
        if (fee == null) {
            fee = BigDecimal.ZERO;
        }
        if (reminderDaysBefore == null) {
            reminderDaysBefore = 30;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
