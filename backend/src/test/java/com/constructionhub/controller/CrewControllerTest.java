package com.constructionhub.controller;

import com.constructionhub.dto.CrewAssignmentRequest;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CrewControllerTest {

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

        MvcResult jobsResult = mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode jobsPage = objectMapper.readTree(jobsResult.getResponse().getContentAsString());
        jobId = jobsPage.get("content").get(0).get("id").asLong();

        MvcResult workersResult = mockMvc.perform(get("/api/workers")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode workersPage = objectMapper.readTree(workersResult.getResponse().getContentAsString());
        workerId = workersPage.get("content").get(0).get("id").asLong();
    }

    @Test
    void listCrew_returnsSeededAssignments() throws Exception {
        mockMvc.perform(get("/api/jobs/" + jobId + "/crew")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void assignWorker_withValidData_returnsAssignment() throws Exception {
        // Create a new job to avoid duplicate assignment conflicts with seeded data
        String jobJson = "{\"title\":\"Crew Test Job\"}";
        MvcResult jobResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson))
                .andExpect(status().isOk())
                .andReturn();
        long newJobId = objectMapper.readTree(
                jobResult.getResponse().getContentAsString()).get("id").asLong();

        CrewAssignmentRequest req = new CrewAssignmentRequest();
        req.setWorkerId(workerId);
        req.setRoleOnJob("Lead Framer");
        req.setStartDate(LocalDate.now());

        mockMvc.perform(post("/api/jobs/" + newJobId + "/crew")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleOnJob").value("Lead Framer"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void assignWorker_missingWorkerId_returns400() throws Exception {
        CrewAssignmentRequest req = new CrewAssignmentRequest();
        req.setRoleOnJob("Test Role");

        mockMvc.perform(post("/api/jobs/" + jobId + "/crew")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.workerId").exists());
    }

    @Test
    void updateAssignment_changesRole() throws Exception {
        // Create a fresh job + assignment
        String jobJson = "{\"title\":\"Update Crew Test\"}";
        MvcResult jobResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson))
                .andExpect(status().isOk())
                .andReturn();
        long newJobId = objectMapper.readTree(
                jobResult.getResponse().getContentAsString()).get("id").asLong();

        CrewAssignmentRequest createReq = new CrewAssignmentRequest();
        createReq.setWorkerId(workerId);
        createReq.setRoleOnJob("Original Role");

        MvcResult assignResult = mockMvc.perform(post("/api/jobs/" + newJobId + "/crew")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        long assignmentId = objectMapper.readTree(
                assignResult.getResponse().getContentAsString()).get("id").asLong();

        // Update role
        CrewAssignmentRequest updateReq = new CrewAssignmentRequest();
        updateReq.setWorkerId(workerId);
        updateReq.setRoleOnJob("Foreman");

        mockMvc.perform(put("/api/jobs/" + newJobId + "/crew/" + assignmentId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleOnJob").value("Foreman"));
    }

    @Test
    void removeWorker_returns204() throws Exception {
        // Create a fresh job + assignment
        String jobJson = "{\"title\":\"Remove Crew Test\"}";
        MvcResult jobResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson))
                .andExpect(status().isOk())
                .andReturn();
        long newJobId = objectMapper.readTree(
                jobResult.getResponse().getContentAsString()).get("id").asLong();

        CrewAssignmentRequest req = new CrewAssignmentRequest();
        req.setWorkerId(workerId);

        MvcResult assignResult = mockMvc.perform(post("/api/jobs/" + newJobId + "/crew")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        long assignmentId = objectMapper.readTree(
                assignResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/jobs/" + newJobId + "/crew/" + assignmentId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void crew_nonexistentJob_returns404() throws Exception {
        mockMvc.perform(get("/api/jobs/999999/crew")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
