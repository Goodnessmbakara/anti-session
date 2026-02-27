package com.freshpress.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshpress.dto.CreateCustomerRequest;
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
@DisplayName("CustomerController Integration Tests")
class CustomerControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String adminToken;
    private static final String BASE_URL = "/api/v1/customers";

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

    // ─── GET ALL CUSTOMERS ────────────────────────────────────────

    @Test
    @DisplayName("GET /customers - should return 200 with paginated customer list")
    void getAllCustomers_returnsPaginatedResponse() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.content[0].name").isNotEmpty())
                .andExpect(jsonPath("$.content[0].phone").isNotEmpty());
    }

    @Test
    @DisplayName("GET /customers?search=Amara - should return only customers matching the name")
    void getAllCustomers_withSearch_filtersResults() throws Exception {
        mockMvc.perform(get(BASE_URL + "?search=Amara")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].name", containsStringIgnoringCase("Amara")));
    }

    @Test
    @DisplayName("GET /customers?search=NonExistentXYZ - should return empty list")
    void getAllCustomers_withUnknownSearch_returnsEmpty() throws Exception {
        mockMvc.perform(get(BASE_URL + "?search=NonExistentXYZ99")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── CREATE CUSTOMER ──────────────────────────────────────────

    @Test
    @DisplayName("POST /customers - should return 200 with created customer and assigned id")
    void createCustomer_withValidPayload_returns200WithId() throws Exception {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setName("Joe Tester");
        req.setPhone("+2348011112222");
        req.setEmail("joe@test.com");
        req.setAddress("10 Test Street, Uyo");

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Joe Tester"))
                .andExpect(jsonPath("$.phone").value("+2348011112222"))
                .andExpect(jsonPath("$.email").value("joe@test.com"));
    }

    @Test
    @DisplayName("POST /customers - should return 400 when required field 'phone' is missing")
    void createCustomer_missingPhone_returns400WithFieldError() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Missing Phone Guy" }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.phone").isNotEmpty());
    }

    @Test
    @DisplayName("POST /customers - should return 400 when required field 'name' is missing")
    void createCustomer_missingName_returns400WithFieldError() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "phone": "+2348011112222" }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").isNotEmpty());
    }

    // ─── GET CUSTOMER BY ID ───────────────────────────────────────

    @Test
    @DisplayName("GET /customers/1 - should return 200 with customer data for seeded customer")
    void getCustomerById_withExistingId_returns200() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").isNotEmpty())
                .andExpect(jsonPath("$.phone").isNotEmpty());
    }

    @Test
    @DisplayName("GET /customers/9999 - should return 404 for non-existent customer")
    void getCustomerById_withNonExistentId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/9999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ─── UPDATE CUSTOMER ──────────────────────────────────────────

    @Test
    @DisplayName("PUT /customers/1 - should update and return the modified customer")
    void updateCustomer_withValidPayload_returns200WithUpdatedData() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Amara Okafor Updated",
                      "phone": "+2348012345678",
                      "email": "amara.updated@email.com",
                      "address": "15 Aba Road, Uyo"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Amara Okafor Updated"))
                .andExpect(jsonPath("$.email").value("amara.updated@email.com"));
    }

    @Test
    @DisplayName("PUT /customers/9999 - should return 404 for non-existent customer")
    void updateCustomer_withNonExistentId_returns404() throws Exception {
        mockMvc.perform(put(BASE_URL + "/9999")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Ghost",
                      "phone": "+23480000000"
                    }
                    """))
                .andExpect(status().isNotFound());
    }
}
