package com.freshpress.service;

import com.freshpress.dto.CreateOrderRequest;
import com.freshpress.dto.DashboardStats;
import com.freshpress.model.*;
import com.freshpress.repository.CustomerRepository;
import com.freshpress.repository.OrderRepository;
import com.freshpress.repository.ServiceItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ServiceItemRepository serviceItemRepository;

    @InjectMocks private OrderService orderService;

    private Customer customer;
    private ServiceItem washFold;
    private Order existingOrder;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .name("Amara Okafor")
                .phone("+2348012345678")
                .email("amara@email.com")
                .address("12 Aba Road, Uyo")
                .build();

        washFold = ServiceItem.builder()
                .name("Wash & Fold")
                .category(ServiceCategory.WASH)
                .pricePerUnit(new BigDecimal("1500.00"))
                .unitType("KG")
                .build();

        existingOrder = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .pickupDate(LocalDateTime.now())
                .deliveryDate(LocalDateTime.now().plusDays(2))
                .build();
    }

    // ─── CREATE ORDER ─────────────────────────────────────────────

    @Test
    @DisplayName("createOrder() - should create order with correct totalAmount")
    void createOrder_withValidInput_createsOrderAndCalculatesTotal() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(washFold));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setPickupDate(LocalDateTime.now());
        request.setDeliveryDate(LocalDateTime.now().plusDays(2));

        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setServiceItemId(1L);
        item.setQuantity(3); // 3 KG × 1500 = 4500
        request.setItems(List.of(item));

        Order result = orderService.createOrder(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("4500.00"));

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder() - should throw RuntimeException when customer not found")
    void createOrder_withUnknownCustomer_throwsRuntimeException() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(999L);

        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setServiceItemId(1L);
        item.setQuantity(1);
        request.setItems(List.of(item));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Customer not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder() - should throw RuntimeException when serviceItem not found")
    void createOrder_withUnknownServiceItem_throwsRuntimeException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(serviceItemRepository.findById(999L)).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);

        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setServiceItemId(999L);
        item.setQuantity(1);
        request.setItems(List.of(item));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Service item not found");
    }

    @Test
    @DisplayName("createOrder() - should set status to PENDING by default")
    void createOrder_shouldDefaultStatusToPending() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(washFold));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setServiceItemId(1L);
        item.setQuantity(1);
        request.setItems(List.of(item));

        Order result = orderService.createOrder(request);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    // ─── GET ORDER BY ID ──────────────────────────────────────────

    @Test
    @DisplayName("getOrderById() - should return order when it exists")
    void getOrderById_withValidId_returnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));

        Order result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("getOrderById() - should throw RuntimeException for unknown id")
    void getOrderById_withUnknownId_throwsRuntimeException() {
        when(orderRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(9999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    // ─── UPDATE ORDER STATUS ──────────────────────────────────────

    @Test
    @DisplayName("updateOrderStatus() - should transition status and save")
    void updateOrderStatus_withValidId_updatesAndSaves() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateOrderStatus(1L, OrderStatus.PROCESSING);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        verify(orderRepository).save(existingOrder);
    }

    @Test
    @DisplayName("updateOrderStatus() - should throw RuntimeException when order not found")
    void updateOrderStatus_withUnknownId_throwsRuntimeException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, OrderStatus.PROCESSING))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    // ─── GET ALL ORDERS ───────────────────────────────────────────

    @Test
    @DisplayName("getAllOrders() - should return paginated orders")
    void getAllOrders_returnsPageOfOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(existingOrder));
        when(orderRepository.findAll(pageable)).thenReturn(page);

        Page<Order> result = orderService.getAllOrders(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getOrdersByStatus() - should return filtered paginated orders")
    void getOrdersByStatus_returnsFiltredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(existingOrder));
        when(orderRepository.findByStatus(OrderStatus.PENDING, pageable)).thenReturn(page);

        Page<Order> result = orderService.getOrdersByStatus(OrderStatus.PENDING, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findByStatus(OrderStatus.PENDING, pageable);
    }

    // ─── DASHBOARD STATS ──────────────────────────────────────────

    @Test
    @DisplayName("getDashboardStats() - should aggregate all counts and revenue")
    void getDashboardStats_returnsCorrectAggregation() {
        when(orderRepository.count()).thenReturn(10L);
        when(customerRepository.countAll()).thenReturn(4L);
        when(orderRepository.calculateTotalRevenue()).thenReturn(new BigDecimal("75000.00"));
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(2L);
        when(orderRepository.countByStatus(OrderStatus.PICKED_UP)).thenReturn(1L);
        when(orderRepository.countByStatus(OrderStatus.PROCESSING)).thenReturn(3L);
        when(orderRepository.countByStatus(OrderStatus.READY)).thenReturn(1L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(3L);
        when(orderRepository.countByStatus(OrderStatus.CANCELLED)).thenReturn(0L);

        DashboardStats stats = orderService.getDashboardStats();

        assertThat(stats.getTotalOrders()).isEqualTo(10L);
        assertThat(stats.getTotalCustomers()).isEqualTo(4L);
        assertThat(stats.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("75000.00"));
        assertThat(stats.getPendingOrders()).isEqualTo(2L);
        assertThat(stats.getProcessingOrders()).isEqualTo(3L);
        assertThat(stats.getReadyOrders()).isEqualTo(1L);
        assertThat(stats.getDeliveredOrders()).isEqualTo(3L);
        assertThat(stats.getStatusBreakdown()).containsKey("PENDING");
        assertThat(stats.getStatusBreakdown()).containsKey("DELIVERED");
    }
}
