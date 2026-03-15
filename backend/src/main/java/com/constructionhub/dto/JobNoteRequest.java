package com.constructionhub.dto;

import com.constructionhub.entity.NoteVisibility;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobNoteRequest {
    @NotBlank(message = "Content is required")
    private String content;
    private NoteVisibility visibility;
}
