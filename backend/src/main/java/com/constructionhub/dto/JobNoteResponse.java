package com.constructionhub.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class JobNoteResponse {
    private Long id;
    private Long jobId;
    private String authorName;
    private Long authorId;
    private String content;
    private String visibility;
    private LocalDateTime createdAt;
}
