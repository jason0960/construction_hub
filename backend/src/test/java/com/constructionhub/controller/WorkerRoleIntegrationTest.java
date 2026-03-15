package com.constructionhub.controller;

import com.constructionhub.dto.CrewAssignmentRequest;
import com.constructionhub.dto.JobNoteRequest;
import com.constructionhub.dto.TimeEntryRequest;
import com.constructionhub.dto.auth.InviteWorkerRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for WORKER role authorization.
 * Verifies workers can only access their assigned jobs and cannot perform admin-only operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class WorkerRoleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String ownerToken;
    private String workerToken;
    private long assignedJobId;
    private long unassignedJobId;
    private long workerProfileId;

    @BeforeEach
    void setup() throws Exception {
        // Login as owner
        LoginRequest ownerLogin = new LoginRequest();
        ownerLogin.setEmail("jason@summit.com");
        ownerLogin.setPassword("demo123");

        MvcResult ownerResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerLogin)))
                .andExpect(status().isOk())
                .andReturn();
        ownerToken = objectMapper.readTree(
                ownerResult.getResponse().getContentAsString()).get("accessToken").asText();

        // Invite a worker user
        String workerEmail = "worker-test-" + System.nanoTime() + "@summit.com";
        InviteWorkerRequest invite = new InviteWorkerRequest();
        invite.setFirstName("TestWorker");
        invite.setLastName("Integration");
        invite.setEmail(workerEmail);
        invite.setPassword("WorkerPass123");
        invite.setTrade("General Labor");

        MvcResult inviteResult = mockMvc.perform(post("/api/auth/invite-worker")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invite)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("WORKER"))
                .andReturn();

        JsonNode inviteResp = objectMapper.readTree(inviteResult.getResponse().getContentAsString());
        workerToken = inviteResp.get("accessToken").asText();

        // Find the worker profile ID by matching the unique email we just used
        MvcResult workersResult = mockMvc.perform(get("/api/workers")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode workersPage = objectMapper.readTree(workersResult.getResponse().getContentAsString());
        JsonNode workersArr = workersPage.get("content");
        for (int i = 0; i < workersArr.size(); i++) {
            JsonNode w = workersArr.get(i);
            if (w.has("email") && !w.get("email").isNull()
                    && w.get("email").asText().equals(workerEmail)) {
                workerProfileId = w.get("id").asLong();
                break;
            }
        }

        // Create two jobs: one to assign the worker to, one not assigned
        String assignedJobJson = "{\"title\":\"Assigned Worker Job\"}";
        MvcResult assignedJobResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignedJobJson))
                .andExpect(status().isOk())
                .andReturn();
        assignedJobId = objectMapper.readTree(
                assignedJobResult.getResponse().getContentAsString()).get("id").asLong();

        String unassignedJobJson = "{\"title\":\"Unassigned Worker Job\"}";
        MvcResult unassignedJobResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unassignedJobJson))
                .andExpect(status().isOk())
                .andReturn();
        unassignedJobId = objectMapper.readTree(
                unassignedJobResult.getResponse().getContentAsString()).get("id").asLong();

        // Assign worker to one job
        CrewAssignmentRequest crewReq = new CrewAssignmentRequest();
        crewReq.setWorkerId(workerProfileId);
        crewReq.setRoleOnJob("Laborer");
        crewReq.setStartDate(LocalDate.now());

        mockMvc.perform(post("/api/jobs/" + assignedJobId + "/crew")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crewReq)))
                .andExpect(status().isOk());
    }

    // ── Job visibility ──

    @Test
    void worker_canSeeAssignedJobs() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode page = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode jobs = page.get("content");

        // Worker should see their assigned job
        boolean foundAssigned = false;
        boolean foundUnassigned = false;
        for (int i = 0; i < jobs.size(); i++) {
            long id = jobs.get(i).get("id").asLong();
            if (id == assignedJobId) foundAssigned = true;
            if (id == unassignedJobId) foundUnassigned = true;
        }

        org.junit.jupiter.api.Assertions.assertTrue(foundAssigned,
                "Worker should see assigned job");
        org.junit.jupiter.api.Assertions.assertFalse(foundUnassigned,
                "Worker should NOT see unassigned job");
    }

    @Test
    void worker_jobsDoNotIncludeFinancials() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode page = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode jobs = page.get("content");
        if (jobs.size() > 0) {
            // Financial fields should be null for workers
            JsonNode job = jobs.get(0);
            org.junit.jupiter.api.Assertions.assertTrue(job.get("laborCost").isNull(), "Worker should not see laborCost");
            org.junit.jupiter.api.Assertions.assertTrue(job.get("profit").isNull(), "Worker should not see profit");
        }
    }

    // ── Admin-only endpoints blocked ──

    @Test
    void worker_cannotCreateJob() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Worker Created Job\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void worker_cannotUpdateJob() throws Exception {
        mockMvc.perform(put("/api/jobs/" + assignedJobId)
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Modified by Worker\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void worker_cannotDeleteJob() throws Exception {
        mockMvc.perform(delete("/api/jobs/" + assignedJobId)
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void worker_cannotListWorkers() throws Exception {
        mockMvc.perform(get("/api/workers")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void worker_cannotCreateWorker() throws Exception {
        mockMvc.perform(post("/api/workers")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Hacker\",\"lastName\":\"Worker\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void worker_cannotCreateMaterial() throws Exception {
        mockMvc.perform(post("/api/jobs/" + assignedJobId + "/materials")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Stolen Material\",\"quantity\":1,\"unitCost\":100}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void worker_cannotCreatePermit() throws Exception {
        mockMvc.perform(post("/api/jobs/" + assignedJobId + "/permits")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permitType\":\"Fake Permit\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void worker_cannotAssignCrew() throws Exception {
        mockMvc.perform(post("/api/jobs/" + assignedJobId + "/crew")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workerId\":" + workerProfileId + "}"))
                .andExpect(status().isForbidden());
    }

    // ── Allowed worker actions ──

    @Test
    void worker_canViewPermits() throws Exception {
        mockMvc.perform(get("/api/jobs/" + assignedJobId + "/permits")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void worker_canViewCrew() throws Exception {
        mockMvc.perform(get("/api/jobs/" + assignedJobId + "/crew")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void worker_canCreateTimeEntry() throws Exception {
        TimeEntryRequest req = new TimeEntryRequest();
        req.setWorkerId(workerProfileId);
        req.setEntryDate(LocalDate.now());
        req.setHours(new BigDecimal("8.00"));
        req.setNotes("Worker's own time entry");

        mockMvc.perform(post("/api/jobs/" + assignedJobId + "/time-entries")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Worker's own time entry"));
    }

    @Test
    void worker_canCreateNote() throws Exception {
        JobNoteRequest req = new JobNoteRequest();
        req.setContent("Worker's note on the job");
        req.setVisibility(NoteVisibility.SHARED);

        mockMvc.perform(post("/api/jobs/" + assignedJobId + "/notes")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Worker's note on the job"));
    }

    @Test
    void worker_canViewTimeEntries() throws Exception {
        mockMvc.perform(get("/api/jobs/" + assignedJobId + "/time-entries")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void worker_canViewNotes() throws Exception {
        mockMvc.perform(get("/api/jobs/" + assignedJobId + "/notes")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void worker_canAccessMe() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("WORKER"));
    }
}
