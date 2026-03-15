package com.constructionhub.controller;

import com.constructionhub.dto.MaterialRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class MaterialControllerTest {

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
    void listMaterials_returnsSeededMaterials() throws Exception {
        mockMvc.perform(get("/api/jobs/" + jobId + "/materials")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createMaterial_withValidData_returnsMaterial() throws Exception {
        MaterialRequest req = new MaterialRequest();
        req.setName("Test Lumber 2x4");
        req.setQuantity(new BigDecimal("50"));
        req.setUnitCost(new BigDecimal("4.99"));

        mockMvc.perform(post("/api/jobs/" + jobId + "/materials")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Lumber 2x4"))
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.unitCost").value(4.99))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void createMaterial_missingName_returns400() throws Exception {
        MaterialRequest req = new MaterialRequest();
        req.setQuantity(new BigDecimal("10"));
        req.setUnitCost(new BigDecimal("5.00"));

        mockMvc.perform(post("/api/jobs/" + jobId + "/materials")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createMaterial_negativeUnitCost_returns400() throws Exception {
        MaterialRequest req = new MaterialRequest();
        req.setName("Bad Material");
        req.setQuantity(new BigDecimal("5"));
        req.setUnitCost(new BigDecimal("-1.00"));

        mockMvc.perform(post("/api/jobs/" + jobId + "/materials")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMaterial_changesFields() throws Exception {
        // Create first
        MaterialRequest createReq = new MaterialRequest();
        createReq.setName("Original Material");
        createReq.setQuantity(new BigDecimal("20"));
        createReq.setUnitCost(new BigDecimal("10.00"));

        MvcResult result = mockMvc.perform(post("/api/jobs/" + jobId + "/materials")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn();

        long materialId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        // Update
        MaterialRequest updateReq = new MaterialRequest();
        updateReq.setName("Updated Material");
        updateReq.setQuantity(new BigDecimal("30"));
        updateReq.setUnitCost(new BigDecimal("12.50"));

        mockMvc.perform(put("/api/jobs/" + jobId + "/materials/" + materialId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Material"))
                .andExpect(jsonPath("$.quantity").value(30));
    }

    @Test
    void deleteMaterial_returns204() throws Exception {
        MaterialRequest req = new MaterialRequest();
        req.setName("Delete Me Material");
        req.setQuantity(new BigDecimal("1"));
        req.setUnitCost(new BigDecimal("1.00"));

        MvcResult result = mockMvc.perform(post("/api/jobs/" + jobId + "/materials")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        long materialId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/jobs/" + jobId + "/materials/" + materialId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void materials_nonexistentJob_returns404() throws Exception {
        mockMvc.perform(get("/api/jobs/999999/materials")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
