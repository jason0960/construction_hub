package com.constructionhub.controller;

import com.constructionhub.dto.WorkerRequest;
import com.constructionhub.dto.auth.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class WorkerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;

    @BeforeEach
    void login() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("jason@summit.com");
        req.setPassword("demo123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        accessToken = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void listWorkers_returnsSeededWorkers() throws Exception {
        mockMvc.perform(get("/api/workers")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void createWorker_withValidData_returnsWorker() throws Exception {
        WorkerRequest req = new WorkerRequest();
        req.setFirstName("Integration");
        req.setLastName("Tester");
        req.setTrade("QA Engineer");
        req.setHourlyRate(new BigDecimal("55.00"));

        mockMvc.perform(post("/api/workers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Integration"))
                .andExpect(jsonPath("$.lastName").value("Tester"))
                .andExpect(jsonPath("$.trade").value("QA Engineer"));
    }

    @Test
    void createWorker_missingFirstName_returns400() throws Exception {
        WorkerRequest req = new WorkerRequest();
        req.setLastName("NoFirst");

        mockMvc.perform(post("/api/workers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists());
    }

    @Test
    void createWorker_invalidEmail_returns400() throws Exception {
        WorkerRequest req = new WorkerRequest();
        req.setFirstName("Bad");
        req.setLastName("Email");
        req.setEmail("not-an-email");

        mockMvc.perform(post("/api/workers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void updateWorker_changesFields() throws Exception {
        // Create worker first
        WorkerRequest createReq = new WorkerRequest();
        createReq.setFirstName("Update");
        createReq.setLastName("Worker");

        MvcResult result = mockMvc.perform(post("/api/workers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        long workerId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        // Update
        WorkerRequest updateReq = new WorkerRequest();
        updateReq.setFirstName("Updated");
        updateReq.setLastName("Name");
        updateReq.setTrade("Plumber");

        mockMvc.perform(put("/api/workers/" + workerId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.trade").value("Plumber"));
    }
}
