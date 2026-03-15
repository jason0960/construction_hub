package com.constructionhub.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {
    private int totalJobs;
    private int activeJobs;
    private int completedJobs;
    private int totalWorkers;
    private BigDecimal totalRevenue;
    private BigDecimal totalCosts;
    private Map<String, Integer> jobsByStatus;
    private java.util.List<RecentJob> recentJobs;

    @Data
    @Builder
    public static class RecentJob {
        private Long id;
        private String title;
        private String status;
        private String clientName;
        private String updatedAt;
    }
}
