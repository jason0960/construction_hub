package com.constructionhub.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MaterialResponse {
    private Long id;
    private Long jobId;
    private String name;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal total;
    private Long receiptDocumentId;
    private String receiptFileName;
    private LocalDateTime createdAt;
}
