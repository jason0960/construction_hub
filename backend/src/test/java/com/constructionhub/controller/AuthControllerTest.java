package com.constructionhub.controller;

import com.constructionhub.dto.auth.LoginRequest;
import com.constructionhub.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_withValidData_returnsTokens() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setOrganizationName("Test Construction LLC");
        req.setFirstName("Test");
        req.setLastName("User");
        req.setEmail("test-register@example.com");
        req.setPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("test-register@example.com"))
                .andExpect(jsonPath("$.user.role").value("OWNER"));
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setOrganizationName("Dup Org");
        req.setFirstName("Dup");
        req.setLastName("User");
        req.setEmail("duplicate@example.com");
        req.setPassword("Password123");

        // First registration succeeds
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Second registration fails
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingFields_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(""); // blank
        req.setPassword("short"); // too short

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void login_withValidCredentials_returnsTokens() throws Exception {
        // Register first (uses dev seeder: jason@summit.com / demo123)
        // The DevDataSeeder seeds this user on app startup.
        LoginRequest req = new LoginRequest();
        req.setEmail("jason@summit.com");
        req.setPassword("demo123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.firstName").value("Jason"));
    }

    @Test
    void login_wrongPassword_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("jason@summit.com");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_nonexistentUser_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@example.com");
        req.setPassword("Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshToken_withValidToken_returnsNewTokens() throws Exception {
        // Login to get tokens
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("jason@summit.com");
        loginReq.setPassword("demo123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = objectMapper.readTree(
                loginResult.getResponse().getContentAsString()).get("refreshToken").asText();

        // Use refresh token
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void protectedEndpoint_withoutToken_returns401or403() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void me_withValidToken_returnsUserInfo() throws Exception {
        // Login first
        LoginRequest req = new LoginRequest();
        req.setEmail("jason@summit.com");
        req.setPassword("demo123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(
                loginResult.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jason@summit.com"))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }
}
