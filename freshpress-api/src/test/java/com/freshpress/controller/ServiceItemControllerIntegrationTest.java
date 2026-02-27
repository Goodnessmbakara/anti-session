package com.freshpress.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshpress.dto.LoginRequest;
import com.freshpress.model.ServiceCategory;
import com.freshpress.model.ServiceItem;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("ServiceItemController Integration Tests")
class ServiceItemControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String adminToken;
    private static final String BASE_URL = "/api/v1/services";

    @BeforeEach
    void setUp() throws Exception {
        // Obtain JWT from the seeded admin account
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@freshpress.com");
        login.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(body).get("token").asText();
    }

    @Test
    @DisplayName("GET /services - should return 200 with non-empty list of seeded services")
    void getAllServices_returnsSeedDataList() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$[0].name").isNotEmpty())
                .andExpect(jsonPath("$[0].pricePerUnit").isNumber())
                .andExpect(jsonPath("$[0].category").isNotEmpty());
    }

    @Test
    @DisplayName("GET /services - should return 401/403 when accessing without token")
    void getAllServices_withoutToken_returns401or403() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @DisplayName("POST /services - should create a new service item and return 200")
    void createService_withValidPayload_returns200AndPersists() throws Exception {
        ServiceItem newService = ServiceItem.builder()
                .name("Express Dry Clean")
                .category(ServiceCategory.DRY_CLEAN)
                .pricePerUnit(new BigDecimal("4500.00"))
                .unitType("PIECE")
                .build();

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Express Dry Clean"))
                .andExpect(jsonPath("$.category").value("DRY_CLEAN"))
                .andExpect(jsonPath("$.pricePerUnit").value(4500.00))
                .andExpect(jsonPath("$.unitType").value("PIECE"));
    }

    @Test
    @DisplayName("POST /services - created service should appear in subsequent GET")
    void createService_thenGet_showsNewService() throws Exception {
        ServiceItem newService = ServiceItem.builder()
                .name("Unique Premium Press")
                .category(ServiceCategory.IRON)
                .pricePerUnit(new BigDecimal("750.00"))
                .unitType("PIECE")
                .build();

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newService)))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Unique Premium Press")));
    }
}
