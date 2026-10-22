package com.company.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ProductDtos {

    public static class ProductRequest {
        @NotBlank(message = "SKU is required")
        private String sku;

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Category is required")
        private String category;

        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be positive")
        private BigDecimal unitPrice;

        @NotNull(message = "Unit cost is required")
        @Positive(message = "Unit cost must be positive")
        private BigDecimal unitCost;

        private boolean active = true;

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public static class ProductResponse {
        private Long id;
        private String sku;
        private String name;
        private String category;
        private BigDecimal unitPrice;
        private BigDecimal unitCost;
        private boolean active;

        public ProductResponse(Long id, String sku, String name, String category, BigDecimal unitPrice, BigDecimal unitCost, boolean active) {
            this.id = id;
            this.sku = sku;
            this.name = name;
            this.category = category;
            this.unitPrice = unitPrice;
            this.unitCost = unitCost;
            this.active = active;
        }

        public Long getId() { return id; }
        public String getSku() { return sku; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getUnitCost() { return unitCost; }
        public boolean isActive() { return active; }
    }
}
