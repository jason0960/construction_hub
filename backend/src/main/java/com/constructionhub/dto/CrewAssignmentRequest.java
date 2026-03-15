package com.constructionhub.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CrewAssignmentRequest {
    private Long workerId;
    private String roleOnJob;
    private LocalDate startDate;
    private LocalDate endDate;
}
