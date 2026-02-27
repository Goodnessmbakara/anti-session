package com.freshpress.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private LocalDateTime pickupDate;
    private LocalDateTime deliveryDate;
    private String notes;

    @NotNull(message = "At least one item is required")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Service item ID is required")
        private Long serviceItemId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        private String notes;
    }
}
