package com.constructionhub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TimeEntryRequest {
    @NotNull(message = "Worker ID is required")
    private Long workerId;

    @NotNull(message = "Date is required")
    private LocalDate entryDate;

    @NotNull(message = "Hours is required")
    private BigDecimal hours;

    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String notes;
}
