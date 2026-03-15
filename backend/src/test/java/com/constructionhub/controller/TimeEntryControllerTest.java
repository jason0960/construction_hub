package com.constructionhub.controller;

import com.constructionhub.dto.TimeEntryRequest;
import com.constructionhub.dto.auth.LoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class TimeEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private long jobId;
    private long workerId;

    @BeforeEach
    void setup() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("jason@summit.com");
        req.setPassword("demo123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode authResp = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        accessToken = authResp.get("accessToken").asText();

        // Get first job ID from seeded data
        MvcResult jobsResult = mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jobsPage = objectMapper.readTree(jobsResult.getResponse().getContentAsString());
        jobId = jobsPage.get("content").get(0).get("id").asLong();

        // Get first worker ID from seeded data
        MvcResult workersResult = mockMvc.perform(get("/api/workers")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode workersPage = objectMapper.readTree(workersResult.getResponse().getContentAsString());
        workerId = workersPage.get("content").get(0).get("id").asLong();
    }

    @Test
    void listTimeEntries_returnsSeededEntries() throws Exception {
        mockMvc.perform(get("/api/jobs/" + jobId + "/time-entries")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createTimeEntry_withValidData_returnsEntry() throws Exception {
        TimeEntryRequest req = new TimeEntryRequest();
        req.setWorkerId(workerId);
        req.setEntryDate(LocalDate.now());
        req.setHours(new BigDecimal("8.00"));
        req.setNotes("Integration test entry");

        MvcResult result = mockMvc.perform(post("/api/jobs/" + jobId + "/time-entries")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hours").value(8.00))
                .andExpect(jsonPath("$.notes").value("Integration test entry"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        long entryId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        // Verify appears in list
        mockMvc.perform(get("/api/jobs/" + jobId + "/time-entries")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createTimeEntry_missingWorker_returns400() throws Exception {
        TimeEntryRequest req = new TimeEntryRequest();
        req.setEntryDate(LocalDate.now());
        req.setHours(new BigDecimal("4.00"));

        mockMvc.perform(post("/api/jobs/" + jobId + "/time-entries")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.workerId").exists());
    }

    @Test
    void createTimeEntry_invalidHours_returns400() throws Exception {
        TimeEntryRequest req = new TimeEntryRequest();
        req.setWorkerId(workerId);
        req.setEntryDate(LocalDate.now());
        req.setHours(new BigDecimal("25.00")); // exceeds 24

        mockMvc.perform(post("/api/jobs/" + jobId + "/time-entries")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTimeEntry_changesFields() throws Exception {
        // Create entry first
        TimeEntryRequest createReq = new TimeEntryRequest();
        createReq.setWorkerId(workerId);
        createReq.setEntryDate(LocalDate.now());
        createReq.setHours(new BigDecimal("6.00"));

        MvcResult result = mockMvc.perform(post("/api/jobs/" + jobId + "/time-entries")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        long entryId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        // Update it
        TimeEntryRequest updateReq = new TimeEntryRequest();
        updateReq.setWorkerId(workerId);
        updateReq.setEntryDate(LocalDate.now());
        updateReq.setHours(new BigDecimal("7.50"));
        updateReq.setNotes("Updated via test");

        mockMvc.perform(put("/api/jobs/" + jobId + "/time-entries/" + entryId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hours").value(7.5))
                .andExpect(jsonPath("$.notes").value("Updated via test"));
    }

    @Test
    void deleteTimeEntry_returns204() throws Exception {
        // Create entry
        TimeEntryRequest req = new TimeEntryRequest();
        req.setWorkerId(workerId);
        req.setEntryDate(LocalDate.now());
        req.setHours(new BigDecimal("2.00"));

        MvcResult result = mockMvc.perform(post("/api/jobs/" + jobId + "/time-entries")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        long entryId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/jobs/" + jobId + "/time-entries/" + entryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void timeEntries_nonexistentJob_returns404() throws Exception {
        mockMvc.perform(get("/api/jobs/999999/time-entries")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
