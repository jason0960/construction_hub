package com.constructionhub.controller;

import com.constructionhub.dto.JobNoteRequest;
import com.constructionhub.dto.auth.LoginRequest;
import com.constructionhub.entity.NoteVisibility;
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
class JobNoteControllerTest {

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
    void listNotes_returnsSeededNotes() throws Exception {
        mockMvc.perform(get("/api/jobs/" + jobId + "/notes")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createNote_sharedVisibility_returnsNote() throws Exception {
        JobNoteRequest req = new JobNoteRequest();
        req.setContent("Integration test note - shared");
        req.setVisibility(NoteVisibility.SHARED);

        mockMvc.perform(post("/api/jobs/" + jobId + "/notes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Integration test note - shared"))
                .andExpect(jsonPath("$.visibility").value("SHARED"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void createNote_ownerOnly_returnsNote() throws Exception {
        JobNoteRequest req = new JobNoteRequest();
        req.setContent("Owner-only integration test note");
        req.setVisibility(NoteVisibility.OWNER_ONLY);

        mockMvc.perform(post("/api/jobs/" + jobId + "/notes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Owner-only integration test note"))
                .andExpect(jsonPath("$.visibility").value("OWNER_ONLY"));
    }

    @Test
    void createNote_missingContent_returns400() throws Exception {
        JobNoteRequest req = new JobNoteRequest();
        req.setContent(""); // blank

        mockMvc.perform(post("/api/jobs/" + jobId + "/notes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.content").exists());
    }

    @Test
    void createNote_defaultVisibility_isShared() throws Exception {
        JobNoteRequest req = new JobNoteRequest();
        req.setContent("Note without explicit visibility");

        mockMvc.perform(post("/api/jobs/" + jobId + "/notes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Note without explicit visibility"));
    }

    @Test
    void notes_nonexistentJob_returns404() throws Exception {
        mockMvc.perform(get("/api/jobs/999999/notes")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
