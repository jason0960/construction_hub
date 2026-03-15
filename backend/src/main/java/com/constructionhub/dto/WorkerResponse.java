package com.constructionhub.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class WorkerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String trade;
    private BigDecimal hourlyRate;
    private String status;
    private List<WorkerJobAssignment> currentJobs;

    @Data
    @Builder
    public static class WorkerJobAssignment {
        private Long jobId;
        private String jobTitle;
        private String roleOnJob;
        private String assignmentStatus;
    }
}
