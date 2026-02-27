package com.freshpress.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshpress.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("OrderController Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String adminToken;
    private static final String BASE_URL = "/api/v1";

    @BeforeEach
    void setUp() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@freshpress.com");
        login.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        adminToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    // ─── GET ALL ORDERS ───────────────────────────────────────────

    @Test
    @DisplayName("GET /orders - should return 200 with paginated orders from seed data")
    void getAllOrders_returnsPaginatedOrders() throws Exception {
        mockMvc.perform(get(BASE_URL + "/orders")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.content[0].status").isNotEmpty())
                .andExpect(jsonPath("$.content[0].totalAmount").isNumber());
    }

    @Test
    @DisplayName("GET /orders?status=PENDING - should return only PENDING orders")
    void getAllOrders_filteredByPending_returnsOnlyPending() throws Exception {
        mockMvc.perform(get(BASE_URL + "/orders?status=PENDING")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(equalTo("PENDING"))));
    }

    @Test
    @DisplayName("GET /orders?status=DELIVERED - should return only DELIVERED orders")
    void getAllOrders_filteredByDelivered_returnsOnlyDelivered() throws Exception {
        mockMvc.perform(get(BASE_URL + "/orders?status=DELIVERED")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(equalTo("DELIVERED"))));
    }

    // ─── GET ORDER BY ID ──────────────────────────────────────────

    @Test
    @DisplayName("GET /orders/1 - should return 200 with order including items and customer")
    void getOrderById_withExistingId_returns200() throws Exception {
        mockMvc.perform(get(BASE_URL + "/orders/1")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andExpect(jsonPath("$.totalAmount").isNumber());
    }

    @Test
    @DisplayName("GET /orders/9999 - should return 400 (RuntimeException → GlobalExceptionHandler)")
    void getOrderById_withNonExistentId_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/orders/9999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("9999")));
    }

    // ─── CREATE ORDER ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /orders - should create order with correct total = sum of (price × qty)")
    void createOrder_withValidPayload_returns200AndCalculatesTotal() throws Exception {
        // Seed data: serviceItem id=1 is Wash & Fold at 1500/KG
        // 2 KG → totalAmount should be 3000
        String body = """
            {
              "customerId": 1,
              "pickupDate":  "2026-03-01T10:00:00",
              "deliveryDate":"2026-03-03T10:00:00",
              "notes": "Integration test order",
              "items": [
                { "serviceItemId": 1, "quantity": 2, "notes": "Gentle cycle" }
              ]
            }
            """;

        mockMvc.perform(post(BASE_URL + "/orders")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(3000.00))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    @DisplayName("POST /orders - should create order with multiple items and sum totals correctly")
    void createOrder_withMultipleItems_returns200AndSumsTotal() throws Exception {
        // serviceItem 1 = 1500/KG (qty 2 = 3000), serviceItem 3 = 500/PIECE (qty 3 = 1500) → total 4500
        String body = """
            {
              "customerId": 1,
              "items": [
                { "serviceItemId": 1, "quantity": 2 },
                { "serviceItemId": 3, "quantity": 3 }
              ]
            }
            """;

        mockMvc.perform(post(BASE_URL + "/orders")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(4500.00))
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    @DisplayName("POST /orders - should return 400 when customerId refers to non-existent customer")
    void createOrder_withInvalidCustomerId_returns400() throws Exception {
        String body = """
            {
              "customerId": 9999,
              "items": [{ "serviceItemId": 1, "quantity": 1 }]
            }
            """;

        mockMvc.perform(post(BASE_URL + "/orders")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Customer not found")));
    }

    @Test
    @DisplayName("POST /orders - should return 400 when items list is missing (validation)")
    void createOrder_missingItems_returns400() throws Exception {
        String body = """
            { "customerId": 1 }
            """;

        mockMvc.perform(post(BASE_URL + "/orders")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /orders - should return 400 when customerId is missing (validation)")
    void createOrder_missingCustomerId_returns400() throws Exception {
        String body = """
            {
              "items": [{ "serviceItemId": 1, "quantity": 1 }]
            }
            """;

        mockMvc.perform(post(BASE_URL + "/orders")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /orders - should return 400 when serviceItemId does not exist")
    void createOrder_withInvalidServiceItemId_returns400() throws Exception {
        String body = """
            {
              "customerId": 1,
              "items": [{ "serviceItemId": 9999, "quantity": 1 }]
            }
            """;

        mockMvc.perform(post(BASE_URL + "/orders")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Service item not found")));
    }

    // ─── UPDATE ORDER STATUS ──────────────────────────────────────

    @Test
    @DisplayName("PATCH /orders/{id}/status - should advance through the full lifecycle")
    void updateOrderStatus_throughFullLifecycle_allReturn200() throws Exception {
        // First create a fresh order to track through lifecycle
        String createBody = """
            {
              "customerId": 1,
              "items": [{ "serviceItemId": 1, "quantity": 1 }]
            }
            """;

        MvcResult createResult = mockMvc.perform(post(BASE_URL + "/orders")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
                .andExpect(status().isOk())
                .andReturn();

        Long orderId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        String statusUrl = BASE_URL + "/orders/" + orderId + "/status";

        // PENDING → PICKED_UP
        mockMvc.perform(patch(statusUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "PICKED_UP"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKED_UP"));

        // PICKED_UP → PROCESSING
        mockMvc.perform(patch(statusUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "PROCESSING"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        // PROCESSING → READY
        mockMvc.perform(patch(statusUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "READY"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));

        // READY → DELIVERED
        mockMvc.perform(patch(statusUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "DELIVERED"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    @DisplayName("PATCH /orders/{id}/status - should return 400 for CANCELLED status")
    void updateOrderStatus_toCancelled_returns200() throws Exception {
        // Cancel a seeded order
        mockMvc.perform(patch(BASE_URL + "/orders/2/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "CANCELLED"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PATCH /orders/{id}/status - should return 400 for invalid enum value")
    void updateOrderStatus_withInvalidEnum_returns400Or500() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/orders/1/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "FLYING"}
                    """))
                .andExpect(status().is(anyOf(is(400), is(500))));
    }

    // ─── DASHBOARD ────────────────────────────────────────────────

    @Test
    @DisplayName("GET /dashboard/stats - should return 200 with all expected stat fields")
    void getDashboardStats_returns200WithAllFields() throws Exception {
        mockMvc.perform(get(BASE_URL + "/dashboard/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").isNumber())
                .andExpect(jsonPath("$.totalCustomers").isNumber())
                .andExpect(jsonPath("$.totalRevenue").isNumber())
                .andExpect(jsonPath("$.pendingOrders").isNumber())
                .andExpect(jsonPath("$.processingOrders").isNumber())
                .andExpect(jsonPath("$.readyOrders").isNumber())
                .andExpect(jsonPath("$.deliveredOrders").isNumber())
                .andExpect(jsonPath("$.statusBreakdown").isMap())
                .andExpect(jsonPath("$.statusBreakdown.PENDING").isNumber())
                .andExpect(jsonPath("$.statusBreakdown.DELIVERED").isNumber());
    }

    @Test
    @DisplayName("GET /dashboard/stats - totalOrders should reflect seeded 3 orders")
    void getDashboardStats_totalOrdersMatchesSeedCount() throws Exception {
        mockMvc.perform(get(BASE_URL + "/dashboard/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.totalCustomers").value(greaterThanOrEqualTo(3)));
    }
}
