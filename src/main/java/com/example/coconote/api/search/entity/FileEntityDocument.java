package com.example.coconote.api.search.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntityDocument {
    @Id
    private String fileId;  // OpenSearch 문서 ID
    private String fileName;
    private String fileUrl;
    private Long folderId;
    private Long creatorId;
    private Long channelId;
    private Boolean isDeleted;
    private LocalDateTime deletedTime;
}
