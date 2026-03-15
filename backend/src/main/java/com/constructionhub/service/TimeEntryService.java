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
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final JobRepository jobRepository;
    private final WorkerRepository workerRepository;

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getJobTimeEntries(Long jobId, Long organizationId) {
        jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        return timeEntryRepository.findByJobIdOrderByEntryDateDesc(jobId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getWorkerTimeEntries(Long workerId) {
        return timeEntryRepository.findByWorkerIdOrderByEntryDateDesc(workerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public TimeEntryResponse createTimeEntry(Long jobId, TimeEntryRequest request, User currentUser, Long organizationId) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        Worker worker = workerRepository.findByIdAndOrganizationId(request.getWorkerId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker", request.getWorkerId()));

        TimeEntry entry = TimeEntry.builder()
                .job(job)
                .worker(worker)
                .entryDate(request.getEntryDate())
                .hours(request.getHours())
                .clockIn(request.getClockIn())
                .clockOut(request.getClockOut())
                .enteredBy(currentUser)
                .notes(request.getNotes())
                .build();

        entry = timeEntryRepository.save(entry);
        return mapToResponse(entry);
    }

    @Transactional
    public TimeEntryResponse updateTimeEntry(Long entryId, TimeEntryRequest request, User currentUser, Long organizationId) {
        TimeEntry entry = timeEntryRepository.findByIdAndOrganizationId(entryId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry", entryId));

        entry.setEntryDate(request.getEntryDate());
        entry.setHours(request.getHours());
        entry.setClockIn(request.getClockIn());
        entry.setClockOut(request.getClockOut());
        entry.setNotes(request.getNotes());

        entry = timeEntryRepository.save(entry);
        return mapToResponse(entry);
    }

    @Transactional
    public void deleteTimeEntry(Long entryId, Long organizationId) {
        timeEntryRepository.findByIdAndOrganizationId(entryId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry", entryId));
        timeEntryRepository.deleteById(entryId);
    }

    private TimeEntryResponse mapToResponse(TimeEntry entry) {
        return TimeEntryResponse.builder()
                .id(entry.getId())
                .jobId(entry.getJob().getId())
                .jobTitle(entry.getJob().getTitle())
                .workerId(entry.getWorker().getId())
                .workerName(entry.getWorker().getFullName())
                .entryDate(entry.getEntryDate())
                .hours(entry.getHours())
                .clockIn(entry.getClockIn())
                .clockOut(entry.getClockOut())
                .enteredByName(entry.getEnteredBy().getFullName())
                .notes(entry.getNotes())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
