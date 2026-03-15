package com.constructionhub.repository;

import com.constructionhub.entity.JobNote;
import com.constructionhub.entity.NoteVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobNoteRepository extends JpaRepository<JobNote, Long> {
    List<JobNote> findByJobIdOrderByCreatedAtDesc(Long jobId);
    List<JobNote> findByJobIdAndVisibilityOrderByCreatedAtDesc(Long jobId, NoteVisibility visibility);
}
