package com.constructionhub.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CrewAssignmentResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long workerId;
    private String workerName;
    private String workerTrade;
    private BigDecimal workerHourlyRate;
    private String roleOnJob;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private BigDecimal totalHours;
}
