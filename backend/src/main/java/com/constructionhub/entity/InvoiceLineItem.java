package com.constructionhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_line_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false)
    private String description;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @PrePersist
    protected void onCreate() {
        calculateTotal();
        if (sortOrder == null) sortOrder = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotal();
    }

    private void calculateTotal() {
        if (quantity != null && unitPrice != null) {
            total = quantity.multiply(unitPrice);
        }
    }
}
