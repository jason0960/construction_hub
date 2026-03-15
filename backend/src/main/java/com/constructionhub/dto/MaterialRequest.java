package com.constructionhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MaterialRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    @NotNull(message = "Unit cost is required")
    private BigDecimal unitCost;

    private Long receiptDocumentId;
}
