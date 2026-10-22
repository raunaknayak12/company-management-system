package com.company.cms.dto;

import com.company.cms.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SalesOrderDtos {

    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class CreateOrderRequest {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        private Long salesRepId; // If null, defaults to logged-in user's employee ID

        @DecimalMin(value = "0.0", message = "Discount cannot be negative")
        @DecimalMax(value = "30.0", message = "Discount cannot exceed 30%")
        private BigDecimal discountPercent = BigDecimal.ZERO;

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        private List<OrderItemRequest> items;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public Long getSalesRepId() { return salesRepId; }
        public void setSalesRepId(Long salesRepId) { this.salesRepId = salesRepId; }
        public BigDecimal getDiscountPercent() { return discountPercent; }
        public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
    }

    public static class ChangeStatusRequest {
        @NotNull(message = "New status is required")
        private OrderStatus newStatus;

        public OrderStatus getNewStatus() { return newStatus; }
        public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }
    }

    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productSku;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal unitCost;
        private BigDecimal lineTotal;

        public OrderItemResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductSku() { return productSku; }
        public void setProductSku(String productSku) { this.productSku = productSku; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
        public BigDecimal getLineTotal() { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    }

    public static class StatusHistoryResponse {
        private Long id;
        private OrderStatus previousStatus;
        private OrderStatus newStatus;
        private String changedBy;
        private LocalDateTime changedAt;

        public StatusHistoryResponse(Long id, OrderStatus previousStatus, OrderStatus newStatus, String changedBy, LocalDateTime changedAt) {
            this.id = id;
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
            this.changedBy = changedBy;
            this.changedAt = changedAt;
        }

        public Long getId() { return id; }
        public OrderStatus getPreviousStatus() { return previousStatus; }
        public OrderStatus getNewStatus() { return newStatus; }
        public String getChangedBy() { return changedBy; }
        public LocalDateTime getChangedAt() { return changedAt; }
    }

    public static class SalesOrderResponse {
        private Long id;
        private String orderNumber;
        private LocalDateTime orderDate;
        private Long customerId;
        private String customerName;
        private Long salesRepId;
        private String salesRepName;
        private String region;
        private OrderStatus status;
        private BigDecimal subtotalAmount;
        private BigDecimal discountPercent;
        private BigDecimal totalAmount;
        private BigDecimal totalCost;
        private List<OrderItemResponse> items;
        private List<StatusHistoryResponse> statusHistories;

        public SalesOrderResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public Long getSalesRepId() { return salesRepId; }
        public void setSalesRepId(Long salesRepId) { this.salesRepId = salesRepId; }
        public String getSalesRepName() { return salesRepName; }
        public void setSalesRepName(String salesRepName) { this.salesRepName = salesRepName; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public BigDecimal getSubtotalAmount() { return subtotalAmount; }
        public void setSubtotalAmount(BigDecimal subtotalAmount) { this.subtotalAmount = subtotalAmount; }
        public BigDecimal getDiscountPercent() { return discountPercent; }
        public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
        public List<OrderItemResponse> getItems() { return items; }
        public void setItems(List<OrderItemResponse> items) { this.items = items; }
        public List<StatusHistoryResponse> getStatusHistories() { return statusHistories; }
        public void setStatusHistories(List<StatusHistoryResponse> statusHistories) { this.statusHistories = statusHistories; }
    }
}
