package com.constructionhub.dto;

import com.constructionhub.entity.JobStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class JobRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    private Long clientId;
    private JobStatus status;
    private String siteAddress;
    private String siteCity;
    private String siteState;
    private String siteZip;
    private String siteUnit;
    private BigDecimal contractPrice;
    private LocalDate startDate;
    private LocalDate estimatedEndDate;
    private LocalDate actualEndDate;

    // Inline client creation (if clientId is null)
    private String clientName;
    private String clientPhone;
    private String clientEmail;
}
