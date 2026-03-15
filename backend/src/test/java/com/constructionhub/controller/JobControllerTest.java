package com.constructionhub.controller;

import com.constructionhub.dto.JobRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class JobControllerTest {

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
    void listJobs_returnsSeededJobs() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void createJob_withValidData_returnsJob() throws Exception {
        JobRequest req = new JobRequest();
        req.setTitle("Test Job - Integration");
        req.setDescription("Created by integration test");

        MvcResult result = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Job - Integration"))
                .andExpect(jsonPath("$.status").value("LEAD"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        // Verify we can fetch it
        long jobId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Job - Integration"));
    }

    @Test
    void createJob_missingTitle_returns400() throws Exception {
        JobRequest req = new JobRequest();
        req.setDescription("No title provided");

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void updateJob_changesFields() throws Exception {
        // Create a job first
        JobRequest createReq = new JobRequest();
        createReq.setTitle("Update Me");

        MvcResult createResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        long jobId = objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asLong();

        // Update it
        JobRequest updateReq = new JobRequest();
        updateReq.setTitle("Updated Title");
        updateReq.setDescription("Now with description");

        mockMvc.perform(put("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Now with description"));
    }

    @Test
    void deleteJob_returns204() throws Exception {
        // Create a job first
        JobRequest req = new JobRequest();
        req.setTitle("Delete Me");

        MvcResult createResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        long jobId = objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void getJob_nonexistent_returns404() throws Exception {
        mockMvc.perform(get("/api/jobs/999999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateJobStatus_changesStatus() throws Exception {
        // Create a job
        JobRequest req = new JobRequest();
        req.setTitle("Status Change Test");

        MvcResult result = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        long jobId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/jobs/" + jobId + "/status?status=IN_PROGRESS")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
