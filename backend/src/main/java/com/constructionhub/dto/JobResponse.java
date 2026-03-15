package com.constructionhub.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate estimatedEndDate;
    private LocalDate actualEndDate;
    private BigDecimal contractPrice;

    // Site info
    private String siteAddress;
    private String siteCity;
    private String siteState;
    private String siteZip;
    private String siteUnit;

    // Client info (inline)
    private Long clientId;
    private String clientName;
    private String clientPhone;
    private String clientEmail;

    // Financials (owner only)
    private BigDecimal laborCost;
    private BigDecimal materialsCost;
    private BigDecimal permitFees;
    private BigDecimal profit;

    // Metadata
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
