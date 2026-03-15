package com.constructionhub.controller;

import com.constructionhub.dto.PermitRequest;
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
class PermitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private long jobId;

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
    }

    @Test
    void listPermits_returnsSeededPermits() throws Exception {
        mockMvc.perform(get("/api/jobs/" + jobId + "/permits")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createPermit_withValidData_returnsPermit() throws Exception {
        PermitRequest req = new PermitRequest();
        req.setPermitType("Electrical");
        req.setPermitNumber("EL-2025-001");
        req.setIssuingAuthority("City of Austin");
        req.setFee(new BigDecimal("250.00"));
        req.setApplicationDate(LocalDate.now());

        mockMvc.perform(post("/api/jobs/" + jobId + "/permits")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permitType").value("Electrical"))
                .andExpect(jsonPath("$.permitNumber").value("EL-2025-001"))
                .andExpect(jsonPath("$.issuingAuthority").value("City of Austin"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void createPermit_missingType_returns400() throws Exception {
        PermitRequest req = new PermitRequest();
        req.setPermitNumber("NO-TYPE");

        mockMvc.perform(post("/api/jobs/" + jobId + "/permits")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.permitType").exists());
    }

    @Test
    void createPermit_negativeFee_returns400() throws Exception {
        PermitRequest req = new PermitRequest();
        req.setPermitType("Building");
        req.setFee(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/jobs/" + jobId + "/permits")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePermit_changesFields() throws Exception {
        // Create first
        PermitRequest createReq = new PermitRequest();
        createReq.setPermitType("Plumbing");
        createReq.setPermitNumber("PL-001");

        MvcResult result = mockMvc.perform(post("/api/jobs/" + jobId + "/permits")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        long permitId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        // Update
        PermitRequest updateReq = new PermitRequest();
        updateReq.setPermitType("Plumbing - Updated");
        updateReq.setPermitNumber("PL-001-R");
        updateReq.setFee(new BigDecimal("150.00"));

        mockMvc.perform(put("/api/jobs/" + jobId + "/permits/" + permitId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permitType").value("Plumbing - Updated"))
                .andExpect(jsonPath("$.permitNumber").value("PL-001-R"));
    }

    @Test
    void deletePermit_returns204() throws Exception {
        PermitRequest req = new PermitRequest();
        req.setPermitType("Temp Permit Delete");

        MvcResult result = mockMvc.perform(post("/api/jobs/" + jobId + "/permits")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        long permitId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/jobs/" + jobId + "/permits/" + permitId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void permits_nonexistentJob_returns404() throws Exception {
        mockMvc.perform(get("/api/jobs/999999/permits")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
