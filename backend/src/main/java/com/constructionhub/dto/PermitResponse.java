package com.constructionhub.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PermitResponse {
    private Long id;
    private Long jobId;
    private String permitType;
    private String permitNumber;
    private String issuingAuthority;
    private String status;
    private BigDecimal fee;
    private LocalDate applicationDate;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private Integer reminderDaysBefore;
    private String notes;
    private LocalDateTime createdAt;
}
