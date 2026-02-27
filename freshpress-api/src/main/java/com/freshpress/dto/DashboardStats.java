package com.freshpress.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class DashboardStats {
    private long totalOrders;
    private long totalCustomers;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long processingOrders;
    private long readyOrders;
    private long deliveredOrders;
    private Map<String, Long> statusBreakdown;
}
