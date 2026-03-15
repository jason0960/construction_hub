package com.constructionhub.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TimeEntryResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long workerId;
    private String workerName;
    private LocalDate entryDate;
    private BigDecimal hours;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String enteredByName;
    private String notes;
    private LocalDateTime createdAt;
}
