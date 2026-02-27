package com.freshpress.service;

import com.freshpress.dto.CreateOrderRequest;
import com.freshpress.dto.DashboardStats;
import com.freshpress.model.*;
import com.freshpress.repository.CustomerRepository;
import com.freshpress.repository.OrderRepository;
import com.freshpress.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ServiceItemRepository serviceItemRepository;

    public Page<com.freshpress.model.Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Page<com.freshpress.model.Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    public com.freshpress.model.Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Transactional
    public com.freshpress.model.Order createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        com.freshpress.model.Order order = com.freshpress.model.Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .pickupDate(request.getPickupDate())
                .deliveryDate(request.getDeliveryDate())
                .notes(request.getNotes())
                .build();

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            ServiceItem serviceItem = serviceItemRepository.findById(itemReq.getServiceItemId())
                    .orElseThrow(() -> new RuntimeException("Service item not found: " + itemReq.getServiceItemId()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .serviceItem(serviceItem)
                    .quantity(itemReq.getQuantity())
                    .notes(itemReq.getNotes())
                    .subtotal(serviceItem.getPricePerUnit().multiply(java.math.BigDecimal.valueOf(itemReq.getQuantity())))
                    .build();

            order.getItems().add(orderItem);
        }

        order.recalculateTotal();
        return orderRepository.save(order);
    }

    @Transactional
    public com.freshpress.model.Order updateOrderStatus(Long id, OrderStatus newStatus) {
        com.freshpress.model.Order order = getOrderById(id);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public DashboardStats getDashboardStats() {
        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        Arrays.stream(OrderStatus.values()).forEach(status ->
            statusBreakdown.put(status.name(), orderRepository.countByStatus(status))
        );

        return DashboardStats.builder()
                .totalOrders(orderRepository.count())
                .totalCustomers(customerRepository.countAll())
                .totalRevenue(orderRepository.calculateTotalRevenue())
                .pendingOrders(orderRepository.countByStatus(OrderStatus.PENDING))
                .processingOrders(orderRepository.countByStatus(OrderStatus.PROCESSING))
                .readyOrders(orderRepository.countByStatus(OrderStatus.READY))
                .deliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .statusBreakdown(statusBreakdown)
                .build();
    }
}
