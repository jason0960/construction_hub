package com.constructionhub.dto;

import com.constructionhub.entity.PermitStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PermitRequest {
    @NotBlank(message = "Permit type is required")
    private String permitType;
    private String permitNumber;
    private String issuingAuthority;
    private PermitStatus status;
    private BigDecimal fee;
    private LocalDate applicationDate;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private Integer reminderDaysBefore;
    private String notes;
}
