package com.freshpress.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshpress.dto.LoginRequest;
import com.freshpress.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/auth";

    // ─── REGISTER ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/register - should return 200 with token and user details on success")
    void register_withValidPayload_returns200WithToken() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("New User");
        req.setEmail("newuser@freshpress.com");
        req.setPassword("password123");

        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("newuser@freshpress.com"))
                .andExpect(jsonPath("$.fullName").value("New User"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 when email is already registered")
    void register_withDuplicateEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Admin");
        req.setEmail("admin@freshpress.com"); // seeded by DataSeeder
        req.setPassword("password123");

        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("admin@freshpress.com")));
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 when fullName is blank")
    void register_withMissingFullName_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName": "",
                      "email": "valid@freshpress.com",
                      "password": "password123"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.fullName").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 when email format is invalid")
    void register_withInvalidEmail_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName": "Test User",
                      "email": "not-an-email",
                      "password": "password123"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/register - should return 400 when password is too short")
    void register_withShortPassword_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName": "Test User",
                      "email": "test@freshpress.com",
                      "password": "abc"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.password").isNotEmpty());
    }

    // ─── LOGIN ────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login - should return 200 with token for valid seeded admin credentials")
    void login_withValidAdminCredentials_returns200WithToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@freshpress.com");
        req.setPassword("password123");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@freshpress.com"))
                .andExpect(jsonPath("$.fullName").value("Goodness Mbakara"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /auth/login - should return 401/403 when password is wrong")
    void login_withWrongPassword_returns403() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@freshpress.com");
        req.setPassword("wrongpassword");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 when email field is missing")
    void login_withMissingEmail_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "password": "password123" }
                    """))
                .andExpect(status().isBadRequest());
    }

    // ─── SECURITY ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /orders - should return 401 or 403 when no Authorization header is sent")
    void protectedEndpoint_withNoToken_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }
}
