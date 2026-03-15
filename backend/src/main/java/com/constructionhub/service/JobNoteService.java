package com.constructionhub.service;

import com.constructionhub.dto.*;
import com.constructionhub.entity.*;
import com.constructionhub.exception.ResourceNotFoundException;
import com.constructionhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobNoteService {

    private final JobNoteRepository jobNoteRepository;
    private final JobRepository jobRepository;

    @Transactional(readOnly = true)
    public List<JobNoteResponse> getJobNotes(Long jobId, boolean includeOwnerOnly, Long organizationId) {
        jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        List<JobNote> notes;
        if (includeOwnerOnly) {
            notes = jobNoteRepository.findByJobIdOrderByCreatedAtDesc(jobId);
        } else {
            notes = jobNoteRepository.findByJobIdAndVisibilityOrderByCreatedAtDesc(jobId, NoteVisibility.SHARED);
        }
        return notes.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public JobNoteResponse createNote(Long jobId, JobNoteRequest request, User currentUser, Long organizationId) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        // Workers can only create SHARED notes
        NoteVisibility visibility = request.getVisibility();
        if (currentUser.getRole() == UserRole.WORKER) {
            visibility = NoteVisibility.SHARED;
        }
        if (visibility == null) {
            visibility = NoteVisibility.SHARED;
        }

        JobNote note = JobNote.builder()
                .job(job)
                .author(currentUser)
                .content(request.getContent())
                .visibility(visibility)
                .build();

        note = jobNoteRepository.save(note);
        return mapToResponse(note);
    }

    private JobNoteResponse mapToResponse(JobNote note) {
        return JobNoteResponse.builder()
                .id(note.getId())
                .jobId(note.getJob().getId())
                .authorName(note.getAuthor().getFullName())
                .authorId(note.getAuthor().getId())
                .content(note.getContent())
                .visibility(note.getVisibility().name())
                .createdAt(note.getCreatedAt())
                .build();
    }
}
